package io.github.core55.joinup;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationManager implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Context context;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mRequestLocationUpdatesPendingIntent;

    public LocationManager(Context context) {
        this.context = context;
    }

    public void start() {
        // Initialize Google Play Services
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    private synchronized void buildGoogleApiClient() {
        Log.d("locationtesting", "buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("locationtesting", "onConnected 1");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // 120*1000
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F);

        // create the Intent to use WebViewActivity to handle results
        Intent mRequestLocationUpdatesIntent = new Intent(context, LocationService.class);

        // create a PendingIntent
        mRequestLocationUpdatesPendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, mRequestLocationUpdatesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // request location updates
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mRequestLocationUpdatesPendingIntent);
            Log.d("locationtesting", "onConnected 2");
        }


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}
