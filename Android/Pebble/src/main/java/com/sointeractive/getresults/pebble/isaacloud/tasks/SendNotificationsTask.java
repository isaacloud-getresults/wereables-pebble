package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.config.PebbleSettings;
import com.sointeractive.getresults.pebble.utils.Application;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class SendNotificationsTask extends AsyncTask<Integer, Integer, List<String>> {
    private static final String TAG = SendNotificationsTask.class.getSimpleName();

    private static final String PATH = "/queues/notifications";
    private static final String[] FIELDS = new String[]{"data"};

    @Override
    protected List<String> doInBackground(final Integer... userIds) {
        Log.d(TAG, "Action: Get notification where userId=" + userIds[0] + " in background");

        if (userIds.length != 1) {
            throw new IllegalArgumentException("You have to use exactly one id to to get user notifications");
        }

        try {
            return getNotifications(userIds[0]);
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
        final HttpResponse response = getHttpResponse(userId);
        final List<String> notificationMessages = new LinkedList<String>();

        final JSONArray notificationsArray = response.getJSONArray();
        for (int i = 0; i < notificationsArray.length(); i++) {
            final String notificationMessage = getMessage(notificationsArray, i);
            notificationMessages.add(notificationMessage);
        }

        return notificationMessages;
    }

    private String getMessage(final JSONArray notificationsArray, final int i) throws JSONException {
        final JSONObject notification = notificationsArray.getJSONObject(i);
        return notification.getJSONObject("data").getJSONObject("body").getString("message");
    }

    private HttpResponse getHttpResponse(final int userId) throws IOException, IsaaCloudConnectionException {
        Log.d(TAG, "Action: Query for notifications where userId=" + userId);

        final Map<String, Object> query = getQuery(userId);

        return Application
                .getIsaacloudConnector()
                .path(PATH)
                .withFields(FIELDS)
                .withQuery(query)
                .get();
    }

    private Map<String, Object> getQuery(final int userId) {
        final Map<String, Object> query = new HashMap<String, Object>();
        query.put("subjectId", userId);
        query.put("typeId", IsaaCloudSettings.PEBBLE_NOTIFICATION_ID);
        query.put("status", 0);
        return query;
    }

    @Override
    protected void onPostExecute(final List<String> result) {
        if (result == null) {
            Log.e(TAG, "Error: Returned null");
        } else {
            // TODO: Replace trimmed list by whole result
            for (final String body : result.subList(0, 1)) {
                Application.getPebbleConnector().sendNotification(PebbleSettings.IC_NOTIFICATION_HEADER, body);
            }
        }
    }
}