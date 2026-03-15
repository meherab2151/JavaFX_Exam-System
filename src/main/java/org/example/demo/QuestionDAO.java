package org.example.demo;

import java.sql.*;
import java.util.ArrayList;

// ═══════════════════════════════════════════════════════════
//  QuestionDAO.java
//  Data Access Object for all Question types.
//  Handles save, load, update, delete from the questions table.
// ═══════════════════════════════════════════════════════════
public class QuestionDAO {

    // ── Save a new question (any type) ────────────────────────
    public static boolean save(Question q) {
        if (q instanceof MCQ mcq) {
            return saveMCQ(mcq);
        } else if (q instanceof TextQuestion tq) {
            return saveText(tq);
        } else if (q instanceof RangeQuestion rq) {
            return saveRange(rq);
        }
        return false;
    }

    private static boolean saveMCQ(MCQ q) {
        String sql = """
            INSERT INTO questions (type, subject, grade, question_text, options, correct_index)
            VALUES ('MCQ', ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, q.getSubject());
            ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText());
            ps.setString(4, String.join("|", q.getOptions())); // store as "A|B|C|D"
            ps.setInt(5, q.getCorrectIndex());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] saveMCQ failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean saveText(TextQuestion q) {
        String sql = """
            INSERT INTO questions (type, subject, grade, question_text, answer)
            VALUES ('TEXT', ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, q.getSubject());
            ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText());
            ps.setDouble(4, q.getAnswer());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] saveText failed: " + e.getMessage());
            return false;
        }
    }

    private static boolean saveRange(RangeQuestion q) {
        String sql = """
            INSERT INTO questions (type, subject, grade, question_text, min_val, max_val)
            VALUES ('RANGE', ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, q.getSubject());
            ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText());
            ps.setDouble(4, q.getMin());
            ps.setDouble(5, q.getMax());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] saveRange failed: " + e.getMessage());
            return false;
        }
    }

    // ── Load ALL questions from DB into QuestionBank ──────────
    public static ArrayList<Question> loadAll() {
        ArrayList<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions ORDER BY id ASC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Question q = rowToQuestion(rs);
                if (q != null) list.add(q);
            }
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] loadAll failed: " + e.getMessage());
        }
        return list;
    }

    // ── Delete a question by object (matches on text + subject + grade) ──
    public static boolean delete(Question q) {
        String sql = "DELETE FROM questions WHERE question_text = ? AND subject = ? AND grade = ? AND type = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, q.getQuestionText());
            ps.setString(2, q.getSubject());
            ps.setInt(3, q.getGrade());
            ps.setString(4, q instanceof MCQ ? "MCQ" : q instanceof TextQuestion ? "TEXT" : "RANGE");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[QuestionDAO] delete failed: " + e.getMessage());
            return false;
        }
    }

    // ── Update an existing question ───────────────────────────
    // We identify by DB id, so we store it transiently on the Question object.
    // For now we delete + re-insert (simpler, safe for small banks).
    public static boolean update(Question oldQ, Question newQ) {
        return delete(oldQ) && save(newQ);
    }

    // ── Map a DB row → Question object ────────────────────────
    private static Question rowToQuestion(ResultSet rs) throws SQLException {
        String type    = rs.getString("type");
        String subject = rs.getString("subject");
        int    grade   = rs.getInt("grade");
        String text    = rs.getString("question_text");

        return switch (type) {
            case "MCQ" -> {
                String raw = rs.getString("options");
                String[] opts = (raw != null) ? raw.split("\\|") : new String[]{};
                yield new MCQ(subject, grade, text, opts, rs.getInt("correct_index"));
            }
            case "TEXT" -> new TextQuestion(subject, grade, text, rs.getDouble("answer"));
            case "RANGE" -> new RangeQuestion(subject, grade, text,
                    rs.getDouble("min_val"), rs.getDouble("max_val"));
            default -> {
                System.err.println("[QuestionDAO] Unknown type: " + type);
                yield null;
            }
        };
    }
}