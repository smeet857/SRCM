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
import com.google.android.gms.common.api.Api;
import com.techinnovators.srcm.Activity.TaskActivity2;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.Database.DbClient;
import com.techinnovators.srcm.R;
import com.techinnovators.srcm.callbacks.ProcessCompleteCallback;
import com.techinnovators.srcm.models.Tasks;
import com.techinnovators.srcm.volleyhelper.APIVInterface;
import com.techinnovators.srcm.volleyhelper.VolleyService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class NetworkUtils {
    private static ConnectivityManager connectivityManager;
   private static ConnectivityManager.NetworkCallback networkCallback;
   public static boolean isNetworkConnected = false;


   private static boolean firstTimeCall = true;

    public static boolean isNetworkConnected(Context context) {
        return isNetworkConnected;
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        //should check null because in airplane mode it will be null
//        return (netInfo != null && netInfo.isConnected());
    }

    public static synchronized void startNetworkChangeListener(Context context){

       if(networkCallback == null){
           networkCallback = new ConnectivityManager.NetworkCallback() {
               @Override
               public void onAvailable(Network network) {
                   isNetworkConnected = true;
                   if(Application.isLogin){
                       if(firstTimeCall){
                           firstTimeCall = false;
                       }else{
                           syncData();
                       }
                   }
               }

               @Override
               public void onLost(Network network) {
                   isNetworkConnected = false;
                   if(firstTimeCall){
                       firstTimeCall = false;
                   }
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

    /// Optional parameter function
    public static synchronized void syncData(ProcessCompleteCallback syncCompleteCallback){
        final ArrayList<Tasks> tasksDbList  = (ArrayList<Tasks>)DbClient.getInstance().tasksDao().getNotSyncData();

        if(!tasksDbList.isEmpty()){
            AppUtils.showProgress(Application.context,"Sync data...");
            for (int i = 0;i< tasksDbList.size() ; i++) {
                final Tasks t = tasksDbList.get(i);

                if(!t.isSync) {
                    int finalI = i;
                    createTaskFromSync(t, (error) -> {
                        /// is Last
                        if(tasksDbList.size() - 1 == finalI){
                            AppUtils.dismissProgress();
                            syncCompleteCallback.onComplete(error);
                        }
                    });
                }else if(!t.isCheckInSync){
                    int finalI = i;
                    checkInTaskFromSync(t, (error) -> {
                        /// is Last
                        if(tasksDbList.size() - 1 == finalI){
                            AppUtils.dismissProgress();
                            syncCompleteCallback.onComplete(error);
                        }
                    });
                }else if(!t.isCheckOutSync){
                    int finalI = i;
                    checkOutTaskFromSync(t, (error) -> {
                        /// is Last
                        if(tasksDbList.size() - 1 == finalI){
                            AppUtils.dismissProgress();
                            syncCompleteCallback.onComplete(error);
                        }
                    }, 0);
                }
            }
        }else{
            syncCompleteCallback.onComplete(false);
        }
    }
    public static synchronized void syncData(){
        final ArrayList<Tasks> tasksDbList  = (ArrayList<Tasks>)DbClient.getInstance().tasksDao().getNotSyncData();

        if(!tasksDbList.isEmpty()){
            AppUtils.showProgress(Application.context,"Sync data...");
            for (int i = 0;i< tasksDbList.size() ; i++) {
                final Tasks t = tasksDbList.get(i);

                if(!t.isSync) {
                    int finalI = i;
                    createTaskFromSync(t, (error) -> {
                        /// is Last
                        if(tasksDbList.size() - 1 == finalI){
                            AppUtils.dismissProgress();
                        }
                    });
                }else if(!t.isCheckInSync){
                    int finalI = i;
                    checkInTaskFromSync(t, (error) -> {
                        /// is Last
                        if(tasksDbList.size() - 1 == finalI){
                            AppUtils.dismissProgress();
                        }
                    });
                }else if(!t.isCheckOutSync){
                    int finalI = i;
                    checkOutTaskFromSync(t, (error) -> {
                        /// is Last
                        if(tasksDbList.size() - 1 == finalI){
                            AppUtils.dismissProgress();
                        }
                    }, 0);
                }
            }
        }
    }

    @SuppressLint("LongLogTag")
    private static synchronized void createTaskFromSync(Tasks t, ProcessCompleteCallback processCompleteCallback){
        try{
            String apiUrl = Application.context.getString(R.string.api_create_task);

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    t.isSync = true;
                    DbClient.getInstance().tasksDao().update(t);

                    if(!t.isCheckInSync){
                        checkInTaskFromSync(t,processCompleteCallback);
                    }else if(!t.isCheckOutSync){
                        checkOutTaskFromSync(t,processCompleteCallback, 0);
                    }else{
                        processCompleteCallback.onComplete(false);
                    }
                }

                @Override
                public void notifyError(VolleyError error) {
                    Log.e("Error on create task",error.toString());
                    processCompleteCallback.onComplete(true);
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    Log.e("Error on create task",response.toString());
                    processCompleteCallback.onComplete(false);
                }
            };

            final VolleyService mVolleyService = new VolleyService(callback, Application.context);
            mVolleyService.postDataVolley(apiUrl, t.createTaskJson());
        }catch (Exception e){
            Log.e("Error on api create task",e.getMessage());
            processCompleteCallback.onComplete(true);
        }
    }

    private static synchronized void checkInTaskFromSync(Tasks t,ProcessCompleteCallback ProcessCompleteCallback){
        try{

            String api = Application.context.getString(R.string.api_check_in);
            api += "/" + t.name;

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    /// set data local
                    t.isCheckInSync = true;
                    DbClient.getInstance().tasksDao().update(t);

                    if(!t.isCheckOutSync){
                        checkOutTaskFromSync(t,ProcessCompleteCallback, 0);
                    }else{
                        ProcessCompleteCallback.onComplete(false);
                    }
                }

                @Override
                public void notifyError(VolleyError error) {
                    Log.e("Error on check in",error.toString());
                    ProcessCompleteCallback.onComplete(true);
                }

                @SuppressLint("LongLogTag")
                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    Log.e("Error on check in parse response",response.toString());
                    ProcessCompleteCallback.onComplete(false);
                }
            };

            final VolleyService volleyService = new VolleyService(callback,Application.context);
            volleyService.putDataVolley(api,t.checkInJson());

        }catch (Exception e){
            Log.e("Error on api check in",e.getMessage());
            ProcessCompleteCallback.onComplete(true);
        }
    }

    public static synchronized void checkOutTaskFromSync(Tasks t, ProcessCompleteCallback ProcessCompleteCallback, int imgPos){
        try{
            String api = Application.context.getString(R.string.api_check_out);
            String[] separated = t.images.split(",");
            if (separated.length > 0 && imgPos <= separated.length - 1) {
                uploadImage(t, separated[imgPos], imgPos, ProcessCompleteCallback);
            } else {
                api += "/" + t.name;

                final APIVInterface callback = new APIVInterface() {
                    @Override
                    public void notifySuccess(JSONObject response) {
                        /// set data local
                        t.isCheckOutSync = true;

                        DbClient.getInstance().tasksDao().update(t);
                        ProcessCompleteCallback.onComplete(false);
                    }

                    @Override
                    public void notifyError(VolleyError error) {
                        Log.e("Check Out Error", error.toString());
                        ProcessCompleteCallback.onComplete(true);
                    }

                    @Override
                    public void notifyNetworkParseResponse(NetworkResponse response) {
                        Log.e("Check out Error", response.toString());
                        ProcessCompleteCallback.onComplete(false);
                    }
                };

                final VolleyService volleyService = new VolleyService(callback, Application.context);
                volleyService.putDataVolley(api, t.checkOutJson());
            }
        }catch (Exception e){
            Log.e("Error on api check out",e.getMessage());
            ProcessCompleteCallback.onComplete(true);
        }
    }

    public static synchronized void  uploadImage(Tasks t, String img, int pos, ProcessCompleteCallback ProcessCompleteCallback) {
        try{
            String api = Application.context.getString(R.string.api_upload);

            final APIVInterface callback = new APIVInterface() {
                @Override
                public void notifySuccess(JSONObject response) {
                    ProcessCompleteCallback.onComplete(false);
                    checkOutTaskFromSync(t, ProcessCompleteCallback, pos+1);
                }

                @Override
                public void notifyError(VolleyError error) {
                    Log.e("Check Out Error",error.toString());
                    ProcessCompleteCallback.onComplete(true);
                }

                @Override
                public void notifyNetworkParseResponse(NetworkResponse response) {
                    Log.e("Check out Error",response.toString());
                    ProcessCompleteCallback.onComplete(false);
                }
            };

            final VolleyService volleyService = new VolleyService(callback,Application.context);

            final HashMap<String, Object> map = new HashMap<>();
            map.put("filename", System.currentTimeMillis() / 1000 + ".jpg");
            map.put("filedata", img);
            map.put("from_form", 1);
            map.put("doctype", "Visit Request");
            map.put("app_auth_key", "536aa3df73a76dcdf6c64a3919f4a0d3");
            map.put("pass_token", Application.getUserModel().token);
            map.put("usr", Application.getUserModel().userName);
            map.put("docname", t.name);

            volleyService.putDataVolley(api, new JSONObject(map));
        }catch (Exception e){
            Log.e("Error on api check out",e.getMessage());
            ProcessCompleteCallback.onComplete(true);
        }
    }
}