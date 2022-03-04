package lab3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Lab3SecureChatClient {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;

	// Key-related objects
	Cipher cipherDES = null;
	Cipher cipherRSA = null;

	PublicKey puKey;
	PublicKey serverKey;
	PrivateKey prKey;
	SecretKey sessionKey;

	// Socket and input/output stream declarations
	private Socket socket = null;
	private InputStream input = null;
	private OutputStream output = null;

	// Buffer array representing a packet
	private byte[] buf = new byte[PACKET_SIZE];

	private byte[] msg = new byte[PACKET_SIZE];

	public Lab3SecureChatClient() {
		try {
			generateKey();
			if (connect()) {
				// Send public key to server
				sendMessage(puKey.getEncoded());
				printBytesAsHex(puKey.getEncoded());

				// Receive server's public key
				buf = input.readNBytes(PACKET_SIZE);
				msg = unpad(buf);
				serverKey = createPublicKey(msg);
				System.out.println("Server public key retrieved:");
				printBytesAsHex(msg);
				
				
				

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void initializeCiphers() throws NoSuchAlgorithmException, NoSuchPaddingException {
		cipherDES = Cipher.getInstance("DES");
		cipherRSA = Cipher.getInstance("RSA");
	}

	void generateKey() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		KeyPair kp = kpg.genKeyPair();
		puKey = kp.getPublic();
		prKey = kp.getPrivate();
	}

	PublicKey createPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(keySpec);
	}

	void generateSessionKey() throws NoSuchAlgorithmException {
		// Key generation step
		KeyGenerator kg = null;
		kg = KeyGenerator.getInstance("DES");
		sessionKey = kg.generateKey();
	}

	boolean connect() throws UnknownHostException, IOException {
		// Step 1: Connect to server
		socket = new Socket(ADDRESS, PORT);
		input = socket.getInputStream();
		output = socket.getOutputStream();

		if (socket.isConnected()) {
			System.out.println("Successfully connected to server");
			return true;
		} else {
			System.out.println("Failed to connect");
			return false;
		}
	}

	void sendMessage(byte[] msg) throws IOException {
		if (msg.length <= PACKET_SIZE) {
			buf = Arrays.copyOf(msg, PACKET_SIZE);
			output.write(buf);
			System.out.println("Wrote to server: ");
			printBytesAsHex(buf);
		} else {
			for (int i = 0; i < msg.length; i++) {
				// send message then start overwriting buffer
				if (i % PACKET_SIZE == 0) {
					output.write(buf);
					System.out.println("Wrote to server: ");
					printBytesAsHex(buf);
				} else {
					buf[i % PACKET_SIZE] = msg[i];
				}
			}
		}
		output.flush();
	}

	// Encrypts the byteArr fed into it as input
	public byte[] encryptDES(byte[] byteArr)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipherDES.init(Cipher.ENCRYPT_MODE, sessionKey);

		System.out.println("Byte Array to encrypt: ");
		System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(byteArr);

		output = cipherDES.doFinal(byteArr);
		System.out.println("Byte Array encrypted: ");
		System.out.println("String Representation: " + new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;

	}

	// Decrypts the byteArr fed into it as input
	public byte[] decryptDES(byte[] byteArr)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipherDES.init(Cipher.DECRYPT_MODE, sessionKey);

		System.out.println("Byte Array to decrypt: ");
		System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
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
		return encryptRSA(byteArr, true);
	}

	// encrypts the byteArr fed into it using RSA algorithm
	public byte[] encryptRSA(byte[] byteArr, boolean pub)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipherDES.init(Cipher.ENCRYPT_MODE, puKey);

		System.out.println("Byte Array to encrypt: ");
		System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
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
		return decryptRSA(byteArr, true);
	}

	// Decrypts the byteArr fed into it using RSA algorithm
	public byte[] decryptRSA(byte[] byteArr, boolean priv)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipherRSA.init(Cipher.DECRYPT_MODE, prKey);

		System.out.println("Byte Array to decrypt: ");
		System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(byteArr);

		output = cipherRSA.doFinal(byteArr);
		System.out.println("Byte Array decrypted: ");
		System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;
	}

	/**
	 * Unpads input byte array removes the null bytes at the end
	 * 
	 * @return Input byte array without null padding bytes
	 */
	byte[] unpad(byte[] arr) {
		byte[] output = null;
		for (int i = arr.length - 1; i >= 0; i--) {
			if (arr[i] != 0) {
				output = Arrays.copyOf(arr, i + 1);
				break;
			}
		}
		return output;
	}

	public static void main(String[] args) {
		Lab3SecureChatClient client = new Lab3SecureChatClient();
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
