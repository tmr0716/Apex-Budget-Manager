package ui;

import model.Transaction;
import service.FinanceManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BudgetDashboard extends JFrame {
    private FinanceManager manager;
    private DefaultTableModel tableModel;

    // Header & Labels
    private JLabel accountBadgeLabel;
    private JLabel totalSpentLabel;
    private JLabel remainingLabel;
    private JProgressBar overallProgressBar;

    // Panels & Inputs
    private JPanel categoryProgressPanel;
    private JTextField descField;
    private JTextField amountField;
    private JComboBox<String> categoryBox;

    // --- TOKYO NIGHT PALETTE ---
    private static final Color BG_DARK = new Color(26, 27, 38);          // Deep Night Blue
    private static final Color CARD_BG = new Color(36, 40, 59);          // Night Surface
    private static final Color INPUT_BG = new Color(22, 22, 30);         // Inset Dark Well
    private static final Color TEXT_MAIN = new Color(192, 202, 245);     // Off-White Blue
    private static final Color TEXT_MUTED = new Color(86, 95, 137);      // Muted Night Slate
    private static final Color ACCENT_CYAN = new Color(122, 162, 247);   // Soft Pastel Blue
    private static final Color ACCENT_PINK = new Color(247, 118, 142);   // Coral Pink Alert
    private static final Color ACCENT_GREEN = new Color(158, 206, 106);  // Pistachio Green
    private static final Color BORDER_COLOR = new Color(65, 72, 104);    // Night Border

    public BudgetDashboard() {
        String accountName = promptAccountSelection("Enter Account Name (e.g., Personal, College, Club):");
        if (accountName == null || accountName.trim().isEmpty()) {
            accountName = "DefaultUser";
        }

        this.manager = new FinanceManager(accountName);
        if (manager.getMonthlyBudget() <= 0) {
            promptBudgetSetting();
        }

        initUI();
    }

    private String promptAccountSelection(String message) {
        return JOptionPane.showInputDialog(
                null,
                message,
                "Select User Account Profile",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    private void promptBudgetSetting() {
        String input = JOptionPane.showInputDialog(
                null,
                "Set Target Monthly Budget for " + manager.getCurrentAccountName() + " (₹):",
                "Set Monthly Budget",
                JOptionPane.QUESTION_MESSAGE
        );

        if (input != null && !input.trim().isEmpty()) {
            try {
                double budget = Double.parseDouble(input.trim());
                if (budget > 0) {
                    manager.setMonthlyBudget(budget);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void initUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Apex Budget Manager - " + manager.getCurrentAccountName());
        setSize(960, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // Header Section with Account Switcher
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        accountBadgeLabel = new JLabel("Account: " + manager.getCurrentAccountName());
        accountBadgeLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        accountBadgeLabel.setForeground(ACCENT_CYAN);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerButtons.setOpaque(false);

        JButton switchAccBtn = new JButton("Switch Account");
        styleButton(switchAccBtn, CARD_BG, ACCENT_CYAN);
        switchAccBtn.addActionListener(e -> onSwitchAccount());

        JButton setBudgetBtn = new JButton("Set Budget");
        styleButton(setBudgetBtn, CARD_BG, ACCENT_CYAN);
        setBudgetBtn.addActionListener(e -> onChangeBudget());

        headerButtons.add(switchAccBtn);
        headerButtons.add(setBudgetBtn);

        headerPanel.add(accountBadgeLabel, BorderLayout.WEST);
        headerPanel.add(headerButtons, BorderLayout.EAST);
        root.add(headerPanel, BorderLayout.NORTH);

        // 2-Column Dashboard Grid
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 16));
        centerGrid.setOpaque(false);

        centerGrid.add(buildLeftControlColumn());
        centerGrid.add(buildRightDataColumn());

        root.add(centerGrid, BorderLayout.CENTER);

        refreshUI();
    }

    private JPanel buildLeftControlColumn() {
        JPanel column = new JPanel(new BorderLayout(14, 14));
        column.setOpaque(false);

        // Budget Summary Card
        JPanel overviewCard = createCard();
        overviewCard.setLayout(new GridLayout(3, 1, 6, 6));

        totalSpentLabel = new JLabel("Total Spent: ₹0.00");
        totalSpentLabel.setFont(new Font("Consolas", Font.BOLD, 15));
        totalSpentLabel.setForeground(TEXT_MAIN);

        remainingLabel = new JLabel("Remaining Budget: ₹0.00");
        remainingLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        remainingLabel.setForeground(ACCENT_GREEN);

        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setPreferredSize(new Dimension(200, 20));
        overallProgressBar.setBackground(INPUT_BG);
        overallProgressBar.setForeground(ACCENT_CYAN);
        overallProgressBar.setFont(new Font("Consolas", Font.BOLD, 11));

        overviewCard.add(totalSpentLabel);
        overviewCard.add(remainingLabel);
        overviewCard.add(overallProgressBar);

        // Form Card
        JPanel formCard = createCard();
        formCard.setLayout(new GridLayout(7, 1, 6, 6));

        JLabel formTitle = new JLabel("Add New Expense");
        formTitle.setFont(new Font("Consolas", Font.BOLD, 14));
        formTitle.setForeground(ACCENT_CYAN);

        descField = createStyledTextField();
        amountField = createStyledTextField();
        categoryBox = new JComboBox<>(new String[]{"Food", "Transport", "Utilities", "Entertainment", "Shopping", "Other"});
        categoryBox.setBackground(INPUT_BG);
        categoryBox.setForeground(ACCENT_CYAN);
        categoryBox.setFont(new Font("Consolas", Font.PLAIN, 12));

        JButton addButton = new JButton("Add Expense");
        styleButton(addButton, ACCENT_CYAN, Color.BLACK);
        addButton.addActionListener(e -> onAddTransaction());

        formCard.add(formTitle);
        formCard.add(createLabel("Description:"));
        formCard.add(descField);
        formCard.add(createLabel("Amount (₹):"));
        formCard.add(amountField);
        formCard.add(createLabel("Category:"));
        formCard.add(categoryBox);

        column.add(overviewCard, BorderLayout.NORTH);
        column.add(formCard, BorderLayout.CENTER);
        column.add(addButton, BorderLayout.SOUTH);

        return column;
    }

    private JPanel buildRightDataColumn() {
        JPanel column = new JPanel(new BorderLayout(14, 14));
        column.setOpaque(false);

        // Category Breakdown Card
        JPanel catCard = createCard();
        catCard.setLayout(new BorderLayout(6, 6));
        JLabel catTitle = new JLabel("Category Breakdown");
        catTitle.setFont(new Font("Consolas", Font.BOLD, 13));
        catTitle.setForeground(ACCENT_CYAN);

        categoryProgressPanel = new JPanel();
        categoryProgressPanel.setLayout(new BoxLayout(categoryProgressPanel, BoxLayout.Y_AXIS));
        categoryProgressPanel.setOpaque(false);

        catCard.add(catTitle, BorderLayout.NORTH);
        catCard.add(categoryProgressPanel, BorderLayout.CENTER);

        // Table Log Card
        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout(6, 6));

        String[] cols = {"Description", "Amount (₹)", "Category", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_MAIN);
        table.setGridColor(BORDER_COLOR);
        table.setFont(new Font("Consolas", Font.PLAIN, 12));

        JTableHeader header = table.getTableHeader();
        header.setBackground(INPUT_BG);
        header.setForeground(ACCENT_PINK);
        header.setFont(new Font("Consolas", Font.BOLD, 12));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBackground(CARD_BG);
        renderer.setForeground(TEXT_MAIN);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(CARD_BG);
        scroll.setBorder(new LineBorder(BORDER_COLOR, 1));

        tableCard.add(scroll, BorderLayout.CENTER);

        column.add(catCard, BorderLayout.NORTH);
        column.add(tableCard, BorderLayout.CENTER);

        return column;
    }

    private JPanel createCard() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return p;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(INPUT_BG);
        tf.setForeground(ACCENT_CYAN);
        tf.setCaretColor(ACCENT_CYAN);
        tf.setFont(new Font("Consolas", Font.PLAIN, 12));
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(4, 6, 4, 6)));
        return tf;
    }

    private JLabel createLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(TEXT_MUTED);
        l.setFont(new Font("Consolas", Font.PLAIN, 11));
        return l;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(fg, 1), new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void onSwitchAccount() {
        String newAcc = promptAccountSelection("Enter Account Name to switch or create:");
        if (newAcc != null && !newAcc.trim().isEmpty()) {
            manager.switchAccount(newAcc.trim());
            accountBadgeLabel.setText("Account: " + manager.getCurrentAccountName());
            setTitle("Apex Budget Manager - " + manager.getCurrentAccountName());
            if (manager.getMonthlyBudget() <= 0) {
                promptBudgetSetting();
            }
            refreshUI();
        }
    }

    private void onChangeBudget() {
        String input = JOptionPane.showInputDialog(
                this,
                "Enter new monthly budget for " + manager.getCurrentAccountName() + " (₹):",
                String.format("%.2f", manager.getMonthlyBudget())
        );

        if (input != null && !input.trim().isEmpty()) {
            try {
                double newBudget = Double.parseDouble(input.trim());
                if (newBudget > 0) {
                    manager.setMonthlyBudget(newBudget);
                    refreshUI();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric value.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAddTransaction() {
        String desc = descField.getText().trim();
        String amtStr = amountField.getText().trim();

        if (desc.isEmpty() || amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both description and amount.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double amt = Double.parseDouble(amtStr);
            String cat = (String) categoryBox.getSelectedItem();

            Transaction t = new Transaction(desc, amt, cat, LocalDate.now());
            manager.addTransaction(t);

            descField.setText("");
            amountField.setText("");
            refreshUI();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshUI() {
        tableModel.setRowCount(0);
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Transaction t : manager.getTransactions()) {
            tableModel.addRow(new Object[]{
                t.getDescription(),
                String.format("%.2f", t.getAmount()),
                t.getCategory(),
                t.getDate()
            });
            categoryTotals.put(t.getCategory(), categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
        }

        double spent = manager.getTotalSpent();
        double budget = manager.getMonthlyBudget();
        double remaining = budget - spent;
        int pct = budget > 0 ? (int) Math.min((spent / budget) * 100, 100) : 0;

        totalSpentLabel.setText(String.format("Total Spent: ₹%.2f / ₹%.2f", spent, budget));
        remainingLabel.setText(String.format("Remaining Budget: ₹%.2f", remaining));
        overallProgressBar.setValue(pct);

        if (remaining < 0) {
            remainingLabel.setForeground(ACCENT_PINK);
            overallProgressBar.setForeground(ACCENT_PINK);
        } else {
            remainingLabel.setForeground(ACCENT_GREEN);
            overallProgressBar.setForeground(ACCENT_CYAN);
        }

        categoryProgressPanel.removeAll();
        for (String cat : new String[]{"Food", "Transport", "Utilities", "Entertainment", "Shopping", "Other"}) {
            double catSpent = categoryTotals.getOrDefault(cat, 0.0);
            if (catSpent > 0) {
                JPanel barRow = new JPanel(new BorderLayout(6, 2));
                barRow.setOpaque(false);

                JLabel catLbl = new JLabel(String.format("%s: ₹%.2f", cat, catSpent));
                catLbl.setForeground(TEXT_MUTED);
                catLbl.setFont(new Font("Consolas", Font.PLAIN, 11));

                JProgressBar catBar = new JProgressBar(0, (int) Math.max(budget, 1));
                catBar.setValue((int) catSpent);
                catBar.setForeground(ACCENT_CYAN);
                catBar.setBackground(INPUT_BG);

                barRow.add(catLbl, BorderLayout.NORTH);
                barRow.add(catBar, BorderLayout.SOUTH);

                categoryProgressPanel.add(barRow);
                categoryProgressPanel.add(Box.createVerticalStrut(4));
            }
        }
        categoryProgressPanel.revalidate();
        categoryProgressPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BudgetDashboard().setVisible(true));
    }
}
