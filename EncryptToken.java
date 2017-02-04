package android.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptToken {
	private final String TAG = "EncryptToken";
	private String usbRoot;
	private String deviceRoot;
	private String deviceUUID;
	public EncryptToken () {
		deviceUUID = "secmem";
		usbRoot = "C:/secmem";
		deviceRoot = "C:/secmem";
	}
	public EncryptToken(String deviceUUID, String usbRoot, String deviceRoot) {
		this.deviceUUID = hashingUUID(deviceUUID);
		this.usbRoot = usbRoot;
		this.deviceRoot = deviceRoot;
	}
	// public key와 private key를 keypair로 생성하고 private key의 modulus와 exponent를 pvk.dat에 저장
	// public key를 통해 인증서 cipher.dat(인증날짜 기록)을 저장
	public void HybridEncrypt() throws Exception {
		KeyPairGenerator clsKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
		clsKeyPairGenerator.initialize(2048);
		
		KeyPair clsKeyPair = clsKeyPairGenerator.genKeyPair();
		Key clsPublicKey = clsKeyPair.getPublic();
		Key clsPrivateKey = clsKeyPair.getPrivate();
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec clsPublicKeySpec = fact.getKeySpec( clsPublicKey, RSAPublicKeySpec.class);
		RSAPrivateKeySpec clsPrivateKeySpec = fact.getKeySpec( clsPrivateKey, RSAPrivateKeySpec.class);
		Log.i(TAG, "public key modulus(" + clsPublicKeySpec.getModulus( ) + ") exponent(" + clsPublicKeySpec.getPublicExponent( ) + ")" );
		Log.i(TAG, "private key modulus(" + clsPrivateKeySpec.getModulus( ) + ") exponent(" + clsPrivateKeySpec.getPrivateExponent( ) + ")" );

		// 암호화 한다.
		GregorianCalendar cal = new GregorianCalendar();
		String certificateData = "" + cal.getTime();
		Log.i(TAG, "result(" + certificateData + ")" );
		
		Cipher clsCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		clsCipher.init( Cipher.ENCRYPT_MODE, clsPublicKey );
		byte[] arrCipherData = clsCipher.doFinal(certificateData.getBytes( ) );
		
		File cipherFile = new File(usbRoot + "/cipher.dat");
		File deviceFile = new File(deviceRoot + "/date.dat");
		ObjectOutputStream out = null;
		try{ 
			out = new ObjectOutputStream(new FileOutputStream(cipherFile));
			out.writeObject(arrCipherData);
			out.flush();
			AESEncrypt(clsPrivateKeySpec.getModulus( ), clsPrivateKeySpec.getPrivateExponent( ));
			out = new ObjectOutputStream(new FileOutputStream(deviceFile));
			out.writeObject(certificateData);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 디바이스 UUID를 통해 인증서 복호화를 위한 pvk.dat 파일의
	// private key의 modulus와 exponent를 암호화
	private void AESEncrypt(BigInteger modulus, BigInteger exponent)
			throws Exception {
		/*byte[] seedB = deviceUUID.getBytes();
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
		sr.setSeed(seedB);
		
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, sr); // 192 and 256 bits may not be available
		// Generate the secret key specs.
		SecretKey skey = kgen.generateKey();

		SecretKeySpec skeySpec = new SecretKeySpec(skey.getEncoded(), "AES");
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

		byte[] encryptedModulus = cipher.doFinal(modulus.toByteArray());
		byte[] encryptedprivateExponent = cipher.doFinal(exponent.toByteArray());*/

		File pvkMFile = new File(usbRoot + "/pkvM.dat");
		File pvkEFile = new File(usbRoot + "/pkvE.dat");
		
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new FileOutputStream(pvkMFile));
			out.writeObject(modulus.toByteArray());
			out.flush();
			out = new ObjectOutputStream(new FileOutputStream(pvkEFile));
			out.writeObject(exponent.toByteArray());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
