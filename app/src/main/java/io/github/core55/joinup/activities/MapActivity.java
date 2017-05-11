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
import com.google.gson.Gson;
import com.mikepenz.materialdrawer.Drawer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.helpers.AuthenticationHelper;
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
    public static final String API_URL = "https://dry-cherry.herokuapp.com/api/";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;

    private LocationManager locationManager;
    private HashMap<Long, MarkerOptions> markersOnMap = new HashMap<>();

    private MarkerOptions meetupMarker;
    private Marker meetupMarkerView;

    private Double lat;
    private Double lon;

    private ArrayList userList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        AuthenticationHelper.syncDataHolder(this);

        // Inject the navigation drawer
        NavigationDrawer.buildDrawer(this);

        // Retrieve map hash from applink
        handleAppLink();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationHelper.askLocationPermission(this);

        locationManager = new LocationManager(this);
        locationManager.start();

        createShareButtonListener();
        createPeopleButtonListener();

        if (DataHolder.getInstance().isAnonymous() && DataHolder.getInstance().getUser().getNickname() == null) {
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

            lat = intent.getDoubleExtra("lat", -1);
            lon = intent.getDoubleExtra("lon", -1);

            if (lat != null && lat != -1 && lon != null && lon != -1) {

                RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
                final String url = "https://dry-cherry.herokuapp.com/api/users/" + DataHolder.getInstance().getUser().getId();

                User user = new User(lon, lat);

                GsonRequest<User> request = new GsonRequest<>(
                        Request.Method.PATCH, url, user, User.class,

                        new Response.Listener<User>() {

                            @Override
                            public void onResponse(User user) {
                                DataHolder.getInstance().setUser(user);
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
        Meetup meetup = DataHolder.getInstance().getMeetup();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(meetup.getCenterLatitude(), meetup.getCenterLongitude()),
                meetup.getZoomLevel()));

        if (meetupMarker == null && meetupMarkerView == null && meetup.getPinLatitude() != -1 && meetup.getPinLongitude() != -1) {
            meetupMarker = new MarkerOptions().draggable(true);
            meetupMarker.position(new LatLng(meetup.getPinLatitude(), meetup.getPinLongitude()));
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

        // show blue dot on map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        if (DataHolder.getInstance().getUser().getNickname() != null) {
            Context context = getApplicationContext();
            CharSequence text = "Welcome " + DataHolder.getInstance().getUser().getNickname() + "!";
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
                sendMeetupPinLocation(marker.getPosition().longitude, marker.getPosition().latitude);
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
                String applinkHash = matcher.group(1);
                fetchMeetup(applinkHash);
                Log.d(TAG, "hash = " + DataHolder.getInstance().getMeetup().getHash());

                User user;
                if (DataHolder.getInstance().isAuthenticated() || DataHolder.getInstance().isAnonymous()) {
                    user = DataHolder.getInstance().getUser();
                } else {
                    // TODO: Handle error if coordinates are not available
                    user = new User(lon, lat);
                }

                linkUserToMeetup(user);

                if (DataHolder.getInstance().isAnonymous() && DataHolder.getInstance().getUser().getNickname() == null) {
                    namePrompt();
                } else if (!DataHolder.getInstance().isAuthenticated() && !DataHolder.getInstance().isAnonymous()) {
                    namePrompt();
                }
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

                    patchNickname(nickname);
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

    public void patchNickname(String nickname) {
        RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
        final String url = "https://dry-cherry.herokuapp.com/api/users/" + DataHolder.getInstance().getUser().getId();

        User user = new User();
        user.setNickname(nickname);

        GsonRequest<User> request = new GsonRequest<>(
                Request.Method.PATCH, url, user, User.class,

                new Response.Listener<User>() {

                    @Override
                    public void onResponse(User user) {
                        DataHolder.getInstance().setUser(user);
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
                url.setText(DataHolder.getInstance().getMeetup().getHash());

                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });
    }

    public void copyToCliboard(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", DataHolder.getInstance().getMeetup().getHash());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Link is copied!", Toast.LENGTH_SHORT).show();
    }

    public void launchNetworkService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, NetworkService.class);

        i.putExtra("hash", DataHolder.getInstance().getMeetup().getHash());

        // Start the service
        startService(i);
    }

    private void createUser() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String hash = DataHolder.getInstance().getMeetup().getHash();
        final String url = "http://dry-cherry.herokuapp.com/api/meetups/" + hash + "/users/save";

        User user = new User(lon,lat);

        GsonRequest<User> request = new GsonRequest<>(
                Request.Method.POST, url, user, User.class,

                new Response.Listener<User>() {

                    @Override
                    public void onResponse(User user) {
                        DataHolder.getInstance().setUser(user);
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

    private void fetchMeetup(String hash) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "http://dry-cherry.herokuapp.com/api/meetups/" + hash;

        GsonRequest<Meetup> request = new GsonRequest<>(
                Request.Method.GET, url, Meetup.class,

                new Response.Listener<Meetup>() {

                    @Override
                    public void onResponse(Meetup meetup) {
                        DataHolder.getInstance().setMeetup(meetup);
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
        String[] nicknames = {"Hussam", "Patrick", "Marcel", "Phillip", "Simone", "Dean", "Juan Luis", "Jiho", "Pepe", "Pablo"};
        String[] status = {"Hello, its me", "Maple syrup", "applestrudel", "biscuits please, not cookies", "Planet-tricky", "Baras", "biscuits please, not cookies", "biscuits please, not cookies", "biscuits please, not cookies", "biscuits please, not cookies"};
        int[] profilePictures = {R.drawable.emoji_1, R.drawable.emoji_2, R.drawable.emoji_3, R.drawable.emoji_4, R.drawable.emoji_1, R.drawable.emoji_2, R.drawable.emoji_2, R.drawable.emoji_1, R.drawable.emoji_3, R.drawable.emoji_4};
        for (int i = 0; i < nicknames.length; i++) {
            User u = new User(nicknames[i], status[i], profilePictures[i]);
            userList.add(u);
        }
    }

    private void sendMeetupPinLocation(Double pinLongitude, Double pinLatitude) {
        RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
        final String url = "http://dry-cherry.herokuapp.com/api/meetups/" + DataHolder.getInstance().getMeetup().getHash();

        Meetup meetup = new Meetup();
        meetup.setPinLongitude(pinLongitude);
        meetup.setPinLatitude(pinLatitude);

        GsonRequest<Meetup> request = new GsonRequest<>(
                Request.Method.PATCH, url, meetup, Meetup.class,

                new Response.Listener<Meetup>() {

                    @Override
                    public void onResponse(Meetup meetup) {
                        DataHolder.getInstance().setMeetup(meetup);
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

    private void linkUserToMeetup(User user) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String hash = DataHolder.getInstance().getMeetup().getHash();
        final String url = "http://dry-cherry.herokuapp.com/api/meetups/" + hash + "/users/save";

        GsonRequest<User> request = new GsonRequest<>(
                Request.Method.POST, url, user, User.class,

                new Response.Listener<User>() {

                    @Override
                    public void onResponse(User user) {
                        setSharedPreferences(new Gson().toJson(user));
                        DataHolder.getInstance().setUser(user);
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

    private void setSharedPreferences(String user) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.anonymous_user), user);
        editor.commit();
    }
}