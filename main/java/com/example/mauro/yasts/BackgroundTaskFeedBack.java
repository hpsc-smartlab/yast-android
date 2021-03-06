package com.example.mauro.yasts;


import android.app.Activity;
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
import java.net.URLEncoder;


public class BackgroundTaskFeedBack extends AsyncTask<String, Void, String> {

    String register_url = "http://yasts.altervista.org/fineCorsa.php";
    Context ctx;
    Activity activity;

    BackgroundTaskFeedBack(Context ctx){
        this.ctx = ctx;
        activity = (Activity)ctx;
    }

    @Override
    protected String doInBackground(String... params) {

        String usernamePasseggero = params[0];
        String feedback = params[1];

        try {
            URL url = new URL(register_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

            String data = URLEncoder.encode("usernamePasseggero","UTF-8")+"="+URLEncoder.encode(usernamePasseggero,"UTF-8")+"&"+
                    URLEncoder.encode("feedback","UTF-8")+"="+URLEncoder.encode(feedback,"UTF-8");

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
}
