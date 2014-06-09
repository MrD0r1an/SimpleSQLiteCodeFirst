package de.dorian.SimpleSQLiteCodeFirst;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Rudolph on 07.06.14.
 */
public interface ISQLDatabase {
    public ResultSet executeQuery(String query) throws SQLException;
    public void executeUpdate(String query) throws SQLException;
    public long getLastInsertRowId() throws SQLException;
    public PreparedStatement prepareStatement(String query) throws SQLException;
    public void close() throws SQLException;
}
