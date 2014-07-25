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
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.sointeractive.getresults.pebble.R;
import com.sointeractive.getresults.pebble.config.Settings;
import com.sointeractive.getresults.pebble.pebble.PebbleCommunicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class PebbleActivity extends Activity implements Observer {
    private static final String TAG = PebbleActivity.class.getSimpleName();

    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    public List<Beacon> beacons = new ArrayList<Beacon>();
    private Context context;
    private CheckBox checkBox;
    private Button notification_send_button;
    private BeaconManager beaconManager;

    private void showInfo(String msg) {
        Log.d(TAG, "Info: Showing info: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    private PebbleCommunicator getPebbleCommunicator() {
        return PebbleCommunicator.getCommunicator(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Event: onCreate");
        super.onCreate(savedInstanceState);

        initInstance();
        registerPebbleCommunicator();
        checkPebbleConnection();
        setBeaconManager();
        checkBluetooth();
        registerButtonHandlers();
    }

    private void initInstance() {
        Log.d(TAG, "Init: Initializing instance");
        setContentView(R.layout.pebble_activity);
        context = getApplicationContext();
        checkBox = (CheckBox) findViewById(R.id.pebble_connected_checkBox);
        notification_send_button = (Button) findViewById(R.id.notification_send_button);
    }

    private void registerPebbleCommunicator() {
        Log.d(TAG, "Init: Registering PebbleCommunicator");
        getPebbleCommunicator().addObserver(this);
    }

    private void checkPebbleConnection() {
        Log.d(TAG, "Init: Checking Pebble connection");
        PebbleCommunicator communicator = getPebbleCommunicator();
        if (communicator.isPebbleConnected()) {
            if (communicator.areAppMessagesSupported()) {
                showInfo("Connection to Pebble OK");
            } else {
                showInfo("Sorry, AppMessages are not supported");
            }
        } else {
            showInfo("Pebble not connected");
        }
    }

    private void setBeaconManager() {
        Log.d(TAG, "Init: Setting beacon manager");
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List<Beacon> newBeacons) {
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
        boolean hasBluetoothLE = beaconManager.hasBluetooth();
        Log.d(TAG, "Check: Smartphone " + (hasBluetoothLE ? "has" : "has not") + " got Bluetooth Low Energy");

        if (hasBluetoothLE) {
            checkBluetoothEnabled();
        } else {
            showInfo("Device does not have Bluetooth Low Energy");
        }
    }

    private void checkBluetoothEnabled() {
        boolean bluetoothEnabled = beaconManager.hasBluetooth();
        Log.d(TAG, "Check: Smartphone has bluetooth " + (bluetoothEnabled ? "enabled" : "disabled"));

        if (bluetoothEnabled) {
            connectToService();
        } else {
            Log.d(TAG, "Action: Trying to enable bluetooth by enableBtIntent");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Settings.REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Settings.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Event: Success on enabling bluetooth by enableBtIntent");
                connectToService();
            } else {
                Log.d(TAG, "Event: Failure on enabling bluetooth by enableBtIntent");
                showInfo("Bluetooth not enabled");
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
                } catch (RemoteException e) {
                    showInfo("Cannot find beacons");
                    Log.e(TAG, "Error: Cannot start ranging", e);
                }
            }
        });
    }

    private void registerButtonHandlers() {
        Log.d(TAG, "Init: Registering button click handlers");

        notification_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPebbleCommunicator().sendNotification("Test Message", "Whoever said nothing was impossible never tried to slam a revolving door.");
            }
        });
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
        } catch (RemoteException e) {
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
    public void update(Observable observable, Object o) {
        Log.d(TAG, "Event: Observable value has changed");
        if (observable == getPebbleCommunicator()) {
            onConnectionStateChanged();
        }
    }

    private void onConnectionStateChanged() {
        if (getPebbleCommunicator().connectionState) {
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
