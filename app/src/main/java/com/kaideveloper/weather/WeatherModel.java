package com.kaideveloper.weather;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.kaideveloper.weather.common.WeatherTable;
import com.kaideveloper.weather.database.DbHelper;
import com.kaideveloper.weather.model.Weather;
import com.kaideveloper.weather.utils.JSONWeatherParser;

import org.json.JSONException;

public class WeatherModel {
    private final DbHelper dbHelper;

    public WeatherModel(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void loadWeather(LoadWeatherCallback callback) {
        LoadWeatherTask loadWeatherTask = new LoadWeatherTask(callback);
        loadWeatherTask.execute();
    }

    public void addWeather(ContentValues contentValues, CompleteCallback callback) {
        AddWeatherTask addWeatherTask = new AddWeatherTask(callback);
        addWeatherTask.execute(contentValues);
    }

    public void clearWeather(CompleteCallback completeCallback) {
        ClearWeatherTask clearWeatherTask = new ClearWeatherTask(completeCallback);
        clearWeatherTask.execute();
    }


    interface LoadWeatherCallback {
        void onLoad(Weather weather);
    }

    interface CompleteCallback {
        void onComplete();
    }

    class LoadWeatherTask extends AsyncTask<Void, Void, Weather> {

        private final LoadWeatherCallback callback;

        LoadWeatherTask(LoadWeatherCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Weather doInBackground(Void... params) {
            Weather weather = new Weather();
            Cursor cursor = dbHelper.getReadableDatabase().query(WeatherTable.TABLE, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                try {
                    weather = JSONWeatherParser.getWeather(cursor.getString(cursor.getColumnIndex(WeatherTable.COLUMN.VALUE)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            if (callback != null) {
                callback.onLoad(weather);
            }
        }
    }

    class AddWeatherTask extends AsyncTask<ContentValues, Void, Void> {

        private final CompleteCallback callback;

        AddWeatherTask(CompleteCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(ContentValues... params) {
            ContentValues cvWeather = params[0];
            Cursor cursor = dbHelper.getReadableDatabase().query(WeatherTable.TABLE, null,
                    null, null, null, null, null);
            if (cursor.moveToFirst())
                dbHelper.getWritableDatabase().update(WeatherTable.TABLE, cvWeather,
                        "_id = ? ",
                        new String[] {Integer.toString(Integer.parseInt(cursor.getString(cursor.getColumnIndex(WeatherTable.COLUMN.ID))))});
            else
                dbHelper.getWritableDatabase().insert(WeatherTable.TABLE, null, cvWeather);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (callback != null) {
                callback.onComplete();
            }
        }
    }

    class ClearWeatherTask extends AsyncTask<Void, Void, Void> {

        private final CompleteCallback callback;

        ClearWeatherTask(CompleteCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            dbHelper.getWritableDatabase().delete(WeatherTable.TABLE, null, null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (callback != null) {
                callback.onComplete();
            }
        }
    }
}
