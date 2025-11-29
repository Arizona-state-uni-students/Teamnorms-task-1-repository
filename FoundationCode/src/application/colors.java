package application;
public class colors {

    // --- Role Colors (Primary/Default Button Color) ---

    // Admin (Red)
    public static final String ADMIN_PRIMARY = "-fx-background-color: #D32F2F;";

    // Instructor (Purple)
    public static final String INSTRUCTOR_PRIMARY = "-fx-background-color: #7B1FA2;";

    // Staff (Yellow)
    public static final String STAFF_PRIMARY = "-fx-background-color: #FBC02D; -fx-text-fill: #000;";

    // Reviewer (Green)
    public static final String REVIEWER_PRIMARY = "-fx-background-color: #388E3C;";

    // Student (Blue)
    public static final String STUDENT_PRIMARY = "-fx-background-color: #0099ff;";

    // User (Gray)
    public static final String USER_PRIMARY = "-fx-background-color: #9E9E9E;";

    // --- Accent Colors (Lighter for Backgrounds/Darker for Text/Hover) ---

    // Admin Accents
    public static final String ADMIN_ACCENT_DARK = "#B71C1C"; // Deeper Red (Hover)
    public static final String ADMIN_ACCENT_LIGHT = "#FFCDD2"; // Very Light Red (Backgrounds)

    // Instructor Accents
    public static final String INSTRUCTOR_ACCENT_DARK = "#4A148C"; // Darkest Purple (Text)
    public static final String INSTRUCTOR_ACCENT_LIGHT = "#E1BEE7"; // Very Light Purple (Backgrounds)

    // Staff Accents
    public static final String STAFF_ACCENT_DARK = "#F57F17"; // Dark Mustard (Text/Borders)
    public static final String STAFF_ACCENT_LIGHT = "#FFF9C4"; // Pale Yellow (Backgrounds)

    // Reviewer Accents
    public static final String REVIEWER_ACCENT_DARK = "#1B5E20"; // Dark Forest Green (Text/Borders)
    public static final String REVIEWER_ACCENT_LIGHT = "#C8E6C9"; // Pale Green (Backgrounds)

    // Student Accents
    public static final String STUDENT_ACCENT_DARK = "#0D47A1"; // Navy Blue (Text/Borders)
    public static final String STUDENT_ACCENT_LIGHT = "#BBDEFB"; // Light Sky Blue (Backgrounds)

    // User Accents
    public static final String USER_ACCENT_DARK = "#616161"; // Dark Gray (Text/Borders)
    public static final String USER_ACCENT_LIGHT = "#F5F5F5"; // Off-White Gray (Backgrounds)

    // General Neutral Color (e.g., for disabled states or general UI)
    public static final String WHITE = "#FFFFFF";
    public static final String BLACK = "#212121";
    
    public static final String BASIC = "-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #666; -fx-text-fill: white;";
    public static final String GO = "-fx-font-size: 14px; -fx-padding: 5 20; -fx-background-color: #4CAF50; -fx-text-fill: white;";
}