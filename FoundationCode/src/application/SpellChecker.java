package application;


import java.util.*;


/**

 * Enhanced SpellChecker with double HashMap for better spell checking and auto-correction.

 * Uses a dictionary + common misspellings approach with edit distance calculations.

 */

public class SpellChecker {

    

    // Primary dictionary of correct words

    private static final Set<String> DICTIONARY = new HashSet<>(Arrays.asList(

        // Common words

        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with",

        "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",

        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up", "out", "if",

        "about", "who", "get", "which", "go", "me", "when", "make", "can", "like", "time", "no", "just", "him",

        "know", "take", "people", "into", "year", "your", "good", "some", "could", "them", "see", "other", "than",

        "then", "now", "look", "only", "come", "its", "over", "think", "also", "back", "after", "use", "two",

        "how", "our", "work", "first", "well", "way", "even", "new", "want", "because", "any", "these", "give",

        "day", "most", "us", "is", "was", "are", "been", "has", "had", "were", "said", "did", "having", "may",

        

        // Programming/CS related words

        "code", "program", "java", "class", "method", "function", "variable", "array", "list", "loop", "if",

        "else", "while", "for", "return", "string", "int", "boolean", "void", "public", "private", "static",

        "object", "inheritance", "polymorphism", "encapsulation", "interface", "abstract", "exception", "try",

        "catch", "throw", "import", "package", "extends", "implements", "new", "null", "true", "false",

        "algorithm", "data", "structure", "linked", "binary", "tree", "hash", "table", "recursion", "iteration",

        "sort", "search", "complexity", "big", "notation", "stack", "queue", "graph", "node", "pointer",

        "memory", "heap", "compile", "run", "debug", "error", "syntax", "semantic", "logic", "bug", "fix",

        "test", "output", "input", "scanner", "system", "print", "println", "main", "args", "constructor",

        "getter", "setter", "override", "overload", "parameter", "argument", "scope", "access", "modifier",

        "question", "answer", "help", "please", "thanks", "understand", "explain", "example", "problem",

        "solution", "issue", "working", "trying", "need", "want", "does", "doesn't", "works", "doesn't",

        "getting", "error", "cannot", "unable", "confused", "stuck", "appreciate", "feedback", "review"

    ));

    

    // Common misspellings mapped to correct spellings

    private static final Map<String, String> COMMON_MISSPELLINGS = new HashMap<>();

    static {

        // Basic typos

        COMMON_MISSPELLINGS.put("recieve", "receive");

        COMMON_MISSPELLINGS.put("occured", "occurred");

        COMMON_MISSPELLINGS.put("seperate", "separate");

        COMMON_MISSPELLINGS.put("definately", "definitely");

        COMMON_MISSPELLINGS.put("teh", "the");

        COMMON_MISSPELLINGS.put("thier", "their");

        COMMON_MISSPELLINGS.put("youre", "you're");

        

        // Contractions

        COMMON_MISSPELLINGS.put("cant", "can't");

        COMMON_MISSPELLINGS.put("dont", "don't");

        COMMON_MISSPELLINGS.put("wont", "won't");

        COMMON_MISSPELLINGS.put("shouldnt", "shouldn't");

        COMMON_MISSPELLINGS.put("couldnt", "couldn't");

        COMMON_MISSPELLINGS.put("wouldnt", "wouldn't");

        COMMON_MISSPELLINGS.put("hasnt", "hasn't");

        COMMON_MISSPELLINGS.put("havent", "haven't");

        COMMON_MISSPELLINGS.put("wasnt", "wasn't");

        COMMON_MISSPELLINGS.put("werent", "weren't");

        COMMON_MISSPELLINGS.put("isnt", "isn't");

        COMMON_MISSPELLINGS.put("arent", "aren't");

        COMMON_MISSPELLINGS.put("didnt", "didn't");

        COMMON_MISSPELLINGS.put("doesnt", "doesn't");

        

        // Programming specific

        COMMON_MISSPELLINGS.put("methid", "method");

        COMMON_MISSPELLINGS.put("funciton", "function");

        COMMON_MISSPELLINGS.put("retrun", "return");

        COMMON_MISSPELLINGS.put("calss", "class");

        COMMON_MISSPELLINGS.put("prviate", "private");

        COMMON_MISSPELLINGS.put("publc", "public");

        COMMON_MISSPELLINGS.put("strng", "string");

        COMMON_MISSPELLINGS.put("intger", "integer");

        COMMON_MISSPELLINGS.put("boolean", "boolean");

        COMMON_MISSPELLINGS.put("algoritm", "algorithm");

        COMMON_MISSPELLINGS.put("recusion", "recursion");

        COMMON_MISSPELLINGS.put("iteraton", "iteration");

        

        // Common question words

        COMMON_MISSPELLINGS.put("questin", "question");

        COMMON_MISSPELLINGS.put("anwser", "answer");

        COMMON_MISSPELLINGS.put("hlep", "help");

        COMMON_MISSPELLINGS.put("probelm", "problem");

        COMMON_MISSPELLINGS.put("soultion", "solution");

        COMMON_MISSPELLINGS.put("expain", "explain");

        COMMON_MISSPELLINGS.put("understnd", "understand");

    }

    

