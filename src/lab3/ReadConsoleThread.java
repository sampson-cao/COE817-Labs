package lab3;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Thread for reading user input from console then sending it to server
 * 
 * @author Sampson
 */
public class ReadConsoleThread extends Thread {
	private BufferedReader reader;
	private Entity user;

	/**
	 * Constructor for thread, providing it with the client to operate on
	 * 
	 * @param client
	 */
	public ReadConsoleThread(Entity user) {
		this.user = user;
	}

	/**
	 * Runs the loop for reading user input
	 */
	public void run() {
		System.out.println("Running user input thread");
		reader = new BufferedReader(new InputStreamReader(System.in));
		String msg;
		try {

			while (true) {
				msg = reader.readLine();

				if (msg.equals("/send image.png")) {
					user.sendImage(msg.split(" ")[1]);
				} else {
					System.out.println("\nSending message: " + msg);
					user.sendText(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
