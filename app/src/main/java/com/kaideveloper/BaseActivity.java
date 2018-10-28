package com.kaideveloper;

import android.support.v7.app.AppCompatActivity;

import com.kaideveloper.weather.App;

public class BaseActivity extends AppCompatActivity {

    public App getApp() {
        return (App) getApplication();
    }

}
