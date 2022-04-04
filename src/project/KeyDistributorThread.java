package project;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.PublicKey;

public class KeyDistributorThread extends Thread{
	
	private String address = "localhost";
	private int port = 3000;
	
	private Connection connection;
	private ServerSocket server;
	private Socket socket;
	private String id;
	private Key key;

	public KeyDistributorThread(String id, Key puKey, String address, int port) {
		this.id = id;
		this.key = puKey;
		this.address = address;
		this.port = port;
	}
	
	public void run() {
		System.out.println("Running public key distribution thread");
		
		try {
			// Create new server with Address and port
			server = new ServerSocket();
			server.bind(new InetSocketAddress(address, port));
			connection = new Connection(address, port);
			
			while (true) {
				System.out.println("Waiting for client to connect...");
				
				if (connection.serverConnect(server)) {
					System.out.println("Client connected, sending public key");
					connection.sendMessage(key.getEncoded());
					
					connection.closeConnection();
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
	}

}
