package com.sointeractive.getresults.pebble.socket;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public abstract class SocketIO {
    static final String TAG = SocketIO.class.getSimpleName();

    private Socket socket;

    public SocketIO(final String address) {
        try {
            final IO.Options opts = getOptions();
            socket = IO.socket(address, opts);
            bindEvents();
        } catch (final URISyntaxException e) {
            Log.e(TAG, "Error: Websocket server address is not valid");
        }
    }

    private IO.Options getOptions() {
        final IO.Options opts = new IO.Options();
        opts.forceNew = false;
        opts.reconnection = true;
        return opts;
    }

    public void connect() {
        socket.connect();
    }

    Socket getSocket() {
        return socket;
    }

    private void bindEvents() {
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onConnect(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onConnectionError(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onConnectionTimeout(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onDisconnect(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onError(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onMessage(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_RECONNECT, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onReconnect(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_RECONNECT_ATTEMPT, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onReconnectAttempt(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_RECONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onReconnectError(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onReconnectFailed(getSafeString(args));
            }
        });

        socket.on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                onReconnecting(getSafeString(args));
            }
        });
    }

    String getSafeString(final Object... args) {
        if (args.length > 0) {
            return args[0].toString();
        } else {
            return "";
        }
    }

    abstract void onConnect(final String message);

    abstract void onReconnecting(final String safeString);

    abstract void onReconnectFailed(final String safeString);

    abstract void onReconnectError(final String safeString);

    abstract void onReconnectAttempt(final String safeString);

    abstract void onReconnect(final String safeString);

    abstract void onMessage(final String safeString);

    abstract void onError(final String safeString);

    abstract void onDisconnect(final String safeString);

    abstract void onConnectionTimeout(final String safeString);

    abstract void onConnectionError(final String message);
}
