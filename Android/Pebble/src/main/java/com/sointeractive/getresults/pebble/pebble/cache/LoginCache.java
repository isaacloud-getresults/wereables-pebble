package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.RoomsProvider;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserAchievementsProvider;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserProvider;
import com.sointeractive.getresults.pebble.pebble.responses.LoginResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class LoginCache {
    public static final LoginCache INSTANCE = new LoginCache();

    private Collection<ResponseItem> loginResponse;

    private LoginCache() {
        // Exists only to defeat instantiation.
    }

    public Collection<ResponseItem> getData() {
        if (loginResponse == null) {
            reload();
        }
        return loginResponse;
    }

    public void reload() {
        final UserIC userIC = UserProvider.INSTANCE.getUpToDateData();
        final int roomsNumber = RoomsProvider.INSTANCE.getData().size();
        final int achievementsNumber = UserAchievementsProvider.INSTANCE.getData().size();

        final ResponseItem login = new LoginResponse(userIC.getFullName(), userIC.points, userIC.rank, roomsNumber, achievementsNumber);
        loginResponse = listWrap(login);
    }

    private Collection<ResponseItem> listWrap(final ResponseItem item) {
        final Collection<ResponseItem> list = new LinkedList<ResponseItem>();
        list.add(item);
        return list;
    }
}
