package com.kaideveloper.weather;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.kaideveloper.weather.common.WeatherTable;
import com.kaideveloper.weather.model.Weather;
import com.kaideveloper.weather.utils.JSONWeatherParser;
import com.kaideveloper.weather.utils.WeatherHttpClient;

import org.json.JSONException;

import static android.content.Context.LOCATION_SERVICE;

public class WeatherPresenter {
    private MainActivity view;
    private final WeatherModel model;
    private LocationManager mLocationManager;
    private Boolean isFindLocation = false;


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if (location.getAccuracy() <= 100 && !isFindLocation) {
                stopLocation();
                updateWeather(Double.toString(location.getLatitude()), Double.toString(location.getLongitude()));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    WeatherPresenter(WeatherModel model) {
        this.model = model;
    }

    void attachView(MainActivity mainActivity) {
        view = mainActivity;
    }

    void detachView() {
        view = null;
    }

    void viewIsReady() {
        loadWeather();
    }

    private void loadWeather() {
        model.loadWeather(new WeatherModel.LoadWeatherCallback() {
            @Override
            public void onLoad(Weather weather) {
                view.showWeather(weather);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void requestLocation(Context context) {
        view.showProgress();
        isFindLocation = false;
        mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String providerFine = mLocationManager.getBestProvider(criteria, true);

        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String providerCoarse = mLocationManager.getBestProvider(criteria, true);

        if (providerCoarse != null) {
            mLocationManager.requestLocationUpdates(providerCoarse, 0, 0, mLocationListener);
        }
        if (providerFine != null) {
            mLocationManager.requestLocationUpdates(providerFine, 0, 0, mLocationListener);
        }

    }

    public void stopLocation() {
        view.hideProgress();
        isFindLocation = true;
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    public void add(String weather) {
        if (TextUtils.isEmpty(weather)) {
            return;
        }

        ContentValues cv = new ContentValues(1);
        cv.put(WeatherTable.COLUMN.VALUE, weather);
        view.showProgress();
        model.addWeather(cv, new WeatherModel.CompleteCallback() {
            @Override
            public void onComplete() {
                view.hideProgress();
                loadWeather();
            }
        });
    }

    public void clear() {
        view.showProgress();
        model.clearWeather(new WeatherModel.CompleteCallback() {
            @Override
            public void onComplete() {
                view.hideProgress();
                loadWeather();
            }
        });
    }


    public void updateWeather(String location) {
        if (location == null || location.isEmpty())
            return;
        view.showProgress();
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute(location);
    }

    public void updateWeather(String latitude, String longitude) {
        view.showProgress();
        JSONWeatherTask task = new JSONWeatherTask();
        task.execute("lat="+latitude+"&lon="+longitude);
    }


    @SuppressLint("StaticFieldLeak")
    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... params) {
            Weather weather = new Weather();
            String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));

            if (data == null)
                return null;

            ContentValues cv = new ContentValues(1);
            cv.put(WeatherTable.COLUMN.VALUE, data);
            model.addWeather(cv, new WeatherModel.CompleteCallback() {
                @Override
                public void onComplete() {
                }
            });

            try {
                weather = JSONWeatherParser.getWeather(data);

                // Let's retrieve the icon
                weather.iconData = ( (new WeatherHttpClient()).getImage(weather.currentCondition.getIcon()));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return weather;

        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            view.hideProgress();
            view.showWeather(weather);
        }
    }
}
