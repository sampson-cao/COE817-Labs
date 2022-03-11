package lab3;

import static lab3.EncryptionUtil.decryptDES;
import static lab3.EncryptionUtil.decryptRSA;
import static lab3.EncryptionUtil.encryptDES;
import static lab3.EncryptionUtil.encryptRSA;
import static lab3.EncryptionUtil.encryptRSAPrivate;
import static lab3.EncryptionUtil.generateRSAKeys;
import static lab3.EncryptionUtil.getPrivateKey;
import static lab3.EncryptionUtil.getPublicKey;
import static lab3.EncryptionUtil.initializeCiphers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

public class SecureChatClient implements Entity {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;
	final static private String CLIENT_ID = "client";

	// Verification variables
	/** Counter for nonce */
	private int ctr = 0;
	/** Time stamp for nonce */
	private ZonedDateTime timeStamp;

	PublicKey serverKey;
	SecretKey sessionKey;

	private Connection connection = null;

	private ReadConsoleThread readThread;
	private WriteConsoleThread writeThread;

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	public SecureChatClient() {
		try {
			initializeCiphers();
			generateRSAKeys(PACKET_SIZE);
			connection = new Connection(ADDRESS, PORT);
			if (connection.clientConnect()) {
				// Start of Asymmetric key distribution
				// Send public key to server
				connection.sendMessage(getPublicKey().getEncoded());
				printBytesAsHex(getPrivateKey().getEncoded());

				// Receive server's public key
				byteMsg = connection.readMessage();
				serverKey = createPublicKey(byteMsg);

				// Start of Symmetric key distribution
				// Step 1: Send client ID and nonce to server
				System.out.println("\nStep 1:");
				strMsg = generateNonce();
				System.out.println("Message: " + strMsg);
				byteMsg = strMsg.getBytes();
				byteMsg = encryptRSA(byteMsg, serverKey);
				connection.sendMessage(byteMsg);

				// Step 2: Receive nonce2 from server
				System.out.println("\nStep 2:");
				byteMsg = connection.readMessage();
				byteMsg = decryptRSA(byteMsg);
				strMsg = new String(byteMsg, StandardCharsets.UTF_8);
				parseNonce(strMsg.split(",")[1]);

				// Step 3: Send nonce3 back to server
				System.out.println("\nStep 3:");
				strMsg = generateNonce();
				System.out.println("Message: " + strMsg);
				byteMsg = strMsg.getBytes();
				byteMsg = encryptRSA(byteMsg, serverKey);
				connection.sendMessage(byteMsg);

				// Step 4: Generate and send session key
				System.out.println("\nStep 4:");
				EncryptionUtil.generateSessionKey();
				sessionKey = EncryptionUtil.getSessionKey();
				byteMsg = encryptRSAPrivate(sessionKey.getEncoded());
				byteMsg = encryptRSA(byteMsg, serverKey);
				connection.sendMessage(byteMsg);
				System.out.println("Session key sent... End of verification step\n");

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	PublicKey createPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(new X509EncodedKeySpec(key));
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
		nonce = CLIENT_ID + ";" + ctr + ";" + timeStamp.toString();
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

			// on initialization, any counter received is valid
			if (this.timeStamp == null) {
				ctr = counter - 1;
			}

			// otherwise, check if the counter received is 1 higher than the current one
			if (this.ctr + 1 != counter) {
				System.out.println("Counter is invalid: " + counter + " " + ctr);
				return false;
			}

			// non-functional time check (can remove)
			/*
			 * recvTime = ZonedDateTime.parse(timeStamp); Duration duration =
			 * Duration.between(recvTime, ZonedDateTime.now()); if (duration.getNano() < 0
			 * || duration.getNano() >= 3000000000L) {
			 * System.out.println("Invalid timestamp: " + recvTime + " " + this.timeStamp);
			 * System.out.println(duration.getNano()); return false; }
			 */

			ctr = counter;
			// this.timeStamp = recvTime;
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
		SecureChatClient client = new SecureChatClient();
		client.startChat();
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
