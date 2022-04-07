package project;

import static project.EncryptionUtil.createPublicKey;
import static project.EncryptionUtil.decryptRSA;
import static project.EncryptionUtil.encryptRSA;
import static project.EncryptionUtil.generateRSAKeys;
import static project.EncryptionUtil.getPrivateKey;
import static project.EncryptionUtil.getPublicKey;
import static project.EncryptionUtil.initializeCiphers;
import static project.EncryptionUtil.printBytesAsHex;
import static project.EncryptionUtil.signMessage;
import static project.EncryptionUtil.verify;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Supervisor {

	final static private int PACKET_SIZE = 1024;

	final static private String ADDRESS = "localhost";

	final static private int EMAIL_PORT = 4445;
	final static private int TCP_PORT = 3001;
	final static private int TCP_PURCHASER_PORT = 3000;
	final static private int TCP_ORDERS_DEPT_PORT = 3002;
	final static private int UDP_PORT = 4001;
	final static private int UDP_PURCHASER_PORT = 4000;
	final static private int UDP_ORDERS_DEPT_PORT = 4002;

	final static private String CLIENT_ID = "purchaser";

	private TCPConnection connection = null;

	// Public keys of the other clients
	public PublicKey puKeyPurchaser;
	public PublicKey puKeyOrdersDept;

	// Message array representing the byte form of a message
	private byte[] byteMsg = new byte[PACKET_SIZE];
	// Message String representing the string form of a message
	private String strMsg = "";

	public Supervisor() {
		try {
			initializeCiphers();
			generateRSAKeys(PACKET_SIZE);

			startKeyDistributor(CLIENT_ID, getPublicKey());

			connection = new TCPConnection(ADDRESS, TCP_PURCHASER_PORT);
			if (connection.clientConnect()) {
				byteMsg = connection.readMessage();
				printBytesAsHex(byteMsg);
				puKeyPurchaser = createPublicKey(byteMsg);
				System.out.println("Received purchaser public key");
			}

			connection = new TCPConnection(ADDRESS, TCP_ORDERS_DEPT_PORT);
			if (connection.clientConnect()) {
				byteMsg = connection.readMessage();
				printBytesAsHex(byteMsg);
				puKeyOrdersDept = createPublicKey(byteMsg);
				System.out.println("Received orders dept public key");
			}

			System.out.println("All keys received, proceeding to UDP step");

			// UDPConnection udpConnection = new UDPConnection(ADDRESS, EMAIL_PORT);
			UDPConnection udpConnection = new UDPConnection(ADDRESS, UDP_PORT);

			// Receive Message from Purchaser
			byte[] Emessage = udpConnection.receiveMessage();
			byte[] purchaserSignature = udpConnection.receiveMessage();

			System.out.print("Received Email message: ");
			printBytesAsHex(Emessage);
			System.out.print("\nSignature: ");
			printBytesAsHex(purchaserSignature);

			// Decrypt
			byte[] decryptMsg = decryptRSA(Emessage);

			if (verify(decryptMsg, puKeyPurchaser, purchaserSignature)) {
				// If approved encrypt with Order public key
				// Get message hash and Sign with Supervisor private key
				byte[] encryptedMsg = encryptRSA(decryptMsg, puKeyOrdersDept);
				byte[] superSign = signMessage(decryptMsg, (PrivateKey) getPrivateKey());

				// send to orders department
				udpConnection.sendMessage(encryptedMsg, UDP_ORDERS_DEPT_PORT);
				udpConnection.sendMessage(superSign, UDP_ORDERS_DEPT_PORT);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void startKeyDistributor(String id, Key puKey) {
		KeyDistributorThread keyDisThread = new KeyDistributorThread(id, puKey, ADDRESS, TCP_PORT);
		keyDisThread.start();
	}

	public static void main(String[] args) {
		Supervisor supervisor = new Supervisor();
	}

}