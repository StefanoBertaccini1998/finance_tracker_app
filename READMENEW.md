# 💸 Finance Tracker App

## 🧾 Overview

**Finance Tracker** is a modular, CLI-based Java application for personal financial management. It enables users to:

* Create and manage multiple **accounts**
* Record and analyze **Income**, **Expense**, and **Transfer** transactions
* Organize financial data through **categories**
* Import/export transactions via **CSV**
* Persist and restore user sessions using the **Memento pattern**

🔧 Designed with clean OOP, SOLID principles, and modularity in mind, the project is structured as a testable backend MVP, ready for extension into a full-stack dashboard.

---

## 🛠️ Technologies & Features

| Technology                      | Use Case                                                       |
| ------------------------------- | -------------------------------------------------------------- |
| **Java Collections & Generics** | Strongly typed handling of user, account, and transaction data |
| **Java I/O (NIO2)**             | File-based persistence, transaction CSV input/output           |
| **Custom Annotations**          | `@Sanitize` provides declarative field validation              |
| **Reflection API**              | Powers dynamic validation using annotations                    |
| **Logging (Singleton pattern)** | Application-wide log file system without interfering with CLI  |
| **JUnit 5 + Mockito**           | Unit testing and mocking for isolation                         |
| **Stream API + Lambdas**        | Filtering, mapping, and transformation of transaction data     |
| **Inversion of Control (IoC)**  | Constructor injection for service decoupling and testability   |
| **Exception Shielding**         | Controlled propagation of errors with CLI-safe messages        |

---

## 🧩 Design Patterns Used

| Pattern                 | Implementation                                  | Justification                                                     |
| ----------------------- | ----------------------------------------------- | ----------------------------------------------------------------- |
| **Factory**             | `TransactionFactory`                            | Decouples creation of Income, Expense, Movement objects           |
| **Abstract Factory**    | `DefaultFinanceFactory`                         | Centralized factory for accounts and transactions with validation |
| **Strategy**            | Internal map of creators in transaction factory | Flexible selection of transaction creation logic                  |
| **Composite**           | `TransactionList`                               | Treats single and grouped transactions uniformly                  |
| **Iterator**            | `TransactionIterator`                           | Navigates hierarchical transaction lists                          |
| **Builder**             | Applied during CSV parsing / test scenarios     | Constructs transaction entities in a clean, readable way          |
| **Memento**             | `UserSnapshot`, `MementoService`                | Save/restore user sessions via JSON snapshot mechanism            |
| **Singleton**           | `LoggerFactory`                                 | Centralized, file-based logger instance shared app-wide           |
| **Exception Shielding** | All Services                                    | Prevents internal exceptions from leaking to CLI output           |

---

## 🧪 Testing Strategy

| Module               | Covered                                     |
| -------------------- | ------------------------------------------- |
| `TransactionService` | ✅ Fully unit tested with factory mocks      |
| `AccountService`     | ✅ Includes positive/negative path tests     |
| `CsvImporter/Writer` | ✅ Integration tested with mock + real files |
| `MementoService`     | ✅ Snapshot save/load coverage               |
| `InputSanitizer`     | ✅ Validates all field rules via annotations |
| `LoggerFactory`      | ✅ Tested fallback and log generation        |

> ✨ Test suite is designed for clarity, isolation, and no side effects.

---

## 📂 Setup & Execution

### Prerequisites:

* Java 17+

### Build & Run:

```bash
git clone https://github.com/StefanoBertaccini1998/finance_tracker_app.git
cd finance_tracker_app
javac -d out $(find . -name "*.java")
java -cp out it.finance.sb.FinanceTrackApplication
```

---

## 📐 UML Diagrams

> See `assets/uml/` folder for `.png` and `.puml` files.
> [FinanceApp.puml](FinanceApp.puml)

### 🧱 Class Diagram Highlights:

* `User` ↔ `AccountInterface`, `TransactionList`
* `AbstractTransaction` ⇨ `IncomeTransaction`, `ExpenseTransaction`, `MovementTransaction`
* `TransactionFactory`, `TransactionCreator`, `DefaultFinanceFactory`
* `LoggerFactory`, `InputSanitizer`, `@Sanitize`

### 🏛️ Architecture:

```
+-----------------------------+
|      CLI Controllers       |
|----------------------------|
| MainMenu, User, Tx, CSV    |
+-------------+--------------+
              |
              v
+-----------------------------+
|         Services            |
|----------------------------|
| Transaction, Account,      |
| User, Memento, CSV         |
+-------------+--------------+
              |
              v
+-----------------------------+
|       Core / Model          |
|----------------------------|
| User, AccountInterface,    |
| Transaction Types          |
+-----------------------------+
```

---

## 🚧 Known Limitations & Future Work

| Limitation                        | Improvement Idea                                                  |
| --------------------------------- | ----------------------------------------------------------------- |
| ❌ No multithreading in CSV import | Add parallel file parsing using Java `ExecutorService`            |
| ❌ No REST or GUI interface        | Extend CLI backend to Spring Boot REST or JavaFX GUI              |
| ❌ Validation only via annotation  | Integrate `Chain of Responsibility` pattern for validation layers |
| ❌ No user-level authentication    | Add basic credential support and login persistence                |
| ❌ Transaction search is linear    | Implement indexed search or categorization trees                  |

---

## 📋 Logging Policy

* All system logs (info, warning, error) are written to timestamped files in `/log`
* CLI remains clean: user interaction uses `System.out` only
* Log messages use lazy evaluation (`logger.info(() -> ...)`)
* `LoggerFactory.getSafeLogger(...)` avoids crashes from logging init failures

---

## ✅ Summary

This Finance Tracker CLI app demonstrates clean Java architecture with a strong focus on:

* Extensibility and OOP best practices
* Real-world design pattern integration
* Developer-quality logging and validation
* Foundation for testing and full-stack growth
