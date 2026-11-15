import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

// ==========================================
// 1. DOMAIN MODEL
// ==========================================
class Transaction {
    private final String description;
    private final double amount;
    private final String category;
    private final LocalDate date;

    public Transaction(String description, double amount, String category, LocalDate date) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        this.description = Objects.requireNonNull(description, "Description cannot be null").trim();
        this.amount = amount;
        this.category = Objects.requireNonNull(category, "Category cannot be null");
        this.date = Objects.requireNonNull(date, "Date cannot be null");
    }

    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }

    public String toCSV() {
        return String.format("%s,%.2f,%s,%s", description, amount, category, date);
    }

    public static Transaction fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Malformed CSV line: " + csvLine);
        }
        return new Transaction(
            parts[0].trim(),
            Double.parseDouble(parts[1].trim()),
            parts[2].trim(),
            LocalDate.parse(parts[3].trim())
        );
    }
}

// ==========================================
// 2. SERVICE & SMART SAVINGS ENGINE
// ==========================================
class FinanceManager {
    private final List<Transaction> transactions = new ArrayList<>();
    private String currentAccountName;
    private double monthlyBudget = 0.0;
    private final String dataDir = "data";

    public FinanceManager(String accountName) {
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        switchAccount(accountName);
    }

    public void switchAccount(String newAccountName) {
        this.currentAccountName = newAccountName.trim().replaceAll("[^a-zA-Z0-9_-]", "_");
        this.transactions.clear();
        this.monthlyBudget = 0.0;
        loadAccountData();
    }

    public String getCurrentAccountName() {
        return currentAccountName;
    }

    public void addTransaction(Transaction t) {
        transactions.add(Objects.requireNonNull(t));
        saveAccountData();
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public double getTotalSpent() {
        return transactions.stream().mapToDouble(Transaction::getAmount).sum();
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
        saveAccountData();
    }

    // --- SMART SAVINGS ANALYZER ---
    public String generateSavingsRecommendation() {
        if (transactions.isEmpty()) {
            return "Budget limit reached! Avoid non-essential purchases and review your fixed expenses.";
        }

        // Group spending by category to find the biggest drain
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction t : transactions) {
            categoryTotals.put(t.getCategory(), categoryTotals.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
        }

        String topCategory = "Other";
        double maxSpent = 0.0;
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > maxSpent) {
                maxSpent = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        double totalSpent = getTotalSpent();
        int sharePct = totalSpent > 0 ? (int) Math.round((maxSpent / totalSpent) * 100) : 0;

        String tip;
        switch (topCategory) {
            case "Food":
                tip = "Cook meals at home, carry a water bottle, and pause food delivery apps this week.";
                break;
            case "Shopping":
                tip = "Apply the 48-hour waiting rule before buying non-essentials and unsubscribe from promo alerts.";
                break;
            case "Entertainment":
                tip = "Pause unused streaming subscriptions and opt for free campus/local events instead of paid outings.";
                break;
            case "Transport":
                tip = "Use public transit passes, carpool, or walk short distances instead of booking solo cabs.";
                break;
            case "Utilities":
                tip = "Switch off standby appliances, reduce AC usage, and audit mobile/data recharge plans.";
                break;
            default:
                tip = "Freeze discretionary spending for the rest of the month and track every small cash expense.";
                break;
        }

        return String.format(
            "Highest Drain: %s (₹%.2f | %d%% of spending).\nAction Plan: %s",
            topCategory, maxSpent, sharePct, tip
        );
    }

    private File getAccountFile() {
        return new File(dataDir + File.separator + currentAccountName + ".csv");
    }

    private void saveAccountData() {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(getAccountFile())))) {
            writer.println("BUDGET:" + monthlyBudget);
            for (Transaction t : transactions) {
                writer.println(t.toCSV());
            }
        } catch (IOException e) {
            System.err.println("Error saving account data: " + e.getMessage());
        }
    }

    private void loadAccountData() {
        File file = getAccountFile();
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("BUDGET:")) {
                    this.monthlyBudget = Double.parseDouble(line.substring(7));
                } else {
                    try {
                        transactions.add(Transaction.fromCSV(line));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading account data: " + e.getMessage());
        }
    }
}

