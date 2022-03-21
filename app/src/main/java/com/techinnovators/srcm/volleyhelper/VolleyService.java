package com.techinnovators.srcm.volleyhelper;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.techinnovators.srcm.utils.SharedPreferencesManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VolleyService {

    APIVInterface mResultCallback = null;
    Context mContext;

    public VolleyService(APIVInterface resultCallback, Context context) {
        mResultCallback = resultCallback;
        mContext = context;
    }


    public void postDataVolley(String url, JSONObject sendObj) {
        try {
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObj = new JsonObjectRequest(url, sendObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (mResultCallback != null)
                        mResultCallback.notifySuccess(response);
                }
            }, error -> {
                if (mResultCallback != null)
                    mResultCallback.notifyError(error);
            }) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    if (mResultCallback != null) {
                        mResultCallback.notifyNetworkParseResponse(response);
                    }
                    return super.parseNetworkResponse(response);
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                    //..add other headers
                    if (!sharedPreferencesManager.getToken().equals("")) {
                        params.put("Authorization", sharedPreferencesManager.getToken());
                    }
                    params.put("Content-Type", "application/json");
                    params.put("Accept", "application/json");
                    return params;
                }

            };
//            jsonObj.setRetryPolicy(new DefaultRetryPolicy(
//                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
//                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(jsonObj);

        } catch (Exception e) {
            Log.e("exceptionVolleyService:", e.getMessage());
        }
    }

    public void getDataVolley(String url, JSONObject jsonObject) {
        try {
            RequestQueue queue = Volley.newRequestQueue(mContext);
            if (jsonObject != null) {
                JsonObjectRequest jsonObj = new JsonObjectRequest(url, jsonObject, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mResultCallback != null)
                            mResultCallback.notifySuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mResultCallback != null)
                            mResultCallback.notifyError(error);
                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                        //..add other headers
                        params.put("Authorization", sharedPreferencesManager.getToken());
                        params.put("Content-Type", "application/json");
                        params.put("Accept", "application/json");
                        return params;
                    }

                };
//                jsonObj.setRetryPolicy(new DefaultRetryPolicy(
//                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
//                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonObj);
            } else {
                JsonObjectRequest jsonObj = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mResultCallback != null)
                            mResultCallback.notifySuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mResultCallback != null)
                            mResultCallback.notifyError(error);
                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        SharedPreferencesManager sharedPreferencesManager = new SharedPreferencesManager(mContext);
                        //..add other headers
                        params.put("Authorization", sharedPreferencesManager.getToken());
                        params.put("Content-Type", "application/json");
                        params.put("Accept", "application/json");
                        return params;
                    }

                };
//                jsonObj.setRetryPolicy(new DefaultRetryPolicy(
//                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2,
//                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(jsonObj);
            }

        } catch (Exception e) {
            Log.e("exceptionVolleyService:", e.getMessage());
        }
    }
}


