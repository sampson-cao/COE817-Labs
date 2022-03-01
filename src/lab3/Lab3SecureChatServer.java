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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

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

	public Lab3SecureChatServer() {
		try {
			keyGeneration();
			createServer();
			if (connect()) {
				// do stuff
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void keyGeneration() throws NoSuchAlgorithmException {
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
	
	PublicKey getClientKey() {
		
	}
	
	void sendMessage(String msg, boolean encrypt) {
		
	}

	public static void main(String[] args) {
		Lab3SecureChatServer server = new Lab3SecureChatServer();
	}

}
