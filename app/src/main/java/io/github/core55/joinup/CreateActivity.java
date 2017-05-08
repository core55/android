package io.github.core55.joinup;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Response;
import com.afollestad.bridge.ResponseConvertCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLastLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15));
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

        //RequestQueue queue = Volley.newRequestQueue(this);

        //int method = Request.Method.POST;
        String url = "http://dry-cherry.herokuapp.com/api/meetups";
        JSONObject data = new JSONObject();

        final LatLng centerLocation = mMap.getCameraPosition().target;
        final int zoomLevel = (int) mMap.getCameraPosition().zoom;

        try {
            data.put("centerLongitude", centerLocation.longitude);
            data.put("centerLatitude", centerLocation.latitude);
            data.put("zoomLevel", zoomLevel);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, data.toString());


        Bridge.config()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Cache-Control", "no-cache")
                .defaultHeader("Accept", "application/json, application/hal+json");

        Bridge
                .post(url)
                .body(data)
                .asJsonObject(new ResponseConvertCallback<JSONObject>() {
                    @Override
                    public void onResponse(@NonNull Response response, @Nullable JSONObject object, @Nullable BridgeException e) {
                        if (e != null) {
                            // See the 'Error Handling' section for information on how to process BridgeExceptions
                            int reason = e.reason();
                        } else {
                            String hash = "";

                            try {
                                hash = object.getString("hash");
                            } catch (JSONException je) {
                                je.printStackTrace();
                            }

                            Intent i = new Intent(CreateActivity.this, MapActivity.class);
                            i.putExtra("hash", hash);
                            i.putExtra("centerLongitude", centerLocation.longitude);
                            i.putExtra("centerLatitude", centerLocation.latitude);
                            i.putExtra("zoomLevel", zoomLevel);
                            startActivity(i);
                        }
                    }
                });


        /*
        HeaderRequest jsonObjectRequest = new HeaderRequest
                (method, url, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d(TAG, "response...");

                        String hash = "";

                        try {
                            hash = response.getString("hash");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent i = new Intent(CreateActivity.this, MapActivity.class);
                        i.putExtra("hash", hash);
                        startActivity(i);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "response error...");
                        error.printStackTrace();
                    }
                });
           */

        // Add a request to your RequestQueue.
        //VolleyController.getInstance(this).addToRequestQueue(jsonObjectRequest);
        //queue.add(jsonObjectRequest);

    }

}
