package com.techinnovators.srcm.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.techinnovators.srcm.R;

@Entity(tableName = "User")
public class UserModel {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @SerializedName("user_name")
    @ColumnInfo(name = "user_name")
    @Expose
    public String userName = "";

    @SerializedName("home_page")
    @ColumnInfo(name = "home_page")
    @Expose
    public String homePage = "";

    @SerializedName("message")
    @ColumnInfo(name = "message")
    @Expose
    public String message = "";

    @SerializedName("full_name")
    @ColumnInfo(name = "full_name")
    @Expose
    public String fullName = "";

    /// Attendance fields

    @SerializedName("api_key")
    @ColumnInfo(name = "api_key")
    @Expose
    public String apiKey = "";

    @SerializedName("api_secret")
    @ColumnInfo(name = "api_secret")
    @Expose
    public String apiSecret = "";

    @SerializedName("employee_id")
    @ColumnInfo(name = "employee_id")
    @Expose
    public String employeeId = "";

    @SerializedName("token")
    @ColumnInfo(name = "token")
    @Expose
    public String token = "";

    @SerializedName("company")
    @ColumnInfo(name = "company")
    @Expose
    public String company = "";

    /// Form details
    @SerializedName("project_name")
    @ColumnInfo(name = "project_name")
    @Expose
    public String projectName = "";

    @SerializedName("project_type")
    @ColumnInfo(name = "project_type")
    @Expose
    public String projectType = "";

    @SerializedName("organization_name")
    @ColumnInfo(name = "organization_name")
    @Expose
    public String organizationName = "";

    @SerializedName("visit_state")
    @ColumnInfo(name = "visit_state")
    @Expose
    public String visitState = "";

    @SerializedName("district")
    @ColumnInfo(name = "district")
    @Expose
    public String district = "";

    @SerializedName("taluka")
    @ColumnInfo(name = "taluka")
    @Expose
    public String taluka = "";

    @SerializedName("location_of_visit")
    @ColumnInfo(name = "location_of_visit")
    @Expose
    public String locationOfVisit = "";

    /// Attendance details

    @SerializedName("first_checkin")
    @ColumnInfo(name = "first_checkin")
    @Expose
    public String firstCheckin = "";

    @SerializedName("last_check_out")
    @ColumnInfo(name = "last_check_out")
    @Expose
    public String lastCheckOut = "";

    @SerializedName("employee")
    @ColumnInfo(name = "employee")
    @Expose
    public String employee = "";

    @SerializedName("attendance_date")
    @ColumnInfo(name = "attendance_date")
    @Expose
    public String attendanceDate = "";

    @SerializedName("name")
    @ColumnInfo(name = "name")
    @Expose
    public String name = "";

    @SerializedName("check_in")
    @ColumnInfo(name = "check_in")
    @Expose
    public int checkIn = 0;

    @SerializedName("check_out")
    @ColumnInfo(name = "check_out")
    @Expose
    public int checkOut = 0;
}
