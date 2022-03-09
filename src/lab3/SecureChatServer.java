package lab3;

import static lab3.EncryptionUtil.*;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;

public class SecureChatServer {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;
	final static private String SERVER_ID = "server";

	PublicKey clientKey;
	SecretKey sessionKey;

	// Socket and input/output stream declarations
	private Socket socket = null;
	private ServerSocket server = null;
	private InputStream input = null;
	private OutputStream output = null;

	private byte[] header = new byte[4];
	// Buffer array representing a packet
	private byte[] buf = new byte[PACKET_SIZE];

	private byte[] byteMsg = null;

	private String strMsg = "";

	// counter (to prevent replay attacks) and client ID
	private String clientID = "";
	private int ctr = 0;
	private ZonedDateTime timeStamp = ZonedDateTime.now(ZoneId.of("UTC"));

	public SecureChatServer() {
		try {
			initializeCiphers();
			generateRSAKeys();
			createServer();
			if (connect()) {
				// Start of Asymmetric key distribution
				// Retrieve public key from client and send server's public key
				readMessage();
				clientKey = createPublicKey(byteMsg);
				System.out.println("Received client's public key");

				// Send server's public key to client
				System.out.println("Sending server's public key to client");
				buf = getPublicKey().getEncoded();
				sendMessage(buf);

				// Start of Symmetric key distribution
				// Step 1: Receive client ID, nonce, and timestamp from client
				System.out.println("\nStep 1:");
				readMessage();
				byteMsg = decryptRSA(byteMsg);
				strMsg = new String(byteMsg, StandardCharsets.UTF_8);
				parseNonce(strMsg);

				// Step 2: send N1|N2 to client
				System.out.println("\nStep 2:");
				strMsg = strMsg + "," + generateNonce();
				byteMsg = strMsg.getBytes();
				byteMsg = encryptRSA(byteMsg, clientKey);
				sendMessage(byteMsg);
				printBytesAsHex(byteMsg);

				// Step 3: Receive N3 from server
				System.out.println("\nStep 3:");
				readMessage();
				byteMsg = decryptRSA(byteMsg);
				if (parseNonce(new String(byteMsg, StandardCharsets.UTF_8))) {
					System.out.println("Successfully verified client");
				} else {
					System.out.println("Failed to verify client. Closing connection.");
					return;
				}

				// Step 4: Receive session key from client
				System.out.println("\nStep 4:");
				readMessage();
				byteMsg = decryptRSA(byteMsg);
				byteMsg = decryptRSA(byteMsg, clientKey);
				getSessionKey();
				
				// Main body of loop
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				
				while (true) {
					
					break;
				}

			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
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

	void readMessage() throws IOException {
		int msgLen = 0;
		// Receive length of message
		header = input.readNBytes(4);
		msgLen = ((header[0] & 0xff) << 0 | (header[1] & 0xff) << 8 | (header[2] & 0xff) << 16
				| (header[3] & 0xff) << 24);
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
	
	public static void main (String[] args) {
		SecureChatServer server = new SecureChatServer();
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
