package lab3;

/**
 * Thread for writing messages from the server to the console
 * 
 * @author Sampson
 */
public class WriteConsoleThread extends Thread {
	private Entity user;

	/**
	 * Constructor for thread, providing it with the client
	 * 
	 * @param socket
	 */
	public WriteConsoleThread(Entity user) {
		this.user = user;
	}

	/**
	 * Runs the loop for writing messages to console
	 */
	public void run() {
		System.out.println("Running console printing thread");
		String msg;
		try {

			while (true) {
				System.out.println(user.readMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
