package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.checker.UserChangeChecker;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserProvider;
import com.sointeractive.getresults.pebble.pebble.responses.EmptyResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

public class LoginCache {
    public static final LoginCache INSTANCE = new LoginCache();

    private ResponseItem loginResponse = EmptyResponse.INSTANCE;

    private LoginCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData() {
        if (loginResponse instanceof EmptyResponse) {
            reload();
        }
        return getCollection();
    }

    private Collection<ResponseItem> getCollection() {
        final Collection<ResponseItem> responseList = new LinkedList<ResponseItem>();
        responseList.add(loginResponse);
        return responseList;
    }

    public void reload() {
        @Nullable final UserIC newUserIC = UserProvider.INSTANCE.getUpToDateData();
        if (newUserIC == null) {
            return;
        }

        final ResponseItem oldLoginResponse = loginResponse;
        loginResponse = getLoginResponse(newUserIC);

        UserChangeChecker.check(oldLoginResponse, loginResponse);
    }

    private ResponseItem getLoginResponse(final UserIC userIC) {
        final String roomName = BeaconsCache.INSTANCE.getRoomName(userIC.getBeacon());
        final int roomsNumber = BeaconsCache.INSTANCE.getSize();
        return userIC.toLoginResponse(roomName, roomsNumber);
    }

    public void clear() {
        UserProvider.INSTANCE.clear();
        loginResponse = EmptyResponse.INSTANCE;
    }
}
