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
import java.util.Scanner;

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
				puKeySupervisor = createPublicKey(byteMsg);
				System.out.println("Received supervisor public key");
			}

			tcpConnection = new TCPConnection(ADDRESS, TCP_ORDERS_DEPT_PORT);
			if (tcpConnection.clientConnect()) {
				byteMsg = tcpConnection.readMessage();
				puKeyOrdersDept = createPublicKey(byteMsg);
				System.out.println("Received order dept public key");
			}

			System.out.println("All keys received, proceeding to UDP step");

			UDPConnection udpConnection = new UDPConnection(ADDRESS, UDP_PORT);

			while (true) {
				try {
					String order = null;
					Scanner input = new Scanner(System.in);
					System.out.println("Enter item to purchase: ");
					String item = input.nextLine();
					String amount;
					String cost;
					byte[] message = null;
					if (item != null) {
						System.out.println("Quantity: ");
						amount = input.nextLine();
						if (Integer.parseInt(amount) >= 0) {
							System.out.println("Cost per unit: ");
							cost = input.nextLine();
							if (Double.parseDouble(cost) >= 0) {
								amount = amount.toString();
								cost = cost.toString();
								order = item + ", " + amount + ", " + cost;
								message = order.getBytes();
							} else {
								System.out.println("Cost must be more than 0");
							}
						} else {
							System.out.println("Amount must be more than 0");
						}

					} else {
						System.out.println("Invalid item");
					}

					// Sending order
					System.out.println("Order to send: " + order);
					printBytesAsHex(message);

					byte[] hash = hashMessage(message);

					// Get private key and sign hash with it
					byte[] sign = signMessage(hash, (PrivateKey) getPrivateKey());


					// Encrypt hashed message with Supervisor Public Key
					byte[] encryptedSendSuper = encryptRSA(message, puKeySupervisor);
					System.out.println("Sending encrypted message to supervisor");
					udpConnection.sendMessage(encryptedSendSuper, UDP_SUPERVISOR_PORT);
					System.out.println("Sending signature to supervisor");
					udpConnection.sendMessage(sign, UDP_SUPERVISOR_PORT);
					System.out.println("Sending signature to order's department");
					udpConnection.sendMessage(sign, UDP_ORDERS_DEPT_PORT);

					System.out.println("Waiting for confirmation message from order's department...");
					byte[] recieved = udpConnection.receiveMessage();
					String status = new String(recieved);
					System.out.println("Status of purchase order: " + status);


				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
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
		Purchaser purchaser = new Purchaser();
	}

}
