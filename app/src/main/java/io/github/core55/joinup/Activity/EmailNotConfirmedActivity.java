package io.github.core55.joinup.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import io.github.core55.joinup.R;

public class EmailNotConfirmedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_not_confirmed);

        registerOnTouchListener();
    }

    private void registerOnTouchListener() {
        // Redirect to welcome page
        TextView mRedirectToWelcome = (TextView) findViewById(R.id.email_confirm_redirect_welcome);
        mRedirectToWelcome.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(EmailNotConfirmedActivity.this, WelcomeActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
}
