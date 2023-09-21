package com.techinnovators.srcm.models;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity(tableName = "Tasks")
public class Tasks {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @SerializedName("event_sector")
    @ColumnInfo(name = "event_sector")
    @Expose
    public String event_sector = "";
    @SerializedName("project_name")
    @ColumnInfo(name = "project_name")
    @Expose
    public String project_name = "";
    @SerializedName("name")
    @ColumnInfo(name = "name")
    @Expose
    public String name = "";
    @SerializedName("visit_place")
    @ColumnInfo(name = "visit_place")
    @Expose
    public String visit_place = "";
    @SerializedName("visit_location")
    @ColumnInfo(name = "visit_location")
    @Expose
    public String visit_location = "";
    @SerializedName("visit_location_to_be")
    @ColumnInfo(name = "visit_location_to_be")
    @Expose
    public String visit_location_to_be = "";
    @SerializedName("visit_date")
    @ColumnInfo(name = "visit_date")
    @Expose
    public String visit_date = "";
    @SerializedName("project_type")
    @ColumnInfo(name = "project_type")
    @Expose
    public String project_type = "";
    @SerializedName("visit_checkin")
    @ColumnInfo(name = "visit_checkin")
    @Expose
    public String visit_checkin = "";
    @SerializedName("visit_state")
    @ColumnInfo(name = "visit_state")
    @Expose
    public String visit_state = "";
    @SerializedName("contact_person_mobile_no")
    @ColumnInfo(name = "contact_person_mobile_no")
    @Expose
    public String contact_person_mobile_no = "";
    @SerializedName("visit_district")
    @ColumnInfo(name = "visit_district")
    @Expose
    public String visit_district = "";
    @SerializedName("visit_taluka")
    @ColumnInfo(name = "visit_taluka")
    @Expose
    public String visit_taluka = "";
    @SerializedName("contact_person_name")
    @ColumnInfo(name = "contact_person_name")
    @Expose
    public String contact_person_name = "";

    //    visit_type\",\"visit_mode\",\"visit_expiry_date\",\"visit_checkin\",\"visit_checkout
    @SerializedName("visit_type")
    @ColumnInfo(name = "visit_type")
    @Expose
    public String visit_type = "";
    @SerializedName("visit_mode")
    @ColumnInfo(name = "visit_mode")
    @Expose
    public String visit_mode = "";
    @SerializedName("visit_expiry_date")
    @ColumnInfo(name = "visit_expiry_date")
    @Expose
    public String visit_expiry_date = "";
    @SerializedName("visit_checkout")
    @ColumnInfo(name = "visit_checkout")
    @Expose
    public String visit_checkout = "";

    @SerializedName("visit_request_added")
    @ColumnInfo(name = "visit_request_added")
    @Expose
    public boolean visitRequestAdded = false;

    @SerializedName("visit_completed")
    @ColumnInfo(name = "visit_completed")
    @Expose
    public int visitCompleted = 0;

    @SerializedName("visit_map_location")
    @ColumnInfo(name = "visit_map_location")
    @Expose
    public String visitMapLocation = "";

    @SerializedName("isCheckInSync")
    @ColumnInfo(name = "isCheckInSync")
    @Expose
    public boolean isCheckInSync = true;

    @SerializedName("isSync")
    @ColumnInfo(name = "isSync")
    @Expose
    public boolean isSync = true;

    @SerializedName("isCheckOutSync")
    @ColumnInfo(name = "isCheckOutSync")
    @Expose
    public boolean isCheckOutSync = true;

    @SerializedName("total_number_of_participants")
    @ColumnInfo(name = "total_number_of_participants")
    @Expose
    public String totalParticipants = "";

    @SerializedName("prefect_trainer_name")
    @ColumnInfo(name = "prefect_trainer_name")
    @Expose
    public String trainerName = "";

    @SerializedName("data_of_participants_taken")
    @ColumnInfo(name = "data_of_participants_taken")
    @Expose
    public boolean dataTaken = false;

    @SerializedName("remarks")
    @ColumnInfo(name = "remarks")
    @Expose
    public String remarks = "";

    @SerializedName("images")
    @ColumnInfo(name = "images")
    @Expose
    public String images = "";

    public int getId(){
        return this.id;
    }
    public String getEvent_sector() {
        return event_sector;
    }

    public void setEventSector(String event_sector) {
        this.event_sector = event_sector;
    }

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVisit_place() {
        return visit_place;
    }

    public void setVisit_place(String visit_place) {
        this.visit_place = visit_place;
    }

