package com.example.mauro.yasts;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static android.content.ContentValues.TAG;


public class BackgroundTaskUpdateCorsa extends AsyncTask<String, Void, String> {


    String register_url = "http://yasts.altervista.org/updateValueCorsa.php";
    Context ctx;
    Activity activity;


    public BackgroundTaskUpdateCorsa(Context ctx){
        this.ctx = ctx;
        activity = (Activity) ctx;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... params) {

        String esitoCorsa = params[0];
        String usernamePasseggero = params[1];
        String usernameAutista = params[2];
        String costoCorsa = params[3];

        try {
            URL url = new URL(register_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

            String data =
                    URLEncoder.encode("esito","UTF-8")+"="+URLEncoder.encode(esitoCorsa,"UTF-8")+"&"+
                            URLEncoder.encode("usernamePasseggero","UTF-8")+"="+URLEncoder.encode(usernamePasseggero,"UTF-8")+"&"+
                            URLEncoder.encode("usernameAutista","UTF-8")+"="+URLEncoder.encode(usernameAutista,"UTF-8")+"&"+
                            URLEncoder.encode("costoCorsa","UTF-8")+"="+URLEncoder.encode(costoCorsa,"UTF-8");

            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();

            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while ( (line=bufferedReader.readLine()) != null ) {
                stringBuilder.append(line+"\n");

            }

            httpURLConnection.disconnect();
            return stringBuilder.toString().trim();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String json) {


    }

}
