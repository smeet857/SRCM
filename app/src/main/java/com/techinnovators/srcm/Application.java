package com.techinnovators.srcm;

import android.content.Context;

import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.models.UserModel;
import com.techinnovators.srcm.utils.LocationUtils;

public class Application extends android.app.Application {

    private static UserModel userModel;
    public static Context context;

    public static UserModel getUserModel(){
        return userModel == null ? new UserModel() : userModel;
    }
    public static void setUserModel (UserModel newValue){
        userModel = newValue;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        LocationUtils.init();
    }
}