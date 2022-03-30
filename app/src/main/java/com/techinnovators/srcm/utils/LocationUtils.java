package com.techinnovators.srcm.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.R;

public class LocationUtils {
    private static FusedLocationProviderClient fusedLocationProviderClient;
    private static LocationManager locationManager;

    public static void init() {
    }

    public static void getCurrentLocation(Context context, GpsCallback gpsCallback) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {


            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtils.requestLocationPermission((Activity) context);
                return;
            }
            final Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            gpsCallback.onSuccess(location);

//            if (PermissionUtils.hasLocationPermission()) {
//
////                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
////                    @Override
////                    public void onComplete(@NonNull Task<Location> task) {
////                        if (task.isSuccessful()) {
////                            gpsCallback.onSuccess(task.getResult());
////                        } else {
////                            LocationRequest locationRequest = LocationRequest.create();
////                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
////                            locationRequest.setInterval(30 * 1000);
////                            locationRequest.setFastestInterval(5 * 1000);
////                            locationRequest.setNumUpdates(1);
////
////                            LocationCallback locationCallback = new LocationCallback() {
////                                @Override
////                                public void onLocationResult(LocationResult locationResult) {
////                                    super.onLocationResult(locationResult);
////                                    gpsCallback.onSuccess(locationResult.getLastLocation());
////                                }
////                            };
////                        }
////                    }
////                });
//            }else{
//                PermissionUtils.requestLocationPermission((Activity) context);
//            }
        } else {
            new AlertDialog.Builder(context).setTitle(R.string.location_title).
                    setMessage(R.string.location_service_msg).
                    setPositiveButton("SETTINGS", (dialogInterface, i) ->
                            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                    .setCancelable(false).show();
        }
    }
}

