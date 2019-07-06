package com.example.enigmator.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.enigmator.activity.CategoriesActivity;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserEnigmator implements Serializable {
    private int id;
    private int rank;
    private String rang;
    private Date inscription_date;
    private String username;

    public static UserEnigmator getCurrentUser(Context context) {
        Gson gson = new Gson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String json = prefs.getString(CategoriesActivity.PREF_USER, null);
        return json == null ? null : gson.fromJson(json, UserEnigmator.class);
    }
}
