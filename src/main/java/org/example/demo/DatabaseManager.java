package org.example.demo;

import java.sql.*;

// ═══════════════════════════════════════════════════════════
//  DatabaseManager.java
//  Single point of truth for the SQLite connection.
//  Call DatabaseManager.init() once at app startup.
//  Call DatabaseManager.getConnection() anywhere you need DB.
// ═══════════════════════════════════════════════════════════
public class DatabaseManager {

    // DB file will be created in the working directory as "eduexam.db"
    private static final String URL = "jdbc:sqlite:eduexam.db";

    private static Connection connection;

    // ── Initialise: open connection + create tables ───────────
    public static void init() {
        try {
            connection = DriverManager.getConnection(URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON;");
            createTables();
            System.out.println("[DB] Connected to SQLite: eduexam.db");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    // ── Return the shared connection ──────────────────────────
    public static Connection getConnection() {
        if (connection == null) throw new IllegalStateException("DatabaseManager not initialised. Call init() first.");
        return connection;
    }

    // ── Close on app exit ────────────────────────────────────
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Create all tables if they don't exist ─────────────────
    private static void createTables() throws SQLException {
        Statement st = connection.createStatement();

        // Teachers
        st.execute("""
            CREATE TABLE IF NOT EXISTS teachers (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name   TEXT NOT NULL,
                email       TEXT NOT NULL UNIQUE,
                password    TEXT NOT NULL
            );
        """);

        // Students
        st.execute("""
            CREATE TABLE IF NOT EXISTS students (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id  TEXT NOT NULL UNIQUE,
                name        TEXT NOT NULL,
                email       TEXT NOT NULL,
                password    TEXT NOT NULL
            );
        """);

        // Questions
        // 'type' is one of: MCQ, TEXT, RANGE
        // 'options' stores MCQ choices as pipe-separated string: "A|B|C|D"
        // 'correct_index' is used by MCQ
        // 'answer' is used by TextQuestion
        // 'min_val' / 'max_val' used by RangeQuestion
        st.execute("""
            CREATE TABLE IF NOT EXISTS questions (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                type            TEXT NOT NULL,
                subject         TEXT NOT NULL,
                grade           INTEGER NOT NULL,
                question_text   TEXT NOT NULL,
                options         TEXT,
                correct_index   INTEGER,
                answer          REAL,
                min_val         REAL,
                max_val         REAL
            );
        """);

        st.close();
        System.out.println("[DB] Tables verified/created.");
    }
}