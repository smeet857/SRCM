package com.techinnovators.srcm.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.models.Taluka;
import com.techinnovators.srcm.models.TalukaResponse;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.models.VisitDistrict;
import com.techinnovators.srcm.models.VisitDistrictResponse;
import com.techinnovators.srcm.models.VisitLocation;
import com.techinnovators.srcm.models.VisitLocationResponse;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddVisitRequestActivity2 extends AppCompatActivity implements View.OnClickListener {

    private ArrayAdapter<String> arrayAdapterProjectName,
            arrayAdapterProjectType,
            arrayAdapterOrganizationName,
            arrayAdapterVisitState,
            arrayAdapterVisitDist,
            arrayAdapterVisitTaluka,
            arrayAdapterVisitLocation;

    private AutoCompleteTextView acProjectName,
            acProjectType,
            acOrganizationName,
            acVisitState,
            acDistrict,
            acTaluka,
            acLocation;

    private ConstraintLayout csMain;

    private AppCompatEditText /*etVisitDate,*/etVisitAssignedTo, etContPersonName, etContPersonNo;

    private Spinner spinnerVisitType, spinnerVisitMode;

    private ImageView ivBack;

    private AppCompatButton btnSubmit;

    private DbClient db;

    private ArrayList<VisitDistrict> visitDistrictList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visit_request);
        init();
        initListener();
        initData();
    }

    private void init() {
        ivBack = findViewById(R.id.ivBack);
        btnSubmit = findViewById(R.id.btnSubmit);

        spinnerVisitMode = findViewById(R.id.spin_visitmode);
        spinnerVisitType = findViewById(R.id.spin_activity);

        csMain = findViewById(R.id.csMain);

        acProjectName = findViewById(R.id.acProjectName);
        acProjectType = findViewById(R.id.acProjectType);
        acOrganizationName = findViewById(R.id.acOrgName);
        acVisitState = findViewById(R.id.acState);
        acDistrict = findViewById(R.id.acDistrict);
        acTaluka = findViewById(R.id.acTaluka);
        acLocation = findViewById(R.id.acVLocation);

//        etVisitDate = findViewById(R.id.etDate);
        etVisitAssignedTo = findViewById(R.id.etAssignedTo);
        etContPersonName = findViewById(R.id.etContPersonName);
        etContPersonNo = findViewById(R.id.etContPersonNo);

        arrayAdapterProjectName = new ArrayAdapter<>(this, R.layout.autocomplete_text_item);
        arrayAdapterProjectType = new ArrayAdapter<>(this, R.layout.autocomplete_text_item);
        arrayAdapterOrganizationName = new ArrayAdapter<>(this, R.layout.autocomplete_text_item);
        arrayAdapterVisitState = new ArrayAdapter<>(this, R.layout.autocomplete_text_item);
        arrayAdapterVisitDist = new ArrayAdapter<>(this, R.layout.autocomplete_text_item);
        arrayAdapterVisitTaluka = new ArrayAdapter<>(this, R.layout.autocomplete_text_item);
        arrayAdapterVisitLocation = new ArrayAdapter<>(this, R.layout.autocomplete_text_item);

        acProjectName.setAdapter(arrayAdapterProjectName);
        acProjectName.setThreshold(1);

        acProjectType.setAdapter(arrayAdapterProjectType);
        acProjectType.setThreshold(1);

        acOrganizationName.setAdapter(arrayAdapterOrganizationName);
        acOrganizationName.setThreshold(1);

        acVisitState.setAdapter(arrayAdapterVisitState);
        acVisitState.setThreshold(1);

        acDistrict.setAdapter(arrayAdapterVisitDist);
        acDistrict.setThreshold(1);

        acTaluka.setAdapter(arrayAdapterVisitTaluka);
        acTaluka.setThreshold(1);

        acLocation.setAdapter(arrayAdapterVisitLocation);
        acLocation.setThreshold(1);

        etVisitAssignedTo.setText(Application.getUserModel().employeeId);
//        etVisitDate.setText(AppUtils.dispCurrentDateFirst());

        db = DbClient.getInstance();
    }

    private void initListener() {
        ivBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
//        etVisitDate.setOnClickListener(this);

        acProjectName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acProjectName.showDropDown();
            }
        });

        acProjectType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acProjectType.showDropDown();
            }
        });

        acOrganizationName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acOrganizationName.showDropDown();
            }
        });

        acVisitState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acVisitState.showDropDown();
            }
        });

        acVisitState.setOnItemClickListener((adapterView, view, i, l) -> {
            acDistrict.getText().clear();
            setDistrictArrayAdapter(visitDistrictList);
        });

        acDistrict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acDistrict.showDropDown();
            }
        });

        acTaluka.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acTaluka.showDropDown();
            }
        });

        acLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acLocation.showDropDown();
            }
        });
    }

    private void initData() {
        getProjectName();
        getProjectType();
        getOrganisationName();
        getVisitState();
        getDistrict();
        getTaluka();
        getLocationOfVisit();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivBack) {
            finish();
        }/*else if (view.getId() == R.id.etDate){
            onDateTap();
        }*/ else if (view.getId() == R.id.btnSubmit) {
            onSubmitTap();
        }
    }

