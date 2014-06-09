package de.dorian.SimpleSQLiteCodeFirst;

import java.util.List;

/**
 * Created by Rudolph on 06.06.14. ölhkl
 */
public class Test {
    public static void main(String[] args){
        try {
            Database database = new Database("test");
            TestEntity entity = new TestEntity();
            database.deleteTable(TestEntity.class);
            database.createTable(TestEntity.class);
            database.addEntity(entity);
            List<TestEntity> entities = database.getEntities(TestEntity.class);
            for (TestEntity e : entities){
                System.out.println(String.format("id: %d string: %s int: %d date: %s", e.getDatabaseId(), e.test, e.test2, e.date.toString()));
            }
            database.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
