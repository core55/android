package io.github.core55.joinup.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.github.core55.joinup.Model.AccountCredentials;
import io.github.core55.joinup.Model.AuthenticationResponse;
import io.github.core55.joinup.Model.GoogleToken;
import io.github.core55.joinup.entities.User;
import io.github.core55.joinup.helpers.AuthenticationHelper;
import io.github.core55.joinup.helpers.DataHolder;
import io.github.core55.joinup.helpers.GsonRequest;
import io.github.core55.joinup.helpers.HeaderRequest;
import io.github.core55.joinup.R;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final String TAG = "LoginActivity";

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_GET_TOKEN = 9002;
    public SharedPreferences sharedPref;

    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AuthenticationHelper.syncDataHolder(this);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Set the dimensions of the Google sign-in button.
        SignInButton signInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        registerOnClickListener();
    }

    private void registerOnClickListener() {
        Button btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordinaryLogin(v);
            }
        });
    }

    protected void ordinaryLogin(View v) {
        EditText emailEditText = (EditText) findViewById(R.id.login_email);
        String email = emailEditText.getText().toString();

        EditText passwordEditText = (EditText) findViewById(R.id.login_password);
        String password = passwordEditText.getText().toString();

        loginBackend(email, password);
    }

    protected void googleLogin() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_TOKEN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                String idToken = result.getSignInAccount().getIdToken();

                loginGoogleBackend(idToken);
            }
        }
    }

    private void loginBackend(String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "https://dry-cherry.herokuapp.com/api/login";

        AccountCredentials credentials = new AccountCredentials(username, password);

        GsonRequest<AuthenticationResponse> request = new GsonRequest<>(
                Request.Method.POST, url, credentials, AuthenticationResponse.class,
                new Response.Listener<AuthenticationResponse>() {

                    @Override
                    public void onResponse(AuthenticationResponse authenticationResponse) {
                        User user = authenticationResponse.getUser();
                        String jwt = authenticationResponse.getJwt();

                        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.current_user), gson.toJson(user));
                        editor.putString(getString(R.string.jwt_string), jwt);
                        editor.commit();

                        DataHolder.getInstance().setUser(user);
                        DataHolder.getInstance().setJwt(jwt);

                        Intent intent = new Intent(LoginActivity.this,
                                CreateActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;

                        if (response != null && response.data != null) {
                            String json = new String(response.data);
                            String message = trimMessage(json, "message");

                            switch (response.statusCode) {
                                case 422:
                                    if (message != null) displayMessage(message);
                                    break;
                                case 401:
                                    if (message != null) displayMessage(message);
                                    break;
                            }
                        }
                    }
                });
        queue.add(request);
    }

    private void loginGoogleBackend(String idToken) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "https://dry-cherry.herokuapp.com/api/login/token";

        GoogleToken googleToken = new GoogleToken(idToken);

        GsonRequest<AuthenticationResponse> request = new GsonRequest<>(
                Request.Method.POST, url, googleToken, AuthenticationResponse.class,
                new Response.Listener<AuthenticationResponse>() {

                    @Override
                    public void onResponse(AuthenticationResponse authenticationResponse) {
                        User user = authenticationResponse.getUser();
                        String jwt = authenticationResponse.getJwt();

                        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.current_user), gson.toJson(user));
                        editor.putString(getString(R.string.jwt_string), jwt);
                        editor.commit();

                        DataHolder.getInstance().setUser(user);
                        DataHolder.getInstance().setJwt(jwt);

                        Intent intent = new Intent(LoginActivity.this,
                                CreateActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;

                        if (response != null && response.data != null) {
                            String json = new String(response.data);
                            String message = trimMessage(json, "message");

                            switch (response.statusCode) {
                                case 422:
                                    if (message != null) displayMessage(message);
                                    break;
                                case 401:
                                    if (message != null) displayMessage(message);
                                    break;
                            }
                        }
                    }
                });
        queue.add(request);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_sign_in_button:
                googleLogin();
                break;
        }
    }

    public String trimMessage(String json, String key) {
        String trimmedString = null;

        try {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString(key);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }

    //Somewhere that has access to a context
    public void displayMessage(String toastString) {
        Toast.makeText(this, toastString, Toast.LENGTH_LONG).show();
    }
}
