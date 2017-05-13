/*
  Authors: Simone Stefani, Patrick Richer St-Onge
 */

package io.github.core55.joinup.Activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.core55.joinup.Entity.Meetup;
import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.Helper.AuthenticationHelper;
import io.github.core55.joinup.Helper.GsonRequest;
import io.github.core55.joinup.Helper.HttpRequestHelper;
import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.Model.UserList;
import io.github.core55.joinup.R;

public class ApplinkActivity extends AppCompatActivity {

    public static final String TAG = "ApplinkActivity";

    private static final String WEB_APP_URL_PREFIX = "m/";
    public static final String API_URL = "https://dry-cherry.herokuapp.com/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applink);

        AuthenticationHelper.syncDataHolder(this);
        AuthenticationHelper.authenticationLogger(this);

        appLinkHandler();
    }

    /**
     * Extracts the hash value of a meetup from the App Link URL.
     */
    private void appLinkHandler() {

        // Get data from app link call including URL
        Intent appLinkIntent = getIntent();
        Uri appLinkData = appLinkIntent.getData();

        if (appLinkData != null && appLinkData.isHierarchical()) {
            String uri = appLinkIntent.getDataString();
            Log.d(TAG, "AppLink URL: " + uri);

            // Apply pattern matching to extract meetup hash
            Pattern pattern = Pattern.compile("/#/" + WEB_APP_URL_PREFIX + "(.*)");
            Matcher matcher = pattern.matcher(uri);

            if (matcher.find()) {
                String appLinkHash = matcher.group(1);
                Log.d(TAG, "AppLink hash: " + appLinkHash);

                // Fetch corresponding meetup
                fetchMeetup(appLinkHash);
            }
        }
    }

    /**
     * Retrieves the meetup specified through the App Link.
     *
     * @param hash is the string that identifies the meetup
     */
    private void fetchMeetup(String hash) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = API_URL + "meetups/" + hash;

        GsonRequest<Meetup> request = new GsonRequest<>(Request.Method.GET, url, Meetup.class,
                new Response.Listener<Meetup>() {
                    @Override
                    public void onResponse(Meetup meetup) {
                        DataHolder.getInstance().setMeetup(meetup);
                        linkUserToMeetup(prepareUserForMeetup(meetup), meetup.getHash());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HttpRequestHelper.handleErrorResponse(error.networkResponse, ApplinkActivity.this);
                    }
                });
        queue.add(request);
    }

    /**
     * Creates a relationship between a user and a meetup.
     *
     * @param user is a user entity
     * @param hash is the string that identifies the meetup
     */
    private void linkUserToMeetup(User user, final String hash) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = API_URL + "meetups/" + hash + "/users/save";

        GsonRequest<User> request = new GsonRequest<>(Request.Method.POST, url, user, User.class,
                new Response.Listener<User>() {
                    @Override
                    public void onResponse(User user) {
                        DataHolder.getInstance().setUser(user);
                        AuthenticationHelper.syncSharedPreferences(ApplinkActivity.this);

                        fetchUserList(DataHolder.getInstance().getMeetup().getHash());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HttpRequestHelper.handleErrorResponse(error.networkResponse, ApplinkActivity.this);
                    }
                });
        queue.add(request);
    }

    /**
     * Retrieves the list of users participating in the meetup; then starts MapActivity.
     *
     * @param hash is the string that identifies the meetup
     */
    private void fetchUserList(String hash) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = API_URL + "meetups/" + hash + "/users";

        GsonRequest<UserList> request = new GsonRequest<>(Request.Method.GET, url, UserList.class,
                new Response.Listener<UserList>() {
                    @Override
                    public void onResponse(UserList userList) {
                        DataHolder.getInstance().setUserList(userList.getUsers());

                        Intent intent = new Intent(ApplinkActivity.this, MapActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HttpRequestHelper.handleErrorResponse(error.networkResponse, ApplinkActivity.this);
                    }
                });
        queue.add(request);
    }

    /**
     * Retrieve current user from DataHolder if present, otherwise create a new user with default
     * coordinates set to the center of the meetup.
     *
     * @param meetup is the meetup to associate the user
     * @return a user
     */
    private User prepareUserForMeetup(Meetup meetup) {
        AuthenticationHelper.syncDataHolder(this);
        DataHolder store = DataHolder.getInstance();

        User user;

        if (store.isAuthenticated() || store.isAnonymous()) {
            user = store.getUser();
        } else {
            user = new User(meetup.getCenterLongitude(), meetup.getCenterLatitude());
            store.setAnonymous(true);
        }

        return user;
    }
}
