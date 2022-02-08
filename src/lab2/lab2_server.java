package lab2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class lab2_server {
	// initialize socket and input/output streams
	private Socket socket = null;
	private ServerSocket server = null;
	private BufferedReader input = null;
	private BufferedWriter output = null;
	
	public lab2_server(int port) {
		

		try {
			// start server and wait for incoming connection
			server = new ServerSocket(port);
			System.out.println("Server started");
			
			System.out.println("Waiting for client to connect...");
			
			socket = server.accept();
			System.out.println("Client accepted");
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String in = "";
			
			while (in != "exit") {
				try
                {
                    in = input.readLine();
                    System.out.println("Received Input: " + in);
                    
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
            }
            System.out.println("Closing connection");
 
            // close connection
            socket.close();
            input.close();
            output.close();
			
			
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
	}

}
