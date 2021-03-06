package com.sointeractive.getresults.pebble.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.sointeractive.getresults.pebble.R;
import com.sointeractive.getresults.pebble.config.IsaaCloudSettings;
import com.sointeractive.getresults.pebble.utils.Application;
import com.sointeractive.getresults.pebble.utils.CacheManager;
import com.sointeractive.getresults.pebble.utils.PebbleConnector;

import java.util.Observable;
import java.util.Observer;

public class PebbleActivity extends Activity implements Observer {
    private static final String TAG = PebbleActivity.class.getSimpleName();

    private Context context;
    private CheckBox checkBox;
    private Button login_button;
    private Button notification_send_button;
    private Button cache_clear_button;
    private TextView notification_title_text_view;
    private TextView notification_body_text_view;
    private TextView email_text_view;

    private PebbleConnector pebbleConnector;

    private void showInfo(final int id) {
        final String msg = context.getString(id);
        Log.i(TAG, "Info: Showing info: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "Event: onCreate");
        super.onCreate(savedInstanceState);

        initInstance();
        registerPebbleConnector();
        checkPebbleConnection();
        registerButtonHandlers();
    }

    private void initInstance() {
        Log.d(TAG, "Init: Initializing instance");
        setContentView(R.layout.pebble_activity);

        context = getApplicationContext();
        getGuiComponents();
        email_text_view.setText(IsaaCloudSettings.LOGIN_EMAIL);
    }

    private void getGuiComponents() {
        checkBox = (CheckBox) findViewById(R.id.pebble_connected_checkBox);
        notification_send_button = (Button) findViewById(R.id.notification_send_button);
        cache_clear_button = (Button) findViewById(R.id.cache_clear_button);
        notification_title_text_view = (TextView) findViewById(R.id.notification_title_text_view);
        notification_body_text_view = (TextView) findViewById(R.id.notification_body_text_view);
        login_button = (Button) findViewById(R.id.login_button);
        email_text_view = (TextView) findViewById(R.id.email_text_view);
    }

    private void registerPebbleConnector() {
        Log.d(TAG, "Init: Registering" + PebbleConnector.class.getSimpleName());
        pebbleConnector = Application.getPebbleConnector();
        pebbleConnector.addObserver(this);
    }

    private void checkPebbleConnection() {
        Log.d(TAG, "Init: Checking Pebble connection");
        if (pebbleConnector.isPebbleConnected()) {
            if (pebbleConnector.areAppMessagesSupported()) {
                showInfo(R.string.ok_connection_to_pebble);
                CacheManager.INSTANCE.setAutoReload(context);
                onPebbleConnected();
            } else {
                showInfo(R.string.app_messages_not_supported);
            }
        } else {
            showInfo(R.string.pebble_not_connected);
        }
    }

    private void registerButtonHandlers() {
        Log.d(TAG, "Init: Registering button click handlers");

        notification_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String title = notification_title_text_view.getText().toString();
                final String body = notification_body_text_view.getText().toString();
                pebbleConnector.sendNotification(title, body);
            }
        });

        cache_clear_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                CacheManager.INSTANCE.reload();
            }
        });

        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                IsaaCloudSettings.LOGIN_EMAIL = email_text_view.getText().toString();
                CacheManager.INSTANCE.reload();
            }
        });
    }

    @Override
    public void update(final Observable observable, final Object o) {
        Log.d(TAG, "Event: Observable value has changed");
        if (observable == pebbleConnector) {
            onConnectionStateChanged();
        }
    }

    private void onConnectionStateChanged() {
        if (pebbleConnector.isPebbleConnected()) {
            onPebbleConnected();
        } else {
            onPebbleDisconnected();
        }
    }

    private void onPebbleConnected() {
        Log.i(TAG, "Event: Pebble connected");
        checkBox.setChecked(true);
    }

    private void onPebbleDisconnected() {
        Log.i(TAG, "Event: Pebble disconnected");
        checkBox.setChecked(false);
    }
}
