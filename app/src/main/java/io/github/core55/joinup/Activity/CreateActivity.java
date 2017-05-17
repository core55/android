/*
  Authors: Simone Stefani, Patrick Richer St-Onge
 */

package io.github.core55.joinup.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import io.github.core55.joinup.Entity.Meetup;
import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.Helper.AuthenticationHelper;
import io.github.core55.joinup.Helper.GsonRequest;
import io.github.core55.joinup.Helper.HttpRequestHelper;
import io.github.core55.joinup.Helper.LocationHelper;
import io.github.core55.joinup.Helper.NavigationDrawer;
import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.R;

public class CreateActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = "CreateActivity";
    private final String API_BASE_URL = "https://dry-cherry.herokuapp.com/api/";

    private static final long SMALLEST_DISPLACEMENT = 2; // 2 meters
    private static final long UPDATE_INTERVAL = 10 * 1000;  // 10 secs
    private static final long FASTEST_INTERVAL = 2 * 1000; // 2 secs
    private static final int ZOOM_LEVEL = 15;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private MarkerOptions meetupMarker;

    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private LatLng pinLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        AuthenticationHelper.syncDataHolder(this);
        AuthenticationHelper.authenticationLogger(this);

        // Inject the navigation drawer and setup click listeners
        //NavigationDrawer.buildDrawer(this, false);
        registerOnClickListeners();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.create_map_fragment);
        mapFragment.getMapAsync(this);

        LocationHelper.askLocationPermission(this);
        buildGoogleApiClient();

        createLocationRequest();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        AuthenticationHelper.syncDataHolder(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AuthenticationHelper.syncDataHolder(this);

        // Resume receiving location updates
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AuthenticationHelper.syncSharedPreferences(this);

        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        AuthenticationHelper.syncSharedPreferences(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AuthenticationHelper.syncSharedPreferences(this);
    }

    private void registerOnClickListeners() {

        // Register search button listener
        Button mSearchButton = (Button) findViewById(R.id.location_search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLocation();
            }
        });

        // Register map creation button listener
        ImageButton mCreateMapButton = (ImageButton) findViewById(R.id.create_meetup_button);
        mCreateMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastLocation != null) {
                    createMeetup();
                } else {
                    Toast.makeText(CreateActivity.this, "Failed to create meetup. User location unavailable", Toast.LENGTH_LONG).show();
                }
            }
        });
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

        // Show blue dot on map
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Create meetup draggable marker
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (meetupMarker == null) {
                    meetupMarker = new MarkerOptions().draggable(true);
                    meetupMarker.position(latLng);
                    meetupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_meetup));
                    mMap.addMarker(meetupMarker);
                    pinLocation = meetupMarker.getPosition();
                }
            }
        });

        // Drag marker and retrieve final position
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

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Determine user current location as soon as connected with Google API
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {

        // If first time getting location, then move the camera to center it
        if (mLastLocation == null) {
            // Show blue dot on map
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), ZOOM_LEVEL);
            mMap.animateCamera(cameraUpdate);

            // Display helpful text
            Toast.makeText(this, "Click on the map to place a meetup pin", Toast.LENGTH_LONG).show();
        }

        // Update location
        mLastLocation = location;
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Disconnected. Please re-connect.");
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to connect to Google Map API.");
        }
    }

    public void searchLocation() {

        // Get location from user input
        EditText locationSearch = (EditText) findViewById(R.id.location_search_field);
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!location.equals("")) {

            Geocoder geocoder = new Geocoder(this);

            // Use Google Map Geocoder to determine list of locations from input
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Cannot find location!", Toast.LENGTH_LONG).show();
            }

            // If a valid location is found then move the map focus
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            } else {
                Toast.makeText(this, "Cannot find location!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void createMeetup() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = API_BASE_URL + "meetups";

        // Infer meetup coordinates and zoom from camera
        LatLng centerLocation = mMap.getCameraPosition().target;
        int zoomLevel = (int) mMap.getCameraPosition().zoom;

        // Create meetup object, possibly also with pin coordinates
        Meetup meetup = new Meetup(centerLocation.longitude, centerLocation.latitude, zoomLevel);
        if (pinLocation != null) {
            meetup.setPinLongitude(pinLocation.longitude);
            meetup.setPinLatitude(pinLocation.latitude);
        }

        GsonRequest<Meetup> request = new GsonRequest<>(
                Request.Method.POST, url, meetup, Meetup.class,

                new Response.Listener<Meetup>() {

                    @Override
                    public void onResponse(Meetup meetup) {
                        DataHolder.getInstance().setMeetup(meetup);

                        // If first-time visitor create anonymous user otherwise retrieve from DataHolder
                        User user;
                        if (DataHolder.getInstance().isAuthenticated() || DataHolder.getInstance().isAnonymous()) {
                            user = DataHolder.getInstance().getUser();
                        } else {
                            user = new User(mLastLocation.getLongitude(), mLastLocation.getLatitude());
                        }

                        // Persist relationship between user and newly created meetup
                        linkUserToMeetup(user);
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HttpRequestHelper.handleErrorResponse(error.networkResponse, CreateActivity.this);
                    }
                });
        queue.add(request);
    }

    private void linkUserToMeetup(User user) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String hash = DataHolder.getInstance().getMeetup().getHash();
        final String url = API_BASE_URL + "meetups/" + hash + "/users/save";

        GsonRequest<User> request = new GsonRequest<>(
                Request.Method.POST, url, user, User.class,

                new Response.Listener<User>() {

                    @Override
                    public void onResponse(User user) {
                        if (!DataHolder.getInstance().isAuthenticated() && !DataHolder.getInstance().isAnonymous()) {
                            DataHolder.getInstance().setAnonymous(true);
                        }

                        DataHolder.getInstance().setUser(user);
                        AuthenticationHelper.syncSharedPreferences(CreateActivity.this);

                        Intent i = new Intent(CreateActivity.this, MapActivity.class);
                        startActivity(i);
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HttpRequestHelper.handleErrorResponse(error.networkResponse, CreateActivity.this);
                    }
                });
        queue.add(request);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    @Override
    public void onBackPressed() {
    }
}
