package project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;


public class testClient {
    private DatagramSocket socket;
    private InetAddress address;
    private byte[] header = new byte[4];
    private byte[] buf = new byte[1024];

    public testClient() throws SocketException, IOException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public String sendEcho(String msg) throws IOException {
        buf = msg.getBytes();
        int msgLen = buf.length;
        header[0] = (byte) (msgLen);
		header[1] = (byte) (msgLen >> 8);
		header[2] = (byte) (msgLen >> 16);
		header[3] = (byte) (msgLen >> 24);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1028);
		outputStream.write(header);
		outputStream.write(msg.getBytes());
		
		buf = outputStream.toByteArray();
        
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength());
        //String[] arr = received.split(",");
        received = received.trim();
        return received;
    }

    public void close() {
        socket.close();
    }
    
    public static void main(String[] args) {
    	
    	UDPConnection udpConnection = new UDPConnection("localhost", 6215);
		Scanner sc = new Scanner(System.in);
		while(true) {
			String a = sc.nextLine();
			udpConnection.sendMessage(a.getBytes());
		}
    	
    	/*
    	testClient client;
		try {
			client = new testClient();
			String authority = "purchaser";
			client.sendEcho(authority);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
    	
    }
}
