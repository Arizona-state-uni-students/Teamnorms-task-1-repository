package application;


public class PasswordEvaluator {
	/**
	 * <p> Title: Directed Graph-translated Password Assessor. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Directed Graph 
	 * diagram into an executable Java program using the Password Evaluator Directed Graph. 
	 * The code detailed design is based on a while loop with a cascade of if statements</p>
	 * 
	 * <p> Copyright: Lynn Robert Carter © 2022 </p>
	 * 
	 * @author Lynn Robert Carter
	 * 
	 * @version 0.00		2018-02-22	Initial baseline 
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message and a 
	 * pointer to the character of the error will enhance the user experience.
	 * 
	 */

	public static String passwordErrorMessage = "";		// The error message text
	public static String passwordInput = "";			// The input being processed
	public static int passwordIndexofError = -1;		// The index where the error was located
	public static boolean PfoundUpperCase = false;
	public static boolean PfoundLowerCase = false;
	public static boolean PfoundNumericDigit = false;
	public static boolean PfoundSpecialChar = false;
	public static boolean PfoundLongEnough = false;
	public static boolean PfoundOtherChar = false;
	public static boolean UfoundUpperCase = false;
	public static boolean UfoundLowerCase = false;
	public static boolean UfoundNumericDigit = false;
	public static boolean UfoundSpecialChar = false;
	public static boolean UfoundLongEnough = false;
	public static boolean UfoundOtherChar = false;
	private static String inputLine = "";				// The input line
	private static char currentChar;					// The current character in the line
	private static int currentCharNdx;					// The index of the current character
	private static boolean running;						// The flag that specifies if the FSM is 
														// running

	/**********
	 * This private method display the input line and then on a line under it displays an up arrow
	 * at the point where an error should one be detected.  This method is designed to be used to 
	 * display the error message on the console terminal.
	 * 
	 * @param input				The input string
	 * @param currentCharNdx	The location where an error was found
	 * @return					Two lines, the entire input line followed by a line with an up arrow
	 */
//	private static void displayInputState() {
//		// Display the entire input line
//		System.out.println(inputLine);
//		System.out.println(inputLine.substring(0,currentCharNdx) + "?");
//		System.out.println("The password size: " + inputLine.length() + "  |  The currentCharNdx: " + 
//				currentCharNdx + "  |  The currentChar: \"" + currentChar + "\"");
//	}

	/**********
	 * This method is a mechanical transformation of a Directed Graph diagram into a Java
	 * method.
	 * 
	 * @param input		The input string for directed graph processing
	 * @return			An output string that is empty if every things is okay or it will be
	 * 						a string with a help description of the error follow by two lines
	 * 						that shows the input line follow by a line with an up arrow at the
	 *						point where the error was found.
	 */
	public static String evaluatePassword(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordErrorMessage = "";
		passwordIndexofError = 0;			// Initialize the IndexofError
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		
		if(input.length() <= 0) return "*** Error *** The password is empty!";
		
		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0);		// The current character from the above indexed position

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition to a next state

		passwordInput = input;				// Save a copy of the input
		PfoundUpperCase = false;				// Reset the Boolean flag
		PfoundLowerCase = false;				// Reset the Boolean flag
		PfoundNumericDigit = false;			// Reset the Boolean flag
		PfoundSpecialChar = false;			// Reset the Boolean flag
		PfoundOtherChar = false;			    // Reset the Boolean flag
		PfoundLongEnough = false;			// Reset the Boolean flag
		running = true;						// Start the loop

