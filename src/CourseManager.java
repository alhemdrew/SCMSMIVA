import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CourseManager {
    private static final Path COURSE_FILE = Paths.get("data", "courses.txt");
    private final List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }

        for (Course existingCourse : courses) {
            if (existingCourse.getCourseCode().equals(course.getCourseCode())) {
                throw new IllegalArgumentException(
                        "A course with code " + course.getCourseCode() + " already exists.");
            }
        }

        courses.add(course);
    }

    public List<Course> getCourses() {
        return new ArrayList<>(courses);
    }

    public Course findCourseByCode(String courseCode) {
        String normalizedCode = normalizeCourseCode(courseCode);
        for (Course course : courses) {
            if (course.getCourseCode().equals(normalizedCode)) {
                return course;
            }
        }
        return null;
    }

    public int calculateTotalCreditUnits() {
        return calculateTotalCreditUnits(courses, 0);
    }

    public void saveCourses() throws IOException {
        Path parentDirectory = COURSE_FILE.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        Path temporaryFile = Files.createTempFile(parentDirectory, "courses-", ".tmp");
        boolean saved = false;
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                for (Course course : courses) {
                    writer.write(course.getCourseCode());
                    writer.write('|');
                    writer.write(course.getCourseTitle());
                    writer.write('|');
                    writer.write(Integer.toString(course.getCreditUnits()));
                    writer.newLine();
                }
            }

            try {
                Files.move(temporaryFile, COURSE_FILE, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, COURSE_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            saved = true;
        } finally {
            if (!saved) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }

    public LoadResult loadCourses() throws IOException {
        List<Course> loadedCourses = new ArrayList<>();
        int rejectedRecords = 0;

        try (BufferedReader reader = Files.newBufferedReader(COURSE_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\\|", -1);
                if (fields.length != 3) {
                    rejectedRecords++;
                    continue;
                }

                try {
                    Course course = new Course(fields[0], fields[1], Integer.parseInt(fields[2].trim()));
                    if (containsCourseCode(loadedCourses, course.getCourseCode())) {
                        rejectedRecords++;
                    } else {
                        loadedCourses.add(course);
                    }
                } catch (IllegalArgumentException exception) {
                    rejectedRecords++;
                }
            }
        }

        // Replace the current list only after the entire file has been read.
        courses.clear();
        courses.addAll(loadedCourses);
        return new LoadResult(loadedCourses.size(), rejectedRecords);
    }

    private boolean containsCourseCode(List<Course> courseList, String courseCode) {
        for (Course course : courseList) {
            if (course.getCourseCode().equals(courseCode)) {
                return true;
            }
        }
        return false;
    }

    private int calculateTotalCreditUnits(List<Course> courseList, int index) {
        if (index >= courseList.size()) {
            return 0;
        }

        // Add this course's units to the total calculated for the remaining courses.
        return courseList.get(index).getCreditUnits()
                + calculateTotalCreditUnits(courseList, index + 1);
    }

    private String normalizeCourseCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be null or blank.");
        }
        return courseCode.trim().toUpperCase(Locale.ROOT);
    }

    public static class LoadResult {
        private final int loadedCount;
        private final int rejectedCount;

        private LoadResult(int loadedCount, int rejectedCount) {
            this.loadedCount = loadedCount;
            this.rejectedCount = rejectedCount;
        }

        public int getLoadedCount() {
            return loadedCount;
        }

        public int getRejectedCount() {
            return rejectedCount;
        }
    }
}