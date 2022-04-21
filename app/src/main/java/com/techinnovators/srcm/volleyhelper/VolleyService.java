package com.techinnovators.srcm.volleyhelper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.techinnovators.srcm.Activity.AddVisitRequestActivity2;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.utils.AppUtils;
import com.techinnovators.srcm.utils.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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

            JsonObjectRequest jsonObj = new JsonObjectRequest(url, sendObj, response -> {
                if (mResultCallback != null)
                    mResultCallback.notifySuccess(response);
            }, error -> {
                if (mResultCallback != null)
                    mResultCallback.notifyError(error);
            }) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//                    if (mResultCallback != null) {
//                        mResultCallback.notifyNetworkParseResponse(response);
//                    }
                    return super.parseNetworkResponse(response);
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    //..add other headers
                    if (!Application.getUserModel().token.isEmpty()) {
                        params.put("Authorization", Application.getUserModel().token);
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
                        if (!Application.getUserModel().token.isEmpty()) {
                            params.put("Authorization", Application.getUserModel().token);
                        }
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
                        if (!Application.getUserModel().token.isEmpty()) {
                            params.put("Authorization", Application.getUserModel().token);
                        }
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

    public void putDataVolley(String url, JSONObject sendObj) {
        try {
            RequestQueue queue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObj = new JsonObjectRequest(Request.Method.PUT,url, sendObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (mResultCallback != null)
                        mResultCallback.notifySuccess(response);
                }
            }, error -> {
                if (mResultCallback != null){
                    mResultCallback.notifyError(error);
                }

            }) {
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//                    if (mResultCallback != null) {
//                        mResultCallback.notifyNetworkParseResponse(response);
//                    }
                    return super.parseNetworkResponse(response);
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    //..add other headers
                    if (!Application.getUserModel().token.isEmpty()) {
                        params.put("Authorization", Application.getUserModel().token);
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
}


