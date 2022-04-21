package com.techinnovators.srcm.Database.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.techinnovators.srcm.models.Tasks;

import java.util.List;

@Dao
public interface TasksDao {
    @Query("SELECT * FROM Tasks")
    List<Tasks> getAll();

    @Query("SELECT * FROM Tasks WHERE isSync = 0 OR isCheckOutSync = 0 OR isCheckInSync = 0")
    List<Tasks> getNotSyncData();

    @Query("SELECT * FROM Tasks WHERE id IN (:taskId)")
    List<Tasks> getById(int[] taskId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Tasks> tasks);

    @Insert
    void insert(Tasks tasks);

    @Update
    void update(Tasks tasks);

    @Query("DELETE FROM Tasks")
    void deleteAll();
}

