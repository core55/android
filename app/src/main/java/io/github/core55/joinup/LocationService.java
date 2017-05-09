package io.github.core55.joinup;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

public class LocationService extends IntentService {

    public static final String ACTION = "io.github.core55.joinup.LocationService";
    public static final String TAG = "LocationService";

    private LocalBroadcastManager mLocalBroadcastManager;

    public LocationService() {
        // Used to name the worker thread, important only for debugging.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered

        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                Log.d(TAG, "accuracy: " + location.getAccuracy() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude());

                Intent i = new Intent(ACTION);
                i.putExtra("lat", location.getLatitude());
                i.putExtra("lon", location.getLongitude());
                mLocalBroadcastManager.sendBroadcast(i);

            }
        }

    }

}


