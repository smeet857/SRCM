package com.techinnovators.srcm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VisitLocationResponse {
    @SerializedName("data")
    @Expose
    private List<VisitLocation> data = null;

    public List<VisitLocation> getData() {
        return data;
    }

    public void setData(List<VisitLocation> data) {
        this.data = data;
    }
}
