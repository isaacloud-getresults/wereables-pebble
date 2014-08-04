package com.sointeractive.getresults.pebble.isaacloud.checker;

import android.util.Log;

import com.sointeractive.getresults.pebble.pebble.communication.Request;
import com.sointeractive.getresults.pebble.pebble.communication.Responder;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class UserChecker {
    private static final String TAG = UserChecker.class.getSimpleName();

    public static void check(final ResponseItem oldUser, final ResponseItem newUser) {
        if (!newUser.equals(oldUser)) {
            Log.i(TAG, "Event: User data changed");
            final Collection<ResponseItem> loginResponse = new LinkedList<ResponseItem>();
            loginResponse.add(newUser);
            Responder.sendResponseItemsToPebble(Request.LOGIN.id, loginResponse);
        }
    }
}
