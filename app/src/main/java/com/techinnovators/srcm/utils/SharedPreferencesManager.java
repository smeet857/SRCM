package com.techinnovators.srcm.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techinnovators.srcm.models.Taluka;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.VisitDistrict;
import com.techinnovators.srcm.models.VisitDistrictResponse;
import com.techinnovators.srcm.models.VisitLocation;
import com.techinnovators.srcm.models.VisitRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesManager {
    Context context;
    private static final String EMPLOYEE = "employee";
    public final static String PREFS_NAME = "srcm";
    public final static String USER_NAME = "usr";
    public final static String TOKEN = "token";
    public final static String API_KEY = "api_key";
    public final static String API_SECRET = "api_secret";
    private static final String FIRST_CHECKIN = "first_checkin";
    private static final String LAST_CHECK_OUT = "last_check_out";
    private static final String ATTENDANCE_DATE = "attendance_date";
    private static final String NAME = "name";
    public final static String ACTIVITY_DETAILS = "activity_details";
    public final static String CHECKED_IN = "checked_in";
    public final static String CHECKED_OUT = "checked_out";
    public final static String VISIT_REQUESTS = "visit_requests";
    public final static String VISIT_REQUEST_CHECK_IN = "visit_request_check_in";
    public final static String VISIT_REQUEST_CHECK_OUT = "visit_request_check_out";
    public final static String PROJECT_NAME = "project_name";
    public final static String PROJECT_TYPE = "project_type";
    public final static String ORG_NAME = "org_name";
    public final static String VISIT_STATE = "visit_state";
    public final static String VISIT_DISTRICT = "visit_district";
    public final static String VISIT_TALUKA = "visit_taluka";
    public final static String VISIT_LOCATION = "visit_location";

    public SharedPreferencesManager(Context context) {
        this.context = context;
    }

    public void setUserName(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USER_NAME, value);
        editor.apply();
    }

    public String getUserName() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(USER_NAME, "");
    }

    public void setEmployee(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(EMPLOYEE, value);
        editor.apply();
    }

    public String getEmployee() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(EMPLOYEE, "");
    }

    public void setFirstCheckIn(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FIRST_CHECKIN, value);
        editor.apply();
    }

    public String getFirstCheckIn() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(FIRST_CHECKIN, "");
    }

    public void setCheckedIn(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CHECKED_IN, value);
        editor.apply();
    }

    public String getCheckedIn() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(CHECKED_IN, "");
    }

    public void clearCheckedIn() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(CHECKED_IN);
        editor.apply();
    }

    public void setCheckedOut(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CHECKED_OUT, value);
        editor.apply();
    }

    public String getCheckedOut() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(CHECKED_OUT, "");
    }

    public void clearCheckedOut() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(CHECKED_OUT);
        editor.apply();
    }

    public void setLastCheckOut(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_CHECK_OUT, value);
        editor.apply();
    }

    public String getLastCheckOut() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(LAST_CHECK_OUT, "");
    }

    public void setAttendanceDate(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ATTENDANCE_DATE, value);
        editor.apply();
    }

    public String getAttendanceDate() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(ATTENDANCE_DATE, "");
    }

    public void setName(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(NAME, value);
        editor.apply();
    }

    public String getName() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(NAME, "");
    }

    public void setToken(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(TOKEN, value);
        editor.apply();
    }

    public String getToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(TOKEN, "");
    }

    public void setAPISecret(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(API_SECRET, value);
        editor.apply();
    }

    public String getAPISecret() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(API_SECRET, "");
    }

    public void saveVisitRequests(List<Tasks> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(VISIT_REQUESTS, json);
        editor.apply();
    }

//    public void getVisitRequests() {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
//        Gson gson = new Gson();
//        String json = prefs.getString(VISIT_REQUESTS, null);
//        Type type = new TypeToken<ArrayList<Tasks>>() {
//        }.getType();
//        tasks = gson.fromJson(json, type);
//        return tasks;
//    }

    public void clearVisitRequests() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(VISIT_REQUESTS);
        editor.apply();
    }

    public void setActivityData(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ACTIVITY_DETAILS, value);
        editor.apply();
    }

    public String getActivityData() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(ACTIVITY_DETAILS, "");
    }

    public void setAPIKey(String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(API_KEY, value);
        editor.apply();
    }

    public String getAPIKey() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(API_KEY, "");
    }

    public void clearAttendanceDate() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(ATTENDANCE_DATE);
        editor.apply();
    }

    public void clearFirstCheckIn() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(FIRST_CHECKIN);
        editor.apply();
    }

    public void clearName() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(NAME);
        editor.apply();
    }

    public void clearLastCheckout() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(LAST_CHECK_OUT);
        editor.apply();
    }

    public void clear() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN);
        editor.remove(API_KEY);
        editor.remove(API_SECRET);
        editor.remove(USER_NAME);
        editor.remove(FIRST_CHECKIN);
        editor.remove(LAST_CHECK_OUT);
        editor.remove(ATTENDANCE_DATE);
        editor.remove(NAME);
        editor.remove(ACTIVITY_DETAILS);
        editor.remove(CHECKED_IN);
        editor.remove(CHECKED_OUT);
        editor.remove(VISIT_REQUESTS);
        editor.remove(VISIT_REQUEST_CHECK_IN);
        editor.clear();
        editor.apply();
    }

    public void clearActivityData() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(ACTIVITY_DETAILS);
        editor.apply();
    }

    public void clearVisitRequestCheckIn() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(VISIT_REQUEST_CHECK_IN);
        editor.apply();
    }

    public void setVisitRequestCheckIn(ArrayList<VisitRequest> visitRequests) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(visitRequests);
        editor.putString(VISIT_REQUEST_CHECK_IN, json);
        editor.apply();
    }

    public void setVisitRequestCheckOut(ArrayList<VisitRequest> visitRequests) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(visitRequests);
        editor.putString(VISIT_REQUEST_CHECK_OUT, json);
        editor.apply();
    }


    public void saveProjectName(ArrayList<String> projectName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(projectName);
        editor.putString(PROJECT_NAME, json);
        editor.apply();
    }

    public void saveProjectType(ArrayList<String> projectType) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(projectType);
        editor.putString(PROJECT_TYPE, json);
        editor.apply();
    }

    public void saveOrgName(ArrayList<String> orgName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(orgName);
        editor.putString(ORG_NAME, json);
        editor.apply();
    }

    public void saveVisitState(ArrayList<String> visitState) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(visitState);
        editor.putString(VISIT_STATE, json);
        editor.apply();
    }

    public void saveVisitDistrict(ArrayList<VisitDistrict> visitDistrict) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(visitDistrict);
        editor.putString(VISIT_DISTRICT, json);
        editor.apply();
    }

    public void saveVisitTaluka(ArrayList<Taluka> visitTaluka) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(visitTaluka);
        editor.putString(VISIT_TALUKA, json);
        editor.apply();
    }

    public void saveVisitLocation(ArrayList<VisitLocation> visitLocation) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(visitLocation);
        editor.putString(VISIT_LOCATION, json);
        editor.apply();
    }
}
