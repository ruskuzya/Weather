package com.kaideveloper.weather;

import android.app.Application;
import android.content.SharedPreferences;

public class App extends Application {
    private static final String PREF_KEY = "app_pref";
    private static final String KEY_IS_LOCATION = "key_is_location";
    private static final String KEY_BY_CITY = "key_by_city";

    public App() {
        super();
    }

    public boolean isLocation() {
        SharedPreferences sp = getSharedPreferences(PREF_KEY, 0);
        return sp.getBoolean(KEY_IS_LOCATION, true);
    }

    public void setIsLocation(Boolean isLocation) {
        SharedPreferences sp = getSharedPreferences(PREF_KEY, 0);
        SharedPreferences.Editor spe = sp.edit();
        spe.putBoolean(KEY_IS_LOCATION, isLocation);
        spe.commit();
    }

    public String getCity() {
        SharedPreferences sp = getSharedPreferences(PREF_KEY, 0);
        return sp.getString(KEY_BY_CITY, "");
    }

    public void setCity(String city) {
        SharedPreferences sp = getSharedPreferences(PREF_KEY, 0);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(KEY_BY_CITY, city);
        spe.commit();
    }
}
