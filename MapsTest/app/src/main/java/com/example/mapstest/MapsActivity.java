package com.example.mapstest;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mapstest.databinding.ActivityMapsBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    //initialises google map object
    private GoogleMap mMap;
    //initialises arraylist containing all crime data for each marker
    ArrayList<CrimeData> crimeData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        //creates settings button on top of map
        Button settingsButton = new Button(this);
        settingsButton.setText("Settings");
        addContentView(settingsButton, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        //intent to take user to settings activity
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
    public void showNotification() {
        //function that makes a notification when app opened
        String info = "Crime Mapper Data at Location : Loughborough";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MapsActivity.this, "notificationChannel");
        builder.setContentTitle("Crime Mapper Tool");
        builder.setContentText(info);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MapsActivity.this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        managerCompat.notify(1, builder.build());
    }
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //checks if location permission has been granted
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        //creates notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notificationChannel", "notificationChannel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            showNotification();
        }
        double longitude = 0;
        double latitude = 0;
        //tries to get user's current longitude and latitude
        try {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
        if (latitude < 50.10319 || latitude > 60.15456 || longitude < -7.64133 || longitude > 1.75159){
            //if latitude and longitude outside UK bounds, then defaults user location to Loughborough
            latitude = 52.7721;
            longitude = -1.2062;
        }
        //stores location in sharedpreferences
        SharedPreferences sp = getSharedPreferences("locationDetails" ,Context.MODE_PRIVATE);
        sp.edit().putString("Latitude",String.valueOf(latitude)).apply();
        sp.edit().putString("Longitude",String.valueOf(longitude)).apply();
        //calculates boundaries of initial search box
        String lowerRight = (latitude-0.02)+","+(longitude+0.05);
        String lowerLeft = (latitude-0.02)+","+(longitude-0.05);
        String upperRight = (latitude+0.02)+","+(longitude+0.05);
        String upperLeft = (latitude+0.02)+","+(longitude-0.05);
        //executes async task - gets data from police api using above boundaries
        new GetApiData().execute("https://data.police.uk/api/crimes-street/all-crime?poly="+lowerRight+":"+lowerLeft+":"+upperRight+":"+upperLeft+"&date=2023-03");
        mMap = googleMap;
        LatLng lboro = new LatLng(latitude, longitude);
        //makes a default red marker at user location
        mMap.addMarker(new MarkerOptions().position(lboro).title("Marker at user location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lboro, 15));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                //if marker is held down, opens new activity with more details
                // this simulates a long press
                String category = marker.getTitle();
                String streetName = marker.getSnippet();
                LatLng pos = marker.getPosition();
                String latitude = String.valueOf(pos.latitude);
                String longitude = String.valueOf(pos.longitude);
                Intent intent = new Intent(MapsActivity.this,InfoActivity.class);
                //uses a bundle to send multiple pieces of data across to new activity
                Bundle extras = new Bundle();
                extras.putString("category", category);
                extras.putString("streetName", streetName);
                extras.putString("latitude", latitude);
                extras.putString("longitude", longitude);
                intent.putExtras(extras);
                startActivity(intent);
                marker.remove();
                //removes old marker and creates a new one at the same position
                //this is to prevent the drag action
                double lat = marker.getPosition().latitude-0.001;
                double lng = marker.getPosition().longitude;
                LatLng newItem = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(newItem).title(category).snippet(streetName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).draggable(true));
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {}
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class GetApiData extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... urls) {
            URL url;
            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            HttpURLConnection connection;
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                connection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            }
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try {
                connection.connect();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            BufferedReader br;
            try {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String content = "", line;
            while (true) {
                try {
                    if ((line = br.readLine()) == null) {
                        break;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                content += line + "\n";
            }
            return content;
        }
        protected void onProgressUpdate(Integer... progress) {
        }
        protected void onPostExecute(String result) {
            // executes after background thread
            // this is where the markers get put on the map
            try {
                //adds markers to arraylist
                JSONArray array = new JSONArray(result);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject crimeDetail = array.getJSONObject(i);
                    String category = crimeDetail.getString("category");
                    String month = crimeDetail.getString("month");
                    JSONObject locDetail = crimeDetail.getJSONObject("location");
                    String lats = locDetail.getString("latitude");
                    float latitude = Float.parseFloat(lats);
                    String longs = locDetail.getString("longitude");
                    float longitude = Float.parseFloat(longs);
                    JSONObject streetDetail = locDetail.getJSONObject("street");
                    String streetName = streetDetail.getString("name");
                    CrimeData crimeInstance = new CrimeData(category, latitude, longitude, streetName, month);
                    crimeData.add(crimeInstance);
                }
                Log.e("marker", "pre markers");
                for (CrimeData crimeItem : crimeData) {
                    //puts markers on map
                    LatLng newItem = new LatLng(crimeItem.getLatitude(), crimeItem.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(newItem).title(crimeItem.getCategory()).snippet(crimeItem.getStreetName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).draggable(true));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

}