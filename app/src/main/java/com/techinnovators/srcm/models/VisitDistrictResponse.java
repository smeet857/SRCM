package com.techinnovators.srcm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VisitDistrictResponse {
    @SerializedName("data")
    @Expose
    private List<VisitDistrict> data = null;

    public List<VisitDistrict> getData() {
        return data;
    }

    public void setData(List<VisitDistrict> data) {
        this.data = data;
    }
}
