package android.os;
interface IUsableModeService {
/**
* {@hide}
*/
	int getUsableMode();
	void setUsableMode(int mode);
	int getSocketMode();
	void setSocketMode(int mode);
}
