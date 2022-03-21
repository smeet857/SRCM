package com.techinnovators.srcm.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techinnovators.srcm.ActivityDetails;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.VisitRequest;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.utils.SharedPreferencesManager;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.techinnovators.srcm.utils.SharedPreferencesManager.ACTIVITY_DETAILS;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.PREFS_NAME;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.VISIT_REQUESTS;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.VISIT_REQUEST_CHECK_IN;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {
    Context context;
    ArrayList<Tasks> tasks;
    ArrayList<VisitRequest> visitRequestCheckIns;
    ArrayList<Tasks> tasksArrayList;

    public TasksAdapter(Context context, ArrayList<Tasks> tasksArrayList) {
        this.tasks = tasksArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tasks_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (!TextUtils.isEmpty(tasks.get(position).getProject_name())) {
            holder.tvActivityUnderProject.setText(tasks.get(position).getProject_name());
        } else {
            holder.ivActivityIcon.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(tasks.get(position).getProject_type())) {
            holder.tvProjectName.setText(tasks.get(position).getProject_type());
        } else {
            holder.ivProject.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(tasks.get(position).getVisit_place())) {
            holder.tvSchoolName.setText(tasks.get(position).getVisit_place());
        } else {
            holder.ivSchool.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(tasks.get(position).getName())) {
            holder.tvRPName.setText(tasks.get(position).getName());
        } else {
            holder.ivName.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(tasks.get(position).getVisit_location()) &&
                !TextUtils.isEmpty(tasks.get(position).getVisit_taluka()) &&
                !TextUtils.isEmpty(tasks.get(position).getVisit_district()) &&
                !TextUtils.isEmpty(tasks.get(position).getVisit_state())) {
            holder.tvVisitLocation.setText(tasks.get(position).getVisit_location() + "," + " " + tasks.get(position).getVisit_taluka() + "," + " " + tasks.get(position).getVisit_district() + "," + " " + tasks.get(position).getVisit_state());
        } else {
            holder.ivLocation.setVisibility(View.GONE);
            holder.tvVisitLocation.setText("");
        }

        String strVisitDate = "";
        if (!TextUtils.isEmpty(tasks.get(position).getVisit_date())) {
            strVisitDate = tasks.get(position).getVisit_date();
            SimpleDateFormat simpleDateFormat_response = new SimpleDateFormat(context.getString(R.string.dateFormat), Locale.getDefault());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.dateFormat_display), Locale.getDefault());
            try {
                Date visitDay = simpleDateFormat_response.parse(strVisitDate);
                if (visitDay != null) {
                    strVisitDate = simpleDateFormat.format(visitDay);
                    if (!strVisitDate.isEmpty()) {
                        holder.tvVisitDay.setText(strVisitDate);
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        if (!TextUtils.isEmpty(tasks.get(position).getVisit_checkin())) {
            holder.tvCheckIn.setVisibility(View.GONE);
            holder.tvCheckOut.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(tasks.get(position).getContact_person_name())) {
            holder.tvCPerson.setVisibility(View.VISIBLE);
            holder.ivCPerson.setVisibility(View.VISIBLE);
            holder.tvCPerson.setText(tasks.get(position).getContact_person_name());
        } else {
            holder.ivCPerson.setVisibility(View.GONE);
            holder.tvCPerson.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(tasks.get(position).getContact_person_mobile_no())) {
            holder.ivCPersonNo.setVisibility(View.VISIBLE);
            holder.tvCPersonNo.setVisibility(View.VISIBLE);
            holder.tvCPersonNo.setText(tasks.get(position).getContact_person_mobile_no());
        } else {
            holder.ivCPersonNo.setVisibility(View.GONE);
            holder.tvCPersonNo.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvCheckIn, tvActivityUnderProject, tvProjectName, tvRPName, tvVisitLocation, tvSchoolName, tvVisitDay, tvCheckOut, tvCPerson, tvCPersonNo;
        ImageView ivActivityIcon;
        ImageView ivProject;
        ImageView ivName;
        ImageView ivDate;
        ImageView ivSchool;
        ImageView ivLocation;
        ImageView ivCPerson, ivCPersonNo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvCheckIn = itemView.findViewById(R.id.tvCheckIn);
            tvActivityUnderProject = itemView.findViewById(R.id.tvActivityUnderProject);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvRPName = itemView.findViewById(R.id.tvReportPerson);
            tvVisitLocation = itemView.findViewById(R.id.tvSchoolLocation);
            tvSchoolName = itemView.findViewById(R.id.tvSchoolName);
            tvVisitDay = itemView.findViewById(R.id.tvVisitDay);
            ivActivityIcon = itemView.findViewById(R.id.ivActivity);
            ivProject = itemView.findViewById(R.id.ivProject);
            ivName = itemView.findViewById(R.id.ivVisit);
            ivDate = itemView.findViewById(R.id.ivDate);
            ivSchool = itemView.findViewById(R.id.ivSchool);
            ivLocation = itemView.findViewById(R.id.ivLocation);
            ivCPerson = itemView.findViewById(R.id.ivCPerson);
            ivCPersonNo = itemView.findViewById(R.id.ivCPersonNo);
            tvCheckOut = itemView.findViewById(R.id.tvCheckOut);
            tvCPerson = itemView.findViewById(R.id.tvCPerson);
            tvCPersonNo = itemView.findViewById(R.id.tvCPersonNo);


            tvCheckIn.setOnClickListener(view -> {
                if (NetworkUtils.isNetworkConnected(context)) {
                    if (getAdapterPosition() >= 0) {
                        visitRequestCheckIn(tasks.get(getAdapterPosition()).getName(), tvCheckIn, tvCheckOut);
                    }
                } else {
                    SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(context.getString(R.string.timeFormat), Locale.getDefault());
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.dateFormat), Locale.getDefault());
                    String strCurrentDate = simpleDateFormat.format(new Date());
                    String strCurrentTime = simpleTimeFormat.format(new Date());
                    String strVisitCheckIn = strCurrentDate + " " + strCurrentTime;
                    if (getAdapterPosition() >= 0) {
                        storeVisitRequestCheckInData(tasks.get(getAdapterPosition()).getName(), strVisitCheckIn, tvCheckIn, tvCheckOut);
                    }
                    AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", context.getString(R.string.internet_off));
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == itemView.getId()) {
                Intent intent = new Intent(context, ActivityDetails.class);
                intent.putExtra(context.getString(R.string.intent_visit_request_key), tasks.get(getAdapterPosition()).getName());
                context.startActivity(intent);
            }
        }
    }

    private ArrayList<Tasks> getVisitRequests() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_REQUESTS, null);
        Type type = new TypeToken<ArrayList<Tasks>>() {
        }.getType();
        tasksArrayList = gson.fromJson(json, type);
        if (tasksArrayList == null) {
            tasksArrayList = new ArrayList<>();
        }

        return tasksArrayList;
    }

    private ArrayList<VisitRequest> getVisitRequestCheckIns() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_REQUEST_CHECK_IN, null);
        Type type = new TypeToken<ArrayList<VisitRequest>>() {
        }.getType();
        visitRequestCheckIns = gson.fromJson(json, type);
        if (visitRequestCheckIns == null) {
            visitRequestCheckIns = new ArrayList<>();
        }

        return visitRequestCheckIns;
    }

    private void storeVisitRequestCheckInData(String name, String visitCheckIn, TextView tvCheckIn, TextView tvCheckOut) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        ArrayList<VisitRequest> visitRequestCheckIns = new ArrayList<>();
        VisitRequest visitRequest = new VisitRequest();
        visitRequest.setName(name);
        visitRequest.setVisit_checkin(visitCheckIn);
        visitRequestCheckIns.add(visitRequest);
        sharedPreferencesManager.setVisitRequestCheckIn(visitRequestCheckIns);
        visitRequestCheckIns.clear();
        visitRequestCheckIns.addAll(getVisitRequestCheckIns());
        if (!visitRequestCheckIns.isEmpty()) {
            Toast.makeText(context, "Visit Request" + " " + name + "check in time stored in local successfully", Toast.LENGTH_LONG).show();
            tvCheckIn.setVisibility(View.GONE);
            tvCheckOut.setVisibility(View.VISIBLE);
        }
        ArrayList<Tasks> tasks = new ArrayList<>(getVisitRequests());
        if (!tasks.isEmpty()) {
            for (int index = 0; index < tasks.size(); index++) {
                String strName = tasks.get(index).getName();
                if (strName.equals(name)) {
                    tasks.get(index).setVisit_checkin(visitCheckIn);
                    break;
                }
            }
            sharedPreferencesManager.saveVisitRequests(tasks);
        }
    }


    private void visitRequestCheckIn(String fsVisitRequestNo, TextView fTvCheckIn, TextView fTvCheckout) {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(context);
        String apiUrl = context.getString(R.string.api_url);
        String endPoint = context.getString(R.string.api_methodname_visit_request);
        endPoint += "/" + fsVisitRequestNo;
        apiUrl += endPoint;
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat(context.getString(R.string.timeFormat), Locale.getDefault());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(context.getString(R.string.dateFormat), Locale.getDefault());
        String strCurrentDate = simpleDateFormat.format(new Date());
        String strCurrentTime = simpleTimeFormat.format(new Date());
        String strVisitCheckIn = strCurrentDate + " " + strCurrentTime;
        JSONObject jsonRequest = new JSONObject();
        ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMax(100);
        progressDialog.setMessage(context.getString(R.string.prog_dialog_title));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        try {

            RequestQueue queue = Volley.newRequestQueue(context);
            jsonRequest.put(context.getString(R.string.visit_checkin_param_key), strVisitCheckIn);
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, apiUrl, jsonRequest,
                    (JSONObject response) -> {
                        progressDialog.dismiss();
                        try {
                            JSONObject dataResponse = response.getJSONObject(context.getString(R.string.data_param_key));
                            if (!dataResponse.toString().isEmpty()) {
                                String strName = dataResponse.getString(context.getString(R.string.name_param_key));
                                if (!TextUtils.isEmpty(strName)) {
                                    if (strName.equals(fsVisitRequestNo)) {
                                        Toast.makeText(context, "Visit request check in time updated successfully", Toast.LENGTH_LONG).show();
                                    }
                                }
                                fTvCheckIn.setVisibility(View.GONE);
                                fTvCheckout.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            progressDialog.dismiss();
                            e.printStackTrace();
                            AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", e.getMessage());
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        if (error.getMessage() != null) {
                            if (error.getMessage().equals(context.getString(R.string.unknown_host))) {
                                storeVisitRequestCheckInData(fsVisitRequestNo, strVisitCheckIn, fTvCheckIn, fTvCheckout);
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
                                            AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                    AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", context.getString(R.string.error_403));
                                case 404:
                                    AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", context.getString(R.string.error_404));
                                    break;
                                case 417:
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, "utf-8");
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString(context.getString(R.string._server_messages_param_key)).isEmpty()) {
                                            AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", data.getString("_server_messages"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", context.getString(R.string.error_500));
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
            AppUtils.displayAlertMessage(context, "VISIT REQUEST CHECK IN", e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateList(ArrayList<Tasks> data){
        tasks = data;
        this.notifyDataSetChanged();
    }
}
