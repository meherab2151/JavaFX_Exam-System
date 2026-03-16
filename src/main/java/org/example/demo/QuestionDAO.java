package org.example.demo;

import java.sql.*;
import java.util.ArrayList;

// ═══════════════════════════════════════════════════════════
//  QuestionDAO.java
//  UPDATED: save() now returns the generated DB id,
//           loadAll() populates dbId on every Question.
// ═══════════════════════════════════════════════════════════
public class QuestionDAO {

    // ── Update an existing question in place (by dbId) ────────
    public static boolean update(Question q) {
        if (q.getDbId() < 1) return false;
        try {
            if (q instanceof MCQ mcq) {
                String sql = "UPDATE questions SET question_text=?, options=?, correct_index=? WHERE id=?";
                try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                    ps.setString(1, mcq.getQuestionText());
                    ps.setString(2, String.join("|", mcq.getOptions()));
                    ps.setInt(3, mcq.getCorrectIndex());
                    ps.setInt(4, mcq.getDbId());
                    ps.executeUpdate();
                }
            } else if (q instanceof TextQuestion tq) {
                String sql = "UPDATE questions SET question_text=?, answer=? WHERE id=?";
                try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                    ps.setString(1, tq.getQuestionText());
                    ps.setDouble(2, tq.getAnswer());
                    ps.setInt(3, tq.getDbId());
                    ps.executeUpdate();
                }
            } else if (q instanceof RangeQuestion rq) {
                String sql = "UPDATE questions SET question_text=?, min_val=?, max_val=? WHERE id=?";
                try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                    ps.setString(1, rq.getQuestionText());
                    ps.setDouble(2, rq.getMin());
                    ps.setDouble(3, rq.getMax());
                    ps.setInt(4, rq.getDbId());
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) { System.err.println("[QuestionDAO] update: " + e.getMessage()); }
        return false;
    }

    // ── Save a new question — returns DB id, or -1 on fail ───
    public static int save(Question q) {
        if (q instanceof MCQ mcq)           return saveMCQ(mcq);
        if (q instanceof TextQuestion tq)   return saveText(tq);
        if (q instanceof RangeQuestion rq)  return saveRange(rq);
        return -1;
    }

    private static int saveMCQ(MCQ q) {
        String sql = """
            INSERT INTO questions (type, subject, grade, question_text, options, correct_index)
            VALUES ('MCQ', ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getSubject());
            ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText());
            ps.setString(4, String.join("|", q.getOptions()));
            ps.setInt(5, q.getCorrectIndex());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) { int id = keys.getInt(1); q.setDbId(id); return id; }
        } catch (SQLException e) { System.err.println("[QuestionDAO] saveMCQ: " + e.getMessage()); }
        return -1;
    }

    private static int saveText(TextQuestion q) {
        String sql = """
            INSERT INTO questions (type, subject, grade, question_text, answer)
            VALUES ('TEXT', ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getSubject());
            ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText());
            ps.setDouble(4, q.getAnswer());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) { int id = keys.getInt(1); q.setDbId(id); return id; }
        } catch (SQLException e) { System.err.println("[QuestionDAO] saveText: " + e.getMessage()); }
        return -1;
    }

    private static int saveRange(RangeQuestion q) {
        String sql = """
            INSERT INTO questions (type, subject, grade, question_text, min_val, max_val)
            VALUES ('RANGE', ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getSubject());
            ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText());
            ps.setDouble(4, q.getMin());
            ps.setDouble(5, q.getMax());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) { int id = keys.getInt(1); q.setDbId(id); return id; }
        } catch (SQLException e) { System.err.println("[QuestionDAO] saveRange: " + e.getMessage()); }
        return -1;
    }

    // ── Load ALL questions — populates dbId on each object ───
    public static ArrayList<Question> loadAll() {
        ArrayList<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions ORDER BY id ASC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Question q = rowToQuestion(rs);
                if (q != null) list.add(q);
            }
        } catch (SQLException e) { System.err.println("[QuestionDAO] loadAll: " + e.getMessage()); }
        return list;
    }

    // ── Delete by DB id (preferred) or fall back to text match ─
    public static boolean delete(Question q) {
        if (q.getDbId() > 0) {
            String sql = "DELETE FROM questions WHERE id = ?";
            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setInt(1, q.getDbId());
                ps.executeUpdate();
                return true;
            } catch (SQLException e) { System.err.println("[QuestionDAO] delete by id: " + e.getMessage()); }
        } else {
            String sql = "DELETE FROM questions WHERE question_text=? AND subject=? AND grade=? AND type=?";
            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
                ps.setString(1, q.getQuestionText());
                ps.setString(2, q.getSubject());
                ps.setInt(3, q.getGrade());
                ps.setString(4, q instanceof MCQ ? "MCQ" : q instanceof TextQuestion ? "TEXT" : "RANGE");
                ps.executeUpdate();
                return true;
            } catch (SQLException e) { System.err.println("[QuestionDAO] delete fallback: " + e.getMessage()); }
        }
        return false;
    }

    // ── Map a DB row → Question object (sets dbId) ────────────
    public static Question rowToQuestion(ResultSet rs) throws SQLException {
        int    id      = rs.getInt("id");
        String type    = rs.getString("type");
        String subject = rs.getString("subject");
        int    grade   = rs.getInt("grade");
        String text    = rs.getString("question_text");

        Question q = switch (type) {
            case "MCQ" -> {
                String raw = rs.getString("options");
                String[] opts = (raw != null) ? raw.split("\\|") : new String[]{};
                yield new MCQ(subject, grade, text, opts, rs.getInt("correct_index"));
            }
            case "TEXT"  -> new TextQuestion(subject, grade, text, rs.getDouble("answer"));
            case "RANGE" -> new RangeQuestion(subject, grade, text,
                    rs.getDouble("min_val"), rs.getDouble("max_val"));
            default -> null;
        };
        if (q != null) q.setDbId(id);
        return q;
    }
}