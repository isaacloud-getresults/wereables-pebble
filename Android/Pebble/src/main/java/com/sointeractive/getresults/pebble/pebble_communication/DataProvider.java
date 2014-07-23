package com.sointeractive.getresults.pebble.pebble_communication;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DataProvider {
    /*
    * ResponseDataProvider mock-up -- will be replaced by communication with IsaaCloud.
    */

    private static final int PERCENT_RANGE = 101;

    private static final List<String> beaconsInRange;

    static {
        beaconsInRange = new LinkedList<String>();
        beaconsInRange.add("Kitchen 1");
        beaconsInRange.add("Meeting Room");
        beaconsInRange.add("Meeting Room 2");
        beaconsInRange.add("Boss Room");
    }

    private static final List<String> beaconsOutOfRange;

    static {
        beaconsOutOfRange = new LinkedList<String>();
        beaconsOutOfRange.add("Kitchen 2");
        beaconsOutOfRange.add("Games Room");
        beaconsOutOfRange.add("Games Room 2");
    }

    private static final List<String> activeGames;

    static {
        activeGames = new LinkedList<String>();
        activeGames.add("150 visits");
        activeGames.add("300 visits");
        activeGames.add("Without queue");
    }

    private static final List<String> completedGames;

    static {
        completedGames = new LinkedList<String>();
        completedGames.add("5 visits");
        completedGames.add("25 visits");
        completedGames.add("50 visits");
    }

    private static final Map<String, String> gameDetails;

    static {
        gameDetails = new HashMap<String, String>();
        gameDetails.put("5 visits", "Visit room 5 times.");
        gameDetails.put("25 visits", "Visit room 25 times.");
        gameDetails.put("50 visits", "Visit room 50 times.");
        gameDetails.put("150 visits", "Visit room 150 times.");
        gameDetails.put("300 visits", "Visit room 300 times.");
        gameDetails.put("Without queue", "Enter empty room.");
    }

    private static final Map<String, Integer> distances;

    static {
        distances = new HashMap<String, Integer>();
        distances.put("Kitchen 1", 100);
        distances.put("Meeting Room", 90);
        distances.put("Meeting Room 2", 75);
        distances.put("Boss Room", 50);
        distances.put("Kitchen 2", 25);
        distances.put("Games Room", 0);
        distances.put("Games Room 2", 0);
    }

    private static final Map<String, Integer> activeGamesCount;

    static {
        activeGamesCount = new HashMap<String, Integer>();
        activeGamesCount.put("Kitchen 1", 0);
        activeGamesCount.put("Meeting Room", 1);
        activeGamesCount.put("Meeting Room 2", 2);
        activeGamesCount.put("Boss Room", 3);
        activeGamesCount.put("Kitchen 2", 4);
        activeGamesCount.put("Games Room", 5);
        activeGamesCount.put("Games Room 2", 6);
    }

    private static final Map<String, Integer> completedGamesCount;

    static {
        completedGamesCount = new HashMap<String, Integer>();
        completedGamesCount.put("Kitchen 1", 6);
        completedGamesCount.put("Meeting Room", 5);
        completedGamesCount.put("Meeting Room 2", 4);
        completedGamesCount.put("Boss Room", 3);
        completedGamesCount.put("Kitchen 2", 2);
        completedGamesCount.put("Games Room", 1);
        completedGamesCount.put("Games Room 2", 0);
    }

    private static final List<String> achievements;

    static {
        achievements = new LinkedList<String>();
        achievements.add("Employee of the month");
        achievements.add("Quick eater");
        achievements.add("Swimmer");
    }

    private static final Random random = new Random();

    public static List<String> getBeaconsInRange() {
        return beaconsInRange;
    }

    public static List<String> getBeaconsOutOfRange() {
        return beaconsOutOfRange;
    }

    public static List<String> getActiveGames() {
        return activeGames;
    }

    public static List<String> getCompletedGames() {
        return completedGames;
    }

    public static int getDistance(String beacon) {
        return distances.get(beacon);
    }

    public static int getActiveGamesCount(String beacon) {
        return activeGamesCount.get(beacon);
    }

    public static int getCompletedGamesCount(String beacon) {
        return completedGamesCount.get(beacon);
    }

    public static int getProgress(String game) {
        return random.nextInt(PERCENT_RANGE);
    }

    public static String getGameDetails(String game) {
        return gameDetails.get(game);
    }

    public static String getLogin() {
        return "Janusz Tester";
    }

    public static int getPoints(String user) {
        return 58008;
    }

    public static List<String> getAchievements(String user) {
        return achievements;
    }

    public static int getRank(String user) {
        return 15;
    }
}