# Apex Budget Manager 📊

> A sleek, standalone desktop personal finance tracker, expense analytics dashboard, and Smart Savings Advisor built with **Pure Core Java**.

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![UI Framework](https://img.shields.io/badge/UI-Java%20Swing-blue?style=for-the-badge)
![Dependencies](https://img.shields.io/badge/Dependencies-Zero%20(Pure%20JDK)-brightgreen?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-Standalone%203--Tier-orange?style=for-the-badge)

---

## 🌟 Key Features

* **Multi-Account Profile System:** Switch between or create isolated user profiles (`Personal`, `College`, `Club`, etc.) on the fly. Each profile maintains its own independent budget and transaction history.
* **Smart Savings Advisor:** Automatically analyzes spending distribution across categories when the budget reaches `₹0` or goes negative, identifying the highest expense drain and providing tailored cost-cutting recommendations via both a live dashboard panel and instant alert popups.
* **Dynamic Budget Management:** Prompts for a custom monthly budget on initial setup and allows real-time limit updates anytime during runtime.
* **Category Breakdown Analytics:** Real-time visual progress bars track spending proportions across `Food`, `Transport`, `Utilities`, `Entertainment`, `Shopping`, and `Other`.
* **Three-Stage Visual Status Alerts:** Dynamically shifts UI indicators between Pistachio Green (safe), Soft Amber (`<20%` budget remaining), and Coral Pink (budget depleted/exceeded).
* **Zero-Config CSV Persistence:** Automatically saves and loads per-account records inside a local `data/` folder using standard Java I/O.
* **Tokyo Night Dark Theme:** Custom-engineered dark workspace featuring deep night-blue panels, pastel indigo highlights, and custom table cell renderers.

---

## 🛠️ Technology Stack & Architecture

* **Language:** Java (JDK 17 or higher)
* **GUI Engine:** Standard Java Swing (`javax.swing.*`) & AWT (`java.awt.*`)
* **Storage:** Java I/O (`BufferedReader`, `PrintWriter`) with automated CSV serialization
* **Architecture:** Modular 3-Tier Design (`Transaction` Model, `FinanceManager` Service, `BudgetDashboard` UI) consolidated into a single standalone `.java` file for zero-setup compilation.

### Project Structure

```text
Apex-Budget-Manager/
├── BudgetDashboard.java    # Complete standalone source (Model, Service, & UI)
├── README.md               # Project documentation
└── data/                   # Auto-generated per-account CSV storage
    └── <AccountName>.csv
