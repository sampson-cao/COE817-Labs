package project;

import java.security.Key;
import java.security.PublicKey;
import static project.EncryptionUtil.*;

public class Purchaser {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "localhost";
	final static private int PORT = 3000;
	final static private int EMAIL_PORT = 4445;
	final static private String CLIENT_ID = "purchaser";
	
	final static private int SUPERVISOR_PORT = 3001;
	final static private int ORDERS_DEPT_PORT = 3002;

	private TCPConnection tcpConnection = null;

	// Public keys of the other clients
	public PublicKey puKeySupervisor;
	public PublicKey puKeyOrdersDept;

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	public Purchaser() {
		try {
			System.out.println("Purchaser Client");
			// Initialize elements
			initializeCiphers();
			generateRSAKeys(PACKET_SIZE);
			
			// Run key distributor thread to send keys to anyone who connects
			// to this address
			startKeyDistributor(CLIENT_ID, getPublicKey());
			
			// Start off by retrieving keys from the other entities
			tcpConnection = new TCPConnection(ADDRESS, SUPERVISOR_PORT);
			if (tcpConnection.clientConnect()) {
				byteMsg = tcpConnection.readMessage();
				printBytesAsHex(byteMsg);
				puKeySupervisor = createPublicKey(byteMsg);
				System.out.println("Received supervisor public key");
			}
			
			tcpConnection = new TCPConnection(ADDRESS, ORDERS_DEPT_PORT);
			if (tcpConnection.clientConnect()) {
				byteMsg = tcpConnection.readMessage();
				printBytesAsHex(byteMsg);
				puKeyOrdersDept = createPublicKey(byteMsg);
				System.out.println("Received order dept public key");
			}
			
			System.out.println("All keys received, proceeding...");
			
			UDPConnection udpConnection = new UDPConnection(ADDRESS, EMAIL_PORT);
			
			udpConnection.sendMessage("hello world".getBytes());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void startKeyDistributor(String id, Key puKey) {
		KeyDistributorThread keyDisThread = new KeyDistributorThread(id, puKey, ADDRESS, PORT);
		keyDisThread.start();
	}

	public static void main(String[] args) {
		Purchaser purchaser = new Purchaser();
	}

}
