package org.example.demo;

import java.sql.*;

// ═══════════════════════════════════════════════════════════
//  DatabaseManager.java
//  UPDATED: also calls ExamDAO.createTables() on init.
// ═══════════════════════════════════════════════════════════
public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:eduexam.db";
    private static Connection connection;

    public static void init() {
        try {
            connection = DriverManager.getConnection(URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON;");
            createTables();
            ExamDAO.createTables();
            ResultDAO.createTable();
            System.out.println("[DB] Connected to SQLite: eduexam.db");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        if (connection == null) throw new IllegalStateException("DatabaseManager not initialised.");
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void createTables() throws SQLException {
        Statement st = connection.createStatement();

        st.execute("""
            CREATE TABLE IF NOT EXISTS teachers (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                email     TEXT NOT NULL UNIQUE,
                password  TEXT NOT NULL
            );
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS students (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id TEXT NOT NULL UNIQUE,
                name       TEXT NOT NULL,
                email      TEXT NOT NULL,
                password   TEXT NOT NULL
            );
        """);

        st.execute("""
            CREATE TABLE IF NOT EXISTS questions (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                type          TEXT    NOT NULL,
                subject       TEXT    NOT NULL,
                grade         INTEGER NOT NULL,
                question_text TEXT    NOT NULL,
                options       TEXT,
                correct_index INTEGER,
                answer        REAL,
                min_val       REAL,
                max_val       REAL
            );
        """);

        st.close();
        System.out.println("[DB] Core tables verified/created.");
    }
}