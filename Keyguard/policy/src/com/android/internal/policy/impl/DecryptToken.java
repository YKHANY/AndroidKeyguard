package com.android.internal.policy.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
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

import android.util.Log;

public class DecryptToken {
	private final String TAG = "DecryptToken";
	private String usbRoot;
	private String deviceRoot;
	private String deviceUUID;
	public DecryptToken () {
		deviceUUID = "secmem";
		usbRoot = "C:/secmem";
		deviceRoot = "C:/secmem";
	}
	public DecryptToken(String deviceUUID, String usbRoot, String deviceRoot) {
		this.deviceUUID = hashingUUID(deviceUUID);
		this.usbRoot = usbRoot;
		this.deviceRoot = deviceRoot;
	}
	public boolean HybridDecrypt() throws Exception 
	{
		Cipher clsCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		KeyFactory fact = KeyFactory.getInstance("RSA");
		
		File cipherFile = new File(usbRoot + "/cipher.dat");
		File pvkMFile = new File(usbRoot + "/pkvM.dat");
		File pvkEFile = new File(usbRoot + "/pkvE.dat");
		File deviceFile = new File(deviceRoot + "/date.dat");
		
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(cipherFile));
		byte[] cipherDataArr = (byte[])in.readObject();
		
		in = new ObjectInputStream(new FileInputStream(pvkMFile));
		//BigInteger modulus = new BigInteger(AESDecrypt((byte[])in.readObject()));
		BigInteger modulus = new BigInteger((byte[])in.readObject());

		in = new ObjectInputStream(new FileInputStream(pvkEFile));
		//BigInteger privateExponent = new BigInteger(AESDecrypt((byte[])in.readObject()));
		BigInteger privateExponent = new BigInteger((byte[])in.readObject());

		Log.i(TAG, "private key modulus(" + modulus + ") exponent(" + privateExponent + ")" );

		Key clsPrivateKey = fact.generatePrivate(new RSAPrivateKeySpec(modulus, privateExponent));
	    clsCipher.init( Cipher.DECRYPT_MODE, clsPrivateKey );
	    byte[] arrData = clsCipher.doFinal( cipherDataArr );
	    
	    in = new ObjectInputStream(new FileInputStream(deviceFile));
	    String deviceResult = (String)in.readObject();
	    String strResult = new String( arrData );
	    Log.i(TAG, "result(" + strResult + ")" );
	    return deviceResult.equals(strResult);
	}
	private byte[] AESDecrypt(byte[] encrypted) throws Exception {
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
	public String getUsbRoot() {
		return usbRoot;
	}
	public void setUsbRoot(String usbRoot) {
		this.usbRoot = usbRoot;
	}
	public String getDeviceRoot() {
		return deviceRoot;
	}
	public void setDeviceRoot(String deviceRoot) {
		this.deviceRoot = deviceRoot;
	}
}
