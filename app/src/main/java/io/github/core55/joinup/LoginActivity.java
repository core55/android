package io.github.core55.joinup;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
    protected void emailLogin (){
        EditText emailEditText = (EditText) findViewById(R.id.etUsername);
        String email =  emailEditText.getText().toString();

        EditText passwordEditText = (EditText) findViewById(R.id.etPassword);
        String password =  emailEditText.getText().toString();
    }
}
