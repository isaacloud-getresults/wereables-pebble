package com.sointeractive.getresults.pebble.pebble;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.PebbleDictionaryBuilder;

import java.util.List;

public enum Response {
    UNKNOWN(0, "Response: UNKNOWN RESPONSE", null),
    USER_DETAILS(21, "Response: User details", new SendUserDetails()),
    BEACONS(22, "Response: Sending beacons list", new SendBeaconsList()),
    GAMES(24, "Response: Sending games list", new SendGamesList()),
    USER_INFO(26, "Response: Sending user info", new SendUser()),
    BEACON_DETAILS(27, "Response: Sending beacon details", new SendBeaconDetails()),
    GAME_PROGRESS(28, "Response: Sending progress", new SendProgress()),
    GAME_DETAILS(29, "Response: Game details", new SendGameDetails());

    public static final int RESPONSE_TYPE = 200;
    public static final int RESPONSE_LENGTH = 201;

    private final int id;
    private final String logMessage;
    private final ResponseAction responseAction;
    private String query = "";

    private Response(int id, String logMessage, ResponseAction responseAction) {
        this.id = id;
        this.logMessage = logMessage;
        this.responseAction = responseAction;
    }

    public PebbleDictionary getDataToSend() {
        return responseAction.execute(id, query);
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    private interface ResponseAction {
        public PebbleDictionary execute(int id, String query);
    }

    private static class SendBeaconsList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> beaconsInRange = DataProvider.getBeaconsInRange();
            List<String> beaconsOutOfRange = DataProvider.getBeaconsOutOfRange();
            return new PebbleDictionaryBuilder(id)
                    .addInt(beaconsInRange.size())
                    .addInt(beaconsOutOfRange.size())
                    .addList(beaconsInRange)
                    .addList(beaconsOutOfRange)
                    .build();
        }
    }

    private static class SendGamesList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> activeGames = DataProvider.getActiveGames();
            List<String> completedGames = DataProvider.getCompletedGames();
            return new PebbleDictionaryBuilder(id)
                    .addInt(activeGames.size())
                    .addInt(completedGames.size())
                    .addList(activeGames)
                    .addList(completedGames)
                    .build();
        }
    }

    private static class SendBeaconDetails implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final int distance = DataProvider.getDistance(query);
            final int activeGamesCount = DataProvider.getActiveGamesCount(query);
            final int completedGamesCount = DataProvider.getCompletedGamesCount(query);

            return new PebbleDictionaryBuilder(id)
                    .addInt(distance)
                    .addInt(activeGamesCount)
                    .addInt(completedGamesCount)
                    .build();
        }
    }

    private static class SendProgress implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final int progress = DataProvider.getProgress(query);
            return new PebbleDictionaryBuilder(id)
                    .addInt(progress)
                    .build();
        }
    }

    private static class SendGameDetails implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final String gameDetails = DataProvider.getGameDetails(query);
            return new PebbleDictionaryBuilder(id)
                    .addString(gameDetails)
                    .build();
        }
    }

    private static class SendUser implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final String login = DataProvider.getLogin();
            final int points = DataProvider.getPoints(login);
            return new PebbleDictionaryBuilder(id)
                    .addString(login)
                    .addInt(points)
                    .build();
        }

    }

    private static class SendUserDetails implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final int rank = DataProvider.getRank(query);
            final int points = DataProvider.getPoints(query);
            List<String> achievements = DataProvider.getAchievements(query);
            return new PebbleDictionaryBuilder(id)
                    .addInt(rank)
                    .addInt(points)
                    .addList(achievements)
                    .build();
        }
    }
}
