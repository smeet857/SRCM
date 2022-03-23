package com.techinnovators.srcm.utils;

import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.techinnovators.srcm.Application;

class PermissionUtils {
    public static void askLocationPermission() {

    }

    public static boolean checkLocationPermission() {
        return
                ContextCompat.checkSelfPermission(Application.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(Application.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
