package com.techinnovators.srcm.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.techinnovators.srcm.Application;

import org.json.JSONException;
import org.json.JSONObject;

@Entity(tableName = "Tasks")
public class Tasks {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @SerializedName("project_name")
    @ColumnInfo(name = "project_name")
    @Expose
    public String project_name;
    @SerializedName("name")
    @ColumnInfo(name = "name")
    @Expose
    public String name;
    @SerializedName("visit_place")
    @ColumnInfo(name = "visit_place")
    @Expose
    public String visit_place;
    @SerializedName("visit_location")
    @ColumnInfo(name = "visit_location")
    @Expose
    public String visit_location;
    @SerializedName("visit_date")
    @ColumnInfo(name = "visit_date")
    @Expose
    public String visit_date;
    @SerializedName("project_type")
    @ColumnInfo(name = "project_type")
    @Expose
    public String project_type;
    @SerializedName("visit_checkin")
    @ColumnInfo(name = "visit_checkin")
    @Expose
    public String visit_checkin;
    @SerializedName("visit_state")
    @ColumnInfo(name = "visit_state")
    @Expose
    public String visit_state;
    @SerializedName("contact_person_mobile_no")
    @ColumnInfo(name = "contact_person_mobile_no")
    @Expose
    public String contact_person_mobile_no;
    @SerializedName("visit_district")
    @ColumnInfo(name = "visit_district")
    @Expose
    public String visit_district;
    @SerializedName("visit_taluka")
    @ColumnInfo(name = "visit_taluka")
    @Expose
    public String visit_taluka;
    @SerializedName("contact_person_name")
    @ColumnInfo(name = "contact_person_name")
    @Expose
    public String contact_person_name;

    //    visit_type\",\"visit_mode\",\"visit_expiry_date\",\"visit_checkin\",\"visit_checkout
    @SerializedName("visit_type")
    @ColumnInfo(name = "visit_type")
    @Expose
    public String visit_type;
    @SerializedName("visit_mode")
    @ColumnInfo(name = "visit_mode")
    @Expose
    public String visit_mode;
    @SerializedName("visit_expiry_date")
    @ColumnInfo(name = "visit_expiry_date")
    @Expose
    public String visit_expiry_date;
    @SerializedName("visit_checkout")
    @ColumnInfo(name = "visit_checkout")
    @Expose
    public String visit_checkout;

    @SerializedName("visit_request_added")
    @ColumnInfo(name = "visit_request_added")
    @Expose
    public boolean visitRequestAdded = false;

    @SerializedName("isSync")
    @ColumnInfo(name = "isSync")
    @Expose
    public boolean isSync = false;

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


    public JSONObject createTaskJson(){
        JSONObject jsonObject = new JSONObject();
        try {
        jsonObject.put("visit_location",this.visit_location);
        jsonObject.put("project_type",this.project_type);
        jsonObject.put("visit_place",this.visit_place);
        jsonObject.put("employee", Application.getUserModel().employeeId);
        jsonObject.put("project_name", this.project_name);
        jsonObject.put("visit_date", this.visit_date);
        jsonObject.put("visit_type", this.visit_type);
        jsonObject.put("visit_mode", this.visit_mode);
        jsonObject.put("visit_state", this.visit_state);
        jsonObject.put("visit_district", this.visit_district);
        jsonObject.put("visit_taluka", this.visit_taluka);
        jsonObject.put("contact_person_mobile_no", this.contact_person_mobile_no);
        jsonObject.put("contact_person_name", this.contact_person_name);

        } catch (JSONException e) {
            e.printStackTrace();
        }

//        jsonObject.put("visit_taluka", this.per);
//        jsonObject.put("visit_taluka", this.visit_taluka);

        return jsonObject;
    }
}