		// The Directed Graph simulation continues until the end of the input is reached or at some 
		// state the current character does not match any valid transition
		while (running) {
			//displayInputState();
			// The cascading if statement sequentially tries the current character against all of the
			// valid transitions
			if (currentChar >= 'A' && currentChar <= 'Z') {
				System.out.println("Upper case letter found");
				PfoundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				System.out.println("Lower case letter found");
				PfoundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				System.out.println("Digit found");
				PfoundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+{}[]|:,.?/".indexOf(currentChar) >= 0) { //change special characters to reflect requirements
				System.out.println("Special character found");
				PfoundSpecialChar = true;
			} else {
				passwordIndexofError = currentCharNdx;
				PfoundOtherChar = true; //bad character identified
				return "*** Error *** An invalid character has been found!";
			}
			if (currentCharNdx >= 5) {
				System.out.println("At least 6 characters found");
				PfoundLongEnough = true;
			}
			
			// Go to the next character if there is one
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(currentCharNdx);
			
			System.out.println();
		}
		
		String errMessage = "";
		if (!PfoundUpperCase)
			errMessage += "Upper case; ";
		
		if (!PfoundLowerCase)
			errMessage += "Lower case; ";
		
		if (!PfoundNumericDigit)
			errMessage += "Numeric digits; ";
			
		if (!PfoundSpecialChar)
			errMessage += "Special character; ";
			
		if (!PfoundLongEnough)
			errMessage += "Long Enough; ";
		
		if (errMessage == "")
			return "";
		
		passwordIndexofError = currentCharNdx;
		return errMessage + "conditions were not satisfied";

	}
	
	public static String evaluateUsername(String input) {
		// The following are the local variable used to perform the Directed Graph simulation
		passwordErrorMessage = "";
		passwordIndexofError = 0;			// Initialize the IndexofError
		inputLine = input;					// Save the reference to the input line as a global
		currentCharNdx = 0;					// The index of the current character
		
		if(input.length() <= 0) return "*** Error *** The password is empty!";
		
		// The input is not empty, so we can access the first character
		currentChar = input.charAt(0);		// The current character from the above indexed position
		passwordInput = input;				// Save a copy of the input
		UfoundUpperCase = false;				// Reset the Boolean flag
		UfoundLowerCase = false;				// Reset the Boolean flag
		UfoundNumericDigit = false;			// Reset the Boolean flag
		UfoundSpecialChar = false;			// Reset the Boolean flag
		UfoundOtherChar = false;			    // Reset the Boolean flag
		UfoundLongEnough = false;			// Reset the Boolean flag
		running = true;						// Start the loop
		while (running) {
			//displayInputState();
			if (currentChar >= 'A' && currentChar <= 'Z') {
				System.out.println("Upper case letter found");
				UfoundUpperCase = true;
			} else if (currentChar >= 'a' && currentChar <= 'z') {
				System.out.println("Lower case letter found");
				UfoundLowerCase = true;
			} else if (currentChar >= '0' && currentChar <= '9') {
				System.out.println("Digit found");
				UfoundNumericDigit = true;
			} else if ("~`!@#$%^&*()_-+{}[]|:,.?/ ".indexOf(currentChar) >= 0) { //change special characters to reflect requirements
				System.out.println("Special character found");
				UfoundSpecialChar = true;
			} else {
				passwordIndexofError = currentCharNdx;
				UfoundOtherChar = true; //bad character identified
				return "*** Error *** An invalid character has been found!";
			}
			if (currentCharNdx >= 3) {
				System.out.println("At least 4 characters found");
				UfoundLongEnough = true;
			}
			
			// Go to the next character if there is one
			currentCharNdx++;
			if (currentCharNdx >= inputLine.length())
				running = false;
			else
				currentChar = input.charAt(currentCharNdx);
			
			System.out.println();
		}
		
		String errMessage = "";
//		if (!UfoundUpperCase)
//			errMessage += "Upper case; ";
//		
//		if (!UfoundLowerCase)
//			errMessage += "Lower case; ";
//		
//		if (!UfoundNumericDigit)
//			errMessage += "Numeric digits; ";
//			
		if (UfoundSpecialChar)
			errMessage += "Special character; ";
			
		if (!UfoundLongEnough)
			errMessage += "Long Enough; ";
		
		if (errMessage == "")
			return "";
		
		passwordIndexofError = currentCharNdx;
		return errMessage + "conditions were not satisfied";

	}
}
