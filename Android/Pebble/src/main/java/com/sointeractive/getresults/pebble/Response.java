package com.sointeractive.getresults.pebble;

import com.sointeractive.android.kit.util.PebbleDictionary;

import java.util.List;

public enum Response {
    RESPONSE_UNKNOWN(0, "Response: UNKNOWN RESPONSE", null),
    RESPONSE_BEACONS_IN_RANGE(22, "Response: Sending beacons in range list", new SendBeaconsInRangeList()),
    RESPONSE_BEACONS_OUT_OF_RANGE(23, "Response: Sending beacons out of range list", new SendBeaconsOutOfRangeList()),
    RESPONSE_GAMES_ACTIVE(24, "Response: Sending active games list", new SendActiveGamesList()),
    RESPONSE_GAMES_COMPLETED(25, "Response: Sending completed games list", new SendCompletedGamesList()),
    RESPONSE_LOGIN(26, "Response: Sending login", new SendLogin()),
    RESPONSE_BEACON_DETAILS(27, "Response: Sending beacon details", new SendBeaconDetails()),
    RESPONSE_PROGRESS(28, "Response: Sending progress", new SendProgress()),
    RESPONSE_GAME_DETAILS(29, "Response: Game details", new SendGameDetails());

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
            List<String> beaconsInRange = ResponseDataProvider.getBeaconsInRange();
            return ResponseFactory.makeListResponse(id, beaconsInRange);
        }
    }

    private static class SendBeaconsOutOfRangeList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> beaconsOutOfRangeList = ResponseDataProvider.getBeaconsOutOfRange();
            return ResponseFactory.makeListResponse(id, beaconsOutOfRangeList);
        }
    }

    private static class SendActiveGamesList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> activeGameList = ResponseDataProvider.getActiveGames();
            return ResponseFactory.makeListResponse(id, activeGameList);
        }
    }

    private static class SendCompletedGamesList implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            List<String> completedGameList = ResponseDataProvider.getCompletedGames();
            return ResponseFactory.makeListResponse(id, completedGameList);
        }
    }

    private static class SendBeaconDetails implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final int distance = ResponseDataProvider.getDistance(query);
            final int activeGamesCount = ResponseDataProvider.getActiveGamesCount(query);
            final int completedGamesCount = ResponseDataProvider.getCompletedGamesCount(query);

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
            final int progress = ResponseDataProvider.getProgress(query);
            return ResponseFactory.makeSingleValueResponse(id, progress);
        }
    }

    private static class SendGameDetails implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final String gameDetails = ResponseDataProvider.getGameDetails(query);
            return ResponseFactory.makeSingleValueResponse(id, gameDetails);
        }
    }

    private static class SendLogin implements ResponseAction {
        @Override
        public PebbleDictionary execute(int id, String query) {
            final String login = ResponseDataProvider.getLogin();
            return ResponseFactory.makeSingleValueResponse(id, login);
        }

    }

}

