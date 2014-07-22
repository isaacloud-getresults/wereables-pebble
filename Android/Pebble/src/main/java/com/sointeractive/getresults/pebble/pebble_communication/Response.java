package com.sointeractive.getresults.pebble.pebble_communication;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.PebbleDictionaryBuilder;

import java.util.List;

public enum Response {
    UNKNOWN(0, "Response: UNKNOWN RESPONSE", null),
    BEACONS_IN_RANGE(22, "Response: Sending beacons in range list", new SendBeaconsInRangeList()),
    BEACONS_OUT_OF_RANGE(23, "Response: Sending beacons out of range list", new SendBeaconsOutOfRangeList()),
    GAMES_ACTIVE(24, "Response: Sending active games list", new SendActiveGamesList()),
    GAMES_COMPLETED(25, "Response: Sending completed games list", new SendCompletedGamesList()),
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

    private static class SendBeaconsInRangeList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> beaconsInRange = DataProvider.getBeaconsInRange();
            return ResponseFactory.makeListResponse(id, beaconsInRange);
        }
    }

    private static class SendBeaconsOutOfRangeList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> beaconsOutOfRangeList = DataProvider.getBeaconsOutOfRange();
            return ResponseFactory.makeListResponse(id, beaconsOutOfRangeList);
        }
    }

    private static class SendActiveGamesList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> activeGameList = DataProvider.getActiveGames();
            return ResponseFactory.makeListResponse(id, activeGameList);
        }
    }

    private static class SendCompletedGamesList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> completedGameList = DataProvider.getCompletedGames();
            return ResponseFactory.makeListResponse(id, completedGameList);
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
            return ResponseFactory.makeSingleValueResponse(id, progress);
        }
    }

    private static class SendGameDetails implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final String gameDetails = DataProvider.getGameDetails(query);
            return ResponseFactory.makeSingleValueResponse(id, gameDetails);
        }
    }

    private static class SendUser implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final String login = DataProvider.getLogin();
            final int points = DataProvider.getPoints();
            return new PebbleDictionaryBuilder(id)
                    .addString(login)
                    .addInt(points)
                    .build();
        }

    }

}

