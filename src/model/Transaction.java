package model;

import java.time.LocalDate;
import java.util.Objects;

public class Transaction {
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
