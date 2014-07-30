package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetUserTask;

import java.util.concurrent.ExecutionException;

public class UserProvider implements Provider {
    public final static UserProvider INSTANCE = new UserProvider();
    private UserIC userIC;

    private UserProvider() {
        // Exists only to defeat instantiation.
    }

    public UserIC getData() {
        if (userIC == null) {
            reload();
        }
        return userIC;
    }

    public UserIC getUpToDateData() {
        reload();
        return userIC;
    }

    private void reload() {
        final GetUserTask getUser = new GetUserTask();
        try {
            userIC = getUser.execute(Settings.USER_EMAIL).get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
