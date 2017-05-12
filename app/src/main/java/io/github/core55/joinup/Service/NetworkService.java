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
import io.github.core55.joinup.Model.UserList;

public class NetworkService extends Service {

    public static final String ACTION = "io.github.core55.joinup.services.NetworkService";
    public static final int UPDATE_TIME = 7;  //in seconds, how often communication is settled with the database.
    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean started = true;
    private Handler handler = new Handler();


    @Override
    public void onCreate() {
        super.onCreate();
        // Fires when a service is first initialized
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this); //TODO: is this necessary anymore? What does it do? JLRTO

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Fires when a service is started up, do work here!
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
            requestMeetup("https://dry-cherry.herokuapp.com/api/meetups/" + DataHolder.getInstance().getMeetup().getHash());
            sendLocation("https://dry-cherry.herokuapp.com/api/users/" + DataHolder.getInstance().getUser().getId());
            requestUserList("https://dry-cherry.herokuapp.com/api/meetups/"+ DataHolder.getInstance().getMeetup().getHash() + "/users");
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
                (method, url, User.class, new Response.Listener<User>() {
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

        User locationPatch = new User();

            locationPatch.setLastLongitude(DataHolder.getInstance().getUser().getLastLongitude());
            locationPatch.setLastLatitude(DataHolder.getInstance().getUser().getLastLatitude());


        int method = Request.Method.PATCH;

        GsonRequest<User> request = new GsonRequest<>
                (method, url, locationPatch ,User.class, new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        Log.e("loc upd",response.getNickname());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add a request to your RequestQueue.
        VolleyController.getInstance(this).addToRequestQueue(request);

    }

    public void requestUserList(String url) {

        int method = Request.Method.GET;

        GsonRequest<UserList> request = new GsonRequest<UserList>
                (method, url,UserList.class, new Response.Listener<UserList>() {
                    @Override
                    public void onResponse(UserList response) {
                        DataHolder.getInstance().setUserList(response.getUsers());
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        // Add a request to your RequestQueue.
        VolleyController.getInstance(this).addToRequestQueue(request);
    }

}
