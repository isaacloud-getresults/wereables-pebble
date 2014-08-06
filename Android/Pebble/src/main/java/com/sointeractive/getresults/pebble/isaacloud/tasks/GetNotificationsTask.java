package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.notification.IsaacloudNotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetNotificationsTask extends AsyncTask<Integer, Integer, List<String>> {
    private static final String TAG = GetNotificationsTask.class.getSimpleName();

    @Override
    protected List<String> doInBackground(final Integer... userId) {
        Log.d(TAG, "Action: Get notification where userId=" + userId[0] + " in background");

        try {
            return getNotifications(userId[0]);
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        return null;
    }

    private List<String> getNotifications(final Integer userId) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.getNotifications(userId);
        final List<String> notificationMessages = new LinkedList<String>();

        final JSONArray notificationsArray = response.getJSONArray();
        for (int i = 0; i < notificationsArray.length(); i++) {
            final JSONObject notification = notificationsArray.getJSONObject(i);
            final String notificationMessage = notification.getJSONObject("data").getJSONObject("body").getString("message");
            notificationMessages.add(notificationMessage);
        }

        return notificationMessages;
    }

    @Override
    protected void onPostExecute(final List<String> result) {
        if (result == null) {
            Log.e(TAG, "Error: Returned null");
        } else {
            final String title = "IsaaCloud notification";
            final String body = result.get(0);
            final IsaacloudNotification notification = new IsaacloudNotification(title, body);
            notification.send();
        }
    }
}