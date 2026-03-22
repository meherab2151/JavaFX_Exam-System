package org.example.demo;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ═══════════════════════════════════════════════════════════
//  ResultDAO.java
//  Saves and loads ExamResult objects.
//  One-attempt-only rule per student per exam.
//  Auto-saves in-progress answers so student can resume.
//  Manages student_exam_codes for dashboard persistence.
// ═══════════════════════════════════════════════════════════
public class ResultDAO {

    public static void createTable() throws SQLException {
        Statement st = DatabaseManager.getConnection().createStatement();

        st.execute("""
            CREATE TABLE IF NOT EXISTS exam_results (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id    TEXT    NOT NULL,
                exam_id       INTEGER NOT NULL,
                exam_code     TEXT    NOT NULL DEFAULT '',
                exam_title    TEXT,
                exam_subject  TEXT,
                exam_grade    INTEGER,
                score         REAL    NOT NULL,
                total_marks   REAL    NOT NULL,
                correct       INTEGER NOT NULL,
                total_q       INTEGER NOT NULL,
                taken_at      INTEGER NOT NULL,
                FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
            );
        """);
        // Migration: add exam_code to existing DBs that were created before this version
        try { st.execute("ALTER TABLE exam_results ADD COLUMN exam_code TEXT NOT NULL DEFAULT '';"); }
        catch (SQLException ignored) {} // column already exists

        // Announcements table
        st.execute("""
            CREATE TABLE IF NOT EXISTS announcements (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                title      TEXT NOT NULL,
                body       TEXT NOT NULL,
                color      TEXT NOT NULL DEFAULT '#2563eb',
                created_at INTEGER NOT NULL,
                expire_at  INTEGER NOT NULL DEFAULT 0
            );
        """);
        try { st.execute("ALTER TABLE announcements ADD COLUMN expire_at INTEGER NOT NULL DEFAULT 0;"); }
        catch (SQLException ignored) {}

        // student_exam_codes: persists which exams a student has unlocked
        st.execute("""
            CREATE TABLE IF NOT EXISTS student_exam_codes (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id TEXT    NOT NULL,
                exam_id    INTEGER NOT NULL,
                status     TEXT    NOT NULL DEFAULT 'scheduled',
                added_at   INTEGER NOT NULL DEFAULT 0,
                UNIQUE(student_id, exam_id),
                FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
            );
        """);

        // ── exam_in_progress: auto-save answers during exam ──────────────
        // answers stored as pipe-separated "questionIndex:answer" pairs
        st.execute("""
            CREATE TABLE IF NOT EXISTS exam_in_progress (
                id             INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id     TEXT    NOT NULL,
                exam_id        INTEGER NOT NULL,
                answers_json   TEXT    NOT NULL DEFAULT '',
                flagged_json   TEXT    NOT NULL DEFAULT '',
                started_at     INTEGER NOT NULL DEFAULT 0,
                UNIQUE(student_id, exam_id),
                FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
            );
        """);

        st.close();
        System.out.println("[DB] All result/progress tables verified/created.");
    }

