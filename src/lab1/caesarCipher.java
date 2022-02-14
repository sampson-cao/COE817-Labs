/* 
 * Sampson Cao
 * COE817
 * 
 * A simple program to encrypt/decrypt Caesar ciphers
 */
package lab1;

public class caesarCipher {

	private static final int NUM_OF_CHARACTERS = 26;
	private static final int ASCII_OFFSET = 97;

	/**
	 * A function for encrypting/decrypting a Caesar Cipher (only lowercase)
	 * 
	 * The encryption algorithm operates by taking the ASCII code of a letter,
	 * adding the key, adding the number of characters in the character system (to
	 * account for underflow), and subtracting the ASCII offset to bring the
	 * character code to a number between which we then take mod26 of that number to
	 * bring a character code (96-122) to a number between 0 and 25. Then we add the
	 * ASCII_OFFSET to bring it back to the char code.
	 * 
	 * @param input - a piece of plaintext or ciphertext
	 * @param key   - an integer, postive to encrypt, negative to decrypt
	 * @return an encrypted or decrypted message
	 */
	public static String encrypt(String input, int key) {

		StringBuilder output = new StringBuilder();
		// to lowercase the input string to make processing capitalizations easy
		input = input.toLowerCase();

		for (int i = 0; i < input.length(); i++) {

			if (Character.isAlphabetic(input.charAt(i))) {
				output.append((char) ((input.charAt(i) + key + NUM_OF_CHARACTERS - ASCII_OFFSET) % NUM_OF_CHARACTERS
						+ ASCII_OFFSET));
			} else {
				output.append(input.charAt(i));
			}
		}

		return output.toString();
	}

	// method for testing
	public static void main(String args[]) {

		String a = "Glzkx g cnork, Rozzrk Jaiqrotm cgy zoxkj ul vrgeotm. Ynk yixgshrkj av utzu \r\n"
				+ "znk mxgyye hgtq gtj lrallkj uaz nkx lkgznkxy zu jxe. Gxuatj nkx znk cotj \r\n"
				+ "cnoyvkxkj ot znk mxgyy. Znk rkgbky xayzrkj gtj ubkxnkgj znk yqe mxkc \r\n"
				+ "jgxq. Rozzrk Jaiqrotm xkgrofkj ynk cgy grr grutk.";

		System.out.println(encrypt(a, -6));

	}
}
