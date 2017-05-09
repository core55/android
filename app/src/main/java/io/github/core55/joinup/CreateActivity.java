package io.github.core55.joinup;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateActivity extends DrawerActivity implements
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

    private MarkerOptions meetupMarker;
    private LatLng pinLocation;

    private String hash = "";
    private LatLng centerLocation;
    private int zoomLevel;

    int id;

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

    public void onMapSearch(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.editText);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise map styling via json
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_styles));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // show blue dot on map
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (meetupMarker == null) {
                    meetupMarker = new MarkerOptions().draggable(true);
                    meetupMarker.position(latLng);
                    meetupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.meetup));
                    mMap.addMarker(meetupMarker);
                }
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                pinLocation = marker.getPosition();
            }
        });

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

        int method = Request.Method.POST;
        String url = "http://dry-cherry.herokuapp.com/api/meetups";
        JSONObject data = new JSONObject();

        centerLocation = mMap.getCameraPosition().target;
        zoomLevel = (int) mMap.getCameraPosition().zoom;

        try {
            data.put("centerLongitude", centerLocation.longitude);
            data.put("centerLatitude", centerLocation.latitude);
            data.put("zoomLevel", zoomLevel);
            if (pinLocation != null) {
                data.put("pinLongitude", pinLocation.longitude);
                data.put("pinLatitude", pinLocation.latitude);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HeaderRequest meetupCreationRequest = new HeaderRequest
                (method, url, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            hash = response.getJSONObject("data").getString("hash");
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }

                        createUser();

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json, application/hal+json");
                return params;
            }
        };

        VolleyController.getInstance(this).addToRequestQueue(meetupCreationRequest);

    }

    private void createUser() {

        int method = Request.Method.POST;
        String url = "http://dry-cherry.herokuapp.com/api/meetups/" + hash + "/users/save";
        JSONObject data = new JSONObject();

        try {
            data.put("lastLongitude", mLastLocation.getLongitude());
            data.put("lastLatitude", mLastLocation.getLatitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HeaderRequest userCreationRequest = new HeaderRequest
                (method, url, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        id = -1;

                        try {
                            id = response.getJSONObject("data").getInt("id");
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }

                        startMapActivity();

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json, application/hal+json");
                return params;
            }
        };

        VolleyController.getInstance(this).addToRequestQueue(userCreationRequest);

    }

    private void startMapActivity() {
        Intent i = new Intent(CreateActivity.this, MapActivity.class);
        i.putExtra("hash", hash);
        i.putExtra("centerLongitude", centerLocation.longitude);
        i.putExtra("centerLatitude", centerLocation.latitude);
        i.putExtra("zoomLevel", zoomLevel);
        if (pinLocation != null) {
            i.putExtra("pinLongitude", pinLocation.longitude);
            i.putExtra("pinLatitude", pinLocation.latitude);
        }
        i.putExtra("id", id);
        startActivity(i);
    }

}
