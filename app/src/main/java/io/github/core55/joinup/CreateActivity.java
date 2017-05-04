package io.github.core55.joinup;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateActivity extends AppCompatActivity implements
        View.OnClickListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = "CreateActivity";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private static final long UPDATE_INTERVAL = 20 * 1000;  // 20 secs
    private static final long FASTEST_INTERVAL = 2000; // 2 secs

    private GoogleMap mMap;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;
    private String hash = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapCreation);
        mapFragment.getMapAsync(this);

        askLocationPermission();
        buildGoogleApiClient();

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createMapButton:
                createMeetup();

                Intent i = new Intent(this, MapActivity.class);
                while (hash.equals(""));
                i.putExtra("hash", hash);
                startActivity(i);

                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // show blue dot on map
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

    }

    public void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    private synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /*
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F);

        // create the Intent to use WebViewActivity to handle results
        Intent mRequestLocationUpdatesIntent = new Intent(this, LocationService.class);

        // request location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, null);
        }
        */

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLastLocation != null) {
            //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            Log.d(TAG, "last location");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Disconnected. Please re-connect.");
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Network lost. Please re-connect.");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            // Google Play services can fix the issue
            try {
                connectionResult.startResolutionForResult(this, 0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            // Google Play services has no idea how to fix the issue
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    private void createMeetup() {

        int method = Request.Method.POST;
        String url = "https://dry-cherry.herokuapp.com/api/meetups";
        JSONObject data = new JSONObject();

        LatLng centerLocation = mMap.getCameraPosition().target;
        float zoomLevel = mMap.getCameraPosition().zoom;

        try {
            data.put("centerLongitude", centerLocation.longitude);
            data.put("centerLatitude", centerLocation.latitude);
            data.put("zoomLevel", zoomLevel);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (method, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            hash = response.getString("hash");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add a request to your RequestQueue.
        VolleyController.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
}
