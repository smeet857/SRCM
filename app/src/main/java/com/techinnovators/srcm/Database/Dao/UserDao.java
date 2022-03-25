package com.techinnovators.srcm.Database.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.UserModel;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM User")
    List<UserModel> getAll();

    @Insert
    void insert(UserModel userModel);

    @Delete
    void delete(UserModel userModel);
}