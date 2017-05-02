package io.github.core55.joinup;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends Activity {

    private TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    protected void skipLogin(View v){
        Intent i = new Intent(this,MapActivity.class);
        startActivity(i);

    }


    protected void emailLogin(View v){
        EditText emailEditText = (EditText) findViewById(R.id.etUsername);
        String email =  emailEditText.getText().toString();

        EditText passwordEditText = (EditText) findViewById(R.id.etPassword);
        String password =  passwordEditText.getText().toString();

        loginBackend(email, password);

    }

    private void loginBackend(String username, String password) {
        RequestQueue queue = Volley.newRequestQueue(this);
        final String URL = "http://130.229.144.78:8080/api/login";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);

        JsonObjectRequest request_json = new JsonObjectRequest(Request.Method.POST,
                URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("PEW", response.toString());
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
