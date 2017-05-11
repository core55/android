/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.Helper;

import android.app.Activity;
import android.widget.Toast;

import com.android.volley.NetworkResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequestHelper {

    public static void handleErrorResponse(NetworkResponse response, Activity activity) {
        if (response != null && response.data != null) {
            String json = new String(response.data);

            String message = null;
            try {
                JSONObject obj = new JSONObject(json);
                message = obj.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            switch (response.statusCode) {
                case 422:
                    if (message != null)
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                    break;
                case 401:
                    if (message != null)
                        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
