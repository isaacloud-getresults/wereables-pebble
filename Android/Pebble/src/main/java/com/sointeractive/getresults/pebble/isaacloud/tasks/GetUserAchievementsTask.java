package com.sointeractive.getresults.pebble.isaacloud.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import pl.sointeractive.isaacloud.connection.HttpResponse;
import pl.sointeractive.isaacloud.exceptions.IsaaCloudConnectionException;

public class GetUserAchievementsTask extends AsyncTask<Integer, Integer, Collection<AchievementIC>> {
    private static final String TAG = GetUserAchievementsTask.class.getSimpleName();

    @Override
    protected Collection<AchievementIC> doInBackground(final Integer... userId) {
        Log.i(TAG, "Action: Get achievements in background");

        try {
            return getAchievements(userId[0].toString());
        } catch (final JSONException e) {
            Log.e(TAG, "Error: JSON error");
        } catch (final IsaaCloudConnectionException e) {
            Log.e(TAG, "Error: IsaaCloudConnection error");
        } catch (final IOException e) {
            Log.e(TAG, "Error: IO error");
        }

        return null;
    }

    private Collection<AchievementIC> getAchievements(final String user) throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.ACHIEVEMENTS.getResponse(user);

        final Collection<AchievementIC> result = new LinkedList<AchievementIC>();
        final JSONArray achievements = response.getJSONArray();
        for (int i = 0; i < achievements.length(); i++) {
            final JSONObject achievementJSON = (JSONObject) achievements.get(i);
            result.add(new AchievementIC(achievementJSON));
        }

        Log.i(TAG, "Event: " + result.size() + " achievements found");
        return result;
    }

    @Override
    protected void onPostExecute(final Collection<AchievementIC> result) {
        if (result != null) {
            Log.i(TAG, "Event: Success");
        }
    }
}