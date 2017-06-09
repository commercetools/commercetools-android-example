package de.commercetools.android_example;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static de.commercetools.android_example.SharedPrefKeys.CTP_REFRESH_TOKEN_KEY;

public class AuthenticationService extends Service {
    private GlobalRequestQueue globalRequestQueue;
    private Context context;

    public AuthenticationService() {
    }

    public void getAccessToken(final Response.Listener<JSONObject> listener) {
        globalRequestQueue.addToRequestQueue(getAccessTokenRequest(listener));
    }

    private JsonObjectRequest getAccessTokenRequest(final Response.Listener<JSONObject> listener) {
        Log.i(getClass().getSimpleName(), "token requested");
        final SharedPreferences preferences = getSharedPreferences(SharedPrefKeys.CTP_PREFS_KEY, 0);
        final boolean refreshTokenAvailable = preferences.contains(CTP_REFRESH_TOKEN_KEY);
        final String projectKey = getString(R.string.project);
        final String authUrl = getString(R.string.authUrl) + "/oauth/" + projectKey + "/anonymous/token/";
        if (refreshTokenAvailable) {
            Log.i(getClass().getSimpleName(), "cached refresh token is used to get access token");
            final String refreshToken = preferences.getString(CTP_REFRESH_TOKEN_KEY, "");
            final String grantType = "grant_type=refresh_token";

            return new JsonObjectRequest(Request.Method.POST, authUrl + "?" + grantType + "&refresh_token=" + refreshToken, listener, new AuthenticationFailedToastListener()) {
                @Override
                public Map<String, String> getHeaders() {
                    return createAuthenticationHeaderMap();
                }
            };
        } else {
            Log.i(getClass().getSimpleName(), "fetching access token");
            final String grantType = getString(R.string.grantType);
            final String scope = createScopesString(projectKey);
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, authUrl + "?" + grantType + "&" + scope, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    final String refresh_token = response.optString("refresh_token");
                    preferences.edit().putString(CTP_REFRESH_TOKEN_KEY, refresh_token).commit();
                    listener.onResponse(response);
                }
            }, new AuthenticationFailedToastListener()) {
                @Override
                public Map<String, String> getHeaders() {
                    return createAuthenticationHeaderMap();
                }
            };
            return request;
        }
    }

    @NonNull
    private Map<String, String> createAuthenticationHeaderMap() {
        final Map<String, String> params = new HashMap<>();
        params.put("Authorization", "Basic " + createBasicAuthToken());
        return params;
    }

    private String createScopesString(String projectKey) {
        final String[] scopesArray = getResources().getStringArray(R.array.scopes);
        final StringBuilder builder = new StringBuilder();
        for (final String scope : scopesArray) {
            builder.append(scope).append(":").append(projectKey).append(" ");
        }
        return builder.toString().trim();
    }

    private void showAuthenticationFailedToast() {
        final CharSequence text = "Authentication failed!\n" +
                "Check your credentials. See README.md for details.";

        final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Set your credentials in credentials.xml (See README.md for details)
     * https://github.com/sphereio/commercetools-android-example/tree/master#commercetools-android-example
     */
    private String createBasicAuthToken() {
        final byte[] bytes = (getString(R.string.clientId) + ":" + getString(R.string.clientSecret)).getBytes();
        return android.util.Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /**
     * Lifecycle stuff
     */

    @Override
    public IBinder onBind(Intent intent) {
        context = this.getApplicationContext();
        globalRequestQueue = GlobalRequestQueue.getInstance(context);
        return new AuthenticationServiceBinder(this);
    }

    private class AuthenticationFailedToastListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error.networkResponse != null) {
                showAuthenticationFailedToast();
            }
        }
    }
}
