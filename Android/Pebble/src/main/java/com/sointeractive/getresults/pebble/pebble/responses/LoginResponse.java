package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

public class LoginResponse implements ResponseItem {
    public final String roomName;
    private final String name;
    private final int points;
    private final int rank;
    private final int beaconsSize;

    public LoginResponse(final String name, final int points, final int rank, final String roomName, final int beaconsSize) {
        this.name = name;
        this.points = points;
        this.rank = rank;
        this.roomName = roomName;
        this.beaconsSize = beaconsSize;
    }

    @Override
    public PebbleDictionary getData(final int responseType) {
        return new DictionaryBuilder(responseType)
                .addString(name)
                .addString(roomName)
                .addInt(points)
                .addInt(rank)
                .addInt(beaconsSize)
                .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LoginResponse that = (LoginResponse) o;
        return rank == that.rank &&
                points == that.points &&
                name.equals(that.name) &&
                roomName.equals(that.roomName);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + points;
        result = 31 * result + rank;
        result = 31 * result + roomName.hashCode();
        return result;
    }
}
