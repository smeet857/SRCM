package com.techinnovators.srcm.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

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

    public static void init(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Application.context);
        locationManager = (LocationManager) Application.context.getSystemService(Context.LOCATION_SERVICE);
    }

    public static void getCurrentLocation(Context context,GpsCallback gpsCallback) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){

            if(PermissionUtils.hasLocationPermission()){
               fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            gpsCallback.onSuccess(task.getResult());
                        } else {
                            LocationRequest locationRequest = LocationRequest.create();
                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                            locationRequest.setInterval(30 * 1000);
                            locationRequest.setFastestInterval(5 * 1000);
                            locationRequest.setNumUpdates(1);
                            LocationCallback locationCallback = new LocationCallback() {
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    gpsCallback.onSuccess(locationResult.getLastLocation());
                                }
                            };
//                            if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                                    ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                                // TODO: Consider calling
//                                //    ActivityCompat#requestPermissions
//                                // here to request the missing permissions, and then overriding
//                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                //                                          int[] grantResults)
//                                // to handle the case where the user grants the permission. See the documentation
//                                // for ActivityCompat#requestPermissions for more details.
//                                return;
//                            }
//                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        }
                    }
                });
            }else{
                PermissionUtils.requestLocationPermission((Activity) context);
            }
        } else {
            new AlertDialog.Builder(Application.context).setTitle(R.string.location_title).
                    setMessage(R.string.location_service_msg).
                    setPositiveButton("SETTINGS", (dialogInterface, i) ->
                            Application.context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                    .setCancelable(false).show();
        }
    }
}

interface GpsCallback{
    void onSuccess(Location location);
}
