package io.github.core55.joinup;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import android.net.Uri;

import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    String meetupHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // Handle app links
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if (appLinkData != null && appLinkData.isHierarchical()) {
            String uri = appLinkIntent.getDataString();
            Log.i("JoinUp", "Deep link clicked " + uri);

            Pattern pattern = Pattern.compile("/meetups/(.*?)");
            Matcher matcher = pattern.matcher(uri);
            if (matcher.find()) {
                meetupHash = matcher.group(1);
                Log.i("JoinUp", "Meetup hash " + meetupHash);
            }

        }

        // start maps activity
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("meetupHash",meetupHash);
        startActivity(intent);
    }

}
