package com.android.server;

import android.content.ComponentName;
import com.android.server.SecmemBroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.app.UsableModeManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ISecmemService;
import android.provider.Settings.Secure;
import android.util.Log;

public class SecmemService extends ISecmemService.Stub {
	private static final String TAG = "Secmem";
	private String deviceUUID;
	private SecmemWorkerThread sWorker;
	private SecmemWorkerHandler sHandler;   
	private Context sContext;
	private IUnlockService mUnlock;
	private ServiceConnection mSrvConn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.i(TAG, "RPC Service Failed");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mUnlock = IUnlockService.Stub.asInterface(service);
			try {
				mUnlock.unlock();
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
	};
	public SecmemService(Context context) {
		super();
		sContext = context;
		deviceUUID = getDeviceUUID(context);
		//sWorker = new SecmemWorkerThread("SecmemServiceWorker");
		//sWorker.start();
		Log.i(TAG, "Spawned worker thread");
	}
	
	public boolean trying2Unlock() {
		boolean res = false;
		Log.i(TAG, "Service : trying2Unlock");
		Intent intent = new Intent();
		intent.setAction("com.android.server.UnlockService");
		try{
	        Log.d("Secmem", "Going to call service");
	        sContext.bindService(intent, mSrvConn, sContext.BIND_AUTO_CREATE);
	        Log.d("Secmem", "Service called successfully");
	    } catch(Exception e){
	        Log.d("Secmem", "FAILED to call service");
	        e.printStackTrace();
	    }
		UsableModeManager om = (UsableModeManager) sContext
					.getSystemService("usable_mode");
		if (om.getUsableMode() == 1) {
			res = true;
		}
		sContext.unbindService(mSrvConn);
		return res;
		//Message msg = Message.obtain();
		//msg.what = SecmemWorkerHandler.MESSAGE_TRY;
		//sHandler.sendMessage(msg);
	}
	
	public void trying2Unlock2(String ip, String port, String time) {
		Log.i(TAG, "Service : trying2Unlock2");
		Intent myintent = new Intent(sContext, SecmemBroadcastReceiver.class);
		myintent.putExtra("ip", ip);
		myintent.putExtra("port", port);
		myintent.putExtra("time", time);
		myintent.setAction("org.secmem.intent.fake.MODECHANGE");
		sContext.sendBroadcast(myintent);
		Log.d("Secmem", "send broadcast start!");
	}

	public boolean trying2Lock() {
		boolean res = false;
		Log.i(TAG, "Service : trying2Lock");
		UsableModeManager om = (UsableModeManager)sContext.getSystemService("usable_mode");
		om.setUsableMode(0);
		om.setSocketMode(0);
		if (om.getUsableMode() == 0 && om.getSocketMode() == 0) {
			res = true;
		}
		Log.i(TAG, "success lock event");
		
		//Message msg = Message.obtain();
		//msg.what = SecmemWorkerHandler.MESSAGE_TRY;
		//sHandler.sendMessage(msg);
		return res;
	}
	
	private class SecmemWorkerThread extends Thread {
		public SecmemWorkerThread (String name) {
			super(name);
		}
		public void run() {
			Looper.prepare();
			sHandler = new SecmemWorkerHandler();
			Looper.loop();
		}
	}
	
	private class SecmemWorkerHandler extends Handler {
		private static final int MESSAGE_CHECK = 0;
		private static final int MESSAGE_TRY = 1;
		public void handlerMessage(Message msg) {
			try{
				if (msg.what == MESSAGE_CHECK) {
					Log.i(TAG, "check security mode ");
					
				}
				else if (msg.what == MESSAGE_TRY) {
					Log.i(TAG, "trying to unlock using a usb security token");
					
				}
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
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
}
