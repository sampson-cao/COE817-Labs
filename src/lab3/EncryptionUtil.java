package lab3;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Helper class housing helper functions involving cryptography for the chat appliation
 * @author Sampson Cao
 *
 */
public class EncryptionUtil {

	public EncryptionUtil() {
		// TODO Auto-generated constructor stub
	}
	
	// Encrypts the byteArr fed into it as input
		public byte[] encryptDES(byte[] byteArr)
				throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
			byte[] output = null;
			cipherDES.init(Cipher.ENCRYPT_MODE, sessionKey);

			System.out.println("Byte Array to encrypt: ");
			System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
			printBytesAsHex(byteArr);

			output = cipherDES.doFinal(byteArr);
			System.out.println("Byte Array encrypted: ");
			System.out.println("String Representation: " + new String(output, StandardCharsets.UTF_8));
			printBytesAsHex(output);
			return output;

		}

		// Decrypts the byteArr fed into it as input
		public byte[] decryptDES(byte[] byteArr)
				throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
			byte[] output = null;
			cipherDES.init(Cipher.DECRYPT_MODE, sessionKey);

			System.out.println("Byte Array to decrypt: ");
			System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
			printBytesAsHex(byteArr);

			output = cipherDES.doFinal(byteArr);
			System.out.println("Byte Array decrypted: ");
			System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
			printBytesAsHex(output);
			return output;
		}

		// method overloading to make the boolean an optional parameter
		public byte[] encryptRSA(byte[] byteArr)
				throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
			return encryptRSA(byteArr, true);
		}

		// encrypts the byteArr fed into it using RSA algorithm
		public byte[] encryptRSA(byte[] byteArr, boolean pub)
				throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

			byte[] output = null;

			if (pub == true) {
				cipherDES.init(Cipher.ENCRYPT_MODE, puKey);
			} else {
				cipherDES.init(Cipher.ENCRYPT_MODE, prKey);
			}

			System.out.println("Byte Array to encrypt: ");
			System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
			printBytesAsHex(byteArr);

			output = cipherRSA.doFinal(byteArr);
			System.out.println("Byte Array encrypted");
			System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
			printBytesAsHex(output);
			return output;
		}

		// method overloading to make the boolean an optional parameter
		public byte[] decryptRSA(byte[] byteArr)
				throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
			return decryptRSA(byteArr, true);
		}

		// Decrypts the byteArr fed into it using RSA algorithm
		public byte[] decryptRSA(byte[] byteArr, boolean priv)
				throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

			byte[] output = null;

			if (priv == true) {
				cipherDES.init(Cipher.DECRYPT_MODE, puKey);
			} else {
				cipherDES.init(Cipher.DECRYPT_MODE, prKey);
			}

			System.out.println("Byte Array to decrypt: ");
			System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
			printBytesAsHex(byteArr);

			output = cipherRSA.doFinal(byteArr);
			System.out.println("Byte Array decrypted: ");
			System.out.println("String representation: " + new String(output, StandardCharsets.UTF_8));
			printBytesAsHex(output);
			return output;
		}

}
