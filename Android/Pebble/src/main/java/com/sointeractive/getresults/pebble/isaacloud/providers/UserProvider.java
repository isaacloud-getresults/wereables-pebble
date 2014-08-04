package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetLoginTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetUserTask;

import java.util.concurrent.ExecutionException;

public class UserProvider {
    public static final UserProvider INSTANCE = new UserProvider();

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
        try {
            if (isLoaded()) {
                final GetUserTask getUser = new GetUserTask();
                final UserIC newUserData = getUser.execute(userIC.id).get();
                if (newUserData != null) {
                    userIC = newUserData;
                }
            } else {
                final GetLoginTask getLogin = new GetLoginTask();
                userIC = getLogin.execute(Settings.LOGIN_EMAIL).get();
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    private boolean isLoaded() {
        return userIC != null;
    }
}
