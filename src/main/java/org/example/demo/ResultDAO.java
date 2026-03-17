package org.example.demo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// ═══════════════════════════════════════════════════════════
//  ResultDAO.java
//  Saves and loads ExamResult objects.
//  Best-score rule: if a student retakes the same exam,
//  only the attempt with the highest score is kept/shown.
// ═══════════════════════════════════════════════════════════
public class ResultDAO {

    // ╔══════════════════════════════════════════════════════╗
    //  TABLE CREATION  (called from DatabaseManager.init)
    // ╚══════════════════════════════════════════════════════╝
    public static void createTable() throws SQLException {
        Statement st = DatabaseManager.getConnection().createStatement();
        st.execute("""
            CREATE TABLE IF NOT EXISTS exam_results (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id    TEXT    NOT NULL,
                exam_id       INTEGER NOT NULL,
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
        // Add expire_at column to existing DBs that were created before this version
        try { st.execute("ALTER TABLE announcements ADD COLUMN expire_at INTEGER NOT NULL DEFAULT 0;"); }
        catch (SQLException ignored) {}  // column already exists

        st.close();
        System.out.println("[DB] Result / announcement tables verified/created.");
    }

    // ╔══════════════════════════════════════════════════════╗
    //  SAVE RESULT  — best-score rule:
    //  If a previous result exists for the same student+exam,
    //  only replace it when the new score is strictly higher.
    // ╚══════════════════════════════════════════════════════╝
    public static void save(ExamResult r) {
        // Check for an existing result for this student + exam
        String sel = "SELECT id, score FROM exam_results WHERE student_id=? AND exam_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sel)) {
            ps.setString(1, r.studentId);
            ps.setInt(2, r.examId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int    existId    = rs.getInt("id");
                double existScore = rs.getDouble("score");
                if (r.score > existScore) {
                    // Update with better score
                    String upd = """
                        UPDATE exam_results SET score=?, total_marks=?, correct=?,
                        total_q=?, taken_at=?, exam_title=?, exam_subject=?, exam_grade=?
                        WHERE id=?
                    """;
                    try (PreparedStatement up = DatabaseManager.getConnection().prepareStatement(upd)) {
                        up.setDouble(1, r.score);
                        up.setDouble(2, r.totalMarks);
                        up.setInt(3, r.correct);
                        up.setInt(4, r.totalQ);
                        up.setLong(5, r.takenAt);
                        up.setString(6, r.examTitle);
                        up.setString(7, r.examSubject);
                        up.setInt(8, r.examGrade);
                        up.setInt(9, existId);
                        up.executeUpdate();
                        System.out.println("[ResultDAO] Updated best score for student=" + r.studentId + " exam=" + r.examId);
                    }
                } else {
                    System.out.println("[ResultDAO] Kept existing best score for student=" + r.studentId + " exam=" + r.examId);
                }
                return;
            }
        } catch (SQLException e) { System.err.println("[ResultDAO] save check: " + e.getMessage()); }

        // No existing row — insert
        String ins = """
            INSERT INTO exam_results
              (student_id,exam_id,exam_title,exam_subject,exam_grade,score,total_marks,correct,total_q,taken_at)
            VALUES (?,?,?,?,?,?,?,?,?,?)
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(ins)) {
            ps.setString(1, r.studentId);
            ps.setInt(2, r.examId);
            ps.setString(3, r.examTitle);
            ps.setString(4, r.examSubject);
            ps.setInt(5, r.examGrade);
            ps.setDouble(6, r.score);
            ps.setDouble(7, r.totalMarks);
            ps.setInt(8, r.correct);
            ps.setInt(9, r.totalQ);
            ps.setLong(10, r.takenAt);
            ps.executeUpdate();
            System.out.println("[ResultDAO] Inserted result for student=" + r.studentId + " exam=" + r.examId);
        } catch (SQLException e) { System.err.println("[ResultDAO] save insert: " + e.getMessage()); }
    }

    // ╔══════════════════════════════════════════════════════╗
    //  LOAD FOR ONE STUDENT  (best scores only, newest first)
    // ╚══════════════════════════════════════════════════════╝
    public static List<ExamResult> loadForStudent(String studentId) {
        List<ExamResult> list = new ArrayList<>();
        String sql = """
            SELECT * FROM exam_results WHERE student_id=? ORDER BY taken_at DESC
        """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rowToResult(rs));
        } catch (SQLException e) { System.err.println("[ResultDAO] loadForStudent: " + e.getMessage()); }
        return list;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  LOAD ALL  (for teacher leaderboard / analytics)
    // ╚══════════════════════════════════════════════════════╝
    public static List<ExamResult> loadAll() {
        List<ExamResult> list = new ArrayList<>();
        String sql = "SELECT * FROM exam_results ORDER BY taken_at DESC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(rowToResult(rs));
        } catch (SQLException e) { System.err.println("[ResultDAO] loadAll: " + e.getMessage()); }
        return list;
    }

    // ╔══════════════════════════════════════════════════════╗
    //  LOAD FOR ONE EXAM  (for teacher leaderboard)
    // ╚══════════════════════════════════════════════════════╝
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

    /** Delete all announcements whose expire_at has passed (> 0 and <= now). */
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