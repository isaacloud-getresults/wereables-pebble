package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.checker.UserChecker;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.RoomsProvider;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserProvider;
import com.sointeractive.getresults.pebble.pebble.responses.LoginResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

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
            final int roomsNumber = RoomsProvider.INSTANCE.getSize();
            final String roomName = RoomsProvider.INSTANCE.getRoomName(newUserIC.beacon);

            final ResponseItem oldLoginResponse = loginResponse;
            final ResponseItem newLoginResponse = new LoginResponse(newUserIC.getFullName(), newUserIC.points, newUserIC.rank, roomName, roomsNumber);

            if (oldLoginResponse != null) {
                UserChecker.check(oldLoginResponse, newLoginResponse);
            }

            loginResponse = newLoginResponse;
        }
    }
}
