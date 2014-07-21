package com.sointeractive.getresults.pebble.Activities;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.sointeractive.android.kit.PebbleKit;
import com.sointeractive.android.kit.util.PebbleDictionary;
import com.sointeractive.getresults.pebble.PebbleCommunication.Request;
import com.sointeractive.getresults.pebble.PebbleCommunication.Response;
import com.sointeractive.getresults.pebble.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PebbleActivity extends Activity {
    private static final UUID PEBBLE_APP_UUID = UUID.fromString("51b19145-0542-474f-8b62-c8c34ae4b87b");
    private static final String APP_NAME = "GetResults!";
    private static final String TAG = PebbleActivity.class.getSimpleName();

    private final PebbleKit.PebbleDataReceiver receivedDataHandler = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
        @Override
        public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
            Log.d(TAG, "Event: message received, value: " + data.toJsonString());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Action: Acknowledgement sent to Pebble, transactionId: " + transactionId);
                    PebbleKit.sendAckToPebble(context, transactionId);

                    receivedDataAction(data);
                }
            });
        }
    };

    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private static final int REQUEST_ENABLE_BT = 1234;
    private final Handler handler = new Handler();
    public List<Beacon> beacons = new ArrayList<Beacon>();
    private Context context;
    private CheckBox checkBox;
    private Button notification_send_button;
    private BeaconManager beaconManager;

    private void receivedDataAction(PebbleDictionary data) {
        Request request = Request.getByData(data);
        Log.d(TAG, request.getLogMessage());
        if (request != Request.REQUEST_UNKNOWN) {
            sendResponse(request.getResponse());
        }
    }

    private void sendResponse(Response response) {
        Log.d(TAG, response.getLogMessage());
        if (response != Response.RESPONSE_UNKNOWN) {
            sendDataToPebble(response.getDataToSend());
        }
    }

    private void sendDataToPebble(PebbleDictionary data) {
        if (isPebbleConnected()) {
            Log.d(TAG, "Action: sending response: " + data.toJsonString());
            PebbleKit.sendDataToPebble(context, PEBBLE_APP_UUID, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Event: onCreate");
        super.onCreate(savedInstanceState);

        initInstance();
        addActionBarBack();
        initPebble();
        registerButtonHandlers();
        setBeaconManager();
        checkBluetooth();
    }

    private void initInstance() {
        setContentView(R.layout.pebble_communication);
        context = getApplicationContext();
        checkBox = (CheckBox) findViewById(R.id.pebble_connected_checkBox);
        notification_send_button = (Button) findViewById(R.id.notification_send_button);
    }

    private void addActionBarBack() {
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initPebble() {
        if (isPebbleConnected()) {
            PebbleKit.startAppOnPebble(context, PEBBLE_APP_UUID);
            onConnectAction();
            if (areAppMessagesSupported()) {
                showInfo("Connection to Pebble OK");
            } else {
                showInfo("Sorry, AppMessages are not supported");
            }
        } else {
            showInfo("Pebble not connected");
        }
    }

    private void registerButtonHandlers() {
        Log.d(TAG, "Handlers: Registering button click handlers");

        notification_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendNotification("Test Message", "Whoever said nothing was impossible never tried to slam a revolving door.");
            }
        });
    }

    private void setBeaconManager() {
        Log.d(TAG, "Action: Setting new beacon manager");
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
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
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

    private boolean isPebbleConnected() {
        boolean connected = PebbleKit.isWatchConnected(context);
        Log.d(TAG, "Check: Pebble is " + (connected ? "connected" : "not connected"));

        if (connected) {
            onConnectAction();
        } else {
            onDisconnectAction();
        }

        return connected;
    }

    private void onConnectAction() {
        checkBox.setChecked(true);
    }

    private void onDisconnectAction() {
        checkBox.setChecked(false);
    }

    private boolean areAppMessagesSupported() {
        boolean appMessagesSupported = PebbleKit.areAppMessagesSupported(context);
        Log.d(TAG, "Check: AppMessages " + (appMessagesSupported ? "are supported" : "are not supported"));
        return appMessagesSupported;
    }

    private void showInfo(String msg) {
        Log.d(TAG, "Info: Showing info: " + msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "Event: onResume");
        super.onResume();

        registerMessageHandlers();
    }

    private void registerMessageHandlers() {
        Log.d(TAG, "Handlers: Registering received data handler");
        PebbleKit.registerReceivedDataHandler(this, receivedDataHandler);
    }

    private void sendNotification(String title, String body) {
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map<String, String> data = new HashMap<String, String>();
        data.put("title", title);
        data.put("body", body);

        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", APP_NAME);
        i.putExtra("notificationData", notificationData);

        Log.d(TAG, "Notification: sending: " + notificationData);
        sendBroadcast(i);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Event: onPause");
        unregisterMessageHandler();

        super.onPause();
    }

    private void unregisterMessageHandler() {
        Log.d(TAG, "Handlers: Unregistering received data handler");
        unregisterReceiver(receivedDataHandler);
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

        closeAppOnPebble();
        beaconManager.disconnect();

        super.onDestroy();
    }

    private void closeAppOnPebble() {
        Log.d(TAG, "Action: Closing app on Pebble");
        PebbleKit.closeAppOnPebble(context, PEBBLE_APP_UUID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "Event: Menu item selected");
        return super.onOptionsItemSelected(item);
    }
}
