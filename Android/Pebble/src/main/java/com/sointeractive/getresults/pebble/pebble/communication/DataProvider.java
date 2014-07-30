package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.getresults.pebble.isaacloud.IsaacloudProxy;
import com.sointeractive.getresults.pebble.pebble.responses.LoginResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class DataProvider {
    /*
    * ResponseDataProvider mock-up -- will be replaced by data from IsaaCloud.
    */

    public static Collection<ResponseItem> getLogin() {
        final ResponseItem login = new LoginResponse(getUserName(), getPoints(), getRank(), getBeaconsSize(), getAchievementsSize());
        return listWrap(login);
    }

    private static String getUserName() {
        return IsaacloudProxy.getUserName();
    }

    private static int getPoints() {
        return IsaacloudProxy.getUserPoints();
    }

    private static int getRank() {
        return IsaacloudProxy.getUserRank();
    }

    private static int getBeaconsSize() {
        return IsaacloudProxy.getBeaconsSize();
    }

    private static int getAchievementsSize() {
        return IsaacloudProxy.getUserAchievementsSize();
    }

    private static Collection<ResponseItem> listWrap(final ResponseItem item) {
        final Collection<ResponseItem> list = new LinkedList<ResponseItem>();
        list.add(item);
        return list;
    }

    public static Collection<ResponseItem> getBeacons() {
        return IsaacloudProxy.getBeacons();
    }

    public static Collection<ResponseItem> getAchievements() {
        return IsaacloudProxy.getUserAchievements();
    }

    public static Collection<ResponseItem> getPeople(final String query) {
        return IsaacloudProxy.getPeopleResponse(query);
    }
}