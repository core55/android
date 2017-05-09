/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import io.github.core55.joinup.R;

public class AuthenticationHelper {

    public static void syncDataHolder(Activity activity) {
        final Context context = activity.getApplicationContext();

        SharedPreferences sharedPref = activity.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString(context.getString(R.string.current_user), "Unknown User");
        String jwt = sharedPref.getString(context.getString(R.string.jwt_string), "No JWT");

        if (!currentUser.equals("Unknown User")) {
            HashMap<String, String> parsedUser = new Gson().fromJson(currentUser, new TypeToken<HashMap<String, String>>() {
            }.getType());
            DataHolder.getInstance().setUser(parsedUser);
            DataHolder.getInstance().setJwt(jwt);
            DataHolder.getInstance().setAuthenticated(true);
        } else {
            DataHolder.getInstance().setUser(null);
            DataHolder.getInstance().setJwt(null);
            DataHolder.getInstance().setAuthenticated(false);
        }
    }
}
