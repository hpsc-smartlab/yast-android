package com.example.mauro.yasts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.mauro.yasts_test.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.ExecutionException;

public class driver extends AppCompatActivity {

    private String username;
    private boolean checkConn, checkGPS;
    private static final String MyPREFERENCES = "MyPrefs" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentDriver second = new fragmentDriver();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragmentPasseggero,second).commit();


        username = getIntent().getExtras().getString("username").trim();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.piggy);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConn = checkConnessione();
                if (!checkConn) {
                    Toast.makeText(driver.this, "Connessione assente", Toast.LENGTH_LONG).show();
                }
                else {
                    username = getIntent().getExtras().getString("username").trim();
                    BackgroundTaskBitWheels ruotini = new BackgroundTaskBitWheels(driver.this);
                    String user = null;
                    try {
                        user = ruotini.execute(username.trim()).get();
                        JSONObject jsonObject = new JSONObject(user);
                        JSONArray jsonArray = jsonObject.getJSONArray("server_response");
                        JSONObject jo = jsonArray.getJSONObject(0);
                        String message = jo.getString("message");
                        Snackbar.make(view, "Hai: " + message + " BitWheels a disposizione", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab1.setImageResource(R.drawable.change);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConn = checkConnessione();
                if (!checkConn) {
                    Toast.makeText(driver.this, "Connessione assente", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent(driver.this, schedaUtente.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                }
            }
        });


        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab2.setImageResource(R.drawable.gift);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConn = checkConnessione();
                if (!checkConn) {
                    Toast.makeText(driver.this, "Connessione assente", Toast.LENGTH_LONG).show();
                }
                else {
                    Random rand = new Random();
                    int n = rand.nextInt(20) + 5;
                    String nS;
                    nS = String.valueOf(n);
                    username = getIntent().getExtras().getString("username").trim();
                    BackgroundTaskRegalo regalo = new BackgroundTaskRegalo(driver.this);
                    try {
                        String esito = regalo.execute(username, nS).get();
                        JSONObject jsonObject = new JSONObject(esito);
                        JSONArray jsonArray = jsonObject.getJSONArray("server_response");
                        JSONObject jo = jsonArray.getJSONObject(0);
                        String message = jo.getString("message");
                        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


    }

    public boolean checkConnessione() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Toast.makeText(driver.this, "Connessione network assente", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }




    @Override
    public void onBackPressed() {
        // Write your code here
        AlertDialog.Builder miaAlert = new AlertDialog.Builder(this);
        miaAlert.setMessage("Vuoi davvero uscire da Yasts?");
        miaAlert.setTitle("Yasts");
        miaAlert.setCancelable(false);
        miaAlert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                BackgroundTaskLogout logout = new BackgroundTaskLogout(driver.this);
                logout.execute(username, "0");
                try {
                    Thread.sleep(100);
                    Intent intent = new Intent(driver.this,Splash.class);
                    startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                android.os.Process.killProcess(android.os.Process.myPid());

                finish();
            }

        });
        miaAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });AlertDialog alert = miaAlert.create();
        alert.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.item1:
                AlertDialog.Builder miaAlert = new AlertDialog.Builder(this);
                miaAlert.setMessage("Vuoi davvero effettuare il logout da Yasts?\nCiò eliminerà la sessione in corso!");
                miaAlert.setTitle("Yasts");
                miaAlert.setCancelable(false);
                miaAlert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BackgroundTaskLogout logout = new BackgroundTaskLogout(driver.this);
                        logout.execute(username,"0");
                        Toast.makeText(driver.this,"Alla prossima",Toast.LENGTH_LONG).show();
                        try {
                            Thread.sleep(100);
                            SharedPreferences preferences = getSharedPreferences(MyPREFERENCES,
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();

                            editor.clear();
                            editor.commit();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        android.os.Process.killProcess(android.os.Process.myPid());

                        finish();
                    }
                });
                miaAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog alert = miaAlert.create();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




}
