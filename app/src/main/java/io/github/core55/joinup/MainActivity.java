package io.github.core55.joinup;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.net.Uri;
import android.util.Log;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;





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

            Pattern pattern = Pattern.compile("/meetups/(.*)");
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

        ImageButton mShowDialog = (ImageButton) findViewById(R.id.imageButton);
        mShowDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_share, null);
                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            }

        });
    }



    public void copyToCliboard(View v) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", "it actually works!!!");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this,"Link is copied!", Toast.LENGTH_SHORT).show();
    }

    }




