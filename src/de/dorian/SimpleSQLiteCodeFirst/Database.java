package de.dorian.SimpleSQLiteCodeFirst;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        for (Field field : fields){
            Object value = field.get(entity);
            if (value instanceof Integer || value instanceof String || value instanceof Double || value instanceof Float){
                valueList.add(value);
                columns += "[" + field.getName() + "],";
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
                if (value instanceof String){
                    statement.setString(i + 1, (String)value);
                } else if (value instanceof Integer){
                    statement.setInt(i + 1, (Integer)value);
                } else if (value instanceof Float) {
                    statement.setFloat(i + 1, (Float)value);
                } else if (value instanceof Double) {
                    statement.setDouble(i + 1, (Double)value);
                }
            }
            statement.executeUpdate();
            statement.close();
            entity.setDatabaseId(database.getLastInsertRowId());
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
                updateString += String.format("[%s] = %s, ", field.getName(), value.toString());
            } else if (value instanceof String){
                updateString += String.format("%s = '%s', ", field.getName(), value.toString());
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
                }
            }
            entities.add(entity);
        }

        return entities;
    }

    public void createTable(Class entityClass) throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS [%s] ([databaseId] INTEGER PRIMARY KEY AUTOINCREMENT %s);";
        String otherColumns = "";
        Field[] fields = entityClass.getFields();
        for (Field field : fields){
            Class fieldClass = field.getType();
            String fieldName = field.getName();
            if (fieldClass.equals(int.class)){
                otherColumns += ", [" + fieldName + "] INT NOT NULL";
            } else if (fieldClass.equals(String.class)){
                otherColumns += ", [" + fieldName + "] TEXT NOT NULL";
            } else if (fieldClass.equals(float.class) || fieldClass.equals(double.class)){
                otherColumns += ", [" + fieldName + "] REAL NOT NULL";
            }
        }
        query = String.format(query, entityClass.getName(), otherColumns);
        database.executeUpdate(query);
    }

    public void test(){
        try {
            Class entityClass = Class.forName("de.dorian.SimpleSQLiteCodeFirst.TestEntity");
            Field[] fields =  entityClass.getFields();
            Object entity = entityClass.newInstance();
            for (Field field : fields){
                Object value = field.get(entity);
                System.out.println(value);
            }
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    public void close() throws SQLException {
        database.close();
    }
}