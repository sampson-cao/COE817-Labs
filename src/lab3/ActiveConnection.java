package lab3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * A class that houses an open connection between the client and server. Also
 * contains helper functions involving the communication across the two entities
 * 
 * @author Sampson Cao
 *
 */
public class ActiveConnection {

	final static private int PACKET_SIZE = 1024;
	final static private String ADDRESS = "0.0.0.0";
	final static private int PORT = 8000;
	byte[] buf = new byte[PACKET_SIZE];

	// Socket and input/output stream declarations
	private Socket socket = null;
	private InputStream input = null;
	private OutputStream output = null;

	void sendMessage(byte[] msg) throws IOException {
		if (msg.length <= PACKET_SIZE) {
			buf = Arrays.copyOf(msg, PACKET_SIZE);
			output.write(buf);
		} else {
			for (int i = 0; i < msg.length; i++) {
				// send message then start overwriting buffer
				if (i % PACKET_SIZE == 0) {
					output.write(buf);
					output.flush();
				} else {
					buf[i % PACKET_SIZE] = msg[i];
				}
			}
		}
	}
	
	boolean connect() throws UnknownHostException, IOException {
		socket = new Socket(ADDRESS, PORT);
		input = socket.getInputStream();
		output = socket.getOutputStream();

		if (socket.isConnected()) {
			System.out.println("Successfully connected to server");
			return true;
		} else {
			System.out.println("Failed to connect");
			return false;
		}
	}

	private ActiveConnection() {
		// TODO Auto-generated constructor stub
	}

}
