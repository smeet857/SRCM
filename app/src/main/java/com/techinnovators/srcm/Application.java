package com.techinnovators.srcm;

import android.content.Context;

import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.models.UserModel;
import com.techinnovators.srcm.utils.LocationUtils;
import com.techinnovators.srcm.utils.NetworkUtils;

import java.util.ArrayList;

public class Application extends android.app.Application {

    private static UserModel userModel;
    public static Context context;
    public static boolean isLogin = false;

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
        checkLogin();
    }

    private void checkLogin(){
        ArrayList<UserModel> data = (ArrayList<UserModel>) DbClient.getInstance().userDao().getAll();

        if(data.size() > 0){
            Application.setUserModel(data.get(0));
            isLogin = true;
        }else{
            isLogin = false;
        }
    }
}