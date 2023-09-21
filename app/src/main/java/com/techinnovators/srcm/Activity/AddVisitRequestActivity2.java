package com.techinnovators.srcm.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.models.EventCategory;
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

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class AddVisitRequestActivity2 extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<EventCategory> projectTypes, eventCategories = new ArrayList<>();
    private ArrayList<String>
            arrayEventSector,
            arrayProjectType,
            arrayEventCategory,
            arrayOrganizationName,
            arrayVisitState,
            arrayVisitDist,
            arrayVisitTaluka,
            arrayVisitLocation,
            arrayVisitLocationToBe = new ArrayList<>();

    private AutoCompleteTextView
            acEventSector,
            acEventCategory,
            acProjectType,
            acOrganizationName,
            acVisitState,
            acDistrict,
            acTaluka,
            acLocation,
            acVisitLocationToBe;

    private ConstraintLayout csMain;

    private AppCompatEditText etContPersonName;
    private AppCompatEditText etContPersonNo;

    private Spinner spinnerVisitType, spinnerVisitMode;

    private ImageView ivBack, imgAddOrganizationName;


    private AppCompatButton btnSubmit;

    private DbClient db;

    private ArrayList<VisitDistrict> visitDistrictList = new ArrayList<>();
    private ArrayList<Taluka> visitTalukaList = new ArrayList<>();
    private ArrayList<VisitLocation> visitLocationList = new ArrayList<>();

    private SpinnerDialog spinnerDialog;
    private NestedScrollView nestedScrollView;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_visit_request);
        Application.context = this;

        init();
        initListener();
        initData();
    }

    @Override
    protected void onResume() {
        Application.context = this;
        super.onResume();
    }

    private void init() {
        nestedScrollView = findViewById(R.id.nestedScrollView);
        progressBar = findViewById(R.id.progress);

        ivBack = findViewById(R.id.ivBack);
        imgAddOrganizationName = findViewById(R.id.imgAddOrganizationName);
        btnSubmit = findViewById(R.id.btnSubmit);

        spinnerVisitMode = findViewById(R.id.spin_visitmode);
        spinnerVisitType = findViewById(R.id.spin_activity);

        csMain = findViewById(R.id.csMain);

        acEventSector = findViewById(R.id.acEventSector);
        acEventCategory = findViewById(R.id.avEventCategory);
        acProjectType = findViewById(R.id.acProjectType);
        acOrganizationName = findViewById(R.id.acOrgName);
        acVisitState = findViewById(R.id.acState);
        acDistrict = findViewById(R.id.acDistrict);
        acTaluka = findViewById(R.id.acTaluka);
        acLocation = findViewById(R.id.acVLocation);
        acVisitLocationToBe = findViewById(R.id.acVisitLocationToBe);

        AppCompatEditText etVisitAssignedTo = findViewById(R.id.etAssignedTo);
        etContPersonName = findViewById(R.id.etContPersonName);
        etContPersonNo = findViewById(R.id.etContPersonNo);

        etVisitAssignedTo.setText(Application.getUserModel().employeeId);

        db = DbClient.getInstance();
    }

    private void initListener() {
        ivBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
//        etVisitDate.setOnClickListener(this);

        acEventSector.setOnClickListener(v -> {
            spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayEventSector, "Select Event Sector", "Close");
            spinnerDialog.bindOnSpinerListener((item, position) -> {
                acEventSector.setText(item);

                acEventCategory.setText("");
                acProjectType.setText("");

                arrayEventCategory = new ArrayList<>();
                arrayProjectType = new ArrayList<>();

                for (int i = 0; i < eventCategories.size(); i++) {
                    if (Objects.equals(item.trim(), eventCategories.get(i).getName().trim())) {
                        arrayEventCategory = eventCategories.get(i).getTypes();
                        break;
                    }
                }

                for (int i = 0; i < projectTypes.size(); i++) {
                    if (Objects.equals(item.trim(), projectTypes.get(i).getName().trim())) {
                        arrayProjectType = projectTypes.get(i).getTypes();
                        break;
                    }
                }
            });
            spinnerDialog.showSpinerDialog();
        });

        acEventCategory.setOnClickListener(v -> {
            if(acEventSector.getText().toString().isEmpty()){
                AppUtils.showSnackBar(AddVisitRequestActivity2.this, csMain, "Please select event sector first");
            }else{
                spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayEventCategory, "Select Event Type", "Close");
                spinnerDialog.bindOnSpinerListener((item, position) -> acEventCategory.setText(item));
                spinnerDialog.showSpinerDialog();
            }
        });

        acProjectType.setOnClickListener(v -> {
            if(acEventCategory.getText().toString().isEmpty()) {
                AppUtils.showSnackBar(AddVisitRequestActivity2.this, csMain, "Please select event sector first");
            }else{
                spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayProjectType, "Select Event Categories", "Close");
                spinnerDialog.bindOnSpinerListener((item, position) -> acProjectType.setText(item));
                spinnerDialog.showSpinerDialog();
            }
        });

        acOrganizationName.setOnClickListener(v -> {
//                acOrganizationName.showDropDown();
            spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayOrganizationName, "Select Organization", "Close");
            spinnerDialog.bindOnSpinerListener((item, position) -> acOrganizationName.setText(item));
            spinnerDialog.showSpinerDialog();
        });

        acVisitState.setOnClickListener(v -> {
//                acVisitState.showDropDown();
            spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayVisitState, "Select Visit State", "Close");
            spinnerDialog.bindOnSpinerListener((item, position) -> {
                acVisitState.setText(item);

                acDistrict.getText().clear();
                acTaluka.getText().clear();
                acLocation.getText().clear();
                setDistrictArrayAdapter(visitDistrictList);
            });
            spinnerDialog.showSpinerDialog();
        });

        acDistrict.setOnClickListener(v -> {
            if (acVisitState.getText().toString().isEmpty()) {
                AppUtils.showSnackBar(AddVisitRequestActivity2.this, csMain, "Please select visit state first");
            } else {
                spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayVisitDist, "Select Visit District", "Close");
                spinnerDialog.bindOnSpinerListener((item, position) -> {
                    acDistrict.setText(item);

                    acTaluka.getText().clear();
                    acLocation.getText().clear();
                    setTalukaArrayAdapter(visitTalukaList);
                });
                spinnerDialog.showSpinerDialog();
            }
        });

        acTaluka.setOnClickListener(v -> {
            if (acDistrict.getText().toString().isEmpty()) {
                AppUtils.showSnackBar(AddVisitRequestActivity2.this, csMain, "Please select visit district first");
            } else {
                spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayVisitTaluka, "Select Visit Taluka", "Close");
                spinnerDialog.bindOnSpinerListener((item, position) -> {
                    acTaluka.setText(item);
                    acLocation.getText().clear();
                    setVisitLocationArrayAdapter(visitLocationList);
                });
                spinnerDialog.showSpinerDialog();
            }
        });

        acLocation.setOnClickListener(v -> {
            if (acTaluka.getText().toString().isEmpty()) {
                AppUtils.showSnackBar(AddVisitRequestActivity2.this, csMain, "Please select visit taluka first");
            } else {
                spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayVisitLocation, "Select Visit Location", "Close");
                spinnerDialog.bindOnSpinerListener((item, position) -> acLocation.setText(item));
                spinnerDialog.showSpinerDialog();
            }
        });

        arrayVisitLocationToBe.add("Existing");
        arrayVisitLocationToBe.add("New");

        acVisitLocationToBe.setOnClickListener(v -> {
                spinnerDialog = new SpinnerDialog(AddVisitRequestActivity2.this, arrayVisitLocationToBe, "Select Visit Location To Be", "Close");
                spinnerDialog.bindOnSpinerListener((item, position) -> acVisitLocationToBe.setText(item));
                spinnerDialog.showSpinerDialog();
        });
    }

    private void initData() {

        if (NetworkUtils.isNetworkConnected(this)) {
            imgAddOrganizationName.setVisibility(View.VISIBLE);
            imgAddOrganizationName.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Add Organization Name");

                final EditText input = new EditText(this);
                input.setHint("Organization name");
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                FrameLayout container = new FrameLayout(this);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = 30;
                params.rightMargin = 30;
                params.topMargin = 20;

                input.setLayoutParams(params);
                container.addView(input);

                builder.setView(container);

                builder.setPositiveButton("Add",
                        (dialog, which) -> {

                        });


                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                final AlertDialog dialog = builder.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                    if (input.getText().toString().isEmpty()) {
                        input.setError("can not be blank");
                        return;
                    }
                    dialog.cancel();
                    addOrganizationName(input.getText().toString());
                });
            });
        } else {
            imgAddOrganizationName.setVisibility(View.GONE);
        }

        getEventSectors();
        getAllEventCategories();
        getAllProjectTypes();
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
        }else if (view.getId() == R.id.btnSubmit) {
            onSubmitTap();
        }
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        nestedScrollView.setVisibility(View.GONE);
    }

    private void dismissProgress() {
        progressBar.setVisibility(View.GONE);
        nestedScrollView.setVisibility(View.VISIBLE);
    }

    private void onSubmitTap() {
        if (TextUtils.isEmpty(acEventSector.getText().toString())) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.event_sector_empty_msg));
            return;
        }
        if (TextUtils.isEmpty(acEventCategory.getText().toString())) {
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
        if (TextUtils.isEmpty(acVisitLocationToBe.getText().toString())) {
            AppUtils.showSnackBar(this, csMain, getString(R.string.visitlocationToBe_empty_msg));
            return;
        }

        apiCreateTask();
    }

    private void apiCreateTask() {
        AppUtils.hideKeyboard(this);

        Tasks visitRequest = new Tasks();

        visitRequest.setVisit_district(acDistrict.getText().toString());
        visitRequest.setVisit_location(acLocation.getText().toString());
        visitRequest.setVisit_location_to_be(acVisitLocationToBe.getText().toString());
        visitRequest.setVisit_taluka(acTaluka.getText().toString());
        visitRequest.setVisit_state(acVisitState.getText().toString());
        visitRequest.setProject_type(acEventCategory.getText().toString().trim());
        visitRequest.setEventSector(acEventSector.getText().toString().trim());
        visitRequest.setProject_name(acProjectType.getText().toString());
        visitRequest.setVisit_place(acOrganizationName.getText().toString());

        visitRequest.setVisit_type(spinnerVisitType.getSelectedItem().toString());
        visitRequest.setVisit_mode(spinnerVisitMode.getSelectedItem().toString());

        visitRequest.setContact_person_mobile_no(Objects.requireNonNull(etContPersonNo.getText()).toString());
        visitRequest.setContact_person_name(Objects.requireNonNull(etContPersonName.getText()).toString());

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
                                        responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                        JSONObject data = new JSONObject(responseBody);
                                        if (!data.getString("message").isEmpty()) {
                                            AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.create_Visit), data.getString("message"));
                                        }
                                    } catch (JSONException e) {
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
            AppUtils.dismissProgress();
            AppUtils.displayAlertMessage(this, "Alert", "No internet connectivity");
        }
    }

    private void getEventSectors() {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                String apiUrl = getString(R.string.api_event_sector);

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
                                setEventTypeArrayAdapter(arrayList);
                                dismissProgress();

                                /// set in local storage
                                Application.getUserModel().eventSector = arrayList.toString();
                                db.userDao().update(Application.getUserModel());
                            }
                        } catch (JSONException jsonException) {
                            setEventSectorFromLocal();
                            jsonException.printStackTrace();
                        }
                    }

                    @Override
                    public void notifyError(VolleyError error) {
                        setEventSectorFromLocal();
                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 401:
                                    String responseBody;
                                    try {
                                        responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                        JSONObject data = new JSONObject(responseBody);
                                        if (!data.getString("message").isEmpty()) {
                                            AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", data.getString("message"));
                                        }
                                    } catch (JSONException e) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403: {
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString("_server_messages").isEmpty()) {
                                            final String message = data.getString("_server_messages");

                                            JSONArray jsonArray = new JSONArray(message);
                                            JSONObject jo = new JSONObject(jsonArray.getString(0));
                                            AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                        }
                                    } catch (JSONException e) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                        e.printStackTrace();
                                    }
                                    break;
                                }
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
                        setEventSectorFromLocal();
                    }
                };

                VolleyService mVolleyService = new VolleyService(callback, this);
                mVolleyService.getDataVolley(apiUrl, null);

            } catch (Exception e) {
                AppUtils.displayAlertMessage(this, "PROJECT NAME", e.getMessage());
                setEventSectorFromLocal();
            }
        } else {
            setEventSectorFromLocal();
        }
    }

    private void setEventSectorFromLocal(){
        /// set from local storage
        try{
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().eventSector);
            setEventTypeArrayAdapter(data);
            dismissProgress();
        }catch(Exception e){
            Log.e("setEventSectorFromLocal",e.toString());
        }
    }

    private void getAllEventCategories() {
        if (NetworkUtils.isNetworkConnected(this)) {
            String apiUrl = getString(R.string.api_all_event_category);

            apiUrl += "?limit_page_length=None";

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONObject data = response.getJSONObject(getString(R.string.param_message));

                        final ArrayList<EventCategory> finalList = new ArrayList<>();
                        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                            String name = (String) it.next();
                            JSONArray arr = data.optJSONArray(name);

                            EventCategory eventCategory = new EventCategory();
                            eventCategory.setName(name);

                            ArrayList<String> type = new ArrayList<>();

                            for (int i = 0; i < Objects.requireNonNull(arr).length(); i++) {
                                type.add((String) arr.get(i));
                            }

                            eventCategory.setTypes(type);

                            finalList.add(eventCategory);
                        }

                        Gson gson = new Gson();
                        Application.getUserModel().eventCategories = gson.toJson(finalList, new TypeToken<ArrayList<EventCategory>>() {
                        }.getType());
                        db.userDao().update(Application.getUserModel());

                        eventCategories = finalList;

                    } catch (JSONException jsonException) {
                        setEventCategoriesFromLocal();
                        jsonException.printStackTrace();
                    }
                }

                @Override
                public void notifyError(VolleyError error) {
                    setEventCategoriesFromLocal();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", data.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }
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
                    setEventCategoriesFromLocal();
                }
            };

            try {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(getString(R.string.param_app_auth_key), getString(R.string.auth_key));

                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.postDataVolley(apiUrl, jsonObject);

            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", e.getMessage());
                setEventCategoriesFromLocal();
            }
        } else {
            setEventCategoriesFromLocal();
        }
    }

    @SuppressLint("LongLogTag")
    private void setEventCategoriesFromLocal(){
     try{
         Gson gson = new Gson();
         TypeToken<ArrayList<EventCategory>> token = new TypeToken<ArrayList<EventCategory>>() {
         };
         eventCategories = gson.fromJson(Application.getUserModel().eventCategories, token.getType());
     }catch (Exception e){
         Log.e("setEventCategoriesFromLocal",e.toString());
     }
    }

    private void getAllProjectTypes() {
        if (NetworkUtils.isNetworkConnected(this)) {
            String apiUrl = getString(R.string.api_all_project_type);

            apiUrl += "?limit_page_length=None";

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    try {
                        JSONObject data = response.getJSONObject(getString(R.string.param_message));

                        final ArrayList<EventCategory> finalList = new ArrayList<>();
                        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                            String name = (String) it.next();
                            JSONArray arr = data.optJSONArray(name);

                            EventCategory eventCategory = new EventCategory();
                            eventCategory.setName(name);

                            ArrayList<String> type = new ArrayList<>();

                            for (int i = 0; i < Objects.requireNonNull(arr).length(); i++) {
                                type.add((String) arr.get(i));
                            }

                            eventCategory.setTypes(type);

                            finalList.add(eventCategory);
                        }

                        Gson gson = new Gson();
                        Application.getUserModel().projectTypes = gson.toJson(finalList, new TypeToken<ArrayList<EventCategory>>() {
                        }.getType());
                        db.userDao().update(Application.getUserModel());

                        projectTypes = finalList;

                    } catch (JSONException jsonException) {
                        setProjectTypesFromLocal();
                        jsonException.printStackTrace();
                    }
                }

                @Override
                public void notifyError(VolleyError error) {
                    setProjectTypesFromLocal();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", data.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }
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

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(getString(R.string.param_app_auth_key), getString(R.string.auth_key));

                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.postDataVolley(apiUrl, jsonObject);

            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT TYPE", e.getMessage());
                setProjectTypesFromLocal();
            }
        } else {
            setProjectTypesFromLocal();
        }
    }

    @SuppressLint("LongLogTag")
    private void setProjectTypesFromLocal(){
        try{
            Gson gson = new Gson();
            TypeToken<ArrayList<EventCategory>> token = new TypeToken<ArrayList<EventCategory>>() {
            };
            projectTypes = gson.fromJson(Application.getUserModel().projectTypes, token.getType());
        }catch (Exception e){
            Log.e("setProjectTypesFromLocal",e.toString());
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
                        setOrganisationNameFromLocal();
                        jsonException.printStackTrace();
                    }

                }

                @Override
                public void notifyError(VolleyError error) {
                    setOrganisationNameFromLocal();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), data.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }
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
                    setOrganisationNameFromLocal();
                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.orgname), e.getMessage());
                setOrganisationNameFromLocal();
            }
        } else {
            setOrganisationNameFromLocal();
        }
    }

    @SuppressLint("LongLogTag")
    private void setOrganisationNameFromLocal(){
        try{
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().organizationName);
            setOrganizationNameArrayAdapter(data);
        }catch (Exception e){
            Log.e("setOrganisationNameFromLocal",e.toString());
        }
    }

    private void addOrganizationName(String title) {
        if (NetworkUtils.isNetworkConnected(this)) {
            try {
                String apiUrl = getString(R.string.api_organization_name);

                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        getOrganisationName();
                    }

                    @Override
                    public void notifyError(VolleyError error) {
                        if (error.networkResponse != null) {
                            switch (error.networkResponse.statusCode) {
                                case 401:
                                    String responseBody;
                                    try {
                                        responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                        JSONObject data = new JSONObject(responseBody);
                                        if (!data.getString("message").isEmpty()) {
                                            AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", data.getString("message"));
                                        }
                                    } catch (JSONException e) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "PROJECT NAME", e.getMessage());
                                        e.printStackTrace();
                                    }
                                    break;
                                case 403: {
                                    String response;
                                    try {
                                        response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                        JSONObject data = new JSONObject(response);
                                        if (!data.getString("_server_messages").isEmpty()) {
                                            final String message = data.getString("_server_messages");

                                            JSONArray jsonArray = new JSONArray(message);
                                            JSONObject jo = new JSONObject(jsonArray.getString(0));
                                            AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                        }
                                    } catch (JSONException e) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                        e.printStackTrace();
                                    }
                                    break;
                                }
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

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(getString(R.string.param_place), title);

                VolleyService mVolleyService = new VolleyService(callback, this);
                mVolleyService.postDataVolley(apiUrl, jsonObject);

            } catch (Exception e) {
                AppUtils.displayAlertMessage(this, "PROJECT NAME", e.getMessage());
            }
        } else {
            AppUtils.displayAlertMessage(this, "Alert", "No internet connectivity");
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
                        setVisitStateFromLocal();
                        jsonException.printStackTrace();
                    }
                }

                @Override
                public void notifyError(VolleyError error) {
                    setVisitStateFromLocal();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), data.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }
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
                    setVisitStateFromLocal();
                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.state), e.getMessage());
                setVisitStateFromLocal();
            }
        } else {
            setVisitStateFromLocal();
        }
    }

    @SuppressLint("LongLogTag")
    private void setVisitStateFromLocal(){
        try{
            final ArrayList<String> data = AppUtils.stringToArray(Application.getUserModel().visitState);
            setVisitStateArrayAdapter(data);
        }catch (Exception e){
            Log.e("setVisitStateFromLocal",e.toString());
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

                                Application.getUserModel().district = arrayJson.toString();
                                db.userDao().update(Application.getUserModel());
                            }
                        }
                    } catch (
                            JSONException jsonException) {
                        setDistrictFromLocal();
                        jsonException.printStackTrace();
                    }

                }

                @Override
                public void notifyError(VolleyError error) {
                    setDistrictFromLocal();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }
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
                    setDistrictFromLocal();
                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                setDistrictFromLocal();
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), e.getMessage());
            }
        } else {
            setDistrictFromLocal();
        }
    }

    @SuppressLint("LongLogTag")
    private void setDistrictFromLocal(){
        try{
            final ArrayList<String> data = AppUtils.jsonArrayStringToStringArray(Application.getUserModel().district);
            Gson gson = new Gson();

            for (int i = 0; i < data.size(); i++) {
                final String str = data.get(i);
                visitDistrictList.add(gson.fromJson(str, VisitDistrict.class));
            }
        }catch (Exception e){
            Log.e("setDistrictFromLocal",e.toString());
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

                            Gson gson = new Gson();
                            Type listType = new TypeToken<TalukaResponse>() {
                            }.getType();

                            TalukaResponse tasksResponse = gson.fromJson(response.toString(), listType);
                            visitTalukaList = new ArrayList<>(tasksResponse.getData());

                            if (!visitTalukaList.isEmpty()) {
                                ArrayList<String> arrayJson = new ArrayList<>();
                                for (int index = 0; index < visitTalukaList.size(); index++) {
                                    final Taluka t = visitTalukaList.get(index);
                                    arrayJson.add(gson.toJson(t));
                                }

                                Application.getUserModel().taluka = arrayJson.toString();
                                db.userDao().update(Application.getUserModel());
                            }
                        }
                    } catch (JSONException jsonException) {
                        setTalukaFromLocal();
                        jsonException.printStackTrace();
                    }
                }

                @Override
                public void notifyError(VolleyError error) {
                    setTalukaFromLocal();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }
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
                    setTalukaFromLocal();
                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                setTalukaFromLocal();
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), e.getMessage());
            }
        } else {
            setTalukaFromLocal();
        }
    }

    @SuppressLint("LongLogTag")
    private void setTalukaFromLocal(){
        try{
            final ArrayList<String> data = AppUtils.jsonArrayStringToStringArray(Application.getUserModel().taluka);

            Gson gson = new Gson();

            for (int i = 0; i < data.size(); i++) {
                final String str = data.get(i);
                visitTalukaList.add(gson.fromJson(str, Taluka.class));
            }
        }catch (Exception e){
            Log.e("setTalukaFromLocal",e.toString());
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

                            Gson gson = new Gson();
                            Type listType = new TypeToken<VisitLocationResponse>() {
                            }.getType();

                            VisitLocationResponse tasksResponse = gson.fromJson(response.toString(), listType);
                            visitLocationList = new ArrayList<>(tasksResponse.getData());

                            if (!visitLocationList.isEmpty()) {
                                ArrayList<String> arrayJson = new ArrayList<>();

                                for (int index = 0; index < visitLocationList.size(); index++) {
                                    final VisitLocation visitLocation = visitLocationList.get(index);
                                    arrayJson.add(gson.toJson(visitLocation));
                                }

                                Application.getUserModel().locationOfVisit = arrayJson.toString();
                                db.userDao().update(Application.getUserModel());
                            }
                        }
                    } catch (JSONException jsonException) {
                        setLocationOfVisitFromLocal();
                        jsonException.printStackTrace();
                    }

                }

                @Override
                public void notifyError(VolleyError error) {
                    setLocationOfVisitFromLocal();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visit_district), data.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visitlocation), e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 403: {
                                String response;
                                try {
                                    response = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                                    JSONObject data = new JSONObject(response);
                                    if (!data.getString("_server_messages").isEmpty()) {
                                        final String message = data.getString("_server_messages");

                                        JSONArray jsonArray = new JSONArray(message);
                                        JSONObject jo = new JSONObject(jsonArray.getString(0));
                                        AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", Html.fromHtml(jo.getString("message")).toString());
                                    }
                                } catch (JSONException e) {
                                    AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, "Alert", getString(R.string.error_403));
                                    e.printStackTrace();
                                }
                                break;
                            }
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
                    setLocationOfVisitFromLocal();
                }
            };

            try {
                VolleyService mVolleyService = new VolleyService(callback, AddVisitRequestActivity2.this);
                mVolleyService.getDataVolley(apiUrl, null);
            } catch (Exception e) {
                setLocationOfVisitFromLocal();
                AppUtils.displayAlertMessage(AddVisitRequestActivity2.this, getString(R.string.visitlocation), e.getMessage());
            }
        } else {
            setLocationOfVisitFromLocal();
        }
    }

    @SuppressLint("LongLogTag")
    private void setLocationOfVisitFromLocal(){
        try{
            final ArrayList<String> data = AppUtils.jsonArrayStringToStringArray(Application.getUserModel().locationOfVisit);

            Gson gson = new Gson();

            for (int i = 0; i < data.size(); i++) {
                final String str = data.get(i);
                visitLocationList.add(gson.fromJson(str, VisitLocation.class));
            }
        }catch (Exception e){
            Log.e("setLocationOfVisitFromLocal",e.toString());
        }
    }

    private void setEventTypeArrayAdapter(ArrayList<String> data) {
        arrayEventSector = data;
    }

    private void setOrganizationNameArrayAdapter(ArrayList<String> data) {
        arrayOrganizationName = data;
    }

    private void setVisitStateArrayAdapter(ArrayList<String> data) {
        arrayVisitState = data;
    }

    private void setDistrictArrayAdapter(ArrayList<VisitDistrict> data) {
        ArrayList<String> arrayNames = new ArrayList<>();

        for (int index = 0; index < data.size(); index++) {
            final VisitDistrict vd = data.get(index);
            if (vd.getState().trim().equals(acVisitState.getText().toString().trim())) {
                arrayNames.add(vd.getName());
            }
        }

        arrayVisitDist = arrayNames;

        if (arrayNames.isEmpty()) {
            AppUtils.showSnackBar(AddVisitRequestActivity2.this, findViewById(android.R.id.content), "No district of selected visit state");
        }

    }

    private void setTalukaArrayAdapter(ArrayList<Taluka> data) {
        ArrayList<String> arrayNames = new ArrayList<>();

        for (int index = 0; index < data.size(); index++) {
            final Taluka t = data.get(index);
            if (t.getDistrict().trim().equals(acDistrict.getText().toString().trim())) {
                arrayNames.add(t.getName());
            }
        }

        arrayVisitTaluka = arrayNames;

        if (arrayNames.isEmpty()) {
            AppUtils.showSnackBar(AddVisitRequestActivity2.this, findViewById(android.R.id.content), "No taluka of selected district");
        }
    }

    private void setVisitLocationArrayAdapter(ArrayList<VisitLocation> data) {
        ArrayList<String> arrayNames = new ArrayList<>();

        for (int index = 0; index < data.size(); index++) {
            final VisitLocation vl = data.get(index);
            if (vl.getTaluka().trim().equals(acTaluka.getText().toString().trim())) {
                arrayNames.add(vl.getName());
            }
        }

        arrayVisitLocation = arrayNames;

        if (arrayNames.isEmpty()) {
            AppUtils.showSnackBar(AddVisitRequestActivity2.this, findViewById(android.R.id.content), "No Location of selected taluka");
        }
    }
}