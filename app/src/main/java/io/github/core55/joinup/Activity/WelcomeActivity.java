/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import io.github.core55.joinup.R;
import io.github.core55.joinup.Helper.AuthenticationHelper;
import io.github.core55.joinup.Model.DataHolder;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        AuthenticationHelper.syncDataHolder(this);
        AuthenticationHelper.authenticationLogger(this);

        // If user is authenticated move to create meetup activity
        if (DataHolder.getInstance().isAuthenticated() || DataHolder.getInstance().isAnonymous()) {
            Intent i = new Intent(this, CreateActivity.class);
            startActivity(i);
        }

        registerOnClickListener();
    }

    private void registerOnClickListener() {

        // Create meetup button
        Button mCreateButton = (Button) findViewById(R.id.welcome_create_meetup_button);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMeetup();
            }
        });

        // Login button
        Button mLoginButton = (Button) findViewById(R.id.welcome_login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginActivity();
            }
        });

        // Register button
        Button mRegisterButton = (Button) findViewById(R.id.welcome_register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerActivity();
            }
        });
    }

    protected void createMeetup() {
        Intent i = new Intent(this, CreateActivity.class);
        startActivity(i);
    }

    protected void loginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    protected void registerActivity() {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }
}
