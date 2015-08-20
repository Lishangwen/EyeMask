package com.example.bletest;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 0;
	BluetoothAdapter mBluetoothAdapter;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Use this check to determine whether BLE is supported on the device. Then
//		// you can selectively disable BLE-related features.
//		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//		    Toast.makeText(this, "no support device", Toast.LENGTH_SHORT).show();
//		    finish();
//		}
//		
//		// Initializes Bluetooth adapter.
//		final BluetoothManager bluetoothManager =
//		        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//		 mBluetoothAdapter = bluetoothManager.getAdapter();
//		 
//		// Ensures Bluetooth is available on the device and it is enabled. If not,
//		// displays a dialog requesting user permission to enable Bluetooth.
//		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//		}
		
	    Intent intent = new Intent(MainActivity.this,DeviceScanActivity.class);
	    startActivity(intent);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
