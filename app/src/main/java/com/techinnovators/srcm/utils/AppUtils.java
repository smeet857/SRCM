package com.techinnovators.srcm.utils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonArray;
import com.techinnovators.srcm.Application;
import com.techinnovators.srcm.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class AppUtils {
    private static ProgressDialog progressDialog;

    public static void showProgress(Context context,String message){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMax(100);
        progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    public static void dismissProgress(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    public static void showSnackBar(Context fContext, View foView, String fsMsg) {
        Snackbar snackbar = Snackbar.make(foView, Html.fromHtml("<font color='#ffffff' >" +
                fsMsg + "</font>"), BaseTransientBottomBar.LENGTH_LONG);
        View viewX = snackbar.getView();
        if (viewX.getBackground() != null) {
            viewX.setBackgroundColor(fContext.getResources().getColor(R.color.rosegold));
        }

        snackbar.show();
    }

    public static void displayAlertMessage(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(
                context.getString(R.string.OKAY),
                (dialog, id) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        }
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = ((Activity) activity).getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String dispCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        month = month + 1;
        String lsMonth;
        String lsDay;
        if (month <= 9) {
            lsMonth = "0" + month;
        } else {
            lsMonth = String.valueOf(month);
        }
        if (day <= 9) {
            lsDay = "0" + day;
        } else {
            lsDay = String.valueOf(day);
        }
        return year + "-" + lsMonth + "-" + lsDay;
    }
    public static String dispCurrentDateFirst() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        month = month + 1;
        String lsMonth;
        String lsDay;
        if (month <= 9) {
            lsMonth = "0" + month;
        } else {
            lsMonth = String.valueOf(month);
        }
        if (day <= 9) {
            lsDay = "0" + day;
        } else {
            lsDay = String.valueOf(day);
        }
        return lsDay + "-" + lsMonth + "-" + year;
    }

    public static String getCurrentTime() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        int secs = mcurrentTime.get(Calendar.SECOND);
        String strHour = "", strMin = "", strSecs = "";
        if (hour <= 9) {
            strHour = "0" + hour;
        } else {
            strHour = String.valueOf(hour);
        }
        if (minute <= 9) {
            strMin = "0" + minute;
        } else {
            strMin = String.valueOf(minute);

        }
        if (secs <= 9) {
            strSecs = "0" + secs;
        } else {
            strSecs = String.valueOf(secs);
        }
        return strHour + ":" + strMin + ":" + strSecs;
    }

    public static void setDate(Context context, EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String lsDateDisp;
                String lsMonth = "";
                String lsDay = "";
                if (month <= 9) {
                    lsMonth = "0" + month;
                } else {
                    lsMonth = String.valueOf(month);
                }
                if (dayOfMonth <= 9) {
                    lsDay = "0" + dayOfMonth;
                } else {
                    lsDay = String.valueOf(dayOfMonth);
                }
                lsDateDisp = lsDay + "-" + lsMonth + "-" + year;
                editText.setText(lsDateDisp);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    public static ArrayList<String> stringToArray(String text) {
        if(text.isEmpty()){
            return new ArrayList<>();
        }else{
            text = text.replaceAll("\\[","");
            text = text.replaceAll("]","");

            final String[] str = text.split(",");

            return new ArrayList<>(Arrays.asList(str));
        }
    }

    public static ArrayList<String> jsonArrayStringToStringArray(String text) {
        ArrayList<String> strArray = new ArrayList<>();

        if(text.isEmpty()){
            return strArray;
        }else{
            try {
                JSONArray jsonArray = new JSONArray(text);

                for(int i = 0; i< jsonArray.length(); i++){
                    strArray.add(jsonArray.getString(i));
                }

                return strArray;
            } catch (JSONException e) {
                e.printStackTrace();
                return strArray;
            }
        }
    }
}
