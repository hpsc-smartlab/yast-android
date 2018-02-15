package com.example.mauro.yasts;

import android.content.Context;
import android.os.AsyncTask;

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
import java.util.ArrayList;

public class BackgroundTaskMostraLuoghi extends AsyncTask<Object, Object, String> {

    Context ctx;
    String test,JSON_STRING;

    ArrayList esito = new ArrayList<String>();

    public BackgroundTaskMostraLuoghi(Context ctx){
        this.ctx = ctx;
    }


    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(Object... args) {

        Object partenza;
        partenza = args[0];


        try {
            StringBuilder sb = new StringBuilder();
            sb.append("https://maps.googleapis.com/maps/api/place/autocomplete/json?input="+partenza+"&key=AIzaSyA_6gNahUBS6enRZCfi_H74uEpUyObLBI8");

            test = sb.toString();

            URL url = new URL(test);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);


            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));


            bufferedWriter.flush();
            bufferedWriter.close();

            outputStream.close();

            InputStream inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder stringBuilder = new StringBuilder();

            while ((JSON_STRING = bufferedReader.readLine()) != null) {
                stringBuilder.append(JSON_STRING + "\n");
            }


            bufferedReader.close();
            inputStream.close();
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
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {

    }

}