package android.os;
interface ISecmemService {
/**
* {@hide}
*/
	boolean trying2Unlock();
	void trying2Unlock2(String ip, String port, String time);
	boolean trying2Lock();
}
