package com.techinnovators.srcm.Database.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.techinnovators.srcm.models.Tasks;

import java.util.List;

@Dao
public interface TasksDao {
    @Query("SELECT * FROM Tasks")
    List<Tasks> getAll();

    @Query("SELECT * FROM Tasks WHERE id IN (:taskId)")
    List<Tasks> getById(int[] taskId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Tasks> tasks);

    @Insert
    void insert(Tasks tasks);

    @Query("DELETE FROM Tasks")
    void deleteAll();
}

