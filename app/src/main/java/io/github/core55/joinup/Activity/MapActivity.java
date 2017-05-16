/*
  Authors:Juan, Patrick, S.Stefani and Hussam
 */
package io.github.core55.joinup.Activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.github.core55.joinup.Entity.Meetup;
import io.github.core55.joinup.Entity.User;

import io.github.core55.joinup.Helper.DrawerFragment;
import io.github.core55.joinup.Helper.OutOfBoundsHelper;
import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.Helper.AuthenticationHelper;
import io.github.core55.joinup.Helper.CircleTransform;
import io.github.core55.joinup.Helper.GsonRequest;
import io.github.core55.joinup.Helper.HttpRequestHelper;
import io.github.core55.joinup.Helper.LocationHelper;
import io.github.core55.joinup.Helper.NavigationDrawer;
import io.github.core55.joinup.Helper.OutOfBoundsHelper;
import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.R;
import io.github.core55.joinup.Service.LocationManager;
import io.github.core55.joinup.Service.LocationService;
import io.github.core55.joinup.Service.NetworkService;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "MapActivity";
    public static final String API_URL = "https://dry-cherry.herokuapp.com/api/";
    public static final String WEBAPP_URL = "https://culater.herokuapp.com/#/";
    private static final String WEBAPP_URL_PREFIX = "m/";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;

    private LocationManager locationManager;
    private HashMap<Long, Marker> markersHashMap = new HashMap<>();
    private HashMap<Long, Bitmap> bmpPictureHashMap = new HashMap<>();

    private MarkerOptions meetupMarker;
    private Marker meetupMarkerView;

    private Double lat;
    private Double lon;

    private HashMap<Long, TextView> outOfBoundsIndicators = new HashMap<>();
    private RelativeLayout outOfBoundsViewGroup;
    private long previousOutOfBoundsUpdateTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        AuthenticationHelper.syncDataHolder(this);
        AuthenticationHelper.authenticationLogger(this);

        // Inject the navigation drawer
        /*NavigationDrawer.buildDrawer(this, true);*/ //We use DrawerFragment now
        LocationHelper.askLocationPermission(this);

        //instantiates drawer, puts it in the dataholder and creates fragment with it
        DrawerFragment drawer =  DrawerFragment.Companion.newInstance("DrawerFragment",DataHolder.getInstance(),this);
        DataHolder.getInstance().setDrawer(drawer);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_drawer_container,DataHolder.getInstance().getDrawer())
                .commit();

        // get the view wrapper
        this.outOfBoundsViewGroup = (RelativeLayout) findViewById(R.id.outOfBoundsIndicators);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = new LocationManager(this);
        locationManager.start();

        createShareButtonListener();
        createSwitchListener();

        if (DataHolder.getInstance().getUser() != null && DataHolder.getInstance().getUser().getNickname() == null) {
            namePrompt();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AuthenticationHelper.syncDataHolder(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkServiceReceiver);
        // or `unregisterReceiver(networkServiceReceiver)` for a normal broadcast
        AuthenticationHelper.syncSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(NetworkService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(networkServiceReceiver, filter);

        IntentFilter filter2 = new IntentFilter(LocationService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationReceiver, filter2);
        AuthenticationHelper.syncDataHolder(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AuthenticationHelper.syncSharedPreferences(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AuthenticationHelper.syncSharedPreferences(this);
    }

    // TODO: fix
    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            lat = intent.getDoubleExtra("lat", -1);
            lon = intent.getDoubleExtra("lon", -1);

            if (lat != -1 && lon != -1 && (DataHolder.getInstance().getUser() != null)) {

                RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
                final String url = API_URL + "users/" + DataHolder.getInstance().getUser().getId();

                User user = new User(lon, lat);
                user.setNickname(DataHolder.getInstance().getUser().getNickname());

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

            Meetup m = DataHolder.getInstance().getMeetup();
            if (m != null) {
                if (meetupMarker == null && meetupMarkerView == null && m.getPinLatitude() != null && m.getPinLongitude() != null) {
                    meetupMarker = new MarkerOptions().draggable(true);
                    meetupMarker.position(new LatLng(m.getPinLatitude(), m.getPinLongitude()));
                    meetupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_meetup));
                    meetupMarkerView = mMap.addMarker(meetupMarker);
                } else if (meetupMarker != null && meetupMarkerView != null && m.getPinLatitude() != null && m.getPinLongitude() != null) {
                    meetupMarker.position(new LatLng(m.getPinLatitude(), m.getPinLongitude()));
                    meetupMarkerView.setPosition(new LatLng(m.getPinLatitude(), m.getPinLongitude()));
                }
            }

            List<User> users = DataHolder.getInstance().getUserList();

            for (final User u : users) {
                if (u == null) {
                    return;
                }

                if (markersHashMap.containsKey(u.getId())) {

                    Marker marker = markersHashMap.get(u.getId());
                    marker.setTitle(u.getNickname());
                    LatLng lastLatLng = new LatLng(u.getLastLatitude(), u.getLastLongitude());

                    //smooth-marker movement
                    animateMarker(marker, lastLatLng, false);

                    /*********** Update pin color ***********/
                    long epoch = System.currentTimeMillis() / 1000; // in seconds

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    long updatedAt = 0;
                    try {
                        updatedAt = df.parse(u.getUpdatedAt()).getTime() / 1000; // in seconds
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "epoch = " + epoch);
                    Log.d(TAG, "updatedAt = " + updatedAt);

                    Bitmap bmpPin;

                    if (epoch - updatedAt < 5 * 60) {
                        // green pin if active in the last 5 min
                        bmpPin = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_green);
                        Log.d(TAG, "green");
                    } else if (epoch - updatedAt < 20 * 60) {
                        // yellow pin if active in the last 20 min
                        bmpPin = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_yellow);
                        Log.d(TAG, "yellow");
                    } else {
                        // red pin otherwise
                        bmpPin = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_red);
                        Log.d(TAG, "red");
                    }

                    if (bmpPictureHashMap.get(u.getId()) == null) {
                        // no picture
                        Bitmap bmpCanvas = Bitmap.createBitmap(bmpPin.getWidth(), bmpPin.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas1 = new Canvas(bmpCanvas);
                        canvas1.drawBitmap(bmpPin, 0, 0, null);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmpCanvas));
                    } else {
                        // picture is available
                        Bitmap bmpCanvas = Bitmap.createBitmap(bmpPin.getWidth(), bmpPin.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas1 = new Canvas(bmpCanvas);
                        canvas1.drawBitmap(bmpPin, 0, 0, null);
                        Bitmap scaledPicture = Bitmap.createScaledBitmap(bmpPictureHashMap.get(u.getId()), bmpPin.getWidth() - 10, bmpPin.getWidth() - 10, false);
                        canvas1.drawBitmap(scaledPicture, 5, 5, null);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmpCanvas));
                    }


                } else {
                    Log.d(TAG, "pin create");
                    MarkerOptions newMarker = new MarkerOptions();
                    newMarker.position(new LatLng(u.getLastLatitude(), u.getLastLongitude()));
                    newMarker.title(u.getNickname());

                    if (u.getNickname() == null) {
                        newMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_anon));
                        Marker marker = mMap.addMarker(newMarker);
                        markersHashMap.put(u.getId(), marker);
                    } else {

                        String pictureURL;

                        if (u.getGooglePictureURI() != null) {
                            pictureURL = u.getGooglePictureURI();
                        } else if (u.getGravatarURI() != null) {
                            pictureURL = u.getGravatarURI();
                        } else {
                            // no google or gravatar picture, so display the default marker and return
                            newMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_default));
                            Marker marker = mMap.addMarker(newMarker);
                            markersHashMap.put(u.getId(), marker);
                            return;
                        }

                        ImageRequest imageRequest = new ImageRequest(pictureURL,
                                new Response.Listener<Bitmap>() {
                                    @Override
                                    public void onResponse(Bitmap response) {
                                        Log.d(TAG, "imageRequest:onResponse");
                                        CircleTransform circleTransform = new CircleTransform();
                                        bmpPictureHashMap.put(u.getId(), circleTransform.transform(response));
                                    }
                                }, 0, 0, null, null,
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });

                        RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
                        queue.add(imageRequest);

                        if (bmpPictureHashMap.get(u.getId()) == null) {
                            return;
                        }

                        Bitmap bmpPin = BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_default);
                        Bitmap bmpCanvas = Bitmap.createBitmap(bmpPin.getWidth(), bmpPin.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas1 = new Canvas(bmpCanvas);
                        canvas1.drawBitmap(bmpPin, 0, 0, null);
                        Bitmap scaledPicture = Bitmap.createScaledBitmap(bmpPictureHashMap.get(u.getId()), bmpPin.getWidth() - 10, bmpPin.getWidth() - 10, false);
                        canvas1.drawBitmap(scaledPicture, 5, 5, null);
                        newMarker.icon(BitmapDescriptorFactory.fromBitmap(bmpCanvas));

                        Marker marker = mMap.addMarker(newMarker);
                        markersHashMap.put(u.getId(), marker);

                    }
                }


            }

        }
    };

    /**
     * This method is in charge of animating the marker movement, when we move on the map,
     * the marker will update position and will move towards desired destination when we update the view.
     *
     * @param marker     the marker we are animating
     * @param toPosition the position to move to
     * @param hideMarker marker visibility
     */
    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

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

        if (meetupMarker == null && meetupMarkerView == null
                && meetup.getPinLatitude() != null && meetup.getPinLongitude() != null) {
            meetupMarker = new MarkerOptions().draggable(true);
            meetupMarker.position(new LatLng(meetup.getPinLatitude(), meetup.getPinLongitude()));
            meetupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_meetup));
            meetupMarkerView = mMap.addMarker(meetupMarker);
        }

        try {
            // Customise map styling via json
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_styles));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // disable rotation gestures, because they are not reflected in the bounds
        // of the visible area. avoids false out of bound indicators.
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        // show blue dot on map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
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
                updateMeetupPinLocation(marker.getPosition().longitude, marker.getPosition().latitude);
            }
        });

        launchNetworkService();

        // User out of bounds indicators
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                long timestamp = new Date().getTime();
                if (timestamp - previousOutOfBoundsUpdateTimestamp < 10) {
                    return;
                }

                previousOutOfBoundsUpdateTimestamp = timestamp;
                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                DataHolder data = DataHolder.getInstance();
                CameraPosition camera = mMap.getCameraPosition();

                // skip if no data available
                if (data.getMeetup() == null || data.getUserList() == null) {
                    return;
                }

                List<User> userList = data.getUserList();

                for (User user : userList) {
                    Marker marker = markersHashMap.get(user.getId());

                    // skip if marker was not put on map yet
                    if (marker == null) continue;

                    // if marker visible, remove indicator if present
                    if (bounds.contains(marker.getPosition())) {
                        if (outOfBoundsIndicators.containsKey(user.getId())) {
                            TextView indicator = outOfBoundsIndicators.get(user.getId());
                            indicator.setVisibility(View.GONE);
                        }

                        continue;
                    }

                    String nickname = user != null ? user.getNickname() : "Anonymous";

                    // create indicator if not already present
                    if (!outOfBoundsIndicators.containsKey(user.getId())) {
                        TextView indicator = OutOfBoundsHelper.generatePositionIdicator(nickname, View.generateViewId(), getBaseContext());
                        outOfBoundsIndicators.put(user.getId(), indicator);
                        outOfBoundsViewGroup.addView(indicator);
                    }

                    // reposition and update indicator
                    TextView indicator = outOfBoundsIndicators.get(user.getId());
                    indicator.setText(nickname);
                    OutOfBoundsHelper.setIndicatorPosition(bounds, marker, indicator, camera);
                    indicator.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void launchNetworkService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, NetworkService.class);

        // Start the service
        startService(i);
    }

    /**
     * A name prompt is displayed to the non-registered users
     */
    private void namePrompt() {

        // Build modal to fill in user nickname
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_name, null);
        mBuilder.setView(mView);

        final EditText name = (EditText) mView.findViewById(R.id.enter_name);

        // Prompt modal
        final AlertDialog dialog = mBuilder.create();

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

                    updateNickname(nickname);
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

    /**
     * The nicknames are updated in the database
     *
     * @param nickname is the input nickname
     */
    public void updateNickname(String nickname) {
        RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
        final String url = API_URL + "users/" + DataHolder.getInstance().getUser().getId();

        User user = new User();
        user.setNickname(nickname);

        GsonRequest<User> request = new GsonRequest<>(
                Request.Method.PATCH, url, user, User.class,

                new Response.Listener<User>() {

                    @Override
                    public void onResponse(User user) {
                        DataHolder.getInstance().setUser(user);
                        AuthenticationHelper.syncSharedPreferences(MapActivity.this);
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

    /**
     * when clicking on the share button, a dialog is built
     * the dialog shows the
     */
    private void createShareButtonListener() {
        ImageButton mShowDialog = (ImageButton) findViewById(R.id.imageButton);
        mShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_share, null);
                mBuilder.setView(mView);

                EditText url = (EditText) mView.findViewById(R.id.location_search_field);
                String meetupLink = WEBAPP_URL + WEBAPP_URL_PREFIX + DataHolder.getInstance().getMeetup().getHash();
                url.setText(meetupLink);

                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });
    }

    /**
     * The switch is responsible for controlling the location update
     * The switch is colored-green by default which indicates that the updates are on.
     */
    private void createSwitchListener() {
        Switch toggle = (Switch) findViewById(R.id.toggleSwitch);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //enable location for users
                    locationManager.restart();
                } else {
                    //disable location for users
                    locationManager.stop();
                }
            }
        });
    }

    /**
     * The method is used to copy the provided link to the clipboard when clicking on the copy button
     *
     * @param v is the current view
     */
    public void copyToCliboard(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String meetupLink = WEBAPP_URL + WEBAPP_URL_PREFIX + DataHolder.getInstance().getMeetup().getHash();
        ClipData clip = ClipData.newPlainText("label", meetupLink);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Link is copied!", Toast.LENGTH_SHORT).show();
    }


    private void updateMeetupPinLocation(Double pinLongitude, Double pinLatitude) {
        RequestQueue queue = Volley.newRequestQueue(MapActivity.this);
        final String url = API_URL + "meetups/" + DataHolder.getInstance().getMeetup().getHash();

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

    public void centerMapOnMarker(Long id){
        if (markersHashMap.containsKey(id)){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markersHashMap.get(id).getPosition(),14));
        }

    }
}
