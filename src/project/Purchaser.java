package project;

import java.security.Key;
import java.security.PublicKey;
import static project.EncryptionUtil.*;
import static project.EncryptionUtil.printBytesAsHex;

public class Purchaser {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "localhost";
	final static private int PORT = 3000;
	final static private String CLIENT_ID = "purchaser";
	
	final static private int SUPERVISOR_PORT = 3001;
	final static private int ORDERS_DEPT_PORT = 3002;

	private Connection connection = null;

	// Public keys of the other clients
	public PublicKey puKeySupervisor;
	public PublicKey puKeyOrdersDept;

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	public Purchaser() {
		try {
			// Initialize elements
			initializeCiphers();
			generateRSAKeys(PACKET_SIZE);
			
			// Run key distributor thread to send keys to anyone who connects
			// to this address
			startKeyDistributor(CLIENT_ID, getPublicKey());
			
			// Start off by retrieving keys from the other entities
			connection = new Connection(ADDRESS, SUPERVISOR_PORT);
			
			if (connection.clientConnect()) {
				byteMsg = connection.readMessage();
				printBytesAsHex(byteMsg);
			}

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
