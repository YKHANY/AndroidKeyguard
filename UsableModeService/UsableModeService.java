package com.android.server;

import android.content.Context;
import android.os.IUsableModeService;
import android.util.Log;

public class UsableModeService extends IUsableModeService.Stub {
	private static final String TAG = "UsableMode";
	private int usableMode;
	private int socketMode;
	private Context sContext;
	public UsableModeService(Context context) {
		super();
		usableMode = 0;
		socketMode = 0;
		sContext = context;
		Log.i(TAG, "Spawned worker thread");
	}
	
	public int getUsableMode() {
		// lock = 0,  unlock = 1
		Log.i(TAG, "Service : getUsableMode");
		return usableMode;
	}
	
	public void setUsableMode(int mode) {
		// lock = 0,  unlock = 1
		Log.i(TAG, "Service : setUsableMode");
		this.usableMode = mode;
	}

	public int getSocketMode() {
		// ing = 0,  finish = 1, 
		Log.i(TAG, "Service : getSocketMode");
		return socketMode;
	}
	
	public void setSocketMode(int mode) {
		// ing = 0,  finish = 1
		Log.i(TAG, "Service : setSocketMode");
		this.socketMode = mode;
	}
}
