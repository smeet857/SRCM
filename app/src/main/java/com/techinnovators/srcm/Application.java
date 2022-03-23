package com.techinnovators.srcm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

public class Application extends android.app.Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
         context= getApplicationContext();
    }

    public static Context getAppContext() {
        return context;
    }
}