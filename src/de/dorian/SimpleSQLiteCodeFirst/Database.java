package de.dorian.SimpleSQLiteCodeFirst;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by Rudolph on 06.06.14.
 */
public class Database {
    SQLiteDatabase database;

    public Database(String databaseName) throws SQLException, ClassNotFoundException {
        database = new SQLiteDatabase(databaseName);
    }

    public void addEntity(BaseEntity entity) throws IllegalAccessException, SQLException {
        if (entity.getDatabaseId() != -1) return;

        String query = "INSERT INTO [%s](%s) VALUES (%s)";
        String columns = "";
        String values = "?";

        Class entityClass = entity.getClass();
        Field[] fields =  entityClass.getFields();
        List valueList = new ArrayList();
        List<Field> fieldsToSaveLater = new ArrayList<Field>();
        for (Field field : fields){
            Object value = field.get(entity);
            if (value instanceof Integer || value instanceof String || value instanceof Double || value instanceof Float || value instanceof Date || value instanceof BaseEntity || value instanceof Long){
                valueList.add(value);
                columns += "[" + field.getName() + "],";
            } else if (isSavableList(field)){
                fieldsToSaveLater.add(field);
            }
        }

        if (valueList.size() > 0){
            for (int i = 1; i < valueList.size(); ++i){
                values += ", ?";
            }
            columns = columns.substring(0, columns.length() - 1);
            query = String.format(query, entityClass.getName(), columns, values);
            System.out.println(query);
            PreparedStatement statement = database.prepareStatement(query);
            for (int i = 0; i < valueList.size(); ++i){
                Object value = valueList.get(i);
                setStatementValue(statement, value, i + 1);
            }
            statement.executeUpdate();
            statement.close();
            entity.setDatabaseId(database.getLastInsertRowId());

            for (Field field : fieldsToSaveLater){
                saveList(entity, field, field.get(entity));
            }
        }
    }

    private void setStatementValue(PreparedStatement statement, Object value, int index) throws SQLException, IllegalAccessException {
        if (value instanceof String){
            statement.setString(index, (String)value);
        } else if (value instanceof Integer){
            statement.setInt(index, (Integer)value);
        } else if (value instanceof Float) {
            statement.setFloat(index, (Float)value);
        } else if (value instanceof Double) {
            statement.setDouble(index, (Double)value);
        } else if (value instanceof Date){
            statement.setLong(index, ((Date)value).getTime());
        } else if (value instanceof BaseEntity){
            BaseEntity e = (BaseEntity)value;
            createTable(value.getClass());
            if (e.getDatabaseId() == -1){
                addEntity(e);
            } else {
                updateEntity(e);
            }
            statement.setLong(index, e.getDatabaseId());
        }
    }

    public void updateEntity(BaseEntity entity) throws IllegalAccessException, SQLException {
        if (entity.getDatabaseId() == -1) return;

        String query = "UPDATE [%s] SET %s WHERE databaseId = " + entity.getDatabaseId();
        String updateString = "";

        Class entityClass = entity.getClass();
        Field[] fields =  entityClass.getFields();
        for (Field field : fields){
            Object value = field.get(entity);
            if (value instanceof Integer || value instanceof Double || value instanceof Float){
                updateString += String.format("[%s] = %s, ", field.getName(), NumberFormat.getInstance(Locale.ENGLISH).format(value));
            } else if (value instanceof String){
                updateString += String.format("%s = '%s', ", field.getName(), value.toString());
            } else if (value instanceof Date){
                updateString += String.format("[%s] = %s, ", field.getName(), ((Date)value).getTime() + "");
            } else if (value instanceof BaseEntity){
                BaseEntity e = (BaseEntity)value;
                createTable(value.getClass());
                if (e.getDatabaseId() == -1){
                    addEntity(e);
                } else {
                    updateEntity(e);
                }
                updateString += String.format("[%s] = %s, ", field.getName(), e.getDatabaseId() + "");
            } else if (isSavableList(field)){
                saveList(entity, field, value);
            }
        }

        if (updateString.length() > 0){
            updateString = updateString.substring(0, updateString.length() - 2);
            query = String.format(query, entityClass.getName(), updateString);
            System.out.println(query);
            PreparedStatement statement = database.prepareStatement(query);
            System.out.println(statement.executeUpdate());
            statement.close();
        }
    }

    public <T extends BaseEntity> List<T> getEntities(Class<T> entityClass) throws SQLException, IllegalAccessException, InstantiationException {
        List<T> entities = new ArrayList<T>();

        String query = "SELECT * FROM [" + entityClass.getName() + "]";
        System.out.println(query);
        ResultSet resultSet = database.executeQuery(query);

        while(resultSet.next()){
            T entity = getEntity(entityClass, resultSet);
            entities.add(entity);
        }

        return entities;
    }

    private <T extends BaseEntity> T getEntity(Class<T> entityClass, ResultSet resultSet) throws IllegalAccessException, InstantiationException, SQLException {
        T entity = entityClass.newInstance();
        entity.setDatabaseId(resultSet.getLong(1));
        Field[] fields = entityClass.getFields();
        for (Field field : fields){
            Class fieldClass = field.getType();
            if (fieldClass.equals(int.class)){
                field.set(entity, resultSet.getInt(field.getName()));
            } else if (fieldClass.equals(String.class)){
                field.set(entity, resultSet.getString(field.getName()));
            } else if (fieldClass.equals(float.class)){
                field.set(entity, resultSet.getFloat(field.getName()));
            } else if (fieldClass.equals(double.class)){
                field.set(entity, resultSet.getDouble(field.getName()));
            } else if (fieldClass.equals(Date.class)){
                field.set(entity, new Date(resultSet.getLong(field.getName())));
            } else if (BaseEntity.class.isAssignableFrom(fieldClass)){
                long id = resultSet.getLong(field.getName());
                Object e = getEntityById(fieldClass, id);
                field.set(entity, e);
            }
        }
        return entity;
    }

