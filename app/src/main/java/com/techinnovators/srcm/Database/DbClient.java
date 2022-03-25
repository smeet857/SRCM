package com.techinnovators.srcm.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.Dao.TasksDao;
import com.techinnovators.srcm.Database.Dao.UserDao;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.UserModel;

@Database(entities = {Tasks.class, UserModel.class}, version = 1)
public abstract class DbClient extends RoomDatabase {
    public abstract TasksDao tasksDao();
    public abstract UserDao userDao();

    private static DbClient instance;

    public static synchronized DbClient getInstance(){
        if(instance == null){
            instance = Room.databaseBuilder(Application.context, DbClient.class, "srcm.db").allowMainThreadQueries().build();
        }
        return instance;
    }
}



