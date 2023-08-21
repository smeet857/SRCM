package com.techinnovators.srcm.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.sangcomz.fishbun.FishBun;
import com.sangcomz.fishbun.adapter.image.impl.GlideAdapter;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ViewHolder> {
    Context context;
    Activity activity;
    ArrayList<Tasks> tasks;
    MutableLiveData<ArrayList<Uri>> imageListener = new MutableLiveData<>();

    public TasksAdapter(Context context, Activity activity, ArrayList<Tasks> tasksArrayList, MutableLiveData<ArrayList<Uri>> imageListener) {
        this.tasks = tasksArrayList;
        this.activity = activity;
        this.context = context;
        this.imageListener = imageListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tasks_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tasks data = tasks.get(position);

        if (data.visit_place.isEmpty()) {
            holder.ivSchool.setVisibility(View.GONE);
            holder.tvSchoolName.setVisibility(View.GONE);
        } else {
            holder.ivSchool.setVisibility(View.VISIBLE);
            holder.tvSchoolName.setVisibility(View.VISIBLE);
        }

        /// name
        if (data.name.isEmpty()) {
            holder.tvRPName.setVisibility(View.GONE);
            holder.ivName.setVisibility(View.GONE);
        } else {
            holder.tvRPName.setVisibility(View.VISIBLE);
            holder.ivName.setVisibility(View.VISIBLE);
        }

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
        if (!TextUtils.isEmpty(data.visit_checkin) && !TextUtils.isEmpty(data.visit_checkout)) {
            holder.tvCheckIn.setVisibility(View.GONE);
            holder.tvCheckOut.setVisibility(View.GONE);
        } else if (TextUtils.isEmpty(tasks.get(position).visit_checkin)) {
            holder.tvCheckIn.setVisibility(View.VISIBLE);
            holder.tvCheckOut.setVisibility(View.GONE);
        } else {
            holder.tvCheckIn.setVisibility(View.GONE);
            holder.tvCheckOut.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(data.contact_person_name)) {
            holder.tvCPerson.setVisibility(View.VISIBLE);
            holder.ivCPerson.setVisibility(View.VISIBLE);
            holder.tvCPerson.setText(tasks.get(position).getContact_person_name());
        } else {
            holder.ivCPerson.setVisibility(View.GONE);
            holder.tvCPerson.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(data.contact_person_mobile_no)) {
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
            tvProjectName = itemView.findViewById(R.id.tvEventCategory);
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

        private void checkIn() {
            try {

                Tasks tasksModel = new Tasks();
                tasksModel.visit_checkin = getCheckInCheckOutDate();

                if (NetworkUtils.isNetworkConnected(context)) {
                    AppUtils.showProgress(context, context.getString(R.string.prog_dialog_title));

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

                            AppUtils.displayAlertMessage(context, "Check In", "Successfully");
                        }

                        @Override
                        public void notifyError(VolleyError error) {
                            AppUtils.dismissProgress();

                            AppUtils.displayAlertMessage(context, "Alert", error.getMessage());
                        }

                        @Override
                        public void notifyNetworkParseResponse(NetworkResponse response) {
                            AppUtils.dismissProgress();
                        }
                    };

                    final VolleyService volleyService = new VolleyService(callback, context);
                    volleyService.putDataVolley(api, tasksModel.checkInJson());
                } else {
                    /// set data local
                    final Tasks t = tasks.get(getAdapterPosition());
                    t.visit_checkin = tasksModel.visit_checkin;
                    t.isCheckInSync = false;
                    DbClient.getInstance().tasksDao().update(t);

                    tvCheckIn.setVisibility(View.GONE);
                    tvCheckOut.setVisibility(View.VISIBLE);

                    AppUtils.displayAlertMessage(context, "Check In", "Successfully");
                }
            } catch (Exception e) {
                AppUtils.dismissProgress();
                AppUtils.displayAlertMessage(context, "Error Exception", e.getMessage());
                Log.e("Error on api check in", e.getMessage());
            }
        }

        public void checkOut() {
            try {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_checkout);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setCancelable(false);

                AppCompatEditText etTotalParticipant, etTrainerName, etRemarks;
                CheckBox check;
                AppCompatButton btnPick, btnCancel, btnSubmit;
                RecyclerView recycler;

                etTotalParticipant = dialog.findViewById(R.id.etTotalParticipant);
                etTrainerName = dialog.findViewById(R.id.etTrainerName);
                check = dialog.findViewById(R.id.check);
                btnPick = dialog.findViewById(R.id.btnPick);
                btnCancel = dialog.findViewById(R.id.btnCancel);
                btnSubmit = dialog.findViewById(R.id.btnSubmit);
                etRemarks = dialog.findViewById(R.id.etRemarks);
                recycler = dialog.findViewById(R.id.recycler);

                imageListener.observe((LifecycleOwner) context, new Observer<ArrayList<Uri>>() {
                    @Override
                    public void onChanged(ArrayList<Uri> uris) {
                        recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                        ImageAdapter adapter = new ImageAdapter(context, uris);
                        recycler.setAdapter(adapter);
                    }
                });

                btnPick.setOnClickListener(v -> {
                    FishBun.with(activity)
                            .setImageAdapter(new GlideAdapter())
                            .setMaxCount(30)
                            .setMinCount(1)
                            .setPickerSpanCount(5)
                            .setAlbumSpanCount(3, 3)
                            .setButtonInAlbumActivity(false)
                            .setCamera(true)
                            .exceptGif(true)
                            .setReachLimitAutomaticClose(true)
                            .setAllViewTitle("All")
                            .setMenuAllDoneText("All Done")
                            .setActionBarTitle("Images")
                            .startAlbumWithOnActivityResult(1010);
                });

                btnCancel.setOnClickListener(v -> {
                    imageListener.setValue(new ArrayList<>());
                    dialog.dismiss();
                });

                btnSubmit.setOnClickListener(v -> {
                    boolean isValid = true;
                    if (etTotalParticipant.getText().toString().equals("")) {
                        etTotalParticipant.setError("can not be blank");
                        isValid = false;
                    }
                    if (etTrainerName.getText().toString().equals("")) {
                        etTrainerName.setError("can not be blank");
                        isValid = false;
                    }

                    if (etRemarks.getText().toString().equals("")) {
                        etRemarks.setError("can not be blank");
                        isValid = false;
                    }

                    if (!isValid) {
                        return;
                    }

                    dialog.dismiss();

                    Tasks tasksModel = new Tasks();
                    tasksModel.visit_checkout = getCheckInCheckOutDate();
                    tasksModel.visitCompleted = 1;
                    tasksModel.visitMapLocation = "https://maps.google.com?=3243443,4343243";
                    tasksModel.totalParticipants = etTotalParticipant.getText().toString();
                    tasksModel.trainerName = etTrainerName.getText().toString();
                    tasksModel.dataTaken = check.isChecked();
                    tasksModel.remarks = etRemarks.getText().toString();
                    if(imageListener!=null && imageListener.getValue()!=null) {
                        for (int i = 0; i < imageListener.getValue().size(); i++) {
                            String encodedImage = encodeImage(imageListener.getValue().get(i));

                            if (i == 0) {
                                tasksModel.images = encodedImage;
                            } else {
                                tasksModel.images = tasksModel.images + "," + encodedImage;
                            }

                        }
                    }
                    _checkout(tasksModel, 0);

                });

                dialog.show();
            } catch (Exception e) {
                AppUtils.dismissProgress();
                AppUtils.displayAlertMessage(context, "Error Exception", e.getMessage());
                Log.e("Error on api check out", e.getMessage());
            }
        }

        private void _checkout(Tasks tasksModel, int imgPos) {
            if (NetworkUtils.isNetworkConnected(context)) {
                if(imgPos == 0) {
                    AppUtils.showProgress(context, context.getString(R.string.prog_dialog_title));
                }

                String[] separated = tasksModel.images.split(",");
                if (!tasksModel.images.equals("") && separated.length > 0 && imgPos <= separated.length - 1) {
                    uploadImage(tasksModel, separated[imgPos], imgPos);
                } else {
                    imageListener.setValue(new ArrayList<>());
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
                            t.totalParticipants = tasksModel.totalParticipants;
                            t.trainerName = tasksModel.trainerName;
                            t.dataTaken = tasksModel.dataTaken;
                            t.remarks = tasksModel.remarks;
                            t.images = tasksModel.images;
                            t.isCheckOutSync = true;

                            DbClient.getInstance().tasksDao().update(t);

                            tvCheckOut.setVisibility(View.GONE);

                            AppUtils.displayAlertMessage(context, "Check Out", "Successfully");

                        }

                        @Override
                        public void notifyError(VolleyError error) {
                            AppUtils.dismissProgress();
                            AppUtils.displayAlertMessage(context, "Alert", error.getMessage());

                        }

                        @Override
                        public void notifyNetworkParseResponse(NetworkResponse response) {
                            AppUtils.dismissProgress();
                        }
                    };

                    final VolleyService volleyService = new VolleyService(callback, context);
                    volleyService.putDataVolley(api, tasksModel.checkOutJson());
                }
            } else {
                /// set data local
                final Tasks t = tasks.get(getAdapterPosition());
                t.visit_checkout = tasksModel.visit_checkout;
                t.visitCompleted = tasksModel.visitCompleted;
                t.visitMapLocation = tasksModel.visitMapLocation;
                t.totalParticipants = tasksModel.totalParticipants;
                t.trainerName = tasksModel.trainerName;
                t.dataTaken = tasksModel.dataTaken;
                t.remarks = tasksModel.remarks;
                t.images = tasksModel.images;
                t.isCheckOutSync = false;

                DbClient.getInstance().tasksDao().update(t);

                tvCheckOut.setVisibility(View.GONE);

                AppUtils.displayAlertMessage(context, "Check Out", "Successfully");
            }
        }

        private String getCheckInCheckOutDate() {
            final Date date = Calendar.getInstance().getTime();
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return dateFormat.format(date);
        }


        private void uploadImage(Tasks tasksModel, String img, int pos) {
            if (NetworkUtils.isNetworkConnected(context)) {
                String api = context.getString(R.string.api_upload);
                //api += "/" + tvRPName.getText().toString();
                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        _checkout(tasksModel, pos + 1);
                    }

                    @Override
                    public void notifyError(VolleyError error) {
                        imageListener.setValue(new ArrayList<>());
                        AppUtils.dismissProgress();
                        AppUtils.displayAlertMessage(context, "Alert", error.getMessage());
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {
                        imageListener.setValue(new ArrayList<>());
                        AppUtils.dismissProgress();
                    }
                };

                final VolleyService volleyService = new VolleyService(callback, context);

                final HashMap<String, Object> map = new HashMap<>();
                map.put("filename", System.currentTimeMillis() / 1000 + ".jpg");
                map.put("filedata", img);
                map.put("from_form", 1);
                map.put("doctype", "Visit Request");
                map.put("docname", tvRPName.getText().toString());
                map.put("app_auth_key", "536aa3df73a76dcdf6c64a3919f4a0d3");
                map.put("pass_token", Application.getUserModel().token);
                map.put("usr", Application.getUserModel().userName);

                volleyService.putDataVolley(api, new JSONObject(map));
            } else {
                AppUtils.displayAlertMessage(context, "Check Out", "Successfully");
            }
        }
    }


    public void updateList(ArrayList<Tasks> data) {
        tasks = data;
        this.notifyDataSetChanged();
    }

    private String encodeImage(Uri uri) {
        FileInputStream fis = null;
        File file = new File(getRealPathFromURI(uri));
        try {
            fis = new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        //Base64.de
        return encImage;

    }

    public String getRealPathFromURI(Uri contentUri) {
        String path = null;
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

}
