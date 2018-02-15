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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


public class BackgroundTaskMostraRider extends AsyncTask<Object, Object, ArrayList> {


    Context ctx;
    String test,JSON_STRING;
    ArrayList esito = new ArrayList();

    String autisti_url = "http://yasts.altervista.org/rider.php";



    public BackgroundTaskMostraRider(Context ctx){
        this.ctx = ctx;

    }

    protected void onPreExecute() {
    }

    @Override
    protected ArrayList doInBackground(Object... voids) {

        try {


            URL url = new URL(autisti_url);
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

            esito.add(stringBuilder.toString());


            return esito;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    public void onPostExecute(ArrayList result) {

    }
}
