package com.techinnovators.srcm.models;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventCategory {
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("types")
    @Expose
    private ArrayList<String> types;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }
}