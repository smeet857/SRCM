package com.techinnovators.srcm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VisitLocation {
    //    "name\",\"taluka
    @SerializedName("taluka")
    @Expose
    public String taluka = "";

    public String getTaluka() {
        return taluka == null ? "" : taluka;
    }

    public void setTaluka(String state) {
        this.taluka = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SerializedName("name")
    @Expose
    String name = "";
}
