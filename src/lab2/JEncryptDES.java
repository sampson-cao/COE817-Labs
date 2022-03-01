package lab2;

import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.security.*;
import javax.crypto.*;

/* This code demonstrates DES encryption in Java
 * The bytes for the user input, resulting ciphertext,
 * and decrypted message will be shown
 */
public class JEncryptDES {

	public static void main(String args[]) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		Scanner sc = new Scanner(System.in);
		String msg;
		byte[] msgArr;
		String cipherText;
		byte[] cipherArr;
		
		System.out.println("Please enter your message");
		msg = sc.nextLine();
		msgArr = msg.getBytes(StandardCharsets.UTF_8);
		System.out.println("Message Entered: " + msg);
		System.out.println("Message byte array:");
		printBytesAsHex(msgArr);
		
		// Key generation step
		KeyGenerator kg = null;
		kg = KeyGenerator.getInstance("DES");
		SecretKey key = kg.generateKey();
		
		// Cipher configuration
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		
		// Actual encryption
		cipherArr = cipher.doFinal(msgArr);
		cipherText = new String(cipherArr, StandardCharsets.UTF_8);
		
		System.out.println("Encrypted Text: " + cipherText);
		System.out.println("Encrypted byte array:");
		printBytesAsHex(cipherArr);
		
		// Reconfigure cipher to decrypt mode
		cipher.init(Cipher.DECRYPT_MODE, key);
		
		// Actual decryption step
		msgArr = cipher.doFinal(cipherArr);
		msg = new String(msgArr, StandardCharsets.UTF_8);
		
		System.out.println("Decrypted text: " + msg);
		System.out.println("Decrypted text bytes:");
		printBytesAsHex(msgArr);
		
	}
	
	// Helper function for printing byte array into hex values
	public static void printBytesAsHex (byte[] byteArr) {
		StringBuilder byteArray = new StringBuilder();
		for (byte b : byteArr) {
			byteArray.append(String.format("%02X ", b));
		}
		System.out.println(byteArray);
	}

}
