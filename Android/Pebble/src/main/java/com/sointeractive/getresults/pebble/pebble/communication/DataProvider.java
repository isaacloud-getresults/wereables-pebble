package com.sointeractive.getresults.pebble.pebble.communication;

import com.sointeractive.getresults.pebble.isaacloud.data.AchievementIC;
import com.sointeractive.getresults.pebble.isaacloud.data.PersonIC;
import com.sointeractive.getresults.pebble.isaacloud.data.RoomIC;
import com.sointeractive.getresults.pebble.isaacloud.data.UserIC;
import com.sointeractive.getresults.pebble.isaacloud.providers.AchievementProvider;
import com.sointeractive.getresults.pebble.isaacloud.providers.PeopleProvider;
import com.sointeractive.getresults.pebble.isaacloud.providers.RoomsProvider;
import com.sointeractive.getresults.pebble.isaacloud.providers.UserProvider;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.BeaconResponse;
import com.sointeractive.getresults.pebble.pebble.responses.LoginResponse;
import com.sointeractive.getresults.pebble.pebble.responses.PersonResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

@SuppressWarnings("StaticMethodOnlyUsedInOneClass")
class DataProvider {
    public final static DataProvider INSTANCE = new DataProvider();

    private DataProvider() {
        // Exists only to defeat instantiation.
    }

    private static Collection<ResponseItem> listWrap(final ResponseItem item) {
        final Collection<ResponseItem> list = new LinkedList<ResponseItem>();
        list.add(item);
        return list;
    }

    public Collection<ResponseItem> getLogin() {
        final UserIC userIC = UserProvider.INSTANCE.getData();
        final int roomsNumber = RoomsProvider.INSTANCE.getData().size();
        final int achievementsNumber = getAchievements().size();
        // TODO: Provide real rank position
        final ResponseItem login = new LoginResponse(userIC.getFullName(), userIC.points, 0, roomsNumber, achievementsNumber);
        return listWrap(login);
    }

    public Collection<ResponseItem> getBeacons() {
        final Collection<RoomIC> rooms = RoomsProvider.INSTANCE.getData();

        final Collection<ResponseItem> beacons = new LinkedList<ResponseItem>();
        for (final RoomIC room : rooms) {
            // TODO: Provide real beacon distance
            final int peopleNumber = getPeople(room.name).size();
            beacons.add(new BeaconResponse(room.name, 0, peopleNumber));
        }
        return beacons;
    }

    public Collection<ResponseItem> getAchievements() {
        final Collection<AchievementIC> allAchievements = AchievementProvider.INSTANCE.getData();
        final UserIC currentUser = UserProvider.INSTANCE.getData();

        final Collection<ResponseItem> userAchievements = new LinkedList<ResponseItem>();
        for (final AchievementIC achievement : allAchievements) {
            if (currentUser.achievements.contains(achievement.id)) {
                userAchievements.add(new AchievementResponse(achievement.name, achievement.description));
            }
        }
        return userAchievements;
    }

    public Collection<ResponseItem> getPeople(final String query) {
        final Collection<PersonIC> people = PeopleProvider.INSTANCE.getData();
        final int roomId = getRoomId(query);

        final Collection<ResponseItem> peopleInRoom = new LinkedList<ResponseItem>();
        for (final PersonIC person : people) {
            if (person.beacon == roomId && !person.getFullName().equals("null null")) {
                peopleInRoom.add(new PersonResponse(person.getFullName()));
            }
        }
        return peopleInRoom;
    }

    private int getRoomId(final String query) throws NoSuchElementException {
        final Collection<RoomIC> roomsIC = RoomsProvider.INSTANCE.getData();
        for (final RoomIC room : roomsIC) {
            if (room.name.equals(query)) {
                return room.id;
            }
        }
        return -1;
    }
}