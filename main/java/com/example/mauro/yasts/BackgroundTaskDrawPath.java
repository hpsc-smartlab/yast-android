package com.example.mauro.yasts;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BackgroundTaskDrawPath extends AsyncTask<String, Void, String> {


    String test;
    String JSON_STRING;
    String colore;

    Polyline line;
    Context ctx;

    GoogleMap mGoogleMap;

    AlertDialog.Builder builder;

    public BackgroundTaskDrawPath(Context ctx, GoogleMap map,String colore){
        this.ctx = ctx;
        this.mGoogleMap = map;
        this.colore = colore;
    }

    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... args) {

        Object partenza, arrivo;
        partenza = args[0];
        arrivo = args[1];

        try {



            String key = URLEncoder.encode("AIzaSyA_6gNahUBS6enRZCfi_H74uEpUyObLBI8");
            StringBuilder sb = new StringBuilder();
            sb.append("https://maps.googleapis.com/maps/api/directions/json");
            //sb.append("?origin=Napoli,IT");
            //sb.append(",&destination=Quarto,IT");
            sb.append("?origin="+partenza+",IT");
            sb.append(",&destination="+arrivo+",IT");
            sb.append("&sensor=false&mode=driving&alternatives=true&key=" + key);
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

            String esito = stringBuilder.toString().trim();

            return esito;

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
    protected void onPostExecute(String result) {
        drawPath(result);
    }

    public void drawPath(String result) {

        try {

            Random rnd = new Random();
            //int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            //int color = Color.BLUE;
            int color = 0;
            if ( colore.equals("blue") )
                color = Color.BLUE;
            if ( colore.equals("red") )
                color = Color.RED;
            if ( colore.equals("tra") )
                color = Color.CYAN;

            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            //PolylineOptions options = new PolylineOptions().width(20).color(color).geodesic(true);
            PolylineOptions options = new PolylineOptions().width(10).color(color).geodesic(true);

            for (int z = 0; z < list.size(); z++) {
                LatLng point = list.get(z);
                options.add(point);
            }
            line = mGoogleMap.addPolyline(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

}