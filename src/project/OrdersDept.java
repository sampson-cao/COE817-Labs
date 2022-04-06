package project;

import static project.EncryptionUtil.*;

import java.security.Key;
import java.security.PublicKey;

public class OrdersDept {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "localhost";
	final static private int PORT = 3002;
	final static private String CLIENT_ID = "purchaser";

	final static private int PURCHASER_PORT = 3000;
	final static private int SUPERVISOR_PORT = 3001;

	private TCPConnection connection = null;

	// Public keys of the other clients
	public PublicKey puKeyPurchaser;
	public PublicKey puKeySupervisor;

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	public OrdersDept() {
		try {
			initializeCiphers();
			generateRSAKeys(PACKET_SIZE);

			startKeyDistributor(CLIENT_ID, getPublicKey());

			connection = new TCPConnection(ADDRESS, PURCHASER_PORT);

			if (connection.clientConnect()) {
				byteMsg = connection.readMessage();
				printBytesAsHex(byteMsg);
				puKeyPurchaser = createPublicKey(byteMsg);
				System.out.println("Received purchaser public key");
			}

			connection = new TCPConnection(ADDRESS, SUPERVISOR_PORT);

			if (connection.clientConnect()) {
				byteMsg = connection.readMessage();
				printBytesAsHex(byteMsg);
				puKeySupervisor = createPublicKey(byteMsg);
				System.out.println("Received supervisor public key");
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
		OrdersDept ordersDept = new OrdersDept();
	}

}
