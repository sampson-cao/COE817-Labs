package lab2;

import java.net.*;
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

import java.io.*;

/* This class represents a client attempting to connect to the server
 * by sending its ID encrypted with a master key using DES encryption
 * Upon receiving a response from the server, it will decrypt the message
 * using the master key and then send a response to the server containing
 * the server's ID, encrypted with the server key as a form of authentication
 */
public class lab2_client {
	
	// packet size for sending/receiving
	final private int PACKET_SIZE = 1024;
	
	// Input data
	// client ID
	private String ida = "INITIATOR A";
	// master key
	private String km = "NETWORK SECURITY";
	// sent key
	private String ks = null;
	
	// Socket and input/output stream declarations
	private Socket socket = null;
	private InputStream input = null;
	private OutputStream output = null;
	
	// Key generation and cipher objects
	Cipher cipher = null;
	SecretKeyFactory skf = null;
	SecretKey key = null;
	
	// Buffer array representing a packet
	private byte[] buf = new byte[PACKET_SIZE];
	
	// Constructor for the client
	public lab2_client(String address, int port) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
		
		// Key and cipher intialization
		skf = SecretKeyFactory.getInstance("DES");
		key = skf.generateSecret(new DESKeySpec(km.getBytes()));
		cipher = Cipher.getInstance("DES");
		
		// Step 0: Connect to server
		socket = new Socket(address, port);
		System.out.println("Successfully connected to server");
		input = socket.getInputStream();
		output = socket.getOutputStream();
		
		// Step 1: send client's ID to server
		System.out.println("Message to send: " + ida);
		buf = Arrays.copyOf(ida.getBytes(), PACKET_SIZE);
		output.write(buf);
		System.out.println("Message sent");
		
		// Step 2: Receive response message from server
		buf = input.readNBytes(PACKET_SIZE);
		
		// removes all trailing bytes from padding in the packet
		for (int i = 0; i < buf.length; i++) {
			if (buf[i] == 0) {
				buf = Arrays.copyOf(buf, i);
				break;
			}
		}
		
		// Display received message
		String msg = new String(buf, StandardCharsets.UTF_8);
		System.out.println("Response received: " + msg);
		System.out.println("Size: " + buf.length);
		printBytesAsHex(buf);
		
		// decrypt message
		buf = decrypt(buf);
		msg = new String(buf, StandardCharsets.UTF_8);
		
		// process message and retrieve tokens
		msg = msg.trim();
		String[] tokens = msg.split(";");
		String idb = tokens[1];
		String ks = tokens[2];
		System.out.println("Server ID = " + idb + ", Key Received = " + ks);
		
		// encrypt outgoing packet with received key
		key = skf.generateSecret(new DESKeySpec(ks.getBytes()));	
		buf = Arrays.copyOf(encrypt(idb.getBytes()), PACKET_SIZE);
		
		// Step 3: Send message to server
		output.write(buf);
		System.out.println("Message Sent");
		
		// close resources and connection
		System.out.println("Closing connection");
		input.close();
		output.close();
		socket.close();

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
			lab2_client c = new lab2_client("0.0.0.0", 8000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
