package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetNotificationsTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetUserIdTask;
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
            final int userId = getUserId();
            final GetUserTask getUser = new GetUserTask();
            final UserIC newUserData = getUser.execute(userId).get();

            if (newUserData != null) {
                if (userIC == null) {
                    final GetNotificationsTask getNotifications = new GetNotificationsTask();
                    getNotifications.execute(userId);
                }
                userIC = newUserData;
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    private int getUserId() throws ExecutionException, InterruptedException {
        if (isLoaded()) {
            return userIC.id;
        } else {
            final Integer userId = logIn();

            if (userId == null) {
                return 0;
            } else {
                return userId;
            }
        }
    }

    private Integer logIn() throws ExecutionException, InterruptedException {
        final GetUserIdTask getLoginId = new GetUserIdTask();
        return getLoginId.execute(IsaaCloudSettings.LOGIN_EMAIL).get();
    }

    private boolean isLoaded() {
        return userIC != null;
    }

    public void logOut() {
        userIC = null;
    }
}
