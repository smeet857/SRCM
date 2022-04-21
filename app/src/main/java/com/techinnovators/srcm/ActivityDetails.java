package com.techinnovators.srcm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.techinnovators.srcm.Activity.TaskActivity2;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.utils.SharedPreferencesManager;

public class ActivityDetails extends AppCompatActivity {
    private static final int LOCATION_PERMISSIONS_CODE = 100;
    FusedLocationProviderClient fusedLocationProviderClient;
    String msLat, msLong;

    private boolean checkAllPermission() {
        int result_location = ContextCompat.checkSelfPermission(ActivityDetails.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result_location_coarse = ContextCompat.checkSelfPermission(ActivityDetails.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return
                result_location == PackageManager.PERMISSION_GRANTED &&
                        result_location_coarse == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissions() {
        ActivityCompat.requestPermissions(ActivityDetails.this, new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSIONS_CODE);

    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        msLat = String.valueOf(location.getLatitude());
                        msLong = String.valueOf(location.getLongitude());
                    } else {
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setInterval(30 * 1000);
                        locationRequest.setFastestInterval(5 * 1000);
                        locationRequest.setNumUpdates(1);
                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                Location location = locationResult.getLastLocation();
                                msLat = String.valueOf(location.getLatitude());
                                msLong = String.valueOf(location.getLongitude());
                                super.onLocationResult(locationResult);
                            }
                        };
                        if (ActivityCompat.checkSelfPermission(ActivityDetails.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(ActivityDetails.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

                    }
                }
            });
        } else {
            new AlertDialog.Builder(ActivityDetails.this).setTitle(R.string.location_title).setMessage(R.string.location_service_msg).setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }).setCancelable(false).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_CODE && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation();
        } else {
            new AlertDialog.Builder(ActivityDetails.this).setTitle(R.string.location_title).setMessage(R.string.location_service_msg).setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }).setCancelable(false)
                    .show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ActivityDetails.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkAllPermission()) {
                showPermissions();
                if (ActivityCompat.checkSelfPermission(
                        ActivityDetails.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(ActivityDetails.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
            } else {
                getCurrentLocation();
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tvTitle = toolbar.findViewById(R.id.tvVisitRequest);
        String strVisitRequest = getIntent().getStringExtra(getString(R.string.intent_visit_request_key));
        if (!strVisitRequest.isEmpty()) {
            tvTitle.setText(strVisitRequest);
        } else {
            tvTitle.setText(R.string.ACTIVITY_DETAILS_TITLE);
        }
        String strActivityName = "Hello World";

        AppCompatButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(view -> {
            if (NetworkUtils.isNetworkConnected(ActivityDetails.this)) {
                submitActivity();
            } else {
                saveActivityToPreferences(strActivityName);
            }
        });
        ImageView ivBack = toolbar.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(view -> {
            Intent intent = new Intent(ActivityDetails.this, TaskActivity2.class);
            startActivity(intent);
        });
    }

    private void saveActivityToPreferences(String fsActivityName) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(ActivityDetails.this);
        sharedPreferencesManager.setActivityData(fsActivityName);
    }

    private void submitActivity() {

    }
}