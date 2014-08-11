package com.sointeractive.getresults.pebble.socket;

public class SocketIONotifier extends SocketIOClient {
    public SocketIONotifier(final String address) {
        super(address);
        socket.emit("chat message", "{ \"token\" : \"abc\", \"url\" : \"/queues/notifications\"}");
    }


}
