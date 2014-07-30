package com.sointeractive.getresults.pebble.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.sointeractive.getresults.pebble.R;
import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.pebble.communication.PebbleConnector;
import com.sointeractive.getresults.pebble.pebble.utils.Application;
import com.sointeractive.getresults.pebble.pebble.utils.CacheReloader;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class PebbleActivity extends Activity implements Observer {
    private static final String TAG = PebbleActivity.class.getSimpleName();

    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private List<Beacon> beacons = new ArrayList<Beacon>();
    private Context context;
    private CheckBox checkBox;
    private Button notification_send_button;
    private TextView notification_title_text_view;
    private TextView notification_body_text_view;

    private BeaconManager beaconManager;
    private PebbleConnector pebbleConnector;

    private void showInfo(final int id) {
        final String msg = context.getString(id);
        Log.d(TAG, "Info: Showing info: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(TAG, "Event: onCreate");
        super.onCreate(savedInstanceState);

        initInstance();
        registerPebbleConnector();
        checkPebbleConnection();
        setBeaconManager();
        checkBluetooth();
        registerButtonHandlers();
        preloadIsaacloudData();
    }

    private void initInstance() {
        Log.d(TAG, "Init: Initializing instance");
        setContentView(R.layout.pebble_activity);
        context = getApplicationContext();
        checkBox = (CheckBox) findViewById(R.id.pebble_connected_checkBox);
        notification_send_button = (Button) findViewById(R.id.notification_send_button);
        notification_title_text_view = (TextView) findViewById(R.id.notification_title_text_view);
        notification_body_text_view = (TextView) findViewById(R.id.notification_body_text_view);
    }

    private void registerPebbleConnector() {
        Log.d(TAG, "Init: Registering" + PebbleConnector.class.getSimpleName());
        pebbleConnector = Application.pebbleConnector;
        pebbleConnector.addObserver(this);
    }

    private void checkPebbleConnection() {
        Log.d(TAG, "Init: Checking Pebble connection");
        if (pebbleConnector.isPebbleConnected()) {
            if (pebbleConnector.areAppMessagesSupported()) {
                showInfo(R.string.ok_connection_to_pebble);
            } else {
                showInfo(R.string.app_messages_not_supported);
            }
        } else {
            showInfo(R.string.pebble_not_connected);
        }
    }

    private void setBeaconManager() {
        Log.d(TAG, "Init: Setting beacon manager");
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(final Region region, final List<Beacon> newBeacons) {
                Log.d(TAG, "Event: New beacons discovered");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        beacons = new ArrayList<Beacon>(newBeacons);
                    }
                });
            }
        });
    }

    private void checkBluetooth() {
        final boolean hasBluetoothLE = beaconManager.hasBluetooth();
        Log.d(TAG, "Check: Smartphone " + (hasBluetoothLE ? "has" : "has not") + " got Bluetooth Low Energy");

        if (hasBluetoothLE) {
            checkBluetoothEnabled();
        } else {
            showInfo(R.string.bluetooth_low_energy_not_supported);
        }
    }

    private void checkBluetoothEnabled() {
        final boolean bluetoothEnabled = beaconManager.hasBluetooth();
        Log.d(TAG, "Check: Smartphone has bluetooth " + (bluetoothEnabled ? "enabled" : "disabled"));

        if (bluetoothEnabled) {
            connectToService();
        } else {
            Log.d(TAG, "Action: Trying to enable bluetooth by enableBtIntent");
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Settings.REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Settings.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Event: Success on enabling bluetooth by enableBtIntent");
                connectToService();
            } else {
                Log.d(TAG, "Event: Failure on enabling bluetooth by enableBtIntent");
                showInfo(R.string.bluetooth_not_enabled);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToService() {
        Log.d(TAG, "Action: Connecting to beacon scan service");
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (final RemoteException e) {
                    showInfo(R.string.scan_beacons_error);
                    Log.e(TAG, "Error: Cannot start ranging", e);
                }
            }
        });
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
    }

    private void preloadIsaacloudData() {
        CacheReloader.INSTANCE.reload();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "Event: onStop");
        stopRanging();

        super.onStop();
    }

    private void stopRanging() {
        Log.d(TAG, "Action: Stopping ranging");
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (final RemoteException e) {
            Log.e(TAG, "Error: while stopping ranging", e);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "Event: onDestroy");

        beaconManager.disconnect();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Event: onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Event: onPause");
        super.onPause();
    }

    @Override
    public void update(final Observable observable, final Object o) {
        Log.d(TAG, "Event: Observable value has changed");
        if (observable == pebbleConnector) {
            onConnectionStateChanged();
        }
    }

    private void onConnectionStateChanged() {
        if (pebbleConnector.connectionState) {
            onPebbleConnected();
        } else {
            onPebbleDisconnected();
        }
    }

    private void onPebbleConnected() {
        Log.d(TAG, "Event: Pebble connected");
        checkBox.setChecked(true);
    }

    private void onPebbleDisconnected() {
        Log.d(TAG, "Event: Pebble disconnected");
        checkBox.setChecked(false);
    }
}
