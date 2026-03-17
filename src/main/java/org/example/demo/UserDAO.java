package org.example.demo;

import java.sql.*;
import java.util.ArrayList;

// ═══════════════════════════════════════════════════════════
//  UserDAO.java
//  Data Access Object for Teacher and Student accounts.
//  All DB reads/writes for users go through here.
// ═══════════════════════════════════════════════════════════
public class UserDAO {

    // ╔══════════════════════════════════════════════════════╗
    //  TEACHER OPERATIONS
    // ╚══════════════════════════════════════════════════════╝

    /** Insert a new teacher. Returns true on success, false if email already exists. */
    public static boolean registerTeacher(String fullName, String email, String password) {
        String sql = "INSERT INTO teachers (full_name, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // SQLITE_CONSTRAINT = duplicate email
            System.err.println("[UserDAO] registerTeacher failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Find a teacher by email/username + password.
     * Returns a Teacher object on success, null if not found.
     */
    public static Teacher loginTeacher(String emailOrName, String password) {
        String sql = """
            SELECT full_name, email, password FROM teachers
            WHERE (email = ? OR full_name = ?) AND password = ?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, emailOrName);
            ps.setString(2, emailOrName);
            ps.setString(3, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Teacher(rs.getString("full_name"), rs.getString("email"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] loginTeacher failed: " + e.getMessage());
        }
        return null;
    }

    /** Load all teachers from DB into an ArrayList (used to seed in-memory list). */
    public static ArrayList<Teacher> loadAllTeachers() {
        ArrayList<Teacher> list = new ArrayList<>();
        String sql = "SELECT full_name, email, password FROM teachers";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Teacher(rs.getString("full_name"), rs.getString("email"), rs.getString("password")));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] loadAllTeachers failed: " + e.getMessage());
        }
        return list;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  STUDENT OPERATIONS
    // ╚══════════════════════════════════════════════════════╝

    /**
     * Insert a new student.
     * Returns true on success, false if student_id already exists.
     */
    public static boolean registerStudent(String studentId, String name, String email, String password) {
        String sql = "INSERT INTO students (student_id, name, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[UserDAO] registerStudent failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Find a student by ID/email + password.
     * Returns a Student object on success, null if not found.
     */
    public static Student loginStudent(String idOrEmail, String password) {
        String sql = """
            SELECT student_id, name, email, password FROM students
            WHERE (student_id = ? OR email = ?) AND password = ?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, idOrEmail);
            ps.setString(2, idOrEmail);
            ps.setString(3, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] loginStudent failed: " + e.getMessage());
        }
        return null;
    }

    /** Load all students from DB into an ArrayList. */
    public static ArrayList<Student> loadAllStudents() {
        ArrayList<Student> list = new ArrayList<>();
        String sql = "SELECT student_id, name, email, password FROM students";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] loadAllStudents failed: " + e.getMessage());
        }
        return list;
    }

    /** Check if a student ID is already taken. */
    public static boolean studentIdExists(String studentId) {
        String sql = "SELECT 1 FROM students WHERE student_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            System.err.println("[UserDAO] studentIdExists failed: " + e.getMessage());
            return false;
        }
    }

    /** Check if a teacher email is already taken. */
    public static boolean teacherEmailExists(String email) {
        String sql = "SELECT 1 FROM teachers WHERE email = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            System.err.println("[UserDAO] teacherEmailExists failed: " + e.getMessage());
            return false;
        }
    }
}