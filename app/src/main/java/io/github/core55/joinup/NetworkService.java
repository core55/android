package io.github.core55.joinup;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkService extends Service {

    public static final String ACTION = "io.github.core55.joinup.NetworkService";
    public static final String TAG = "NetworkService";

    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean started = true;
    private Handler handler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        // Fires when a service is first initialized

        RequestQueue queue = VolleyController.getInstance(this.getApplicationContext()).getRequestQueue();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Fires when a service is started up, do work here!

        // Send broadcast out with action filter and extras
        Intent i = new Intent(ACTION);
        i.putExtra("result", "baz");
        mLocalBroadcastManager.sendBroadcast(i);

        handlerStart();

        Log.d(TAG, "onStart");

        // Return "sticky" for services that are explicitly started and stopped as needed by the app.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // Cleanup service before destruction

    }

    public void requestMeetup(int method, String url, JSONObject data) {

        Log.d(TAG, "requestMeetup");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (method, url, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        String url = "";

                        try {
                            url = response.getJSONObject("_links").getJSONObject("users").getString("href");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        requestUserList(response, url);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add a request to your RequestQueue.
        VolleyController.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void requestUserList(final JSONObject meetup, String url) {

        int method = Request.Method.GET;
        JSONObject data = new JSONObject();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (method, url, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        JSONArray jsonUserArray = new JSONArray();

                        try {
                            jsonUserArray = response.getJSONObject("_embedded").getJSONArray("users");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent i = new Intent(ACTION);
                        i.putExtra("meetup", Meetup.fromJson(meetup, jsonUserArray));
                        mLocalBroadcastManager.sendBroadcast(i);

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add a request to your RequestQueue.
        VolleyController.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            requestMeetup(Request.Method.GET, "https://dry-cherry.herokuapp.com/api/meetups/9c40b759e39c42838335bd5f143b26e5", null);

            if (started) {
                handlerStart();
            }
        }
    };

    public void handlerStart() {
        started = true;
        handler.postDelayed(runnable, 7 * 1000);
    }

    public void handlerStop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

}
