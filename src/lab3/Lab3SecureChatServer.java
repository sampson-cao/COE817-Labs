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

import javax.crypto.SecretKey;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;

public class Lab3SecureChatServer {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;

	PublicKey puKey;
	PublicKey clientKey;
	PrivateKey prKey;

	// Socket and input/output stream declarations
	private Socket socket = null;
	private ServerSocket server = null;
	private InputStream input = null;
	private OutputStream output = null;

	// Buffer array representing a packet
	private byte[] buf = new byte[PACKET_SIZE];

	private byte[] msg = null;

	public Lab3SecureChatServer() {
		try {
			generateKey();
			createServer();
			if (connect()) {

				// Retrieve public key from client and send server's public key
				buf = input.readNBytes(PACKET_SIZE);
				System.out.println("Received from client: ");
				printBytesAsHex(buf);
				msg = unpad(buf);
				clientKey = createPublicKey(msg);
				System.out.println("Received client's public key: ");
				printBytesAsHex(msg);
				
				// Send server's public key to client
				System.out.println("Sending server's public key to client");
				buf = puKey.getEncoded();
				sendMessage(buf);
				
				

			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
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
