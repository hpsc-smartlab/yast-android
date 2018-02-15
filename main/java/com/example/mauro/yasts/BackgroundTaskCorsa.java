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


public class BackgroundTaskCorsa extends AsyncTask<String, Void, String> {


    String register_url = "http://yasts.altervista.org/registerCorsa.php";
    Context ctx;
    Activity activity;

    AlertDialog.Builder builder;

    public BackgroundTaskCorsa(Context ctx){
        this.ctx = ctx;
        activity = (Activity) ctx;
    }

    @Override
    protected void onPreExecute() {
        builder = new AlertDialog.Builder(ctx);
    }

    @Override
    protected String doInBackground(String... params) {

        String indirizzoPasseggero = params[0];
        String indirizzoAutista = params[1];
        String destinazione = params[2];
        String usernamePasseggero = params[3].trim();
        String usernameAutista = params[4].trim();
        String costoCorsa = params[5].trim();
        String targa = params[6].trim();

        try {
            URL url = new URL(register_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));

            String data =
                    URLEncoder.encode("indirizzoPasseggero","UTF-8")+"="+URLEncoder.encode(indirizzoPasseggero,"UTF-8")+"&"+
                            URLEncoder.encode("indirizzoAutista","UTF-8")+"="+URLEncoder.encode(indirizzoAutista,"UTF-8")+"&"+
                            URLEncoder.encode("destinazione","UTF-8")+"="+URLEncoder.encode(destinazione,"UTF-8")+"&"+
                            URLEncoder.encode("usernamePasseggero","UTF-8")+"="+URLEncoder.encode(usernamePasseggero,"UTF-8")+"&"+
                            URLEncoder.encode("usernameAutista","UTF-8")+"="+URLEncoder.encode(usernameAutista,"UTF-8")+"&"+
                            URLEncoder.encode("costoCorsa","UTF-8")+"="+URLEncoder.encode(costoCorsa,"UTF-8")+"&"+
                            URLEncoder.encode("targa","UTF-8")+"="+URLEncoder.encode(targa,"UTF-8");

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
                Toast.makeText(ctx,message.toString(),Toast.LENGTH_SHORT).show();
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
