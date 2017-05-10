/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.R;
import io.github.core55.joinup.entities.User;

public class AuthenticationHelper {

    public static void syncDataHolder(Activity activity) {
        final Context context = activity.getApplicationContext();

        // Retrieve data about authenticated user from shared preferences
        SharedPreferences sharedPref = activity.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String currentUser = sharedPref.getString(context.getString(R.string.current_user), "Unknown User");
        String jwt = sharedPref.getString(context.getString(R.string.jwt_string), "No JWT");

        // If user data exist then update dataHolder and set isAuthenticated flag to true. Otherwise
        // clean dataHolder and set isAuthenticated flag to false
        if (!currentUser.equals("Unknown User")) {
            User user = new Gson().fromJson(currentUser, User.class);
            DataHolder.getInstance().setUser(user);
            DataHolder.getInstance().setJwt(jwt);
            DataHolder.getInstance().setAuthenticated(true);
        } else {
            DataHolder.getInstance().setUser(null);
            DataHolder.getInstance().setJwt(null);
            DataHolder.getInstance().setAuthenticated(false);
        }
    }
}
