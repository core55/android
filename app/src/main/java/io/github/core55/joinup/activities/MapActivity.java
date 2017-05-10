package io.github.core55.joinup.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.materialdrawer.Drawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.helpers.GsonRequest;
import io.github.core55.joinup.helpers.HeaderRequest;
import io.github.core55.joinup.helpers.HttpRequestHelper;
import io.github.core55.joinup.helpers.LocationHelper;
import io.github.core55.joinup.services.LocationManager;
import io.github.core55.joinup.services.LocationService;
import io.github.core55.joinup.entities.Meetup;
import io.github.core55.joinup.helpers.NavigationDrawer;
import io.github.core55.joinup.services.NetworkService;
import io.github.core55.joinup.R;
import io.github.core55.joinup.entities.User;
import io.github.core55.joinup.helpers.UserAdapter;
import io.github.core55.joinup.helpers.VolleyController;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "MapActivity";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;
    int count = 0;

    private LocationManager locationManager;
    private String meetupHash = "028baffc294c434c8c8a4a610aa68e00";
    private int id_user = 716;
    private HashMap<Long, MarkerOptions> markersOnMap = new HashMap<>();

    private Double centerLatitude;
    private Double centerLongitude;
    private Integer zoomLevel;

    private MarkerOptions meetupMarker;
    private Marker meetupMarkerView;
    private Double pinLongitude;
    private Double pinLatitude;

    private Double lastLatitude;
    private Double lastLongitude;

    private String mActivityTitle;
    private ListView lv;
    private ArrayList userList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Inject the navigation drawer
        NavigationDrawer.buildDrawer(this);

        meetupHash = getIntent().getStringExtra("hash");
        centerLatitude = getIntent().getDoubleExtra("centerLatitude", -1);
        centerLongitude = getIntent().getDoubleExtra("centerLongitude", -1);
        zoomLevel = getIntent().getIntExtra("zoomLevel", -1);
        pinLongitude = getIntent().getDoubleExtra("pinLongitude", -1);
        pinLatitude = getIntent().getDoubleExtra("pinLatitude", -1);
        id_user = getIntent().getIntExtra("id", -1);

        // Retrieve map hash from applink
        handleAppLink();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationHelper.askLocationPermission(this);
