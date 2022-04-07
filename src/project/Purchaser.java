package project;

import static project.EncryptionUtil.createPublicKey;
import static project.EncryptionUtil.encryptRSA;
import static project.EncryptionUtil.generateRSAKeys;
import static project.EncryptionUtil.getPrivateKey;
import static project.EncryptionUtil.getPublicKey;
import static project.EncryptionUtil.hashMessage;
import static project.EncryptionUtil.initializeCiphers;
import static project.EncryptionUtil.printBytesAsHex;
import static project.EncryptionUtil.signMessage;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Purchaser {

	final static private int PACKET_SIZE = 1024;

	final static private String ADDRESS = "localhost";

	final static private int TCP_PORT = 3000;
	final static private int EMAIL_PORT = 4445;
	final static private int TCP_SUPERVISOR_PORT = 3001;
	final static private int TCP_ORDERS_DEPT_PORT = 3002;
	final static private int UDP_PORT = 4000;
	final static private int UDP_SUPERVISOR_PORT = 4001;
	final static private int UDP_ORDERS_DEPT_PORT = 4002;

	final static private String CLIENT_ID = "purchaser";

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
			tcpConnection = new TCPConnection(ADDRESS, TCP_SUPERVISOR_PORT);
			if (tcpConnection.clientConnect()) {
				byteMsg = tcpConnection.readMessage();
				printBytesAsHex(byteMsg);
				puKeySupervisor = createPublicKey(byteMsg);
				System.out.println("Received supervisor public key");
			}
			
			tcpConnection = new TCPConnection(ADDRESS, TCP_ORDERS_DEPT_PORT);
			if (tcpConnection.clientConnect()) {
				byteMsg = tcpConnection.readMessage();
				printBytesAsHex(byteMsg);
				puKeyOrdersDept = createPublicKey(byteMsg);
				System.out.println("Received order dept public key");
			}
			
			System.out.println("All keys received, proceeding to UDP step");
			
			UDPConnection udpConnection = new UDPConnection(ADDRESS, UDP_PORT);

			// udpConnection.sendMessage("hello world".getBytes());
			String order = "Banana, 3, 0.99";
			byte[] message = order.getBytes();

			System.out.println("Order to send: " + order);
			printBytesAsHex(message);

			try {
				byte[] hash = hashMessage(message);

				System.out.print("Hashed message: ");
				printBytesAsHex(hash);

				// Get private key and sign hash with it
				byte[] sign = signMessage(hash, (PrivateKey) getPrivateKey());

				System.out.println("Signature: ");
				printBytesAsHex(sign);

				// Encrypt with Supervisor Public Key
				byte[] encryptedSendSuper = encryptRSA(message, puKeySupervisor);
				udpConnection.sendMessage(encryptedSendSuper, UDP_SUPERVISOR_PORT);
				udpConnection.sendMessage(sign, UDP_SUPERVISOR_PORT);
				udpConnection.sendMessage(sign, UDP_ORDERS_DEPT_PORT);

				byte[] recieved = udpConnection.receiveMessage();
				String status = new String(recieved);
				System.out.println(status);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Send to Supervisor & Orders Dept
			/*
			 * send(encryptedSendSuper,Super) send(hash,super) send(sign,super)
			 * 
			 * send(encryptedSendorders,orders) send(hash,orders) send(sign,orders)
			 */;
			// Wait for approve/denied from ordersdept

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void startKeyDistributor(String id, Key puKey) {
		KeyDistributorThread keyDisThread = new KeyDistributorThread(id, puKey, ADDRESS, TCP_PORT);
		keyDisThread.start();
	}

	public static void main(String[] args) {
		Purchaser purchaser = new Purchaser();
	}

}
