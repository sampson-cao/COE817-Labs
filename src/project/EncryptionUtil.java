package project;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.security.MessageDigest;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


/**
 * Helper class that generates and manages keys and functions involving
 * cryptography for the chat application
 * 
 * @author Sampson Cao
 *
 */
public class EncryptionUtil {
	
	private final static int PACKET_SIZE = 1024;

	/** Cipher for DES algorithm */
	private static Cipher cipherDES = null;
	/** Cipher for RSA algorithm */
	private static Cipher cipherRSA = null;

	/** User's own public key */
	private static PublicKey puKey;
	/** User's own private key */
	private static PrivateKey prKey;
	/** Session key for the active chat instance */
	private static SecretKey sessionKey;

	/** Sets {@link sessionKey} */
	public static void setSessionKey(SecretKey key) {
		sessionKey = key;
	}
	
	/** returns {@link sessionKey} */
	public static SecretKey getSessionKey() {
		return sessionKey;
	}

	/** returns {@link puKey} */
	public static Key getPublicKey() {
		return puKey;
	}
	
	/** returns {@link prKey} */
	public static Key getPrivateKey() {
		return prKey;
	}

	/** Initializes {@link cipherDES} and {@link cipherRSA} */
	public static void initializeCiphers() throws NoSuchAlgorithmException, NoSuchPaddingException {
		cipherDES = Cipher.getInstance("DES");
		cipherRSA = Cipher.getInstance("RSA");
	}
	
	/**
	 * Generates an RSA public key provided a byte array key
	 * @param key - encoded byte array of a key
	 * @return public key
	 */
	public static PublicKey createPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(new X509EncodedKeySpec(key));
	}

	/**
	 * Generates the RSA key pair for public key {@link puKey} and private key
	 * {@link prKey}
	 */
	public static void generateRSAKeys(int keySize) throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(keySize);
		KeyPair kp = kpg.genKeyPair();
		puKey = kp.getPublic();
		prKey = kp.getPrivate();
	}

	/** Generates session key {@link sessionKey} */
	public static void generateSessionKey() throws NoSuchAlgorithmException {
		KeyGenerator kg = null;
		kg = KeyGenerator.getInstance("DES");
		sessionKey = kg.generateKey();
	}

	/**
	 * Encrypts the input byte array using the DES algorithm. Returns the cipher
	 * text
	 */
	public static byte[] encryptDES(byte[] byteArr)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipherDES.init(Cipher.ENCRYPT_MODE, sessionKey);

		System.out.println("Byte Array to encrypt: ");
		System.out.println("String representation: " + new String(byteArr, StandardCharsets.UTF_8));
		printBytesAsHex(byteArr);

		output = cipherDES.doFinal(byteArr);
		System.out.println("Byte Array encrypted: ");
		System.out.println("String Representation: " + new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;

	}

	/**
	 * Decrypts the input byte array using the DES algorithm. Returns the plain text
	 */
	public static byte[] decryptDES(byte[] byteArr)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipherDES.init(Cipher.DECRYPT_MODE, sessionKey);

		System.out.println("Byte Array to decrypt: ");
		System.out.println("String representation: " + new String(byteArr, StandardCharsets.UTF_8));
		printBytesAsHex(byteArr);

		output = cipherDES.doFinal(byteArr);
		System.out.println("Byte Array decrypted: ");
		System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;
	}
	
	/**
	 * EncryptRSA method for encrypting with private key
	 * {@link EncryptionUtil#encryptRSA(byte[], Key, boolean)
	 * 
	 * @see EncryptionUtil#encryptRSA(byte[], Key, boolean)
	 */
	public static byte[] encryptRSAPrivate(byte[] byteArr)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return encryptRSA(byteArr, null, false);
	}

	/**
	 * overloaded encryptRSA method for encryption using the provided key
	 * {@link EncryptionUtil#encryptRSA(byte[], Key, boolean)
	 * 
	 * @see EncryptionUtil#encryptRSA(byte[], Key, boolean)
	 */
	public static byte[] encryptRSA(byte[] byteArr, PublicKey key)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return encryptRSA(byteArr, key, true);
	}

	/**
	 * encrypts the byteArr with the provided public key or the user's private key
	 * if pub = true
	 */
	public static byte[] encryptRSA(byte[] byteArr, PublicKey key, boolean pub)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		byte[] output = null;

		if (pub == true) {
			
			if (key == null) {
				System.out.println("Key must be defined");
				return null;
			}
			
			System.out.println("Encrypting with public key");
			cipherRSA.init(Cipher.ENCRYPT_MODE, key);
		} else {
			System.out.println("Encrypting with private key");
			cipherRSA.init(Cipher.ENCRYPT_MODE, prKey);
		}

		System.out.println("Byte Array to encrypt: ");
		System.out.println("String representation: " + new String(byteArr, StandardCharsets.UTF_8));
		printBytesAsHex(byteArr);

		output = cipherRSA.doFinal(byteArr);
		System.out.println("Byte Array encrypted");
		System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;
	}

	/**
	 * overloaded decryptRSA method making the key and boolean pub/priv key optional
	 * {@link EncryptionUtil#decryptRSA(byte[], Key, boolean)
	 * 
	 * @see EncryptionUtil#decryptRSA(byte[], Key, boolean)
	 */
	public static byte[] decryptRSA(byte[] byteArr)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return decryptRSA(byteArr, prKey, true);
	}

	/**
	 * overloaded decryptRSA method for decryption using provided key
	 * {@link EncryptionUtil#decryptRSA(byte[], Key, boolean)
	 * 
	 * @see EncryptionUtil#decryptRSA(byte[], Key, boolean)
	 */
	public static byte[] decryptRSA(byte[] byteArr, PublicKey key)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return decryptRSA(byteArr, key, false);
	}

	/**
	 * decrypts the byteArr with the provided key or the user's private key if priv
	 * = true
	 */
	public static byte[] decryptRSA(byte[] byteArr, Key key, boolean priv)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		byte[] output = null;

		if (priv == true) {
			cipherRSA.init(Cipher.DECRYPT_MODE, prKey);
		} else {
			cipherRSA.init(Cipher.DECRYPT_MODE, key);
		}

		System.out.println("Byte Array to decrypt: ");
		System.out.println("String representation: " + new String(byteArr, StandardCharsets.UTF_8));
		printBytesAsHex(byteArr);

		output = cipherRSA.doFinal(byteArr);
		System.out.println("Byte Array decrypted: ");
		System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;
	}
	
	public static byte[] hashMessage(byte[] message) throws NoSuchAlgorithmException {
		byte[] output = null;
		MessageDigest hash = MessageDigest.getInstance("SHA-256");
		output = hash.digest(message);
		return output;
	}
	
	public static byte[] signMessage(byte[] message, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		byte[] output = null;
		Signature prSignature = Signature.getInstance("SHA256withRSA");
		prSignature.initSign(privateKey);
		prSignature.update(message);
		output  = prSignature.sign();
		return output;
	}
	
	public static boolean verify(byte[] message, PublicKey publickey, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature pubSignature = Signature.getInstance("SHA256withRSA");
		pubSignature.initVerify(publickey);
		pubSignature.update(message);
		boolean verified = pubSignature.verify(signature);
		return verified;
	}

	/** Prints a byteArr in hexadecimal format */
	public static void printBytesAsHex(byte[] byteArr) {
		StringBuilder byteArray = new StringBuilder();
		System.out.print("Byte representation: ");
		for (byte b : byteArr) {
			byteArray.append(String.format("%02X ", b));
		}
		System.out.println(byteArray);
	}

}
