package io.github.core55.joinup.activities;

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

import java.io.IOException;
import java.util.List;

import io.github.core55.joinup.entities.Meetup;
import io.github.core55.joinup.entities.User;
import io.github.core55.joinup.helpers.AuthenticationHelper;
import io.github.core55.joinup.helpers.GsonRequest;
import io.github.core55.joinup.helpers.HttpRequestHelper;
import io.github.core55.joinup.helpers.NavigationDrawer;
import io.github.core55.joinup.R;

public class CreateActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = "CreateActivity";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private MarkerOptions meetupMarker;
    private LatLng pinLocation;
    private String hash = "";
    private LatLng centerLocation;
    private int zoomLevel;
    private Long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        AuthenticationHelper.syncDataHolder(this);

        // Inject the navigation drawer
        NavigationDrawer.buildDrawer(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapCreation);
        mapFragment.getMapAsync(this);

        askLocationPermission();
        buildGoogleApiClient();

        // Register search button listener
        Button search_button = (Button) findViewById(R.id.search_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMapSearch(v);
            }
        });

        // Register map creation button listener
        ImageButton createMapButton = (ImageButton) findViewById(R.id.createMapButton);
        createMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMeetup();
            }
        });
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    // TODO: to check
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
            if (!addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            } else {
                Toast.makeText(this, "Could not find location", Toast.LENGTH_SHORT).show();
            }


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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Create meetup draggable marker
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (meetupMarker == null) {
                    meetupMarker = new MarkerOptions().draggable(true);
                    meetupMarker.position(latLng);
                    meetupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.meetup));
                    mMap.addMarker(meetupMarker);
                    pinLocation = meetupMarker.getPosition();
                }
            }
        });

        // Drag marker and retrieve final position
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) { }

            @Override
            public void onMarkerDrag(Marker marker) { }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                pinLocation = marker.getPosition();
            }
        });

    }

    // TODO: move into a helper
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

    // TODO: move into a helper but before check "synchronized"
    private synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    // TODO: to check
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // Determine user current location as soon as connected with Google API
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // If we have a current location, then move the camera to center it
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
            // TODO: Notify user of the problem
        }
    }

    @Override
    public void onLocationChanged(Location location) { }

    private void createMeetup() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://dry-cherry.herokuapp.com/api/meetups";

        centerLocation = mMap.getCameraPosition().target;
        zoomLevel = (int) mMap.getCameraPosition().zoom;

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
                        hash = meetup.getHash();
                        // TODO: check if user is auth and eventually create new user
                        createUser();
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

    private void createUser() {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://dry-cherry.herokuapp.com/api/meetups/" + hash + "/users/save";

        User user = new User(mLastLocation.getLongitude(), mLastLocation.getLatitude());

        GsonRequest<User> request = new GsonRequest<>(
                Request.Method.POST, url, user, User.class,

                new Response.Listener<User>() {

                    @Override
                    public void onResponse(User user) {
                        id = user.getId();
                        startMapActivity();
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

    // TODO: Remove extra data. Should do with data holder
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
