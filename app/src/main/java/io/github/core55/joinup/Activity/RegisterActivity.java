package io.github.core55.joinup.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.Helper.AuthenticationHelper;
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

        if (DataHolder.getInstance().isAnonymous()) {
            credentials.setOldUsername(DataHolder.getInstance().getUser().getUsername());
        }

        sendRegistrationRequest(credentials);
    }

    private void sendRegistrationRequest(AccountCredentials credentials) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = API_BASE_URL + "register/send";

        GsonRequest<AuthenticationResponse> request = new GsonRequest<>(
                Request.Method.POST, url, credentials, AuthenticationResponse.class,

                new Response.Listener<AuthenticationResponse>() {

                    @Override
                    public void onResponse(AuthenticationResponse authenticationResponse) {
                        User user = authenticationResponse.getUser();
                        String jwt = authenticationResponse.getJwt();

                        AuthenticationHelper.persistAuthenticatedUser(RegisterActivity.this, user, jwt);

                        AuthenticationHelper.syncDataHolder(RegisterActivity.this);

                        Intent intent = new Intent(RegisterActivity.this, CreateActivity.class);
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
