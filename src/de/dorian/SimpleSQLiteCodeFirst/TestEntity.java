package de.dorian.SimpleSQLiteCodeFirst;

import java.sql.Date;

/**
 * Created by Rudolph on 06.06.14.
 */
public class TestEntity extends BaseEntity {
    public String test = "hallo";
    public int test2 = 43;
    public Date date = Date.valueOf("1997-7-17");
}