    // Reverse mapping: correct word -> list of common misspellings

    private static final Map<String, List<String>> REVERSE_MISSPELLINGS = new HashMap<>();

    static {

        for (Map.Entry<String, String> entry : COMMON_MISSPELLINGS.entrySet()) {

            String misspelling = entry.getKey();

            String correct = entry.getValue();

            REVERSE_MISSPELLINGS.computeIfAbsent(correct, k -> new ArrayList<>()).add(misspelling);

        }

    }

    

    /**

     * Result of spell/grammar check

     */

    public static class ValidationResult {

        private boolean isValid;

        private List<String> errors;

        private List<String> warnings;

        private String correctedText;

        private Map<String, String> corrections;

        

        public ValidationResult() {

            this.isValid = true;

            this.errors = new ArrayList<>();

            this.warnings = new ArrayList<>();

            this.correctedText = "";

            this.corrections = new HashMap<>();

        }

        

        public boolean isValid() { return isValid; }

        public void setValid(boolean valid) { this.isValid = valid; }

        public List<String> getErrors() { return errors; }

        public List<String> getWarnings() { return warnings; }

        public String getCorrectedText() { return correctedText; }

        public void setCorrectedText(String text) { this.correctedText = text; }

        public Map<String, String> getCorrections() { return corrections; }

        

        public void addError(String error) {

            errors.add(error);

            isValid = false;

        }

        

        public void addWarning(String warning) {

            warnings.add(warning);

        }

        

        public void addCorrection(String original, String corrected) {

            corrections.put(original, corrected);

        }

        

        public boolean hasIssues() {

            return !errors.isEmpty() || !warnings.isEmpty();

        }

        

        public String getSummary() {

            StringBuilder sb = new StringBuilder();

            if (!errors.isEmpty()) {

                sb.append("ERRORS:\n");

                for (String error : errors) {

                    sb.append("  • ").append(error).append("\n");

                }

            }

            if (!warnings.isEmpty()) {

                sb.append("WARNINGS:\n");

                for (String warning : warnings) {

                    sb.append("  • ").append(warning).append("\n");

                }

            }

            if (!corrections.isEmpty()) {

                sb.append("AUTO-CORRECTIONS:\n");

                for (Map.Entry<String, String> entry : corrections.entrySet()) {

                    sb.append("  • ").append(entry.getKey()).append(" → ").append(entry.getValue()).append("\n");

                }

            }

            return sb.toString();

        }

    }

    

    /**

     * Validates text for spelling and basic grammar

     */

    public static ValidationResult validateText(String text) {

        ValidationResult result = new ValidationResult();

        

        if (text == null || text.trim().isEmpty()) {

            result.addError("Text cannot be empty");

            return result;

        }

        

        String corrected = text;

        

        // Check for basic grammar issues

        corrected = checkBasicGrammar(corrected, result);

        

        // Check spelling with auto-correction

        corrected = checkSpelling(corrected, result);

        

        // Check for excessive punctuation

        checkPunctuation(corrected, result);

        

        // Check for all caps (yelling)

        checkAllCaps(corrected, result);

        

        // Check for repeated words

        checkRepeatedWords(corrected, result);

        

        result.setCorrectedText(corrected);

        

        return result;

    }

    

