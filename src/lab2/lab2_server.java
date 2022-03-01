package lab2;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/* This class represents a server which authenticates the user
 * Upon receiving an DES encrypted message from a client, it will decrypt the message
 * using the master key and then send a response to the client containing
 * the client's ID, the server's ID, and the server's key.
 * The server then waits for the server's ID encrypted with the server's key
 * to be sent back.
 */
public class lab2_server {
	
	// packet size for sending/receiving
	final private int PACKET_SIZE = 1024;
	
	// Input data
	// Server ID
	private String idb = "RESPONDER B";
	// Master key
	private String km = "NETWORK SECURITY";
	// Key to send
	private String ks = "RYERSON";
	// String representation of message
	String msg = "";

	// Declare socket and input/output streams
	private Socket socket = null;
	private ServerSocket server = null;
	private InputStream input = null;
	private OutputStream output = null;
	
	// Buffer array used to represent a packet
	private byte[] buf = new byte[PACKET_SIZE];
	
	// Key generation and cipher objects 
	SecretKeyFactory skf = null;
	SecretKey key = null;
	Cipher cipher = null;

	public lab2_server(int port) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {

		// Key setup for encryption
		skf = SecretKeyFactory.getInstance("DES");
		key = skf.generateSecret(new DESKeySpec(km.getBytes()));
		cipher = Cipher.getInstance("DES");
		System.out.println("Key Generated");
		// Autokey ks if key is not long enough
		while (ks.length() < 8) {
			int i = 0;
			ks += ks.charAt(i);
			i++;
		}

		// Step 0: start server and wait for incoming connection
		server = new ServerSocket(port);
		System.out.println("Server started");
		System.out.println("Server information: " + server.getInetAddress());
		System.out.println("Waiting for client to connect...");
		socket = server.accept();
		System.out.println("Client accepted");
		input = socket.getInputStream();
		output = socket.getOutputStream();

		// Step 1: receive ID from client
		buf = input.readNBytes(PACKET_SIZE);
		msg = new String(buf, StandardCharsets.UTF_8);
		System.out.println("Received Input: " + msg);
		System.out.println("Size: " + msg.length());
		msg = msg.trim();
		
		// Step 2: Send response message containing client ID, server ID, and a different key
		msg += ";" + idb + ";" + ks;
		System.out.println("Message to send: " + msg);
		buf = Arrays.copyOf(encrypt(msg.getBytes()), PACKET_SIZE);
		output.write(buf);
		System.out.println("Message sent");
		
		// Step 3: Receive message from client
		buf = input.readNBytes(PACKET_SIZE);
		System.out.println("Received Input: " + new String(buf, StandardCharsets.UTF_8));
		System.out.println("Size: " + buf.length);
		printBytesAsHex(buf);
		// remove padding bytes
		for (int i = 0; i < buf.length; i++) {
			if (buf[i] == 0) {
				buf = Arrays.copyOf(buf, i);
			}
		}
		
		// configure key and decrypt message
		key = skf.generateSecret(new DESKeySpec(ks.getBytes()));
		buf = decrypt(buf);
		msg = new String(buf, StandardCharsets.UTF_8);

		// close resources and connection
		System.out.println("Closing connection");
		socket.close();
		input.close();
		output.close();
	}

	// Encrypts the byteArr fed into it as input
	public byte[] encrypt(byte[] byteArr) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipher.init(Cipher.ENCRYPT_MODE, key);
		output = cipher.doFinal(byteArr);
		System.out.println("Byte Array encrypted: ");
		System.out.println(new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;

	}

	// Decrypts the byteArr fed into it as input
	public byte[] decrypt(byte[] byteArr) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] output = null;
		cipher.init(Cipher.DECRYPT_MODE, key);
		output = cipher.doFinal(byteArr);
		System.out.println("Byte Array decrypted: ");
		System.out.println(new String(output, StandardCharsets.UTF_8));
		printBytesAsHex(output);
		return output;
	}

	// Helper function for printing byte array into hex values
	public static void printBytesAsHex(byte[] byteArr) {
		StringBuilder byteArray = new StringBuilder();
		for (byte b : byteArr) {
			byteArray.append(String.format("%02X ", b));
		}
		System.out.println(byteArray);
	}

	public static void main(String args[]) {
		try {
			lab2_server s = new lab2_server(8000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
