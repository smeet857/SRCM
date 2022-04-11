package com.techinnovators.srcm.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.techinnovators.srcm.Activity.TaskActivity2;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONObject;

import java.util.ArrayList;

public class NetworkUtils {
    private static ConnectivityManager connectivityManager;
   private static ConnectivityManager.NetworkCallback networkCallback;
   static boolean isNetworkConnected = false;

    public static boolean isNetworkConnected(Context context) {
        return isNetworkConnected;
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        //should check null because in airplane mode it will be null
//        return (netInfo != null && netInfo.isConnected());
    }

    public static void startNetworkChangeListener(Context context){

       if(networkCallback == null){
           networkCallback = new ConnectivityManager.NetworkCallback() {
               @Override
               public void onAvailable(Network network) {
                   isNetworkConnected = true;
                   syncData();
               }

               @Override
               public void onLost(Network network) {
                   isNetworkConnected = false;
                   Toast.makeText(context, "Internet Not Available", Toast.LENGTH_SHORT).show();

               }
           };
       }

       if(connectivityManager == null){
           connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
       }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }

    public static void closeNetworkChangeListener(){
        if(connectivityManager != null && networkCallback != null){
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    public static void syncData(){
        AppUtils.showProgress(Application.context,"Sync data...");
        final ArrayList<Tasks> tasksDbList  = (ArrayList<Tasks>) DbClient.getInstance().tasksDao().getAll();

        for (int i = 0;i < tasksDbList.size();i++){
            final Tasks t = tasksDbList.get(i);

            if(!t.isSync){
                createTaskFromSync(t);
            }
            if(!t.isCheckInSync){
                checkInTaskFromSync(t);
            }
            if(!t.isCheckOutSync){
                checkOutTaskFromSync(t);
            }
        }

        AppUtils.dismissProgress();
    }

    private static void createTaskFromSync(Tasks t){

        String apiUrl = Application.context.getString(R.string.api_create_task);

        final APIVInterface callback = new APIVInterface() {
            @Override
            public void notifySuccess(JSONObject response) {
                t.isSync = true;
                DbClient.getInstance().tasksDao().update(t);
            }

            @Override
            public void notifyError(VolleyError error) {
                Log.e("Error on create task",error.toString());
            }

            @Override
            public void notifyNetworkParseResponse(NetworkResponse response) {
                Log.e("Error on create task",response.toString());
            }
        };

        final VolleyService mVolleyService = new VolleyService(callback, Application.context);
        mVolleyService.postDataVolley(apiUrl, t.createTaskJson());
    }
    private static void checkInTaskFromSync(Tasks t){
        try{

            String api = Application.context.getString(R.string.api_check_in);
            api += "/" + t.name;

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    /// set data local
                    t.isCheckInSync = true;
                    DbClient.getInstance().tasksDao().update(t);
                }

                @Override
                public void notifyError(VolleyError error) {
                    Log.e("Error on check in",error.toString());
                }

                @SuppressLint("LongLogTag")
                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    Log.e("Error on check in parse response",response.toString());
                }
            };

            final VolleyService volleyService = new VolleyService(callback,Application.context);
            volleyService.putDataVolley(api,t.checkInJson());

        }catch (Exception e){
            Log.e("Error on api check in",e.getMessage());
        }
    }
    public static void checkOutTaskFromSync(Tasks t){
        try{
            String api = Application.context.getString(R.string.api_check_out);
            api += "/" + t.name;

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    /// set data local
                    t.isCheckOutSync = true;

                    DbClient.getInstance().tasksDao().update(t);

                }

                @Override
                public void notifyError(VolleyError error) {
                    Log.e("Check Out Error",error.toString());
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    Log.e("Check out Error",response.toString());
                }
            };

            final VolleyService volleyService = new VolleyService(callback,Application.context);
            volleyService.putDataVolley(api,t.checkOutJson());
        }catch (Exception e){
            Log.e("Error on api check out",e.getMessage());
        }
    }

}
