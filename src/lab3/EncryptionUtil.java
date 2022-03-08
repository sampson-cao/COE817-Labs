package lab3;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

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

	/** Cipher for DES algorithm */
	Cipher cipherDES = null;
	/** Cipher for RSA algorithm */
	Cipher cipherRSA = null;

	/** User's own public key */
	PublicKey puKey;
	/** User's own private key */
	PrivateKey prKey;
	/** Session key for the active chat instance */
	SecretKey sessionKey;

	/** Sets {@link sessionKey} */
	void setSessionKey(SecretKey key) {
		sessionKey = key;
	}

	/** Initializes {@link cipherDES} and {@link cipherRSA} */
	void initializeCiphers() throws NoSuchAlgorithmException, NoSuchPaddingException {
		cipherDES = Cipher.getInstance("DES");
		cipherRSA = Cipher.getInstance("RSA");
	}

	/**
	 * Generates the RSA key pair for public key {@link puKey} and private key
	 * {@link prKey}
	 */
	void generateKey() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(1024);
		KeyPair kp = kpg.genKeyPair();
		puKey = kp.getPublic();
		prKey = kp.getPrivate();
	}

	/** Generates session key {@link sessionKey} */
	void generateSessionKey() throws NoSuchAlgorithmException {
		KeyGenerator kg = null;
		kg = KeyGenerator.getInstance("DES");
		sessionKey = kg.generateKey();
	}

	/** Encrypts the input byte array using the DES algorithm. Returns the cipher text*/
	public byte[] encryptDES(byte[] byteArr)
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

	/** Decrypts the input byte array using the DES algorithm. Returns the plain text */
	public byte[] decryptDES(byte[] byteArr)
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

	// method overloading to make the boolean an optional parameter
	public byte[] encryptRSA(byte[] byteArr)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return encryptRSA(byteArr, pubKey, true);
	}

	public byte[] encryptRSA(byte[] byteArr, boolean pub)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return encryptRSA(byteArr, null, pub);
	}

	// encrypts the byteArr fed into it using RSA algorithm
	public byte[] encryptRSA(byte[] byteArr, PublicKey key, boolean pub)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		byte[] output = null;

		if (pub == true) {
			cipherRSA.init(Cipher.ENCRYPT_MODE, key);
		} else {
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

	// method overloading to make the boolean an optional parameter
	public byte[] decryptRSA(byte[] byteArr)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return decryptRSA(byteArr, serverKey, true);
	}

	public byte[] decryptRSA(byte[] byteArr, PublicKey key)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return decryptRSA(byteArr, key, true);
	}

	// Decrypts the byteArr fed into it using RSA algorithm
	public byte[] decryptRSA(byte[] byteArr, PublicKey key, boolean priv)
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

	// Helper function for printing byte array into hex values
	public static void printBytesAsHex(byte[] byteArr) {
		StringBuilder byteArray = new StringBuilder();
		System.out.print("Byte representation: ");
		for (byte b : byteArr) {
			byteArray.append(String.format("%02X ", b));
		}
		System.out.println(byteArray);
	}

}
