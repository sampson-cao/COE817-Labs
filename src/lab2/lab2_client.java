package lab2;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.io.*;

public class lab2_client {

	private String ida = "INITIATOR A";
	final private int PACKET_SIZE = 1024;
	// initialize socket and input/output streams
	private Socket socket = null;
	private InputStream input = null;
	private OutputStream output = null;

	private byte[] buf = new byte[PACKET_SIZE];

	public lab2_client(String address, int port) {
		// define socket based on constructor parameters
		try {
			socket = new Socket(address, port);

			System.out.println("Successfully connected");

			// takes input from console and sends it to socket
			input = socket.getInputStream();
			output = socket.getOutputStream();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Input reading, exit to leave program
		String in = "";
		in = ida;
		System.out.println("Message to send: " + in);

		buf = Arrays.copyOf(in.getBytes(), PACKET_SIZE);

		try {
			output.write(buf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Insert encryption code here/receive code

		// close connections
		try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}

	public static void main(String args[]) {
		lab2_client c = new lab2_client("0.0.0.0", 8000);
	}

}
