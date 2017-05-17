/*
  Authors: Simone Stefani
 */

package io.github.core55.joinup.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import io.github.core55.joinup.Helper.GsonRequest;
import io.github.core55.joinup.Helper.HttpRequestHelper;
import io.github.core55.joinup.Model.AccountCredentials;
import io.github.core55.joinup.Model.AuthenticationResponse;
import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.R;

public class RegisterActivity extends AppCompatActivity {

    private final String API_BASE_URL = "https://dry-cherry.herokuapp.com/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerOnClickListener();
        registerOnTouchListener();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, WelcomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    private void registerOnClickListener() {

        // Register button
        Button mRegisterButton = (Button) findViewById(R.id.register_main_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerOnTouchListener() {

        // Redirect to login page
        TextView mRedirectToWelcome = (TextView) findViewById(R.id.register_redirect_login);
        mRedirectToWelcome.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }

    /**
     * adds registration fields and creates an account
     */
    private void registerUser() {

        // Get register name
        EditText mRegisterNameField = (EditText) findViewById(R.id.register_name_field);
        String nickname = mRegisterNameField.getText().toString();

        // Get register email
        EditText mRegisterEmailField = (EditText) findViewById(R.id.register_email_field);
        String email = mRegisterEmailField.getText().toString();

        // Get register password
        EditText mRegisterPasswordField = (EditText) findViewById(R.id.register_password_field);
        String password = mRegisterPasswordField.getText().toString();

        AccountCredentials credentials = new AccountCredentials(email, password);
        credentials.setNickname(nickname);

        if (DataHolder.getInstance().isAnonymous()) {
            credentials.setOldUsername(DataHolder.getInstance().getUser().getUsername());
        }

        sendRegistrationRequest(credentials);
    }

    /**
     * sends a Gson request to the backend
     * @param credentials is account credentials
     */
    private void sendRegistrationRequest(AccountCredentials credentials) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = API_BASE_URL + "register/send";

        GsonRequest<AuthenticationResponse> request = new GsonRequest<>(
                Request.Method.POST, url, credentials, AuthenticationResponse.class,

                new Response.Listener<AuthenticationResponse>() {

                    @Override
                    public void onResponse(AuthenticationResponse authenticationResponse) {
                        Intent intent = new Intent(RegisterActivity.this, EmailNotConfirmedActivity.class);
                        startActivity(intent);
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        HttpRequestHelper.handleErrorResponse(error.networkResponse, RegisterActivity.this);
                    }
                });
        queue.add(request);
    }
}
