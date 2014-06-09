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
            entity.setDatabaseId(-1);
            entity.test = "klö";
            entity.test2 = 90;
            //database.updateEntity(entity);
            //database.createTable(TestEntity.class);
            //database.addEntity(entity);
            List<TestEntity> entities = database.getEntities(TestEntity.class);
            for (TestEntity e : entities){
                System.out.println(e.test +" "+ e.getDatabaseId());
            }
            database.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
