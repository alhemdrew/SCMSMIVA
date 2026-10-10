import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

final class AppTheme {
    static final Color NAVY = new Color(24, 43, 59);
    static final Color NAVY_HOVER = new Color(35, 59, 74);
    static final Color TEAL = new Color(24, 132, 128);
    static final Color TEAL_HOVER = new Color(18, 112, 108);
    static final Color CANVAS = new Color(246, 247, 245);
    static final Color SURFACE = Color.WHITE;
    static final Color TEXT = new Color(35, 48, 58);
    static final Color MUTED = new Color(103, 119, 128);
    static final Color BORDER = new Color(220, 226, 225);
    static final Color SUCCESS = new Color(27, 119, 84);
    static final Color WARNING = new Color(159, 105, 28);
    static final Color ERROR = new Color(173, 61, 58);
    static final Color FOCUS = new Color(49, 155, 149);

    static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    static final Font BODY_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    static final Font SMALL = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private AppTheme() {
    }

    static JButton primaryButton(String label) {
        JButton button = new JButton(label);
        button.setFont(BODY_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(TEAL);
        button.setOpaque(true);
        button.setFocusPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(11, 17, 11, 17));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(TEAL_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(TEAL);
            }
        });
        return button;
    }

    static JButton secondaryButton(String label) {
        JButton button = new JButton(label);
        button.setFont(BODY_BOLD);
        button.setForeground(TEXT);
        button.setBackground(SURFACE);
        button.setOpaque(true);
        button.setFocusPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(240, 246, 245));
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(SURFACE);
            }
        });
        return button;
    }

    static JButton navigationButton(String label) {
        JButton button = new JButton(label);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setFont(BODY);
        button.setForeground(new Color(218, 226, 229));
        button.setBackground(NAVY);
        button.setOpaque(true);
        button.setFocusPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 9));
        button.putClientProperty("selected", Boolean.FALSE);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (button.isEnabled() && !Boolean.TRUE.equals(button.getClientProperty("selected"))) {
                    button.setBackground(NAVY_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                if (!Boolean.TRUE.equals(button.getClientProperty("selected"))) {
                    button.setBackground(NAVY);
                }
            }
        });
        return button;
    }

    static void styleInput(JTextField field) {
        field.setFont(BODY);
        field.setForeground(TEXT);
        field.setBackground(SURFACE);
        field.setCaretColor(TEAL);
        field.setBorder(inputBorder(BORDER));
        field.setPreferredSize(new java.awt.Dimension(220, 40));
        field.putClientProperty("invalid", Boolean.FALSE);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (!Boolean.TRUE.equals(field.getClientProperty("invalid"))) {
                    field.setBorder(inputBorder(FOCUS));
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (!Boolean.TRUE.equals(field.getClientProperty("invalid"))) {
                    field.setBorder(inputBorder(BORDER));
                }
            }
        });
    }

    static Border inputBorder(Color color) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color),
                BorderFactory.createEmptyBorder(0, 11, 0, 11));
    }
}