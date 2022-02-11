/* 
 * Sampson Cao
 * COE817
 * 
 * A simple program to encrypt/decrypt Vigenere ciphers
 */
package lab1;

public class Vingenere {

	private static final int NUM_OF_CHARACTERS = 26;
	private static final int ASCII_OFFSET = 97;

	/**
	 * A function for encrypting/decrypting a Vingenere Cipher (only lowercase)
	 * 
	 * Encryption algorithm takes the character code at index i, converts it to the
	 * letter's number (a = 0, z = 25), then adds key's character code at index i
	 * converted to the letter's number, then converts that back to its character
	 * code
	 * 
	 * @param input - a piece of plaintext
	 * @param key   - the key used to encrypt
	 * @return encrypted message
	 */
	public static String encrypt(String input, String key) {

		StringBuilder output = new StringBuilder();

		// checks if the length match, otherwise convert key to a repeating key
		if (input.length() != key.length()) {
			key = repeatKey(input, key);
		}

		// lowercase the input string and key string to make things easier
		input = input.toLowerCase();
		key = key.toLowerCase();

		for (int i = 0; i < input.length(); i++) {
			if (Character.isAlphabetic(input.charAt(i))) {
				output.append(
						(char) (((input.charAt(i) - ASCII_OFFSET) + (key.charAt(i) - ASCII_OFFSET)) % NUM_OF_CHARACTERS
								+ ASCII_OFFSET));
			} else {
				output.append(input.charAt(i));
			}
		}

		return output.toString();
	}

	/**
	 * The same algorithm above but instead of merely adding the input and key's
	 * number together, subtract them and add the NUM_OF_CHARACTERS to account for
	 * underflow
	 * 
	 * @param input - a piece of ciphertext
	 * @param key   - the key used to encrypt
	 * @return decrypted message
	 */
	public static String decrypt(String input, String key) {

		StringBuilder output = new StringBuilder();

		// checks if the lengths match, otherwise convert key to a repeating key
		if (input.length() != key.length()) {
			key = repeatKey(input, key);
		}

		// to lowercase the input string and key string for ease of processing
		input = input.toLowerCase();
		key = key.toLowerCase();

		for (int i = 0; i < input.length(); i++) {

			if (Character.isAlphabetic(input.charAt(i))) {
				output.append(
						(char) (((input.charAt(i) - ASCII_OFFSET) - (key.charAt(i) - ASCII_OFFSET) + NUM_OF_CHARACTERS)
								% NUM_OF_CHARACTERS + ASCII_OFFSET));
			} else {
				output.append(input.charAt(i));
			}
		}

		return output.toString();

	}

	/**
	 * A helper function used to convert a key into a repeating key ex. input =
	 * "save me please", key = "help" will return key = "helphelphelphe"
	 * 
	 * @param input
	 * @param key
	 * @return
	 */
	public static String repeatKey(String input, String key) {

		System.out.println("Input and key don't have the same length, defaulting to repeating key");

		StringBuilder tempKey = new StringBuilder(key);

		for (int i = 0; tempKey.length() != input.length(); i++) {
			tempKey.append(key.charAt(i));

			if (tempKey.length() == input.length()) {
				key = tempKey.toString();
				break;
			}
			// prevent i from going beyond the key's length
			i %= key.length() - 1;
		}
		System.out.println("Key: " + tempKey);
		return tempKey.toString();
	}

	public static void main(String args[]) {

		String input = "To be or not to be that is the question";
		String key = "relations";

		System.out.println("Input String: " + input);

		String ciphertext = encrypt(input, key);

		System.out.println("Ciphertext: " + ciphertext);

		String plaintext = decrypt(ciphertext, key);

		System.out.println("Plaintext: " + plaintext);

	}

}
