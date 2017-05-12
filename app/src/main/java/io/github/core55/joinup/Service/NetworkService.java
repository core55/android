package io.github.core55.joinup.Service;

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

import org.json.JSONException;
import org.json.JSONObject;


import io.github.core55.joinup.Entity.Meetup;
import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.Helper.VolleyController;
import io.github.core55.joinup.Model.DataHolder;

import io.github.core55.joinup.Helper.GsonRequest;

public class NetworkService extends Service {

    public static final String ACTION = "io.github.core55.joinup.services.NetworkService";
    public static final int UPDATE_TIME = 7;  //in seconds, how often communication is settled with the database.
    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean started = true;
    private Handler handler = new Handler();

    //private String meetupHash;

    @Override
    public void onCreate() {
        super.onCreate();
        // Fires when a service is first initialized
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Fires when a service is started up, do work here!
        /*if (intent != null) {
            meetupHash = intent.getStringExtra("hash");
        }*/
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
        handlerStop();
        // Cleanup service before destruction
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            requestMeetup(DataHolder.getInstance().getMeetup().getHash());
            sendLocation("https://dry-cherry.herokuapp.com/api/users" + DataHolder.getInstance().getUser().getId());
            //requestMeetup("https://dry-cherry.herokuapp.com/api/meetups/" + meetupHash);

            if (started) {
                handlerStart();
            }
        }
    };

    public void handlerStart() {
        started = true;
        handler.postDelayed(runnable, UPDATE_TIME * 1000);
    }

    public void handlerStop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void requestMeetup(String url) {
        int method = Request.Method.GET;

        GsonRequest<Meetup> request = new GsonRequest<Meetup>(method, url, Meetup.class,
                new Response.Listener<Meetup>() {
                    @Override
                    public void onResponse(Meetup m) {
                        DataHolder.getInstance().setMeetup(m);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", "Meetup could not be retrieved");
                    }
                });
        // Add a request to your RequestQueue.
        VolleyController.getInstance(this).addToRequestQueue(request);
    }

    public void requestUser(String url) {

        int method = Request.Method.GET;
        GsonRequest<User> request = new GsonRequest<User>
                (method, url, User.class, null, new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {

                        DataHolder.getInstance().setUser(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        // Add a request to your RequestQueue.
        VolleyController.getInstance(this).addToRequestQueue(request);

    }

    public void sendLocation(String url) {

        JSONObject newLocation = new JSONObject();

        try {
            newLocation.put("lastLongitude", DataHolder.getInstance().getUser().getLastLongitude());
            newLocation.put("lastLatitude", DataHolder.getInstance().getUser().getLastLatitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int method = Request.Method.PATCH;

        GsonRequest<JSONObject> request = new GsonRequest<JSONObject>
                (method, url, JSONObject.class, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add a request to your RequestQueue.
        VolleyController.getInstance(this).addToRequestQueue(request);

    }

   /* public void requestUser(String url) {

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

    }*/

   /* private void requestMeetupList(final JSONObject user, String url) {

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

    /*public void sendLocation(Location location, String url) {

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
<<<<<<< HEAD:app/src/main/java/io/github/core55/joinup/Service/NetworkService.java
    }
=======

    }*/

}
