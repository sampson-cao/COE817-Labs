package lab3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
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
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;

public class Lab3SecureChatServer {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;
	final static private String SERVER_ID = "server";

	// Key-related objects
	Cipher cipherDES = null;
	Cipher cipherRSA = null;

	PublicKey puKey;
	PublicKey clientKey;
	PrivateKey prKey;
	SecretKey sessionKey;

	// Socket and input/output stream declarations
	private Socket socket = null;
	private ServerSocket server = null;
	private InputStream input = null;
	private OutputStream output = null;

	// Buffer array representing a packet
	private byte[] buf = new byte[PACKET_SIZE];

	private byte[] byteMsg = null;

	private String strMsg = "";

	// counter (to prevent replay attacks) and client ID
	private String clientID = "";
	private int ctr = 0;
	private ZonedDateTime timeStamp = ZonedDateTime.now(ZoneId.of("UTC"));

	public Lab3SecureChatServer() {
		try {
			initializeCiphers();
			generateKey();
			createServer();
			if (connect()) {
				// Start of Asymmetric key distribution
				// Retrieve public key from client and send server's public key
				buf = input.readNBytes(PACKET_SIZE);
				System.out.println("Received from client: ");
				printBytesAsHex(buf);
				byteMsg = unpad(buf);
				clientKey = createPublicKey(byteMsg);
				System.out.println("Received client's public key: ");
				printBytesAsHex(byteMsg);

				// Send server's public key to client
				System.out.println("Sending server's public key to client");
				buf = puKey.getEncoded();
				sendMessage(buf);

				// Start of Symmetric key distribution
				// Step 1: Receive client ID, nonce, and timestamp from client
				buf = input.readNBytes(PACKET_SIZE);
				System.out.println("Received from client: ");
				printBytesAsHex(buf);
				byteMsg = unpad(buf);
				byteMsg = decryptRSA(byteMsg);
				strMsg = new String(byteMsg, StandardCharsets.UTF_8);
				parseNonce(strMsg);

				// Step 2: send N1|N2 to client
				strMsg = strMsg + "," + generateNonce();
				byteMsg = strMsg.getBytes();
				byteMsg = encryptRSA(byteMsg);
				sendMessage(byteMsg);
				printBytesAsHex(byteMsg);

				// Step 3: Receive N3 from server
				buf = input.readNBytes(PACKET_SIZE);
				System.out.println("Received from client: ");
				printBytesAsHex(buf);
				byteMsg = unpad(buf);
				byteMsg = decryptRSA(byteMsg);
				if (parseNonce(new String(byteMsg, StandardCharsets.UTF_8))) {
					System.out.println("Successfully verified client");
				} else {
					System.out.println("Failed to verify client. Closing connection.");
					return;
				}

			}
		} catch (

		Exception e) {
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

	void createServer() throws UnknownHostException, IOException {

		// Step 1: Create Server
		server = new ServerSocket();
		server.bind(new InetSocketAddress(ADDRESS, PORT));
		System.out.println("Server created: " + server.getInetAddress());
		System.out.println("Waiting for client connection");

	}

	boolean connect() throws IOException {
		socket = server.accept();
		input = socket.getInputStream();
		output = socket.getOutputStream();

		if (socket.isConnected()) {
			System.out.println("Connected to client");
			return true;
		} else {
			System.out.println("Failed to connect to client");
			return false;
		}
	}

	PublicKey createPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(keySpec);
	}

	SecretKey getSessionKey() {
		return null;
	}

	void sendMessage(byte[] msg) throws IOException {
		if (msg.length <= PACKET_SIZE) {
			buf = Arrays.copyOf(msg, PACKET_SIZE);
			output.write(buf);
			output.flush();
			System.out.println("Wrote to client: ");
			printBytesAsHex(buf);
		} else {
			for (int i = 0; i < msg.length; i++) {
				// send message then start overwriting buffer
				if (i % PACKET_SIZE == 0) {
					output.write(buf);
					output.flush();
					System.out.println("Wrote to client: ");
					printBytesAsHex(buf);
				} else {
					buf[i % PACKET_SIZE] = msg[i];
				}
			}
		}
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
		return encryptRSA(byteArr, clientKey, true);
	}

	public byte[] encryptRSA(byte[] byteArr, PublicKey key)
			throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		return encryptRSA(byteArr, key, true);
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
		return decryptRSA(byteArr, clientKey, true);
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
		System.out.println(timeStamp);
		if (timeStamp == null) {
			ctr = (int) (Math.random() * 846974);
		} else {
			// increment nonce
			ctr++;
		}
		timeStamp = ZonedDateTime.now(ZoneId.of("UTC"));
		nonce = SERVER_ID + ";" + ctr + ";" + timeStamp.toString();
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

	public static void main(String[] args) {
		Lab3SecureChatServer server = new Lab3SecureChatServer();
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
