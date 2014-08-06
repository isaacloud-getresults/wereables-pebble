package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

import java.util.List;

public class BeaconResponse implements ResponseItem {
    private static final int RESPONSE_ID = 2;

    public final int id;
    public final String name;
    private final int people;

    public BeaconResponse(final int id, final String name, final int people) {
        this.id = id;
        this.name = name;
        this.people = people;
    }

    @Override
    public List<PebbleDictionary> getData() {
        return new DictionaryBuilder(RESPONSE_ID)
                .addInt(id)
                .addString(name)
                .addInt(people)
                .pack();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BeaconResponse that = (BeaconResponse) o;
        return id == that.id &&
                people == that.people &&
                name.equals(that.name);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + people;
        return result;
    }
}
