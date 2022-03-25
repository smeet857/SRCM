package com.techinnovators.srcm.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "User")
public class UserModel {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @SerializedName("user_name")
    @ColumnInfo(name = "user_name")
    @Expose
    public String userName = "";

    @SerializedName("home_page")
    @ColumnInfo(name = "home_page")
    @Expose
    public String homePage = "";

    @SerializedName("message")
    @ColumnInfo(name = "message")
    @Expose
    public String message = "";

    @SerializedName("full_name")
    @ColumnInfo(name = "full_name")
    @Expose
    public String fullName = "";
}
