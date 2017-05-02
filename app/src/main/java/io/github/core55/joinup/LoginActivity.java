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

    private TextView mTextView;
    String token;
    String jwt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

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
        final String URL = "http://192.168.0.112:8080/api/login";

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

                            HashMap<String,String> user = new Gson().fromJson(data.toString(), new TypeToken<HashMap<String, String>>(){}.getType());
                            DataHolder.getInstance().setUser(user);
                            DataHolder.getInstance().setJwt(jwt);

                            Log.d("PEW", DataHolder.getInstance().getUser().get("nickname"));

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
