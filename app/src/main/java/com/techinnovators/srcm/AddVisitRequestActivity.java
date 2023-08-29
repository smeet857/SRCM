package com.techinnovators.srcm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techinnovators.srcm.Activity.TaskActivity2;
import com.techinnovators.srcm.models.Taluka;
import com.techinnovators.srcm.models.TalukaResponse;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.VisitDistrict;
import com.techinnovators.srcm.models.VisitDistrictResponse;
import com.techinnovators.srcm.models.VisitLocation;
import com.techinnovators.srcm.models.VisitLocationResponse;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.utils.SharedPreferencesManager;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.techinnovators.srcm.utils.SharedPreferencesManager.ORG_NAME;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.PREFS_NAME;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.PROJECT_NAME;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.PROJECT_TYPE;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.VISIT_DISTRICT;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.VISIT_LOCATION;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.VISIT_REQUESTS;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.VISIT_STATE;
import static com.techinnovators.srcm.utils.SharedPreferencesManager.VISIT_TALUKA;

public class AddVisitRequestActivity extends AppCompatActivity implements View.OnClickListener {
    ConstraintLayout csMain;
    APIVInterface mResultCallback;
    ArrayAdapter<String> arrayAdapterProjectName, arrayAdapterProjectType, arrAdOrgName, arrAdVisitState, arrAdVisitDist, arrAdVisitTaluka, arrAdVisitLocation;
    final String[] strProjectType = {""}, strProjectName = {""}, strOrgName = {""}, strVisitState = {""}, strVisitDistrict = {""}, strVisitTaluka = {""}, strVisitLocation = {""};
    ArrayList<Tasks> visitRequests;
    String strDate = "", strPersonPhNo = "", strPerson = "";
    VolleyService mVolleyService;
    ArrayList<VisitDistrict> visitDistrictResponseList;
    ArrayList<Taluka> visitTaluka;
    ArrayList<VisitLocation> visitLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visit_request);
        initView();
    }

    private void initView() {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);
        AppCompatEditText etVisitDate = findViewById(R.id.etDate);
        AppCompatEditText etVisitAssignedTo = findViewById(R.id.etAssignedTo);
        AutoCompleteTextView acOrganizationName = findViewById(R.id.acOrgName);
        visitLocations = new ArrayList<>();
        AutoCompleteTextView acVisitState = findViewById(R.id.acState);
        AutoCompleteTextView acLocation = findViewById(R.id.acVLocation);
        AutoCompleteTextView acDistrict = findViewById(R.id.acDistrict);
        AutoCompleteTextView acTaluka = findViewById(R.id.acTaluka);
        csMain = findViewById(R.id.csMain);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ImageView ivBack = toolbar.findViewById(R.id.ivBack);
        ivBack.setOnClickListener(this);
        AutoCompleteTextView acProjectName = findViewById(R.id.avEventCategory);
        AutoCompleteTextView acProjectType = findViewById(R.id.acProjectType);

        arrayAdapterProjectName = new ArrayAdapter<>(AddVisitRequestActivity.this, R.layout.autocomplete_text_item);
        arrayAdapterProjectType = new ArrayAdapter<>(AddVisitRequestActivity.this, R.layout.autocomplete_text_item);
        arrAdOrgName = new ArrayAdapter<>(AddVisitRequestActivity.this, R.layout.autocomplete_text_item);
        arrAdVisitState = new ArrayAdapter<>(AddVisitRequestActivity.this, R.layout.autocomplete_text_item);
        arrAdVisitDist = new ArrayAdapter<>(AddVisitRequestActivity.this, R.layout.autocomplete_text_item);
        arrAdVisitTaluka = new ArrayAdapter<>(AddVisitRequestActivity.this, R.layout.autocomplete_text_item);
        arrAdVisitLocation = new ArrayAdapter<>(AddVisitRequestActivity.this, R.layout.autocomplete_text_item);

        visitRequests = new ArrayList<>();
        visitTaluka = new ArrayList<>();

        acProjectName.setAdapter(arrayAdapterProjectName);
        acProjectName.setThreshold(1);

        acProjectType.setAdapter(arrayAdapterProjectType);
        acProjectType.setThreshold(1);

        acOrganizationName.setAdapter(arrAdOrgName);
        acOrganizationName.setThreshold(1);

        acVisitState.setAdapter(arrAdVisitState);
        acVisitState.setThreshold(1);

        acDistrict.setAdapter(arrAdVisitDist);
        acDistrict.setThreshold(1);

        acTaluka.setAdapter(arrAdVisitTaluka);
        acTaluka.setThreshold(1);

        acLocation.setAdapter(arrAdVisitLocation);
        acLocation.setThreshold(1);

        etVisitAssignedTo.setText(sharedPreferencesManager.getEmployee());
        etVisitDate.setText(AppUtils.dispCurrentDateFirst());

        etVisitDate.setOnClickListener(view -> {
            AppUtils.setDate(AddVisitRequestActivity.this, etVisitDate);
            AppUtils.hideKeyboard(this);
        });

        ArrayList<String> projectName;
        ArrayList<String> projectType;
        ArrayList<String> orgName;
        ArrayList<String> visitState;
        ArrayList<VisitDistrict> visitDistrict;
        ArrayList<Taluka> visitTaluka;
        ArrayList<VisitLocation> visitLocation;
        if (NetworkUtils.isNetworkConnected(AddVisitRequestActivity.this)) {
            getProjectName();
            getProjectType();
            //getOrganisationName();
            getVisitState();
            getDistrict();
            getTaluka();
            getLocationOfVisit();
        } else {
            projectName = new ArrayList<>(getProjectNameFromLocal());
            if (!projectName.isEmpty()) {
                arrayAdapterProjectName.addAll(projectName);
                arrayAdapterProjectName.notifyDataSetChanged();
            }
            projectType = new ArrayList<>(getProjectTypeFromLocal());
            if (!projectType.isEmpty()) {
                arrayAdapterProjectType.addAll(projectType);
                arrayAdapterProjectType.notifyDataSetChanged();
            }
            orgName = new ArrayList<>(getOrgNameFromLocal());
            if (!orgName.isEmpty()) {
                arrAdOrgName.addAll(orgName);
                arrAdOrgName.notifyDataSetChanged();
            }
            visitState = new ArrayList<>(getVisitStateFromLocal());
            if (!visitState.isEmpty()) {
                arrAdVisitState.addAll(visitState);
                arrAdVisitState.notifyDataSetChanged();
            }
            visitDistrict = new ArrayList<>(getVisitDistrictFromLocal());
            ArrayList<String> districtNames = new ArrayList<>();
            for (int index = 0; index < visitDistrict.size(); index++) {
                String strName = visitDistrict.get(index).getName();
                districtNames.add(strName);
            }
            if (!districtNames.isEmpty()) {
                arrAdVisitDist.addAll(districtNames);
                arrAdVisitDist.notifyDataSetChanged();
            }
            ArrayList<String> visitTalukaNames = new ArrayList<>();
            visitTaluka = new ArrayList<>(getVisitTalukaFromLocal());
            if (!visitTaluka.isEmpty()) {
                for (int index = 0; index < visitTaluka.size(); index++) {
                    String strName = visitTaluka.get(index).getName();
                    visitTalukaNames.add(strName);
                }
                if (!visitTalukaNames.isEmpty()) {
                    arrAdVisitTaluka.addAll(visitTalukaNames);
                    arrAdVisitTaluka.notifyDataSetChanged();
                }
            }
            visitLocation = new ArrayList<>(getVisitLocationFromLocal());

            ArrayList<String> visitLocationNames = new ArrayList<>();
            if (!visitLocation.isEmpty()) {
                for (int index = 0; index < visitLocation.size(); index++) {
                    String strName = visitLocation.get(index).getName();
                    visitLocationNames.add(strName);
                }
                if (!visitLocationNames.isEmpty()) {
                    arrAdVisitLocation.addAll(visitLocationNames);
                    arrAdVisitLocation.notifyDataSetChanged();
                }
            }
            AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.internet_off));
        }
        acProjectType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                acProjectType.showDropDown();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        acProjectType.setOnItemClickListener((adapterView, view, i, l) -> {
            strProjectType[0] = acProjectType.getText().toString();
            AppUtils.hideKeyboard(this);
        });
        acProjectType.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                acProjectType.showDropDown();
            }
        });
        acProjectName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                acProjectName.showDropDown();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        acProjectName.setOnItemClickListener((adapterView, view, i, l) -> {
            strProjectName[0] = acProjectName.getText().toString();
            AppUtils.hideKeyboard(this);
        });
        acProjectName.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                acProjectName.showDropDown();
            }
        });
        acOrganizationName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                acOrganizationName.showDropDown();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        acVisitState.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                acVisitState.showDropDown();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        acDistrict.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                acDistrict.showDropDown();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        acTaluka.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                acTaluka.showDropDown();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        acLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                acLocation.showDropDown();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        acOrganizationName.setOnItemClickListener((adapterView, view, i, l) -> {
            strOrgName[0] = acOrganizationName.getText().toString();
            AppUtils.hideKeyboard(this);
        });
        acOrganizationName.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                acOrganizationName.showDropDown();
            }
        });
        acVisitState.setOnItemClickListener((adapterView, view, i, l) -> {
            strVisitState[0] = acVisitState.getText().toString();
            AppUtils.hideKeyboard(this);
        });
        acVisitState.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                acVisitState.showDropDown();
            }
        });
        acDistrict.setOnItemClickListener((adapterView, view, i, l) -> {
            strVisitDistrict[0] = acDistrict.getText().toString();
            AppUtils.hideKeyboard(this);
        });
        acDistrict.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                acDistrict.showDropDown();
            }
        });
        acTaluka.setOnItemClickListener((adapterView, view, i, l) -> {
            strVisitTaluka[0] = acTaluka.getText().toString();
            AppUtils.hideKeyboard(this);
        });
        acTaluka.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                acTaluka.showDropDown();
            }
        });
        acLocation.setOnItemClickListener((adapterView, view, i, l) -> {
            strVisitLocation[0] = acLocation.getText().toString();
            AppUtils.hideKeyboard(this);
        });
        acLocation.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                acLocation.showDropDown();
            }
        });
        AppCompatButton btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(this);


    }

    private ArrayList<String> getOrgNameFromLocal() {
        ArrayList<String> orgName = new ArrayList<>();
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(ORG_NAME, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            orgName = gson.fromJson(json, type);
        }
        if (orgName == null) {
            orgName = new ArrayList<>();
        }
        return orgName;
    }

    private ArrayList<String> getVisitStateFromLocal() {
        ArrayList<String> visitState = new ArrayList<>();
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_STATE, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            visitState = gson.fromJson(json, type);
        }
        if (visitState == null) {
            visitState = new ArrayList<>();
        }
        return visitState;
    }

    private ArrayList<VisitDistrict> getVisitDistrictFromLocal() {
        ArrayList<VisitDistrict> visitDistrict = new ArrayList<>();
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_DISTRICT, null);
        Type type = new TypeToken<ArrayList<VisitDistrict>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            visitDistrict = gson.fromJson(json, type);
        }
        if (visitDistrict == null) {
            visitDistrict = new ArrayList<>();
        }
        return visitDistrict;
    }


    private ArrayList<Taluka> getVisitTalukaFromLocal() {
        ArrayList<Taluka> visitTaluka = new ArrayList<>();
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_TALUKA, null);
        Type type = new TypeToken<ArrayList<Taluka>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            visitTaluka = gson.fromJson(json, type);
        }
        if (visitTaluka == null) {
            visitTaluka = new ArrayList<>();
        }
        return visitTaluka;
    }

    private ArrayList<VisitLocation> getVisitLocationFromLocal() {
        ArrayList<VisitLocation> visitLocation = new ArrayList<>();
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_LOCATION, null);
        Type type = new TypeToken<ArrayList<VisitLocation>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            visitLocation = gson.fromJson(json, type);
        }
        if (visitLocation == null) {
            visitLocation = new ArrayList<>();
        }
        return visitLocation;
    }

    private void getLocationOfVisit() {
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_visitlocation);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key + "&";
        String fields = getString(R.string.fields_param_key);
        fields += getString(R.string.fields_value_location);
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<VisitLocationResponse>() {
                        }.getType();
                        VisitLocationResponse tasksResponse = gson.fromJson(response.toString(), listType);
                        visitLocations.addAll(tasksResponse.getData());

                        if (!visitLocations.isEmpty()) {
                            ArrayList<String> locationNames = new ArrayList<>();
                            for (int index = 0; index < visitLocations.size(); index++) {
                                String strName = visitLocations.get(index).getName();
                                locationNames.add(strName);
                            }
                            if (!locationNames.isEmpty()) {
                                arrAdVisitLocation.addAll(locationNames);
                                arrAdVisitLocation.notifyDataSetChanged();
                            }

                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);
                            sharedPreferencesManager.saveVisitLocation(visitLocations);
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
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visitlocation), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visitlocation), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visitlocation), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visitlocation), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        endpoint += fields;
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, AddVisitRequestActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visitlocation), e.getMessage());
        }
    }

    private void getDistrict() {
        ArrayList<String> visitDistrict = new ArrayList<>();
        visitDistrictResponseList = new ArrayList<>();
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_visitdistrict);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key + "&";
        String fields = getString(R.string.fields_param_key);
        fields += getString(R.string.fields_value_district);
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<VisitDistrictResponse>() {
                        }.getType();
                        VisitDistrictResponse tasksResponse = gson.fromJson(response.toString(), listType);
                        visitDistrictResponseList.addAll(tasksResponse.getData());
                        if (!visitDistrictResponseList.isEmpty()) {
                            ArrayList<String> districtNames = new ArrayList<>();
                            for (int index = 0; index < visitDistrictResponseList.size(); index++) {
                                String strName = visitDistrictResponseList.get(index).getName();
                                districtNames.add(strName);
                            }
                            if (!districtNames.isEmpty()) {
                                arrAdVisitDist.addAll(districtNames);
                                arrAdVisitDist.notifyDataSetChanged();
                            }
                            ArrayList<VisitDistrict> visitDistrictResponse;
                            visitDistrictResponse = visitDistrictResponseList;
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);
                            sharedPreferencesManager.saveVisitDistrict(visitDistrictResponse);
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
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        }

        ;
        endpoint += fields;
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, AddVisitRequestActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (
                Exception e) {
            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), e.getMessage());
        }

    }

    private void getTaluka() {
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_visittaluka);

        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key + "&";

        String fields = getString(R.string.fields_param_key);
        fields += getString(R.string.fields_value_taluka);

        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<TalukaResponse>() {
                        }.getType();
                        TalukaResponse tasksResponse = gson.fromJson(response.toString(), listType);
                        visitTaluka.addAll(tasksResponse.getData());
                        if (!visitTaluka.isEmpty()) {
                            ArrayList<String> talukaNames = new ArrayList<>();
                            for (int index = 0; index < visitTaluka.size(); index++) {
                                String strName = visitTaluka.get(index).getName();
                                talukaNames.add(strName);
                            }
                            if (!talukaNames.isEmpty()) {
                                arrAdVisitTaluka.addAll(talukaNames);
                                arrAdVisitTaluka.notifyDataSetChanged();
                            }
                            ArrayList<Taluka> talukaResp;
                            talukaResp = visitTaluka;
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);
                            sharedPreferencesManager.saveVisitTaluka(talukaResp);
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
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        endpoint += fields;
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, AddVisitRequestActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.visit_district), e.getMessage());
        }
    }

    private void getVisitState() {
        ArrayList<String> visitState = new ArrayList<>();
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_visitstate);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key;
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        for (int index = 0; index < data.length(); index++) {
                            JSONObject jsonObject = data.getJSONObject(index);
                            if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                String strName = jsonObject.getString(getString(R.string.name_param_key));
                                if (!strName.isEmpty()) {
                                    visitState.add(strName);
                                }
                            }
                        }
                        if (!visitState.isEmpty()) {
                            arrAdVisitState.addAll(visitState);
                            arrAdVisitState.notifyDataSetChanged();
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);
                            sharedPreferencesManager.saveVisitState(visitState);
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
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.state), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.state), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.state), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.state), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.state), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, AddVisitRequestActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.state), e.getMessage());
        }
    }

    private void getOrganisationName() {
        ArrayList<String> orgName = new ArrayList<>();
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_organizationname);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key;
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        for (int index = 0; index < data.length(); index++) {
                            JSONObject jsonObject = data.getJSONObject(index);
                            if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                String strName = jsonObject.getString(getString(R.string.name_param_key));
                                if (!strName.isEmpty()) {
                                    orgName.add(strName);
                                }
                            }
                        }
                        if (!orgName.isEmpty()) {
                            arrAdOrgName.addAll(orgName);
                            arrAdOrgName.notifyDataSetChanged();
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);
                            sharedPreferencesManager.saveOrgName(orgName);
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
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.orgname), data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.orgname), e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.orgname), getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.orgname), getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.orgname), getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, AddVisitRequestActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.orgname), e.getMessage());
        }
    }

    private void getProjectType() {
        ArrayList<String> projectType = new ArrayList<>();
        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_projecttype);
        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key;
        mResultCallback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                try {
                    JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                    if (data.length() > 0) {
                        for (int index = 0; index < data.length(); index++) {
                            JSONObject jsonObject = data.getJSONObject(index);
                            if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                String strName = jsonObject.getString(getString(R.string.name_param_key));
                                if (!strName.isEmpty()) {
                                    projectType.add(strName);
                                }
                            }
                        }
                        if (!projectType.isEmpty()) {
                            arrayAdapterProjectType.addAll(projectType);
                            arrayAdapterProjectType.notifyDataSetChanged();
                            SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);
                            sharedPreferencesManager.saveProjectType(projectType);
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
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT TYPE", data.getString("message"));
                                }
                            } catch (UnsupportedEncodingException | JSONException e) {
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT TYPE", e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        case 403:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT TYPE", getString(R.string.error_403));
                        case 404:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT TYPE", getString(R.string.error_404));
                            break;
                        case 500:
                            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT TYPE", getString(R.string.error_500));
                            break;
                    }
                }
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {

            }
        };
        apiUrl += endpoint;
        try {
            VolleyService mVolleyService = new VolleyService(mResultCallback, AddVisitRequestActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT TYPE", e.getMessage());
        }
    }

    private void getProjectName() {
        ArrayList<String> projectName = new ArrayList<>();
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);

        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_method_project);

        String limit_page_length_key = "limit_page_length=None";
        endpoint += limit_page_length_key;

        apiUrl += endpoint;
        try {
            mResultCallback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONArray data = response.getJSONArray(getString(R.string.data_param_key));
                        if (data.length() > 0) {
                            for (int index = 0; index < data.length(); index++) {
                                JSONObject jsonObject = data.getJSONObject(index);
                                if (jsonObject != null && !jsonObject.toString().isEmpty()) {
                                    String strName = jsonObject.getString(getString(R.string.name_param_key));
                                    if (!strName.isEmpty()) {
                                        projectName.add(strName);
                                    }
                                }
                            }
                            if (!projectName.isEmpty()) {
                                arrayAdapterProjectName.addAll(projectName);
                                arrayAdapterProjectName.notifyDataSetChanged();
                                sharedPreferencesManager.saveProjectName(projectName);
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
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT NAME", data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT NAME", e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT NAME", getString(R.string.error_403));
                            case 404:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT NAME", getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT NAME", getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            VolleyService mVolleyService = new VolleyService(mResultCallback, AddVisitRequestActivity.this);
            mVolleyService.getDataVolley(apiUrl, null);
        } catch (Exception e) {
            AppUtils.displayAlertMessage(AddVisitRequestActivity.this, "PROJECT NAME", e.getMessage());
        }
    }

    private ArrayList<String> getProjectNameFromLocal() {
        ArrayList<String> projectName = new ArrayList<>();
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(PROJECT_NAME, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            projectName = gson.fromJson(json, type);
        }
        if (projectName == null) {
            projectName = new ArrayList<>();
        }
        return projectName;
    }

    private ArrayList<String> getProjectTypeFromLocal() {
        ArrayList<String> projectType = new ArrayList<>();
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(PROJECT_TYPE, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            projectType = gson.fromJson(json, type);
        }
        if (projectType == null) {
            projectType = new ArrayList<>();
        }
        return projectType;
    }


    @Override
    public void onClick(View view) {
        AppCompatEditText etVisitDate = findViewById(R.id.etDate);
        AppCompatEditText etContPerson = findViewById(R.id.etContPersonName);
        AppCompatEditText etContPersonNo = findViewById(R.id.etContPersonNo);
        strDate = Objects.requireNonNull(etVisitDate.getText()).toString();
        strPerson = Objects.requireNonNull(etContPerson.getText()).toString();
        strPersonPhNo = Objects.requireNonNull(etContPersonNo.getText()).toString();
        if (view.getId() == R.id.btnSubmit) {
            if (TextUtils.isEmpty(strProjectName[0])) {
                AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.proj_name_empty_msg));
                return;
            }
            if (TextUtils.isEmpty(strProjectType[0])) {
                AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.proj_name_empty_msg));
                return;
            }
            if (TextUtils.isEmpty(strDate)) {
                AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.date_empty_msg));
                return;
            }
            if (TextUtils.isEmpty(strOrgName[0]) && !TextUtils.equals(strProjectName[0], getString(R.string.project_type_villageconnect))) {
                AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.orgname_empty_msg));
                return;
            }
            if (TextUtils.isEmpty(strVisitState[0])) {
                AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.visitstate_empty_msg));
                return;
            }
            if (TextUtils.isEmpty(strVisitDistrict[0])) {
                AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.visitdist_empty_msg));
                return;
            }
            if (TextUtils.isEmpty(strVisitTaluka[0])) {
                AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.visittaluka_empty_msg));
                return;
            }
            if (TextUtils.isEmpty(strVisitLocation[0])) {
                AppUtils.showSnackBar(AddVisitRequestActivity.this, csMain, getString(R.string.visitlocation_empty_msg));
                return;
            }
            createVisitRequest();
        }
        if (view.getId() == R.id.ivBack) {
            Intent intent = new Intent(AddVisitRequestActivity.this, TaskActivity2.class);
            startActivity(intent);
        }
    }

    private void createVisitRequest() {
        if (NetworkUtils.isNetworkConnected(AddVisitRequestActivity.this)) {
            ProgressDialog progressDialog;
            progressDialog = new ProgressDialog(AddVisitRequestActivity.this);
            progressDialog.setMax(100);
            progressDialog.setMessage(getString(R.string.prog_dialog_title));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            String apiUrl = getString(R.string.api_url);
            String endpoint = getString(R.string.api_methodname_createVisit);

            mResultCallback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    progressDialog.dismiss();

                }

                @Override
                public void notifyError(VolleyError error) {
                    progressDialog.dismiss();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.create_Visit), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.create_Visit), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 404:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.create_Visit), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.create_Visit), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    progressDialog.dismiss();
                }
            };

            apiUrl += endpoint;
            JSONObject jsonObject = new JSONObject();
            try {
                Tasks visitRequest = new Tasks();
                visitRequest.setVisitRequestAddedLocally(true);
                SimpleDateFormat simpleDateFormat_request = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());
                Date visitDate = simpleDateFormat_request.parse(strDate);
                if (visitDate != null) {
                    visitRequest.setVisit_date(visitDate.toString());
                }
                visitRequest.setVisit_district(strVisitDistrict[0]);
                visitRequest.setVisit_location(strVisitLocation[0]);
                visitRequest.setVisit_taluka(strVisitTaluka[0]);
                visitRequest.setName(AppUtils.dispCurrentDate() + " " + AppUtils.getCurrentTime());
                visitRequest.setVisit_state(strVisitState[0]);
                visitRequest.setProject_type(strProjectType[0]);
                visitRequest.setProject_name(strProjectName[0]);
                visitRequest.setVisit_place(strOrgName[0]);
                if (!TextUtils.isEmpty(strPersonPhNo)) {
                    visitRequest.setContact_person_mobile_no(strPersonPhNo);
                } else {
                    visitRequest.setContact_person_mobile_no("");
                }
                if (!TextUtils.isEmpty(strPerson)) {
                    visitRequest.setContact_person_name(strPerson);
                } else {
                    visitRequest.setContact_person_name("");
                }
                //Pass all the values from Task model above to JSON object to create visit request.
                mVolleyService = new VolleyService(mResultCallback, AddVisitRequestActivity.this);

               String json = new Gson().toJson(visitRequest);

                mVolleyService.postDataVolley(apiUrl, jsonObject);
                progressDialog.show();
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity.this, getString(R.string.create_Visit), e.getMessage());
            }
        } else {
            visitRequests.clear();
            Tasks visitRequest = new Tasks();
            visitRequest.setVisitRequestAddedLocally(false);

            SimpleDateFormat simpleDateFormat_response = new SimpleDateFormat(getString(R.string.dateFormat_display), Locale.getDefault());
            SimpleDateFormat simpleDateFormatRequest = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());

            try {
                Date visitDay = simpleDateFormat_response.parse(strDate);
                if (visitDay != null) {
                    visitRequest.setVisit_date(simpleDateFormatRequest.format(visitDay));

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            visitRequest.setVisit_district(strVisitDistrict[0]);
            visitRequest.setVisit_location(strVisitLocation[0]);
            visitRequest.setVisit_taluka(strVisitTaluka[0]);
            visitRequest.setName(AppUtils.dispCurrentDate() + " " + AppUtils.getCurrentTime());
            visitRequest.setVisit_state(strVisitState[0]);
            visitRequest.setProject_type(strProjectType[0]);
            visitRequest.setProject_name(strProjectName[0]);
            visitRequest.setVisit_place(strOrgName[0]);

            visitRequest.setVisit_checkin("");
            visitRequest.setVisit_checkout("");

            if (!TextUtils.isEmpty(strPersonPhNo)) {
                visitRequest.setContact_person_mobile_no(strPersonPhNo);
            } else {
                visitRequest.setContact_person_mobile_no("");
            }
            if (!TextUtils.isEmpty(strPerson)) {
                visitRequest.setContact_person_name(strPerson);
            } else {
                visitRequest.setContact_person_name("");
            }
            visitRequests.add(visitRequest);
            storeVisitRequestInLocal();

            /// go back

            finish();
        }
    }

    private void storeVisitRequestInLocal() {
        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(AddVisitRequestActivity.this);
        visitRequests.addAll(getVisitRequests());
        if (!visitRequests.isEmpty()) {
            sharedPreferencesManager.saveVisitRequests(visitRequests);
        }
    }

    private ArrayList<Tasks> getVisitRequests() {
        ArrayList<Tasks> tasks = new ArrayList<>();
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        String json = prefs.getString(VISIT_REQUESTS, null);
        Type type = new TypeToken<ArrayList<Tasks>>() {
        }.getType();
        if (gson.fromJson(json, type) != null) {
            tasks = gson.fromJson(json, type);
        }
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks;
    }
}