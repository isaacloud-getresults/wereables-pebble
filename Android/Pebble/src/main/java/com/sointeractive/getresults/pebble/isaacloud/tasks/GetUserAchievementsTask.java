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
        Log.d(TAG, "Action: Get achievements in background");

        try {
            return getAchievements(userId[0].toString());
        } catch (final JSONException e) {
            e.printStackTrace();
        } catch (final IsaaCloudConnectionException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
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

        return result;
    }

    @Override
    protected void onPostExecute(final Collection<AchievementIC> result) {
        if (result != null) {
            Log.d(TAG, "Event: Success");
        }
    }
}