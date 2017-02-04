package com.android.server;

import android.app.SecmemManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SecmemBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		Log.d("Secmem", "action : " + action);
		if (action.equals("android.intent.action.ALARM")) {
			Log.d("Secmem", "alarm_event catch!!");
			SecmemManager om = (SecmemManager)context.getSystemService("secmem");
			try{
			    Log.d("Secmem", "Going to call service");
			    om.trying2Lock();
			    Log.d("Secmem", "Service called successfully");
			}catch(Exception e){
			    Log.d("Secmem", "FAILED to call service");
			    e.printStackTrace();
			}
		} else if (action.equals("org.secmem.intent.fake.MODECHANGE")) {
			Log.d("Secmem", "MODECHANGE_event catch!!");
			Intent myIntent = new Intent(context, UnlockService.class);
			
			myIntent.putExtra("ip", intent.getStringExtra("ip"));
			myIntent.putExtra("port", intent.getStringExtra("port"));
			myIntent.putExtra("time", intent.getStringExtra("time"));
			try{
			    Log.d("Secmem", "Going to call service");
				context.startService(myIntent);
			    Log.d("Secmem", "Service called successfully");
			}catch(Exception e){
			    Log.d("Secmem", "FAILED to call service");
			    Log.d("Secmem", e.getMessage());
			}
		} 
	}
}
