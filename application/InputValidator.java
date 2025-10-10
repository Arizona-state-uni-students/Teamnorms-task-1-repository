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
        
        public boolean canSubmit() { return canSubmit; }
        public void setCanSubmit(boolean canSubmit) { this.canSubmit = canSubmit; }
        public List<String> getBlockingErrors() { return blockingErrors; }
        public List<String> getWarnings() { return warnings; }
        public String getCorrectedText() { return correctedText; }
        public void setCorrectedText(String text) { this.correctedText = text; }
        
        public void addBlockingError(String error) {
            blockingErrors.add(error);
            canSubmit = false;
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean hasIssues() {
            return !blockingErrors.isEmpty() || !warnings.isEmpty();
        }
        
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
     * Validates a question title
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
     * Validates question content
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
     * Validates answer content
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