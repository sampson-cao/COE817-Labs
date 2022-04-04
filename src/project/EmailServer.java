package project;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class EmailServer {

	final static private String ADDRESS = "localhost";
	final static private int PORT = 25;
	final static private int PACKET_SIZE = 1024;
	final static private String SERVER_ID = "server";

	// Socket and input/output stream declarations
	private Socket socket = null;
	private ServerSocket server = null;

	private Connection connection = new Connection(ADDRESS, PORT);

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];

	public EmailServer() throws IOException {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", ADDRESS);

		createServer();

		while (true) {
			// Wait for incoming connection
			this.connect();

			byteMsg = connection.readMessage();
			
			printBytesAsHex(byteMsg);
			
			
			
			
			
			
			

			server.close();

		}

	}

	void createServer() throws UnknownHostException, IOException {
		// Step 1: Create Server
		server = new ServerSocket();
		server.bind(new InetSocketAddress(ADDRESS, PORT));
		System.out.println("Server created: " + server.getInetAddress());
		System.out.println("Waiting for client connection...");

	}

	boolean connect() throws IOException {
		socket = server.accept();
		return connection.serverConnect(socket);
	}

	public static void main(String[] args) {
		try {
			EmailServer emailServer = new EmailServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Prints to console a byte array in hexadecimal format
	 * 
	 * @param byteArr byte array to print as hex
	 */
	public static void printBytesAsHex(byte[] byteArr) {
		StringBuilder byteArray = new StringBuilder();
		System.out.print("Byte representation: ");
		for (byte b : byteArr) {
			byteArray.append(String.format("%02X ", b));
		}
		System.out.println(byteArray);
	}

}
