import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MainFrame extends JFrame {
    private static final String APPLICATION_NAME = "Alhemdrew’s Course Management System";
    private static final String SUBTITLE = "Student Course Management | COS 201 — Programming I";
    private static final String DASHBOARD = "Dashboard";
    private static final String COURSES = "All Courses";
    private static final String ADD_COURSE = "Add Course";
    private static final String SEARCH = "Search Courses";

    private final CourseManager courseManager;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageDeck = new JPanel(pageLayout);
    private final List<JButton> navigationButtons = new ArrayList<>();
    private final List<JComponent> interactiveComponents = new ArrayList<>();

    private final DefaultTableModel dashboardModel = createCourseTableModel();
    private final DefaultTableModel coursesModel = createCourseTableModel();
    private final TableRowSorter<DefaultTableModel> coursesSorter = new TableRowSorter<>(coursesModel);

    private JLabel persistenceBadge;
    private JLabel actionMessage;
    private JLabel dashboardCourseCount;
    private JLabel dashboardUnitCount;
    private JLabel courseCount;
    private JLabel courseEmptyTitle;
    private JLabel courseEmptyDescription;
    private JLabel formFeedback;
    private JLabel searchFeedback;
    private JPanel dashboardEmptyState;
    private JPanel courseEmptyState;
    private JPanel dashboardList;
    private JPanel coursesList;
    private JTextField courseCodeField;
    private JTextField courseTitleField;
    private JTextField creditUnitsField;
    private JTextField courseFilterField;
    private JTextField searchField;
    private JPanel searchResultPanel;
    private String lastSearchCode = "";
    private String savedSnapshot = "";
    private String currentPage = DASHBOARD;
    private int rejectedFileRecords;
    private String persistenceError;
    private boolean startupComplete;
    private boolean busy;

    public MainFrame(CourseManager courseManager) {
        this.courseManager = courseManager;

        setTitle(APPLICATION_NAME);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1040, 690));
        setSize(1240, 820);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                requestApplicationClose();
            }
        });

        JPanel application = new JPanel(new BorderLayout());
        application.setBackground(AppTheme.CANVAS);
        application.add(createSidebar(), BorderLayout.WEST);
        application.add(createWorkspace(), BorderLayout.CENTER);
        setContentPane(application);

        pageDeck.add(createPageScrollPane(createDashboardPage()), DASHBOARD);
        pageDeck.add(createPageScrollPane(createCoursesPage()), COURSES);
        pageDeck.add(createPageScrollPane(createAddCoursePage()), ADD_COURSE);
        pageDeck.add(createPageScrollPane(createSearchPage()), SEARCH);
        pageLayout.show(pageDeck, DASHBOARD);
        updateNavigationSelection(DASHBOARD);
        refreshDataViews();
        initializeCourseData();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(AppTheme.NAVY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 17, 19, 17));
        sidebar.setPreferredSize(new Dimension(264, 700));

        JPanel identity = new JPanel(new BorderLayout(12, 0));
        identity.setOpaque(false);
        identity.setAlignmentX(Component.LEFT_ALIGNMENT);
        identity.add(new BrandMark(), BorderLayout.WEST);
        JLabel brandName = new JLabel("<html><div style='width:155px'>Alhemdrew’s Course<br>Management System</div></html>");
        brandName.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        brandName.setForeground(Color.WHITE);
        identity.add(brandName, BorderLayout.CENTER);
        identity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        sidebar.add(identity);

        JLabel courseLabel = new JLabel("COS 201  /  PROGRAMMING I");
        courseLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        courseLabel.setForeground(new Color(111, 201, 190));
        courseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(Box.createVerticalStrut(32));
        sidebar.add(courseLabel);
        sidebar.add(Box.createVerticalStrut(13));

        addNavigation(sidebar, DASHBOARD);
        addNavigation(sidebar, COURSES);
        addNavigation(sidebar, ADD_COURSE);
        addNavigation(sidebar, SEARCH);

        sidebar.add(Box.createVerticalGlue());
        JPanel sidebarFooter = new JPanel();
        sidebarFooter.setLayout(new BoxLayout(sidebarFooter, BoxLayout.Y_AXIS));
        sidebarFooter.setOpaque(false);
        sidebarFooter.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel footerRule = new JLabel("________________________________");
        footerRule.setForeground(new Color(67, 87, 99));
        JLabel footerCourse = new JLabel("COS 201 ACADEMIC PROJECT");
        footerCourse.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        footerCourse.setForeground(new Color(193, 207, 212));
        JLabel footerTerm = new JLabel("Student Course Management");
        footerTerm.setFont(AppTheme.SMALL);
        footerTerm.setForeground(new Color(142, 161, 170));
        sidebarFooter.add(footerRule);
        sidebarFooter.add(Box.createVerticalStrut(8));
        sidebarFooter.add(footerCourse);
        sidebarFooter.add(Box.createVerticalStrut(5));
        sidebarFooter.add(footerTerm);
        sidebar.add(sidebarFooter);

        return sidebar;
    }

    private void addNavigation(JPanel sidebar, String page) {
        JButton button = AppTheme.navigationButton(page);
        button.setPreferredSize(new Dimension(226, 43));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(event -> showPage(page));
        navigationButtons.add(button);
        interactiveComponents.add(button);
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(5));
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(AppTheme.CANVAS);
        workspace.add(createTopBar(), BorderLayout.NORTH);

        pageDeck.setBackground(AppTheme.CANVAS);
        workspace.add(pageDeck, BorderLayout.CENTER);
        workspace.add(createProjectFooter(), BorderLayout.SOUTH);
        return workspace;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(18, 0));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(13, 26, 12, 24)));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel name = new JLabel(APPLICATION_NAME);
        name.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        name.setForeground(AppTheme.TEXT);
        JLabel subtitle = new JLabel(SUBTITLE);
        subtitle.setFont(AppTheme.SMALL);
        subtitle.setForeground(AppTheme.MUTED);
        titleBlock.add(name);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton loadButton = AppTheme.secondaryButton("Load");
        JButton saveButton = AppTheme.primaryButton("Save");
        loadButton.addActionListener(event -> loadCourses());
        saveButton.addActionListener(event -> saveCourses());
        actions.add(loadButton);
        actions.add(saveButton);
        interactiveComponents.add(loadButton);
        interactiveComponents.add(saveButton);
        topBar.add(actions, BorderLayout.EAST);

        JPanel statusLine = new JPanel(new BorderLayout(14, 0));
        statusLine.setOpaque(false);
        persistenceBadge = new JLabel("CHECKING  Reading course data");
        persistenceBadge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        persistenceBadge.setForeground(AppTheme.MUTED);
        actionMessage = new JLabel(" ");
        actionMessage.setFont(AppTheme.SMALL);
        actionMessage.setForeground(AppTheme.MUTED);
        statusLine.add(persistenceBadge, BorderLayout.WEST);
        statusLine.add(actionMessage, BorderLayout.CENTER);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleBlock, BorderLayout.NORTH);
        header.add(statusLine, BorderLayout.SOUTH);
        topBar.add(header, BorderLayout.WEST);
        return topBar;
    }

    private JPanel createProjectFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(241, 244, 242));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
            BorderFactory.createEmptyBorder(6, 22, 6, 22)));
        String description = "Alhemdrew’s Course Management System is a Java-based desktop application developed as a COS 201 — Programming I academic project. "
                + "It helps students organize semester courses by recording course codes, titles, and credit units; viewing and searching course records; "
                + "calculating total credit units through recursion; and saving or loading course information using file handling. "
                + "The project demonstrates object-oriented programming, classes and methods, collections, decision-making, loops, recursion, input validation, exception handling, and persistent file storage through a practical student-focused application.";
        javax.swing.JTextArea footerText = new javax.swing.JTextArea(description);
        footerText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        footerText.setForeground(AppTheme.MUTED);
        footerText.setLineWrap(true);
        footerText.setWrapStyleWord(true);
        footerText.setEditable(false);
        footerText.setFocusable(false);
        footerText.setOpaque(false);
        footerText.setRows(4);
        footer.add(footerText, BorderLayout.CENTER);
        return footer;
    }

    private JPanel createDashboardPage() {
        JPanel page = createPage();
        addHeading(page, "Welcome to your course workspace",
                "Keep your semester course list organized and your credit load in view.");

        JPanel metrics = new JPanel(new GridLayout(1, 3, 13, 0));
        metrics.setOpaque(false);
        metrics.setAlignmentX(Component.LEFT_ALIGNMENT);
        dashboardCourseCount = new JLabel("0");
        dashboardUnitCount = new JLabel("0");
        metrics.setLayout(new GridLayout(1, 2, 13, 0));
        metrics.add(createMetricCard("REGISTERED COURSES", dashboardCourseCount,
            "Courses in your current collection"));
        metrics.add(createMetricCard("TOTAL CREDIT UNITS", dashboardUnitCount,
                "Calculated from the current course list"));
        page.add(metrics);
        page.add(Box.createVerticalStrut(18));

        JPanel actionRow = new JPanel(new BorderLayout());
        actionRow.setOpaque(false);
        JLabel actionHint = bodyLabel("What would you like to work on?");
        JButton addAction = AppTheme.primaryButton("Add New Course");
        addAction.addActionListener(event -> showPage(ADD_COURSE));
        interactiveComponents.add(addAction);
        actionRow.add(actionHint, BorderLayout.WEST);
        actionRow.add(addAction, BorderLayout.EAST);
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(actionRow);
        page.add(Box.createVerticalStrut(20));

        JPanel currentSection = createSection();
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        sectionHeader.add(sectionTitle("Current course collection"), BorderLayout.WEST);
        JLabel currentTag = smallTag("LIVE FROM COURSE MANAGER");
        sectionHeader.add(currentTag, BorderLayout.EAST);

        dashboardList = new JPanel(new CardLayout());
        dashboardList.setOpaque(false);
        dashboardEmptyState = createDashboardEmptyState();
        dashboardList.add(dashboardEmptyState, "empty");
        dashboardList.add(createTableScrollPane(createCourseTable(dashboardModel)), "table");
        currentSection.add(sectionHeader, BorderLayout.NORTH);
        currentSection.add(dashboardList, BorderLayout.CENTER);
        page.add(currentSection);
        return page;
    }

    private JPanel createCoursesPage() {
        JPanel page = createPage();
        addHeading(page, "All Courses", "Review, filter, and refresh the courses in your semester collection.");

        JPanel section = createSection();
        JPanel headingRow = new JPanel(new BorderLayout(12, 0));
        headingRow.setOpaque(false);
        courseCount = sectionTitle("0 courses");
        headingRow.add(courseCount, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton addButton = AppTheme.primaryButton("Add Course");
        JButton refreshButton = AppTheme.secondaryButton("Refresh");
        addButton.addActionListener(event -> showPage(ADD_COURSE));
        refreshButton.addActionListener(event -> {
            refreshDataViews();
            showMessage("Course view refreshed.", AppTheme.MUTED);
        });
        actions.add(addButton);
        actions.add(refreshButton);
        interactiveComponents.add(addButton);
        interactiveComponents.add(refreshButton);
        headingRow.add(actions, BorderLayout.EAST);

        JPanel filterRow = new JPanel(new BorderLayout(10, 0));
        filterRow.setOpaque(false);
        courseFilterField = new JTextField();
        courseFilterField.setToolTipText("Filter by course code, title, or credit units");
        AppTheme.styleInput(courseFilterField);
        courseFilterField.setPreferredSize(new Dimension(250, 39));
        courseFilterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateCourseFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateCourseFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateCourseFilter();
            }
        });
        JLabel filterHint = bodyLabel("Filter by code, title, or units");
        filterRow.add(courseFilterField, BorderLayout.WEST);
        filterRow.add(filterHint, BorderLayout.CENTER);
        interactiveComponents.add(courseFilterField);

        coursesList = new JPanel(new CardLayout());
        coursesList.setOpaque(false);
        courseEmptyState = createCourseEmptyState();
        coursesList.add(courseEmptyState, "empty");
        coursesList.add(createTableScrollPane(createCourseTable(coursesModel)), "table");
        JTable coursesTable = findCourseTable(coursesList);
        coursesTable.setRowSorter(coursesSorter);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setOpaque(false);
        content.add(headingRow, BorderLayout.NORTH);
        content.add(filterRow, BorderLayout.CENTER);
        section.add(content, BorderLayout.NORTH);
        section.add(coursesList, BorderLayout.CENTER);
        page.add(section);
        return page;
    }

    private JPanel createAddCoursePage() {
        JPanel page = createPage();
        addHeading(page, "Add New Course", "Enter the course details below. Course codes are stored in uppercase.");

        JPanel formCard = createSection();
        formCard.setLayout(new BorderLayout(0, 16));
        JLabel formTitle = sectionTitle("Course details");
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        courseCodeField = createFormField("e.g. COS 201");
        courseTitleField = createFormField("e.g. Programming I");
        creditUnitsField = createFormField("e.g. 3");
        addFormRow(fields, 0, "Course Code", "Required. Whitespace is trimmed; code is normalized.", courseCodeField);
        addFormRow(fields, 1, "Course Title", "Required. Use the title as you want it displayed.", courseTitleField);
        addFormRow(fields, 2, "Credit Units", "Enter a positive whole number.", creditUnitsField);

        JPanel formActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        formActions.setOpaque(false);
        JButton addButton = AppTheme.primaryButton("Add Course");
        JButton clearButton = AppTheme.secondaryButton("Clear Form");
        addButton.addActionListener(event -> addCourseFromForm());
        clearButton.addActionListener(event -> clearCourseForm());
        formActions.add(addButton);
        formActions.add(clearButton);
        interactiveComponents.add(courseCodeField);
        interactiveComponents.add(courseTitleField);
        interactiveComponents.add(creditUnitsField);
        interactiveComponents.add(addButton);
        interactiveComponents.add(clearButton);

        courseCodeField.addActionListener(event -> courseTitleField.requestFocusInWindow());
        courseTitleField.addActionListener(event -> creditUnitsField.requestFocusInWindow());
        creditUnitsField.addActionListener(event -> addCourseFromForm());

        JPanel lower = new JPanel(new BorderLayout(0, 12));
        lower.setOpaque(false);
        formFeedback = new JLabel(" ");
        formFeedback.setFont(AppTheme.BODY);
        lower.add(formActions, BorderLayout.NORTH);
        lower.add(formFeedback, BorderLayout.CENTER);
        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(lower, BorderLayout.SOUTH);
        page.add(formCard);
        return page;
    }

    private JPanel createSearchPage() {
        JPanel page = createPage();
        addHeading(page, "Search Courses", "Look up a course by code. Search ignores letter case and surrounding spaces.");

        JPanel searchCard = createSection();
        searchCard.setLayout(new BorderLayout(0, 16));
        searchCard.add(sectionTitle("Find a course"), BorderLayout.NORTH);

        JPanel controls = new JPanel(new BorderLayout(10, 0));
        controls.setOpaque(false);
        searchField = new JTextField();
        AppTheme.styleInput(searchField);
        searchField.setPreferredSize(new Dimension(360, 42));
        searchField.setToolTipText("Enter a course code");
        JButton searchButton = AppTheme.primaryButton("Search");
        JButton clearButton = AppTheme.secondaryButton("Clear");
        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchActions.setOpaque(false);
        searchActions.add(searchButton);
        searchActions.add(clearButton);
        controls.add(searchField, BorderLayout.CENTER);
        controls.add(searchActions, BorderLayout.EAST);
        searchCard.add(controls, BorderLayout.CENTER);

        searchFeedback = new JLabel("Enter a code such as COS 201.");
        searchFeedback.setFont(AppTheme.BODY);
        searchFeedback.setForeground(AppTheme.MUTED);
        searchCard.add(searchFeedback, BorderLayout.SOUTH);

        searchResultPanel = new JPanel(new BorderLayout());
        searchResultPanel.setOpaque(false);
        searchResultPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        searchResultPanel.add(emptyState("Ready when you are", "Search results will appear in this area."),
                BorderLayout.CENTER);
        JPanel resultSection = createSection();
        resultSection.add(sectionTitle("Search result"), BorderLayout.NORTH);
        resultSection.add(searchResultPanel, BorderLayout.CENTER);

        searchButton.addActionListener(event -> performCourseSearch());
        clearButton.addActionListener(event -> clearSearch());
        searchField.addActionListener(event -> performCourseSearch());
        interactiveComponents.add(searchField);
        interactiveComponents.add(searchButton);
        interactiveComponents.add(clearButton);

        page.add(searchCard);
        page.add(Box.createVerticalStrut(16));
        page.add(resultSection);
        return page;
    }

    private JPanel createPage() {
        JPanel page = new PagePanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(AppTheme.CANVAS);
        page.setBorder(BorderFactory.createEmptyBorder(22, 31, 20, 31));
        return page;
    }

    private JScrollPane createPageScrollPane(JPanel page) {
        JScrollPane scrollPane = new JScrollPane(page);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(AppTheme.CANVAS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    private JPanel createDashboardEmptyState() {
        JPanel empty = emptyState("Your course list is ready to begin",
                "Add your first course to see semester records and total credit units here.");
        JButton addButton = AppTheme.secondaryButton("Add your first course");
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.addActionListener(event -> showPage(ADD_COURSE));
        interactiveComponents.add(addButton);
        empty.add(Box.createVerticalStrut(13));
        empty.add(addButton);
        return empty;
    }

    private void addHeading(JPanel page, String title, String supportingText) {
        JLabel heading = new JLabel(title);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 25));
        heading.setForeground(AppTheme.TEXT);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel description = bodyLabel(supportingText);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(heading);
        page.add(Box.createVerticalStrut(5));
        page.add(description);
        page.add(Box.createVerticalStrut(20));
    }

    private JPanel createMetricCard(String heading, JLabel value, String detail) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, AppTheme.TEAL),
                        BorderFactory.createEmptyBorder(13, 15, 14, 15))));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(heading);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        label.setForeground(AppTheme.MUTED);
        value.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 27));
        value.setForeground(AppTheme.TEXT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel detailLabel = bodyLabel(detail);
        detailLabel.setFont(AppTheme.SMALL);
        card.add(label);
        card.add(Box.createVerticalStrut(7));
        card.add(value);
        card.add(Box.createVerticalStrut(3));
        card.add(detailLabel);
        return card;
    }

    private JPanel createSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 13));
        panel.setBackground(AppTheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(16, 17, 17, 17)));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        label.setForeground(AppTheme.TEXT);
        return label;
    }

    private JLabel bodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AppTheme.BODY);
        label.setForeground(AppTheme.MUTED);
        return label;
    }

    private JLabel smallTag(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        label.setForeground(AppTheme.TEAL);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 225, 220)),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        return label;
    }

    private JPanel emptyState(String title, String description) {
        JPanel empty = new JPanel();
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBackground(new Color(249, 250, 248));
        empty.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(20, 19, 20, 19)));
        JLabel titleLabel = sectionTitle(title);
        JLabel descriptionLabel = bodyLabel(description);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        empty.add(titleLabel);
        empty.add(Box.createVerticalStrut(6));
        empty.add(descriptionLabel);
        return empty;
    }

    private JPanel createCourseEmptyState() {
        JPanel empty = new JPanel();
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
        empty.setBackground(new Color(249, 250, 248));
        empty.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER),
                BorderFactory.createEmptyBorder(20, 19, 20, 19)));
        courseEmptyTitle = sectionTitle("No courses recorded yet");
        courseEmptyDescription = bodyLabel("Use Add Course to start your collection.");
        courseEmptyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        courseEmptyDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        empty.add(courseEmptyTitle);
        empty.add(Box.createVerticalStrut(6));
        empty.add(courseEmptyDescription);
        return empty;
    }

    private JTextField createFormField(String placeholder) {
        JTextField field = new JTextField();
        AppTheme.styleInput(field);
        field.setPreferredSize(new Dimension(420, 42));
        field.putClientProperty("placeholder", placeholder);
        return field;
    }

    private void addFormRow(JPanel panel, int row, String labelText, String helpText, JTextField field) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row * 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(7, 0, 3, 18);
        JLabel label = new JLabel(labelText);
        label.setFont(AppTheme.BODY_BOLD);
        label.setForeground(AppTheme.TEXT);
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(7, 0, 3, 0);
        panel.add(field, constraints);

        constraints.gridx = 1;
        constraints.gridy = row * 2 + 1;
        constraints.weightx = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 9, 0);
        JLabel helper = bodyLabel(helpText);
        helper.setFont(AppTheme.SMALL);
        panel.add(helper, constraints);
    }

    private JScrollPane createTableScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(700, 225));
        return scrollPane;
    }

    private JTable createCourseTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(AppTheme.BODY);
        table.setForeground(AppTheme.TEXT);
        table.setBackground(Color.WHITE);
        table.setRowHeight(36);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(AppTheme.BORDER);
        table.setSelectionBackground(new Color(220, 240, 236));
        table.setSelectionForeground(AppTheme.TEXT);
        table.setAutoCreateRowSorter(true);

        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        table.getTableHeader().setForeground(AppTheme.MUTED);
        table.getTableHeader().setBackground(new Color(243, 246, 245));
        table.getTableHeader().setPreferredSize(new Dimension(0, 37));
        table.getColumnModel().getColumn(0).setPreferredWidth(130);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);
        table.getColumnModel().getColumn(2).setPreferredWidth(105);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                    boolean focused, int row, int column) {
                Component cell = super.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                if (!selected) {
                    cell.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 249));
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return cell;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(Integer.class, renderer);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable source, Object value, boolean selected,
                    boolean focused, int row, int column) {
                Component cell = renderer.getTableCellRendererComponent(
                        source, value, selected, focused, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                return cell;
            }
        });
        return table;
    }

    private DefaultTableModel createCourseTableModel() {
        return new DefaultTableModel(new Object[] {"Course Code", "Course Title", "Credit Units"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 2 ? Integer.class : String.class;
            }
        };
    }

    private JTable findCourseTable(JPanel container) {
        JScrollPane scrollPane = (JScrollPane) container.getComponent(1);
        return (JTable) scrollPane.getViewport().getView();
    }

    private void showPage(String page) {
        currentPage = page;
        pageLayout.show(pageDeck, page);
        updateNavigationSelection(page);
        if (ADD_COURSE.equals(page)) {
            courseCodeField.requestFocusInWindow();
        } else if (SEARCH.equals(page)) {
            searchField.requestFocusInWindow();
        }
    }

    private void updateNavigationSelection(String selectedPage) {
        String[] pages = {DASHBOARD, COURSES, ADD_COURSE, SEARCH};
        for (int index = 0; index < navigationButtons.size(); index++) {
            JButton button = navigationButtons.get(index);
            boolean selected = pages[index].equals(selectedPage);
            button.putClientProperty("selected", Boolean.valueOf(selected));
            button.setBackground(selected ? new Color(39, 68, 80) : AppTheme.NAVY);
            button.setForeground(selected ? new Color(133, 222, 208) : new Color(218, 226, 229));
            button.setFont(new Font(Font.SANS_SERIF, selected ? Font.BOLD : Font.PLAIN, 13));
            button.setBorder(selected
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 3, 0, 0, AppTheme.TEAL),
                            BorderFactory.createEmptyBorder(0, 12, 0, 9))
                    : BorderFactory.createEmptyBorder(0, 15, 0, 9));
        }
    }

    private void addCourseFromForm() {
        clearFieldErrors();
        String code = courseCodeField.getText();
        String title = courseTitleField.getText();
        String unitsText = creditUnitsField.getText();

        if (code.trim().isEmpty()) {
            showFormError("Enter a course code.", courseCodeField);
            return;
        }
        if (title.trim().isEmpty()) {
            showFormError("Enter a course title.", courseTitleField);
            return;
        }

        int units;
        try {
            units = Integer.parseInt(unitsText.trim());
        } catch (NumberFormatException exception) {
            showFormError("Credit units must be a whole number.", creditUnitsField);
            return;
        }
        if (units <= 0) {
            showFormError("Credit units must be greater than zero.", creditUnitsField);
            return;
        }

        try {
            Course course = new Course(code, title, units);
            courseManager.addCourse(course);
            courseCodeField.setText("");
            courseTitleField.setText("");
            creditUnitsField.setText("");
            showFormMessage("Course added. Save when you are ready to persist this change.", AppTheme.SUCCESS);
            showMessage("Course " + course.getCourseCode() + " added to the current list.", AppTheme.SUCCESS);
            refreshDataViews();
            courseCodeField.requestFocusInWindow();
        } catch (IllegalArgumentException exception) {
            showFormError(exception.getMessage(), courseCodeField);
        }
    }

    private void showFormError(String message, JTextField field) {
        field.putClientProperty("invalid", Boolean.TRUE);
        field.setBorder(AppTheme.inputBorder(AppTheme.ERROR));
        showFormMessage(message, AppTheme.ERROR);
        showMessage(message, AppTheme.ERROR);
        field.requestFocusInWindow();
    }

    private void clearFieldErrors() {
        courseCodeField.putClientProperty("invalid", Boolean.FALSE);
        courseTitleField.putClientProperty("invalid", Boolean.FALSE);
        creditUnitsField.putClientProperty("invalid", Boolean.FALSE);
        courseCodeField.setBorder(AppTheme.inputBorder(AppTheme.BORDER));
        courseTitleField.setBorder(AppTheme.inputBorder(AppTheme.BORDER));
        creditUnitsField.setBorder(AppTheme.inputBorder(AppTheme.BORDER));
    }

    private void showFormMessage(String text, Color color) {
        formFeedback.setText(text);
        formFeedback.setForeground(color);
    }

    private void clearCourseForm() {
        if (!courseCodeField.getText().isEmpty() || !courseTitleField.getText().isEmpty()
                || !creditUnitsField.getText().isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Clear the values currently entered in the course form?",
                    "Clear course form", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        courseCodeField.setText("");
        courseTitleField.setText("");
        creditUnitsField.setText("");
        clearFieldErrors();
        showFormMessage(" ", AppTheme.MUTED);
        courseCodeField.requestFocusInWindow();
    }

    private void updateCourseFilter() {
        String filter = courseFilterField.getText().trim();
        if (filter.isEmpty()) {
            coursesSorter.setRowFilter(null);
        } else {
            coursesSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(filter)));
        }

        int visibleRows = coursesSorter.getViewRowCount();
        int totalCourses = courseManager.getCourses().size();
        if (totalCourses == 0) {
            courseEmptyTitle.setText("No courses recorded yet");
            courseEmptyDescription.setText("Use Add Course to start your collection.");
            ((CardLayout) coursesList.getLayout()).show(coursesList, "empty");
        } else if (visibleRows == 0) {
            courseEmptyTitle.setText("No matching courses");
            courseEmptyDescription.setText("Try another code, title, or unit value.");
            ((CardLayout) coursesList.getLayout()).show(coursesList, "empty");
        } else {
            ((CardLayout) coursesList.getLayout()).show(coursesList, "table");
        }
    }

    private void performCourseSearch() {
        String code = searchField.getText();
        if (code.trim().isEmpty()) {
            lastSearchCode = "";
            showSearchFeedback("Enter a course code before searching.", AppTheme.ERROR);
            showSearchEmpty("Course code required", "Enter a code such as COS 201 to search.");
            searchField.requestFocusInWindow();
            return;
        }
        lastSearchCode = code;
        displaySearchResult(code);
    }

    private void displaySearchResult(String code) {
        Course course = courseManager.findCourseByCode(code);
        searchResultPanel.removeAll();
        if (course == null) {
            showSearchFeedback("No course found with code " + code.trim() + ".", AppTheme.WARNING);
            searchResultPanel.add(emptyState("No matching course", "Check the code and try again."),
                    BorderLayout.CENTER);
        } else {
            showSearchFeedback("Course found.", AppTheme.SUCCESS);
            JPanel result = new JPanel(new GridLayout(1, 3, 10, 0));
            result.setBackground(Color.WHITE);
            result.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER),
                    BorderFactory.createEmptyBorder(17, 15, 17, 15)));
            result.add(createResultValue("COURSE CODE", course.getCourseCode()));
            result.add(createResultValue("COURSE TITLE", course.getCourseTitle()));
            result.add(createResultValue("CREDIT UNITS", Integer.toString(course.getCreditUnits())));
            searchResultPanel.add(result, BorderLayout.CENTER);
        }
        searchResultPanel.revalidate();
        searchResultPanel.repaint();
    }

    private JPanel createResultValue(String labelText, String valueText) {
        JPanel valuePanel = new JPanel();
        valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.Y_AXIS));
        valuePanel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
        label.setForeground(AppTheme.MUTED);
        JLabel value = new JLabel(valueText);
        value.setFont(AppTheme.BODY_BOLD);
        value.setForeground(AppTheme.TEXT);
        valuePanel.add(label);
        valuePanel.add(Box.createVerticalStrut(7));
        valuePanel.add(value);
        return valuePanel;
    }

    private void showSearchEmpty(String title, String detail) {
        searchResultPanel.removeAll();
        searchResultPanel.add(emptyState(title, detail), BorderLayout.CENTER);
        searchResultPanel.revalidate();
        searchResultPanel.repaint();
    }

    private void showSearchFeedback(String text, Color color) {
        searchFeedback.setText(text);
        searchFeedback.setForeground(color);
    }

    private void clearSearch() {
        searchField.setText("");
        lastSearchCode = "";
        showSearchFeedback("Enter a code such as COS 201.", AppTheme.MUTED);
        showSearchEmpty("Ready when you are", "Search results will appear in this area.");
        searchField.requestFocusInWindow();
    }

    private void refreshDataViews() {
        List<Course> courses = courseManager.getCourses();
        dashboardModel.setRowCount(0);
        coursesModel.setRowCount(0);
        for (Course course : courses) {
            Object[] row = {course.getCourseCode(), course.getCourseTitle(), course.getCreditUnits()};
            dashboardModel.addRow(row);
            coursesModel.addRow(row.clone());
        }

        dashboardCourseCount.setText(Integer.toString(courses.size()));
        dashboardUnitCount.setText(Integer.toString(courseManager.calculateTotalCreditUnits()));
        courseCount.setText(courses.size() + (courses.size() == 1 ? " course" : " courses"));
        ((CardLayout) dashboardList.getLayout()).show(dashboardList,
                courses.isEmpty() ? "empty" : "table");
        updateCourseFilter();
        if (!lastSearchCode.trim().isEmpty()) {
            displaySearchResult(lastSearchCode);
        }
        updatePersistenceStatus();
    }

    private void updatePersistenceStatus() {
        if (!startupComplete) {
            persistenceBadge.setText("CHECKING  Reading course data");
            persistenceBadge.setForeground(AppTheme.MUTED);
            return;
        }

        boolean changed = !snapshotCourses().equals(savedSnapshot);
        if (persistenceError != null) {
            persistenceBadge.setText("ERROR  Saved data unavailable");
            persistenceBadge.setForeground(AppTheme.ERROR);
        } else if (changed) {
            persistenceBadge.setText("UNSAVED  Save to keep changes");
            persistenceBadge.setForeground(AppTheme.WARNING);
        } else if (rejectedFileRecords > 0) {
            persistenceBadge.setText("REVIEW  " + rejectedFileRecords + " file row(s) rejected");
            persistenceBadge.setForeground(AppTheme.WARNING);
        } else {
            persistenceBadge.setText("SAVED  All changes saved");
            persistenceBadge.setForeground(AppTheme.SUCCESS);
        }
    }

    private String snapshotCourses() {
        StringBuilder snapshot = new StringBuilder();
        for (Course course : courseManager.getCourses()) {
            String code = course.getCourseCode();
            String title = course.getCourseTitle();
            snapshot.append(code.length()).append(':').append(code)
                    .append(title.length()).append(':').append(title)
                    .append(course.getCreditUnits()).append(';');
        }
        return snapshot.toString();
    }

    private void saveCourses() {
        if (busy || !startupComplete) {
            return;
        }
        if (rejectedFileRecords > 0) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "The saved file contains " + rejectedFileRecords
                            + " malformed or duplicate row(s). Saving replaces the file and removes those rejected rows. Continue?",
                    "Replace saved course file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        setBusy(true);
        showMessage("Saving course records…", AppTheme.MUTED);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws IOException {
                courseManager.saveCourses();
                return null;
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    get();
                    savedSnapshot = snapshotCourses();
                    rejectedFileRecords = 0;
                    persistenceError = null;
                    refreshDataViews();
                    showMessage("Courses saved to data/courses.txt.", AppTheme.SUCCESS);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    persistenceError = "Save interrupted";
                    updatePersistenceStatus();
                    showMessage("Saving was interrupted. " + exception.getMessage(), AppTheme.ERROR);
                } catch (ExecutionException exception) {
                    persistenceError = exception.getCause().getMessage();
                    updatePersistenceStatus();
                    showMessage("Could not save courses: " + exception.getCause().getMessage(), AppTheme.ERROR);
                }
            }
        }.execute();
    }

    private void loadCourses() {
        if (busy || !startupComplete) {
            return;
        }
        if (!snapshotCourses().equals(savedSnapshot)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Loading replaces the current in-memory course list. Unsaved changes will be lost. Continue?",
                    "Replace current courses", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        setBusy(true);
        showMessage("Loading saved course records…", AppTheme.MUTED);
        new SwingWorker<CourseManager.LoadResult, Void>() {
            @Override
            protected CourseManager.LoadResult doInBackground() throws IOException {
                return courseManager.loadCourses();
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    CourseManager.LoadResult result = get();
                    savedSnapshot = snapshotCourses();
                    rejectedFileRecords = result.getRejectedCount();
                    persistenceError = null;
                    refreshDataViews();
                    String summary = "Loaded " + result.getLoadedCount() + " course(s).";
                    if (rejectedFileRecords > 0) {
                        summary += " Rejected " + rejectedFileRecords + " malformed or duplicate row(s).";
                        showMessage(summary, AppTheme.WARNING);
                    } else {
                        showMessage(summary, AppTheme.SUCCESS);
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    persistenceError = "Load interrupted";
                    updatePersistenceStatus();
                    showMessage("Loading was interrupted. " + exception.getMessage(), AppTheme.ERROR);
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    if (cause instanceof NoSuchFileException) {
                        persistenceError = "Saved course file is missing";
                        updatePersistenceStatus();
                        showMessage("No saved file exists yet at data/courses.txt. Current courses were kept.",
                                AppTheme.WARNING);
                    } else {
                        persistenceError = cause.getMessage();
                        updatePersistenceStatus();
                        showMessage("Could not load courses: " + cause.getMessage()
                                + ". Current courses were kept.", AppTheme.ERROR);
                    }
                }
            }
        }.execute();
    }

    private void initializeCourseData() {
        setBusy(true);
        showMessage("Checking for saved course records…", AppTheme.MUTED);
        new SwingWorker<StartupResult, Void>() {
            @Override
            protected StartupResult doInBackground() {
                try {
                    return new StartupResult(courseManager.loadCourses(), false, true, null);
                } catch (NoSuchFileException exception) {
                    addSampleCourses();
                    try {
                        courseManager.saveCourses();
                        return new StartupResult(null, true, true, null);
                    } catch (IOException saveException) {
                        return new StartupResult(null, true, false, saveException);
                    }
                } catch (IOException exception) {
                    return new StartupResult(null, false, false, exception);
                }
            }

            @Override
            protected void done() {
                setBusy(false);
                startupComplete = true;
                try {
                    StartupResult result = get();
                    if (result.samplesCreated && result.samplesSaved) {
                        savedSnapshot = snapshotCourses();
                        rejectedFileRecords = 0;
                        persistenceError = null;
                        showMessage("Sample courses were initialized because no saved file existed.", AppTheme.SUCCESS);
                    } else if (result.samplesCreated) {
                        savedSnapshot = null;
                        rejectedFileRecords = 0;
                        persistenceError = result.error.getMessage();
                        showMessage("Sample courses are available in this session, but could not be saved: "
                                + result.error.getMessage(), AppTheme.ERROR);
                    } else if (result.error != null) {
                        savedSnapshot = null;
                        rejectedFileRecords = 0;
                        persistenceError = result.error.getMessage();
                        showMessage("Could not read saved courses: " + result.error.getMessage()
                                + ". No sample data was written.", AppTheme.ERROR);
                    } else {
                        savedSnapshot = snapshotCourses();
                        rejectedFileRecords = result.loadResult.getRejectedCount();
                        persistenceError = null;
                        if (rejectedFileRecords > 0) {
                            showMessage("Loaded " + result.loadResult.getLoadedCount()
                                    + " course(s); rejected " + rejectedFileRecords
                                    + " malformed or duplicate row(s). The file was left unchanged.", AppTheme.WARNING);
                        } else if (result.loadResult.getLoadedCount() == 0) {
                            showMessage("The saved course file is empty. Add a course to begin.", AppTheme.MUTED);
                        } else {
                            showMessage("Loaded " + result.loadResult.getLoadedCount()
                                    + " saved course(s).", AppTheme.SUCCESS);
                        }
                    }
                    refreshDataViews();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showMessage("Startup loading was interrupted.", AppTheme.ERROR);
                } catch (ExecutionException exception) {
                    savedSnapshot = null;
                    persistenceError = exception.getCause().getMessage();
                    updatePersistenceStatus();
                    showMessage("Could not initialize course data: " + exception.getCause().getMessage(),
                            AppTheme.ERROR);
                }
            }
        }.execute();
    }

    private void addSampleCourses() {
        courseManager.addCourse(new Course("COS 201", "Programming I", 3));
        courseManager.addCourse(new Course("MTH 201", "Discrete Mathematics", 3));
        courseManager.addCourse(new Course("CSC 205", "Data Structures", 4));
    }

    private void setBusy(boolean value) {
        busy = value;
        for (JComponent component : interactiveComponents) {
            component.setEnabled(!value);
        }
        for (JButton button : navigationButtons) {
            button.setEnabled(!value);
        }
        persistenceBadge.setForeground(value ? AppTheme.MUTED : persistenceBadge.getForeground());
    }

    private void showMessage(String message, Color color) {
        actionMessage.setText(message);
        actionMessage.setForeground(color);
    }

    private void requestApplicationClose() {
        if (busy) {
            showMessage("Please wait for the current file operation to finish.", AppTheme.WARNING);
            return;
        }
        if (!startupComplete || snapshotCourses().equals(savedSnapshot)) {
            dispose();
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "There are unsaved course changes. Exit without saving?",
                "Unsaved changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private static class StartupResult {
        private final CourseManager.LoadResult loadResult;
        private final boolean samplesCreated;
        private final boolean samplesSaved;
        private final IOException error;

        private StartupResult(CourseManager.LoadResult loadResult, boolean samplesCreated,
                boolean samplesSaved, IOException error) {
            this.loadResult = loadResult;
            this.samplesCreated = samplesCreated;
            this.samplesSaved = samplesSaved;
            this.error = error;
        }
    }

    private static class BrandMark extends JComponent {
        private BrandMark() {
            setPreferredSize(new Dimension(43, 43));
            setMinimumSize(new Dimension(43, 43));
            setMaximumSize(new Dimension(43, 43));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D drawing = (Graphics2D) graphics.create();
            drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawing.setColor(AppTheme.TEAL);
            drawing.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            drawing.setColor(Color.WHITE);
            drawing.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
            int textWidth = drawing.getFontMetrics().stringWidth("A");
            int textHeight = drawing.getFontMetrics().getAscent();
            drawing.drawString("A", (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - 2);
            drawing.dispose();
        }
    }

    private static class PagePanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRectangle, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRectangle, int orientation, int direction) {
            return Math.max(36, visibleRectangle.height - 36);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}