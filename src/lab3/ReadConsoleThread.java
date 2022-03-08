package lab3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadConsoleThread {
	private BufferedReader reader;
	private InputStream input;
	
	
	public ReadConsoleThread(InputStream input) {
		this.input = input;
		reader = new BufferedReader(new InputStreamReader(input));
	}
	
	public void run() {
		String msg;
		while (true) {
			try {
				msg = reader.readLine();
				System.out.println("\n" + msg);
				
				
			} catch (IOException e) {
				System.out.println("IO error");
				e.printStackTrace();
				break;
			}
		}
	}

}