    /**

     * Checks for basic grammar issues

     */

    private static String checkBasicGrammar(String text, ValidationResult result) {

        String corrected = text;

        

        // Check if starts with lowercase (should start with capital)

        if (text.length() > 0 && Character.isLowerCase(text.charAt(0))) {

            corrected = Character.toUpperCase(corrected.charAt(0)) + corrected.substring(1);

            result.addCorrection("lowercase start", "Capitalized first letter");

        }

        

        // Check for missing question mark on questions

        String lowerText = text.toLowerCase().trim();

        if ((lowerText.startsWith("what") || lowerText.startsWith("how") || 

            lowerText.startsWith("why") || lowerText.startsWith("when") ||

            lowerText.startsWith("where") || lowerText.startsWith("who") ||

            lowerText.startsWith("is") || lowerText.startsWith("are") ||

            lowerText.startsWith("can") || lowerText.startsWith("could") ||

            lowerText.startsWith("would") || lowerText.startsWith("should")) &&

            !text.trim().endsWith("?")) {

            result.addWarning("Question should end with a question mark (?)");

        }

        

        // Check for double spaces

        if (corrected.contains("  ")) {

            result.addWarning("Remove extra spaces between words");

            corrected = corrected.replaceAll("\\s+", " ");

        }

        

        // Check for space before punctuation

        corrected = corrected.replaceAll("\\s+([.,!?;:])", "$1");

        

        return corrected;

    }

    

    /**

     * Enhanced spell checking with edit distance for suggestions

     */

    private static String checkSpelling(String text, ValidationResult result) {

        String corrected = text;

        String[] words = text.split("\\b");

        

        for (int i = 0; i < words.length; i++) {

            String word = words[i];

            if (word.isEmpty() || word.length() <= 1 || !word.matches(".*[a-zA-Z].*")) {

                continue;

            }

            

            String lowerWord = word.toLowerCase();

            

            // Check common misspellings first (exact match)

            if (COMMON_MISSPELLINGS.containsKey(lowerWord)) {

                String correction = COMMON_MISSPELLINGS.get(lowerWord);

                words[i] = preserveCase(word, correction);

                result.addCorrection(word, words[i]);

                continue;

            }

            

            // Check dictionary

            if (DICTIONARY.contains(lowerWord)) {

                continue;

            }

            

            // Skip if it looks like a technical term

            if (looksLikeTechnicalTerm(word)) {

                continue;

            }

            

            // Skip proper nouns (capitalized in middle of sentence)

            if (i > 0 && Character.isUpperCase(word.charAt(0))) {

                continue;

            }

            

            // Try to find close matches using edit distance

            String suggestion = findClosestMatch(lowerWord);

            if (suggestion != null) {

                String correctedWord = preserveCase(word, suggestion);

                words[i] = correctedWord;

                result.addCorrection(word, correctedWord);

            } else {

                result.addWarning("Possible misspelling: '" + word + "'");

            }

        }

        

        // Reconstruct the text

        corrected = String.join("", words);

        

        return corrected;

    }

    

    /**

     * Finds the closest matching word using edit distance

     */

    private static String findClosestMatch(String word) {

        int minDistance = Integer.MAX_VALUE;

        String bestMatch = null;

        

        // Only check if word is reasonably long

        if (word.length() < 3) {

            return null;

        }

        

        // Check dictionary words

        for (String dictWord : DICTIONARY) {

            int distance = editDistance(word, dictWord);

            if (distance < minDistance && distance <= 2) { // Allow up to 2 edits

                minDistance = distance;

                bestMatch = dictWord;

            }

        }

        

        // Check if a common misspelling correction is better

        for (Map.Entry<String, String> entry : COMMON_MISSPELLINGS.entrySet()) {

            int distance = editDistance(word, entry.getKey());

            if (distance < minDistance && distance <= 1) { // Stricter for known misspellings

                minDistance = distance;

                bestMatch = entry.getValue();

            }

        }

        

        return bestMatch;

    }

    

