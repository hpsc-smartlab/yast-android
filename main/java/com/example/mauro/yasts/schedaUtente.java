package com.example.mauro.yasts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.mauro.yasts_test.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class schedaUtente extends AppCompatActivity {

    private Button button_cambiaPwd;
    private EditText password,passwordUtenteConf;
    private String passwordS,passwordUtenteConfS,username;
    private int pwdUguali;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheda_utente);

        button_cambiaPwd = (Button) findViewById(R.id.button_cambiaPwd);
        password = (EditText) findViewById(R.id.password);
        passwordUtenteConf = (EditText) findViewById(R.id.passwordUtenteConf);

        button_cambiaPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordS = password.getText().toString();
                passwordUtenteConfS = passwordUtenteConf.getText().toString();
                username = getIntent().getExtras().getString("username").trim();
                pwdUguali = passwordS.compareTo(passwordUtenteConfS);
                if ( pwdUguali == 0 && passwordS.length() > 0 && passwordUtenteConfS.length() > 0 ){
                    BackgroundTaskUpdatePassword backgroundTaskUpdatePassword = new BackgroundTaskUpdatePassword(schedaUtente.this);
                    String esito = null;
                    try {
                        esito = backgroundTaskUpdatePassword.execute(username,passwordS).get();
                        JSONObject jsonObject = new JSONObject(esito);
                        JSONArray jsonArray = jsonObject.getJSONArray("server_response");
                        JSONObject jo = jsonArray.getJSONObject(0);
                        String code = jo.getString("code");
                        String message = jo.getString("message");

                        if (code.equals("pwd_ok"))
                            Toast.makeText(schedaUtente.this, "La password è stata modificata :)", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(schedaUtente.this,"Oops qualcosa è andato storto!",Toast.LENGTH_LONG).show();
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
