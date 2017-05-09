package io.github.core55.joinup.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
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

import io.github.core55.joinup.entities.Meetup;
import io.github.core55.joinup.helpers.VolleyController;

public class NetworkService extends Service {

    public static final String ACTION = "io.github.core55.joinup.services.NetworkService";
    public static final String TAG = "NetworkService";

    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean started = true;
    private Handler handler = new Handler();

    private String meetupHash;

    @Override
    public void onCreate() {
        super.onCreate();
        // Fires when a service is first initialized

        RequestQueue queue = VolleyController.getInstance(this.getApplicationContext()).getRequestQueue();

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Fires when a service is started up, do work here!

        if (intent != null) {
            meetupHash = intent.getStringExtra("hash");
        }


        handlerStart();

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

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            requestMeetup("https://dry-cherry.herokuapp.com/api/meetups/" + meetupHash);

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

    public void requestMeetup(String url) {

        Log.d(TAG, "requestMeetup");
        int method = Request.Method.GET;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (method, url, null, new Response.Listener<JSONObject>() {
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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (method, url, null, new Response.Listener<JSONObject>() {
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

    /*
    public void requestUser(String url) {

        Log.d(TAG, "requestMeetup");
        int method = Request.Method.GET;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (method, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        String url = "";

                        try {
                            url = response.getJSONObject("_links").getJSONObject("meetups").getString("href");
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

    private void requestMeetupList(final JSONObject user, String url) {

        int method = Request.Method.GET;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (method, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        JSONArray jsonMeetupArray = new JSONArray();

                        try {
                            jsonMeetupArray = response.getJSONObject("_embedded").getJSONArray("meetups");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent i = new Intent(ACTION);
                        i.putExtra("user", Meetup.fromJson(user, jsonMeetupArray));
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
    */

    public void sendLocation(Location location, String url) {

        JSONObject newLocation = new JSONObject();

        try {
            newLocation.put("lastLongitude", location.getLongitude());
            newLocation.put("lastLatitude", location.getLatitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int method = Request.Method.PATCH;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (method, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

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
