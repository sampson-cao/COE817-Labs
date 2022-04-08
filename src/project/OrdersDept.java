package project;

import static project.EncryptionUtil.createPublicKey;
import static project.EncryptionUtil.decryptRSA;
import static project.EncryptionUtil.generateRSAKeys;
import static project.EncryptionUtil.getPublicKey;
import static project.EncryptionUtil.hashMessage;
import static project.EncryptionUtil.initializeCiphers;
import static project.EncryptionUtil.verify;

import java.security.Key;
import java.security.PublicKey;

public class OrdersDept {

	final static private int PACKET_SIZE = 1024;

	final static private String ADDRESS = "localhost";

	final static private int TCP_PORT = 3002;
	final static private int TCP_PURCHASER_PORT = 3000;
	final static private int TCP_SUPERVISOR_PORT = 3001;
	final static private int UDP_PORT = 4002;
	final static private int UDP_PURCHASER_PORT = 4000;
	final static private int UDP_SUPERVISOR_PORT = 4001;

	final static private String CLIENT_ID = "orders";

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

			connection = new TCPConnection(ADDRESS, TCP_PURCHASER_PORT);

			if (connection.clientConnect()) {
				byteMsg = connection.readMessage();
				puKeyPurchaser = createPublicKey(byteMsg);
				System.out.println("Received purchaser public key");
			}

			connection = new TCPConnection(ADDRESS, TCP_SUPERVISOR_PORT);

			if (connection.clientConnect()) {
				byteMsg = connection.readMessage();
				puKeySupervisor = createPublicKey(byteMsg);
				System.out.println("Received supervisor public key");
			}

			System.out.println("All keys received, proceeding to UDP step");

			UDPConnection udpConnection = new UDPConnection(ADDRESS, UDP_PORT);

			while (true) {

				System.out.println("Waiting for Purchaser's signature...");
				byte[] purchaserSignature = udpConnection.receiveMessage();

				System.out.println("Waiting for Email message from supervisor...");
				byte[] EMessage = udpConnection.receiveMessage();

				System.out.println("Waiting for Supervisor's signature...");
				byte[] superSignature = udpConnection.receiveMessage();

				System.out.println("Decrypting email");
				byte[] decryptMsg = decryptRSA(EMessage);

				byte[] hash = hashMessage(decryptMsg);

				// If purchaser and supervisor is verfied
				if (verify(hash, puKeyPurchaser, purchaserSignature) && verify(hash, puKeySupervisor, superSignature)) {

					// Approve the order
					String message = "Order Approved";
					udpConnection.sendMessage(message.getBytes(), UDP_PURCHASER_PORT);
				} else {
					// Otherwise, deny the order
					String message = "Order Denied";
					udpConnection.sendMessage(message.getBytes(), UDP_PURCHASER_PORT);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startKeyDistributor(String id, Key puKey) {
		KeyDistributorThread keyDisThread = new KeyDistributorThread(id, puKey, ADDRESS, TCP_PORT);
		keyDisThread.start();
	}

	public static void main(String[] args) {
		OrdersDept ordersDept = new OrdersDept();
	}

}