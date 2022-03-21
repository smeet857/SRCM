package com.techinnovators.srcm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TalukaResponse {
    @SerializedName("data")
    @Expose
    private List<Taluka> data = null;

    public List<Taluka> getData() {
        return data;
    }

    public void setData(List<Taluka> data) {
        this.data = data;
    }
}
