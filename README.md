# 💸 Finance Tracker App

## 🧾 Overview

**Finance Tracker** is a modular, CLI-based Java application for personal financial management. Users can:

- Create and manage **accounts**
- Track **Income**, **Expense**, and **Movement** transactions
- Categorize spending
- Import/export transactions via CSV
- Persist and restore users via Memento pattern

🔧 Built for extensibility and testability, this MVP serves as the backend foundation for a future full-stack finance dashboard.

---

## 🛠️ Technologies & Features

| Technology        | Use Case |
|------------------|----------|
| **Java Collections & Generics** | Manage users, accounts, and transactions with type safety |
| **Java I/O**      | CSV import/export and file-based persistence |
| **Custom Annotation `@Sanitize`** | Enforces declarative validation for domain fields |
| **Logger (Singleton)** | App-wide logging to a single timestamped `.log` file |
| **JUnit 5 + Mockito** | Unit testing + mock-based isolation for file and service layers |
| **Reflection**    | Dynamic validation engine via `InputSanitizer` |
| **Inversion of Control (IoC)** | Services injected with dependencies for modularity |
| **Stream API + Lambdas** | Filtering and mapping for categories, transactions |
| **Exception Shielding** | Controlled error propagation with user-friendly CLI feedback |

---

## 🧩 Design Patterns (with Justification)

| Pattern              | Class(es)                              | Purpose |
|----------------------|-----------------------------------------|---------|
| **Factory**          | `TransactionFactory`                    | Encapsulates creation of transaction types |
| **Abstract Factory** | `TransactionCreator` interface strategy | Flexible creation logic by transaction type |
| **Composite**        | `TransactionList`, `CompositeTransaction` | Nested transaction hierarchies |
| **Iterator**         | `TransactionIterator`                   | Navigates composite transaction lists |
| **Builder**          | `IncomeTransactionBuilder`              | Clean construction of transaction objects |
| **Strategy**         | Creator strategy map inside factory     | Dynamic behavior switching |
| **Memento**          | `MementoService`, `UserSnapshot`        | Save/restore user sessions via JSON |
| **Singleton**        | `LoggerFactory`                         | Centralized, configurable logging instance |
| **Exception Shielding** | All services                          | User-facing error control & internal logging |

---

## 🧪 Testing Suite

| Module | Coverage |
|--------|----------|
| `TransactionService`, `AccountService` | Full unit testing |
| `CsvImporter`, `CsvWriter`, `FileIOService` | Tested using mocks and real IO |
| `InputSanitizer` | Validates annotation logic |
| `MementoService` | Load/save snapshot tests |
| `Mockito` | Used to isolate IO dependencies |

---

## 🧠 Technology Justifications

- ✅ **Reflection** allows you to write `InputSanitizer` once and use it across all models with annotations — avoiding repetitive boilerplate.
- ✅ **Custom Annotations** (`@Sanitize`) enable domain-driven, declarative validation.
- ✅ **Stream API + Lambdas** simplify category filtering and transaction flattening.
- ✅ **Inversion of Control** ensures testability and clean constructor-based dependency injection.
- ✅ **Exception Shielding** prevents internal errors from reaching users while enabling detailed log diagnostics.
- ✅ **Mockito** lets you simulate file or user operations without side effects — essential for IO layer testing.
- ✅ **Singleton LoggerFactory** keeps logs clean, consolidated, and non-intrusive to the CLI experience.

---

## 📂 Setup & Execution

```bash
git clone https://github.com/StefanoBertaccini1998/finance_tracker_app.git
cd finance_tracker_app
javac -d out $(find . -name "*.java")
java -cp out it.finance.sb.FinanceTrackApplication