//        askLocationPermission();

        locationManager = new LocationManager(this);
        locationManager.start();

        createShareButtonListener();
        createPeopleButtonListener();

        mActivityTitle = getTitle().toString();

        //TODO: Check if the user has nickname. Not only auth problem
        if (!DataHolder.getInstance().isAuthenticated()) {
            namePrompt();
        }
    }

    // TODO: What these methods do?
    @Override
    protected void onResume() {
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(NetworkService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(networkServiceReceiver, filter);

        IntentFilter filter2 = new IntentFilter(LocationService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, filter2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkServiceReceiver);
        // or `unregisterReceiver(networkServiceReceiver)` for a normal broadcast
    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Double lat = intent.getDoubleExtra("lat", -1);
            Double lon = intent.getDoubleExtra("lon", -1);

            // TODO: Why the null check?
            if (lat != null && lat != -1 && lon != null && lon != -1) {

                lastLatitude = lat;
                lastLongitude = lon;

                RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
                final String url = "https://dry-cherry.herokuapp.com/api/users/" + id_user;

                User user = new User(lon, lat);

                GsonRequest<User> request = new GsonRequest<>(
                        Request.Method.PATCH, url, user, User.class,

                        new Response.Listener<User>() {

                            @Override
                            public void onResponse(User user) {
                                // TODO: Update user coords here?
                            }
                        },

                        new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                HttpRequestHelper.handleErrorResponse(error.networkResponse, MapActivity.this);
                            }
                        });
                queue.add(request);
            }

        }
    };

    // Define the callback for what to do when message is received
    private BroadcastReceiver networkServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Meetup m = intent.getParcelableExtra("meetup");
            if (m != null) {

                if (meetupMarker == null && meetupMarkerView == null && m.getPinLatitude() != null && m.getPinLongitude() != null) {
                    meetupMarker = new MarkerOptions().draggable(true);
                    meetupMarker.position(new LatLng(m.getPinLatitude(), m.getPinLongitude()));
                    meetupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.meetup));
                    meetupMarkerView = mMap.addMarker(meetupMarker);
                } else if (meetupMarker != null && meetupMarkerView != null && m.getPinLatitude() != null && m.getPinLongitude() != null) {
                    meetupMarker.position(new LatLng(m.getPinLatitude(), m.getPinLongitude()));
                    meetupMarkerView.setPosition(new LatLng(m.getPinLatitude(), m.getPinLongitude()));
                }

                for (User u : m.getUsersList()) {
                    if (markersOnMap.containsKey(u.getId())) {
                        MarkerOptions marker = markersOnMap.get(u.getId());
                        marker.position(new LatLng(u.getLastLatitude(), u.getLastLongitude()));
                        marker.title(u.getNickname());
                    } else {
                        MarkerOptions newMarker = new MarkerOptions();
                        newMarker.position(new LatLng(u.getLastLatitude(), u.getLastLongitude()));
                        newMarker.title(u.getNickname());
                        if (newMarker.getTitle() == null) {
                            newMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                        } else {
                            newMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                        }
                        markersOnMap.put(u.getId(), newMarker);
                        mMap.addMarker(newMarker);
                    }


                }

            }

        }
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLatitude, centerLongitude), zoomLevel));

        if (meetupMarker == null && meetupMarkerView == null && pinLatitude != -1 && pinLongitude != -1) {
            meetupMarker = new MarkerOptions().draggable(true);
            meetupMarker.position(new LatLng(pinLatitude, pinLongitude));
            meetupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.meetup));
            meetupMarkerView = mMap.addMarker(meetupMarker);
        }

        try {
            // Customise map styling via json
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_styles));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // show blue dot on map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        String tempNickname;
        try {
            tempNickname = DataHolder.getInstance().getUser().getNickname();
        } catch (NullPointerException e) {
            tempNickname = null;
        }

        if (DataHolder.getInstance().isAuthenticated() && tempNickname != null && !tempNickname.equals("")) {
            Context context = getApplicationContext();
            CharSequence text = "Welcome " + tempNickname + "!";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                Log.d(TAG, "marker drag end");
                pinLongitude = marker.getPosition().longitude;
                pinLatitude = marker.getPosition().latitude;
                sendMeetupPinLocation();
            }
        });

        launchNetworkService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the task you need to do.
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
                break;
            }
            // other 'case' lines to check for other permissions this app might request
        }
    }

    private void handleAppLink() {
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if (appLinkData != null && appLinkData.isHierarchical()) {
            String uri = appLinkIntent.getDataString();
            Log.d(TAG, "url = " + uri);

            Pattern pattern = Pattern.compile("/\\#/m/(.*)");
            Matcher matcher = pattern.matcher(uri);
            if (matcher.find()) {
                meetupHash = matcher.group(1);
                Log.d(TAG, "hash = " + meetupHash);

                createUser();

            }
        }
    }


    private void namePrompt() {

        // Build modal to fill in user nickname
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_name, null);
        mBuilder.setView(mView);

        final EditText name = (EditText) mView.findViewById(R.id.enter_name);

        // Prompt modal
        final AlertDialog dialog = mBuilder.create();

        // TODO: Introduce validation
        if (name.getText().toString().matches("")) {
            dialog.show();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);

            Button enter = (Button) dialog.findViewById(R.id.enter);
            enter.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    String nickname = name.getText().toString();
                    Log.d(TAG, nickname);

                    patchNickName(nickname);
                    dialog.dismiss();
                }

            });
        }

        // Prompt for keyboard
        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
    }

    public void patchNickName(String name) {
        RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
        final String url = "https://dry-cherry.herokuapp.com/api/users/" + id_user;

        User user = new User();
        user.setNickname(name);

        GsonRequest<User> request = new GsonRequest<>(
                Request.Method.PATCH, url, user, User.class,

                new Response.Listener<User>() {

                    @Override
                    public void onResponse(User user) {
                        // TODO: Update shared prefs and data holder
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HttpRequestHelper.handleErrorResponse(error.networkResponse, MapActivity.this);
                    }
                });
        queue.add(request);
    }

    private void createShareButtonListener() {
        ImageButton mShowDialog = (ImageButton) findViewById(R.id.imageButton);
        mShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_share, null);
                mBuilder.setView(mView);

                EditText url = (EditText) mView.findViewById(R.id.editText);
                url.setText(meetupHash);

                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });
    }

    public void copyToCliboard(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", meetupHash);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Link is copied!", Toast.LENGTH_SHORT).show();
    }


    public void launchNetworkService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, NetworkService.class);

        i.putExtra("hash", meetupHash);

        // Start the service
        startService(i);
    }

    private void createUser() {

        int method = Request.Method.POST;
        String url = "http://dry-cherry.herokuapp.com/api/meetups/" + meetupHash + "/users/save";
        JSONObject data = new JSONObject();

        try {
            data.put("lastLongitude", lastLongitude);
            data.put("lastLatitude", lastLatitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HeaderRequest userCreationRequest = new HeaderRequest
                (method, url, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        int id = -1;

                        try {
                            id = response.getJSONObject("data").getInt("id");
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }

                        id_user = id;

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json, application/hal+json");
                return params;
            }
        };

        VolleyController.getInstance(this).addToRequestQueue(userCreationRequest);

    }

    private void createPeopleButtonListener() {
        final ImageButton mShowDialog = (ImageButton) findViewById(R.id.peopleButton);
        mShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
                importUsers();
                UserAdapter adapter = new UserAdapter(getApplicationContext(), 0, userList);
                mBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                View mView = getLayoutInflater().inflate(R.layout.content_user_list, null);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });
    }

    void importUsers() {
        int method = Request.Method.GET;
        meetupHash = "98c06bfb82ad425e845057d2b2129c83";
        String url = "http://dry-cherry.herokuapp.com/api/meetups/" + meetupHash + "/users";
        Log.e("url", url);
        HeaderRequest retrieveUsersOnMeetupRequest = new HeaderRequest
                (method, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray usersJson = response.getJSONObject("data").getJSONObject("_embedded").getJSONArray("users");
                            JSONObject userJson; //one user
                            for (int i = 0; i < usersJson.length(); i++) {
                                userJson = (JSONObject) usersJson.get(i);
                                String nickname = userJson.getString("nickname");
                                String status = userJson.getString("status");
                                //retrieve link for picture: first google, if it doesn't exist -> gravatar, if it doesn't exist -> emoji
                                String profileURL = userJson.getString("googlePictureURI"); //googlePictureURI gravatarURI
                                if (profileURL.equals("null")){
                                    profileURL = userJson.getString("gravatarURI");
                                    if (profileURL.equals("null")){
                                        profileURL="emoji";
                                    }
                                }
                                User u = new User(nickname, status, profileURL);
                                userList.add(u);
                            }
                        } catch (Exception je) {
                            je.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Accept", "application/json, application/hal+json");
                return params;
            }
        };
        VolleyController.getInstance(this).addToRequestQueue(retrieveUsersOnMeetupRequest);
    }


    private void sendMeetupPinLocation() {
        RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
        final String url = "http://dry-cherry.herokuapp.com/api/meetups/" + meetupHash;

        Meetup meetup = new Meetup();
        meetup.setPinLongitude(pinLongitude);
        meetup.setPinLatitude(pinLatitude);

        GsonRequest<Meetup> request = new GsonRequest<>(
                Request.Method.PATCH, url, meetup, Meetup.class,

                new Response.Listener<Meetup>() {

                    @Override
                    public void onResponse(Meetup meetup) {
                        // TODO: Update shared prefs and data holder
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HttpRequestHelper.handleErrorResponse(error.networkResponse, MapActivity.this);
                    }
                });
        queue.add(request);
    }

}