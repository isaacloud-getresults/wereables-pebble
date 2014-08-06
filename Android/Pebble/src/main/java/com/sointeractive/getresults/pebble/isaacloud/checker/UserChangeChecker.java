package com.sointeractive.getresults.pebble.isaacloud.checker;

import android.util.Log;

import com.sointeractive.getresults.pebble.isaacloud.notification.IsaacloudNotification;
import com.sointeractive.getresults.pebble.pebble.communication.Responder;
import com.sointeractive.getresults.pebble.pebble.responses.LoginResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class UserChangeChecker {
    private static final String TAG = UserChangeChecker.class.getSimpleName();

    public static void check(final ResponseItem oldUser, final ResponseItem newUser) {
        if (!newUser.equals(oldUser)) {
            Log.i(TAG, "Checker: User data changed");
            final Collection<ResponseItem> loginResponse = new LinkedList<ResponseItem>();
            loginResponse.add(newUser);
            Responder.sendResponseItemsToPebble(loginResponse);

            checkRoomChanged((LoginResponse) oldUser, (LoginResponse) newUser);
        }
    }

    private static void checkRoomChanged(final LoginResponse oldUser, final LoginResponse newUser) {
        if (!oldUser.roomName.equals(newUser.roomName)) {
            sendNotification(newUser.roomName);
        }
    }

    private static void sendNotification(final String room) {
        final String title = "Room notification";
        final String body = "You have just entered " + room + ".";
        final IsaacloudNotification notification = new IsaacloudNotification(title, body);
        notification.send();
    }
}
