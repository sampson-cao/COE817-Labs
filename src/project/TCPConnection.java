package project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * A class that houses an open connection between the client and server. Also
 * contains helper functions involving the communication across the two entities
 * 
 * @author Sampson Cao
 *
 */
public class TCPConnection {

	/** Transmission packet size in bytes */
	final private static int PACKET_SIZE = 1024;

	/** Address to connect to */
	private String address = "localhost";
	/** Port to connect to */
	private int port = 8000;

	/** Socket to connect to */
	private Socket socket = null;
	private InputStream input = null;
	private OutputStream output = null;

	/** 4 byte header packet (for length of message */
	private byte[] header = new byte[4];
	/** Buffer byte array for sending and receiving messages */
	private byte[] buf = new byte[PACKET_SIZE];

	/**
	 * Creates a connection provided user id, address, and port
	 * 
	 * @param address address of connection
	 * @param port    port of connection
	 */
	public TCPConnection(String address, int port) {
		this.address = address;
		this.port = port;
	}

	/**
	 * Returns the socket
	 * 
	 * @return socket or null if connect() hasn't been called yet
	 */
	public Socket getSocket() {
		if (socket == null) {
			System.out.println("Connection not made yet");
			return null;
		} else {
			return socket;
		}
	}

	/**
	 * For client connections, instantiates the socket and creates the input and
	 * output streams to read and write to
	 * 
	 * @return true on successful connection
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	boolean clientConnect() throws UnknownHostException, IOException, InterruptedException {
		// Step 1: Connect to server
		while (true) {
			socket = new Socket();

			try {
				socket.connect(new InetSocketAddress(address, port), 10000);
				System.out.println("Connected to server");
				break;
			} catch (ConnectException ce) {
				System.out.println("Connection failed, retrying...");
				Thread.currentThread().sleep(2000);
			}

		}
		input = socket.getInputStream();
		output = socket.getOutputStream();

		if (socket.isConnected()) {
			System.out.println("Successfully connected");
			return true;
		} else {
			System.out.println("Failed to connect");
			return false;
		}
	}

	/**
	 * For server, use the socket provided and create the input and output streams
	 * to read and write to
	 * 
	 * @return true on successful connection
	 * @throws IOException
	 */
	boolean serverConnect(ServerSocket server) throws IOException {
		this.socket = server.accept();
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

	/**
	 * Reads a message from the socket and removes padding bytes
	 * 
	 * @throws IOException
	 */
	byte[] readMessage() throws IOException {
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
		buf = Arrays.copyOf(buf, msgLen);
		return buf;
	}

	/**
	 * Sends the byte array provided over the socket
	 * 
	 * @param msg msg in byte array format
	 * @throws IOException
	 */
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
			System.out.println("Wrote: ");
			printBytesAsHex(buf);
		} else {
			for (int i = 0; i < msg.length; i++) {
				// send message then start overwriting buffer
				if (i % PACKET_SIZE == 0) {
					output.write(buf);
					System.out.println("Wrote: ");
					printBytesAsHex(buf);
				} else {
					buf[i % PACKET_SIZE] = msg[i];
				}
			}
		}
		output.flush();
	}

	void closeConnection() throws IOException {
		socket.close();
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
