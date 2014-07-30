package com.sointeractive.getresults.pebble.pebble.cache;

import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.RoomsProvider;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserProvider;
import com.sointeractive.getresults.pebble.pebble.responses.LoginResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

public class LoginCache implements Cache {
    public static final Cache INSTANCE = new LoginCache();

    private Collection<ResponseItem> loginResponse;

    private LoginCache() {
        // Exists only to defeat instantiation.
    }

    @Override
    public Collection<ResponseItem> getData() {
        if (loginResponse == null) {
            reload();
        }
        return loginResponse;
    }

    @Override
    public Collection<ResponseItem> getUpToDateData() {
        reload();
        return loginResponse;
    }

    public void reload() {
        final UserIC userIC = UserProvider.INSTANCE.getData();
        final int roomsNumber = RoomsProvider.INSTANCE.getData().size();
        final int achievementsNumber = AchievementsCache.INSTANCE.getData().size();

        final ResponseItem login = new LoginResponse(userIC.getFullName(), userIC.points, userIC.rank, roomsNumber, achievementsNumber);
        loginResponse = listWrap(login);
    }

    private Collection<ResponseItem> listWrap(final ResponseItem item) {
        final Collection<ResponseItem> list = new LinkedList<ResponseItem>();
        list.add(item);
        return list;
    }
}
