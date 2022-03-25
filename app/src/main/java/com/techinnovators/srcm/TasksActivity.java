package com.techinnovators.srcm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techinnovators.srcm.Activity.LoginActivity;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.adapter.TasksAdapter;
import com.techinnovators.srcm.models.Taluka;
import com.techinnovators.srcm.models.TalukaResponse;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.TasksReponse;
import com.techinnovators.srcm.models.TasksVo;
import com.techinnovators.srcm.models.VisitDistrict;
import com.techinnovators.srcm.models.VisitDistrictResponse;
import com.techinnovators.srcm.models.VisitLocation;
import com.techinnovators.srcm.models.VisitLocationResponse;
import com.techinnovators.srcm.models.VisitRequest;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.utils.PermissionUtils;
import com.techinnovators.srcm.utils.SharedPreferencesManager;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.techinnovators.srcm.utils.SharedPreferencesManager.PREFS_NAME;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.VISIT_REQUEST_CHECK_IN;

public class TasksActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSIONS_CODE = 100;
    private static final String VISIT_REQUESTS = "visit_requests";
    private static final long SPLASH_SCREEN_TIME = 5000;

    ArrayList<Tasks> tasksList;
    ConstraintLayout csMain;

    TasksAdapter tasksAdapter;
    VolleyService mVolleyService;
    APIVInterface mResultCallback;

    String msLat = "", msLong = "";

    ImageView ivCheckIn;

    Thread thread;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<VisitRequest> visitRequestCheckIns;
    ArrayList<Tasks> tasksNotAdded;
    ArrayList<VisitDistrict> visitDistrictResponseList;
    ArrayList<Taluka> visitTaluka;
    ArrayList<VisitLocation> visitLocations;

    RecyclerView rvTasks;

    private boolean checkAllPermission() {
        int result_location = ContextCompat.checkSelfPermission(TasksActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result_location_coarse = ContextCompat.checkSelfPermission(TasksActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        return
                result_location == PackageManager.PERMISSION_GRANTED &&
                        result_location_coarse == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissions() {
        ActivityCompat.requestPermissions(TasksActivity.this, new String[]
                {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSIONS_CODE);

    }

    private void getCurrentLocation() {
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
//                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            if (checkAllPermission()) {
//                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Location> task) {
//                        Location location = task.getResult();
//                        if (location != null) {
//                            msLat = String.valueOf(location.getLatitude());
//                            msLong = String.valueOf(location.getLongitude());
//                        } else {
//                            LocationRequest locationRequest = LocationRequest.create();
//                            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                            locationRequest.setInterval(30 * 1000);
//                            locationRequest.setFastestInterval(5 * 1000);
//                            locationRequest.setNumUpdates(1);
//                            LocationCallback locationCallback = new LocationCallback() {
//                                @Override
//                                public void onLocationResult(LocationResult locationResult) {
//                                    Location location = locationResult.getLastLocation();
//                                    msLat = String.valueOf(location.getLatitude());
//                                    msLong = String.valueOf(location.getLongitude());
//                                    super.onLocationResult(locationResult);
//                                }
//                            };
//                            if (checkAllPermission()) {
////                                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//                            }else{
//                                showPermissions();
//                            }
//                        }
//                    }
//                });
//            }else{
//                showPermissions();
//            }
//        } else {
//            new AlertDialog.Builder(TasksActivity.this).setTitle(R.string.location_title).setMessage(R.string.location_service_msg).setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                }
//            }).setCancelable(false).show();
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_CODE && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation();
        } else {
            new AlertDialog.Builder(TasksActivity.this).setTitle(R.string.location_title).setMessage(R.string.location_service_msg).setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
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
        setContentView(R.layout.activity_tasks);

        tasksList = new ArrayList<>();
        tasksNotAdded = new ArrayList<>();
        visitLocations = new ArrayList<>();
        visitTaluka = new ArrayList<>();
        visitRequestCheckIns = new ArrayList<>();

        csMain = findViewById(R.id.csMain);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);

        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(TasksActivity.this));

        Toolbar toolbar = findViewById(R.id.toolbar);

        ImageView ivSync = toolbar.findViewById(R.id.ivSync);
        ivSync.setOnClickListener(view -> {
            if (NetworkUtils.isNetworkConnected(TasksActivity.this)) {
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                if (!TextUtils.isEmpty(sharedPreferencesManager.getActivityData())) {
                    submitActivity(sharedPreferencesManager.getActivityData());
                }
            }
        });

        ImageView ivLogout = toolbar.findViewById(R.id.ivLogout);
        ivLogout.setOnClickListener(view -> logout());

        ivCheckIn = toolbar.findViewById(R.id.ivCheckIn);

        ImageView ivWorkHour = findViewById(R.id.ivWorkHour);
        TextView tvWorkHours = findViewById(R.id.tvWorkingHours);

        ImageView ivCheckOut = toolbar.findViewById(R.id.ivCheckOut);
        ImageView ivAddActivity = toolbar.findViewById(R.id.ivAdd);

        ivAddActivity.setOnClickListener(view -> {
            Intent intent = new Intent(TasksActivity.this, AddVisitRequestActivity.class);
            startActivityForResult(intent,50);
        });

        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());
        String strCurrentDate = simpleDateFormat.format(new Date());
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault());

        String strCurrentTime = simpleTimeFormat.format(new Date());
        String strCompanyName = "SRCM";
        String strEmployeeId = sharedPreferencesManager.getEmployee();

        //Show Hide check in, check out by checking if current date matches with local storage date
        //if current date not matches with local storage date, clear first checkin, last checkout, clear date, clear attendance name(ID), clear checked In, clear checked out
        //If user checked in already hide check in, if user checked out already hide checkout
        if (!TextUtils.isEmpty(sharedPreferencesManager.getAttendanceDate())) {
            ivCheckOut.setVisibility(View.VISIBLE);
            String strAttendanceDate = sharedPreferencesManager.getAttendanceDate();

            if (!strAttendanceDate.equals(strCurrentDate)) {
                sharedPreferencesManager.clearAttendanceDate();
                sharedPreferencesManager.clearName();
                sharedPreferencesManager.clearFirstCheckIn();
                sharedPreferencesManager.clearLastCheckout();
                sharedPreferencesManager.clearCheckedIn();
                sharedPreferencesManager.clearCheckedOut();
                ivCheckIn.setVisibility(View.VISIBLE);
                ivCheckOut.setVisibility(View.GONE);
            } else {
                if (!TextUtils.isEmpty(sharedPreferencesManager.getCheckedIn())) {
                    if (sharedPreferencesManager.getCheckedIn().equals("1")) {
                        ivCheckIn.setVisibility(View.GONE);
                    }
                    if (sharedPreferencesManager.getCheckedOut().equals("1")) {
                        ivCheckOut.setVisibility(View.GONE);
                    }
                }
            }
        } else {
            ivCheckIn.setVisibility(View.VISIBLE);
        }
        //Show total hours,minutes,seconds to user every second.(Current time - First check in time)
        String strFirstCheckIn = sharedPreferencesManager.getFirstCheckIn();
        if (!TextUtils.isEmpty(strFirstCheckIn) && !strFirstCheckIn.equals("0.0")) {
            thread = new Thread() {
                @Override
                public void run() {
                    while (!isInterrupted()) {
                        try {
                            sleep(1000);  //1000ms = 1 sec
                            runOnUiThread(() -> displayWorkingHours(strFirstCheckIn, tvWorkHours, ivWorkHour));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
        }
        //Show total hours,minutes,seconds to user every second.(Last checkout time - First check in time)
        if (!TextUtils.isEmpty(sharedPreferencesManager.getCheckedOut())) {
            if (sharedPreferencesManager.getCheckedOut().equals("1")) {
                if (thread != null && !thread.isInterrupted()) {
                    thread.interrupt();
                }
                if (!TextUtils.isEmpty(sharedPreferencesManager.getLastCheckOut())) {
                    ivWorkHour.setVisibility(View.VISIBLE);
                    tvWorkHours.setText("");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    long timeDiff;
                    long hours;
                    String strHours = "", strMinutes = "", strSeconds = "";
                    long minutes;
                    long seconds;
                    try {
                        Date firstCheckInDate = dateFormat.parse(sharedPreferencesManager.getFirstCheckIn());
                        Date currentDateTime = dateFormat.parse(strCurrentTime);
                        if (firstCheckInDate != null && currentDateTime != null) {
                            timeDiff = currentDateTime.getTime() - firstCheckInDate.getTime();
                            hours = (timeDiff / (60 * 60 * 1000)) % 24;
                            minutes = (timeDiff / (60 * 1000)) % 60;
                            seconds = (timeDiff / 1000) % 60;

                            if (hours < 0) {
                                hours = Math.abs(hours);
                                strHours = Long.toString(hours);
                            } else if (hours <= 9) {
                                strHours = "0" + hours;
                            } else {
                                strHours = Long.toString(hours);
                            }
                            if (minutes < 0) {
                                minutes = Math.abs(minutes);
                                strMinutes = Long.toString(minutes);
                            } else if (minutes <= 9) {
                                strMinutes = "0" + minutes;
                            } else {
                                strMinutes = Long.toString(minutes);
                            }
                            if (seconds < 0) {
                                seconds = Math.abs(seconds);
                                strSeconds = Long.toString(seconds);
                            } else if (seconds <= 9) {
                                strSeconds = "0" + seconds;
                            } else {
                                strSeconds = Long.toString(seconds);
                            }
                            String strTime = strHours + ":" + strMinutes + ":" + strSeconds;
                            ivWorkHour.setVisibility(View.VISIBLE);
                            tvWorkHours.setText(strTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        ivCheckIn.setOnClickListener(view -> {
            if (NetworkUtils.isNetworkConnected(TasksActivity.this)) {
                checkIn(strCompanyName, strEmployeeId, ivCheckIn, ivCheckOut);
            } else {
                AppUtils.showSnackBar(TasksActivity.this, csMain, getString(R.string.internet_off));
            }
        });
        ivCheckOut.setOnClickListener(view -> {
            double hoursFromMinutes;
            double totalWorkingHours = 0.0;
            if (NetworkUtils.isNetworkConnected(TasksActivity.this)) {
                String strFirstCheckInTime = sharedPreferencesManager.getFirstCheckIn();
                if (!TextUtils.isEmpty(strFirstCheckInTime) && !strFirstCheckInTime.equals("0.0")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    long timeDiff;
                    long hours;
                    long minutes;
                    try {
                        Date firstCheckInDate = dateFormat.parse(sharedPreferencesManager.getFirstCheckIn());
                        Date currentDateTime = dateFormat.parse(strCurrentTime);
                        if (firstCheckInDate != null && currentDateTime != null) {
                            timeDiff = currentDateTime.getTime() - firstCheckInDate.getTime();
                            hours = (timeDiff / (60 * 60 * 1000)) % 24;
                            minutes = (timeDiff / (60 * 1000)) % 60;
                            hoursFromMinutes = (double) minutes / 60;
                            totalWorkingHours = hours + hoursFromMinutes;
                            DecimalFormat df = new DecimalFormat("#.#");
                            totalWorkingHours = Double.parseDouble(df.format(totalWorkingHours));
                        }
                        if (totalWorkingHours > 0.0) {
                            checkOut(strCurrentTime, totalWorkingHours, ivCheckIn, ivCheckOut);
                        } else {
                            AppUtils.showSnackBar(TasksActivity.this, csMain, "Total working hours cannot be 0 while checking out from system.");
                        }
                    } catch (ParseException e) {
                        AppUtils.showSnackBar(TasksActivity.this, csMain, "Unable to find working hours from the app. Please contact tech support");
                    }
                } else {
                    AppUtils.showSnackBar(TasksActivity.this, csMain, "Unable to find check in time from system. Please contact tech support.");
                }
            } else {
                AppUtils.showSnackBar(TasksActivity.this, csMain, getString(R.string.internet_off));
            }
        });


        if (NetworkUtils.isNetworkConnected(TasksActivity.this)) {
            //Get Token and store in local storage, load visit requests after receiving token and if response has module named visit request.
            getUserDetails();
            //Check for visit requests checked in already and not submitted to SRCM server and update such visit requests.
            visitRequestCheckIns.addAll(getVisitRequestCheckIns());
            if (!visitRequestCheckIns.isEmpty()) {
                for (int index = 0; index < visitRequestCheckIns.size(); index++) {
                    VisitRequest visitRequest = visitRequestCheckIns.get(index);
                    visitRequestCheckIn(visitRequest.getName(), visitRequest.getVisit_checkin());
                }
            }
            //Check for visit requests added to server from local or not and add such visit requests to server using REST API.
            tasksNotAdded.addAll(getVisitRequests());
            if (!tasksNotAdded.isEmpty()) {
                for (int index = 0; index < tasksNotAdded.size(); index++) {
                    Tasks visitRequest = tasksNotAdded.get(index);
                    if (!TextUtils.isEmpty(tasksNotAdded.get(index).getName()) && !tasksNotAdded.get(index).getName().startsWith("V")) {
                        if (tasksNotAdded.get(index).isVisitRequestAdded()) {
                            createVisitRequest(visitRequest);
                        }
                    }
                }
            }

        } else {
            //Load visit request from local storage and refresh list
            getVisitRequests();
            setTasksList(tasksList);

            AppUtils.showSnackBar(TasksActivity.this, csMain, getString(R.string.internet_off));
        }
    }

    private void setRefreshIndicator(){
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black));
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
            mSwipeRefreshLayout.setRefreshing(false);
            if (NetworkUtils.isNetworkConnected(TasksActivity.this)) {
                getTasksList(sharedPreferencesManager.getEmployee());
                getProjectName();
                getProjectType();
                getOrganisationName();
                getVisitState();
                getDistrict();
                getTaluka();
                getLocationOfVisit();
            } else { ;
                tasksList = getVisitRequests();
                tasksAdapter.updateList(tasksList);

                AppUtils.showSnackBar(TasksActivity.this, csMain, getString(R.string.internet_off));
            }
        });
    }
    private void setTasksList(ArrayList<Tasks> data){
        if(tasksAdapter == null){
            tasksAdapter = new TasksAdapter(TasksActivity.this, tasksList);
            rvTasks.setAdapter(tasksAdapter);
        }else{
            tasksAdapter.updateList(data);
        }
    }

    private void createVisitRequest(Tasks visitRequest) {
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_methodname_createVisit);
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                if (response != null && !response.toString().isEmpty()) {
                    //set flag visitRequestAdded=true in visit requests from local storage  after getting success.
                }
            }

            @Override
            public void notifyError(VolleyError error) {
                if (error.networkResponse != null) {
                    switch (error.networkResponse.statusCode) {
                        case 401:
                            String responseBody;
                            try {
                                responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (!data.getString("message").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.create_Visit), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.create_Visit), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.create_Visit), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.create_Visit), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {
            }
        };
        apiUrl += endpoint;
        JSONObject jsonObject = new JSONObject();
        try {
//            Tasks visitRequest = new Tasks();
//            visitRequest.setVisitRequestAdded(false);
//            visitRequest.setVisit_date(strDate);
//            visitRequest.setVisit_district(strVisitDistrict[0]);
//            visitRequest.setVisit_location(strVisitLocation[0]);
//            visitRequest.setVisit_taluka(strVisitTaluka[0]);
//            visitRequest.setName(AppUtils.dispCurrentDate() + " " + AppUtils.getCurrentTime());
//            visitRequest.setVisit_state(strVisitState[0]);
//            visitRequest.setProject_type(strProjectType[0]);
//            visitRequest.setProject_name(strProjectName[0]);
//            visitRequest.setVisit_place(strOrgName[0]);
//            if (!TextUtils.isEmpty(strPersonPhNo)) {
//                visitRequest.setContact_person_mobile_no(strPersonPhNo);
//            } else {
//                visitRequest.setContact_person_mobile_no("");
//            }
//            if (!TextUtils.isEmpty(strPerson)) {
//                visitRequest.setContact_person_name(strPersonPhNo);
//            } else {
//                visitRequest.setContact_person_name("");
//            }
            //Pass all the values from Task model above to JSON object to create visit request.
            mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.postDataVolley(apiUrl, jsonObject);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.create_Visit), e.getMessage());
        }
    }

    private void submitActivity(String fsActivityData) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
        sharedPreferencesManager.clearActivityData();
    }

    private void visitRequestCheckIn(String fsVisitRequestNo, String fsVisitCheckIn) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);

        String apiUrl = getString(R.string.api_url);
        String endPoint = getString(R.string.api_methodname_visit_request);

        endPoint += "/" + fsVisitRequestNo;
        apiUrl += endPoint;

        JSONObject jsonRequest = new JSONObject();

        try {

            RequestQueue queue = Volley.newRequestQueue(TasksActivity.this);
            jsonRequest.put(getString(R.string.visit_checkin_param_key), fsVisitCheckIn);
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, apiUrl, jsonRequest,
                    (JSONObject response) -> {
                        AppUtils.dismissProgrees();
                        try {
                            JSONObject dataResponse = response.getJSONObject(getString(R.string.data_param_key));
                            if (!dataResponse.toString().isEmpty()) {
                                String strName = dataResponse.getString(getString(R.string.name_param_key));
                                if (!TextUtils.isEmpty(strName)) {
                                    if (strName.equals(fsVisitRequestNo)) {
                                        getTasksList(sharedPreferencesManager.getEmployee());
                                        ArrayList<VisitRequest> visitRequests = new ArrayList<>(getVisitRequestCheckIns());
                                        if (!visitRequests.isEmpty()) {
                                            for (int index = 0; index < visitRequests.size(); index++) {
                                                VisitRequest visitRequest = visitRequests.get(index);
                                                if (visitRequest.getName().equals(strName)) {
                                                    visitRequests.remove(visitRequest);
                                                    break;
                                                }
                                            }
                                            sharedPreferencesManager.setVisitRequestCheckIn(visitRequests);
                                            ArrayList<VisitRequest> checkIns = new ArrayList<>(getVisitRequestCheckIns());
                                            if (!checkIns.isEmpty()) {
                                                visitRequestCheckIns.addAll(checkIns);
                                            } else {
                                                visitRequestCheckIns.clear();
                                            }
                                        }
                                        Toast.makeText(TasksActivity.this, "Visit request check in time updated successfully", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                            AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", e.getMessage());
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        if (error.getMessage() != null) {
                            if (error.getMessage().equals(TasksActivity.this.getString(R.string.unknown_host))) {
                                storeVisitRequestCheckInData(fsVisitRequestNo, fsVisitCheckIn);
                            }
                        }

                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 401:
                                    String responseBody;
                                    try {
                                        responseBody = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(responseBody);
                                        if (!data.getString("message").isEmpty()) {
                                            AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                    AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", getString(R.string.error_403));
                                case 404:
                                    AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", getString(R.string.error_404));
                                    break;
                                case 417:
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString(getString(R.string._server_messages_param_key)).isEmpty()) {
                                            AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", data.getString("_server_messages"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", getString(R.string.error_500));
                                    break;
                            }
                        }
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    //..add other headers
                    if (!sharedPreferencesManager.getToken().equals("")) {
                        params.put("Authorization", sharedPreferencesManager.getToken());
                    }
                    params.put("Content-Type", "application/json");
                    params.put("Accept", "application/json");
                    return params;
                }
            };
            queue.add(putRequest);
            AppUtils.showProgress(this,getString(R.string.prog_dialog_title));
        } catch (JSONException e) {
            AppUtils.dismissProgrees();
            AppUtils.displayAlertMessage(TasksActivity.this, "VISIT REQUEST CHECK IN", e.getMessage());
            e.printStackTrace();
        }
    }

    private void storeVisitRequestCheckInData(String fsVisitRequestNo, String strVisitCheckIn) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
        ArrayList<VisitRequest> visitRequestCheckIns = new ArrayList<>();
        VisitRequest visitRequest = new VisitRequest();
        visitRequest.setName(fsVisitRequestNo);
        visitRequest.setVisit_checkin(strVisitCheckIn);
        visitRequestCheckIns.add(visitRequest);
        sharedPreferencesManager.setVisitRequestCheckIn(visitRequestCheckIns);
        visitRequestCheckIns.clear();
        visitRequestCheckIns.addAll(getVisitRequestCheckIns());
        if (!visitRequestCheckIns.isEmpty()) {
            Toast.makeText(TasksActivity.this, "Visit Request" + " " + fsVisitRequestNo + "check in time stored in local successfully", Toast.LENGTH_LONG).show();

        }
        ArrayList<Tasks> tasks = new ArrayList<>(getVisitRequests());
        if (!tasks.isEmpty()) {
            for (int index = 0; index < tasks.size(); index++) {
                String strName = tasks.get(index).getName();
                if (strName.equals(fsVisitRequestNo)) {
                    tasks.get(index).setVisit_checkin(strVisitCheckIn);
                    break;
                }
            }

            sharedPreferencesManager.saveVisitRequests(tasks);
        }
    }

    private void checkOut(String fsLastCheckOutTime, Double fdWorkingHours, ImageView fIvCheckIn, ImageView fIvCheckout) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
        String apiUrl = getString(R.string.api_url);
        String endPoint = getString(R.string.api_methodname_attendance);
        endPoint += "/" + sharedPreferencesManager.getName();
        apiUrl += endPoint;
        JSONObject jsonRequest = new JSONObject();
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(TasksActivity.this);
        progressDialog.setMax(100);
        progressDialog.setMessage(getString(R.string.prog_dialog_title));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        try {

            RequestQueue queue = Volley.newRequestQueue(TasksActivity.this);
            jsonRequest.put(getString(R.string.docstatus_param_key), 1);
            jsonRequest.put(getString(R.string.last_checkout_param_key), fsLastCheckOutTime);
            jsonRequest.put(getString(R.string.total_working_hours_param_key), fdWorkingHours);
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, apiUrl, jsonRequest,
                    (JSONObject response) -> {
                        progressDialog.dismiss();
                        try {
                            JSONObject dataResponse = response.getJSONObject(getString(R.string.data_param_key));
                            if (!dataResponse.toString().isEmpty()) {
                                fIvCheckIn.setVisibility(View.VISIBLE);
                                fIvCheckout.setVisibility(View.GONE);
                                sharedPreferencesManager.clearName();
                                sharedPreferencesManager.clearFirstCheckIn();
                                String strLastCheckout = dataResponse.getString(getString(R.string.last_checkout_param_key));
                                if (!TextUtils.isEmpty(strLastCheckout)) {
                                    sharedPreferencesManager.setLastCheckOut(strLastCheckout);
                                }
                                sharedPreferencesManager.setCheckedOut("1");
                                AppUtils.showSnackBar(TasksActivity.this, csMain, "You have checked out from system at" + strLastCheckout + ".");
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                            AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", e.getMessage());
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 401:
                                    String responseBody;
                                    try {
                                        responseBody = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(responseBody);
                                        if (!data.getString("message").isEmpty()) {
                                            AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                    AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", getString(R.string.error_403));
                                case 404:
                                    AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", getString(R.string.error_404));
                                    break;
                                case 417:
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString(getString(R.string._server_messages_param_key)).isEmpty()) {
                                            AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", data.getString("_server_messages"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", getString(R.string.error_500));
                                    break;
                            }
                        }
                    }
            ) {
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    //..add other headers
                    if (!sharedPreferencesManager.getToken().equals("")) {
                        params.put("Authorization", sharedPreferencesManager.getToken());
                    }
                    params.put("Content-Type", "application/json");
                    params.put("Accept", "application/json");
                    return params;
                }
            };
            queue.add(putRequest);
            progressDialog.show();
        } catch (JSONException e) {
            progressDialog.dismiss();
            AppUtils.displayAlertMessage(TasksActivity.this, "CHECK OUT", e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayWorkingHours(String fsCheckInTime, TextView fTvWorkHours, ImageView ivWorkHour) {
        String strWorkHours = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String strCurrentTime = AppUtils.getCurrentTime();
        long timeDiff;
        long hours;
        long minutes;
        long seconds;
        String strHours = "";
        String strMinutes = "";
        String strSeconds = "";
        try {
            Date firstCheckInDate = dateFormat.parse(fsCheckInTime);
            Date currentDateTime = dateFormat.parse(strCurrentTime);
            if (firstCheckInDate != null && currentDateTime != null) {
                timeDiff = currentDateTime.getTime() - firstCheckInDate.getTime();
                hours = (timeDiff / (60 * 60 * 1000)) % 24;
                minutes = (timeDiff / (60 * 1000)) % 60;
                seconds = (timeDiff / 1000) % 60;
                if (hours < 0) {
                    hours = Math.abs(hours);
                    strHours = Long.toString(hours);
                } else if (hours <= 9) {
                    strHours = "0" + hours;
                } else {
                    strHours = Long.toString(hours);
                }
                if (minutes < 0) {
                    minutes = Math.abs(minutes);
                    strMinutes = Long.toString(minutes);
                } else if (minutes <= 9) {
                    strMinutes = "0" + minutes;
                } else {
                    strMinutes = Long.toString(minutes);
                }
                if (seconds < 0) {
                    seconds = Math.abs(seconds);
                    strSeconds = Long.toString(seconds);
                } else if (seconds <= 9) {
                    strSeconds = "0" + seconds;
                } else {
                    strSeconds = Long.toString(seconds);
                }
            }
            strWorkHours = strHours + ":" + strMinutes + ":" + strSeconds;
            fTvWorkHours.setVisibility(View.VISIBLE);
            ivWorkHour.setVisibility(View.VISIBLE);
            fTvWorkHours.setText(strWorkHours);
        } catch (ParseException e) {
            AppUtils.showSnackBar(TasksActivity.this, csMain, "Unable to find working hours from the app.");
        }
    }

    private void checkIn(String fsCompany, String fsEmpID, ImageView fIvCheckIn, ImageView fIvCheckout) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(TasksActivity.this);
        progressDialog.setMax(100);
        progressDialog.setMessage(getString(R.string.prog_dialog_title));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                progressDialog.dismiss();
                if (response != null && !response.toString().isEmpty()) {
                    try {
                        JSONObject jsonData = response.getJSONObject(getString(R.string.data_param_key));
                        if (!jsonData.toString().isEmpty()) {

                            String strFirstCheckIn = jsonData.getString(getString(R.string.first_checkin_param_key));
                            String strEmployee = jsonData.getString(getString(R.string.employee_param_key));
                            String strAttendanceDate = jsonData.getString(getString(R.string.attendance_date_param_key));
                            String strName = jsonData.getString(getString(R.string.name_param_key));

                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);

                            fIvCheckIn.setVisibility(View.GONE);
                            fIvCheckout.setVisibility(View.VISIBLE);

                            sharedPreferencesManager.clearLastCheckout();

                            if (!TextUtils.isEmpty(strFirstCheckIn)) {
                                sharedPreferencesManager.setFirstCheckIn(strFirstCheckIn);
                            }
                            if (!TextUtils.isEmpty(strEmployee)) {
                                sharedPreferencesManager.setEmployee(strEmployee);
                            }
                            if (!TextUtils.isEmpty(strAttendanceDate)) {
                                sharedPreferencesManager.setAttendanceDate(strAttendanceDate);
                            }
                            if (!TextUtils.isEmpty(strName)) {
                                sharedPreferencesManager.setName(strName);
                            }

                            sharedPreferencesManager.setCheckedIn("1");

                            AppUtils.showSnackBar(TasksActivity.this, csMain, "You have checked in successfully in system at" + " " + strFirstCheckIn + ".");
                        }
                    } catch (JSONException jsonException) {
                        AppUtils.displayAlertMessage(TasksActivity.this, "CHECK IN", jsonException.getMessage());
                        jsonException.printStackTrace();
                    }
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
                                    AppUtils.displayAlertMessage(TasksActivity.this, "CHECK IN", data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, "CHECK IN", e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(TasksActivity.this, "CHECK IN", getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, "CHECK IN", getString(R.string.error_404));
                            break;
                        case 417:
                            String response;
                            try {
                                response = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(response);
                                if (!data.getString("_server_messages").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, "CHECK IN", data.getString("_server_messages"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, "CHECK IN", e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, "CHECK IN", getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {
                progressDialog.dismiss();
            }
        };
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_methodname_attendance);
        apiUrl += endpoint;
        try {
            mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            JSONObject jsonObject = new JSONObject();
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault());
            String strCurrentTime = simpleTimeFormat.format(new Date());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());
            String strCurrentDate = simpleDateFormat.format(new Date());
            jsonObject.put(getString(R.string.attendance_date_param_key), strCurrentDate);
            jsonObject.put(getString(R.string.company_param_key), fsCompany);
            jsonObject.put(getString(R.string.docstatus_param_key), 0);
            jsonObject.put(getString(R.string.doctype_param_key), getString(R.string.attendance_param_value));
            jsonObject.put(getString(R.string.employee_param_key), fsEmpID);
            jsonObject.put(getString(R.string.first_checkin_param_key), strCurrentTime);
            jsonObject.put(getString(R.string.last_checkout_param_key), "");
            jsonObject.put(getString(R.string.status_param_key), getString(R.string.attendance_default_status));
            jsonObject.put(getString(R.string.total_working_hours_param_key), 0.0);
            mVolleyService.postDataVolley(apiUrl, jsonObject);
            progressDialog.show();
        } catch (Exception e) {
            progressDialog.dismiss();
            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", e.getMessage());
        }
    }

    private void getUserDetails() {
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_fieldwork_tracking_get_user_details);

        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                AppUtils.dismissProgrees();

                ArrayList<String> appModules = new ArrayList<>();
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                try {
                    JSONArray jsonMessage = response.getJSONArray(getString(R.string.message_param_key));
                    if (jsonMessage.length() > 0) {
                        for (int index = 0; index < jsonMessage.length(); index++) {
                            try {
                                JSONObject jsonObject = jsonMessage.getJSONObject(index);
                                int enabled = jsonObject.getInt(getString(R.string.enabled_param_key));
                                if (enabled == 0) {
                                    logout();
                                } else if (enabled == 1) {
                                    sharedPreferencesManager.setAPIKey(jsonObject.optString(getString(R.string.api_key_param_key)));
                                    sharedPreferencesManager.setAPISecret(jsonObject.optString(getString(R.string.api_secret_param_key)));
                                    sharedPreferencesManager.setEmployee(jsonObject.optString(getString(R.string.employee_id_param_key)));
                                    sharedPreferencesManager.setToken(String.format(getString(R.string.token_value),
                                            sharedPreferencesManager.getAPIKey(), sharedPreferencesManager.getAPISecret()));
                                    String strAppModule = jsonObject.getString(getString(R.string.user_app_module_param_key));
                                    if (!strAppModule.isEmpty()) {
                                        if (strAppModule.contains(getString(R.string.visit_request_param_key))) {
                                            appModules.add(getString(R.string.visit_request_param_value));
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        if (!appModules.isEmpty()) {
                            for (int index = 0; index < appModules.size(); index++) {
                                String strVisitRequest = appModules.get(index);
                                if (!TextUtils.isEmpty(strVisitRequest)) {
                                    getTasksList(sharedPreferencesManager.getEmployee());
                                }
                            }
                        }
                    }
                } catch (JSONException jsonException) {
                    AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", jsonException.getMessage());
                }
            }

            @Override
            public void notifyError(VolleyError error) {
                AppUtils.dismissProgrees();

                if (error.networkResponse != null) {
                    switch (error.networkResponse.statusCode) {
                        case 401:
                            String responseBody;
                            try {
                                responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (!data.getString("message").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {
                AppUtils.dismissProgrees();
            }
        };
        apiUrl += endpoint;
        try {
            mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);

            /// Params
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(getString(R.string.login_req_api_param_usr), Application.getUserModel().userName);
            jsonObject.put(getString(R.string.app_auth_key_param_key), getString(R.string.app_auth_key));

            mVolleyService.postDataVolley(apiUrl, jsonObject);
            AppUtils.showProgress(this,getString(R.string.prog_dialog_title));
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", e.getMessage());
        }
    }

    private void getLocationOfVisit() {
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_visitlocation);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key + "&";
        String fields = getString(R.string.fields_param_key);
        fields += getString(R.string.fields_value_location);
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<VisitLocationResponse>() {
                        }.getType();
                        VisitLocationResponse tasksResponse = gson.fromJson(response.toString(), listType);
                        visitLocations.addAll(tasksResponse.getData());
                        if (!visitLocations.isEmpty()) {
                            ArrayList<VisitLocation> visitLocationResp;
                            visitLocationResp = visitLocations;
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                            sharedPreferencesManager.saveVisitLocation(visitLocationResp);
                        }
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }

            @Override
            public void notifyError(VolleyError error) {
                if (error.networkResponse != null) {
                    switch (error.networkResponse.statusCode) {
                        case 401:
                            String responseBody;
                            try {
                                responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (!data.getString("message").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visitlocation), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visitlocation), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visitlocation), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visitlocation), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        endpoint += fields;
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visitlocation), e.getMessage());
        }
    }

    private void getDistrict() {
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_visitdistrict);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key + "&";
        String fields = getString(R.string.fields_param_key);
        fields += getString(R.string.fields_value_district);
        visitDistrictResponseList = new ArrayList<>();
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {

                        Gson gson = new Gson();
                        Type listType = new TypeToken<VisitDistrictResponse>() {
                        }.getType();
                        VisitDistrictResponse tasksResponse = gson.fromJson(response.toString(), listType);
                        visitDistrictResponseList.addAll(tasksResponse.getData());

                        if (!visitDistrictResponseList.isEmpty()) {
//                            arrAdVisitDist.addAll(visitDistrict);
//                            arrAdVisitDist.notifyDataSetChanged();
                            ArrayList<VisitDistrict> visitDistrictList = new ArrayList<>();
                            visitDistrictList = visitDistrictResponseList;
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                            //STORE ENTIRE RESPONSE
                            sharedPreferencesManager.saveVisitDistrict(visitDistrictList);
                        }
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }

            @Override
            public void notifyError(VolleyError error) {
                if (error.networkResponse != null) {
                    switch (error.networkResponse.statusCode) {
                        case 401:
                            String responseBody;
                            try {
                                responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (!data.getString("message").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        endpoint += fields;
        apiUrl += endpoint;
        try {
            mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), e.getMessage());
        }
    }

    private void getTaluka() {
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_visittaluka);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key + "&";
        String fields = getString(R.string.fields_param_key);
        fields += getString(R.string.fields_value_taluka);
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<TalukaResponse>() {
                        }.getType();
                        TalukaResponse tasksResponse = gson.fromJson(response.toString(), listType);
                        visitTaluka.addAll(tasksResponse.getData());
                        if (!visitTaluka.isEmpty()) {
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                            sharedPreferencesManager.saveVisitTaluka(visitTaluka);
                        }
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }

            @Override
            public void notifyError(VolleyError error) {
                if (error.networkResponse != null) {
                    switch (error.networkResponse.statusCode) {
                        case 401:
                            String responseBody;
                            try {
                                responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (!data.getString("message").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        endpoint += fields;
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.visit_district), e.getMessage());
        }
    }

    private void getVisitState() {
        ArrayList<String> visitState = new ArrayList<>();
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_visitstate);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key;
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        for (int index = 0; index < data.length(); index++) {
                            JSONObject jsonObject = data.getJSONObject(index);
                            if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                String strName = jsonObject.getString(getString(R.string.name_param_key));
                                if (!strName.isEmpty()) {
                                    visitState.add(strName);
                                }
                            }
                        }
                        if (!visitState.isEmpty()) {
//                            arrAdVisitState.addAll(visitState);
//                            arrAdVisitState.notifyDataSetChanged();
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                            sharedPreferencesManager.saveVisitState(visitState);
                        }
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }

            @Override
            public void notifyError(VolleyError error) {
                if (error.networkResponse != null) {
                    switch (error.networkResponse.statusCode) {
                        case 401:
                            String responseBody;
                            try {
                                responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (!data.getString("message").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.state), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.state), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.state), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.state), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.state), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.state), e.getMessage());
        }
    }

    private void getOrganisationName() {
        ArrayList<String> orgName = new ArrayList<>();
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_organizationname);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key;
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        for (int index = 0; index < data.length(); index++) {
                            JSONObject jsonObject = data.getJSONObject(index);
                            if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                String strName = jsonObject.getString(getString(R.string.name_param_key));
                                if (!strName.isEmpty()) {
                                    orgName.add(strName);
                                }
                            }
                        }
                        if (!orgName.isEmpty()) {
//                            arrAdOrgName.addAll(orgName);
//                            arrAdOrgName.notifyDataSetChanged();
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                            sharedPreferencesManager.saveOrgName(orgName);
                        }
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }

            @Override
            public void notifyError(VolleyError error) {
                if (error.networkResponse != null) {
                    switch (error.networkResponse.statusCode) {
                        case 401:
                            String responseBody;
                            try {
                                responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (!data.getString("message").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.orgname), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.orgname), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.orgname), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.orgname), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.orgname), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, getString(R.string.orgname), e.getMessage());
        }
    }

    private void getProjectType() {
        ArrayList<String> projectType = new ArrayList<>();
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_projecttype);
        String limit_page_length_key = "limit_page_length=None";
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        for (int index = 0; index < data.length(); index++) {
                            JSONObject jsonObject = data.getJSONObject(index);
                            if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                String strName = jsonObject.getString(getString(R.string.name_param_key));
                                if (!strName.isEmpty()) {
                                    projectType.add(strName);
                                }
                            }
                        }
                        if (!projectType.isEmpty()) {
//                            arrayAdapterProjectType.addAll(projectType);
//                            arrayAdapterProjectType.notifyDataSetChanged();
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                            sharedPreferencesManager.saveProjectType(projectType);
                        }
                    }
                } catch (JSONException jsonException) {
                    jsonException.printStackTrace();
                }

            }

            @Override
            public void notifyError(VolleyError error) {
                if (error.networkResponse != null) {
                    switch (error.networkResponse.statusCode) {
                        case 401:
                            String responseBody;
                            try {
                                responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                if (!data.getString("message").isEmpty()) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT TYPE", data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT TYPE", e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT TYPE", getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT TYPE", getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT TYPE", getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        endpoint += limit_page_length_key;
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT TYPE", e.getMessage());
        }
    }

    private void getProjectName() {
        ArrayList<String> projectName = new ArrayList<>();
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_project);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key;
        apiUrl += endpoint;
        try {
            mResultCallback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                        if (data.length() > 0) {
                            for (int index = 0; index < data.length(); index++) {
                                JSONObject jsonObject = data.getJSONObject(index);
                                if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                    String strName = jsonObject.getString(getString(R.string.name_param_key));
                                    if (!strName.isEmpty()) {
                                        projectName.add(strName);
                                    }
                                }
                            }
                            if (!projectName.isEmpty()) {
                                // arrayAdapterProjectName.addAll(projectName);
                                // arrayAdapterProjectName.notifyDataSetChanged();
                                sharedPreferencesManager.saveProjectName(projectName);
                            }
                        }
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }

                }

                @Override
                public void notifyError(VolleyError error) {
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT NAME", data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT NAME", e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT NAME", getString(R.string.error_403));
                            case 404:
                                AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT NAME", getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT NAME", getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            VolleyService mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, "PROJECT NAME", e.getMessage());
        }
    }

    public ArrayList<Tasks> getVisitRequests() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_REQUESTS, null);
        Type type = new TypeToken<ArrayList<Tasks>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            tasksList = gson.fromJson(json, type);
        }
        if (tasksList == null) {
            tasksList = new ArrayList<>();
        }
        return tasksList;
    }

    private ArrayList<VisitRequest> getVisitRequestCheckIns() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_REQUEST_CHECK_IN, null);
        Type type = new TypeToken<ArrayList<VisitRequest>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            visitRequestCheckIns = gson.fromJson(json, type);
        }
        if (visitRequestCheckIns == null) {
            visitRequestCheckIns = new ArrayList<>();
        }
        return visitRequestCheckIns;
    }

    private void logout() {
        DbClient.getInstance().clearAllTables();

        Intent intent = new Intent(TasksActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void getTasksList(String fsEmpName) {
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(TasksActivity.this);
        progressDialog.setMax(100);
        progressDialog.setMessage(getString(R.string.prog_dialog_title));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_methodname_visit_requests);
        String fields_key = getString(R.string.fields_param_key);
        String fields_value = getString(R.string.fields_value);
        String filters_key = getString(R.string.filters_param_key);
        String strToday = AppUtils.dispCurrentDate();
        String filters_value = "[[\"employee\", \"=\"," + '"' + fsEmpName + '"' + "],[\"visit_expiry_date\",\">=\"," +
                '"' + strToday + '"' + "]]";
        endpoint += fields_key + fields_value + "&" + filters_key + filters_value;
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                progressDialog.dismiss();
                JSONArray tasks;
                SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(TasksActivity.this);
                //clear local list variable, load API response,store in local list and refresh list, get visit requests from local storage,
                // clear visit requests from local storage, save visit requests in local storage,
                try {
                    Gson gson = new Gson();
                    TasksVo tasksVo = gson.fromJson(response.toString(), TasksVo.class);

                    tasks = response.getJSONArray(getString(R.string.data_param_key));
                    if (tasks.length() > 0) {
                        Log.v("S", tasks.toString());
                        Type listType = new TypeToken<TasksReponse>() {
                        }.getType();

                        TasksReponse tasksResponse = gson.fromJson(response.toString(), listType);

                        tasksList = (ArrayList<Tasks>) tasksResponse.getData();
                        setTasksList(tasksList);

                        tasksList = getVisitRequests();

                        if (!tasksList.isEmpty()) {
                            sharedPreferencesManager.clearVisitRequests();
                        }

                        sharedPreferencesManager.saveVisitRequests(tasksResponse.getData());
                    } else {
                        if (tasksList.isEmpty()) {
                            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", getString(R.string.no_records));
                        }
                    }
                } catch (JSONException jsonException) {
                    AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", jsonException.getMessage());
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
                                    AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 404:
                            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS",
                                    getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS",
                                    getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {
                progressDialog.dismiss();
            }
        };
        apiUrl += endpoint;
        try {
            mVolleyService = new VolleyService(mResultCallback, TasksActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
            progressDialog.show();
        } catch (Exception e) {
            AppUtils.displayAlertMessage(TasksActivity.this, "TASKS", e.getMessage());
        }
    }

    private void refreshTask(){
        tasksList = getVisitRequests();
        tasksAdapter.updateList(tasksList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 50) {
            refreshTask();
        }
    }
}