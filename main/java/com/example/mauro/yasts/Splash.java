package com.example.mauro.yasts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.mauro.yasts_test.R;

import java.io.IOException;

public class Splash extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 1500;
    private LocationManager locationManager;
    private ImageButton next;
    private ProgressBar progressBar2;

    LocationListener locationListener;
    Criteria criteria;
    double lat, lng;
    String bestProvider;

    boolean gps,conn;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splashscreen);
        next = (ImageButton) findViewById(R.id.next);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);

        try {
            coordinateGPS();
        } catch (IOException e) {
            e.printStackTrace();
        }


        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                progressBar2.setVisibility(View.VISIBLE);
                gps = checkGPS();
                conn = checkConnessione();
                if (!gps || !conn) {

                    new AlertDialog.Builder(Splash.this)
                            .setTitle("Oops :(")
                            .setMessage("GPS e/o rete dati assente")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .show();
                }
                else{
                    try {
                        Thread.sleep(1500);
                        progressBar2.setVisibility(View.INVISIBLE);
                        next.setVisibility(View.VISIBLE);
                        next.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(Splash.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

            }
        }, SPLASH_DISPLAY_LENGTH);

    }

    public boolean checkGPS() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Attenzione: devi attivare il gps!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public boolean checkConnessione() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Toast.makeText(getApplication(), "Connessione network assente", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }



    public void coordinateGPS() throws IOException {
        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);
        if ( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null ){
            locationManager.requestLocationUpdates(bestProvider, 10000, 0, locationListener);
            lat = 0 ;
            lng = 0 ;
        }
        else{
            locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);

            lat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
            lng = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();

        }
    }



}
