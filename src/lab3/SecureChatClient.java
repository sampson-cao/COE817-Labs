package lab3;

import lab3.EncryptionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SecureChatClient{

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;
	final static private String CLIENT_ID = "client";



	PublicKey puKey;
	PublicKey serverKey;
	PrivateKey prKey;
	SecretKey sessionKey;

	// Socket and input/output stream declarations
	private Socket socket = null;
	private InputStream input = null;
	private OutputStream output = null;
	
	private byte[] header = new byte[4];
	// Buffer array representing a packet
	private byte[] buf = new byte[PACKET_SIZE];
	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	// Verification variables
	private int ctr = 0;
	private ZonedDateTime timeStamp;

	public SecureChatClient() {
		try {
			initializeCiphers();
			generateSessionKey();
			generateKey();
			if (connect()) {
				// Start of Asymmetric key distribution
				// Send public key to server
				sendMessage(puKey.getEncoded());
				printBytesAsHex(puKey.getEncoded());

				// Receive server's public key
				readMessage();
				serverKey = createPublicKey(byteMsg);

				// Start of Symmetric key distribution
				// Step 1: Send client ID and nonce to server
				System.out.println("\nStep 1:");
				strMsg = generateNonce();
				System.out.println("Message: " + strMsg);
				byteMsg = strMsg.getBytes();
				byteMsg = encryptRSA(byteMsg);
				sendMessage(byteMsg);
				printBytesAsHex(byteMsg);

				// Step 2: Receive nonce2 from server
				System.out.println("\nStep 2:");
				readMessage();
				byteMsg = decryptRSA(byteMsg);
				strMsg = new String(byteMsg, StandardCharsets.UTF_8);
				parseNonce(strMsg.split(",")[1]);

				// Step 3: Send nonce3 back to server
				System.out.println("\nStep 3:");
				strMsg = generateNonce();
				System.out.println("Message: " + strMsg);
				byteMsg = strMsg.getBytes();
				byteMsg = encryptRSA(byteMsg);
				sendMessage(byteMsg);
				printBytesAsHex(byteMsg);

				// Step 4: Send session key
				System.out.println("\nStep 4:");
				byteMsg = encryptRSA(sessionKey.getEncoded(), false);
				byteMsg = encryptRSA(byteMsg);
				sendMessage(byteMsg);
				System.out.println("Session key sent");
				
				// Setup finished, actual chat program begins here
				
				// Main body of loop
				while (true) {
					break;
				}

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
		kpg.initialize(1024);
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

	void readMessage() throws IOException {
		int msgLen = 0;
		// Receive length of message
		header = input.readNBytes(4);
		msgLen = ((header[0] & 0xff) << 0 | (header[1] & 0xff) << 8 | (header[2] & 0xff) << 16 | (header[3] & 0xff) << 24);
		System.out.println("Message length: " + msgLen);

		// Receive message
		System.out.println("Message Received: ");
		buf = input.readNBytes(PACKET_SIZE);
		printBytesAsHex(buf);
		byteMsg = unpad(buf);
	}

	void sendMessage(byte[] msg) throws IOException {
		int msgLen = msg.length;
		// convert integer value message length into byte array and write
		header[0] = (byte) (msgLen);
		header[1] = (byte) (msgLen >> 8);
		header[2] = (byte) (msgLen >> 16);
		header[3] = (byte) (msgLen >> 24);
		output.write(header);
		output.flush();
		System.out.println("Message to send length: " + msgLen);

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
		System.out.println("String representation: " + new String(byteArr, StandardCharsets.UTF_8));
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
		return encryptRSA(byteArr, serverKey, true);
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

	String generateNonce() {
		String nonce = "";
		// Initialization case
		if (timeStamp == null) {
			ctr = (int) (Math.random() * 846974);
		} else {
			// increment nonce
			ctr++;
		}
		timeStamp = ZonedDateTime.now(ZoneId.of("UTC"));
		nonce = CLIENT_ID + ";" + ctr + ";" + timeStamp.toString();
		System.out.println("Generated nonce: " + nonce);
		return nonce;
	}

	boolean parseNonce(String nonce) {
		String id = "";
		int counter;
		String timeStamp = "";

		try {
			String tokens[] = nonce.split(";");
			ZonedDateTime recvTime;
			id = tokens[0];
			counter = Integer.parseInt(tokens[1]);
			timeStamp = tokens[2];

			System.out.println("Nonce tokens: " + id + ", " + counter + ", " + timeStamp);

			if (counter == this.ctr + 1) {
				System.out.println("Counter is invalid: " + counter + " " + ctr);
				return false;
			}

			recvTime = ZonedDateTime.parse(timeStamp);
			Duration duration = Duration.between(recvTime, ZonedDateTime.now());
			if (duration.getNano() < 0 || duration.getNano() >= 3000000000L) {
				System.out.println("Invalid timestamp: " + recvTime + " " + this.timeStamp);
				System.out.println(duration.getNano());
				return false;
			}

			ctr = counter;
			this.timeStamp = recvTime;
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Something went wrong, " + nonce);
			return false;
		}

	}
	
	public static void main (String[] args) {
		SecureChatClient client = new SecureChatClient();
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
