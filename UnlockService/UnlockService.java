package com.android.server;

import android.app.UsableModeManager;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.app.Service;
import android.os.Build;
import android.os.IBinder;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.DecryptToken;
import android.util.DecryptToken2;
import android.util.EncryptToken;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class UnlockService extends Service {
	private static final String TAG = "UnlockService";

	IUnlockService.Stub mStub = new IUnlockService.Stub() {
		public void unlock() {
			boolean res = false;
			Log.i(TAG, "Service(trick) : trying2Unlock");
			DecryptToken dec = new DecryptToken(
					getDeviceUUID(getApplicationContext()), Environment
							.getExternalStorageDirectory().getPath()
							+ "/usb/certificate", Environment
							.getExternalStorageDirectory().getPath()
							+ "/certificate");
			try {
				res = dec.HybridDecrypt();
				if (res) {
					UsableModeManager om = (UsableModeManager) getApplicationContext()
							.getSystemService("usable_mode");
					om.setUsableMode(1);

					EncryptToken enc = new EncryptToken(
							getDeviceUUID(getApplicationContext()), Environment
									.getExternalStorageDirectory().getPath()
									+ "/usb/certificate", Environment
									.getExternalStorageDirectory().getPath()
									+ "/certificate");
					try {
						// 인증서 날짜기록 암호화
						enc.HybridEncrypt();
					} catch (Exception e) {
						Log.i(TAG, e.getMessage());
					}
					Log.i(TAG, "success unlock event");
				} else {
					Log.i(TAG, "failed unlock event");
				}
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
		}
	};

	public void onCreate() {
		super.onCreate();
	}

	public IBinder onBind(Intent intent) {
		return mStub;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "service start command!");
		String hostName = intent.getStringExtra("ip");
		String port = intent.getStringExtra("port");
		String time = intent.getStringExtra("time");
		int portNum = 10500;
		int timeNum = 600;
		try {
			portNum = Integer.parseInt(port);
			timeNum = Integer.parseInt(time);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
		}
		Log.i(TAG, "thread making!");
		MyConnectionThread conn = new MyConnectionThread(hostName, portNum, timeNum, getApplicationContext());
		conn.start();
		return Service.START_STICKY;
	}
	public void onDestroy() {
		super.onDestroy();
	}
	
	private String getDeviceUUID(Context context) {
		String SerId="";
		try {
			SerId = (String)Build.class.getField("SERIAL").get(null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String AndroidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		return SerId + AndroidId;
	}
	private class MyConnectionThread extends Thread {
		String hostName;
		int port;
		int time;
		Context tContext;
		public MyConnectionThread(String hostName, int port, int time, Context context) {
			this.hostName = hostName;
			this.port = port;
			this.time = time;
			this.tContext = context;
		}
		public void run() {
			Log.i(TAG, "myConnectionThread start!");
			try{
				Socket socket = new Socket(hostName, port);
				InputStream is = socket.getInputStream();
				OutputStream os = socket.getOutputStream();
				
				DataOutputStream dos = new DataOutputStream(os);
				Log.d(TAG, "socket start");
				dos.writeUTF("trying2Unlock");
				dos.flush();
				
				ObjectInputStream obin = new ObjectInputStream(new BufferedInputStream(is));
				byte[] cipher = (byte[])(obin.readObject());
				byte[] pkvM = (byte[])(obin.readObject());
				byte[] pkvE = (byte[])(obin.readObject());
				
				Log.d(TAG, "socket success");
				
				boolean res = false;
				
				DecryptToken2 de = new DecryptToken2(getDeviceUUID(getApplicationContext()), Environment.getExternalStorageDirectory().getPath()+"/certificate");
				try { 
	    				String deviceResult = getDeviceUUID(getApplicationContext());
					res = de.HybridDecrypt(cipher, pkvM, pkvE, deviceResult);
					if (res) {
						UsableModeManager om = (UsableModeManager)getSystemService("usable_mode");
						om.setUsableMode(1);
						om.setSocketMode(1);
						Intent myintent = new Intent("org.secmem.intent.fake.MODECHANGE_SUCCESS");
						myintent.putExtra("time", time);
						Log.d(TAG, "unlock_MODECHANGE_SUCCESS!!");
						tContext.sendBroadcast(myintent);
					} else {
						Intent myintent = new Intent("org.secmem.intent.fake.MODECHANGE_FAILED");
						Log.d(TAG, "unlock_MODECHANGE_FAILED!!");
						tContext.sendBroadcast(myintent);
					}
				} catch (Exception e) {
					Intent myintent = new Intent("org.secmem.intent.fake.MODECHANGE_FAILED");
					tContext.sendBroadcast(myintent);
					Log.d(TAG, "unlock_MODECHANGE_FAILED!!");
				}
				
			} catch (Exception e) {
				Intent myintent = new Intent("org.secmem.intent.fake.SOCKET_FAILED");
				tContext.sendBroadcast(myintent);
				Log.d(TAG, "socket failed");
			}
		}
	}
}
