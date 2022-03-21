package com.techinnovators.srcm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Taluka {
    @SerializedName("district")
    @Expose
    String district;

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String state) {
        this.district = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SerializedName("name")
    @Expose
    String name;
}
