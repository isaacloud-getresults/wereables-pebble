package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.checker.UserChangeChecker;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserProvider;
import com.sointeractive.getresults.pebble.pebble.responses.LoginResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;
import com.sointeractive.getresults.pebble.utils.Application;

import java.util.Collection;
import java.util.LinkedList;

public class LoginCache {
    public static final LoginCache INSTANCE = new LoginCache();

    private ResponseItem loginResponse;

    private LoginCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData() {
        if (loginResponse == null) {
            reload();
        }
        return safeLogin();
    }

    private Collection<ResponseItem> safeLogin() {
        if (loginResponse == null) {
            return new LinkedList<ResponseItem>();
        } else {
            final Collection<ResponseItem> responseList = new LinkedList<ResponseItem>();
            responseList.add(loginResponse);
            return responseList;
        }
    }

    public void reload() {
        final UserIC newUserIC = UserProvider.INSTANCE.getUpToDateData();

        if (newUserIC != null) {
            final int roomsNumber = BeaconsCache.INSTANCE.getSize();
            final String roomName = BeaconsCache.INSTANCE.getRoomName(newUserIC.beacon);

            final ResponseItem oldLoginResponse = loginResponse;
            final ResponseItem newLoginResponse = new LoginResponse(newUserIC.getFullName(), newUserIC.points, newUserIC.rank, roomName, roomsNumber);

            if (oldLoginResponse != null) {
                UserChangeChecker.check(oldLoginResponse, newLoginResponse);
            } else {
                PeopleCache.INSTANCE.setObservedRoom(0);
                Application.pebbleConnector.clearSendingQueue();
            }

            loginResponse = newLoginResponse;
        }
    }

    public void clear() {
        UserProvider.INSTANCE.clear();
        loginResponse = null;
    }
}
