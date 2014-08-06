package com.sointeractive.getresults.pebble.pebble.responses;

import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.config.PebbleSettings;
import com.sointeractive.getresults.pebble.utils.DictionaryBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AchievementResponse implements ResponseItem {
    private static final int RESPONSE_HEADER_ID = 4;
    private static final int RESPONSE_ITEM_ID = 6;

    private final int id;
    private final String name;
    private final String description;

    public AchievementResponse(final int id, final String name, final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public static Queue<String> partitionDescription(final String text, final int size) {
        final Queue<String> descriptionParts = new LinkedList<String>();
        for (int start = 0; start < text.length(); start += size) {
            descriptionParts.add(text.substring(start, Math.min(text.length(), start + size)));
        }
        return descriptionParts;
    }

    @Override
    public List<PebbleDictionary> getData() {
        final List<PebbleDictionary> data = new LinkedList<PebbleDictionary>();
        final Queue<String> descriptionParts = partitionDescription(description, PebbleSettings.MAX_ACHIEVEMENTS_DESCTIPTION_STR_LEN);
        final int responsesNumber = descriptionParts.size();

        final PebbleDictionary header = new DictionaryBuilder(RESPONSE_HEADER_ID)
                .addInt(id)
                .addString(name)
                .addInt(responsesNumber)
                .build();
        data.add(header);

        while (!descriptionParts.isEmpty()) {
            final PebbleDictionary item = new DictionaryBuilder(RESPONSE_ITEM_ID)
                    .addInt(id)
                    .addString(descriptionParts.poll())
                    .addInt(responsesNumber - descriptionParts.size())
                    .build();
            data.add(item);
        }
        return data;
    }
}
