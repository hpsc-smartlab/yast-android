package com.example.mauro.yasts;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mauro.yasts_test.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistraUtente extends AppCompatActivity {

    private EditText emailUtente,passwordUtente,confPasswordUtente;
    private ImageView registrati;
    private String nomeUtenteS,emailUtenteS,passwordUtenteS,confPasswordUtenteS;
    private static Pattern emailNamePtrn = Pattern.compile(
            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registra_utente);


        registrati = (ImageView) findViewById(R.id.registrati);
        emailUtente = (EditText) findViewById(R.id.emailUtente);
        passwordUtente = (EditText) findViewById(R.id.passwordUtenteConf);
        confPasswordUtente = (EditText) findViewById(R.id.confPasswordUtente);

        registrati.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nomeUtenteS = emailUtente.getText().toString().trim();
                emailUtenteS = emailUtente.getText().toString().trim();
                passwordUtenteS = passwordUtente.getText().toString().trim();
                confPasswordUtenteS = confPasswordUtente.getText().toString().trim();


                if ( emailUtenteS.matches("")  )
                    Toast.makeText(RegistraUtente.this,"Il campo mail non può essere vuoto",Toast.LENGTH_LONG).show();
                Matcher match = emailNamePtrn.matcher(emailUtenteS);
                if ( !match.matches() )
                    Toast.makeText(RegistraUtente.this,"Inserisci un'email valida",Toast.LENGTH_LONG).show();
                else {
                   BackgroundTaskRegistraNuovoUtente backgroundTaskRegistraNuovoUtente = new BackgroundTaskRegistraNuovoUtente(RegistraUtente.this);
                        try {
                            String esito = backgroundTaskRegistraNuovoUtente.execute(nomeUtenteS, emailUtenteS, passwordUtenteS).get();
                            JSONObject jsonObject = new JSONObject(esito);
                            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
                            JSONObject jo = jsonArray.getJSONObject(0);
                            String code = jo.getString("code");
                            String message = jo.getString("message");

                            if (code.equals("reg_false"))
                                Toast.makeText(RegistraUtente.this, message, Toast.LENGTH_LONG).show();
                            else {
                                   new AlertDialog.Builder(RegistraUtente.this)
                                        .setTitle(message)
                                        .setMessage("Ti è stata inviata un'email all'indirizzo inserito.\nCompleta la registrazione inserendo un documento di riconoscimento valido!")
                                        .setPositiveButton("Completa la registrazione", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String encodeStringEmail = Base64.encodeToString(emailUtenteS.toString().getBytes(),Base64.DEFAULT);
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://yasts.altervista.org/new_user.php?a="+encodeStringEmail));
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
}
