package org.example.demo;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

// ═══════════════════════════════════════════════════════════
//  ExamDAO.java
//  Saves and loads Exam objects including their question
//  links (exam_questions junction table).
//
//  Key design: upsert pattern — save() inserts on first call,
//  updates on subsequent calls (detected via exam.getDbId()).
// ═══════════════════════════════════════════════════════════
public class ExamDAO {

    // ╔══════════════════════════════════════════════════════╗
    //  TABLE CREATION  (called from DatabaseManager.init)
    // ╚══════════════════════════════════════════════════════╝
    public static void createTables() throws SQLException {
        Statement st = DatabaseManager.getConnection().createStatement();

        st.execute("""
            CREATE TABLE IF NOT EXISTS exams (
                id                      INTEGER PRIMARY KEY AUTOINCREMENT,
                subject                 TEXT    NOT NULL,
                grade                   INTEGER NOT NULL,
                total_marks             REAL    NOT NULL,
                duration                TEXT    NOT NULL,
                title                   TEXT,
                description             TEXT,
                exam_code               TEXT,
                is_live                 INTEGER NOT NULL DEFAULT 0,
                live_window             TEXT,
                schedule_details        TEXT,
                live_end_millis         INTEGER NOT NULL DEFAULT 0,
                scheduled_start_millis  INTEGER NOT NULL DEFAULT 0,
                scheduled_end_millis    INTEGER NOT NULL DEFAULT 0
            );
        """);

        // Junction table: which questions belong to which exam, with marks
        st.execute("""
            CREATE TABLE IF NOT EXISTS exam_questions (
                exam_id     INTEGER NOT NULL,
                question_id INTEGER NOT NULL,
                marks       REAL    NOT NULL,
                PRIMARY KEY (exam_id, question_id),
                FOREIGN KEY (exam_id)     REFERENCES exams(id)     ON DELETE CASCADE,
                FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
            );
        """);

        st.close();
        System.out.println("[DB] Exam tables verified/created.");
    }

    // ╔══════════════════════════════════════════════════════╗
    //  SAVE (upsert) — call this on every state change
    // ╚══════════════════════════════════════════════════════╝
    public static boolean save(Exam exam) {
        if (exam.getDbId() > 0) {
            return update(exam);
        } else {
            return insert(exam);
        }
    }

