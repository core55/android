package io.github.core55.joinup;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;

import android.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lucasurbas.listitemview.ListItemView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String TAG = "MapActivity";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private GoogleMap mMap;

    private LocationManager locationManager;

    private String meetupHash = "6f41e80d685a409eb144a3a9c76ce97e";

    private HashMap<Long, MarkerOptions> markersOnMap = new HashMap<>();


    private String mActivityTitle;
    private String[] mMeetups = {"Hola", "Hej", "Hello", "Bonjour","Gurka", "Mandelbrot"};
    private ListView lv;
    private ArrayList userList = new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        handleAppLink();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        askLocationPermission();

        locationManager = new LocationManager(this);
        locationManager.start();

        launchNetworkService();
        createShareButtonListener();
        createPeopleButtonListener();


        mActivityTitle = getTitle().toString();


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(NetworkService.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(networkServiceReceiver, filter);
        // or `registerReceiver(networkServiceReceiver, filter)` for a normal broadcast
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener when the application is paused
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkServiceReceiver);
        // or `unregisterReceiver(networkServiceReceiver)` for a normal broadcast
    }

    // Define the callback for what to do when message is received
    private BroadcastReceiver networkServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Meetup m = intent.getParcelableExtra("meetup");
            if (m != null) {
                LatLng latLng = new LatLng(m.getPinLatitude(), m.getPinLongitude());
                MarkerOptions meetupMarker = new MarkerOptions();
                meetupMarker.position(new LatLng(m.getPinLatitude(), m.getPinLongitude()));
                meetupMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.meetup));
                mMap.addMarker(meetupMarker);

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

                locationManager.getLocation();


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

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // show blue dot on map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        Context context = getApplicationContext();
        CharSequence text = "Welcome " + DataHolder.getInstance().getUser().get("nickname") + "!";
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    /**
     *
     */
    public void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
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

            Pattern pattern = Pattern.compile("/\\#/(.*)");
            Matcher matcher = pattern.matcher(uri);
            if (matcher.find()) {
                meetupHash = matcher.group(1);
                Log.d(TAG, "hash = " + meetupHash);
            }
        }
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
               // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

    private void createPeopleButtonListener() {
        final ImageButton mShowDialog = (ImageButton) findViewById(R.id.peopleButton);
        mShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapActivity.this);
                importUsers();
                UserAdapter adapter = new UserAdapter(getApplicationContext(),0, userList);
                mBuilder.setAdapter(adapter,  new DialogInterface.OnClickListener() {
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

}


