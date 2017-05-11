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
        Button btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMeetup(v);
            }
        });

        Button btn_login_welcome = (Button) findViewById(R.id.btn_login_welcome);
        btn_login_welcome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginActivity(v);
            }
        });
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
