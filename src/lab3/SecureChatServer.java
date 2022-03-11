package lab3;

import static lab3.EncryptionUtil.decryptDES;
import static lab3.EncryptionUtil.decryptRSA;
import static lab3.EncryptionUtil.encryptDES;
import static lab3.EncryptionUtil.encryptRSA;
import static lab3.EncryptionUtil.generateRSAKeys;
import static lab3.EncryptionUtil.getPublicKey;
import static lab3.EncryptionUtil.initializeCiphers;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class SecureChatServer implements Entity {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;
	final static private String SERVER_ID = "server";

	// Verification variables
	/** Counter for nonce */
	private int ctr = 0;
	/** Time stamp for nonce */
	private ZonedDateTime timeStamp;

	PublicKey clientKey;
	SecretKey sessionKey;

	// Socket and input/output stream declarations
	private Socket socket = null;
	private ServerSocket server = null;

	private Connection connection = null;

	private ReadConsoleThread readThread;
	private WriteConsoleThread writeThread;

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	public SecureChatServer() {
		try {
			initializeCiphers();
			/*
			 * server key size is double client's key size to enable double encryption
			 * during session key transmission
			 */
			generateRSAKeys(PACKET_SIZE * 2);
			connection = new Connection(ADDRESS, PORT);
			createServer();
			if (this.connect()) {
				// Start of Asymmetric key distribution
				// Retrieve public key from client and send server's public key
				byteMsg = connection.readMessage();
				clientKey = createPublicKey(byteMsg);
				System.out.println("Received client's public key");

				// Send server's public key to client
				connection.sendMessage(getPublicKey().getEncoded());

				// Start of Symmetric key distribution
				// Step 1: Receive client ID, nonce, and timestamp from client
				System.out.println("\nStep 1:");
				byteMsg = connection.readMessage();
				byteMsg = decryptRSA(byteMsg);
				strMsg = new String(byteMsg, StandardCharsets.UTF_8);
				parseNonce(strMsg);

				// Step 2: send N1|N2 to client
				System.out.println("\nStep 2:");
				strMsg = strMsg + "," + generateNonce();
				byteMsg = strMsg.getBytes();
				byteMsg = encryptRSA(byteMsg, clientKey);
				connection.sendMessage(byteMsg);

				// Step 3: Receive N3 from server
				System.out.println("\nStep 3:");
				byteMsg = connection.readMessage();
				byteMsg = decryptRSA(byteMsg);
				if (parseNonce(new String(byteMsg, StandardCharsets.UTF_8))) {
					System.out.println("Successfully verified client");
				} else {
					System.out.println("Failed to verify client. Closing connection.");
					socket.close();
					return;
				}

				// Step 4: Receive session key from client
				System.out.println("\nStep 4:");
				byteMsg = connection.readMessage();
				byteMsg = decryptRSA(byteMsg);
				byteMsg = decryptRSA(byteMsg, clientKey);
				sessionKey = createSessionKey(byteMsg);
				EncryptionUtil.setSessionKey(sessionKey);

				System.out.println("Client successfully verified... End of verification\n");

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
		return connection.serverConnect(socket);
	}

	PublicKey createPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(new X509EncodedKeySpec(key));
	}

	SecretKey createSessionKey(byte[] key)
			throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		return skf.generateSecret(new DESKeySpec(key));
	}

	/**
	 * Generates a nonce containing a counter and timestamp
	 * 
	 * @return String representation of nonce
	 */
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
		nonce = SERVER_ID + ";" + ctr + ";" + timeStamp.toString();
		System.out.println("Generated nonce: " + nonce);
		return nonce;
	}

	/**
	 * Parses provided nonce String and updates this object's counter and timestamp
	 * 
	 * @param nonce
	 * @return true if successfully parsed
	 */
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

			System.out.println("Nonce tokens: " + id + " " + counter + " " + timeStamp);
			System.out.println("Current counter: " + ctr);
			System.out.println("Received counter: " + counter);
			
			if (this.timeStamp == null) {
				ctr = counter - 1;
			}

			if (this.ctr + 1 != counter) {
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

	public void sendText(String strMsg)
			throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		strMsg += "," + generateNonce();
		byte[] msg = strMsg.getBytes();
		msg = encryptDES(msg);
		connection.sendMessage(msg);
	}

	public void sendImage(String fileName) throws Exception {
		File file = new File(fileName);
		byte[] imgBytes = Files.readAllBytes(file.toPath());
		imgBytes = encryptDES(imgBytes);
		connection.sendMessage(imgBytes);
	}

	public String readMessage()
			throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] byteMsg = connection.readMessage();
		String msg = new String(decryptDES(byteMsg), StandardCharsets.UTF_8);
		int index = msg.lastIndexOf(",");
		if (index < 0) {
			System.out.println("Failed to find nonce");
			return "";
		}

		if (!parseNonce(msg.substring(index))) {
			System.out.println("Invalid nonce received, ignoring message");
			return "";
		}

		return msg.substring(0, index);
	}

	public void startChat() {
		readThread = new ReadConsoleThread(this);
		writeThread = new WriteConsoleThread(this);
		readThread.start();
		writeThread.start();
	}

	public static void main(String[] args) {
		SecureChatServer server = new SecureChatServer();
		server.startChat();
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