//    private void onDateTap(){
//        AppUtils.setDate(this, etVisitDate);
//        AppUtils.hideKeyboard(this);
//    }

    private void onSubmitTap() {
        if (TextUtils.isEmpty(acProjectName.getText().toString())) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.proj_name_empty_msg));
            return;
        }
        if (TextUtils.isEmpty(acProjectType.getText().toString())) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.proj_type_empty_msg));
            return;
        }
        if (TextUtils.isEmpty(acOrganizationName.getText().toString()) && !TextUtils.equals(acProjectType.getText().toString(), getString(R.string.project_type_villageconnect))) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.orgname_empty_msg));
            return;
        }
        if (TextUtils.isEmpty(acVisitState.getText().toString())) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.visitstate_empty_msg));
            return;
        }
        if (TextUtils.isEmpty(acDistrict.getText().toString())) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.visitdist_empty_msg));
            return;
        }
        if (TextUtils.isEmpty(acTaluka.getText().toString())) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.visittaluka_empty_msg));
            return;
        }
        if (TextUtils.isEmpty(acLocation.getText().toString())) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.visitlocation_empty_msg));
            return;
        }

        apiCreateTask();
    }

    private void apiCreateTask() {
        Tasks visitRequest = new Tasks();

        visitRequest.setVisit_district(acDistrict.getText().toString());
        visitRequest.setVisit_location(acLocation.getText().toString());
        visitRequest.setVisit_taluka(acTaluka.getText().toString());
        visitRequest.setVisit_state(acVisitState.getText().toString());
        visitRequest.setProject_type(acProjectType.getText().toString());
        visitRequest.setProject_name(acProjectName.getText().toString());
        visitRequest.setVisit_place(acOrganizationName.getText().toString());

        visitRequest.setVisit_type(spinnerVisitType.getSelectedItem().toString());
        visitRequest.setVisit_mode(spinnerVisitMode.getSelectedItem().toString());

        visitRequest.setContact_person_mobile_no(etContPersonNo.getText().toString());
        visitRequest.setContact_person_name(etContPersonName.getText().toString());

//        SimpleDateFormat displayDateFormat = new SimpleDateFormat(getString(R.string.dateFormat_display),Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault());

        final Date visitDate = new Date();
        final String strDate = dateFormat.format(visitDate);

        visitRequest.setVisit_date(strDate);


        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                AppUtils.showProgress(this, getString(R.string.prog_dialog_title));

                String apiUrl = getString(R.string.api_create_task);

                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        AppUtils.dismissProgress();

                        visitRequest.isSync = true;
                        db.tasksDao().insert(visitRequest);
                        setResult(RESULT_OK);
                        finish();
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
                                            AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.create_Visit), data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.create_Visit), e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 404:
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.create_Visit), getString(R.string.error_404));
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.create_Visit), getString(R.string.error_500));
                                    break;
                            }
                        }
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {
                        AppUtils.dismissProgress();
                    }
                };
                final VolleyService mVolleyService = new VolleyService(callback, this);
                mVolleyService.postDataVolley(apiUrl, visitRequest.createTaskJson());

            } catch (Exception e) {
                AppUtils.dismissProgress();
                AppUtils.displayAlertMessage(this, getString(R.string.create_Visit), e.getMessage());
            }
        } else {
            visitRequest.isSync = false;
            db.tasksDao().insert(visitRequest);
            setResult(RESULT_OK);
            finish();
        }
    }

    private void getProjectName() {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                String apiUrl = getString(R.string.api_project_name);

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
                                setProjectNameArrayAdapter(arrayList);

                                /// set in local storage
                                Application.getUserModel().projectName = arrayList.toString();
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
                                            AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", data.getString("message"));
                                        }
                                    } catch (UnsupportedEncodingException | JSONException e) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403:
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", getString(R.string.error_403));
                                case 404:
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", getString(R.string.error_404));
                                    break;
                                case 500:
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", getString(R.string.error_500));
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
        } else {
            /// set from local storage
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().projectName);
            setProjectNameArrayAdapter(data);
        }
    }

    private void getProjectType() {
        if (NetworkUtils.isNetworkConnected(this)) {
            String apiUrl = getString(R.string.api_project_type);

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

                            setProjectTypeArrayAdapter(arrayList);

                            Application.getUserModel().projectType = arrayList.toString();
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
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", getString(R.string.error_403));
                            case 404:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", e.getMessage());
            }
        } else {
            /// set from local storage
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().projectType);
            setProjectTypeArrayAdapter(data);
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

                            setOrganizationNameArrayAdapter(arrayList);

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
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), getString(R.string.error_403));
                            case 404:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), e.getMessage());
            }
        } else {
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().organizationName);
            setOrganizationNameArrayAdapter(data);
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

                            setVisitStateArrayAdapter(arrayList);

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
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), getString(R.string.error_403));
                            case 404:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), e.getMessage());
            }
        } else {
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().visitState);
            setVisitStateArrayAdapter(data);
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
                            visitDistrictList = new ArrayList<>(tasksResponse.getData());

                            if (!visitDistrictList.isEmpty()) {
                                ArrayList<String> arrayJson = new ArrayList<>();

                                for (int index = 0; index < visitDistrictList.size(); index++) {
                                    final VisitDistrict vd = visitDistrictList.get(index);
                                    arrayJson.add(gson.toJson(vd));
                                }

                                setDistrictArrayAdapter(visitDistrictList);

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
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), getString(R.string.error_403));
                            case 404:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), e.getMessage());
            }
        } else {
            final ArrayList<String> data = AppUtils.jsonArrayStringToStringArray(Application.getUserModel().district);
            Gson gson = new Gson();

            for (int i = 0; i < data.size(); i++) {
                final String str = data.get(i);
                visitDistrictList.add(gson.fromJson(str, VisitDistrict.class));
            }

            setDistrictArrayAdapter(visitDistrictList);
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
                            ArrayList<Taluka> talukas = new ArrayList<>();

                            Gson gson = new Gson();
                            Type listType = new TypeToken<TalukaResponse>() {
                            }.getType();

                            TalukaResponse tasksResponse = gson.fromJson(response.toString(), listType);
                            talukas.addAll(tasksResponse.getData());

                            if (!talukas.isEmpty()) {
                                ArrayList<String> talukaNames = new ArrayList<>();
                                for (int index = 0; index < talukas.size(); index++) {
                                    String strName = talukas.get(index).getName();
                                    talukaNames.add(strName);
                                }
                                if (!talukaNames.isEmpty()) {
                                    setTalukaArrayAdapter(talukaNames);

                                    Application.getUserModel().taluka = talukaNames.toString();
                                    db.userDao().update(Application.getUserModel());
                                }
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
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), getString(R.string.error_403));
                            case 404:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), e.getMessage());
            }
        } else {
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().taluka);
            setTalukaArrayAdapter(data);
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
                            final ArrayList<VisitLocation> visitLocations = new ArrayList<>();

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
                                    setVisitLocationArrayAdapter(locationNames);

                                    Application.getUserModel().locationOfVisit = locationNames.toString();
                                    db.userDao().update(Application.getUserModel());
                                }
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
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visitlocation), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visitlocation), getString(R.string.error_403));
                            case 404:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visitlocation), getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visitlocation), getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {

                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visitlocation), e.getMessage());
            }
        } else {
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().locationOfVisit);
            setVisitLocationArrayAdapter(data);
        }
    }

    private void setProjectNameArrayAdapter(ArrayList<String> data) {
        arrayAdapterProjectName = new ArrayAdapter<>(this, R.layout.autocomplete_text_item, data);
        acProjectName.setAdapter(arrayAdapterProjectName);
    }

    private void setProjectTypeArrayAdapter(ArrayList<String> data) {
        arrayAdapterProjectType = new ArrayAdapter<>(this, R.layout.autocomplete_text_item, data);
        acProjectType.setAdapter(arrayAdapterProjectType);
    }

    private void setOrganizationNameArrayAdapter(ArrayList<String> data) {
        arrayAdapterOrganizationName = new ArrayAdapter<>(this, R.layout.autocomplete_text_item, data);
        acOrganizationName.setAdapter(arrayAdapterOrganizationName);
    }

    private void setVisitStateArrayAdapter(ArrayList<String> data) {
        arrayAdapterVisitState = new ArrayAdapter<>(this, R.layout.autocomplete_text_item, data);
        acVisitState.setAdapter(arrayAdapterVisitState);
    }

    private void setDistrictArrayAdapter(ArrayList<VisitDistrict> data) {
        ArrayList<String> arrayNames = new ArrayList<>();

        for (int index = 0; index < data.size(); index++) {
            final VisitDistrict vd = data.get(index);
            if (vd.getState().trim().equals(acVisitState.getText().toString().trim())) {
                arrayNames.add(vd.getName());
            }
        }

        arrayAdapterVisitDist = new ArrayAdapter<>(this, R.layout.autocomplete_text_item, arrayNames);
        acDistrict.setAdapter(arrayAdapterVisitDist);
    }

    private void setTalukaArrayAdapter(ArrayList<String> data) {
        arrayAdapterVisitTaluka = new ArrayAdapter<>(this, R.layout.autocomplete_text_item, data);
        acTaluka.setAdapter(arrayAdapterVisitTaluka);
    }

    private void setVisitLocationArrayAdapter(ArrayList<String> data) {
        arrayAdapterVisitLocation = new ArrayAdapter<>(this, R.layout.autocomplete_text_item, data);
        acLocation.setAdapter(arrayAdapterVisitLocation);
    }
}