// ==========================================
// 3. UI & APPLICATION ENTRY POINT
// ==========================================
public class BudgetDashboard extends JFrame {
    private FinanceManager manager;
    private DefaultTableModel tableModel;

    // Header & Labels
    private JLabel accountBadgeLabel;
    private JLabel totalSpentLabel;
    private JLabel remainingLabel;
    private JProgressBar overallProgressBar;
    private JTextArea advisorTextArea;

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
    private static final Color ACCENT_AMBER = new Color(224, 175, 104);  // Soft Amber Warning
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
        setSize(980, 680);
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
        JPanel column = new JPanel(new BorderLayout(12, 12));
        column.setOpaque(false);

        // Top Section: Budget Summary + Smart Advisor Box
        JPanel topSummaryWrapper = new JPanel(new BorderLayout(0, 10));
        topSummaryWrapper.setOpaque(false);

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

        // Smart Savings Advisor Panel
        JPanel advisorCard = createCard();
        advisorCard.setLayout(new BorderLayout(4, 4));
        JLabel advisorTitle = new JLabel("Smart Savings Advisor");
        advisorTitle.setFont(new Font("Consolas", Font.BOLD, 12));
        advisorTitle.setForeground(ACCENT_AMBER);

        advisorTextArea = new JTextArea(3, 20);
        advisorTextArea.setEditable(false);
        advisorTextArea.setLineWrap(true);
        advisorTextArea.setWrapStyleWord(true);
        advisorTextArea.setBackground(INPUT_BG);
        advisorTextArea.setForeground(ACCENT_GREEN);
        advisorTextArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        advisorTextArea.setBorder(new EmptyBorder(6, 8, 6, 8));

        advisorCard.add(advisorTitle, BorderLayout.NORTH);
        advisorCard.add(advisorTextArea, BorderLayout.CENTER);

        topSummaryWrapper.add(overviewCard, BorderLayout.NORTH);
        topSummaryWrapper.add(advisorCard, BorderLayout.CENTER);

        // Form Card
        JPanel formCard = createCard();
        formCard.setLayout(new GridLayout(7, 1, 4, 4));

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

        column.add(topSummaryWrapper, BorderLayout.NORTH);
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
                new EmptyBorder(10, 12, 10, 12)
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

            // Trigger instant savings advisor alert if budget hits 0 or goes negative
            double remaining = manager.getMonthlyBudget() - manager.getTotalSpent();
            if (manager.getMonthlyBudget() > 0 && remaining <= 0) {
                String advice = manager.generateSavingsRecommendation();
                JOptionPane.showMessageDialog(
                        this,
                        "⚠️ Budget Limit Reached / Exceeded!\n\n" + advice,
                        "Smart Savings Alert",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
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

        if (budget > 0 && remaining <= 0) {
            remainingLabel.setForeground(ACCENT_PINK);
            overallProgressBar.setForeground(ACCENT_PINK);
            advisorTextArea.setForeground(ACCENT_PINK);
            advisorTextArea.setText("⚠️ BUDGET DEPLETED!\n" + manager.generateSavingsRecommendation());
        } else if (budget > 0 && remaining <= budget * 0.2) {
            remainingLabel.setForeground(ACCENT_AMBER);
            overallProgressBar.setForeground(ACCENT_AMBER);
            advisorTextArea.setForeground(ACCENT_AMBER);
            advisorTextArea.setText("⚡ Low Budget Warning (<20% left).\n" + manager.generateSavingsRecommendation());
        } else {
            remainingLabel.setForeground(ACCENT_GREEN);
            overallProgressBar.setForeground(ACCENT_CYAN);
            advisorTextArea.setForeground(ACCENT_GREEN);
            advisorTextArea.setText("✓ Spending is on track. Keep prioritizing essential expenses to build monthly savings.");
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