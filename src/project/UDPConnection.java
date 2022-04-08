package project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * A class that houses useful functions relating to UDP communications
 * 
 * @author Sampson Cao
 *
 */
public class UDPConnection {
	/** Transmission packet size in bytes */
	final private static int PACKET_SIZE = 1024;
	
	/** Port of the email server */
	final private static int EMAIL_PORT = 4445;

	/** IP address to send UDP packet to */
	private InetAddress host = null;

	/** Port to send UDP packet on (default = 4445) */
	private int port = 4445;

	/** Socket to connect to */
	private DatagramSocket socket = null;

	/** Output stream for concatenating byte arrays */
	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	/** UDP Packet to send/receive */
	DatagramPacket packet = null;

	/** 4 byte header packet (for length of message */
	private byte[] header = new byte[4];
	/** Buffer byte array for sending and receiving messages */
	private byte[] buf = new byte[PACKET_SIZE];

	public UDPConnection(String host, int port) {

		this.port = port;
		packet = new DatagramPacket(buf, PACKET_SIZE, this.host, port);

		try {
			this.host = InetAddress.getByName(host);
			socket = new DatagramSocket(port);

		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("Unknown host");
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends the provided byte message to the email server
	 * @param msg input message
	 */
	public void sendMessage(byte[] msg, int port) {
		// convert integer value message length into a 4 byte header
		Arrays.fill(buf, (byte) 0);

		int msgLen = msg.length;
		header[0] = (byte) (msgLen);
		header[1] = (byte) (msgLen >> 8);
		header[2] = (byte) (msgLen >> 16);
		header[3] = (byte) (msgLen >> 24);

		buf = concat(header, msg);

		buf = Arrays.copyOf(buf, PACKET_SIZE);

		packet = new DatagramPacket(buf, PACKET_SIZE, host, port);
		packet.setData(buf);

		try {
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Unable to send packet");
		}

		System.out.print("Packet of length sent: " + msgLen + " Data: ");
		printBytesAsHex(buf);
	}
	
	/**
	 * Receives a message from the socket and removes the header, removes padding
	 * then returns the byte message itself
	 * @return byte message without header or padding
	 * @throws IOException if something goes wrong
	 */
	public byte[] receiveMessage() throws IOException {
		socket.receive(packet);
		buf = packet.getData();
		// Converts the 4 byte header into an integer value
		int msgLen = ((buf[0] & 0xff) << 0 | (buf[1] & 0xff) << 8 | (buf[2] & 0xff) << 16 | (buf[3] & 0xff) << 24);
		System.out.print("Received packet of length: " + msgLen + " Data: ");
		printBytesAsHex(buf);
		buf = Arrays.copyOfRange(buf, header.length, msgLen + header.length);
		return buf;
	}

	/**
	 * Concatenates two byte arrays and returns resulting byte array
	 * 
	 * @param arr1 first array
	 * @param arr2 second array
	 * @return Concatenated array
	 */
	private byte[] concat(byte[] arr1, byte[] arr2) {
		try {
			outputStream.reset();
			outputStream.write(arr1);
			outputStream.write(arr2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to concatenate header and msg");
			return null;
		}
		return outputStream.toByteArray();
	}

	public void UDPclose() {
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
