package project;

import static project.EncryptionUtil.generateRSAKeys;
import static project.EncryptionUtil.getPublicKey;
import static project.EncryptionUtil.initializeCiphers;
import static project.EncryptionUtil.printBytesAsHex;

import java.security.Key;

public class OrdersDept {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "localhost";
	final static private int PORT = 3002;
	final static private String CLIENT_ID = "purchaser";
	
	final static private int PURCHASER_PORT = 3000;
	final static private int SUPERVISOR_PORT = 3001;
	
	private Connection connection = null;

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	public OrdersDept() {
		try {
			initializeCiphers();
			generateRSAKeys(PACKET_SIZE);

			startKeyDistributor(CLIENT_ID, getPublicKey());

			connection = new Connection(ADDRESS, PURCHASER_PORT);
			
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
	
	public static void main (String[] args) {
		OrdersDept ordersDept = new OrdersDept();
	}

}
