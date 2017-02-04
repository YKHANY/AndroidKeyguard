package android.app;

import android.os.ISecmemService;
import android.os.RemoteException;

public class SecmemManager {
	private final ISecmemService mService;
	
	SecmemManager(ISecmemService service) {
		mService = service;
	}
	
	public boolean trying2Unlock() {
		boolean res = false;
		try{
			res = mService.trying2Unlock();
		} catch (RemoteException ex) {
			
		}
		return res;
	}

	public void trying2Unlock2(String ip, String port, String time) {
		try{
			mService.trying2Unlock2(ip, port, time);
		} catch (RemoteException ex) {
			
		}
	}
	
	public boolean trying2Lock() {
		boolean res = false;
		try{
			res = mService.trying2Lock();
		} catch (RemoteException ex) {
			
		}
		return res;
	}
}
