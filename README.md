# Apex Budget Manager 📊

> A sleek, high-performance desktop budget planner and expense analytics dashboard built with **Pure Core Java**.

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![UI Framework](https://img.shields.io/badge/UI-Java%20Swing-blue?style=for-the-badge)
![Dependencies](https://img.shields.io/badge/Dependencies-Zero%20(Pure%20JDK)-brightgreen?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-MVC%20Pattern-orange?style=for-the-badge)

---

## 🌟 Key Features

* **Multi-Account Profile Management:** Switch between isolated user profiles (`Personal`, `College`, `Club`, etc.) on the fly. Each account maintains its own dataset independently.
* **Dynamic Budgeting:** Set a custom monthly budget limit on startup or adjust your budget ceiling dynamically anytime during runtime.
* **Category Breakdown Analytics:** Dynamic visual progress indicators track spending proportions across categories (`Food`, `Transport`, `Utilities`, `Entertainment`, `Shopping`, `Other`).
* **Real-Time Visual Alerts:** Dual-stage status alerts update indicator colors to green (safe), amber (warning), or glowing magenta (budget over-limit).
* **Data Persistence:** Lightweight CSV file persistence handles saving and loading user transactions transparently per profile inside a dedicated `data/` directory.
* **Dracula Midnight Aesthetic:** Custom-styled dark workspace interface designed using raw Swing borders, custom table cell renderers, and modern color palettes.

---

## 🛠️ Technology Stack & Architecture

* **Language:** Java (JDK 17 or higher)
* **GUI Engine:** Standard Java Swing (`javax.swing.*`) & AWT (`java.awt.*`)
* **Storage:** Java I/O (`BufferedReader`, `PrintWriter`) with CSV serialization
* **Design Pattern:** Model-View-Controller (MVC)

### Package Overview

```text
Apex-Budget-Manager/
├── src/
│   ├── model/
│   │   └── Transaction.java     # Domain model & CSV serialization logic
│   ├── service/
│   │   └── FinanceManager.java  # Business logic, analytics, & account storage manager
│   └── ui/
│       └── BudgetDashboard.java # Event-driven Swing UI & Dracula theme renderer
└── data/                        # Automated per-account CSV database storage
