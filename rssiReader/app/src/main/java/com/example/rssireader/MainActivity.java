package com.example.rssireader;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
	private static String TAG = "_RSSI";
	private static int INTERVAL_FOR_RSSI = 15000; //in mSec
	protected WifiManager mWifiManager;
	private TextView ssidTextView;
	private TextView rssiTextView;
	private Button startButton;
	private Handler wifiScanHandler;
	private MyBroadcastReceiver myBroadcastReceiver;
	private IntentFilter intentFilter;
	private File outputFile;
	private FileOutputStream foo;
	private Runnable wifiScan = new Runnable() {
		@Override
		public void run() {
			if (startButton.getText().equals("Stop")) {
				scanForWifi();
				wifiScanHandler.postDelayed(wifiScan, INTERVAL_FOR_RSSI);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		String requiredpermissions[] = {
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.WRITE_EXTERNAL_STORAGE};
		permissions(this, requiredpermissions);

		ssidTextView = (TextView) findViewById(R.id.setSSID);
		rssiTextView = (TextView) findViewById(R.id.textViewRssi);
		startButton = (Button) findViewById(R.id.start_stop);
		wifiScanHandler = new Handler();
		intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

		myBroadcastReceiver = new MyBroadcastReceiver(this);
		registerReceiver(myBroadcastReceiver, intentFilter);

		outputFile = new File(
				"/storage/self/primary",
				"Rssi" + new Random().nextInt(100));
		if (!outputFile.exists()) {
			try {
				outputFile.createNewFile();
				Log.i(TAG,"File Created");
			} catch (IOException e) {
				e.printStackTrace();
				Log.i(TAG, "File not Created");
			}
		}
	}


	public void start_stop(View v) throws IOException {
		Log.i(TAG, String.valueOf(ssidTextView.getText()));
		if (startButton.getText().equals("Start")) {
			scanForWifi();
			wifiScanHandler.postDelayed(wifiScan, INTERVAL_FOR_RSSI);
			startButton.setText("Stop");

			foo = new FileOutputStream(outputFile);

		} else if (startButton.getText().equals("Stop")) {
			startButton.setText("Start");
			rssiTextView.setText("0");

			foo.close();
		}
	}

	public void scanForWifi() {
		mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		mWifiManager.startScan();

	}

	public void wifiScanSuccess() throws IOException {
		List<ScanResult> mWifiList = mWifiManager.getScanResults();
		boolean ssidfound = false;
		String ssid = String.valueOf(ssidTextView.getText());
		for (ScanResult scanResult : mWifiList) {
			if (scanResult.SSID.equals(ssid)) {
				ssidfound = true;
				rssiTextView.setText(String.valueOf(scanResult.level));

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd G 'at' HH:mm:ss.SSSZ");
				String date = df.format(Calendar.getInstance().getTime());

				String data = date + " " + scanResult.SSID + " " + scanResult.level + "\n";

				foo.write(data.getBytes());

				Log.i(TAG, data);
			}
		}
		if (ssidfound == false) {
			startButton.setText("Start");
			rssiTextView.setText("0");
			Log.i(TAG, ssid + " Not Found");

			foo.close();

			Toast.makeText(this, "SSID not Found", Toast.LENGTH_SHORT).show();
		}


	}

	public void wifiScanFailure() throws IOException {
		// handle failure: new scan did NOT succeed
		// consider using old scan results: these are the OLD results!
		Log.i(TAG, "Previous Results");
		List<ScanResult> mWifiList = mWifiManager.getScanResults();
		boolean ssidfound = false;
		String ssid = String.valueOf(ssidTextView.getText());
		for (ScanResult scanResult : mWifiList) {
			if (scanResult.SSID.equals(ssid)) {
				ssidfound = true;
				rssiTextView.setText(String.valueOf(scanResult.level));

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd G 'at' HH:mm:ss.SSSZ");
				String date = df.format(Calendar.getInstance().getTime());

				String data = date + " " + scanResult.SSID + " " + scanResult.level + "\n";

				foo.write(data.getBytes());

				Log.i(TAG, data);
			}
		}
		if (ssidfound == false) {
			startButton.setText("Start");
			rssiTextView.setText("0");
			Log.i(TAG, ssid + " Not Found");

			foo.close();

			Toast.makeText(this, "SSID not Found", Toast.LENGTH_SHORT).show();
		}
	}

	public void permissions(MainActivity thisActivity, String[] requiredPermission) {
		// Here, thisActivity is the current activity
		LinkedList<String> premissionList = new LinkedList<>();
		for (int i = 0; i < requiredPermission.length; i++) {
			if (ContextCompat.checkSelfPermission(thisActivity,
					requiredPermission[i])
					!= PackageManager.PERMISSION_GRANTED) {
				premissionList.add(requiredPermission[i]);
			}
		}
		if (premissionList.size() != 0) {

			String[] temp = new String[premissionList.size()];
			premissionList.toArray(temp);
			ActivityCompat.requestPermissions(thisActivity,
					temp,
					111);
		}
	}


	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case 111: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay! Do the
					// contacts-related task you need to do.
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}


			// other 'case' lines to check for other
			// permissions this app might request.
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(myBroadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(myBroadcastReceiver, intentFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (outputFile.exists()) {
			if (outputFile.length() == 0) {
				outputFile.delete();

			}
		}
	}
}


class MyBroadcastReceiver extends BroadcastReceiver {

	private MainActivity mActivity;

	public MyBroadcastReceiver(MainActivity mActivity) {
		super();
		this.mActivity = mActivity;
	}

	@Override
	public void onReceive (Context context, Intent intent) {
		String key = intent.getAction();
		Log.i("_RSSI", key);
		if (mActivity.mWifiManager != null) {
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(key)) {
				boolean success = intent.getBooleanExtra(
						WifiManager.EXTRA_RESULTS_UPDATED, false);
				if (success) {
					Log.i("_RSSI", "Sucess");
					try {
						mActivity.wifiScanSuccess();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Log.i("_RSSI", "Failed");
					// scan failure handling
					try {
						mActivity.wifiScanFailure();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
