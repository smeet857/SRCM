package com.techinnovators.srcm.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sangcomz.fishbun.FishBun;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.adapter.TasksAdapter;
import com.techinnovators.srcm.models.EventCategory;
import com.techinnovators.srcm.models.Taluka;
import com.techinnovators.srcm.models.TalukaResponse;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.TasksReponse;
import com.techinnovators.srcm.models.UserModel;
import com.techinnovators.srcm.models.VisitDistrict;
import com.techinnovators.srcm.models.VisitDistrictResponse;
import com.techinnovators.srcm.models.VisitLocation;
import com.techinnovators.srcm.models.VisitLocationResponse;
import com.techinnovators.srcm.utils.AppUtils;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class TaskActivity2 extends AppCompatActivity {

    private RecyclerView rvTasks;
    TasksAdapter tasksAdapter;
    ArrayList<Tasks> data;
    ArrayList<Uri> path;
    MutableLiveData<ArrayList<Uri>> imagePathsListener = new MutableLiveData<>();

    private LinearLayout llEmptyView;

    private AppCompatTextView ivCheckIn,
            ivCheckOut;
    private ImageView ivAdd,
            ivWorkingHours,
            ivLogout,
            ivSync;

    private ConstraintLayout csMain;

    private TextView tvWorkingHours;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<Tasks> tasksList;

    private CountDownTimer countDownTimer;

    private DbClient db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        Application.context = this;

        init();
        initClickListener();
        initData();
    }

    @Override
    protected void onResume() {
        Application.context = this;
        super.onResume();
    }


    private void init() {
        db = DbClient.getInstance();
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

        if (!PermissionUtils.hasLocationPermission()) {
            PermissionUtils.requestLocationPermission(this);
        }
    }

    private void initClickListener() {
        ivAdd.setOnClickListener(view -> onAddTap());

        ivCheckIn.setOnClickListener(view -> checkIn());

        ivCheckOut.setOnClickListener(view -> {
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
        });

        mSwipeRefreshLayout.setOnRefreshListener(this::refreshTaskList);

        ivLogout.setOnClickListener(view -> logout());

        ivSync.setOnClickListener(view -> {
            if (NetworkUtils.isNetworkConnected(TaskActivity2.this)) {
                NetworkUtils.syncData((error) -> {
                    if (error) {
                        AppUtils.displayAlertMessage(TaskActivity2.this, "Sync Data", "Some data is not sync proper please sync again");
                    } else {
                        AppUtils.displayAlertMessage(TaskActivity2.this, "Sync Data", "All data is synced");
                    }
                });
            } else {
                AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", "No internet connectivity");
            }
        });
    }

    private void initData() {
        if (NetworkUtils.isNetworkConnected(this)) {
            NetworkUtils.syncData((error) -> {
                if (error) {
                    AppUtils.displayAlertMessage(TaskActivity2.this, "Sync Data", "Some data is not sync proper please sync again");
                    getUserDetails(true);
                } else {
                    getUserDetails(false);
                }
            });
        } else {
            getUserDetails(false);
        }
    }

    private void setTaskCategories() {
        getEventSectors();
        getEventCategories();
        getEventTypes();
        getOrganisationName();
        getVisitState();
        getDistrict();
        getTaluka();
        getLocationOfVisit();
    }

    private void setCountDownTimer() {
        countDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long l) {
                displayWorkingHours();
            }

            @Override
            public void onFinish() {
                Log.e("countdown", "finish");
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

    private void logout() {
        DbClient.getInstance().clearAllTables();
        Application.isLogin = false;
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

    private void getUserDetails(boolean gettaskListFromLocal) {
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
                                            logout();
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
                                            if (gettaskListFromLocal) {
                                                setTaskListFromLocal();
                                            } else {
                                                getTasksList(Application.getUserModel().employeeId);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException jsonException) {
                            AppUtils.displayAlertMessage(TaskActivity2.this, "TASKS", jsonException.getMessage());
                        }

                        setTaskCategories();
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
                                {
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString("_server_messages").isEmpty()) {
                                            final String message = data.getString("_server_messages");

                                            JSONArray jsonArray = new JSONArray(message);
                                            JSONObject jo = new JSONObject(jsonArray.getString(0));
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                        e.printStackTrace();
                                    }
                                    break;
                                }                                case 404:
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
            setTaskCategories();
        }
    }

    private synchronized void getTasksList(String fsEmpName) {
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

                                Type listType = new TypeToken<TasksReponse>() {
                                }.getType();

                                TasksReponse tasksResponse = gson.fromJson(response.toString(), listType);

                                tasksList = (ArrayList<Tasks>) tasksResponse.getData();

                                for (int i = 0; i < tasksList.size(); i++) {
                                    final Tasks t = tasksList.get(i);

                                    if (t.visit_checkin != null && !t.visit_checkin.isEmpty()) {
                                        t.isCheckInSync = true;
                                    }

                                    if (t.visit_checkout != null && !t.visit_checkout.isEmpty()) {
                                        t.isCheckOutSync = true;
                                    }

                                    t.isSync = true;
                                }

                                DbClient.getInstance().tasksDao().deleteAll();
                                DbClient.getInstance().tasksDao().insertAll(tasksList);

                                setTaskListFromLocal();
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
            setTaskListFromLocal();

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setTaskListFromLocal() {
        /// set task list from local storage
        final ArrayList<Tasks> list = (ArrayList<Tasks>) DbClient.getInstance().tasksDao().getAll();
        if (list.isEmpty()) {
            llEmptyView.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            setTasksList(list);
        }
    }

    private void onAddTap() {
        startActivityForResult(new Intent(this, AddVisitRequestActivity2.class), 100);
    }

    private void setTasksList(ArrayList<Tasks> data) {
        if (data.isEmpty()) {
            llEmptyView.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            llEmptyView.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);

            Collections.sort(data, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));

            this.data = data;
            tasksAdapter = new TasksAdapter(this, this, data, imagePathsListener);
            rvTasks.setAdapter(tasksAdapter);
        }
    }

    private void refreshTaskList() {
        if (NetworkUtils.isNetworkConnected(this)) {
            NetworkUtils.syncData((error) -> {
                if (error) {
                    AppUtils.displayAlertMessage(TaskActivity2.this, "Sync Data", "Some data is not sync proper please sync again");
                    getUserDetails(true);
                } else {
                    getUserDetails(false);
                }
            });
        } else {
            getUserDetails(false);
        }
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
                            case 403: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));

                                        errorMessage = Html.fromHtml(jo.getString("message")).toString();
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    errorMessage = getString(R.string.error_403);
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case 404:
                                errorMessage = getString(R.string.error_404);
                                break;
                            case 417: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));

                                        errorMessage = Html.fromHtml(jo.getString("message")).toString();
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    errorMessage = e.getMessage();
                                    e.printStackTrace();
                                }
                                break;
                            }
                            case 500:
                                errorMessage = getString(R.string.error_500);
                                break;
                        }
                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", errorMessage);
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
                                tvWorkingHours.setVisibility(View.GONE);
                                ivWorkingHours.setVisibility(View.GONE);

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
                                case 403: {
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString("_server_messages").isEmpty()) {
                                            final String message = data.getString("_server_messages");

                                            JSONArray jsonArray = new JSONArray(message);
                                            JSONObject jo = new JSONObject(jsonArray.getString(0));
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                        e.printStackTrace();
                                    }
                                    break;
                                }
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
        if (requestCode == 1010 && resultCode == RESULT_OK) {
            // path = imageData.getStringArrayListExtra(Define.INTENT_PATH);
            // you can get an image path(ArrayList<String>) on <0.6.2

            path = data.getParcelableArrayListExtra(FishBun.INTENT_PATH);
            // you can get an image path(ArrayList<Uri>) on 0.6.2 and later
            Log.e("images", path.toString());
            imagePathsListener.setValue(path);
        }
    }

    private void getEventSectors() {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                String apiUrl = getString(R.string.api_event_sector);

                /// Params
                apiUrl += "?limit_page_length=None";

                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        try {
                            JSONArray data = response.getJSONArray(getString(R.string.param_data));

                            if (data.length() > 0) {
                                final ArrayList<String> arrayList = new ArrayList<>();

                                for (int index = 0; index < data.length(); index++) {

                                    JSONObject jsonObject = data.getJSONObject(index);

                                    if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                        String strName = jsonObject.getString(getString(R.string.param_name));

                                        if (!strName.isEmpty()) {
                                            arrayList.add(strName);
                                        }
                                    }
                                }

                                /// set in local storage
                                Application.getUserModel().eventSector = arrayList.toString();
                                db.userDao().update(Application.getUserModel());
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
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                {
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString("_server_messages").isEmpty()) {
                                            final String message = data.getString("_server_messages");

                                            JSONArray jsonArray = new JSONArray(message);
                                            JSONObject jo = new JSONObject(jsonArray.getString(0));
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                case 404:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", getString(R.string.error_404));
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", getString(R.string.error_500));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {

                    }
                };

                VolleyService mVolleyService = new VolleyService(callback, this);
                mVolleyService.getDataVolley(apiUrl, null);

            } catch (Exception e) {
                AppUtils.displayAlertMessage(this, "PROJECT NAME", e.getMessage());
            }
        }
    }

    private void getEventCategories() {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                //String apiUrl = getString(R.string.api_project_name);
                String apiUrl = getString(R.string.api_all_event_category);

                /// Params
                apiUrl += "?limit_page_length=None";

                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject(getString(R.string.param_message));

                            final ArrayList<EventCategory> finalList = new ArrayList<>();
                            for (Iterator it = data.keys(); it.hasNext(); ) {
                                String name = (String) it.next();
                                JSONArray arr = data.optJSONArray(name);

                                EventCategory eventCategory = new EventCategory();
                                eventCategory.setName(name);

                                ArrayList<String> type = new ArrayList<>();

                                for (int i = 0; i < arr.length(); i++) {
                                    type.add((String) arr.get(i));
                                }

                                eventCategory.setTypes(type);

                                finalList.add(eventCategory);
                            }

                            Gson gson = new Gson();
                            String result = gson.toJson(finalList, new TypeToken<ArrayList<EventCategory>>() {
                            }.getType());
                            Application.getUserModel().eventCategories = result;
                            db.userDao().update(Application.getUserModel());
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
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                {
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString("_server_messages").isEmpty()) {
                                            final String message = data.getString("_server_messages");

                                            JSONArray jsonArray = new JSONArray(message);
                                            JSONObject jo = new JSONObject(jsonArray.getString(0));
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                case 404:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", getString(R.string.error_404));
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", getString(R.string.error_500));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {

                    }
                };

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(getString(R.string.param_app_auth_key), getString(R.string.auth_key));

                VolleyService mVolleyService = new VolleyService(callback, this);
                mVolleyService.postDataVolley(apiUrl, jsonObject);

            } catch (Exception e) {
                AppUtils.displayAlertMessage(this, "PROJECT NAME", e.getMessage());
            }
        }
    }

    private void getEventTypes() {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                //String apiUrl = getString(R.string.api_project_name);
                String apiUrl = getString(R.string.api_all_project_type);

                /// Params
                apiUrl += "?limit_page_length=None";

                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        try {
                            JSONObject data = response.getJSONObject(getString(R.string.param_message));

                            final ArrayList<EventCategory> finalList = new ArrayList<>();
                            for (Iterator it = data.keys(); it.hasNext(); ) {
                                String name = (String) it.next();
                                JSONArray arr = data.optJSONArray(name);

                                EventCategory eventCategory = new EventCategory();
                                eventCategory.setName(name);

                                ArrayList<String> type = new ArrayList<>();

                                for (int i = 0; i < arr.length(); i++) {
                                    type.add((String) arr.get(i));
                                }

                                eventCategory.setTypes(type);

                                finalList.add(eventCategory);
                            }

                            Gson gson = new Gson();
                            String result = gson.toJson(finalList, new TypeToken<ArrayList<EventCategory>>() {
                            }.getType());
                            Application.getUserModel().projectTypes = result;
                            db.userDao().update(Application.getUserModel());
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
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                {
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString("_server_messages").isEmpty()) {
                                            final String message = data.getString("_server_messages");

                                            JSONArray jsonArray = new JSONArray(message);
                                            JSONObject jo = new JSONObject(jsonArray.getString(0));
                                            AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                        e.printStackTrace();
                                    }
                                    break;
                                }                                case 404:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", getString(R.string.error_404));
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "PROJECT NAME", getString(R.string.error_500));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {

                    }
                };

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(getString(R.string.param_app_auth_key), getString(R.string.auth_key));

                VolleyService mVolleyService = new VolleyService(callback, this);
                mVolleyService.postDataVolley(apiUrl, jsonObject);

            } catch (Exception e) {
                AppUtils.displayAlertMessage(this, "PROJECT NAME", e.getMessage());
            }
        }
    }

    private void getOrganisationName() {
        if (NetworkUtils.isNetworkConnected(this)) {
            String apiUrl = getString(R.string.api_organization_name);

            apiUrl += "?limit_page_length=None";

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray(getString(R.string.param_data));
                        if (data.length() > 0) {
                            final ArrayList<String> arrayList = new ArrayList<>();

                            for (int index = 0; index < data.length(); index++) {
                                JSONObject jsonObject = data.getJSONObject(index);
                                if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                    String strName = jsonObject.getString(getString(R.string.param_name));
                                    if (!strName.isEmpty()) {
                                        arrayList.add(strName);
                                    }
                                }
                            }


                            Application.getUserModel().organizationName = arrayList.toString();
                            db.userDao().update(Application.getUserModel());

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
                                        AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.orgname), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.orgname), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                            {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }                            case 404:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.orgname), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.orgname), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, TaskActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.orgname), e.getMessage());
            }
        }
    }

    private void getVisitState() {
        if (NetworkUtils.isNetworkConnected(this)) {
            String apiUrl = getString(R.string.api_visit_state);

            apiUrl += "?limit_page_length=None";

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                        if (data.length() > 0) {
                            ArrayList<String> arrayList = new ArrayList<>();
                            for (int index = 0; index < data.length(); index++) {
                                JSONObject jsonObject = data.getJSONObject(index);
                                if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                    String strName = jsonObject.getString(getString(R.string.name_param_key));
                                    if (!strName.isEmpty()) {
                                        arrayList.add(strName);
                                    }
                                }
                            }

                            Application.getUserModel().visitState = arrayList.toString();
                            db.userDao().update(Application.getUserModel());

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
                                        AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.state), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.state), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                            {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }                            case 404:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.state), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.state), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, TaskActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.state), e.getMessage());
            }
        }
    }

    private void getDistrict() {
        if (NetworkUtils.isNetworkConnected(this)) {
            String apiUrl = getString(R.string.api_district);

            String fields = getString(R.string.fields_param_key);
            String fieldsValue = getString(R.string.fields_value_district);

            apiUrl += "?limit_page_length=None&" + fields + fieldsValue;

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                        if (data.length() > 0) {

                            Gson gson = new Gson();

                            Type listType = new TypeToken<VisitDistrictResponse>() {
                            }.getType();

                            VisitDistrictResponse tasksResponse = gson.fromJson(response.toString(), listType);
                            ArrayList<VisitDistrict> vdList = new ArrayList<>(tasksResponse.getData());

                            if (!vdList.isEmpty()) {
                                ArrayList<String> arrayJson = new ArrayList<>();

                                for (int index = 0; index < vdList.size(); index++) {
                                    final VisitDistrict vd = vdList.get(index);
                                    arrayJson.add(gson.toJson(vd));
                                }

                                Application.getUserModel().district = arrayJson.toString();
                                db.userDao().update(Application.getUserModel());
                            }
                        }
                    } catch (
                            JSONException jsonException) {
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
                                        AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                            {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }                            case 404:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, TaskActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), e.getMessage());
            }
        }
    }

    private void getTaluka() {
        if (NetworkUtils.isNetworkConnected(this)) {
            String apiUrl = getString(R.string.api_taluka);

            String fields = getString(R.string.fields_param_key);
            String fieldsValue = getString(R.string.fields_value_taluka);

            apiUrl += "?limit_page_length=None&" + fields + fieldsValue;

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray(getString(R.string.param_data));

                        if (data.length() > 0) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<TalukaResponse>() {
                            }.getType();

                            TalukaResponse tasksResponse = gson.fromJson(response.toString(), listType);
                            ArrayList<Taluka> talukas = new ArrayList<>(tasksResponse.getData());

                            if (!talukas.isEmpty()) {
                                ArrayList<String> arrayJson = new ArrayList<>();
                                for (int index = 0; index < talukas.size(); index++) {
                                    final Taluka taluka = talukas.get(index);
                                    arrayJson.add(gson.toJson(taluka));
                                }

                                Application.getUserModel().taluka = arrayJson.toString();
                                db.userDao().update(Application.getUserModel());
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
                                        AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                            {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }                            case 404:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, TaskActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), e.getMessage());
            }
        }
    }

    private void getLocationOfVisit() {
        if (NetworkUtils.isNetworkConnected(this)) {
            String apiUrl = getString(R.string.api_visit_location);

            String fields = getString(R.string.fields_param_key);
            String fieldsValue = getString(R.string.fields_value_location);

            apiUrl += "?limit_page_length=None&" + fields + fieldsValue;

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                        if (data.length() > 0) {

                            Gson gson = new Gson();
                            Type listType = new TypeToken<VisitLocationResponse>() {
                            }.getType();

                            VisitLocationResponse tasksResponse = gson.fromJson(response.toString(), listType);
                            final ArrayList<VisitLocation> vl = new ArrayList<>(tasksResponse.getData());

                            if (!vl.isEmpty()) {
                                ArrayList<String> arrayJson = new ArrayList<>();
                                for (int index = 0; index < vl.size(); index++) {
                                    final VisitLocation visitLocation = vl.get(index);
                                    arrayJson.add(gson.toJson(visitLocation));
                                }

                                Application.getUserModel().locationOfVisit = arrayJson.toString();
                                db.userDao().update(Application.getUserModel());
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
                                        AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visitlocation), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                            {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(TaskActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }                            case 404:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visitlocation), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visitlocation), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, TaskActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(TaskActivity2.this, getString(R.string.visitlocation), e.getMessage());
            }
        }
    }
}