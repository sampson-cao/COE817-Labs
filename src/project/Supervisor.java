package project;

import static project.EncryptionUtil.*;

import java.security.Key;

public class Supervisor {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "localhost";
	final static private int PORT = 3001;
	final static private String CLIENT_ID = "purchaser";
	
	final static private int PURCHASER_PORT = 3000;
	final static private int ORDERS_DEPT_PORT = 3002;
	
	private Connection connection = null;

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	public Supervisor() {
		try {
			initializeCiphers();
			generateRSAKeys(PACKET_SIZE);
			
			startKeyDistributor(CLIENT_ID, getPublicKey());
			
			connection = new Connection(ADDRESS, PURCHASER_PORT);
			
			if (connection.clientConnect()){
				byteMsg = connection.readMessage();
				printBytesAsHex(byteMsg);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void startKeyDistributor(String id, Key puKey) {
		KeyDistributorThread keyDisThread = new KeyDistributorThread(id, puKey, ADDRESS, PORT);
		keyDisThread.start();
	}
	
	public static void main (String[] args) {
		Supervisor supervisor = new Supervisor();
	}

}
