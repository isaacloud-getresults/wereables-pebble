package com.sointeractive.getresults.pebble.isaacloud.providers;

import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetUserIdTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetUserTask;
import com.sointeractive.getresults.pebble.socket.SocketIONotifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;

public class UserProvider {
    public static final UserProvider INSTANCE = new UserProvider();

    private UserIC userIC;

    private UserProvider() {
        // Exists only to defeat instantiation.
    }

    @Nullable
    public UserIC getData() {
        if (userIC == null) {
            reload();
        }
        return userIC;
    }

    @Nullable
    public UserIC getUpToDateData() {
        reload();
        return userIC;
    }

    private void reload() {
        try {
            final int userId = getId();
            if (userId < 0) {
                return;
            }

            final GetUserTask getUser = new GetUserTask();
            @Nullable final UserIC newUserData = getUser.execute(userId).get();
            if (newUserData == null) {
                return;
            }

            if (!isLoaded()) {
                onLogInAction(userId);
            }
            logIn(newUserData);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    private int getId() throws ExecutionException, InterruptedException {
        if (isLoaded()) {
            return userIC.getId();
        } else {
            return getUserId();
        }
    }

    @NotNull
    private Integer getUserId() throws ExecutionException, InterruptedException {
        final GetUserIdTask getUserId = new GetUserIdTask();
        return getUserId.execute(IsaaCloudSettings.LOGIN_EMAIL).get();
    }

    private boolean isLoaded() {
        return userIC != null;
    }

    private void onLogInAction(final int userId) {
        SocketIONotifier.INSTANCE.connect(userId);
    }

    private void logIn(final UserIC newUserData) {
        userIC = newUserData;
    }

    public void clear() {
        logIn(null);
    }
}
