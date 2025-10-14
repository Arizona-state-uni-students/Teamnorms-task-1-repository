package databasePart1;

import java.util.Random;

public class dataTest {
	public static String stats;
	public dataTest() {
		// TODO Auto-generated constructor stub

	}
	
	public static String getStats() {
		return stats;
	}
	public static String createString(int length) {
		String string=""; //initialize blank string
		
		for(int i = 0; i<=length; i++) {
		char character = (char) random(32, 95); // get a random ascii character from the keyboard (between 32-126)
		string+=character;
		}
		
		return string;
	}
	private static int random(int min, int max) {
		Random random = new Random();
		int gen = random.nextInt(max - min + 1) + min;
		return gen;
	}
	public static void testQuestion(int tests) {
	    // Requirements:
	    // QuestionTitle: Must be at least 3 characters, max 60
	    // QuestionBody: Must be at least 55 characters, max 500, must contain a questionMark
	    // Other inputs are hardcoded and already validated
		int totalPass=0;
		int totalFail=0;
		int totalUploads=0;
		for(int i=0; i<tests; i++) {
		String Title = createString(random(0, 100));
		String Body = createString(random(0, 600));
		boolean titlePassed = false;
		boolean bodyPassed = false;

		if(Title.length()>=3&&Title.length()<=60) {
			//System.out.println("Title passed");
			totalPass++;
			titlePassed=true;
		}else {
			//System.out.println("Title failed"); 
			totalFail++;
			titlePassed=false;}
		
		if(Body.length()>=55&&Body.length()<=500&&Body.contains("?")) {
			//System.out.println("Body Passed");
			totalPass++;
			bodyPassed=true;
		}else {
			//System.out.println("Body Failed");
			totalFail++;
			bodyPassed=false;
		}
		
		if(titlePassed&&bodyPassed) {
			totalUploads++;
		}
		System.out.println("\nPassed:_"+(titlePassed&&bodyPassed)+"_ Title: "+Title + " Body: "+ Body+"\n  |_Title Length: "+Title.length()+" Body Length:"+Body.length()+" BodyContainsQuestion: "+Body.contains("?"));
		}
		 // Formatted stats output with fixed-width fields and space padding
	    stats = String.format(
	        " |\nResults for Questions Test: || Uploads: %-5d | Passes: %-5d | Fails: %-5d | Tests Run: %-5d",
	        totalUploads, totalPass, totalFail, tests
	    );
	    stats+="\n\tNote: Both questionTitle (3-60 chars) and questionBody (55-500 chars, containing \"?\") must pass to upload.";
		
	}
	
	public static void testAnswer(int tests) {
	    // Requirements:
	    // Answer: Cannot exceed 500 characters, must be atleast 2 characters.
	    // Other inputs are hardcoded and already validated.
		int totalPass=0;
		int totalFail=0;
		int totalUploads=0;
		boolean passed=false;
		for(int i=0; i<tests; i++) {
		String Answer = createString(random(0, 600));

		if(Answer.length()>=2&&Answer.length()<=500) {
			//System.out.println("Body Passed");
			totalPass++;
			totalUploads++;
			passed=true;
		}else {
			//System.out.println("Body Failed");
			totalFail++;
			passed=false;
		}
		System.out.println("\nPassed:_"+(passed)+"_ Answer: "+Answer+"\n  |_Answer Length: "+Answer.length());
		
		}
		 // Formatted stats output with fixed-width fields and space padding
	    stats = String.format(
	        " |\nResults for Answers Test: || Uploads: %-5d | Passes: %-5d | Fails: %-5d | Tests Run: %-5d",
	        totalUploads, totalPass, totalFail, tests
	    );
	    stats+="\n\tNote: Answer can only be up to 500 characters, and must be atleast two characters.";
		
	}

}
