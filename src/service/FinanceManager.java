package service;

import model.Transaction;
import java.io.*;
import java.util.*;

public class FinanceManager {
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
