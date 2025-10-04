package application;

import java.util.*;

/**
 * SpellChecker provides basic spell checking and grammar validation.
 * Uses a simple dictionary approach with common misspellings and grammar patterns.
 */
public class SpellChecker {
    
    // Common dictionary words (expand as needed)
    private static final Set<String> DICTIONARY = new HashSet<>(Arrays.asList(
        // Common words
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with",
        "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up", "out", "if",
        "about", "who", "get", "which", "go", "me", "when", "make", "can", "like", "time", "no", "just", "him",
        "know", "take", "people", "into", "year", "your", "good", "some", "could", "them", "see", "other", "than",
        "then", "now", "look", "only", "come", "its", "over", "think", "also", "back", "after", "use", "two",
        "how", "our", "work", "first", "well", "way", "even", "new", "want", "because", "any", "these", "give",
        "day", "most", "us",
        
        // Programming/CS related words
        "code", "program", "java", "class", "method", "function", "variable", "array", "list", "loop", "if",
        "else", "while", "for", "return", "string", "int", "boolean", "void", "public", "private", "static",
        "object", "inheritance", "polymorphism", "encapsulation", "interface", "abstract", "exception", "try",
        "catch", "throw", "import", "package", "extends", "implements", "new", "null", "true", "false",
        "algorithm", "data", "structure", "linked", "binary", "tree", "hash", "table", "recursion", "iteration",
        "sort", "search", "complexity", "big", "notation", "stack", "queue", "graph", "node", "pointer",
        "memory", "heap", "compile", "run", "debug", "error", "syntax", "semantic", "logic", "bug", "fix",
        "test", "output", "input", "scanner", "system", "print", "println", "main", "args", "constructor",
        "getter", "setter", "override", "overload", "parameter", "argument", "scope", "access", "modifier"
    ));
    
    // Common misspellings map
    private static final Map<String, String> COMMON_MISSPELLINGS = new HashMap<>();
    static {
        COMMON_MISSPELLINGS.put("recieve", "receive");
        COMMON_MISSPELLINGS.put("occured", "occurred");
        COMMON_MISSPELLINGS.put("seperate", "separate");
        COMMON_MISSPELLINGS.put("definately", "definitely");
        COMMON_MISSPELLINGS.put("teh", "the");
        COMMON_MISSPELLINGS.put("thier", "their");
        COMMON_MISSPELLINGS.put("youre", "you're");
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
    }
    
    /**
     * Result of spell/grammar check
     */
    public static class ValidationResult {
        private boolean isValid;
        private List<String> errors;
        private List<String> warnings;
        private String correctedText;
        
        public ValidationResult() {
            this.isValid = true;
            this.errors = new ArrayList<>();
            this.warnings = new ArrayList<>();
            this.correctedText = "";
        }
        
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { this.isValid = valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
        public String getCorrectedText() { return correctedText; }
        public void setCorrectedText(String text) { this.correctedText = text; }
        
        public void addError(String error) {
            errors.add(error);
            isValid = false;
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
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
        corrected = checkBasicGrammar(text, result);
        
        // Check spelling
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
            result.addWarning("Sentence should start with a capital letter");
            corrected = Character.toUpperCase(corrected.charAt(0)) + corrected.substring(1);
        }
        
        // Check for missing question mark on questions
        if (text.toLowerCase().startsWith("what") || text.toLowerCase().startsWith("how") || 
            text.toLowerCase().startsWith("why") || text.toLowerCase().startsWith("when") ||
            text.toLowerCase().startsWith("where") || text.toLowerCase().startsWith("who") ||
            text.toLowerCase().startsWith("is") || text.toLowerCase().startsWith("are") ||
            text.toLowerCase().startsWith("can") || text.toLowerCase().startsWith("could") ||
            text.toLowerCase().startsWith("would") || text.toLowerCase().startsWith("should")) {
            if (!text.trim().endsWith("?")) {
                result.addWarning("Question should end with a question mark (?)");
            }
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
     * Checks spelling against dictionary and common misspellings
     */
    private static String checkSpelling(String text, ValidationResult result) {
        String corrected = text;
        String[] words = text.toLowerCase().split("\\W+");
        
        List<String> misspelledWords = new ArrayList<>();
        Map<String, String> corrections = new HashMap<>();
        
        for (String word : words) {
            if (word.isEmpty() || word.length() <= 1) continue;
            
            // Check if it's a number
            if (word.matches("\\d+")) continue;
            
            String lowerWord = word.toLowerCase();
            
            // Check common misspellings first
            if (COMMON_MISSPELLINGS.containsKey(lowerWord)) {
                String correction = COMMON_MISSPELLINGS.get(lowerWord);
                corrections.put(word, correction);
                misspelledWords.add(word + " → " + correction);
            }
            // Check dictionary
            else if (!DICTIONARY.contains(lowerWord)) {
                // Only flag as misspelled if it's not a technical term or proper noun
                if (!looksLikeTechnicalTerm(word) && !Character.isUpperCase(word.charAt(0))) {
                    result.addWarning("Possible misspelling: '" + word + "'");
                }
            }
        }
        
        // Apply corrections
        for (Map.Entry<String, String> entry : corrections.entrySet()) {
            corrected = corrected.replaceAll("(?i)\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        
        if (!misspelledWords.isEmpty()) {
            result.addWarning("Auto-corrected: " + String.join(", ", misspelledWords));
        }
        
        return corrected;
    }
    
    /**
     * Checks if a word looks like a technical term (camelCase, contains numbers, etc.)
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
        // Check for multiple exclamation marks
        if (text.contains("!!")) {
            result.addWarning("Avoid excessive exclamation marks");
        }
        
        // Check for multiple question marks
        if (text.contains("??")) {
            result.addWarning("Avoid multiple question marks");
        }
        
        // Check for excessive periods
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
            if (word.length() > 2) { // Ignore short words like "I"
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
        
        // Must contain at least some letters
        if (!text.matches(".*[a-zA-Z]+.*")) return false;
        
        return true;
    }
}