import java.util.Locale;

public class Course {
    private final String courseCode;
    private final String courseTitle;
    private final int creditUnits;

    public Course(String courseCode, String courseTitle, int creditUnits) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be null or blank.");
        }
        if (courseTitle == null || courseTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be null or blank.");
        }
        if (courseCode.contains("|") || courseCode.contains("\n") || courseCode.contains("\r")) {
            throw new IllegalArgumentException("Course code cannot contain the record delimiter or line breaks.");
        }
        if (courseTitle.contains("|") || courseTitle.contains("\n") || courseTitle.contains("\r")) {
            throw new IllegalArgumentException("Course title cannot contain the record delimiter or line breaks.");
        }
        if (creditUnits <= 0) {
            throw new IllegalArgumentException("Credit units must be positive.");
        }

        this.courseCode = courseCode.trim().toUpperCase(Locale.ROOT);
        this.courseTitle = courseTitle.trim();
        this.creditUnits = creditUnits;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public int getCreditUnits() {
        return creditUnits;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseTitle + " (" + creditUnits + " units)";
    }
}