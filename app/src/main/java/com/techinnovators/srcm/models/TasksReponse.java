package com.techinnovators.srcm.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.techinnovators.srcm.models.Tasks;

import java.util.List;

public class TasksReponse {

    @SerializedName("data")
    @Expose
    private List<Tasks> data = null;

    public List<Tasks> getData() {
        return data;
    }

    public void setData(List<Tasks> data) {
        this.data = data;
    }


}
