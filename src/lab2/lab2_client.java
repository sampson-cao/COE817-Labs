package lab2;
import java.net.*;
import java.io.*;

public class lab2_client {
	
	// initialize socket and input/output streams
	private Socket socket = null;
	private BufferedReader input = null;
	private BufferedWriter output = null;
	
	public lab2_client(String address, int port){
		
		// define socket based on constructor parameters
		try {
			socket = new Socket(address, port);
			
			System.out.println("Successfully connected");
			
			// takes input from console and sends it to socket
			input = new BufferedReader(new InputStreamReader(System.in));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Input reading, exit to leave program
		String in = "";
		while (in != "exit") {
			try {
				in = input.readLine();
				System.out.println("Message Received: " + in);
				
				
			}catch(IOException ioe) {
				ioe.printStackTrace();
			}
			
			// Insert encryption code here/receive code 
		}
		
		// close connections
		try {
			input.close();
			output.close();
			socket.close();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
	}
	
	public static void main (String args[]) {
		lab2_client c = new lab2_client("192.168.2.1", 8000);
	}

}
