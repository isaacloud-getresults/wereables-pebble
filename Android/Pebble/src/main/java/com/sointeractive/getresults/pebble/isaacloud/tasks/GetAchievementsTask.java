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

public class GetAchievementsTask extends AsyncTask<Void, Integer, Collection<AchievementIC>> {
    private static final String TAG = GetAchievementsTask.class.getSimpleName();

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "Event: onPreExecute");
    }

    @Override
    protected Collection<AchievementIC> doInBackground(final Void... params) {
        Log.d(TAG, "Action: Get achievements in background");

        try {
            return getAchievements();
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final IsaaCloudConnectionException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Collection<AchievementIC> getAchievements() throws IOException, IsaaCloudConnectionException, JSONException {
        final HttpResponse response = Query.ACHIEVEMENTS.getResponse();
        final Collection<AchievementIC> result = new LinkedList<AchievementIC>();

        final JSONArray achievements = response.getJSONArray();
        for (int i = 0; i < achievements.length(); i++) {
            final JSONObject achievementJSON = (JSONObject) achievements.get(i);
            result.add(new AchievementIC(achievementJSON));
        }
        return result;
    }

    @Override
    protected void onPostExecute(final Collection<AchievementIC> result) {
        Log.d(TAG, "Event: onPostExecute");

        if (result != null) {
            Log.d(TAG, "Event: Success");
        }
    }
}