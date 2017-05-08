package io.github.core55.joinup;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

    }

    protected void createMeetup(View v) {
        Intent i = new Intent(this, CreateActivity.class);
        startActivity(i);
    }

    protected void loginActivity(View v) {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}
