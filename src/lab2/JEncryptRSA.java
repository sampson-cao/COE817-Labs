package lab2;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class JEncryptRSA {
	
	/* This code demonstrates RSA encryption in Java
	 * The bytes for the user input, resulting ciphertext,
	 * and decrypted message will be shown
	 */

	public static void main(String args[]) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			IllegalBlockSizeException, BadPaddingException {
		Scanner sc = new Scanner(System.in);
		String msg;
		byte[] msgArr;
		String cipherText = null;
		byte[] cipherArr;

		// Retrieve user input
		System.out.println("Please enter your message");
		msg = sc.nextLine();
		msgArr = msg.getBytes(StandardCharsets.UTF_8);
		System.out.println("Message Entered: " + msg);
		System.out.println("Message byte array:");
		printBytesAsHex(msgArr);

		// Key generation step
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		KeyPair key = kpg.generateKeyPair();
		
		// Cipher configuration
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
		cipherArr = cipher.doFinal(msgArr);
		cipherText = new String(cipherArr, StandardCharsets.UTF_8);
		
		System.out.println("Ciphertext: " + cipherText);
		System.out.println("Ciphertext byte array:");
		printBytesAsHex(cipherArr);
		
		// reconfigure cipher to decrypt
		cipher.init(Cipher.DECRYPT_MODE, key.getPrivate());
		
		msgArr = cipher.doFinal(cipherArr);
		msg = new String(msgArr, StandardCharsets.UTF_8);
		System.out.println("Decrypted text: " + msg);
		System.out.println("Decrypted text bytes:");
		printBytesAsHex(msgArr);

	}

	// Helper function for printing byte array into hex values
	public static void printBytesAsHex(byte[] byteArr) {
		StringBuilder byteArray = new StringBuilder();
		for (byte b : byteArr) {
			byteArray.append(String.format("%02X ", b));
		}
		System.out.println(byteArray);
	}

}
