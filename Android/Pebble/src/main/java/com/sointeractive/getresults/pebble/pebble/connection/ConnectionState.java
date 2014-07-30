package com.sointeractive.getresults.pebble.pebble.connection;

public enum ConnectionState {
    CONNECTED(true),
    DISCONNECTED(false);

    private final boolean connected;

    ConnectionState(final boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}
