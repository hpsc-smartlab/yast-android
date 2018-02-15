package com.example.mauro.yasts;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

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

public class BackgroundTaskRegistraPosRider extends AsyncTask<String, Void, String> {


    String register_url = "http://yasts.altervista.org/registerRider.php";
    Context ctx;
    Activity activity;

    AlertDialog.Builder builder;

    public BackgroundTaskRegistraPosRider(Context ctx){
        this.ctx = ctx;
        activity = (Activity) ctx;
    }

    @Override
    protected void onPreExecute() {
        builder = new AlertDialog.Builder(ctx);
    }

    @Override
    protected String doInBackground(String... params) {

        String lat = params[0];
        String lng = params[1];
        String indirizzo = params[2];
        String username = params[3];
        String ruotini = params[4];

        try {
            URL url = new URL(register_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

            String data =
                    URLEncoder.encode("lng","UTF-8")+"="+URLEncoder.encode(lng,"UTF-8")+"&"+
                            URLEncoder.encode("lat","UTF-8")+"="+URLEncoder.encode(lat,"UTF-8")+"&"+
                            URLEncoder.encode("indirizzo","UTF-8")+"="+URLEncoder.encode(indirizzo,"UTF-8")+"&"+
                            URLEncoder.encode("username","UTF-8")+"="+URLEncoder.encode(username,"UTF-8")+"&"+
                            URLEncoder.encode("ruotini","UTF-8")+"="+URLEncoder.encode(ruotini,"UTF-8");

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

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
            JSONObject jo = jsonArray.getJSONObject(0);
            String code = jo.getString("code");
            String message = jo.getString("message");

            if ( code.equals("reg_true") ){
                //Toast.makeText(ctx,"Posizione registrata",Toast.LENGTH_SHORT).show();
                //showDialog("Registrazione avvenuta",message,code);
            }
            else if(code.equals("reg_false") ){
                showDialog("Registrazione fallita",message,code);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,"Errore");
        }
    }

    public void showDialog(String title, String message, String code){
        builder.setTitle(title);
        if ( code.equals("reg_true") || code.equals("reg_false") ){
            builder.setMessage(message);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
}