    private static boolean insert(Exam exam) {
        String sql = """
            INSERT INTO exams
              (subject, grade, total_marks, duration, title, description,
               exam_code, is_live, live_window, schedule_details,
               live_end_millis, scheduled_start_millis, scheduled_end_millis)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, exam);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                exam.setDbId(keys.getInt(1));
                saveQuestionLinks(exam);
                System.out.println("[ExamDAO] Inserted exam id=" + exam.getDbId());
                return true;
            }
        } catch (SQLException e) { System.err.println("[ExamDAO] insert: " + e.getMessage()); }
        return false;
    }

    private static boolean update(Exam exam) {
        String sql = """
            UPDATE exams SET
              subject=?, grade=?, total_marks=?, duration=?, title=?,
              description=?, exam_code=?, is_live=?, live_window=?,
              schedule_details=?, live_end_millis=?,
              scheduled_start_millis=?, scheduled_end_millis=?
            WHERE id=?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            fillStatement(ps, exam);
            ps.setInt(14, exam.getDbId());
            ps.executeUpdate();
            saveQuestionLinks(exam); // re-sync question links
            System.out.println("[ExamDAO] Updated exam id=" + exam.getDbId());
            return true;
        } catch (SQLException e) { System.err.println("[ExamDAO] update: " + e.getMessage()); }
        return false;
    }

    // ── Bind all 13 exam fields to a PreparedStatement ────────
    private static void fillStatement(PreparedStatement ps, Exam exam) throws SQLException {
        ps.setString(1,  exam.getSubject());
        ps.setInt(2,     exam.getGrade());
        ps.setDouble(3,  exam.getTotalMarks());
        ps.setString(4,  exam.getDuration());
        ps.setString(5,  exam.getTitle());
        ps.setString(6,  exam.getDescription());
        ps.setString(7,  exam.getExamCode());
        ps.setInt(8,     exam.isLive() ? 1 : 0);
        ps.setString(9,  exam.getLiveWindow());
        ps.setString(10, exam.getScheduleDetails());
        ps.setLong(11,   exam.getLiveEndMillis());
        ps.setLong(12,   exam.getScheduledStartMillis());
        ps.setLong(13,   exam.getScheduledEndMillis());
    }

    // ── Sync exam_questions rows for this exam ─────────────────
    // Delete all existing links, then re-insert current ones.
    // Simple and safe for the question counts we're dealing with.
    private static void saveQuestionLinks(Exam exam) {
        try {
            // Delete old links
            try (PreparedStatement del = DatabaseManager.getConnection()
                    .prepareStatement("DELETE FROM exam_questions WHERE exam_id=?")) {
                del.setInt(1, exam.getDbId());
                del.executeUpdate();
            }

            // Insert current links
            String sql = "INSERT INTO exam_questions (exam_id, question_id, marks) VALUES (?,?,?)";
            try (PreparedStatement ins = DatabaseManager.getConnection().prepareStatement(sql)) {
                for (var entry : exam.getQuestionsMap().entrySet()) {
                    Question q = entry.getKey();
                    if (q.getDbId() < 1) {
                        // Question not yet in DB — save it first
                        QuestionDAO.save(q);
                    }
                    if (q.getDbId() > 0) {
                        ins.setInt(1, exam.getDbId());
                        ins.setInt(2, q.getDbId());
                        ins.setDouble(3, entry.getValue());
                        ins.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) { System.err.println("[ExamDAO] saveQuestionLinks: " + e.getMessage()); }
    }

    // ╔══════════════════════════════════════════════════════╗
    //  LOAD ALL
    // ╚══════════════════════════════════════════════════════╝
    public static ArrayList<Exam> loadAll() {
        ArrayList<Exam> list = new ArrayList<>();
        String sql = "SELECT * FROM exams ORDER BY id ASC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Exam e = rowToExam(rs);
                if (e != null) list.add(e);
            }
        } catch (SQLException e) { System.err.println("[ExamDAO] loadAll: " + e.getMessage()); }
        System.out.println("[ExamDAO] Loaded " + list.size() + " exams from DB.");
        return list;
    }

    // ── Map a DB row → Exam object ─────────────────────────────
    private static Exam rowToExam(ResultSet rs) throws SQLException {
        int    id      = rs.getInt("id");
        String subject = rs.getString("subject");
        int    grade   = rs.getInt("grade");
        double marks   = rs.getDouble("total_marks");
        String dur     = rs.getString("duration");

        // Load question links
        HashMap<Question, Double> qMap = loadQuestionLinks(id);

        Exam e = new Exam(subject, grade, marks, dur, qMap);
        e.setDbId(id);
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setExamCode(rs.getString("exam_code"));
        e.setLive(rs.getInt("is_live") == 1);
        e.setLiveWindow(rs.getString("live_window"));
        e.setScheduleDetails(rs.getString("schedule_details"));
        e.setLiveEndMillis(rs.getLong("live_end_millis"));
        e.setScheduledStartMillis(rs.getLong("scheduled_start_millis"));
        e.setScheduledEndMillis(rs.getLong("scheduled_end_millis"));
        return e;
    }

    // ── Load question-marks pairs for one exam ─────────────────
    private static HashMap<Question, Double> loadQuestionLinks(int examId) {
        HashMap<Question, Double> map = new HashMap<>();
        String sql = """
            SELECT q.*, eq.marks
            FROM exam_questions eq
            JOIN questions q ON q.id = eq.question_id
            WHERE eq.exam_id = ?
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Question q = QuestionDAO.rowToQuestion(rs);
                if (q != null) map.put(q, rs.getDouble("marks"));
            }
        } catch (SQLException e) { System.err.println("[ExamDAO] loadQuestionLinks: " + e.getMessage()); }
        return map;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  DELETE
    // ╚══════════════════════════════════════════════════════╝
    public static boolean delete(Exam exam) {
        if (exam.getDbId() < 1) return false;
        // CASCADE on exam_questions handles junction rows automatically
        String sql = "DELETE FROM exams WHERE id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, exam.getDbId());
            ps.executeUpdate();
            System.out.println("[ExamDAO] Deleted exam id=" + exam.getDbId());
            return true;
        } catch (SQLException e) { System.err.println("[ExamDAO] delete: " + e.getMessage()); }
        return false;
    }
}