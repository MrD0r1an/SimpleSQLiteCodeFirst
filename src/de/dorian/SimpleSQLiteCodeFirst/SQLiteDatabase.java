package de.dorian.SimpleSQLiteCodeFirst;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudolph on 07.06.14.
 */
public class SQLiteDatabase implements ISQLDatabase{
    private Connection connection;

    public SQLiteDatabase(String databaseName) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseName + ".db");
        connection.setAutoCommit(true);
    }

    @Override
    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    @Override
    public void executeUpdate(String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
    }

    @Override
    public long getLastInsertRowId() throws SQLException {
        ResultSet resultSet = executeQuery("SELECT last_insert_rowid()");
        return resultSet.getLong(1);
    }

    @Override
    public PreparedStatement prepareStatement(String query) throws SQLException {
        return connection.prepareStatement(query);
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
