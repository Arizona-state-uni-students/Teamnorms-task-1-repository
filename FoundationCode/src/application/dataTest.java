package application;

import java.util.Random;

public class dataTest{
	// This class implements Questions and Answers testing by generating random strings and running a specified amount of tests.


	public static String stats;

	public static String getStats() {
		return stats;
	}
	
	// Creates a random String
	public static String createString(int length) {
		String string=""; //initialize blank string
		
		for(int i = 0; i<=length; i++) {
		char character = (char) random(32, 126); // get a random ascii character from the keyboard (between 32-126)
		string+=character;
		}
		
		return string;
	}
	
	// Creates a random number
	private static int random(int min, int max) {
		Random random = new Random();
		int gen = random.nextInt(max - min + 1) + min;
		return gen;
	}
	
	// Tests Question Title and Body sections and records results.
	public static void testQuestion(int tests) {
	    // Requirements:
	    // QuestionTitle: Must be at least 5 characters, max 100
	    // QuestionBody: Must be at least 10 characters, max 500
	    // Other inputs are hard coded and/or already validated
		int totalPass=0;
		int totalFail=0;

			for(int i=0; i<tests; i++) {
			try {
		System.out.print("Question " + (i + 1) + " Title: ");
		String Title = (createString(random(0, 120))); // included extra 20 to  generate errors
		System.out.println(Title);
		System.out.println("Question " + (i + 1) + " Title Length: " + Title.length() + " characters");
		Question.validateTitle(Title);
		System.out.print("Question " + (i + 1) + " Body: ");
		String Body = (createString(random(0, 120)));
		System.out.println(Body);
		System.out.println("Question " + (i + 1) + " Body Length: " + Body.length() + " characters");
		Question.validateTitle(Body);
		totalPass++;
		System.out.println("Question " + (i + 1) + " Success!");
		System.out.println();
		}

		catch(Exception e) {
			System.out.println("Question " + (i + 1) + " Fail! " + e.getMessage());
			System.out.println();
			totalFail++;
			}
		}
				

		 // Formatted stats output with fixed-width fields and space padding
			System.out.println("_____________________________________________________________________________");
	    stats = String.format(
	        "\nResults for Questions Test: | Passes: %-5d | Fails: %-5d | Tests Run: %-5d |",
	        totalPass, totalFail, tests
	    );
	}
	

	// Tests Answer Content section and records results.
	public static void testAnswer(int tests) {
	    // Requirements:
	    // Answer: Cannot exceed 500 characters, must be at least 5 characters.
	    // Other inputs are hard coded and/or already validated.
		int totalPass=0;
		int totalFail=0;

			for(int i=0; i<tests; i++) {
			try {
		System.out.print("Answer " + (i + 1) + " Content: ");		
		String Ans = (createString(random(0, 600))); // included extra 100 to  generate errors
		System.out.println(Ans);
		System.out.println("Answer " + (i + 1) + " Content Length: " + Ans.length() + " characters");
		Answer.validateContent(Ans);
		totalPass++;
		System.out.println("Answer " + (i + 1) + " Success!");
		System.out.println();
		}

		catch(Exception e) {
			System.out.println("Answer " + (i + 1) + " Fail! " + e.getMessage());
			System.out.println();
			totalFail++;
			}
		}

		 // Formatted stats output with fixed-width fields and space padding
	    stats = String.format(
	        "\nResults for Questions Test: | Passes: %-5d | Fails: %-5d | Tests Run: %-5d |",
	        totalPass, totalFail, tests
	    );
	}
	
	
	
	// main method to run Tests
	public static void main(String[] args) {
		/************** Test cases semi-automation report header **************/
		System.out.println("______________________________________");
		System.out.println("\nTesting Automation");

		/************** Start of the test cases **************/
		testQuestion(5);
		System.out.println(getStats());
		System.out.println("_____________________________________________________________________________");
		System.out.println();
		testAnswer(5);
		System.out.println("_____________________________________________________________________________");
		System.out.println(getStats());	
		/************** End of the test cases **************/
		
		/************** Test cases semi-automation report footer **************/
		System.out.println("_____________________________________________________________________________");
		System.out.println();
		System.out.println("Testing complete");
		System.out.println();
	}
}
