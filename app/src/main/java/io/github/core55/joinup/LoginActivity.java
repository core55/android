package io.github.core55.joinup;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
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

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_GET_TOKEN = 9002;
    public SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Set the dimensions of the sign-in button.
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
            // [START get_id_token]
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                String idToken = result.getSignInAccount().getIdToken();
                Log.d("PEW", idToken);

                loginGoogleBackend(idToken);
            }
            // [END get_id_token]

        }
    }

    private void loginBackend(String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String URL = "https://dry-cherry.herokuapp.com/api/login";

        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        HeaderRequest request_json = new HeaderRequest(Request.Method.POST,
                URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONObject headers = response.getJSONObject("headers");
                            String jwt = headers.getString("Authorization");

                            HashMap<String, String> user = new Gson().fromJson(data.toString(), new TypeToken<HashMap<String, String>>() {
                            }.getType());
                            DataHolder.getInstance().setUser(user);
                            DataHolder.getInstance().setJwt(jwt);

                            Intent intent = new Intent(LoginActivity.this,
                                    MapActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                });

        queue.add(request_json);
    }

    private void loginGoogleBackend(String idToken) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String URL = "https://dry-cherry.herokuapp.com/api/login/token";

        HashMap<String, String> params = new HashMap<>();
        params.put("idToken", idToken);

        HeaderRequest request_json = new HeaderRequest(Request.Method.POST,
                URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("PEW", response.toString());

                        try {
                            JSONObject data = response.getJSONObject("data");
                            JSONObject headers = response.getJSONObject("headers");
                            String jwt = headers.getString("Authorization");

                            HashMap<String, String> user = new Gson().fromJson(data.toString(), new TypeToken<HashMap<String, String>>() {
                            }.getType());
                            DataHolder.getInstance().setUser(user);
                            DataHolder.getInstance().setJwt(jwt);

                            sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(getString(R.string.user_username), data.getString("username"));
                            editor.putString(getString(R.string.user_nickname), data.getString("nickname"));
                            editor.putString(getString(R.string.jwt_string), jwt);
                            editor.commit();


                            Intent intent = new Intent(LoginActivity.this,
                                    CreateActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e("Error: ", error.getMessage());
                    }
                });

        queue.add(request_json);
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
}
