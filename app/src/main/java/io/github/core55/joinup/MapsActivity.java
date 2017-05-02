package io.github.core55.joinup;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity {

    public static final int REQUEST_USERS = 20;
    public static final int REQUEST_MEETUP_INFO = 21;
    public static final int UPDATE_MY_LOCATION = 22;


    private HashMap<Integer, MarkerOptions> markersOnMap = new HashMap<Integer, MarkerOptions>();

    String meetupHash; //stores hash if accessed through app link
    boolean appLinkAccess = false;
    boolean centerUser = false; //used to center your location in the screen the first time

    int userId = 42; //hardcoded to update user 42

    // Instantiate the RequestQueue to send requests to API.
    RequestQueue queue;

    //handler for the loop to update user info
    private boolean started = true;
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            requestToAPI(meetupHash, REQUEST_USERS); //requests to API and calls displayUsersOnMap
            if (started) {
                handlerStart();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        //String hash = this.getIntent().getStringExtra("meetupHash");
        //Toast.makeText(this, hash, Toast.LENGTH_LONG).show();
        //Log.d("hash",hash + "nothing");
        queue = Volley.newRequestQueue(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        meetupHash = this.getIntent().getStringExtra("meetupHash");
        if (meetupHash != null) {
            appLinkAccess = true;
            requestToAPI(meetupHash, REQUEST_USERS); //requests to API and calls displayUsersOnMap
            requestToAPI(meetupHash, REQUEST_MEETUP_INFO); //requests meetup coordinates and displays meeting pin
        } else {
            appLinkAccess = false;
        }
        Log.d("appLinkAccess", String.valueOf(appLinkAccess));


    }


    public void handlerStop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void handlerStart() {
        started = true;
        handler.postDelayed(runnable, 7 * 1000);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    //@Override
    public void onMapReady(GoogleMap googleMap) {


        if (appLinkAccess) {
            requestToAPI(meetupHash, REQUEST_MEETUP_INFO); //requests meetup coordinates and displays meeting pin
            requestToAPI(meetupHash, REQUEST_USERS); //requests to API and calls displayUsersOnMap
            handlerStart();
        }

    }


    void requestToAPI(String hash, final int requestType) {
        requestToAPI(hash, requestType, null);
    }

    void requestToAPI(String hash, final int requestType, JSONObject jsonObject) {

        String url = "https://dry-cherry.herokuapp.com/api"; //TODO make a string in strings.xml
        int httpMethod = -1;
        switch (requestType) {
            case REQUEST_USERS:
                url += "/meetups/" + hash + "/users";
                httpMethod = Request.Method.GET;
                break;
            case REQUEST_MEETUP_INFO:
                url += "/meetups/" + hash;
                httpMethod = Request.Method.GET;
                break;
            case UPDATE_MY_LOCATION:
                url += "/users/" + userId;
                httpMethod = Request.Method.PATCH;
                break;
        }

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (httpMethod, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            switch (requestType) {
                                case REQUEST_USERS:
                                    displayUsersOnMap(response);
                                    break;
                                case REQUEST_MEETUP_INFO:
                                    centerOnMeetupLocation(response);
                                    break;
                                case UPDATE_MY_LOCATION:
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // mTextView.setText("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });


        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }

    void displayUsersOnMap(JSONObject j) throws JSONException {
        JSONArray users = j.getJSONObject("_embedded").getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {// JSONObject user : users){
            JSONObject user = users.getJSONObject(i);
            Log.d("user", user.toString());
            //if(user.getInt()) TODO do not display a user marker for the user of the app,
            LatLng latLng = new LatLng(user.getDouble("lastLatitude"), user.getDouble("lastLongitude"));

            if (markersOnMap.containsKey(user.getInt("id"))) { //if the user has already a marker in the map, we update its location and nickname
                MarkerOptions m = markersOnMap.get(user.getInt("id"));
                m.position(latLng);
                m.title(user.getString("nickname"));
            } else {
                MarkerOptions newMO = new MarkerOptions();
                newMO.position(latLng);
                newMO.title(user.getString("nickname"));
                if (newMO.getTitle() == null) {
                    newMO.icon(BitmapDescriptorFactory.fromResource(R.drawable.null_marker));
                } else {
                    newMO.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
                }
                //Toast.makeText(this, latLng.toString(), Toast.LENGTH_LONG).show();
                markersOnMap.put(user.getInt("id"), newMO);
                //mMap.addMarker(newMO);
            }


        }
    }

    void centerOnMeetupLocation(JSONObject j) throws JSONException {
        Log.d("JSONObject", j.toString());
        LatLng latLng = new LatLng(j.getDouble("centerLatitude"), j.getDouble("centerLongitude"));
        MarkerOptions mapCenter = new MarkerOptions();
        mapCenter.position(latLng);
        mapCenter.title(j.getString("name"));
        mapCenter.icon(BitmapDescriptorFactory.fromResource(R.drawable.meetingpoint));
        // mMap.addMarker(mapCenter);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo((float)j.getDouble("zoomLevel")));


    }


    //@Override
    public void onLocationChanged(Location location) {


        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.you_marker));
        //markerOptions.title("Here you are");
        //Toast.makeText(this, latLng.toString(), Toast.LENGTH_LONG).show();
        //mCurrLocationMarker = mMap.addMarker(markerOptions);

        if (!centerUser) {
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            //mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            centerUser = true;
        }
        JSONObject newLocation = new JSONObject();

        try {
            newLocation.put("lastLongitude", location.getLongitude());
            newLocation.put("lastLatitude", location.getLatitude());
            //newLocation.put("nickname", "Juan Luis");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestToAPI(null, UPDATE_MY_LOCATION, newLocation);

    }


}