    /**

     * Calculates Levenshtein edit distance between two strings

     */

    private static int editDistance(String s1, String s2) {

        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        

        for (int i = 0; i <= s1.length(); i++) {

            dp[i][0] = i;

        }

        

        for (int j = 0; j <= s2.length(); j++) {

            dp[0][j] = j;

        }

        

        for (int i = 1; i <= s1.length(); i++) {

            for (int j = 1; j <= s2.length(); j++) {

                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {

                    dp[i][j] = dp[i - 1][j - 1];

                } else {

                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], 

                                   Math.min(dp[i - 1][j], dp[i][j - 1]));

                }

            }

        }

        

        return dp[s1.length()][s2.length()];

    }

    

    /**

     * Preserves the case pattern of the original word

     */

    private static String preserveCase(String original, String corrected) {

        if (original.isEmpty() || corrected.isEmpty()) {

            return corrected;

        }

        

        // All uppercase

        if (original.equals(original.toUpperCase())) {

            return corrected.toUpperCase();

        }

        

        // First letter uppercase

        if (Character.isUpperCase(original.charAt(0))) {

            return Character.toUpperCase(corrected.charAt(0)) + corrected.substring(1).toLowerCase();

        }

        

        // All lowercase

        return corrected.toLowerCase();

    }

    

    /**

     * Checks if a word looks like a technical term

     */

    private static boolean looksLikeTechnicalTerm(String word) {

        // Check for camelCase

        if (word.matches(".*[a-z][A-Z].*")) return true;

        

        // Check for mixed letters and numbers

        if (word.matches(".*[a-zA-Z].*\\d.*") || word.matches(".*\\d.*[a-zA-Z].*")) return true;

        

        // Check for common technical patterns

        if (word.contains("_") || word.contains("-")) return true;

        

        return false;

    }

    

    /**

     * Checks for excessive punctuation

     */

    private static void checkPunctuation(String text, ValidationResult result) {

        if (text.contains("!!")) {

            result.addWarning("Avoid excessive exclamation marks");

        }

        

        if (text.contains("??")) {

            result.addWarning("Avoid multiple question marks");

        }

        

        if (text.matches(".*\\.{4,}.*")) {

            result.addWarning("Use proper ellipsis (...) instead of multiple periods");

        }

    }

    

    /**

     * Checks for all caps text (considered shouting)

     */

    private static void checkAllCaps(String text, ValidationResult result) {

        String[] words = text.split("\\s+");

        int capsWords = 0;

        int totalWords = 0;

        

        for (String word : words) {

            if (word.length() > 2) {

                totalWords++;

                if (word.equals(word.toUpperCase()) && word.matches(".*[A-Z].*")) {

                    capsWords++;

                }

            }

        }

        

        if (totalWords > 0 && (double) capsWords / totalWords > 0.5) {

            result.addWarning("Avoid using ALL CAPS - it's considered shouting");

        }

    }

    

    /**

     * Checks for repeated words

     */

    private static void checkRepeatedWords(String text, ValidationResult result) {

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i < words.length - 1; i++) {

            if (words[i].equals(words[i + 1]) && words[i].length() > 2) {

                result.addWarning("Repeated word detected: '" + words[i] + "'");

            }

        }

    }

    

    /**

     * Quick check for minimal validation

     */

    public static boolean isValidBasic(String text) {

        if (text == null || text.trim().isEmpty()) return false;

        if (text.length() < 5) return false;

        if (!text.matches(".*[a-zA-Z]+.*")) return false;

        return true;

    }

    

    /**

     * Auto-corrects text without validation warnings

     */

    public static String autoCorrect(String text) {

        if (text == null || text.trim().isEmpty()) {

            return text;

        }

        

        ValidationResult result = validateText(text);

        return result.getCorrectedText();

    }

}