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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Lab3SecureChatClient {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;

	// Key-related objects
	Cipher cipher = null;
	
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

	public Lab3SecureChatClient() {
		try {
			generateKey();
			if (connect()) {
				// Send public key to server
				sendMessage(puKey.getEncoded(), false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void generateKey() throws NoSuchAlgorithmException {
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		KeyPair kp = kpg.genKeyPair();
		puKey = kp.getPublic();
		prKey = kp.getPrivate();
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
	
	
	void sendMessage(byte[] msg, boolean encrypt, String algo) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

		if (encrypt) {
			if (algo.equalsIgnoreCase("DES")) {
				msg = encryptDES(msg);
			}
		}

		buf = Arrays.copyOf(msg, PACKET_SIZE);

	}

	// Encrypts the byteArr fed into it as input
	public byte[] encryptDES(byte[] byteArr) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
		output = cipher.doFinal(byteArr);
		System.out.println("Byte Array encrypted: ");
		System.out.println(new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;

	}

	// Decrypts the byteArr fed into it as input
	public byte[] decryptDES(byte[] byteArr) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipher.init(Cipher.DECRYPT_MODE, sessionKey);
		output = cipher.doFinal(byteArr);
		System.out.println("Byte Array decrypted: ");
		System.out.println(new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;
	}

	public static void main(String[] args) {
		Lab3SecureChatClient client = new Lab3SecureChatClient();
	}

	// Helper function for printing byte array into hex values
	public static void printBytesAsHex(byte[] byteArr) {
		StringBuilder byteArray = new StringBuilder();
		for (byte b : byteArr) {
			byteArray.append(String.format("%02X", b));
		}
		System.out.println(byteArray);
	}

}
