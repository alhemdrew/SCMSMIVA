import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CourseManager courseManager = new CourseManager();
            MainFrame mainFrame = new MainFrame(courseManager);
            mainFrame.setVisible(true);
        });
    }
}