package bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import lib.Protocol;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.util.Log;

	
public class BluetoothHandler extends Thread {
	
	private Activity activity;
	private static final String TAG = "Gamepad";
	private BluetoothManager manager;
	private BluetoothAdapter adapter;
	private BluetoothSocket socket;
	private OutputStream outputStream;
	private UUID ExpectedUUID;
	private SenderImpl si;
	private boolean stopped;
	
	public BluetoothHandler(Activity activity) {
		ExpectedUUID = java.util.UUID.fromString(Protocol.SERVER_UUID);
		this.activity = activity;
		si = new SenderImpl(this);
		initBluetoothAdapter();
		connectToBondedDevice();
		stopped = false;
		//startSendingTestData();
	}
	
	private void startSendingTestData() {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.d(TAG, "Sending data..");
		si.send((byte) 0x03, true);
		si.send((byte) 0x04, 0.4f);
		si.send((byte) 0x04, -0.6f);
		si.send((byte) 0x03, false);
		si.send((byte) 0xAC, true);
		si.send((byte) 0x42, true);
		si.send((byte) 0x24, true);
	}
	
	private void connectToBondedDevice() {
		boolean serverFound = false;
		if (adapter.getBondedDevices() != null && adapter.getBondedDevices().size() != 0) {
			Log.d(TAG, adapter.getBondedDevices().size() + " bounded devices");
			for(BluetoothDevice d : adapter.getBondedDevices()){
				Log.d(TAG, "\t" + d.getName());
				for (ParcelUuid uuid : d.getUuids()) {
					if (uuid.toString().equals(ExpectedUUID.toString())) {
						serverFound = true;
						Log.d(TAG, "Found a gamepad host at device" + d.getName() + " (" + d.getAddress() + ")");
					}
				}
				if (serverFound) {
					Log.d(TAG, "Connecting to server..");	
					connect(d.getAddress());
					return;
				}
			}
		}
		Log.d(TAG, "no servers found!");
	}
	
	private void initBluetoothAdapter() {
		adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null) {
			Log.d(TAG,"No bluetooth adapter detected!");
			return;
		} else {
			Log.d(TAG,"Bluetooth adapter \"" + adapter.getName() + "\" detected");
		}
		if (adapter.isEnabled()) {
			Log.d(TAG,"Bluetooth device is enabled");
		} else {
			Log.d(TAG,"Bluetooth device is disabled");
			Log.d(TAG,"Enabling bluetooth device..");
			Log.d(TAG,adapter.enable() ? "Success" : "Failed");
		}
	}
	
	public synchronized void send(byte[] data) {
		try {
			outputStream.write(data);
		} catch (Exception e) {
			Log.d(TAG, "Unable to send data. The server seems to be down, stopping communication..");
			stopped = true;
		}
	}

	public boolean connect(final String address) {
        if (adapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }



        final BluetoothDevice device = adapter.getRemoteDevice(address);
        
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        
        try {
			socket = device.createInsecureRfcommSocketToServiceRecord(ExpectedUUID);
			socket.connect();
			outputStream = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return true;
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
       // mBluetoothGatt = gattS
     //   Log.d(TAG, "Trying to create a new connection.");
//        mBluetoothDeviceAddress = address;
        //mConnectionState = STATE_CONNECTING;
    }
	
	@Override
	public void run() {
		while (!interrupted() && !stopped) {
			si.poll();
			Log.d(TAG, "poll");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void pressAllButtons() {
		for (int i = 0; i < 20; i++) {
			si.send((byte) i, true);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Log.d(TAG, "Unable to send data. The server seems to be down. Stopping bluetooth communication..");
				stopped = true;
			}
			si.send((byte) i, false);
		}
	}
}
