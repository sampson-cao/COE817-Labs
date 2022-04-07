package project;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class mailServer extends Thread {

	// Do not touch these
	private DatagramSocket socket;
	private boolean running;
	private byte[] buf = new byte[1024];
	private String[] user = { "purchaser", "supervisor", "orders" };
	private UDPConnection udpConnection;
	// touch these
	public mailServer() throws SocketException {
		udpConnection = new UDPConnection("localhost", 4445);
	}

	// check the users authority
	public String checkUser(String r) throws Exception {
		for (int i = 0; i < user.length; i++) {
			if (user[i].equals(r)) {
				return r;
			}
		}
		throw new Exception("User authority not found");
	}

	// run the server
	public void run() {
		running = true;
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		
		while (running) {
			System.out.println("running");
			try {
				buf = udpConnection.receiveMessage();
				
				String received = new String(buf, StandardCharsets.UTF_8);
				System.out.println(received);
				//buf = Arrays.copyOfRange(buf, 4, buf.length);
				
				EncryptionUtil.printBytesAsHex(buf);

				try {
					String authority = checkUser(received);
					System.out.println("Authority: " + authority);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					System.out.println("invalid user authority");
				}

				if (received.equals("end")) {
					running = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				
			}
			
			Arrays.fill(buf, (byte) 0);


		}
		socket.close();
	}

	public static void main(String[] args) {
		try {
			mailServer server = new mailServer();
			server.start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
