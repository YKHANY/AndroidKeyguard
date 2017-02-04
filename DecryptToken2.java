package android.util;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class DecryptToken2 {
	private final String TAG = "DecryptToken";
	private String deviceRoot;
	private String deviceUUID;
	public DecryptToken2 () {
		deviceUUID = "secmem";
		deviceRoot = "C:/secmem";
	}
	public DecryptToken2(String deviceUUID, String deviceRoot) {
		this.deviceUUID = hashingUUID(deviceUUID);
		this.deviceRoot = deviceRoot;
	}
	// public key로 암호화 한 인증서 cipher.dat(인증날짜 기록)를 
	// pkv.dat파일의 modulus와 exponent 값을 통해 private key를 만들어 복호화
	public boolean HybridDecrypt(byte[] cipher, byte[] pkvM, byte[] pkvE, String deviceResult) throws Exception 
	{
		// 복호화 한다. 
		Cipher clsCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		KeyFactory fact = KeyFactory.getInstance("RSA");
		
		//BigInteger modulus = new BigInteger(AESDecrypt((byte[])in.readObject()));
		BigInteger modulus = new BigInteger(pkvM);

		//BigInteger privateExponent = new BigInteger(AESDecrypt((byte[])in.readObject()));
		BigInteger privateExponent = new BigInteger(pkvE);

		Log.i(TAG, "private key modulus(" + modulus + ") exponent(" + privateExponent + ")" );

		Key clsPrivateKey = fact.generatePrivate(new RSAPrivateKeySpec(modulus, privateExponent));
	    clsCipher.init( Cipher.DECRYPT_MODE, clsPrivateKey );
	    byte[] arrData = clsCipher.doFinal( cipher );
	    
	    String strResult = new String( arrData );
	    Log.i(TAG, "result(" + strResult + ")" );
	    return deviceResult.equals(strResult);
	}
	// pkv.dat파일의 modulus와 exponent 값을 UUID를 통해 복호화
	private byte[] AESDecrypt(byte[] encrypted) throws Exception {
		// 복호화 한다.
		byte[] seedB = deviceUUID.getBytes();
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seedB);

		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, sr); // 192 and 256 bits may not be available
		
		// Generate the secret key specs.
		SecretKey skey = kgen.generateKey();
		SecretKeySpec skeySpec = new SecretKeySpec(skey.getEncoded(), "AES");

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		byte[] decrypted = cipher.doFinal(encrypted);

		return decrypted;
	}
	private String hashingUUID(String deviceUUID) {
		byte[] salt = new byte[]{-119, -28, 51, -34, -114, -67, -43, -32, 7, -1, -27, -47, -105, -43, 111, -12};
		KeySpec spec = new PBEKeySpec(deviceUUID.toCharArray(), salt, 65536, 128);
		String res = null;
		try {
			SecretKeyFactory skeyfactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			byte[] hash = skeyfactory.generateSecret(spec).getEncoded();
			res = new BigInteger(1, hash).toString(16);
			Log.i(TAG, "salt: " + new BigInteger(1, salt).toString(16));
			Log.i(TAG, "hash: " + res);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	public String getDeviceUUID() {
		return deviceUUID;
	}
	public void setDeviceUUID(String deviceUUID) {
		this.deviceUUID = deviceUUID;
	}
	public String getDeviceRoot() {
		return deviceRoot;
	}
	public void setDeviceRoot(String deviceRoot) {
		this.deviceRoot = deviceRoot;
	}
}
