package com.techinnovators.srcm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.utils.SharedPreferencesManager;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    FusedLocationProviderClient fusedLocationProviderClient;
    int LOCATION_PERMISSIONS_CODE = 100;
    String msLat = "", msLong = "";
    RelativeLayout rlMain;
    VolleyService mVolleyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        rlMain = findViewById(R.id.rlMain);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(LoginActivity.this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkAllPermission()) {
                showPermissions();
                if (ActivityCompat.checkSelfPermission(
                        LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation();
                }
            } else {
                getCurrentLocation();
            }
        }
        TextView tvLogin = findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(this);

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(LoginActivity.this);
        if (!sharedPreferencesManager.getUserName().isEmpty()) {
            AppUtils.hideKeyboard(LoginActivity.this);

            rlMain.setVisibility(View.GONE);

            Intent intent = new Intent(LoginActivity.this, TasksActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            rlMain.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkAllPermission() {
        int result_location = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result_location_coarse = ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return
                result_location == PackageManager.PERMISSION_GRANTED &&
                        result_location_coarse == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissions() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSIONS_CODE);

    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSIONS_CODE);
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
                        if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            new AlertDialog.Builder(LoginActivity.this).setTitle(R.string.location_title).
                    setMessage(R.string.location_service_msg).
                    setPositiveButton("SETTINGS", (dialogInterface, i) ->
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
                    .setCancelable(false).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_CODE && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation();
        } else {
            new AlertDialog.Builder(LoginActivity.this).setTitle(R.string.location_title).
                    setMessage(R.string.location_service_msg)
                    .setPositiveButton("SETTINGS", (dialogInterface, i)
                            -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))).
                    setCancelable(false).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void login(String strUsername, String strPass) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMax(100);
        progressDialog.setMessage(getString(R.string.prog_dialog_title));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_methodname_login);
        apiUrl += endpoint;

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(getString(R.string.login_req_api_param_usr), strUsername);
            jsonObject.put(getString(R.string.login_req_api_param_pwd), strPass);
            mVolleyService = new VolleyService(new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    progressDialog.dismiss();
                    //RESPONSE_SUCCESS_PARAM IS XXXXX
                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(LoginActivity.this);
                    if (!response.optString("message").isEmpty() && !response.optString("full_name").isEmpty()) {
                        if (response.optString("message").equalsIgnoreCase("No App")) {
                            sharedPreferencesManager.setUserName(strUsername);
                            Intent intent = new Intent(LoginActivity.this, TasksActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        AppUtils.showSnackBar(LoginActivity.this, rlMain, getString(R.string.Invalid_credentials));
                    }
                }

                @Override
                public void notifyError(VolleyError error) {
                    progressDialog.dismiss();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 404:
                                AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    progressDialog.dismiss();
                }
            }, LoginActivity.this);
            mVolleyService.postDataVolley(apiUrl, jsonObject);
            progressDialog.show();
        } catch (Exception e) {
            AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", e.getMessage());
        }
    }

    @Override
    public void onClick(View view) {
        AppCompatEditText etUserName, etPassword;
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
        String strUsername, strPass;
        strUsername = etUserName.getText().toString().trim();
        strPass = etPassword.getText().toString().trim();
        if (view.getId() == R.id.tvLogin) {
            if (strUsername.isEmpty()) {
                AppUtils.showSnackBar(LoginActivity.this, rlMain, "Username can't be empty");
                return;
            }
            if (strPass.isEmpty()) {
                AppUtils.showSnackBar(LoginActivity.this, rlMain, "Password can't be empty");
                return;
            }
            if (NetworkUtils.isNetworkConnected(LoginActivity.this)) {
                login(strUsername, strPass);
            } else {
                AppUtils.showSnackBar(LoginActivity.this, rlMain, getString(R.string.internet_off));
            }
        }
    }


}