    // ══════════════════════════════════════════════════════
    //  SAVE RESULT
    //  Rule: one attempt per (student, exam, exam_code) tuple.
    //  If exam is relaunched with a NEW code, student can retake.
    //  If same code already submitted, block duplicate.
    // ══════════════════════════════════════════════════════
    public static void save(ExamResult r) {
        // Block if same student already submitted this exam with the SAME code
        String sel = "SELECT id FROM exam_results WHERE student_id=? AND exam_id=? AND exam_code=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sel)) {
            ps.setString(1, r.studentId);
            ps.setInt(2, r.examId);
            ps.setString(3, r.examCode != null ? r.examCode : "");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("[ResultDAO] Already submitted with this code — blocking for student=" + r.studentId + " exam=" + r.examId);
                return;
            }
        } catch (SQLException e) { System.err.println("[ResultDAO] save check: " + e.getMessage()); }

        String ins = """
            INSERT INTO exam_results
              (student_id,exam_id,exam_code,exam_title,exam_subject,exam_grade,score,total_marks,correct,total_q,taken_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(ins)) {
            ps.setString(1, r.studentId);
            ps.setInt(2, r.examId);
            ps.setString(3, r.examCode != null ? r.examCode : "");
            ps.setString(4, r.examTitle);
            ps.setString(5, r.examSubject);
            ps.setInt(6, r.examGrade);
            ps.setDouble(7, r.score);
            ps.setDouble(8, r.totalMarks);
            ps.setInt(9, r.correct);
            ps.setInt(10, r.totalQ);
            ps.setLong(11, r.takenAt);
            ps.executeUpdate();
            System.out.println("[ResultDAO] Inserted result for student=" + r.studentId + " exam=" + r.examId);
        } catch (SQLException e) { System.err.println("[ResultDAO] save insert: " + e.getMessage()); }

        // Clear in-progress answers after submission
        clearInProgress(r.studentId, r.examId);
    }

    public static List<ExamResult> loadForStudent(String studentId) {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT * FROM exam_results WHERE student_id=? ORDER BY taken_at DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rowToResult(rs));
        } catch (SQLException e) { System.err.println("[ResultDAO] loadForStudent: " + e.getMessage()); }
        return list;
    }

    public static List<ExamResult> loadAll() {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT * FROM exam_results ORDER BY taken_at DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(rowToResult(rs));
        } catch (SQLException e) { System.err.println("[ResultDAO] loadAll: " + e.getMessage()); }
        return list;
    }

    public static List<ExamResult> loadForExam(int examId) {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT * FROM exam_results WHERE exam_id=? ORDER BY score DESC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rowToResult(rs));
        } catch (SQLException e) { System.err.println("[ResultDAO] loadForExam: " + e.getMessage()); }
        return list;
    }

    private static ExamResult rowToResult(ResultSet rs) throws SQLException {
        ExamResult r = new ExamResult();
        r.id         = rs.getInt("id");
        r.studentId  = rs.getString("student_id");
        r.examId     = rs.getInt("exam_id");
        r.examCode   = rs.getString("exam_code");
        r.examTitle   = rs.getString("exam_title");
        r.examSubject = rs.getString("exam_subject");
        r.examGrade   = rs.getInt("exam_grade");
        r.score       = rs.getDouble("score");
        r.totalMarks  = rs.getDouble("total_marks");
        r.correct     = rs.getInt("correct");
        r.totalQ      = rs.getInt("total_q");
        r.takenAt     = rs.getLong("taken_at");
        return r;
    }

    // ══════════════════════════════════════════════════════
    //  STUDENT EXAM CODES  —  dashboard persistence
    // ══════════════════════════════════════════════════════

    public static void saveStudentExamCode(String studentId, int examId, String status) {
        String sql = """
            INSERT INTO student_exam_codes (student_id, exam_id, status, added_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(student_id, exam_id) DO UPDATE SET status=excluded.status
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ps.setString(3, status);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[ResultDAO] saveStudentExamCode: " + e.getMessage()); }
    }

    public static List<int[]> loadStudentExamCodes(String studentId) {
        List<int[]> list = new ArrayList<>();
        String sql = "SELECT exam_id, added_at FROM student_exam_codes WHERE student_id=? ORDER BY added_at ASC";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new int[]{rs.getInt("exam_id"), (int) rs.getLong("added_at")});
        } catch (SQLException e) { System.err.println("[ResultDAO] loadStudentExamCodes: " + e.getMessage()); }
        return list;
    }

    public static void removeStudentExamCode(String studentId, int examId) {
        String sql = "DELETE FROM student_exam_codes WHERE student_id=? AND exam_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[ResultDAO] removeStudentExamCode: " + e.getMessage()); }
    }

    /** Returns true if student already submitted THIS specific exam code */
    public static boolean hasResult(String studentId, int examId, String examCode) {
        String sql = "SELECT 1 FROM exam_results WHERE student_id=? AND exam_id=? AND exam_code=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ps.setString(3, examCode != null ? examCode : "");
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { System.err.println("[ResultDAO] hasResult: " + e.getMessage()); }
        return false;
    }

    /** Returns true if student has submitted this exam with ANY code (for results page) */
    public static boolean hasAnyResult(String studentId, int examId) {
        String sql = "SELECT 1 FROM exam_results WHERE student_id=? AND exam_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { System.err.println("[ResultDAO] hasAnyResult: " + e.getMessage()); }
        return false;
    }

    public static ExamResult loadSingleResult(String studentId, int examId) {
        String sql = "SELECT * FROM exam_results WHERE student_id=? AND exam_id=? ORDER BY taken_at DESC LIMIT 1";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rowToResult(rs);
        } catch (SQLException e) { System.err.println("[ResultDAO] loadSingleResult: " + e.getMessage()); }
        return null;
    }

    // ══════════════════════════════════════════════════════
    //  IN-PROGRESS EXAM AUTO-SAVE
    //  answers stored as "idx:answer" joined by "||"
    //  flagged stored as comma-separated indices
    // ══════════════════════════════════════════════════════

    /** Save current answers and flagged state to DB for resume */
    public static void saveInProgress(String studentId, int examId,
                                       Map<Integer, String> answers,
                                       java.util.Set<Integer> flagged) {
        StringBuilder ansBuilder = new StringBuilder();
        for (Map.Entry<Integer, String> e : answers.entrySet()) {
            if (ansBuilder.length() > 0) ansBuilder.append("||");
            ansBuilder.append(e.getKey()).append(":").append(e.getValue().replace("||", "").replace(":", "_"));
        }
        StringBuilder flagBuilder = new StringBuilder();
        for (int idx : flagged) {
            if (flagBuilder.length() > 0) flagBuilder.append(",");
            flagBuilder.append(idx);
        }
        String sql = """
            INSERT INTO exam_in_progress (student_id, exam_id, answers_json, flagged_json, started_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(student_id, exam_id) DO UPDATE SET
                answers_json=excluded.answers_json,
                flagged_json=excluded.flagged_json
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ps.setString(3, ansBuilder.toString());
            ps.setString(4, flagBuilder.toString());
            ps.setLong(5, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[ResultDAO] saveInProgress: " + e.getMessage()); }
    }

    /** Load saved answers for resume. Returns null if no in-progress record. */
    public static Map<Integer, String> loadInProgressAnswers(String studentId, int examId) {
        String sql = "SELECT answers_json FROM exam_in_progress WHERE student_id=? AND exam_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<Integer, String> answers = new HashMap<>();
                String raw = rs.getString("answers_json");
                if (raw != null && !raw.isEmpty()) {
                    for (String pair : raw.split("\\|\\|")) {
                        int colon = pair.indexOf(':');
                        if (colon > 0) {
                            try {
                                int idx = Integer.parseInt(pair.substring(0, colon));
                                String ans = pair.substring(colon + 1);
                                answers.put(idx, ans);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                return answers;
            }
        } catch (SQLException e) { System.err.println("[ResultDAO] loadInProgressAnswers: " + e.getMessage()); }
        return null;
    }

    /** Load saved flagged indices for resume */
    public static java.util.Set<Integer> loadInProgressFlagged(String studentId, int examId) {
        java.util.Set<Integer> flagged = new java.util.HashSet<>();
        String sql = "SELECT flagged_json FROM exam_in_progress WHERE student_id=? AND exam_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String raw = rs.getString("flagged_json");
                if (raw != null && !raw.isEmpty()) {
                    for (String s : raw.split(",")) {
                        try { flagged.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } catch (SQLException e) { System.err.println("[ResultDAO] loadInProgressFlagged: " + e.getMessage()); }
        return flagged;
    }

    /** Check if student has an in-progress exam */
    public static boolean hasInProgress(String studentId, int examId) {
        String sql = "SELECT 1 FROM exam_in_progress WHERE student_id=? AND exam_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { System.err.println("[ResultDAO] hasInProgress: " + e.getMessage()); }
        return false;
    }

    /** Clear in-progress after submission */
    public static void clearInProgress(String studentId, int examId) {
        String sql = "DELETE FROM exam_in_progress WHERE student_id=? AND exam_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setInt(2, examId);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[ResultDAO] clearInProgress: " + e.getMessage()); }
    }

    // ══════════════════════════════════════════════════════
    //  ANNOUNCEMENTS
    // ══════════════════════════════════════════════════════
    public static void saveAnnouncement(Announcement a) {
        String sql = "INSERT INTO announcements (title,body,color,created_at,expire_at) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.title);
            ps.setString(2, a.body);
            ps.setString(3, a.color);
            ps.setLong(4, a.createdAt);
            ps.setLong(5, a.expireAt);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) a.id = keys.getInt(1);
        } catch (SQLException e) { System.err.println("[ResultDAO] saveAnnouncement: " + e.getMessage()); }
    }

    public static void deleteAnnouncement(int id) {
        try (PreparedStatement ps = DatabaseManager.getConnection()
                .prepareStatement("DELETE FROM announcements WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[ResultDAO] deleteAnnouncement: " + e.getMessage()); }
    }

    public static void deleteExpired() {
        long now = System.currentTimeMillis();
        String sql = "DELETE FROM announcements WHERE expire_at > 0 AND expire_at <= ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setLong(1, now);
            int deleted = ps.executeUpdate();
            if (deleted > 0) System.out.println("[ResultDAO] Deleted " + deleted + " expired announcement(s).");
        } catch (SQLException e) { System.err.println("[ResultDAO] deleteExpired: " + e.getMessage()); }
    }

    public static List<Announcement> loadAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT * FROM announcements ORDER BY created_at DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Announcement a = new Announcement();
                a.id        = rs.getInt("id");
                a.title     = rs.getString("title");
                a.body      = rs.getString("body");
                a.color     = rs.getString("color");
                a.createdAt = rs.getLong("created_at");
                try { a.expireAt = rs.getLong("expire_at"); } catch (SQLException ignored) {}
                list.add(a);
            }
        } catch (SQLException e) { System.err.println("[ResultDAO] loadAnnouncements: " + e.getMessage()); }
        return list;
    }
}
