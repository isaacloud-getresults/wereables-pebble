package com.sointeractive.getresults.pebble.isaacloud.data;

import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetAchievementsTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetBeaconsTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.GetPeopleTask;
import com.sointeractive.getresults.pebble.isaacloud.tasks.LoginTask;
import com.sointeractive.getresults.pebble.pebble.responses.AchievementResponse;
import com.sointeractive.getresults.pebble.pebble.responses.BeaconResponse;
import com.sointeractive.getresults.pebble.pebble.responses.PersonResponse;
import com.sointeractive.getresults.pebble.pebble.responses.ResponseItem;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

public class IsaacloudProxy {
    private static User user;
    private static Collection<Achievement> achievements;
    private static Collection<ResponseItem> userAchievements;
    private static Collection<Room> rooms;
    private static Collection<ResponseItem> beacons;
    private static Collection<Person> people;

    private static User getUser() {
        if (user == null) {
            reloadUser();
        }
        return user;
    }

    public static void reloadUser() {
        final LoginTask getUser = new LoginTask();
        try {
            user = getUser.execute(Settings.USER_EMAIL).get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static Collection<Achievement> getAchievements() {
        if (achievements == null) {
            reloadAchievements();
        }
        return achievements;
    }

    private static void reloadAchievements() {
        final GetAchievementsTask getAchievements = new GetAchievementsTask();
        try {
            achievements = getAchievements.execute().get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static Collection<Person> getPeople() {
        if (people == null) {
            reloadPeople();
        }
        return people;
    }

    private static void reloadPeople() {
        final GetPeopleTask getPeople = new GetPeopleTask();
        try {
            people = getPeople.execute().get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void reloadAllData() {
        reloadUser();
        reloadAchievements();
        reloadBeacons();
    }

    public static String getUserName() {
        return getUser().getFullName();
    }

    private static void reloadUserAchievements() {
        final Collection<ResponseItem> userAchievements = new LinkedList<ResponseItem>();
        final Collection<Achievement> allAchievements = getAchievements();
        final User currentUser = getUser();

        for (final Achievement achievement : allAchievements) {
            if (currentUser.achievements.contains(achievement.id)) {
                userAchievements.add(new AchievementResponse(achievement.name, achievement.description));
            }
        }

        IsaacloudProxy.userAchievements = userAchievements;
    }

    public static Collection<ResponseItem> getUserAchievements() {
        if (userAchievements == null) {
            reloadUserAchievements();
        }
        return userAchievements;
    }

    public static int getUserAchievementsSize() {
        return getUserAchievements().size();
    }

    private static Collection<Room> getRooms() {
        if (rooms == null) {
            reloadRooms();
        }
        return rooms;
    }

    private static void reloadRooms() {
        final GetBeaconsTask getBeacons = new GetBeaconsTask();
        try {
            rooms = getBeacons.execute().get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static Collection<ResponseItem> getBeacons() {
        if (beacons == null) {
            reloadBeacons();
        }
        return beacons;
    }

    private static void reloadBeacons() {
        final Collection<ResponseItem> beacons = new LinkedList<ResponseItem>();
        final Collection<Room> rooms = getRooms();

        for (final Room room : rooms) {
            beacons.add(new BeaconResponse(room.name, 0, 0, 0));
        }

        IsaacloudProxy.beacons = beacons;
    }

    public static int getBeaconsSize() {
        return getBeacons().size();
    }


    public static Collection<ResponseItem> getPeopleResponse(final String query) {
        try {
            return getPeopleInRoom(getRoomId(query));
        } catch (final NoSuchElementException e) {
            return new LinkedList<ResponseItem>();
        }
    }

    private static Collection<ResponseItem> getPeopleInRoom(final int roomId) {
        final Collection<ResponseItem> peopleInRoom = new LinkedList<ResponseItem>();
        final Collection<Person> people = getPeople();
        for (final Person person : people) {
            if (person.beacon == roomId) {
                peopleInRoom.add(new PersonResponse(person.getFullName()));
            }
        }
        return peopleInRoom;
    }

    private static int getRoomId(final String query) throws NoSuchElementException {
        for (final Room room : rooms) {
            if (room.name.equals(query)) {
                return room.id;
            }
        }
        throw new NoSuchElementException("There is no room of that name.");
    }

    public static int getUserPoints() {
        return getUser().points;
    }

    public static int getUserRank() {
        return 74;
    }
}
