package com.techinnovators.srcm.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techinnovators.srcm.AddVisitRequestActivity;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.adapter.TasksAdapter;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.TasksReponse;
import com.techinnovators.srcm.models.UserModel;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TaskActivity2 extends AppCompatActivity {

    private RecyclerView rvTasks;
    private LinearLayout llEmptyView;
    private ImageView ivAdd;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TasksAdapter tasksAdapter;

    private ArrayList<Tasks> tasksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        init();
        initClickListener();
        initData();
    }

    private void init(){
        llEmptyView = findViewById(R.id.llEmptyView);
        ivAdd = findViewById(R.id.ivAdd);

        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        mSwipeRefreshLayout = findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.black));
    }

    private void initClickListener(){
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddTap();
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            refreshTaskList();
        });
    }

    private void initData(){
        getUserDetails();
    }

    private void getUserDetails() {
        if(NetworkUtils.isNetworkConnected(this)){
            try {
                AppUtils.showProgress(this,getString(R.string.prog_dialog_title));

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
                                            model.token = String.format(getString(R.string.param_token_value),model.apiKey,model.apiSecret);

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
        }else{
            getTasksList(Application.getUserModel().employeeId);
        }
    }

    private void getTasksList(String fsEmpName) {
        if(NetworkUtils.isNetworkConnected(this)){
            try {
                AppUtils.showProgress(this,getString(R.string.prog_dialog_title));

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
        }else{
            /// set task list from local storage
            final ArrayList<Tasks> list = (ArrayList<Tasks>) DbClient.getInstance().tasksDao().getAll();
            if(list.isEmpty()){
                llEmptyView.setVisibility(View.VISIBLE);
                rvTasks.setVisibility(View.GONE);
            }else{
                setTasksList(list);
            }

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void onAddTap(){
        startActivityForResult(new Intent(this, AddVisitRequestActivity2.class),100);
    }

    private void setTaskListFromLocal(){
        final ArrayList<Tasks> list = (ArrayList<Tasks>) DbClient.getInstance().tasksDao().getAll();
        if(list.isEmpty()){
            llEmptyView.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        }else{
            setTasksList(list);
        }
    }

    private void setTasksList(ArrayList<Tasks> data){
        if(tasksAdapter == null){
            Collections.sort(data, new Comparator<Tasks>() {
                public int compare(Tasks o1, Tasks o2) {
                    return o1.getDateTime().compareTo(o2.getDateTime());
                }
            });
            tasksAdapter = new TasksAdapter(this, data);
            rvTasks.setAdapter(tasksAdapter);
        }else{
            tasksAdapter.updateList(data);
        }
    }

    private void refreshTaskList(){
        getTasksList(Application.getUserModel().employeeId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){
            refreshTaskList();
        }
    }
}