    public String getVisit_location() {
        return visit_location;
    }

    public void setVisit_location(String visit_location) {
        this.visit_location = visit_location;
    }

    public void setVisit_location_to_be(String visit_location_to_be) {
        this.visit_location_to_be = visit_location_to_be;
    }

    public String getVisit_date() {
        return visit_date;
    }

    public void setVisit_date(String visit_date) {
        this.visit_date = visit_date;
    }

    public String getProject_type() {
        return project_type;
    }

    public void setProject_type(String project_type) {
        this.project_type = project_type;
    }
    public String getVisit_checkin() {
        return visit_checkin;
    }

    public void setVisit_checkin(String visit_checkin) {
        this.visit_checkin = visit_checkin;
    }

    public String getVisit_state() {
        return visit_state;
    }

    public void setVisit_state(String visit_state) {
        this.visit_state = visit_state;
    }

    public String getContact_person_mobile_no() {
        return contact_person_mobile_no;
    }

    public void setContact_person_mobile_no(String contact_person_mobile_no) {
        this.contact_person_mobile_no = contact_person_mobile_no;
    }

    public String getVisit_district() {
        return visit_district;
    }

    public void setVisit_district(String visit_district) {
        this.visit_district = visit_district;
    }

    public String getVisit_taluka() {
        return visit_taluka;
    }

    public void setVisit_taluka(String visit_taluka) {
        this.visit_taluka = visit_taluka;
    }

    public String getContact_person_name() {
        return contact_person_name;
    }

    public void setContact_person_name(String contact_person_name) {
        this.contact_person_name = contact_person_name;
    }

    public boolean isVisitRequestAdded() {
        return visitRequestAdded;
    }

    public void setVisitRequestAddedLocally(boolean visitRequestAdded) {
        this.visitRequestAdded = visitRequestAdded;
    }

    public String getVisit_type() {
        return visit_type;
    }

    public void setVisit_type(String visit_type) {
        this.visit_type = visit_type;
    }

    public String getVisit_mode() {
        return visit_mode;
    }

    public void setVisit_mode(String visit_mode) {
        this.visit_mode = visit_mode;
    }

    public String getVisit_expiry_date() {
        return visit_expiry_date;
    }

    public void setVisit_expiry_date(String visit_expiry_date) {
        this.visit_expiry_date = visit_expiry_date;
    }

    public String getVisit_checkout() {
        return visit_checkout;
    }

    public void setVisit_checkout(String visit_checkout) {
        this.visit_checkout = visit_checkout;
    }

    public Date getDate (){
        @SuppressLint("SimpleDateFormat")
        DateFormat f = new SimpleDateFormat(Application.context.getString(R.string.dateFormat));
        try {
            return f.parse(visit_date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject createTaskJson(){
        JSONObject jsonObject = new JSONObject();
        try {
        jsonObject.put("visit_location",this.visit_location.trim());
        jsonObject.put("event_sector",this.event_sector.trim());
        jsonObject.put("project_type",this.project_type.trim());
        jsonObject.put("visit_place",this.visit_place.trim());
        jsonObject.put("employee", Application.getUserModel().employeeId.trim());
        jsonObject.put("project_name", this.project_name.trim());
        jsonObject.put("visit_date", this.visit_date.trim());
        jsonObject.put("visit_type", this.visit_type.trim());
        jsonObject.put("visit_mode", this.visit_mode.trim());
        jsonObject.put("visit_state", this.visit_state.trim());
        jsonObject.put("visit_district", this.visit_district.trim());
        jsonObject.put("visit_taluka", this.visit_taluka.trim());
        jsonObject.put("contact_person_mobile_no", this.contact_person_mobile_no.trim());
        jsonObject.put("contact_person_name", this.contact_person_name.trim());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public JSONObject checkInJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("visit_checkin",this.visit_checkin);
        } catch (JSONException e) {
            Log.e("Error on json put",e.getMessage());
            e.printStackTrace();
        }

        return jsonObject;
    }

    public JSONObject checkOutJson(){
        final HashMap<String,Object> map = new HashMap<>();

        map.put("visit_checkout",this.visit_checkout);
        map.put("visit_completed",this.visitCompleted);
        map.put("visit_map_location",this.visit_location);
        map.put("total_number_of_participants",this.totalParticipants);
        map.put("prefect_trainer_name",this.trainerName);
        map.put("data_of_participants_taken",this.dataTaken);
        map.put("remarks",this.remarks);

        return new JSONObject(map);
    }
}
