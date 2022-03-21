package com.techinnovators.srcm.volleyhelper;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface APIVInterface {

    void notifySuccess(JSONObject response);

    void notifyError(VolleyError error);

    void notifyNetworkParseResponse(NetworkResponse response);



}
