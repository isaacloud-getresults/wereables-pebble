package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.utils.Application;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetAchievementsTask extends AsyncTask<Integer, Integer, Collection<AchievementIC>> {
    private static final String TAG = GetAchievementsTask.class.getSimpleName();

    private static final String PATH = "/cache/users/%d/achievements";
    private static final String[] FIELDS = new String[]{"id", "label", "description"};

    @Override
    protected Collection<AchievementIC> doInBackground(final Integer... userIds) {
        Log.d(TAG, "Action: Get achievements in background");

        if (userIds.length != 1) {
            throw new IllegalArgumentException("You have to use exactly one id to to get user achievements");
        }

        try {
            return getAchievements(userIds[0]);
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        Log.e(TAG, "Error: User achievements not found");
        return new LinkedList<AchievementIC>();
    }

    private Collection<AchievementIC> getAchievements(final int userId) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = getHttpResponse(userId);
        final Collection<AchievementIC> result = new LinkedList<AchievementIC>();

        final JSONArray achievements = response.getJSONArray();
        for (int i = 0; i < achievements.length(); i++) {
            if (!achievements.isNull(i)) {
                final AchievementIC achievementIC = getAchievement(achievements, i);
                result.add(achievementIC);
            }
        }

        Log.d(TAG, "Event: " + result.size() + " achievements downloaded");
        return result;
    }

    private AchievementIC getAchievement(final JSONArray achievements, final int i) throws JSONException {
        final JSONObject achievementJSON = achievements.getJSONObject(i);
        return new AchievementIC(achievementJSON);
    }

    private HttpResponse getHttpResponse(final int userId) throws IOException, IsaaCloudConnectionException {
        final String path = String.format(PATH, userId);

        Log.d(TAG, "Action: Query for user achievements");

        return Application
                .getIsaacloudConnector()
                .path(path)
                .withFields(FIELDS)
                .withLimit(IsaaCloudSettings.UNLIMITED)
                .get();
    }
}