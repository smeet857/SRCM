package com.techinnovators.srcm.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.TasksActivity;
import com.techinnovators.srcm.models.UserModel;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout rlMain;
    VolleyService mVolleyService;
    TextView tvLogin;

    AppCompatEditText etUserName, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (hasUserData()) {
            Intent intent = new Intent(LoginActivity.this, TasksActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            rlMain = findViewById(R.id.rlMain);
            tvLogin = findViewById(R.id.tvLogin);
            etUserName = findViewById(R.id.etUserName);
            etPassword = findViewById(R.id.etPassword);

            tvLogin.setOnClickListener(this);
        }

    }
//        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(LoginActivity.this);
//        if (!sharedPreferencesManager.getUserName().isEmpty()) {
//            AppUtils.hideKeyboard(this);
//
//            rlMain.setVisibility(View.GONE);
//
//            Intent intent = new Intent(LoginActivity.this, TasksActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//
//        } else {
//            rlMain.setVisibility(View.VISIBLE);
//        }
//    }

    private boolean hasUserData(){
        ArrayList<UserModel> data = (ArrayList<UserModel>) DbClient.getInstance().userDao().getAll();

        if(data.size() > 0){
            Application.setUserModel(data.get(0));
            return true;
        }else{
            return false;
        }
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == LOCATION_PERMISSIONS_CODE && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
//            getCurrentLocation();
//        } else {
//            new AlertDialog.Builder(LoginActivity.this).setTitle(R.string.location_title).
//                    setMessage(R.string.location_service_msg)
//                    .setPositiveButton("SETTINGS", (dialogInterface, i)
//                            -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).
//                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))).
//                    setCancelable(false).show();
//        }
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

    private void login(String strUsername, String strPass) {

        String apiUrl = getString(R.string.api_url);
        String endpoint = getString(R.string.api_methodname_login);
        apiUrl += endpoint;

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put(getString(R.string.login_req_api_param_usr), strUsername);
            jsonObject.put(getString(R.string.login_req_api_param_pwd), strPass);

            mVolleyService = new VolleyService(new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    AppUtils.dismissProgrees();
                    //RESPONSE_SUCCESS_PARAM IS XXXXX

                    final UserModel userModel = new Gson().fromJson(response.toString(),UserModel.class);

                    if(!userModel.message.isEmpty() && !userModel.fullName.isEmpty()){
                        if(userModel.message.equalsIgnoreCase("No App")){
                            /// Save details to database
                            userModel.userName = strUsername;
                            Application.setUserModel(userModel);
                            DbClient.getInstance().userDao().insert(userModel);

                            Intent intent = new Intent(LoginActivity.this, TasksActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }else{
                        AppUtils.showSnackBar(LoginActivity.this, rlMain, getString(R.string.Invalid_credentials));
                    }
                }

                @Override
                public void notifyError(VolleyError error) {
                    AppUtils.dismissProgrees();
                    if (error.networkResponse != null) {
                        switch (error.networkResponse.statusCode) {
                            case 401:
                                String responseBody;
                                try {
                                    responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(responseBody);
                                    if (!data.getString("message").isEmpty()) {
                                        AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", data.getString("message"));
                                    }
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", e.getMessage());
                                    e.printStackTrace();
                                }
                                break;
                            case 404:
                                AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", getString(R.string.error_404));
                                break;
                            case 500:
                                AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", getString(R.string.error_500));
                                break;
                        }
                    }
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    AppUtils.dismissProgrees();
                }
            }, LoginActivity.this);

            mVolleyService.postDataVolley(apiUrl, jsonObject);
            AppUtils.showProgress(this,getString(R.string.prog_dialog_title));

        } catch (Exception e) {
            AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", e.getMessage());
        }
    }

    private void onLoginTap(){
        final String strUsername = etUserName.getText().toString().trim();
        final String strPass = etPassword.getText().toString().trim();

        if (strUsername.isEmpty()) {
            AppUtils.showSnackBar(LoginActivity.this, rlMain, "Username can't be empty");
            return;
        }
        if (strPass.isEmpty()) {
            AppUtils.showSnackBar(LoginActivity.this, rlMain, "Password can't be empty");
            return;
        }
        if (NetworkUtils.isNetworkConnected(LoginActivity.this)) {
            login(strUsername, strPass);
        } else {
            AppUtils.showSnackBar(LoginActivity.this, rlMain, getString(R.string.internet_off));
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tvLogin){
            onLoginTap();
        }
    }
}