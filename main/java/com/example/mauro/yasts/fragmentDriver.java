package com.example.mauro.yasts;



import android.content.Context;
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
import android.support.v7.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
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


public class fragmentDriver extends Fragment implements OnMapReadyCallback {

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Criteria criteria;
    private String bestProvider;
    private double lat,lng;
    private Switch modalitaNotte;
    private double latPosizioneUtente,lngPosizioneUtente;
    private String latPosizioneUtenteS,lngPosizioneUtenteS;
    private List<Address> posizioneUtenteList = null;
    private Address indirizzoPosizioneUtente;
    private LatLng latlngPosizioneUtente;
    private String miaPos;
    private ArrayList autistiList = null;
    private String latS,lngS,usernameAutista,city;
    private double latAutista,lngAutista;
    private List<Address> addresses;
    private List<Address> partenzaList = null;
    private Handler handler;
    private Timer timer;
    private String username;
    private LatLng latlngDestinazione;
    private TextView infoCorsa;
    private Button btn_accetta,btn_rifiuta;
    private String nomePasseggero;
    private String indirizzoPasseggero,destinazione;
    private boolean esistoClickCorsa = false, checkConn, checkGPS;

    public fragmentDriver() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_driver, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        username = getActivity().getIntent().getExtras().getString("username").trim();
        infoCorsa = (TextView) mView.findViewById(R.id.infoCorsa);
        btn_accetta = (Button) mView.findViewById(R.id.btn_accetta);
        btn_rifiuta = (Button) mView.findViewById(R.id.btn_rifiuta);

