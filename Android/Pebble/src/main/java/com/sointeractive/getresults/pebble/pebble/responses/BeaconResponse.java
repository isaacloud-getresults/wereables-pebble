package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

import java.util.Collection;

public class BeaconResponse implements ResponseItem {
    public final int id;
    public final String name;
    private final int people;

    public BeaconResponse(final int id, final String name, final int people) {
        this.id = id;
        this.name = name;
        this.people = people;
    }

    @Override
    public Collection<PebbleDictionary> getData(final int responseType) {
        return new DictionaryBuilder(responseType)
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
