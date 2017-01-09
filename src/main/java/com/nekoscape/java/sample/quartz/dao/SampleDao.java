package com.nekoscape.java.sample.quartz.dao;

public interface SampleDao {

    void saveEntity(int id, String text);

    void findEntity(int id);

    void deleteEntity(int id);
}
