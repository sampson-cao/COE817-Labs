package lab3;

import static lab3.EncryptionUtil.*;

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


	PublicKey serverKey;
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
			generateRSAKeys(PACKET_SIZE);
			if (connect()) {
				// Start of Asymmetric key distribution
				// Send public key to server
				sendMessage(getPublicKey().getEncoded());
				printBytesAsHex(getPrivateKey().getEncoded());

				// Receive server's public key
				readMessage();
				serverKey = createPublicKey(byteMsg);

				// Start of Symmetric key distribution
				// Step 1: Send client ID and nonce to server
				System.out.println("\nStep 1:");
				strMsg = generateNonce();
				System.out.println("Message: " + strMsg);
				byteMsg = strMsg.getBytes();
				byteMsg = encryptRSA(byteMsg, serverKey);
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
				byteMsg = encryptRSA(byteMsg, serverKey);
				sendMessage(byteMsg);
				printBytesAsHex(byteMsg);

				// Step 4: Generate and send session key
				System.out.println("\nStep 4:");
				generateSessionKey();
				sessionKey = EncryptionUtil.getSessionKey();
				byteMsg = encryptRSAPrivate(sessionKey.getEncoded());
				byteMsg = encryptRSA(byteMsg, serverKey);
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
	
	PublicKey createPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(keySpec);
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
