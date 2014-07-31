package com.sointeractive.getresults.pebble.isaacloud.notification;

import android.content.Intent;

import com.sointeractive.getresults.pebble.pebble.utils.Application;

public class IsaacloudNotification {
    private static final String ACTION = "com.isaacloud.action.ISAACLOUD_NOTIFICATION";

    private final String title;
    private final String body;

    public IsaacloudNotification(final String title, final String body) {
        this.title = title;
        this.body = body;
    }

    public void send() {
        final Intent i = getIntent();
        Application.context.sendBroadcast(i);
    }

    private Intent getIntent() {
        final Intent i = new Intent(ACTION);
        i.putExtra("title", title);
        i.putExtra("body", body);
        return i;
    }
}
