package com.example.mauro.yasts;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mauro.yasts_test.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private EditText emailEdit,passwordEdit;
    private Button login,registrati,web;
    private String emailS,passwordS;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView coord;
    private CheckBox automunito;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String automunitoShared = "automunitoShared";
    public static final String usernameShared = "usernameShared";
    private SharedPreferences sharedpreferences;

    private boolean gps,conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button) findViewById(R.id.button_login);
        registrati = (Button) findViewById(R.id.button_registrati);
        emailEdit = (EditText) findViewById(R.id.emailEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        coord = (TextView) findViewById(R.id.textView);
        automunito = (CheckBox) findViewById(R.id.checkBox);
        web = (Button) findViewById(R.id.button_web);
        infoCreazioneApp();



        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        conn = checkGPS();
        gps = checkConnessione();
        if ( !gps || !conn ){
            login.setEnabled(false);
            registrati.setEnabled(false);
            new AlertDialog.Builder(MainActivity.this)
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
        coordinateGPS();


        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://yasts.altervista.org/cms/login.php"));
                startActivity(browserIntent);
            }
        });

        String automunitoS = sharedpreferences.getString(automunitoShared,null);
        String username = sharedpreferences.getString(usernameShared, null);
        if ( username != null && automunitoS.contains("1") ){
            Intent intent = new Intent(MainActivity.this, driver.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
        if ( username != null && automunitoS.equals("0") ){
            Intent intent = new Intent(MainActivity.this, rider.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }

        login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = sharedpreferences.edit();
                emailS = emailEdit.getText().toString();
                passwordS = passwordEdit.getText().toString();

                if ( emailS.matches("") || passwordS.matches("") )
                    Toast.makeText(MainActivity.this,"I campi non possono esser vuoti",Toast.LENGTH_LONG).show();
                else{



                    if (automunito.isChecked()) {
                        BackgroundTaskLogin backgroundTaskLogin = new BackgroundTaskLogin(MainActivity.this);
                        try {
                            String esito = backgroundTaskLogin.execute(emailS, passwordS, "1").get();
                            JSONObject jsonObject = new JSONObject(esito);
                            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
                            JSONObject jo = jsonArray.getJSONObject(0);
                            String code = jo.getString("code");
                            String message = jo.getString("message");
                            String username = message.replace("Benvenuto:","");



                            if (code.equals("noDriver")){
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Attenzione")
                                        .setMessage("L'accesso a questa modalità è riservata solo a coloro che hanno caricato un documento valido come la patente!")
                                        .setPositiveButton("Fallo subito", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://yasts.altervista.org/cms/login.php"));
                                                startActivity(browserIntent);
                                            }
                                        })
                                        .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .show();
                            }

                            else if (code.equals("noDriverTarga")){
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Attenzione")
                                        .setMessage("Sembra che tu abbia caricato la patente, ma devi ancora inserire la targa della tua vettura")
                                        .setPositiveButton("Fallo subito", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://yasts.altervista.org/cms/login.php"));
                                                startActivity(browserIntent);
                                            }
                                        })
                                        .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                        .show();
                            }

                            else if (code.equals("login_false"))
                                Toast.makeText(MainActivity.this, "Username e/o password errate!", Toast.LENGTH_LONG).show();

                            else {

                                editor.putString(automunitoShared,"1");
                                editor.putString(usernameShared, username);
                                editor.apply();

                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, driver.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("username", username);
                                startActivity(intent);

                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        BackgroundTaskLogin backgroundTaskLogin = new BackgroundTaskLogin(MainActivity.this);
                        try {
                            String esito = backgroundTaskLogin.execute(emailS, passwordS, "0").get();
                            JSONObject jsonObject = new JSONObject(esito);
                            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
                            JSONObject jo = jsonArray.getJSONObject(0);
                            String code = jo.getString("code");
                            String message = jo.getString("message");

                            String username = message.replace("Benvenuto:","");

                            if (code.equals("noUser")){
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Attenzione")
                                        .setMessage("L'accesso a questa modalità è riservata solo a coloro che hanno caricato un documento di riconoscimento!")
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                            }
                                        })
                                        .show();
                            }

                            else if (code.equals("login_false"))
                                Toast.makeText(MainActivity.this, "Username e/o password errate!", Toast.LENGTH_LONG).show();
                            else {

                                editor.putString(automunitoShared,"0");
                                editor.putString(usernameShared, username);
                                editor.apply();

                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(MainActivity.this, rider.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("username", username);
                                startActivity(intent);

                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

       registrati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,RegistraUtente.class);
                startActivity(intent);
            }
        });

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

    public void coordinateGPS() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                coord.setText("Lat: "+location.getLatitude());
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
        if ( locationManager == null ){
            finish();
        }
        if ( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null ){
            locationManager.requestLocationUpdates("gps", 0, 0, locationListener);
        }
        else{
            locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);


        }
    }

    @Override
    public void onBackPressed(){
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onBackPressed();
    }

    public void infoCreazioneApp(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Info")
                .setMessage("Applicazione sviluppata nell’ambito della tesi di laurea triennale in informatica “YASTs: Yet Another Shared Transportation System” presso l’HPSC/Smart-Lab del Dipartimento di Scienze e Tecnologie dell’Università degli Studi di Napoli Parthenope.\nCandidato Mauro Spezzaferro (0124/733), Relatore Raffaele Montella, PhD.\n" +
                "http://yast.uniparthenope.it").setPositiveButton("OK",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }


}
