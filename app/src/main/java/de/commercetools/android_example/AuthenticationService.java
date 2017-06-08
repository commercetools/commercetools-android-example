package de.commercetools.android_example;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Base64;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthenticationService extends Service {
    private GlobalRequestQueue globalRequestQueue;
    private Context context;

    public AuthenticationService() {
    }

    public void getAccessToken(final Response.Listener<JSONObject> listener) {
        globalRequestQueue.addToRequestQueue(getAccessTokenRequest(listener));
    }

    private JsonObjectRequest getAccessTokenRequest(final Response.Listener<JSONObject> listener) {
        final String grantType = getString(R.string.grantType);
        final String projectKey = getString(R.string.project);
        final String authUrl = getString(R.string.authUrl)+ "/oauth/" + projectKey + "/anonymous/token/";
        final String scope = createScopesString(projectKey);
        final String anonymousId = UUID.randomUUID().toString();
        return new JsonObjectRequest(Request.Method.POST, authUrl + "?" + grantType + "&" + scope + "&anonymous_id=" + anonymousId, listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error.networkResponse != null) {
                            showAuthenticationFailedToast();
                        }
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                final Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Basic " + createBasicAuthToken());
                return params;
            }
        };
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
}
