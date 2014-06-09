package de.dorian.SimpleSQLiteCodeFirst;

/**
 * Created by Rudolph on 07.06.14.
 */
public class BaseEntity {
    private long databaseId = -1;

    public final long getDatabaseId(){
        return databaseId;
    }

    public final void setDatabaseId(long databaseId){
        this.databaseId = databaseId;
    }
}
