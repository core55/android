/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import io.github.core55.joinup.Entity.User;
import io.github.core55.joinup.Model.DataHolder;
import io.github.core55.joinup.R;

public class AuthenticationHelper {

    public static void syncDataHolder(Activity activity) {
        final Context context = activity.getApplicationContext();
        final String UNKNOWN_USER = "Unknown User";

        // Retrieve data about authenticated user from shared preferences
        SharedPreferences sharedPref = activity.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String authUser = sharedPref.getString(context.getString(R.string.auth_user), UNKNOWN_USER);
        String anonymousUser = sharedPref.getString(context.getString(R.string.anonymous_user), UNKNOWN_USER);
        String jwt = sharedPref.getString(context.getString(R.string.jwt_string), "No JWT");

        // If user data exist then update dataHolder and set isAuthenticated flag to true. Otherwise
        // clean dataHolder and set isAuthenticated flag to false
        if (!authUser.equals(UNKNOWN_USER)) {
            User user = new Gson().fromJson(authUser, User.class);
            DataHolder.getInstance().setUser(user);
            DataHolder.getInstance().setJwt(jwt);
            DataHolder.getInstance().setAnonymous(false);
            DataHolder.getInstance().setAuthenticated(true);
        } else if (!anonymousUser.equals(UNKNOWN_USER)) {
            User user = new Gson().fromJson(anonymousUser, User.class);
            DataHolder.getInstance().setUser(user);
            DataHolder.getInstance().setJwt(null);
            DataHolder.getInstance().setAnonymous(true);
            DataHolder.getInstance().setAuthenticated(false);
        } else {
            DataHolder.getInstance().setUser(null);
            DataHolder.getInstance().setJwt(null);
            DataHolder.getInstance().setAnonymous(false);
            DataHolder.getInstance().setAuthenticated(false);
        }
    }

    public static void persistAuthenticatedUser(Activity activity, User user, String jwt) {
        final Context context = activity.getApplicationContext();

        SharedPreferences sharedPref = activity.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.auth_user), new Gson().toJson(user));
        editor.putString(context.getString(R.string.jwt_string), jwt);
        editor.commit();

        sharedPref.edit().remove(context.getString(R.string.anonymous_user)).commit();
    }

    public static void syncSharedPreferences(Activity activity) {
        final Context context = activity.getApplicationContext();

        SharedPreferences sharedPref = activity.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        if (DataHolder.getInstance().isAuthenticated()) {
            User user = DataHolder.getInstance().getUser();
            editor.putString(context.getString(R.string.auth_user), new Gson().toJson(user));
        } else if (DataHolder.getInstance().isAnonymous()) {
            User user = DataHolder.getInstance().getUser();
            editor.putString(context.getString(R.string.anonymous_user), new Gson().toJson(user));
        } else {
            sharedPref.edit().remove(context.getString(R.string.auth_user)).commit();
            sharedPref.edit().remove(context.getString(R.string.anonymous_user)).commit();
            sharedPref.edit().remove(context.getString(R.string.jwt_string)).commit();
        }

        editor.commit();
    }

    public static void authenticationLogger(Activity activity) {
        final Context context = activity.getApplicationContext();

        SharedPreferences sharedPref = activity.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        Log.i("Auth", "SP Auth: " + sharedPref.getString(context.getString(R.string.auth_user), "/"));
        Log.i("Auth", "DH Auth: " + DataHolder.getInstance().isAuthenticated());
        Log.i("Auth", "JWT: " + sharedPref.getString(context.getString(R.string.jwt_string), "/"));
        Log.i("Auth", "SP Anon: " + sharedPref.getString(context.getString(R.string.anonymous_user), "/"));
        Log.i("Auth", "DH Anon: " + DataHolder.getInstance().isAnonymous());
    }
}
