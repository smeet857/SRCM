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
import com.techinnovators.srcm.models.UserModel;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.NetworkUtils;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    RelativeLayout rlMain;
    TextView tvLogin;

    AppCompatEditText etUserName, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Application.context = this;
        NetworkUtils.startNetworkChangeListener(this);

        if (hasUserData()) {
            Intent intent = new Intent(LoginActivity.this, TaskActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            init();
            tvLogin.setOnClickListener(this);
        }
    }

    @Override
    protected void onResume() {
        Application.context = this;
        super.onResume();
    }

    private void init(){
        rlMain = findViewById(R.id.rlMain);
        tvLogin = findViewById(R.id.tvLogin);
        etUserName = findViewById(R.id.etUserName);
        etPassword = findViewById(R.id.etPassword);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tvLogin){
            onLoginTap();
        }
    }

    private boolean hasUserData(){
        ArrayList<UserModel> data = (ArrayList<UserModel>) DbClient.getInstance().userDao().getAll();

        if(data.size() > 0){
            Application.setUserModel(data.get(0));
            return true;
        }else{
            return false;
        }
    }

    private void onLoginTap(){
        final String strUsername = Objects.requireNonNull(etUserName.getText()).toString().trim();
        final String strPass = Objects.requireNonNull(etPassword.getText()).toString().trim();

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

    private void login(String strUsername, String strPass) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(getString(R.string.param_usr), strUsername);
            jsonObject.put(getString(R.string.param_pwd), strPass);

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    AppUtils.dismissProgress();

                    final UserModel userModel = new Gson().fromJson(response.toString(),UserModel.class);

                    if(!userModel.message.isEmpty() && !userModel.fullName.isEmpty()){
                        if(userModel.message.equalsIgnoreCase("No App")){
                            /// Save details to database
                            userModel.userName = strUsername;
                            Application.setUserModel(userModel);
                            DbClient.getInstance().userDao().insert(userModel);

                            Intent intent = new Intent(LoginActivity.this, TaskActivity2.class);
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
                    AppUtils.dismissProgress();
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
                    AppUtils.dismissProgress();
                }
            };

            final VolleyService volleyService = new VolleyService(callback, LoginActivity.this);
            volleyService.postDataVolley(getString(R.string.api_login),jsonObject);

            AppUtils.showProgress(this,getString(R.string.prog_dialog_title));

        } catch (Exception e) {
            AppUtils.displayAlertMessage(LoginActivity.this, "LOGIN", e.getMessage());
        }
    }
}