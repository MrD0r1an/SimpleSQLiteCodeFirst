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
            entity.test = "j";
            entity.test2 = 2;
            TestEntity2 entity2 = new TestEntity2();
            entity2.entity = entity;
            database.deleteTable(TestEntity.class);
            database.deleteTable(TestEntity2.class);
            database.createTable(TestEntity2.class);
            database.addEntity(entity2);
            entity.test = "changed";
            database.updateEntity(entity);
            List<TestEntity2> entities = database.getEntities(TestEntity2.class);
            for (TestEntity2 e : entities){
                System.out.println(String.format("id: %d string: %s entity: id: %d string: %s int: %d date: %s", e.getDatabaseId(), e.a, e.entity.getDatabaseId(), e.entity.test, e.entity.test2, e.entity.date.toString()));
            }
            database.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
