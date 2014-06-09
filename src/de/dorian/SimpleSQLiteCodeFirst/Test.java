package de.dorian.SimpleSQLiteCodeFirst;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rudolph on 06.06.14. ölhkl
 */
public class Test {
    public static void main(String[] args){
        test3();
    }

    private static void test3(){
        try {
            Database database = new Database("test3");
            database.createTable(TestEntity3.class);
            TestEntity3 entity3 = new TestEntity3();
            entity3.strings.add("asdf");
            entity3.strings.add("asdfgash");
            entity3.strings.add("kaöjkl");
            database.addEntity(entity3);
            database.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void test(){
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

    private static void test2(){
        List<TestEntity> list = new ArrayList<TestEntity>();
        Class<?> c = list.getClass();
        System.out.println(c);
        System.out.println(List.class.isAssignableFrom(c));
    }
}
