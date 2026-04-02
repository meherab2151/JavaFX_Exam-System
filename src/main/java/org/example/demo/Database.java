package org.example.demo;

import java.sql.*;
import java.util.*;

public final class Database {

    private static final String URL = "jdbc:sqlite:eduexam.db";
    private static Connection conn;

    private Database() {}

    public static void init() {
        try {
            conn = DriverManager.getConnection(URL);
            conn.createStatement().execute("PRAGMA foreign_keys = ON;");
            createAllTables();
            System.out.println("[DB] Connected → eduexam.db");
        } catch (SQLException e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() {
        if (conn == null) throw new IllegalStateException("Database not initialised.");
        return conn;
    }

    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) { conn.close(); System.out.println("[DB] Closed."); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void createAllTables() throws SQLException {
        Statement st = conn.createStatement();

        st.execute("""
            CREATE TABLE IF NOT EXISTS teachers (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                email     TEXT NOT NULL UNIQUE,
                password  TEXT NOT NULL
            );""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS students (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id TEXT NOT NULL UNIQUE,
                name       TEXT NOT NULL,
                email      TEXT NOT NULL,
                password   TEXT NOT NULL
            );""");

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
            );""");

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
            );""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS exam_questions (
                exam_id     INTEGER NOT NULL,
                question_id INTEGER NOT NULL,
                marks       REAL    NOT NULL,
                PRIMARY KEY (exam_id, question_id),
                FOREIGN KEY (exam_id)     REFERENCES exams(id)     ON DELETE CASCADE,
                FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
            );""");

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
            );""");
        try { st.execute("ALTER TABLE exam_results ADD COLUMN exam_code TEXT NOT NULL DEFAULT '';"); }
        catch (SQLException ignored) {}

        st.execute("""
            CREATE TABLE IF NOT EXISTS announcements (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                title      TEXT NOT NULL,
                body       TEXT NOT NULL,
                color      TEXT NOT NULL DEFAULT '#2563eb',
                created_at INTEGER NOT NULL,
                expire_at  INTEGER NOT NULL DEFAULT 0
            );""");
        try { st.execute("ALTER TABLE announcements ADD COLUMN expire_at INTEGER NOT NULL DEFAULT 0;"); }
        catch (SQLException ignored) {}

        st.execute("""
            CREATE TABLE IF NOT EXISTS student_exam_codes (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id TEXT    NOT NULL,
                exam_id    INTEGER NOT NULL,
                status     TEXT    NOT NULL DEFAULT 'scheduled',
                added_at   INTEGER NOT NULL DEFAULT 0,
                UNIQUE(student_id, exam_id),
                FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
            );""");

        st.execute("""
            CREATE TABLE IF NOT EXISTS exam_in_progress (
                id           INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id   TEXT    NOT NULL,
                exam_id      INTEGER NOT NULL,
                answers_json TEXT    NOT NULL DEFAULT '',
                flagged_json TEXT    NOT NULL DEFAULT '',
                started_at   INTEGER NOT NULL DEFAULT 0,
                UNIQUE(student_id, exam_id),
                FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
            );""");

        st.close();
        System.out.println("[DB] All tables verified/created.");
    }

    public static boolean registerTeacher(String fullName, String email, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO teachers (full_name, email, password) VALUES (?,?,?)")) {
            ps.setString(1, fullName); ps.setString(2, email); ps.setString(3, password);
            ps.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("[DB] registerTeacher: " + e.getMessage()); return false; }
    }

    public static Teacher loginTeacher(String emailOrName, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT full_name,email,password FROM teachers WHERE (email=? OR full_name=?) AND password=?")) {
            ps.setString(1, emailOrName); ps.setString(2, emailOrName); ps.setString(3, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Teacher(rs.getString("full_name"), rs.getString("email"), rs.getString("password"));
        } catch (SQLException e) { System.err.println("[DB] loginTeacher: " + e.getMessage()); }
        return null;
    }

    public static List<Teacher> loadAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT full_name,email,password FROM teachers")) {
            while (rs.next())
                list.add(new Teacher(rs.getString("full_name"), rs.getString("email"), rs.getString("password")));
        } catch (SQLException e) { System.err.println("[DB] loadAllTeachers: " + e.getMessage()); }
        return list;
    }

    public static boolean teacherEmailExists(String email) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM teachers WHERE email=?")) {
            ps.setString(1, email); return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public static boolean registerStudent(String studentId, String name, String email, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO students (student_id,name,email,password) VALUES (?,?,?,?)")) {
            ps.setString(1, studentId); ps.setString(2, name);
            ps.setString(3, email);     ps.setString(4, password);
            ps.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("[DB] registerStudent: " + e.getMessage()); return false; }
    }

    public static Student loginStudent(String idOrEmail, String password) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT student_id,name,email,password FROM students WHERE (student_id=? OR email=?) AND password=?")) {
            ps.setString(1, idOrEmail); ps.setString(2, idOrEmail); ps.setString(3, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Student(rs.getString("student_id"), rs.getString("name"),
                    rs.getString("email"), rs.getString("password"));
        } catch (SQLException e) { System.err.println("[DB] loginStudent: " + e.getMessage()); }
        return null;
    }

    public static List<Student> loadAllStudents() {
        List<Student> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT student_id,name,email,password FROM students")) {
            while (rs.next())
                list.add(new Student(rs.getString("student_id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("password")));
        } catch (SQLException e) { System.err.println("[DB] loadAllStudents: " + e.getMessage()); }
        return list;
    }

    public static boolean studentIdExists(String studentId) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM students WHERE student_id=?")) {
            ps.setString(1, studentId); return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public static int saveQuestion(Question q) {
        if (q instanceof MCQ mcq)           return saveMCQ(mcq);
        if (q instanceof TextQuestion tq)   return saveText(tq);
        if (q instanceof RangeQuestion rq)  return saveRange(rq);
        return -1;
    }

    private static int saveMCQ(MCQ q) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO questions (type,subject,grade,question_text,options,correct_index) VALUES ('MCQ',?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getSubject()); ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText()); ps.setString(4, String.join("|", q.getOptions()));
            ps.setInt(5, q.getCorrectIndex()); ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys(); if (k.next()) { int id=k.getInt(1); q.setDbId(id); return id; }
        } catch (SQLException e) { System.err.println("[DB] saveMCQ: " + e.getMessage()); }
        return -1;
    }

    private static int saveText(TextQuestion q) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO questions (type,subject,grade,question_text,answer) VALUES ('TEXT',?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getSubject()); ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText()); ps.setDouble(4, q.getAnswer());
            ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys(); if (k.next()) { int id=k.getInt(1); q.setDbId(id); return id; }
        } catch (SQLException e) { System.err.println("[DB] saveText: " + e.getMessage()); }
        return -1;
    }

    private static int saveRange(RangeQuestion q) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO questions (type,subject,grade,question_text,min_val,max_val) VALUES ('RANGE',?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, q.getSubject()); ps.setInt(2, q.getGrade());
            ps.setString(3, q.getQuestionText()); ps.setDouble(4, q.getMin()); ps.setDouble(5, q.getMax());
            ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys(); if (k.next()) { int id=k.getInt(1); q.setDbId(id); return id; }
        } catch (SQLException e) { System.err.println("[DB] saveRange: " + e.getMessage()); }
        return -1;
    }

    public static boolean updateQuestion(Question q) {
        if (q.getDbId() < 1) return false;
        try {
            if (q instanceof MCQ mcq) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE questions SET question_text=?,options=?,correct_index=? WHERE id=?")) {
                    ps.setString(1, mcq.getQuestionText()); ps.setString(2, String.join("|", mcq.getOptions()));
                    ps.setInt(3, mcq.getCorrectIndex()); ps.setInt(4, mcq.getDbId()); ps.executeUpdate();
                }
            } else if (q instanceof TextQuestion tq) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE questions SET question_text=?,answer=? WHERE id=?")) {
                    ps.setString(1, tq.getQuestionText()); ps.setDouble(2, tq.getAnswer());
                    ps.setInt(3, tq.getDbId()); ps.executeUpdate();
                }
            } else if (q instanceof RangeQuestion rq) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE questions SET question_text=?,min_val=?,max_val=? WHERE id=?")) {
                    ps.setString(1, rq.getQuestionText()); ps.setDouble(2, rq.getMin());
                    ps.setDouble(3, rq.getMax()); ps.setInt(4, rq.getDbId()); ps.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) { System.err.println("[DB] updateQuestion: " + e.getMessage()); return false; }
    }

    public static boolean deleteQuestion(Question q) {
        String sql = q.getDbId() > 0
            ? "DELETE FROM questions WHERE id=?"
            : "DELETE FROM questions WHERE question_text=? AND subject=? AND grade=? AND type=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (q.getDbId() > 0) { ps.setInt(1, q.getDbId()); }
            else {
                ps.setString(1, q.getQuestionText()); ps.setString(2, q.getSubject());
                ps.setInt(3, q.getGrade());
                ps.setString(4, q instanceof MCQ ? "MCQ" : q instanceof TextQuestion ? "TEXT" : "RANGE");
            }
            ps.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("[DB] deleteQuestion: " + e.getMessage()); return false; }
    }

    public static List<Question> loadAllQuestions() {
        List<Question> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM questions ORDER BY id ASC")) {
            while (rs.next()) { Question q = rowToQuestion(rs); if (q != null) list.add(q); }
        } catch (SQLException e) { System.err.println("[DB] loadAllQuestions: " + e.getMessage()); }
        return list;
    }

    public static Question rowToQuestion(ResultSet rs) throws SQLException {
        int id = rs.getInt("id"); String type = rs.getString("type");
        String subject = rs.getString("subject"); int grade = rs.getInt("grade");
        String text = rs.getString("question_text");
        Question q = switch (type) {
            case "MCQ" -> {
                String raw = rs.getString("options");
                yield new MCQ(subject, grade, text,
                        raw != null ? raw.split("\\|") : new String[]{}, rs.getInt("correct_index"));
            }
            case "TEXT"  -> new TextQuestion(subject, grade, text, rs.getDouble("answer"));
            case "RANGE" -> new RangeQuestion(subject, grade, text, rs.getDouble("min_val"), rs.getDouble("max_val"));
            default -> null;
        };
        if (q != null) q.setDbId(id);
        return q;
    }

    public static boolean saveExam(Exam exam) {
        return exam.getDbId() > 0 ? updateExam(exam) : insertExam(exam);
    }

    private static boolean insertExam(Exam exam) {
        String sql = """
            INSERT INTO exams
              (subject,grade,total_marks,duration,title,description,exam_code,is_live,
               live_window,schedule_details,live_end_millis,scheduled_start_millis,scheduled_end_millis)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)""";
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillExam(ps, exam);
                ps.executeUpdate();
                ResultSet k = ps.getGeneratedKeys();
                if (k.next()) {
                    exam.setDbId(k.getInt(1));
                    if (saveQuestionLinks(exam)) {
                        conn.commit();
                        return true;
                    }
                }
            }
            conn.rollback();
        } catch (SQLException e) { System.err.println("[DB] insertExam: " + e.getMessage()); }
        finally {
            try { conn.setAutoCommit(oldAutoCommit); } catch (SQLException ignored) {}
        }
        return false;
    }

    private static boolean updateExam(Exam exam) {
        String sql = """
            UPDATE exams SET subject=?,grade=?,total_marks=?,duration=?,title=?,description=?,
            exam_code=?,is_live=?,live_window=?,schedule_details=?,live_end_millis=?,
            scheduled_start_millis=?,scheduled_end_millis=? WHERE id=?""";
        boolean oldAutoCommit = true;
        try {
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                fillExam(ps, exam);
                ps.setInt(14, exam.getDbId());
                ps.executeUpdate();
            }
            if (saveQuestionLinks(exam)) {
                conn.commit();
                return true;
            }
            conn.rollback();
        } catch (SQLException e) { System.err.println("[DB] updateExam: " + e.getMessage()); return false; }
        finally {
            try { conn.setAutoCommit(oldAutoCommit); } catch (SQLException ignored) {}
        }
        return false;
    }

    private static void fillExam(PreparedStatement ps, Exam e) throws SQLException {
        ps.setString(1, e.getSubject());    ps.setInt(2, e.getGrade());
        ps.setDouble(3, e.getTotalMarks()); ps.setString(4, e.getDuration());
        ps.setString(5, e.getTitle());      ps.setString(6, e.getDescription());
        ps.setString(7, e.getExamCode());   ps.setInt(8, e.isLive() ? 1 : 0);
        ps.setString(9, e.getLiveWindow()); ps.setString(10, e.getScheduleDetails());
        ps.setLong(11, e.getLiveEndMillis());
        ps.setLong(12, e.getScheduledStartMillis());
        ps.setLong(13, e.getScheduledEndMillis());
    }

    private static boolean saveQuestionLinks(Exam exam) {
        if (exam.getQuestionsMap().isEmpty()) {
            System.err.println("[DB] saveQuestionLinks: exam has no questions");
            return false;
        }
        try {
            System.out.println("[DB] saveQuestionLinks: examId=" + exam.getDbId() + ", questions=" + exam.getQuestionsMap().size());
            for (var entry : exam.getQuestionsMap().entrySet()) {
                System.out.println("[DB]   question: " + entry.getKey().getQuestionText() + ", marks=" + entry.getValue() + ", dbId=" + entry.getKey().getDbId());
            }
            try (PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM exam_questions WHERE exam_id=?")) {
                del.setInt(1, exam.getDbId()); del.executeUpdate();
            }
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO exam_questions (exam_id,question_id,marks) VALUES (?,?,?)")) {
                for (var entry : exam.getQuestionsMap().entrySet()) {
                    Question q = entry.getKey();
                    if (q.getDbId() < 1 && saveQuestion(q) < 1) return false;
                    if (q.getDbId() < 1) return false;
                    ins.setInt(1, exam.getDbId());
                    ins.setInt(2, q.getDbId());
                    ins.setDouble(3, entry.getValue());
                    ins.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("[DB] saveQuestionLinks: " + e.getMessage());
            return false;
        }
    }

    public static List<Exam> loadAllExams() {
        List<Exam> list = new ArrayList<>();
        normalizeExamStates();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM exams ORDER BY id ASC")) {
            while (rs.next()) { Exam e = rowToExam(rs); if (e != null) list.add(e); }
        } catch (SQLException e) { System.err.println("[DB] loadAllExams: " + e.getMessage()); }
        return list;
    }

    private static void normalizeExamStates() {
        long now = System.currentTimeMillis();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id,is_live,live_end_millis,scheduled_start_millis,scheduled_end_millis FROM exams");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                boolean isLive = rs.getInt("is_live") == 1;
                long liveEnd = rs.getLong("live_end_millis");
                long schedStart = rs.getLong("scheduled_start_millis");
                long schedEnd = rs.getLong("scheduled_end_millis");

                if (isLive && liveEnd > 0 && now >= liveEnd) {
                    resetExamToDraft(id);
                } else if (!isLive && schedStart > 0) {
                    if (schedEnd > 0 && now >= schedEnd) {
                        resetExamToDraft(id);
                    } else if (now >= schedStart) {
                        promoteScheduledExam(id, schedStart, schedEnd);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB] normalizeExamStates: " + e.getMessage());
        }
    }

    private static void promoteScheduledExam(int examId, long startMs, long endMs) {
        String liveWindow = "";
        if (endMs > startMs && startMs > 0) {
            long minutes = Math.max(1, (endMs - startMs) / 60_000L);
            liveWindow = (minutes / 60) + "h " + (minutes % 60) + "m";
        }
        try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE exams
                SET is_live=1, live_end_millis=?, live_window=?
                WHERE id=?""")) {
            ps.setLong(1, endMs);
            ps.setString(2, liveWindow);
            ps.setInt(3, examId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] promoteScheduledExam: " + e.getMessage());
        }
    }

    private static void resetExamToDraft(int examId) {
        try (PreparedStatement ps = conn.prepareStatement("""
                UPDATE exams
                SET is_live=0,
                    exam_code='',
                    live_window='',
                    schedule_details='',
                    live_end_millis=0,
                    scheduled_start_millis=0,
                    scheduled_end_millis=0
                WHERE id=?""")) {
            ps.setInt(1, examId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] resetExamToDraft: " + e.getMessage());
        }
    }

    private static Exam rowToExam(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        HashMap<Question, Double> qMap = loadQuestionLinks(id);
        Exam e = new Exam(rs.getString("subject"), rs.getInt("grade"),
                rs.getDouble("total_marks"), rs.getString("duration"), qMap);
        e.setDbId(id);
        e.setTitle(rs.getString("title"));           e.setDescription(rs.getString("description"));
        e.setExamCode(rs.getString("exam_code"));    e.setLive(rs.getInt("is_live") == 1);
        e.setLiveWindow(rs.getString("live_window")); e.setScheduleDetails(rs.getString("schedule_details"));
        e.setLiveEndMillis(rs.getLong("live_end_millis"));
        e.setScheduledStartMillis(rs.getLong("scheduled_start_millis"));
        e.setScheduledEndMillis(rs.getLong("scheduled_end_millis"));
        return e;
    }

    private static HashMap<Question, Double> loadQuestionLinks(int examId) {
        HashMap<Question, Double> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT q.*,eq.marks FROM exam_questions eq JOIN questions q ON q.id=eq.question_id WHERE eq.exam_id=?")) {
            ps.setInt(1, examId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) { Question q = rowToQuestion(rs); if (q != null) map.put(q, rs.getDouble("marks")); }
        } catch (SQLException e) { System.err.println("[DB] loadQuestionLinks: " + e.getMessage()); }
        return map;
    }

    public static boolean deleteExam(int dbId) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM exams WHERE id=?")) {
            ps.setInt(1, dbId); ps.executeUpdate(); return true;
        } catch (SQLException e) { System.err.println("[DB] deleteExam: " + e.getMessage()); return false; }
    }

    public static void saveResult(ExamResult r) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT id FROM exam_results WHERE student_id=? AND exam_id=? AND exam_code=?")) {
            ps.setString(1, r.studentId); ps.setInt(2, r.examId); ps.setString(3, nvl(r.examCode));
            if (ps.executeQuery().next()) { System.out.println("[DB] Duplicate submission blocked."); return; }
        } catch (SQLException e) { System.err.println("[DB] saveResult check: " + e.getMessage()); }

        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO exam_results
              (student_id,exam_id,exam_code,exam_title,exam_subject,exam_grade,
               score,total_marks,correct,total_q,taken_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)""")) {
            ps.setString(1, r.studentId);   ps.setInt(2, r.examId);
            ps.setString(3, nvl(r.examCode)); ps.setString(4, nvl(r.examTitle));
            ps.setString(5, nvl(r.examSubject)); ps.setInt(6, r.examGrade);
            ps.setDouble(7, r.score);       ps.setDouble(8, r.totalMarks);
            ps.setInt(9, r.correct);        ps.setInt(10, r.totalQ);
            ps.setLong(11, r.takenAt);      ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] saveResult insert: " + e.getMessage()); }
        clearInProgress(r.studentId, r.examId);
    }

    public static List<ExamResult> loadResultsForStudent(String sid) {
        return loadResults("SELECT * FROM exam_results WHERE student_id=? ORDER BY taken_at DESC", ps -> ps.setString(1, sid));
    }

    public static List<ExamResult> loadResultsForExam(int examId) {
        return loadResults("SELECT * FROM exam_results WHERE exam_id=? ORDER BY score DESC", ps -> ps.setInt(1, examId));
    }

    public static List<ExamResult> loadAllResults() {
        return loadResults("SELECT * FROM exam_results ORDER BY taken_at DESC", ps -> {});
    }

    @FunctionalInterface interface PsBinder { void bind(PreparedStatement ps) throws SQLException; }

    private static List<ExamResult> loadResults(String sql, PsBinder binder) {
        List<ExamResult> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.bind(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(rowToResult(rs));
        } catch (SQLException e) { System.err.println("[DB] loadResults: " + e.getMessage()); }
        return list;
    }

    private static ExamResult rowToResult(ResultSet rs) throws SQLException {
        ExamResult r = new ExamResult();
        r.id = rs.getInt("id"); r.studentId = rs.getString("student_id");
        r.examId = rs.getInt("exam_id"); r.examCode = rs.getString("exam_code");
        r.examTitle = rs.getString("exam_title"); r.examSubject = rs.getString("exam_subject");
        r.examGrade = rs.getInt("exam_grade"); r.score = rs.getDouble("score");
        r.totalMarks = rs.getDouble("total_marks"); r.correct = rs.getInt("correct");
        r.totalQ = rs.getInt("total_q"); r.takenAt = rs.getLong("taken_at");
        return r;
    }

    public static boolean hasResult(String sid, int examId, String code) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM exam_results WHERE student_id=? AND exam_id=? AND exam_code=?")) {
            ps.setString(1, sid); ps.setInt(2, examId); ps.setString(3, nvl(code));
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public static boolean hasAnyResult(String sid, int examId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM exam_results WHERE student_id=? AND exam_id=?")) {
            ps.setString(1, sid); ps.setInt(2, examId); return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public static ExamResult loadSingleResult(String sid, int examId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM exam_results WHERE student_id=? AND exam_id=? ORDER BY taken_at DESC LIMIT 1")) {
            ps.setString(1, sid); ps.setInt(2, examId);
            ResultSet rs = ps.executeQuery(); if (rs.next()) return rowToResult(rs);
        } catch (SQLException e) { System.err.println("[DB] loadSingleResult: " + e.getMessage()); }
        return null;
    }

    public static void saveStudentExamCode(String sid, int examId, String status) {
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO student_exam_codes (student_id,exam_id,status,added_at) VALUES (?,?,?,?)
            ON CONFLICT(student_id,exam_id) DO UPDATE SET status=excluded.status""")) {
            ps.setString(1, sid); ps.setInt(2, examId); ps.setString(3, status);
            ps.setLong(4, System.currentTimeMillis()); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] saveStudentExamCode: " + e.getMessage()); }
    }

    public static List<int[]> loadStudentExamCodes(String sid) {
        List<int[]> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT exam_id,added_at FROM student_exam_codes WHERE student_id=? ORDER BY added_at ASC")) {
            ps.setString(1, sid); ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new int[]{rs.getInt("exam_id"), (int)rs.getLong("added_at")});
        } catch (SQLException e) { System.err.println("[DB] loadStudentExamCodes: " + e.getMessage()); }
        return list;
    }

    public static void removeStudentExamCode(String sid, int examId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM student_exam_codes WHERE student_id=? AND exam_id=?")) {
            ps.setString(1, sid); ps.setInt(2, examId); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] removeStudentExamCode: " + e.getMessage()); }
    }

    public static void saveInProgress(String sid, int examId,
                                       Map<Integer,String> answers, Set<Integer> flagged) {
        StringBuilder ans = new StringBuilder();
        for (var e : answers.entrySet()) {
            if (ans.length() > 0) ans.append("||");
            ans.append(e.getKey()).append(':').append(e.getValue().replace("||","").replace(":","_"));
        }
        StringBuilder fl = new StringBuilder();
        for (int i : flagged) { if (fl.length()>0) fl.append(','); fl.append(i); }
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO exam_in_progress (student_id,exam_id,answers_json,flagged_json,started_at)
            VALUES (?,?,?,?,?)
            ON CONFLICT(student_id,exam_id) DO UPDATE SET
                answers_json=excluded.answers_json, flagged_json=excluded.flagged_json""")) {
            ps.setString(1, sid); ps.setInt(2, examId);
            ps.setString(3, ans.toString()); ps.setString(4, fl.toString());
            ps.setLong(5, System.currentTimeMillis()); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] saveInProgress: " + e.getMessage()); }
    }

    public static Map<Integer,String> loadInProgressAnswers(String sid, int examId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT answers_json FROM exam_in_progress WHERE student_id=? AND exam_id=?")) {
            ps.setString(1, sid); ps.setInt(2, examId); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<Integer,String> map = new HashMap<>();
                String raw = rs.getString("answers_json");
                if (raw != null && !raw.isEmpty()) {
                    for (String pair : raw.split("\\|\\|")) {
                        int colon = pair.indexOf(':');
                        if (colon > 0) { try { map.put(Integer.parseInt(pair.substring(0,colon)), pair.substring(colon+1)); } catch (NumberFormatException ignored) {} }
                    }
                }
                return map;
            }
        } catch (SQLException e) { System.err.println("[DB] loadInProgressAnswers: " + e.getMessage()); }
        return null;
    }

    public static Set<Integer> loadInProgressFlagged(String sid, int examId) {
        Set<Integer> set = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT flagged_json FROM exam_in_progress WHERE student_id=? AND exam_id=?")) {
            ps.setString(1, sid); ps.setInt(2, examId); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String raw = rs.getString("flagged_json");
                if (raw != null && !raw.isEmpty())
                    for (String s : raw.split(","))
                        try { set.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {}
            }
        } catch (SQLException e) { System.err.println("[DB] loadInProgressFlagged: " + e.getMessage()); }
        return set;
    }

    public static boolean hasInProgress(String sid, int examId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM exam_in_progress WHERE student_id=? AND exam_id=?")) {
            ps.setString(1, sid); ps.setInt(2, examId); return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public static void clearInProgress(String sid, int examId) {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM exam_in_progress WHERE student_id=? AND exam_id=?")) {
            ps.setString(1, sid); ps.setInt(2, examId); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] clearInProgress: " + e.getMessage()); }
    }

    public static void saveAnnouncement(Announcement a) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO announcements (title,body,color,created_at,expire_at) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.title); ps.setString(2, a.body); ps.setString(3, a.color);
            ps.setLong(4, a.createdAt); ps.setLong(5, a.expireAt); ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys(); if (k.next()) a.id = k.getInt(1);
        } catch (SQLException e) { System.err.println("[DB] saveAnnouncement: " + e.getMessage()); }
    }

    public static void deleteAnnouncement(int id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM announcements WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { System.err.println("[DB] deleteAnnouncement: " + e.getMessage()); }
    }

    public static void deleteExpiredAnnouncements() {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM announcements WHERE expire_at>0 AND expire_at<=?")) {
            ps.setLong(1, System.currentTimeMillis());
            int n = ps.executeUpdate();
            if (n > 0) System.out.println("[DB] Deleted " + n + " expired announcement(s).");
        } catch (SQLException e) { System.err.println("[DB] deleteExpired: " + e.getMessage()); }
    }

    public static List<Announcement> loadAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM announcements ORDER BY created_at DESC")) {
            while (rs.next()) {
                Announcement a = new Announcement();
                a.id = rs.getInt("id"); a.title = rs.getString("title");
                a.body = rs.getString("body"); a.color = rs.getString("color");
                a.createdAt = rs.getLong("created_at");
                try { a.expireAt = rs.getLong("expire_at"); } catch (SQLException ignored) {}
                list.add(a);
            }
        } catch (SQLException e) { System.err.println("[DB] loadAnnouncements: " + e.getMessage()); }
        return list;
    }

    private static String nvl(String s) { return s != null ? s : ""; }
}
