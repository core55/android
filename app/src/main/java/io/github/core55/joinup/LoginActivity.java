package io.github.core55.joinup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    protected void ordinaryLogin(View v) {
        EditText emailEditText = (EditText) findViewById(R.id.login_email);
        String email = emailEditText.getText().toString();

        EditText passwordEditText = (EditText) findViewById(R.id.login_password);
        String password = passwordEditText.getText().toString();

        loginBackend(email, password);
    }

    // TODO: Implement login with Google Sign-in
    protected void googleLogin(View v) {

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
}