        mMapView = (MapView) mView.findViewById(R.id.map);
        if ( mMapView != null ){

            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
            try {
                coordinateGPS();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void onMapReady(final GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());

        mGoogleMap = googleMap;

        modalitaNotte = (Switch) mView.findViewById(R.id.switch1);
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

        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title("La tua posizione").icon(BitmapDescriptorFactory.fromResource(R.drawable.caroro)));
        CameraPosition liberty = CameraPosition.builder().target(new LatLng(lat,lng)).zoom(16).bearing(0).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(liberty));

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
                            registraPosPasseggero(lat, lng);
                            verificaEsistenzaCorsa(username);
                            if (esistoClickCorsa)
                                disegnaPercoso(miaPos, indirizzoPasseggero, destinazione);
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                    }

                });
            }

        };
        //aspetto 5 secondi prima dell'esecuzione poi viene eseguita per 20 secondi
        timer.schedule(doAsynchronousTask1, 5000, 20000);


    }

    public void MarkerNapoliFisso(){
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(40.851775,14.268124)).title("Napoli"));
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



    public void mostraPasseggeri() throws ExecutionException, InterruptedException, JSONException, IOException {
        BackgroundTaskMostraRider backgroundTaskMostraRider = new BackgroundTaskMostraRider(getActivity());
        autistiList = backgroundTaskMostraRider.execute().get();
        JSONArray distanza = new JSONObject(autistiList.get(0).toString()).getJSONArray("server_response");

        int numero = distanza.length();

        for(int i=0; i<numero; i++){
            JSONObject jsonObject = distanza.getJSONObject(i);
            latS = jsonObject.getString("lat");
            lngS = jsonObject.getString("lng");
            usernameAutista = jsonObject.getString("username");
            latAutista = Double.parseDouble(lngS);
            lngAutista = Double.parseDouble(latS);
            segnaPasseggeri(latAutista,lngAutista,usernameAutista);
        }

    }

    public void test(){

        if ( latlngDestinazione == null  ) {
            latlngDestinazione = new LatLng(0,0);
        }
        else
            mGoogleMap.addMarker(new MarkerOptions().position(latlngDestinazione).title("a").icon(BitmapDescriptorFactory.fromResource(R.drawable.destinazione)));
    }

    public void segnaPasseggeri(Double lat,Double lng,String usernameAutista) throws IOException {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.ITALIAN);
        addresses = geocoder.getFromLocation(lng, lat, 5);
        if ( addresses.get(0).getSubThoroughfare() == null || addresses.get(0).getSubThoroughfare() == null )
            city = addresses.get(0).getLocality();
        else
            city = addresses.get(0).getLocality()+" "+addresses.get(0).getThoroughfare()+" "+addresses.get(0).getSubThoroughfare();
        partenzaList = geocoder.getFromLocationName(city, 1);
        Address location = partenzaList.get(0);
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title(usernameAutista).icon(BitmapDescriptorFactory.fromResource(R.drawable.markerhuman)));
    }

    public void registraPosPasseggero(Double lat, Double lng) throws IOException {
        double latRA = lat;
        double lngRa = lng;
        Geocoder geocoder = new Geocoder(getActivity());
        addresses = geocoder.getFromLocation(latRA, lngRa, 10);

        String latS = String.valueOf(latRA);
        String lngS = String.valueOf(lngRa);
        String indirizzo;

        if ( addresses.get(0).getThoroughfare() == null && addresses.get(0).getSubThoroughfare() == null )
            indirizzo = addresses.get(0).getLocality();
        else if ( addresses.get(0).getThoroughfare() != null && addresses.get(0).getSubThoroughfare() == null )
            indirizzo = addresses.get(0).getLocality()+" "+addresses.get(0).getThoroughfare();
        else
            indirizzo = addresses.get(0).getLocality()+" "+addresses.get(0).getThoroughfare()+" "+addresses.get(0).getSubThoroughfare();
        username = getActivity().getIntent().getExtras().getString("username").trim();

        BackgroundTaskRegistraPosDriver backgroundTaskRegistraPosDriver = new BackgroundTaskRegistraPosDriver(getActivity());
        backgroundTaskRegistraPosDriver.execute(latS,lngS,indirizzo,username,"100","1");
    }

    public void verificaEsistenzaCorsa(final String username) throws ExecutionException, InterruptedException {
        BackgroundTaskVerificaCorsa verificaCorsa = new BackgroundTaskVerificaCorsa(getActivity());
        String esito = verificaCorsa.execute(username).get();
        try {
            JSONObject jsonObject = new JSONObject(esito);
            JSONArray jsonArray = jsonObject.getJSONArray("server_response");
            JSONObject jo = jsonArray.getJSONObject(0);
            String code = jo.getString("code");
            if ( code.equals("corsa") ){
                indirizzoPasseggero = "";
                destinazione = "";
                if ( indirizzoPasseggero.length() == 0 ) {
                    esistoClickCorsa = false;
                    BackgroundTaskDrawPath backgroundTaskDrawPath = new BackgroundTaskDrawPath(getContext(), mGoogleMap, "tra");
                    backgroundTaskDrawPath.execute(miaPos, miaPos);
                    if (backgroundTaskDrawPath.getStatus().equals(AsyncTask.Status.RUNNING))
                        backgroundTaskDrawPath.cancel(true);

                }

            }

            String message = jo.optString("message");
            nomePasseggero = jo.optString("nomePasseggero"); //era getString
            indirizzoPasseggero = jo.optString("indirizzoPasseggero");
            destinazione = jo.optString("destinazione");
            final String costoCorsa = jo.optString("costoCorsa");



            if ( code.equals("corsa_true") ){
                new AlertDialog.Builder(getActivity())
                        .setTitle("Hai una corsa")
                        .setMessage("Ti ricordiamo che accettando questa corsa, guadagnerai "+costoCorsa+" BitWheels\n\n"+message).show();
                infoCorsa.setText(message);

                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(5000);
                btn_accetta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BackgroundTaskUpdateCorsa backgroundTaskUpdateCorsa = new BackgroundTaskUpdateCorsa(getContext());
                        backgroundTaskUpdateCorsa.execute("1",nomePasseggero,username,costoCorsa);
                        Toast.makeText(getContext(),"Hai accettato la corsa, avviso il rider",Toast.LENGTH_LONG).show();
                        esistoClickCorsa = true;
                    }
                });
                btn_rifiuta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BackgroundTaskUpdateCorsa backgroundTaskUpdateCorsa = new BackgroundTaskUpdateCorsa(getContext());
                        backgroundTaskUpdateCorsa.execute("2",nomePasseggero,username,costoCorsa);
                        infoCorsa.setText("");
                        Toast.makeText(getContext(),"Hai rifiutato la corsa, avviso il rider",Toast.LENGTH_LONG).show();

                    }
                });
            }
        } catch (JSONException e) {
        } catch (NullPointerException e){
            Toast.makeText(getContext(),"Connessione limitata o assente",Toast.LENGTH_LONG).show();
        }
     }


     public void disegnaPercoso(String posizioneAutista,String indirizzoPasseggero, String destinazione){
         if ( indirizzoPasseggero != null && destinazione != null  ) {
             indirizzoPasseggero = indirizzoPasseggero.replaceAll(" ","+");
             destinazione = destinazione.replaceAll(" ","+");
             String miaPos = posizioneAutista.replaceAll(" ","+");
             BackgroundTaskDrawPath backgroundTaskDrawPath = new BackgroundTaskDrawPath(getContext(), mGoogleMap, "red");
             backgroundTaskDrawPath.execute(miaPos, indirizzoPasseggero);
             BackgroundTaskDrawPath backgroundTaskDrawPath2 = new BackgroundTaskDrawPath(getContext(), mGoogleMap, "blue");
             backgroundTaskDrawPath2.execute(indirizzoPasseggero, destinazione);
         }
         else{

         }
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
                    Toast.makeText(getContext(),"Connessione limitata o assente",Toast.LENGTH_LONG).show();
                }
                else {

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

                    miaPos = posizioneUtenteList.get(0).getLocality() + " " + posizioneUtenteList.get(0).getThoroughfare() + " " + posizioneUtenteList.get(0).getSubThoroughfare();
                    CameraPosition liberty = CameraPosition.builder().target(new LatLng(lat, lng)).zoom(16).bearing(0).build();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(liberty));
                    try {
                        posizioneUtenteList = geocoder.getFromLocationName(miaPos, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        indirizzoPosizioneUtente = posizioneUtenteList.get(0);
                        latlngPosizioneUtente = new LatLng(indirizzoPosizioneUtente.getLatitude(), indirizzoPosizioneUtente.getLongitude());
                    } catch (IndexOutOfBoundsException e){
                        Toast.makeText(getContext(),"Do stai?",Toast.LENGTH_LONG).show();
                    }
                    mGoogleMap.clear();
                    mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("La tua posizione").icon(BitmapDescriptorFactory.fromResource(R.drawable.caroro)));
                    MarkerNapoliFisso(); //se viene mostrato allora coordinate prelevate olè
                    try {

                        mostraPasseggeri();
                        verificaEsistenzaCorsa(username);
                        if (esistoClickCorsa)
                            disegnaPercoso(miaPos, indirizzoPasseggero, destinazione);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
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
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.INTERNET}
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

}
