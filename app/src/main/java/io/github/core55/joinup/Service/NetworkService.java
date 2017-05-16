package io.github.core55.joinup.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.github.core55.joinup.Activity.MapActivity;
import io.github.core55.joinup.Entity.Meetup;
import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.Model.DataHolder;

import io.github.core55.joinup.Helper.GsonRequest;
import io.github.core55.joinup.Model.UserList;
import io.github.core55.joinup.R;

public class NetworkService extends Service {

    public static final String ACTION = "io.github.core55.joinup.services.NetworkService";
    public static final int UPDATE_TIME = 7;  //in seconds, how often communication is settled with the database.
    private LocalBroadcastManager mLocalBroadcastManager;
    public static final String TAG = "NetworkService";

    private boolean started = true;
    private Handler handler = new Handler();


    @Override
    public void onCreate() {
        super.onCreate();
        // Fires when a service is first initialized
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

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
            if (DataHolder.getInstance().getMeetup() == null || DataHolder.getInstance().getUser() == null) { return; }
            requestMeetup("https://dry-cherry.herokuapp.com/api/meetups/" + DataHolder.getInstance().getMeetup().getHash());
            sendLocation("https://dry-cherry.herokuapp.com/api/users/" + DataHolder.getInstance().getUser().getId());
            requestUserList("https://dry-cherry.herokuapp.com/api/meetups/" + DataHolder.getInstance().getMeetup().getHash() + "/users");
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
        RequestQueue queue = Volley.newRequestQueue(this);

        GsonRequest<Meetup> request = new GsonRequest<>(Request.Method.GET, url, Meetup.class,
                new Response.Listener<Meetup>() {
                    @Override
                    public void onResponse(Meetup meetup) {
                        DataHolder.getInstance().setMeetup(meetup);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("error", "Meetup could not be retrieved");
                    }
                });
        queue.add(request);
    }

//    public void requestUser(String url) {
//
//        int method = Request.Method.GET;
//        GsonRequest<User> request = new GsonRequest<User>
//                (method, url, User.class, new Response.Listener<User>() {
//                    @Override
//                    public void onResponse(User response) {
//
//                        DataHolder.getInstance().setUser(response);
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                    }
//                });
//
//        // Add a request to your RequestQueue.
//        VolleyController.getInstance(this).addToRequestQueue(request);
//
//    }

    public void sendLocation(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        User locationPatch = new User();

        locationPatch.setLastLongitude(DataHolder.getInstance().getUser().getLastLongitude());
        locationPatch.setLastLatitude(DataHolder.getInstance().getUser().getLastLatitude());

        GsonRequest<User> request = new GsonRequest<>(Request.Method.PATCH, url, locationPatch,
                User.class, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                Log.i(TAG, "Updated location of " + response.getUsername());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }

    public void requestUserList(String url) {
        RequestQueue queue = Volley.newRequestQueue(this);

        GsonRequest<UserList> request = new GsonRequest<>(Request.Method.GET, url, UserList.class,
                new Response.Listener<UserList>() {
                    @Override
                    public void onResponse(UserList response) {
                        DataHolder.getInstance().setUserList(response.getUsers());
                        Intent i = new Intent(ACTION);
                        DataHolder.getInstance().getActivity().populateUserList();
                        mLocalBroadcastManager.sendBroadcast(i);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }


}
