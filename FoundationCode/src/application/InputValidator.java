package application;

import java.util.ArrayList;
import java.util.List;

/**
 * InputValidator provides comprehensive validation for user inputs in the Q&A system.
 * Combines length checks, spell checking, and grammar validation.
 */
public class InputValidator {
    
    /**
     * Validation result with details
     */
    public static class ValidationReport {
        private boolean canSubmit;
        private List<String> blockingErrors;
        private List<String> warnings;
        private String correctedText;
        
        public ValidationReport() {
            this.canSubmit = true;
            this.blockingErrors = new ArrayList<>();
            this.warnings = new ArrayList<>();
            this.correctedText = "";
        }
        
        /**
         * Gets canSumbit
         * 
         * @return canSubmit
         */
        public boolean canSubmit() { return canSubmit; }
        
        /**
         * Sets canSubmit
         * 
         * @param canSubmit Boolean to set canSumbit to.
         */
        public void setCanSubmit(boolean canSubmit) { this.canSubmit = canSubmit; }
        
        /**
         * Gets blockingErrors
         * 
         * @return List of errors
         */
        public List<String> getBlockingErrors() { return blockingErrors; }
        
        /**
         * Gets warnings 
         * 
         * @return List of warnings
         */
        public List<String> getWarnings() { return warnings; }
        
        /**
         * Gets correctedText
         * 
         * @return correctedText
         */
        public String getCorrectedText() { return correctedText; }
        
        /**
         * Sets correctedText
         * 
         * @param text String to set correctedText to.
         */
        public void setCorrectedText(String text) { this.correctedText = text; }
        
        /**
         * Adds a blocking error
         * 
         * @param error String of error
         */
        public void addBlockingError(String error) {
            blockingErrors.add(error);
            canSubmit = false;
        }
        
        /**
         * Adds a warning
         * 
         * @param warning String of warning
         */
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        
        /**
         * Returns a boolean based on if there are issues.
         * 
         * @return True or false based on if there are issues or not.
         */
        public boolean hasIssues() {
            return !blockingErrors.isEmpty() || !warnings.isEmpty();
        }
        
        /**
         * Gets a report of any blocking errors and warnings.
         * 
         * @return String of the report.
         */
        public String getFullReport() {
            StringBuilder sb = new StringBuilder();
            
            if (!blockingErrors.isEmpty()) {
                sb.append("❌ BLOCKING ERRORS (must fix):\n");
                for (String error : blockingErrors) {
                    sb.append("  • ").append(error).append("\n");
                }
                sb.append("\n");
            }
            
            if (!warnings.isEmpty()) {
                sb.append("⚠️ WARNINGS (suggestions):\n");
                for (String warning : warnings) {
                    sb.append("  • ").append(warning).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Validates a question title.
     * 
     * @param title String to validate
     * @return Validation report 
     */
    public static ValidationReport validateQuestionTitle(String title) {
        ValidationReport report = new ValidationReport();
        
        // Check null/empty
        if (title == null || title.trim().isEmpty()) {
            report.addBlockingError("Title cannot be empty");
            return report;
        }
        
        String trimmed = title.trim();
        
        // Check length
        if (trimmed.length() < Question.TITLE_MIN_LENGTH) {
            report.addBlockingError("Title must be at least " + Question.TITLE_MIN_LENGTH + " characters (currently " + trimmed.length() + ")");
        }
        
        if (trimmed.length() > Question.TITLE_MAX_LENGTH) {
            report.addBlockingError("Title cannot exceed " + Question.TITLE_MAX_LENGTH + " characters (currently " + trimmed.length() + ")");
        }
        
        // Basic spell/grammar check
        if (trimmed.length() >= Question.TITLE_MIN_LENGTH && trimmed.length() <= Question.TITLE_MAX_LENGTH) {
            SpellChecker.ValidationResult spellResult = SpellChecker.validateText(trimmed);
            report.setCorrectedText(spellResult.getCorrectedText());
            
            for (String error : spellResult.getErrors()) {
                report.addWarning(error);
            }
            
            for (String warning : spellResult.getWarnings()) {
                report.addWarning(warning);
            }
        }
        
        return report;
    }
    
    /**
     * Validates question content.
     * 
     * @param content String to validate
     * @return Validation report 
     */
    public static ValidationReport validateQuestionContent(String content) {
        ValidationReport report = new ValidationReport();
        
        // Check null/empty
        if (content == null || content.trim().isEmpty()) {
            report.addBlockingError("Content cannot be empty");
            return report;
        }
        
        String trimmed = content.trim();
        
        // Check length
        if (trimmed.length() < Question.CONTENT_MIN_LENGTH) {
            report.addBlockingError("Content must be at least " + Question.CONTENT_MIN_LENGTH + " characters (currently " + trimmed.length() + ")");
        }
        
        if (trimmed.length() > Question.CONTENT_MAX_LENGTH) {
            report.addBlockingError("Content cannot exceed " + Question.CONTENT_MAX_LENGTH + " characters (currently " + trimmed.length() + ")");
        }
        
        // Spell/grammar check
        if (trimmed.length() >= Question.CONTENT_MIN_LENGTH && trimmed.length() <= Question.CONTENT_MAX_LENGTH) {
            SpellChecker.ValidationResult spellResult = SpellChecker.validateText(trimmed);
            report.setCorrectedText(spellResult.getCorrectedText());
            
            for (String error : spellResult.getErrors()) {
                report.addWarning(error);
            }
            
            for (String warning : spellResult.getWarnings()) {
                report.addWarning(warning);
            }
        }
        
        return report;
    }
    
    /**
     * Validates answer content.
     * 
     * @param content String to validate
     * @return Validation report 
     */
    public static ValidationReport validateAnswerContent(String content) {
        ValidationReport report = new ValidationReport();
        
        // Check null/empty
        if (content == null || content.trim().isEmpty()) {
            report.addBlockingError("Answer cannot be empty");
            return report;
        }
        
        String trimmed = content.trim();
        
        // Check length
        if (trimmed.length() < Answer.CONTENT_MIN_LENGTH) {
            report.addBlockingError("Answer must be at least " + Answer.CONTENT_MIN_LENGTH + " characters (currently " + trimmed.length() + ")");
        }
        
        if (trimmed.length() > Answer.CONTENT_MAX_LENGTH) {
            report.addBlockingError("Answer cannot exceed " + Answer.CONTENT_MAX_LENGTH + " characters (currently " + trimmed.length() + ")");
        }
        
        // Spell/grammar check
        if (trimmed.length() >= Answer.CONTENT_MIN_LENGTH && trimmed.length() <= Answer.CONTENT_MAX_LENGTH) {
            SpellChecker.ValidationResult spellResult = SpellChecker.validateText(trimmed);
            report.setCorrectedText(spellResult.getCorrectedText());
            
            for (String error : spellResult.getErrors()) {
                report.addWarning(error);
            }
            
            for (String warning : spellResult.getWarnings()) {
                report.addWarning(warning);
            }
        }
        
        return report;
    }
}