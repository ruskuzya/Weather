package com.kaideveloper.weather;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.kaideveloper.BaseActivity;
import com.kaideveloper.weather.database.DbHelper;
import com.kaideveloper.weather.model.Weather;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int MY_PERMISSION_ACCESS_LOCATION = 1;
    private EditText editTextCity;
    private TextView cityText, condDescr, temp, hum, press, windSpeed, windDeg;
    private ProgressDialog progressDialog;

    private WeatherPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init() {

        editTextCity = findViewById(R.id.city);
        cityText = findViewById(R.id.cityText);
        condDescr = findViewById(R.id.condDescr);
        temp = findViewById(R.id.temp);
        hum = findViewById(R.id.hum);
        press = findViewById(R.id.press);
        windSpeed = findViewById(R.id.windSpeed);
        windDeg = findViewById(R.id.windDeg);

        DbHelper dbHelper = new DbHelper(this);
        WeatherModel weatherModel = new WeatherModel(dbHelper);
        presenter = new WeatherPresenter(weatherModel);
        presenter.attachView(this);
        presenter.viewIsReady();

        findViewById(R.id.rbLocation).setOnClickListener(this);
        findViewById(R.id.rbCity).setOnClickListener(this);
        findViewById(R.id.btnCityFind).setOnClickListener(this);

        boolean fineLocationGranted = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                fineLocationGranted = false;
                ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                        MY_PERMISSION_ACCESS_LOCATION );
            }

        }
        if (getApp().isLocation() && fineLocationGranted) {
            (findViewById(R.id.rbLocation)).performClick();
        } else {
            editTextCity.setText(getApp().getCity());
            (findViewById(R.id.rbCity)).performClick();
            presenter.updateWeather(getApp().getCity());
        }
    }

    public void showWeather(Weather weather) {
        if (weather == null || weather.location == null) {
            showMessage(getString(R.string.find_error));
            return;
        }
        cityText.setText(weather.location.getCity() + "," + weather.location.getCountry());
        condDescr.setText(weather.currentCondition.getCondition() + "(" + weather.currentCondition.getDescr() + ")");
        temp.setText("" + Math.round((weather.temperature.getTemp() - 273.15)) + " °C");
        hum.setText("" + weather.currentCondition.getHumidity() + "%");
        press.setText("" + weather.currentCondition.getPressure() + " hPa");
        windSpeed.setText("" + weather.wind.getSpeed() + " mps");
        windDeg.setText("" + weather.wind.getDeg() + "°");
    }

    public void showProgress() {
        progressDialog = ProgressDialog.show(this, "", getString(R.string.please_wait));
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rbLocation:
                getApp().setIsLocation(true);
                (findViewById(R.id.cityLayout)).setVisibility(View.GONE);
                presenter.requestLocation(this);
                break;
            case R.id.rbCity:
                getApp().setIsLocation(false);
                (findViewById(R.id.cityLayout)).setVisibility(View.VISIBLE);
                presenter.stopLocation();
                break;
            case R.id.btnCityFind:
                getApp().setCity(editTextCity.getText().toString());
                presenter.updateWeather(editTextCity.getText().toString());
                break;
        }
    }

    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_ACCESS_LOCATION) {
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                (findViewById(R.id.rbLocation)).performClick();
            } else {
                showMessage(getString(R.string.location_block));
            }
        }
    }
}
