package com.techinnovators.srcm.adapter;

import android.content.Context;

import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.GpsCallback;
import com.techinnovators.srcm.utils.LocationUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONObject;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {
    Context context;
    ArrayList<Tasks> tasks;

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
        if(!TextUtils.isEmpty(tasks.get(position).visit_checkin) && !TextUtils.isEmpty(tasks.get(position).visit_checkout)){
            holder.tvCheckIn.setVisibility(View.GONE);
            holder.tvCheckOut.setVisibility(View.GONE);
        }else if (TextUtils.isEmpty(tasks.get(position).visit_checkin)) {
            holder.tvCheckIn.setVisibility(View.VISIBLE);
            holder.tvCheckOut.setVisibility(View.GONE);
        }else {
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

            tvCheckIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkIn();
                }
            });

            tvCheckOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkOut();
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == itemView.getId()) {
//                Intent intent = new Intent(context, ActivityDetails.class);
//                intent.putExtra(context.getString(R.string.intent_visit_request_key), tasks.get(getAdapterPosition()).getName());
//                context.startActivity(intent);
            }
        }

        private void checkIn(){
            try{

                Tasks tasksModel = new Tasks();
                tasksModel.visit_checkin = getCheckInCheckOutDate();

                if(NetworkUtils.isNetworkConnected(context)){
                    AppUtils.showProgress(context,context.getString(R.string.prog_dialog_title));

                    String api = context.getString(R.string.api_check_in);
                    api += "/" + tvRPName.getText().toString();

                    final APIVInterface callback = new APIVInterface() {
                        @Override
                        public void notifySuccess(JSONObject response) {
                            AppUtils.dismissProgress();

                            /// set data local
                            final Tasks t = tasks.get(getAdapterPosition());
                            t.visit_checkin = tasksModel.visit_checkin;
                            t.isCheckInSync = true;
                            DbClient.getInstance().tasksDao().update(t);

                            tvCheckIn.setVisibility(View.GONE);
                            tvCheckOut.setVisibility(View.VISIBLE);

                            AppUtils.displayAlertMessage(context,"Check In","Successfully");
                        }

                        @Override
                        public void notifyError(VolleyError error) {
                            AppUtils.dismissProgress();

                            AppUtils.displayAlertMessage(context,"Api Error",error.getMessage());
                        }

                        @Override
                        public void notifyNetworkParseResponse(NetworkResponse response) {
                            AppUtils.dismissProgress();
                        }
                    };

                    final VolleyService volleyService = new VolleyService(callback,context);
                    volleyService.putDataVolley(api,tasksModel.checkInJson());
                }else{
                    /// set data local
                    final Tasks t = tasks.get(getAdapterPosition());
                    t.visit_checkin = tasksModel.visit_checkin;
                    t.isCheckInSync = false;
                    DbClient.getInstance().tasksDao().update(t);

                    tvCheckIn.setVisibility(View.GONE);
                    tvCheckOut.setVisibility(View.VISIBLE);

                    AppUtils.displayAlertMessage(context,"Check In","Successfully");
                }
            }catch (Exception e){
                AppUtils.dismissProgress();
                AppUtils.displayAlertMessage(context,"Error Exception",e.getMessage());
                Log.e("Error on api check in",e.getMessage());
            }
        }

        public void checkOut(){
            try{
                Tasks tasksModel = new Tasks();
                tasksModel.visit_checkout = getCheckInCheckOutDate();
                tasksModel.visitCompleted = 1;
                tasksModel.visitMapLocation = "https://maps.google.com?=3243443,4343243";

                if(NetworkUtils.isNetworkConnected(context)){
                    AppUtils.showProgress(context,context.getString(R.string.prog_dialog_title));

                    String api = context.getString(R.string.api_check_out);
                    api += "/" + tvRPName.getText().toString();

                    final APIVInterface callback = new APIVInterface() {
                        @Override
                        public void notifySuccess(JSONObject response) {
                            AppUtils.dismissProgress();

                            /// set data local
                            final Tasks t = tasks.get(getAdapterPosition());
                            t.visit_checkout = tasksModel.visit_checkout;
                            t.visitCompleted = tasksModel.visitCompleted;
                            t.visitMapLocation = tasksModel.visitMapLocation;
                            t.isCheckOutSync = true;

                            DbClient.getInstance().tasksDao().update(t);

                            tvCheckOut.setVisibility(View.GONE);

                            AppUtils.displayAlertMessage(context,"Check Out","Successfully");

                        }

                        @Override
                        public void notifyError(VolleyError error) {
                            AppUtils.dismissProgress();
                            AppUtils.displayAlertMessage(context,"Api Error",error.getMessage());

                        }

                        @Override
                        public void notifyNetworkParseResponse(NetworkResponse response) {
                            AppUtils.dismissProgress();
                        }
                    };

                    final VolleyService volleyService = new VolleyService(callback,context);
                    volleyService.putDataVolley(api,tasksModel.checkOutJson());
                }else{
                    /// set data local
                    final Tasks t = tasks.get(getAdapterPosition());
                    t.visit_checkout = tasksModel.visit_checkout;
                    t.visitCompleted = tasksModel.visitCompleted;
                    t.visitMapLocation = tasksModel.visitMapLocation;
                    t.isCheckOutSync = false;

                    DbClient.getInstance().tasksDao().update(t);

                    tvCheckOut.setVisibility(View.GONE);

                    AppUtils.displayAlertMessage(context,"Check Out","Successfully");

                }
            }catch (Exception e){
                AppUtils.dismissProgress();
                AppUtils.displayAlertMessage(context,"Error Exception",e.getMessage());
                Log.e("Error on api check out",e.getMessage());
            }
        }

        private String getCheckInCheckOutDate(){
            final Date date = Calendar.getInstance().getTime();
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return dateFormat.format(date);
        }
    }

    public void updateList(ArrayList<Tasks> data){
        tasks = data;
        this.notifyDataSetChanged();
    }
}
