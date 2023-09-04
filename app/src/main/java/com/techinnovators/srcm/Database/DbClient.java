package com.techinnovators.srcm.Database;


import android.content.Intent;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.techinnovators.srcm.Activity.LoginActivity;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.Dao.TasksDao;
import com.techinnovators.srcm.Database.Dao.UserDao;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.UserModel;

@Database(entities = {Tasks.class, UserModel.class}, version = 7, exportSchema = false)
public abstract class DbClient extends RoomDatabase {
    public abstract TasksDao tasksDao();
    public abstract UserDao userDao();

    private static DbClient instance;

    public static synchronized DbClient getInstance(){
        if(instance == null){
            instance = Room.databaseBuilder(Application.context, DbClient.class, "srcm.db").addMigrations(MIGRATION_1_2).allowMainThreadQueries()
                    .fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    static final Migration MIGRATION_1_2 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            /*try {
                Application.isLogin = false;
                instance.clearAllTables();
                Intent intent = new Intent(Application.context, LoginActivity.class);
                Application.context.startActivity(intent);
            } catch (Exception e) {
                Log.e("Migration Error => " , e.toString());
                Intent intent = new Intent(Application.context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Application.context.startActivity(intent);
            }*/
        }
    };
}



