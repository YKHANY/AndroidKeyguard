package android.app;

import android.os.IUsableModeService;
import android.os.RemoteException;

public class UsableModeManager {
	private final IUsableModeService mService;
	
	UsableModeManager(IUsableModeService service) {
		mService = service;
	}
	
	public int getUsableMode() {
		int res = 0;
		try{
			res = mService.getUsableMode();
		} catch (RemoteException ex) {
			
		}
		return res;
	}
	
	public void setUsableMode(int mode) {
		try{
			mService.setUsableMode(mode);
		} catch (RemoteException ex) {
			
		}
	}
	
	public int getSocketMode() {
		int res = 0;
		try{
			res = mService.getSocketMode();
		} catch (RemoteException ex) {
			
		}
		return res;
	}
	
	public void setSocketMode(int mode) {
		try{
			mService.setSocketMode(mode);
		} catch (RemoteException ex) {
			
		}
	}
}
