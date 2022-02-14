package lab2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class lab2_server {

	final private int PACKET_SIZE = 1024;

	private String ida = "RESPONDER B";
	private String km = "NETWORK SECURITY";
	private String ks = "RYERSON";
	String in = "";
	int inlen = 0;

	// initialize socket and input/output streams
	private Socket socket = null;
	private ServerSocket server = null;
	private InputStream input = null;
	private OutputStream output = null;

	private byte[] buf = new byte[PACKET_SIZE];

	public lab2_server(int port) {

		try {
			// start server and wait for incoming connection
			server = new ServerSocket(port);
			System.out.println("Server started");
			System.out.println(server.getInetAddress());
			System.out.println("Waiting for client to connect...");

			socket = server.accept();
			System.out.println("Client accepted");

			input = socket.getInputStream();
			
			// receive initial packet
			inlen = input.readNBytes(buf, 0, 1024);
			in = new String(buf, StandardCharsets.UTF_8);
			System.out.println("Received Input: " + in);
			System.out.println("Size: " + inlen + "\n");
			
			// send response packet
			
			
			System.out.println("Closing connection");

			// close connection
			socket.close();
			input.close();
			output.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

	}
	
	public void encrypt(byte[] bytearr) {
		
	}

	public static void main(String args[]) {
		lab2_server s = new lab2_server(8000);
	}

}
