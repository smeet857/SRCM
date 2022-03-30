package com.techinnovators.srcm.Activity;

import static com.techinnovators.srcm.utils.PermissionUtils.LOCATION_PERMISSIONS_CODE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techinnovators.srcm.AddVisitRequestActivity;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.TasksActivity;
import com.techinnovators.srcm.adapter.TasksAdapter;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.TasksReponse;
import com.techinnovators.srcm.models.UserModel;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.GpsCallback;
import com.techinnovators.srcm.utils.LocationUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.utils.PermissionUtils;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TaskActivity2 extends AppCompatActivity {

    private RecyclerView rvTasks;

    private LinearLayout llEmptyView;

    private ImageView ivAdd,
            ivCheckIn,
            ivCheckOut,
            ivWorkingHours,
            ivLogout,
            ivSync;

    private ConstraintLayout csMain;

    private TextView tvWorkingHours;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TasksAdapter tasksAdapter;

    private ArrayList<Tasks> tasksList;

    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        init();
        initClickListener();
        initData();
    }

    private void init() {
        llEmptyView = findViewById(R.id.llEmptyView);
        csMain = findViewById(R.id.csMain);
        ivAdd = findViewById(R.id.ivAdd);

        ivWorkingHours = findViewById(R.id.ivWorkHour);
        tvWorkingHours = findViewById(R.id.tvWorkingHours);

        ivCheckIn = findViewById(R.id.ivCheckIn);
        ivCheckOut = findViewById(R.id.ivCheckOut);

        ivSync = findViewById(R.id.ivSync);
        ivLogout = findViewById(R.id.ivLogout);

        ivAdd = findViewById(R.id.ivAdd);

        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black));

        setCountDownTimer();
        setAttendance();

        if(!PermissionUtils.hasLocationPermission()){
            PermissionUtils.requestLocationPermission(this);
        }
    }

    private void initClickListener() {
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddTap();
            }
        });

        ivCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkIn();
            }
        });

        ivCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double hoursFromMinutes;
                double totalWorkingHours = 0.0;
                if (NetworkUtils.isNetworkConnected(TaskActivity2.this)) {
                    String strFirstCheckInTime = Application.getUserModel().firstCheckin;

                    if (!TextUtils.isEmpty(strFirstCheckInTime) && !strFirstCheckInTime.equals("0.0")) {

                        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                        long timeDiff;
                        long hours;
                        long minutes;

                        try {
                            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault());

                            Date firstCheckInDate = dateFormat.parse(strFirstCheckInTime);
                            String strCurrentTime = simpleTimeFormat.format(new Date());
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
                                checkOut(strCurrentTime, totalWorkingHours);
                            } else {
                                AppUtils.showSnackBar(TaskActivity2.this, csMain, "Total working hours cannot be 0 while checking out from system.");
                            }
                        } catch (ParseException e) {
                            AppUtils.showSnackBar(TaskActivity2.this, csMain, "Unable to find working hours from the app. Please contact tech support");
                        }
                    } else {
                        AppUtils.showSnackBar(TaskActivity2.this, csMain, "Unable to find check in time from system. Please contact tech support.");
                    }
                } else {
                    AppUtils.showSnackBar(TaskActivity2.this, csMain, getString(R.string.internet_off));
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            refreshTaskList();
        });

        ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        ivSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                syncData();
            }
        });
    }

    private void initData() {
        syncData();
        getUserDetails();
    }

    private void setCountDownTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE,1000) {
            @Override
            public void onTick(long l) {
                displayWorkingHours();
            }

            @Override
            public void onFinish() {
                Log.e("countdown","finish");
            }
        };
    }

    private void setAttendance() {
        final UserModel model = Application.getUserModel();

        if (!TextUtils.isEmpty(model.attendanceDate)) {
            ivCheckOut.setVisibility(View.VISIBLE);
            String strAttendanceDate = model.attendanceDate;

            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());
            String strCurrentDate = simpleTimeFormat.format(new Date());

            if (!strAttendanceDate.equals(strCurrentDate)) {

                model.attendanceDate = "";
                model.name = "";
                model.firstCheckin = "";
                model.lastCheckOut = "";
                model.checkIn = 0;
                model.checkOut = 0;

                DbClient.getInstance().userDao().update(model);
                ivCheckIn.setVisibility(View.VISIBLE);
                ivCheckOut.setVisibility(View.GONE);
            } else {
                if (model.checkIn == 1 && model.checkOut == 1) {
                    ivCheckIn.setVisibility(View.GONE);
                    ivCheckOut.setVisibility(View.GONE);
                } else if (model.checkIn == 0 && model.checkOut == 0) {
                    ivCheckIn.setVisibility(View.VISIBLE);
                    ivCheckOut.setVisibility(View.GONE);
                } else if (model.checkIn == 1) {
                    ivCheckIn.setVisibility(View.GONE);
                    ivCheckOut.setVisibility(View.VISIBLE);
                    startTimer();
                }
            }
        } else {
            ivCheckIn.setVisibility(View.VISIBLE);
        }
    }

    private void logout(){
        DbClient.getInstance().clearAllTables();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startTimer() {
        countDownTimer.start();
    }

    private void stopTimer() {
        countDownTimer.cancel();
    }

    private void getUserDetails() {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                AppUtils.showProgress(this, getString(R.string.prog_dialog_title));

                final String api = getString(R.string.api_track_user_details);

                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        AppUtils.dismissProgress();
                        ArrayList<String> appModules = new ArrayList<>();

                        try {
                            JSONArray jsonArray = response.getJSONArray(getString(R.string.param_message));

                            if (jsonArray.length() > 0) {
                                for (int index = 0; index < jsonArray.length(); index++) {
                                    try {
                                        JSONObject jsonObject = jsonArray.getJSONObject(index);

                                        int enabled = jsonObject.getInt(getString(R.string.enabled_param_key));

                                        if (enabled == 0) {
//                                        logout();
                                        } else if (enabled == 1) {
                                            final UserModel model = Application.getUserModel();

                                            model.apiKey = jsonObject.optString(getString(R.string.param_api_key));
                                            model.apiSecret = jsonObject.optString(getString(R.string.param_api_secret));
                                            model.employeeId = jsonObject.optString(getString(R.string.param_employee_id));
                                            model.company = jsonObject.optString(getString(R.string.param_company));
                                            model.token = String.format(getString(R.string.param_token_value), model.apiKey, model.apiSecret);

                                            DbClient.getInstance().userDao().update(model);

                                            String strAppModule = jsonObject.getString(getString(R.string.param_user_app_module));

                                            if (!strAppModule.isEmpty()) {
                                                if (strAppModule.contains(getString(R.string.param_visit_request_key))) {
                                                    appModules.add(getString(R.string.param_visit_request_value));
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", e.getMessage());
                                        e.printStackTrace();
                                    }
                                }

                                if (!appModules.isEmpty()) {
                                    for (int index = 0; index < appModules.size(); index++) {
                                        String strVisitRequest = appModules.get(index);
                                        if (!TextUtils.isEmpty(strVisitRequest)) {
                                            getTasksList(Application.getUserModel().employeeId);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException jsonException) {
                            AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", jsonException.getMessage());
                        }
                    }

                    @Override
                    public void notifyError(VolleyError error) {
                        AppUtils.dismissProgress();

                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 401:
                                    String responseBody;
                                    try {
                                        responseBody = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(responseBody);
                                        if (!data.getString("message").isEmpty()) {
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", getString(R.string.error_403));
                                case 404:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", getString(R.string.error_404));
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", getString(R.string.error_500));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {
                        AppUtils.dismissProgress();
                    }
                };

                final VolleyService volleyService = new VolleyService(callback, this);

                /// Params
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(getString(R.string.param_usr), Application.getUserModel().userName);
                jsonObject.put(getString(R.string.param_app_auth_key), getString(R.string.auth_key));

                volleyService.postDataVolley(api, jsonObject);

            } catch (Exception e) {
                AppUtils.dismissProgress();
                AppUtils.displayAlertMessage(this, "TASKS", e.getMessage());
            }
        } else {
            getTasksList(Application.getUserModel().employeeId);
        }
    }

    private void getTasksList(String fsEmpName) {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                AppUtils.showProgress(this, getString(R.string.prog_dialog_title));

                String apiUrl = getString(R.string.api_visit_requests);

                String fields_key = getString(R.string.param_fields);
                String fields_value = getString(R.string.fields_value);
                String filters_key = getString(R.string.param_filters);

                String strToday = AppUtils.dispCurrentDate();

                String filters_value = "[[\"employee\", \"=\"," + '"' + fsEmpName + '"' + "],[\"visit_expiry_date\",\">=\"," +
                        '"' + strToday + '"' + "]]";

                apiUrl += fields_key + fields_value + "&" + filters_key + filters_value;

                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        AppUtils.dismissProgress();
                        mSwipeRefreshLayout.setRefreshing(false);

                        try {
                            Gson gson = new Gson();

                            final JSONArray jsonArray = response.getJSONArray(getString(R.string.param_data));
                            if (jsonArray.length() > 0) {
                                Log.v("Tasks Response ===> ", jsonArray.toString());

                                Type listType = new TypeToken<TasksReponse>() {}.getType();

                                TasksReponse tasksResponse = gson.fromJson(response.toString(), listType);

                                tasksList = (ArrayList<Tasks>) tasksResponse.getData();

                                for(int i = 0; i< tasksList.size();i++){
                                    final Tasks t = tasksList.get(i);

                                    if(t.visit_checkin != null && !t.visit_checkin.isEmpty()){
                                       t.isCheckInSync = true;
                                    }

                                    if(t.visit_checkout != null && !t.visit_checkout.isEmpty()){
                                        t.isCheckOutSync = true;
                                    }

                                    t.isSync = true;
                                }

                                setTasksList(tasksList);

                                DbClient.getInstance().tasksDao().deleteAll();

                                DbClient.getInstance().tasksDao().insertAll(tasksList);

                            } else {
                                setTaskListFromLocal();
                            }
                        } catch (JSONException jsonException) {
                            AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", jsonException.getMessage());
                        }
                    }

                    @Override
                    public void notifyError(VolleyError error) {
                        AppUtils.dismissProgress();
                        mSwipeRefreshLayout.setRefreshing(false);

                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 401:
                                    String responseBody;
                                    try {
                                        responseBody = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(responseBody);
                                        if (!data.getString("message").isEmpty()) {
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 404:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS",
                                            getString(R.string.error_404));
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS",
                                            getString(R.string.error_500));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {
                        AppUtils.dismissProgress();
                    }
                };


                final VolleyService volleyService = new VolleyService(callback, this);
                volleyService.getDataVolley(apiUrl, null);

            } catch (Exception e) {
                AppUtils.dismissProgress();
                mSwipeRefreshLayout.setRefreshing(false);
                AppUtils.displayAlertMessage(this, "TASKS", e.getMessage());
            }
        } else {
            /// set task list from local storage
            final ArrayList<Tasks> list = (ArrayList<Tasks>) DbClient.getInstance().tasksDao().getAll();
            if (list.isEmpty()) {
                llEmptyView.setVisibility(View.VISIBLE);
                rvTasks.setVisibility(View.GONE);
            } else {
                setTasksList(list);
            }

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onAddTap() {
        startActivityForResult(new Intent(this, AddVisitRequestActivity2.class), 100);
    }

    private void setTaskListFromLocal() {
        final ArrayList<Tasks> list = (ArrayList<Tasks>) DbClient.getInstance().tasksDao().getAll();
        if (list.isEmpty()) {
            llEmptyView.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            setTasksList(list);
        }
    }

    private void setTasksList(ArrayList<Tasks> data) {
        if (tasksAdapter == null) {
            Collections.sort(data, new Comparator<Tasks>() {
                public int compare(Tasks o1, Tasks o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            tasksAdapter = new TasksAdapter(this, data);
            rvTasks.setAdapter(tasksAdapter);
        } else {
            tasksAdapter.updateList(data);
        }
    }

    private void refreshTaskList() {
        getTasksList(Application.getUserModel().employeeId);
    }

    private void checkIn() {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                AppUtils.showProgress(this, getString(R.string.prog_dialog_title));

                final APIVInterface callBack = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        AppUtils.dismissProgress();
                        try {
                            JSONObject jsonData = response.getJSONObject(getString(R.string.data_param_key));

                            UserModel model = Application.getUserModel();

                            String strFirstCheckIn = jsonData.getString(getString(R.string.first_checkin_param_key));
                            String strEmployee = jsonData.getString(getString(R.string.employee_param_key));
                            String strAttendanceDate = jsonData.getString(getString(R.string.attendance_date_param_key));
                            String strName = jsonData.getString(getString(R.string.name_param_key));

                            if (!TextUtils.isEmpty(strFirstCheckIn)) {
                                model.firstCheckin = strFirstCheckIn;
                            }
                            if (!TextUtils.isEmpty(strEmployee)) {
                                model.employee = strEmployee;
                            }
                            if (!TextUtils.isEmpty(strAttendanceDate)) {
                                model.attendanceDate = strAttendanceDate;
                            }
                            if (!TextUtils.isEmpty(strName)) {
                                model.name = strName;
                            }

                            model.checkIn = 1;
                            DbClient.getInstance().userDao().update(model);

                            ivCheckIn.setVisibility(View.GONE);
                            ivCheckOut.setVisibility(View.VISIBLE);

                            startTimer();

                            AppUtils.showSnackBar(TaskActivity2.this, csMain, "You have checked in successfully in system at" + " " + strFirstCheckIn + ".");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void notifyError(VolleyError error) {
                        AppUtils.dismissProgress();
                        String errorMessage = "";
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        errorMessage = data.getString("message");
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    errorMessage = e.getMessage();
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                errorMessage = getString(R.string.error_403);
                                break;
                            case 404:
                                errorMessage = getString(R.string.error_404);
                                break;
                            case 417:
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        errorMessage = data.getString("_server_messages");
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    errorMessage = e.getMessage();
                                    e.printStackTrace();
                                }
                                break;
                            case 500:
                                errorMessage = getString(R.string.error_500);
                                break;
                        }
                        AppUtils.displayAlertMessage(TaskActivity2.this, "Api Error", errorMessage);
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {
                        AppUtils.dismissProgress();
                    }
                };

                String apiUrl = getString(R.string.api_attendance);

                final VolleyService volleyService = new VolleyService(callBack, this);
                final HashMap<String, Object> map = new HashMap<>();

                SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault());
                String strCurrentTime = simpleTimeFormat.format(new Date());

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());
                String strCurrentDate = simpleDateFormat.format(new Date());

                map.put(getString(R.string.attendance_date_param_key), strCurrentDate);
                map.put(getString(R.string.first_checkin_param_key), strCurrentTime);
                map.put(getString(R.string.company_param_key), Application.getUserModel().company);
                map.put(getString(R.string.docstatus_param_key), 0);
                map.put(getString(R.string.doctype_param_key), getString(R.string.attendance_param_value));
                map.put(getString(R.string.employee_param_key), Application.getUserModel().employeeId);
                map.put(getString(R.string.last_checkout_param_key), "");
                map.put(getString(R.string.status_param_key), getString(R.string.attendance_default_status));
                map.put(getString(R.string.total_working_hours_param_key), 0.0);

                volleyService.postDataVolley(apiUrl, new JSONObject(map));
            } catch (Exception e) {
                Log.e("Error on check in", e.getMessage());
                AppUtils.displayAlertMessage(this, "TASKS", e.getMessage());
            }
        } else {
            AppUtils.showSnackBar(this, csMain, getString(R.string.internet_off));
        }
    }

    private void checkOut(String fsLastCheckOutTime, Double fdWorkingHours) {
        String apiUrl = getString(R.string.api_attendance);
        apiUrl += "/" + Application.getUserModel().name;

        JSONObject jsonRequest = new JSONObject();

        AppUtils.showProgress(this, getString(R.string.prog_dialog_title));

        try {

            RequestQueue queue = Volley.newRequestQueue(this);
            jsonRequest.put(getString(R.string.docstatus_param_key), 1);
            jsonRequest.put(getString(R.string.last_checkout_param_key), fsLastCheckOutTime);
            jsonRequest.put(getString(R.string.total_working_hours_param_key), fdWorkingHours);

            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, apiUrl, jsonRequest,
                    (JSONObject response) -> {
                        AppUtils.dismissProgress();
                        try {
                            JSONObject dataResponse = response.getJSONObject(getString(R.string.data_param_key));
                            if (!dataResponse.toString().isEmpty()) {
                                ivCheckIn.setVisibility(View.VISIBLE);
                                ivCheckOut.setVisibility(View.GONE);

                                UserModel model = Application.getUserModel();

                                model.name = "";
                                model.firstCheckin = "";

                                String strLastCheckout = dataResponse.getString(getString(R.string.last_checkout_param_key));
                                if (!TextUtils.isEmpty(strLastCheckout)) {
                                    model.lastCheckOut = strLastCheckout;
                                }
                                model.checkOut = 1;
                                stopTimer();
                                AppUtils.showSnackBar(this, csMain, "You have checked out from system at" + strLastCheckout + ".");
                            }
                        } catch (JSONException e) {
                            AppUtils.dismissProgress();
                            e.printStackTrace();
                            AppUtils.displayAlertMessage(this, "CHECK OUT", e.getMessage());
                        }
                    },
                    error -> {
                        AppUtils.dismissProgress();
                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 401:
                                    String responseBody;
                                    try {
                                        responseBody = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(responseBody);
                                        if (!data.getString("message").isEmpty()) {
                                            AppUtils.displayAlertMessage(this, "CHECK OUT", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(this, "CHECK OUT", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                    AppUtils.displayAlertMessage(this, "CHECK OUT", getString(R.string.error_403));
                                case 404:
                                    AppUtils.displayAlertMessage(this, "CHECK OUT", getString(R.string.error_404));
                                    break;
                                case 417:
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString(getString(R.string._server_messages_param_key)).isEmpty()) {
                                            AppUtils.displayAlertMessage(this, "CHECK OUT", data.getString("_server_messages"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(this, "CHECK OUT", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(this, "CHECK OUT", getString(R.string.error_500));
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
                    if (!Application.getUserModel().token.equals("")) {
                        params.put("Authorization", Application.getUserModel().token);
                    }
                    params.put("Content-Type", "application/json");
                    params.put("Accept", "application/json");
                    return params;
                }
            };
            queue.add(putRequest);
        } catch (JSONException e) {
            AppUtils.dismissProgress();
            AppUtils.displayAlertMessage(this, "CHECK OUT", e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayWorkingHours() {
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
            Date firstCheckInDate = dateFormat.parse(Application.getUserModel().firstCheckin);
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
            tvWorkingHours.setVisibility(View.VISIBLE);
            ivWorkingHours.setVisibility(View.VISIBLE);
            tvWorkingHours.setText(strWorkHours);
        } catch (ParseException e) {
            AppUtils.showSnackBar(TaskActivity2.this, csMain, "Unable to find working hours from the app.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            refreshTaskList();
        }
    }

    private void syncData(){
        if(NetworkUtils.isNetworkConnected(this)){
            AppUtils.showProgress(this,"Sync data...");
            final ArrayList<Tasks> tasksDbList  = (ArrayList<Tasks>) DbClient.getInstance().tasksDao().getAll();
            boolean hasData = false;
            for (int i = 0;i < tasksDbList.size();i++){
                final Tasks t = tasksDbList.get(i);

                if(!t.isSync){
                    hasData = true;
                    createTaskFromSync(t);
                }
                if(!t.isCheckInSync){
                    hasData = true;
                    checkInTaskFromSync(t);
                }
                if(!t.isCheckOutSync){
                    hasData = true;
                    checkOutTaskFromSync(t);
                }
            }

            AppUtils.dismissProgress();

            if(!hasData){
                AppUtils.displayAlertMessage(this,"Sync","All data is synced");
            }
        }else{
            AppUtils.displayAlertMessage(this,"Error","No Internet");
        }
    }

    private void createTaskFromSync(Tasks t){

        String apiUrl = getString(R.string.api_create_task);

        final APIVInterface callback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                t.isSync = true;
                DbClient.getInstance().tasksDao().update(t);
            }

            @Override
            public void notifyError(VolleyError error) {
                Log.e("Error on create task",error.toString());
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {
                Log.e("Error on create task",response.toString());
            }
        };

        final VolleyService mVolleyService = new VolleyService(callback, this);
        mVolleyService.postDataVolley(apiUrl, t.createTaskJson());
    }
    private void checkInTaskFromSync(Tasks t){
        try{

            String api = getString(R.string.api_check_in);
            api += "/" + t.name;

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    /// set data local
                    t.isCheckInSync = true;
                    DbClient.getInstance().tasksDao().update(t);
                }

                @Override
                public void notifyError(VolleyError error) {
                    Log.e("Error on check in",error.toString());
                }

                @SuppressLint("LongLogTag")
                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    Log.e("Error on check in parse response",response.toString());
                }
            };

            final VolleyService volleyService = new VolleyService(callback,this);
            volleyService.putDataVolley(api,t.checkInJson());

        }catch (Exception e){
            Log.e("Error on api check in",e.getMessage());
        }
    }
    public void checkOutTaskFromSync(Tasks t){
        try{
            String api = getString(R.string.api_check_out);
            api += "/" + t.name;

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    /// set data local
                    t.isCheckOutSync = true;

                    DbClient.getInstance().tasksDao().update(t);

                }

                @Override
                public void notifyError(VolleyError error) {
                    Log.e("Check Out Error",error.toString());
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    Log.e("Check out Error",response.toString());
                }
            };

            final VolleyService volleyService = new VolleyService(callback,this);
            volleyService.putDataVolley(api,t.checkOutJson());
        }catch (Exception e){
            Log.e("Error on api check out",e.getMessage());
        }
    }
}