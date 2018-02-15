package com.example.mauro.yasts;


import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mauro.yasts_test.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class fragmentRider extends Fragment implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private double lat, lng;
    private Criteria criteria;
    private String bestProvider;
    private EditText EditDestinazione;
    private String destinazione;
    private Button button_vai,button_clear;
    private Switch modalitaNotte;
    private TextView dettaglioCorsa;
    private Button fineCorsa;
    private boolean checkConn,checkGPS;
    private double bitwheels;
    private List<Marker> listMarker1 = new ArrayList<Marker>();
    private Marker marker1;
    private List<Marker> listMarker = new ArrayList<Marker>();
    private Marker marker;
    private List<Address> destinazioneList = null;
    private Address indirizzoDestinazione;
    private LatLng latlngDestinazione;
    private String latLngDestS;
    private String username;
    private double latDestinazioneM, lngDestinazioneM;
    private String latDestinazioneMS, lngDestinazioneMS;
    private String miaPos;
    private String city1;
    private String part[],split,splitto[],splittoDistance1[],predestinazione;
    private double costoCorsa,distanzakm;
    private Double distanzaTmp;
    private double latPosizioneUtente, lngPosizioneUtente;
    private String latPosizioneUtenteS, lngPosizioneUtenteS;
    private List<Address> posizioneUtenteList = null;
    private Address indirizzoPosizioneUtente;
    private LatLng latlngPosizioneUtente;
    private ArrayList autistiList = null;
    private String latS, lngS, usernameAutista, city;
    private double latAutista, lngAutista;
    private List<Address> addresses;
    private List<Address> partenzaList = null;
    private String arrayPosizioneAutistaS;
    private String arrayPosizioneMiaS;
    private Handler handler;
    private Timer timer;
    private boolean run = true;
    private String esito;
    private String indirizzoAutista;
    private String occupato;
    private String indirizzo;boolean corsaAccettata = false ;String targa;
    private AlertDialog.Builder builder;
    private Double rank;
    private List<String> prova = new ArrayList<String>();

    public fragmentRider() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_rider, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {

            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
            try {
                checkConn = checkConnessione();
                if ( checkConn )
                    coordinateGPS();
                else
                    Toast.makeText(getContext(),"Connessione limitata o assente",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                Toast.makeText(getContext(),"Connessione limitata o assente",Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {


        MapsInitializer.initialize(getContext());

        dettaglioCorsa = (TextView) mView.findViewById(R.id.dettaglioCorsa);
        mGoogleMap = googleMap;
        modalitaNotte = (Switch) mView.findViewById(R.id.modalitaNotte);
        modalitaNotte.setChecked(false);
        modalitaNotte.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!b) {
                    mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json));
                } else {
                    mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_json1));
                }
            }
        });
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("La tua posizione").icon(BitmapDescriptorFactory.fromResource(R.drawable.markerhuman)));
        CameraPosition liberty = CameraPosition.builder().target(new LatLng(lat, lng)).zoom(16).bearing(0).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(liberty));

        EditDestinazione = (EditText) mView.findViewById(R.id.EditDestinazione);
        button_vai = (Button) mView.findViewById(R.id.button_vai);
        button_clear = (Button) mView.findViewById(R.id.button_clear);
        fineCorsa = (Button) mView.findViewById(R.id.fineCorsa);



        handler = new Handler();
        timer = new Timer();
        TimerTask doAsynchronousTask1 = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            checkConn = checkConnessione();
                            if ( !checkConn ){
                                Toast.makeText(getContext(),"Connessione limitata o assente",Toast.LENGTH_LONG).show();
                            }
                            else {
                                controlloDisponibilitaBitWheels();
                                registraPosPasseggero(lat, lng);
                                inviaNotifica(bitwheels);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                    }

                });
            }

        };
        //aspetto 5 secondi prima dell'esecuzione poi viene eseguita per 30 secondi
        timer.schedule(doAsynchronousTask1, 5000, 20000);

        button_vai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destinazione = EditDestinazione.getText().toString();

                if ( !destinazione.matches("") ) {
                        button_vai.setVisibility(View.INVISIBLE);
                        button_clear.setVisibility(View.VISIBLE);
                        button_clear.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditDestinazione.setText("");
                                button_clear.setVisibility(View.INVISIBLE);
                                button_vai.setVisibility(View.VISIBLE);
                            }
                        });

                    try {
                        BackgroundTaskMostraLuoghi distanza1 = new BackgroundTaskMostraLuoghi(getActivity());
                        destinazione = destinazione.replaceAll(" ","+");
                        String dist = distanza1.execute(destinazione,predestinazione).get();
                        JSONArray totale = new JSONObject(dist)
                                .getJSONArray("predictions");
                        int ciccio = totale.length();
                        for(int i=0; i<ciccio; i++){
                            JSONObject jsonRespRouteDistance = new JSONObject(dist)
                                    .getJSONArray("predictions") //rows
                                    .getJSONObject(i);
                            String distance = jsonRespRouteDistance.get("description").toString();
                            prova.add(distance);
                        }
                        new AlertDialog.Builder(getContext())
                                .setTitle("Conferma la destinazione")
                                .setSingleChoiceItems(prova.toArray(new String[prova.size()]),-1, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Geocoder geocoder = new Geocoder(getActivity());
                                            destinazione = prova.get(i);
                                        EditDestinazione.setText(destinazione);
                                        try {
                                            destinazioneList = geocoder.getFromLocationName(destinazione, 1);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        indirizzoDestinazione = destinazioneList.get(0);
                                        latlngDestinazione = new LatLng(indirizzoDestinazione.getLatitude(), indirizzoDestinazione.getLongitude());
                                        double a = indirizzoDestinazione.getLatitude();
                                        double b = indirizzoDestinazione.getLongitude();
                                        try {
                                            addresses = geocoder.getFromLocation(a, b, 5);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        city1 = addresses.get(0).getLocality()+" "+addresses.get(0).getThoroughfare()+" "+addresses.get(0).getSubThoroughfare();
                                        latLngDestS = city1;
                                        marker = mGoogleMap.addMarker(new MarkerOptions().position(latlngDestinazione).title(destinazione).icon(BitmapDescriptorFactory.fromResource(R.drawable.destinazione)));
                                        listMarker.add(marker);

                                        latDestinazioneM = indirizzoDestinazione.getLatitude();
                                        lngDestinazioneM = indirizzoDestinazione.getLongitude();

                                        latDestinazioneMS = String.valueOf(latDestinazioneM);
                                        lngDestinazioneMS = String.valueOf(lngDestinazioneM);
                                    }
                                })
                                .setPositiveButton("Conferma", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        prova.clear();
                                    }
                                }).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        richiediAutista();


    }

    public boolean checkConnessione() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            return false;
        }
        return true;
    }


    public void controlloDisponibilitaBitWheels(){
        checkConn = checkConnessione();
        if (!checkConn) {
            Toast.makeText(getContext(), "Connessione limitata o assente", Toast.LENGTH_LONG).show();
        } else {
            BackgroundTaskBitWheels ruotini = new BackgroundTaskBitWheels(getContext());
            String user = "";
            username = getActivity().getIntent().getExtras().getString("username").trim();
            try {
                user = ruotini.execute(username).get();
                JSONObject jsonObject = new JSONObject(user);
                JSONArray jsonArray = jsonObject.getJSONArray("server_response");
                JSONObject jo = jsonArray.getJSONObject(0);
                String message = jo.getString("message");
                bitwheels = Double.parseDouble(message);
                if (bitwheels <= 0) {
                    Toast.makeText(getContext(), "Non disponi di sufficienti BitWheels per poter effettuare una chiamata :( !", Toast.LENGTH_LONG).show();
                    button_vai.setEnabled(false);
                } else {
                    button_vai.setEnabled(true);
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


    public void verificaEsistenzaCorsa(String username) throws ExecutionException, InterruptedException {
        BackgroundTaskAvvisaRider backgroundTaskAvvisaRider = new BackgroundTaskAvvisaRider(getActivity());
        String esito = backgroundTaskAvvisaRider.execute(username).get();

        try {
            JSONObject jsonObject = new JSONObject(esito);
            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
            JSONObject jo = jsonArray.getJSONObject(0);
            String code = jo.getString("code");
            String message = jo.getString("message");
            indirizzoAutista = jo.optString("indirizzoAutista");
            targa = jo.optString("targa");
            String nomeAutista = jo.optString("nomeAutista");
            if ( code.equals("corsaAccettata_true") ){
                final String usernameA = username;
                new AlertDialog.Builder(getActivity())
                        .setTitle("Corsa accettata")
                        .setMessage(message).show();

                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(5000);

                corsaAccettata = true;


                fineCorsa.setVisibility(View.VISIBLE);
                EditDestinazione.setVisibility(View.INVISIBLE);
                button_vai.setVisibility(View.INVISIBLE);
                dettaglioCorsa.setVisibility(View.VISIBLE);
                dettaglioCorsa.setText("Driver: "+nomeAutista+"\nTarga: "+targa);
                    fineCorsa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            final RatingBar ratingBar;
                            final Dialog rankDialog  = new Dialog(getContext());
                            rankDialog.setContentView(R.layout.rating);
                            rankDialog.setCancelable(true);
                            ratingBar = (RatingBar)rankDialog.findViewById(R.id.dialog_ratingbar);
                            ratingBar.setRating(1);
                            Button updateButton = (Button) rankDialog.findViewById(R.id.rank_dialog_button);
                            updateButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    BackgroundTaskFeedBack backgroundFeed = new BackgroundTaskFeedBack(getContext());
                                    Float voto = ratingBar.getRating();
                                    String votoS = Float.toString(voto);
                                    backgroundFeed.execute(usernameA,votoS);
                                    Toast.makeText(getContext(),"Hai votato",Toast.LENGTH_LONG).show();
                                    resetAll();
                                    fineCorsa.setVisibility(View.INVISIBLE);
                                    dettaglioCorsa.setVisibility(View.INVISIBLE);
                                    EditDestinazione.setVisibility(View.VISIBLE);
                                    button_vai.setVisibility(View.VISIBLE);
                                    rankDialog.dismiss();
                                    destinazione = "";
                                    corsaAccettata = false;
                                }
                            });
                            rankDialog.show();
                    }
                });
            }
            if(code.equals("corsaRifiutata_true") ){
                new AlertDialog.Builder(getActivity())
                        .setTitle("Corsa rifiutata")
                        .setMessage(message).show();

                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(5000);
            }

        } catch (JSONException e) {

        }
    }

    public void resetAll(){
        miaPos = "";
        indirizzoAutista = "";
        destinazione = "";
        EditDestinazione.setText("");
        disegnaPercoso(miaPos,indirizzoAutista,destinazione);
    }

    public void disegnaPercoso(String posizioneUtente, String indirizzoAutista, String destinazione){
        try {

            if (posizioneUtente != null && indirizzoAutista != null && !destinazione.isEmpty()) {
                if (indirizzoAutista != null && corsaAccettata) {
                    String miaPosS = posizioneUtente.replaceAll(" ","_");
                    String latLngDestSS = indirizzoAutista;
                    latLngDestSS = latLngDestSS.replaceAll(" ", "_");
                    destinazione = destinazione.replaceAll(" ","_");
                    BackgroundTaskDrawPath drawPath1 = new BackgroundTaskDrawPath(getActivity(), mGoogleMap,"red");
                    drawPath1.execute(latLngDestSS, miaPosS);
                    BackgroundTaskDrawPath drawPath2 = new BackgroundTaskDrawPath(getActivity(), mGoogleMap,"blue");
                    drawPath2.execute(miaPosS, destinazione);
                }
            } else {
                BackgroundTaskDrawPath drawPath1 = new BackgroundTaskDrawPath(getActivity(), mGoogleMap, "red");
                if (drawPath1.getStatus().equals(AsyncTask.Status.RUNNING))
                    drawPath1.cancel(true);
                BackgroundTaskDrawPath drawPath2 = new BackgroundTaskDrawPath(getActivity(), mGoogleMap , "blue");
                if (drawPath2.getStatus().equals(AsyncTask.Status.RUNNING))
                    drawPath2.cancel(true);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    public void test(){

        if ( latlngDestinazione == null  ) {
            latlngDestinazione = new LatLng(0,0);
        }
        else {
            marker1 = mGoogleMap.addMarker(new MarkerOptions().position(latlngDestinazione).title(destinazione).icon(BitmapDescriptorFactory.fromResource(R.drawable.destinazione)));
            listMarker1.add(marker1);
        }
    }


    public void ciao(){
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(40.851775,14.268124)).title("Napoli"));
    }


    public void mostraAutisti() throws ExecutionException, InterruptedException, JSONException, IOException {
        BackgroundTaskMostraDriver backgroundTaskMostraDriver = new BackgroundTaskMostraDriver(getActivity());
        autistiList = backgroundTaskMostraDriver.execute().get();
        JSONArray distanza = new JSONObject(autistiList.get(0).toString()).getJSONArray("server_response");

        int numero = distanza.length();

        for(int i=0; i<numero; i++){
            JSONObject jsonObject = distanza.getJSONObject(i);
            latS = jsonObject.getString("lat");
            lngS = jsonObject.getString("lng");
            usernameAutista = jsonObject.optString("username");
            String feeedback = jsonObject.optString("feedback");
            String occupato = jsonObject.optString("occupato");
            targa = jsonObject.optString("targa");
            latAutista = Double.parseDouble(lngS);
            lngAutista = Double.parseDouble(latS);
            segnaAutisti(latAutista,lngAutista,usernameAutista,feeedback,occupato,targa);
        }

    }

    public void segnaAutisti(Double lat,Double lng,String usernameAutista,String feedback,String occupatoV,String targa) throws IOException {
        occupato = occupatoV;
        Geocoder geocoder = new Geocoder(getActivity(), Locale.ITALIAN);
        addresses = geocoder.getFromLocation(lng, lat, 5);

        if ( addresses.get(0).getThoroughfare() == null && addresses.get(0).getSubThoroughfare() == null)
            city = addresses.get(0).getLocality();

        else if ( addresses.get(0).getThoroughfare() != null && addresses.get(0).getSubThoroughfare() == null )
            city = addresses.get(0).getLocality()+" "+addresses.get(0).getThoroughfare();
        else
            city = addresses.get(0).getLocality()+" "+addresses.get(0).getThoroughfare()+" "+addresses.get(0).getSubThoroughfare();

        partenzaList = geocoder.getFromLocationName(city, 1);
        Address location = partenzaList.get(0);


        rank = Double.parseDouble(feedback);
        if ( rank <= 2.5 ){
            if ( occupato.equals("1") )
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title(usernameAutista+"-Occupato").snippet(city).icon(BitmapDescriptorFactory.fromResource(R.drawable.caroccupato)));
            else
             mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title(usernameAutista).snippet(city).icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

        }
        if ( rank > 2.5 && rank <= 4.5 ) {
            if ( occupato.equals("1") )
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title(usernameAutista+"-Occupato").snippet(city).icon(BitmapDescriptorFactory.fromResource(R.drawable.caroccupato)));
            else
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(usernameAutista).snippet(city).icon(BitmapDescriptorFactory.fromResource(R.drawable.carargento)));
        }
        if ( rank > 4.5 ) {
            if ( occupato.equals("1") )
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title(usernameAutista+"-Occupato").snippet(city).icon(BitmapDescriptorFactory.fromResource(R.drawable.caroccupato)));
            else
                mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title(usernameAutista).snippet(city).icon(BitmapDescriptorFactory.fromResource(R.drawable.caroro)));

        }


    }

    public void registraPosPasseggero(Double lat, Double lng) throws IOException {
        double latRA = lat;
        double lngRa = lng;
        Geocoder geocoder = new Geocoder(getActivity());
        try {
            addresses = geocoder.getFromLocation(latRA, lngRa, 10);

            String latS = String.valueOf(latRA);
            String lngS = String.valueOf(lngRa);

            if ( addresses.get(0).getThoroughfare() == null || addresses.get(0).getSubThoroughfare() == null )
                indirizzo = addresses.get(0).getLocality();
            else
                indirizzo = addresses.get(0).getLocality() + " " + addresses.get(0).getThoroughfare() + " " + addresses.get(0).getSubThoroughfare();
            username = getActivity().getIntent().getExtras().getString("username").trim();
            BackgroundTaskRegistraPosRider backgroundTaskRegistraPasseggero = new BackgroundTaskRegistraPosRider(getActivity());
            backgroundTaskRegistraPasseggero.execute(latS, lngS, indirizzo, username, "100", "1");
        }catch (IOException e ){
            Toast.makeText(getContext(),"Connessione limitata o assente",Toast.LENGTH_LONG).show();
        }catch (NullPointerException e){
            Toast.makeText(getContext(),"Connessione limitata o assente1",Toast.LENGTH_LONG).show();
        }
    }

    public void richiediAutista(){
        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener(){

            @Override
            public void onInfoWindowClick(Marker marker) {

                destinazione = EditDestinazione.getText().toString();
                if ( destinazione.matches("") || listMarker.isEmpty() )
                    Toast.makeText(getContext(),"Devi prima impostare una destinazione",Toast.LENGTH_LONG).show();
                else{
                    arrayPosizioneAutistaS = marker.getSnippet();
                    arrayPosizioneMiaS = miaPos;
                    miaPos = miaPos.replaceAll(" ","+");
                    predestinazione = destinazione.replaceAll(" ","+");
                    split =  marker.getTitle();
                    part = split.split("-");
                    if ( part.length == 1 ){
                        BackgroundTaskDistanza distanza = new BackgroundTaskDistanza(getActivity());
                        try {
                            String dist = distanza.execute(miaPos,predestinazione).get();
                            JSONObject jsonRespRouteDistance = new JSONObject(dist)
                                    .getJSONArray("rows")
                                    .getJSONObject(0)
                                    .getJSONArray ("elements")
                                    .getJSONObject(0).getJSONObject("distance");
                            String distance = jsonRespRouteDistance.get("text").toString();
                            JSONObject jsonRespRouteDistance1 = new JSONObject(dist)
                                    .getJSONArray("rows")
                                    .getJSONObject(0)
                                    .getJSONArray ("elements")
                                    .getJSONObject(0)
                                    .getJSONObject("duration");
                            String distance1 = jsonRespRouteDistance1.get("text").toString();
                            splitto = distance1.split(" ");


                            if ( splitto.length == 2 ){
                                distance = distance.replace("km"," ");
                                distance1 = distance1.replace("mins"," ");
                                distanzakm = Double.parseDouble(distance);
                                distanzaTmp = Double.parseDouble(distance1);
                            }
                            if ( splitto.length == 4 ){
                                splittoDistance1 = distance1.split(" ");
                            }
                            if ( splitto.length == 2 ){
                                Double min = Double.parseDouble(splitto[0]);
                                costoCorsa = (distanzakm*1.50)+(min*0.20)+1.50;
                            }
                            if ( splitto.length == 4 ){
                                Double h = Double.parseDouble(splitto[0]);
                                Double min = Double.parseDouble(splitto[2]);
                                String tempo = splittoDistance1[0]+"."+splittoDistance1[2];
                                Double timeTot = Double.parseDouble(tempo)*60;
                                costoCorsa = (distanzakm*1.50)*(timeTot*0.20)+1.50;
                            }
                            new AlertDialog.Builder(getContext())
                                    .setTitle("Informazioni sul viaggio")
                                    .setMessage("Partenza: "+arrayPosizioneMiaS+"\nDestinazione: "+destinazione+"\nDistanza km: "+distance+"\nTempo: "+distance1+"\nCosto BitWheels: "+Math.ceil(costoCorsa)+"\nDriver: "+marker.getTitle())
                                    .setPositiveButton("Chiama", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            BackgroundTaskCorsa backgroundTaskCorsa = new BackgroundTaskCorsa(getActivity());
                                            String costo = String.valueOf(Math.ceil(costoCorsa));
                                            if ( bitwheels < Math.ceil(costoCorsa) ){
                                                Toast.makeText(getContext(),"Attenzione non possiedi i BitWheels necessari!",Toast.LENGTH_LONG).show();
                                            }
                                            else
                                                backgroundTaskCorsa.execute(arrayPosizioneMiaS, arrayPosizioneAutistaS, destinazione, username, part[0],costo,targa);
                                        }
                                    })
                                    .setNeutralButton("Info Driver", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                            new AlertDialog.Builder(getContext())

                                                    .setTitle("Dettaglio Driver")
                                                    .setMessage("Email: "+part[0]+"\nTarga: "+targa.toUpperCase()+"\nRank: "+rank+"\nPosizione attuale: "+arrayPosizioneAutistaS)
                                                    .setPositiveButton("Chiama", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            BackgroundTaskCorsa backgroundTaskCorsa = new BackgroundTaskCorsa(getActivity());
                                                            String costo = String.valueOf(Math.ceil(costoCorsa));
                                                            backgroundTaskCorsa.execute(arrayPosizioneMiaS, arrayPosizioneAutistaS, destinazione, username, part[0],costo,targa);
                                                        }
                                                    })
                                                    .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            destinazione = "";
                                                        }
                                                    }).show();
                                        }
                                    })
                                    .setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Toast.makeText(getContext(),"Hai annullato la richiesta",Toast.LENGTH_LONG).show();
                                            destinazione = "";
                                        }
                                    })
                                    .show();

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            Toast.makeText(getContext(),"Attenzione,ridigitare la destinazione ad esempio Napoli,Na",Toast.LENGTH_LONG).show();
                        }
                    }
                    if ( part.length > 1 ){ //significa che è occupato
                        String esito = part[1].trim().toLowerCase();
                        if ( esito.equals("occupato") ){
                            Toast.makeText(getContext(),"Mi dispiace, attualmente il Driver "+part[0]+" è occupato in una corsa",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }


    public void coordinateGPS() throws IOException {
        locationManager = (LocationManager) getActivity().getSystemService(getContext().LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                checkConn = checkConnessione();
                checkGPS = checkGPS();
                if ( !checkConn || !checkGPS ){
                    button_vai.setVisibility(View.INVISIBLE);
                }
                else {
                    button_vai.setVisibility(View.VISIBLE);
                    lat = location.getLatitude();
                    lng = location.getLongitude();

                    latPosizioneUtente = lat;
                    lngPosizioneUtente = lng;

                    latPosizioneUtenteS = String.valueOf(latPosizioneUtente);
                    lngPosizioneUtenteS = String.valueOf(lngPosizioneUtente);

                    Geocoder geocoder = new Geocoder(getActivity());

                    try {
                        posizioneUtenteList = geocoder.getFromLocation(latPosizioneUtente, lngPosizioneUtente, 5);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        miaPos = posizioneUtenteList.get(0).getLocality() + " " + posizioneUtenteList.get(0).getThoroughfare() + " " + posizioneUtenteList.get(0).getSubThoroughfare();
                        CameraPosition liberty = CameraPosition.builder().target(new LatLng(lat, lng)).zoom(14).bearing(0).build();
                        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(liberty));
                    } catch (NullPointerException e) {
                        Toast.makeText(getContext(), "Attenzione", Toast.LENGTH_LONG).show();
                    }
                    try {
                        posizioneUtenteList = geocoder.getFromLocationName(miaPos, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        Toast.makeText(getContext(), "Attenzione", Toast.LENGTH_LONG).show();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(getContext(), "Attenzione", Toast.LENGTH_LONG).show();
                    }

                    try {
                        indirizzoPosizioneUtente = posizioneUtenteList.get(0);
                    } catch (NullPointerException e) {
                        Toast.makeText(getContext(), "Attenzione", Toast.LENGTH_LONG).show();
                    } catch (IndexOutOfBoundsException e){
                        Toast.makeText(getContext(),"Posizione non geocodificata",Toast.LENGTH_LONG).show();
                    }

                    try {
                        latlngPosizioneUtente = new LatLng(indirizzoPosizioneUtente.getLatitude(), indirizzoPosizioneUtente.getLongitude());
                    } catch (NullPointerException e) {
                        Toast.makeText(getContext(), "Attenzione4", Toast.LENGTH_LONG).show();
                    }
                    mGoogleMap.clear();
                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("La tua posizione").icon(BitmapDescriptorFactory.fromResource(R.drawable.markerhuman)));
                    ciao(); //marker napoli fisso
                    test();

                    try {
                        mostraAutisti();
                        disegnaPercoso(miaPos, indirizzoAutista, destinazione);
                        verificaEsistenzaCorsa(username);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e){
                        Toast.makeText(getContext(),"Do stai?",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);
        if ( locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null ){
            locationManager.requestLocationUpdates(bestProvider, 10000, 0, locationListener);
            lat = 0 ;
            lng = 0 ;
        }
        else{
            locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);

            lat = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude();
            lng = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude();

            registraPosPasseggero(lat,lng);

        }

    }

    public boolean checkGPS() {
        locationManager = (LocationManager) getContext().getSystemService(getActivity().LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getContext(), "Attenzione: devi attivare il gps!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    public void inviaNotifica(Double bitwheels) {
        String message = "Ciao, hai a disposizione "+bitwheels+" BitWheels";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext());
        mBuilder.setSmallIcon(R.drawable.car);
        mBuilder.setContentTitle("Yasts");
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        mBuilder.setContentText(message);
        NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

}