    private boolean isSavableList(Field field){
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType && List.class.isAssignableFrom(field.getType())){
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            if (parameterizedType.getActualTypeArguments().length == 1){
                Class entityClass = (Class)parameterizedType.getActualTypeArguments()[0];
                if (entityClass.equals(String.class) || entityClass.equals(double.class) || entityClass.equals(float.class) || entityClass.equals(int.class) || entityClass.equals(Date.class) || BaseEntity.class.isAssignableFrom(entityClass) || entityClass.equals(long.class)){
                    return true;
                }
            }
        }

        return false;
    }

    private void saveList(BaseEntity entity, Field field, Object value) throws SQLException, IllegalAccessException {
        createListTable(entity.getClass(), field);
        String tableName = getListTableName(entity.getClass(), field);

        String deleteQuery = "DELETE FROM %s WHERE [%s] = " + entity.getDatabaseId();
        deleteQuery = String.format(deleteQuery, tableName, entity.getClass().getName());
        System.out.println(deleteQuery);
        database.executeUpdate(deleteQuery);

        String insertQuery = "INSERT INTO %s ([%s], %s) VALUES (?, ?)";
        insertQuery = String.format(insertQuery, tableName, entity.getClass().getName(), field.getName());
        System.out.println(insertQuery);
        PreparedStatement statement = database.prepareStatement(insertQuery);
        statement.setLong(1, entity.getDatabaseId());
        List list = (List)value;
        for (Object item : list){
            setStatementValue(statement, item, 2);
            statement.executeUpdate();
        }
    }

    public <T extends BaseEntity> T getEntityById(Class<T> entityClass, long databaseId) throws SQLException, InstantiationException, IllegalAccessException {
        String query = "SELECT * FROM [" + entityClass.getName() + "] WHERE databaseId = " + databaseId;
        System.out.println(query);
        ResultSet resultSet = database.executeQuery(query);
        return getEntity(entityClass, resultSet);
    }

    public void createTable(Class entityClass) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS [%s] ([databaseId] INTEGER PRIMARY KEY AUTOINCREMENT %s);";
        String otherColumns = "";
        Field[] fields = entityClass.getFields();
        for (Field field : fields){
            Class fieldClass = field.getType();
            String fieldName = field.getName();
            String sqlType = getSQLType(fieldClass);
            if (sqlType != null){
                otherColumns += String.format(", [%s] %s NOT NULL", fieldName, sqlType);
            }
        }
        query = String.format(query, entityClass.getName(), otherColumns);
        System.out.println(query);
        database.executeUpdate(query);
    }

    public void deleteTable(Class entityClass) throws SQLException {
        String query = "DROP TABLE IF EXISTS [%s];";
        query = String.format(query, entityClass.getName());
        System.out.println(query);
        database.executeUpdate(query);
    }

    private String getAssociationTableName(Class class1, Class class2){
        return String.format("[association:%s,%s]", class1.getName(), class2.getName());
    }

    public void createAssociationTable(Class class1, Class class2) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS %s ([%s] INTEGER NOT NULL, [%s] INTEGER NOT NULL)";
        query = String.format(String.format(query, getAssociationTableName(class1, class2), class1.getName(), class2.getName()));
        System.out.println(query);
        database.executeUpdate(query);
    }

    public void deleteAssociationTable(Class class1, Class class2) throws SQLException {
        String query = "DROP TABLE IF EXISTS %s";
        query = String.format(query, getAssociationTableName(class1, class2));
        System.out.println(query);
        database.executeUpdate(query);
    }

    private String getListTableName(Class entityClass, Field listField){
        return String.format("[list:%s,%s]", entityClass.getName(), listField.getName());
    }

    public void createListTable(Class entityClass, Field listField) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS %s ([%s] INTEGER NOT NULL, [%s] %s NOT NULL)";

        Type genericType = listField.getGenericType();
        if (genericType instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Class fieldClass = (Class)parameterizedType.getActualTypeArguments()[0];
            String sqlType = getSQLType(fieldClass);
            query = String.format(query, getListTableName(entityClass, listField), entityClass.getName(), listField.getName(), sqlType);
            System.out.println(query);
            database.executeUpdate(query);
        }
    }

    public void deleteListTable(Class entityClass, Field listField) throws SQLException {
        String query = "DROP TABLE IF EXISTS %s";
        query = String.format(query, getListTableName(entityClass, listField));
        System.out.println(query);
        database.executeUpdate(query);
    }

    private String getSQLType(Class fieldClass){
        if (fieldClass.equals(String.class)){
            return "TEXT";
        } else if (fieldClass.equals(float.class) || fieldClass.equals(double.class)){
            return "REAL";
        } else if (fieldClass.equals(int.class)){
            return "INT";
        } else if (fieldClass.equals(long.class) || fieldClass.equals(Date.class) || BaseEntity.class.isAssignableFrom(fieldClass)){
            return "LONG";
        }
        return null;
    }

    public void deleteEntity(BaseEntity entity) throws SQLException {
        if (entity.getDatabaseId() == -1) return;
        String query = "DELETE FROM [%s] where [databaseId] = " + entity.getDatabaseId();
        query = String.format(query, entity.getClass().getName());
        System.out.println(query);
        database.executeUpdate(query);
    }

    public void close() throws SQLException {
        database.close();
    }
}
