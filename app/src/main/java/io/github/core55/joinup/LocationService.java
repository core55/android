package io.github.core55.joinup;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

public class LocationService extends IntentService {


    public static final String TAG = "LocationService";

    /*
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST_CODE = 0;
    public static final String CONNECT_RESULT_KEY = "connectResult";
    private static final long UPDATE_INTERVAL = 10 * 1000;  //10 secs
    private static final long FASTEST_INTERVAL = 2000; //2 sec
    */

    public LocationService() {
        // Used to name the worker thread, important only for debugging.
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered

        if (LocationResult.hasResult(intent)) {
            LocationResult locationResult = LocationResult.extractResult(intent);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                Log.d("locationtesting", "accuracy: " + location.getAccuracy() + " lat: " + location.getLatitude() + " lon: " + location.getLongitude());
            }
        }

        /*
        // Extract the receiver passed into the service
        ResultReceiver receiver = intent.getParcelableExtra("receiver");

        // Extract additional values from the bundle
        String val = intent.getStringExtra("foo");

        // To send a message to the Activity, create a pass a Bundle
        Bundle bundle = new Bundle();
        bundle.putString("resultValue", "My Result Value. Passed in: " + val);

        // Here we call send passing a resultCode and the bundle of extras
        receiver.send(Activity.RESULT_OK, bundle);
        */
    }

}


