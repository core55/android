/*
  Authors: S. Stefani
 */

package io.github.core55.joinup.helpers;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class GsonRequest<T> extends Request<T> {
    private final Gson gson = new Gson();
    private final Object body;
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;

    /**
     * Make a Http request and return a parsed object from JSON.
     *
     * @param method Http method of the request
     * @param url URL of the request to make
     * @param body Is the body in case of a POST, PUT or PATCH request
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GsonRequest(int method, String url, Object body, Class<T> clazz,
                       Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        Map<String, String> headers = new HashMap<>();
        this.setStandardHeaders(headers);
        this.attachJWT(headers);
        this.body = body;
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
    }

    /**
     * Make a Http request and return a parsed object from JSON.
     *
     * @param method Http method of the request
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     */
    public GsonRequest(int method, String url, Class<T> clazz, Response.Listener<T> listener,
                       Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        Map<String, String> headers = new HashMap<>();
        this.setStandardHeaders(headers);
        this.attachJWT(headers);
        this.body = null;
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
    }

    /**
     * If headers are provided set them in the request.
     *
     * @return The headers to put into the request
     * @throws AuthFailureError indicates an authentication failure when performing a request
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    /**
     * If a payload is provided set it in the request.
     *
     * @return The body to put into the request
     * @throws AuthFailureError indicates an authentication failure when performing a request
     */
    @Override
    public byte[] getBody() throws AuthFailureError {
        return body != null ? gson.toJson(body).getBytes() : super.getBody();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));

            // If response contains an Authorization header create a JSON object that will be casted
            // by reflection into an AuthenticationResponse with User and JWT
            if (response.headers.get("Authorization") != null) {
                JSONObject authenticationResponse = new JSONObject();
                authenticationResponse.put("user", new JSONObject(json));
                authenticationResponse.put("jwt", response.headers.get("Authorization"));
                json = authenticationResponse.toString();
            }

            return Response.success(
                    gson.fromJson(json, clazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        }
    }

    private void attachJWT(Map<String, String> headers) {
        if (DataHolder.getInstance().isAuthenticated()) {
            headers.put("Authorization", DataHolder.getInstance().getJwt());
        }
    }

    private void setStandardHeaders(Map<String, String> headers) {
        headers.put("Accept", "application/json, application/hal+json");
        headers.put("Content-Type", "application/json; charset=utf-8");
    }
}
