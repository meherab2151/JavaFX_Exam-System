package org.example.demo;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class RequestHandler implements Runnable {

    private static final Object REQUEST_LOCK = new Object();

    private final Socket socket;
    private final int    clientId;

    public RequestHandler(Socket socket, int clientId) {
        this.socket   = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        System.out.printf("[Server] Client #%d connected from %s%n",
                clientId, socket.getRemoteSocketAddress());
        try (
            BufferedReader in  = new BufferedReader(
                new InputStreamReader(socket.getInputStream(),  Protocol.CHARSET));
            PrintWriter    out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), Protocol.CHARSET), true)
        ) {
            EduExamServer.register(out);

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                Map<String, Object> req = JsonUtil.parse(line);
                String action = JsonUtil.getStr(req, "action");

                if (Protocol.BYE.equals(action)) break;

                String response = dispatch(action, JsonUtil.getMap(req, "payload"), out);
                out.println(response);
            }
        } catch (IOException e) {
            System.err.printf("[Server] Client #%d IO error: %s%n", clientId, e.getMessage());
        } finally {
            try {
            } catch (Exception ignored) {}
            try { socket.close(); } catch (IOException ignored) {}
            System.out.printf("[Server] Client #%d disconnected.%n", clientId);
        }
    }

    private String dispatch(String action, Map<String, Object> p, PrintWriter out) {
        synchronized (REQUEST_LOCK) {
            try {
                return switch (action) {

                case Protocol.PING -> JsonUtil.ok("\"pong\"");

                case Protocol.TEACHER_LOGIN    -> handleTeacherLogin(p);
                case Protocol.TEACHER_REGISTER -> handleTeacherRegister(p);
                case Protocol.STUDENT_LOGIN    -> handleStudentLogin(p);
                case Protocol.STUDENT_REGISTER -> handleStudentRegister(p);
                case Protocol.TEACHERS_LOAD    -> handleTeachersLoad();
                case Protocol.STUDENTS_LOAD    -> handleStudentsLoad();

                case Protocol.QUESTION_SAVE    -> handleQuestionSave(p);
                case Protocol.QUESTION_UPDATE  -> handleQuestionUpdate(p);
                case Protocol.QUESTION_DELETE  -> handleQuestionDelete(p);
                case Protocol.QUESTIONS_LOAD   -> handleQuestionsLoad();

                case Protocol.EXAM_SAVE        -> handleExamSave(p);
                case Protocol.EXAM_DELETE      -> handleExamDelete(p);
                case Protocol.EXAMS_LOAD       -> handleExamsLoad();

                case Protocol.RESULT_SAVE           -> handleResultSave(p);
                case Protocol.RESULTS_LOAD_STUDENT  -> handleResultsLoadStudent(p);
                case Protocol.RESULTS_LOAD_EXAM     -> handleResultsLoadExam(p);
                case Protocol.RESULTS_LOAD_ALL      -> handleResultsLoadAll();
                case Protocol.RESULT_HAS            -> handleResultHas(p);
                case Protocol.RESULT_HAS_ANY        -> handleResultHasAny(p);
                case Protocol.RESULT_LOAD_SINGLE    -> handleResultLoadSingle(p);

                case Protocol.PROGRESS_SAVE    -> handleProgressSave(p);
                case Protocol.PROGRESS_LOAD    -> handleProgressLoad(p);
                case Protocol.PROGRESS_FLAGGED -> handleProgressFlagged(p);
                case Protocol.PROGRESS_HAS     -> handleProgressHas(p);
                case Protocol.PROGRESS_CLEAR   -> handleProgressClear(p);

                case Protocol.EXAM_CODE_SAVE   -> handleExamCodeSave(p);
                case Protocol.EXAM_CODE_LOAD   -> handleExamCodeLoad(p);
                case Protocol.EXAM_CODE_REMOVE -> handleExamCodeRemove(p);

                case Protocol.ANNOUNCE_SAVE    -> handleAnnounceSave(p);
                case Protocol.ANNOUNCE_DELETE  -> handleAnnounceDelete(p);
                case Protocol.ANNOUNCE_LOAD    -> handleAnnounceLoad();
                case Protocol.ANNOUNCE_PURGE   -> handleAnnouncePurge();

                    default -> JsonUtil.err("Unknown action: " + action);
                };
            } catch (Exception e) {
                System.err.println("[Server] Error action=" + action + ": " + e.getMessage());
                return JsonUtil.err("Server error: " + e.getMessage());
            }
        }
    }

    private String handleTeacherLogin(Map<String, Object> p) {
        Teacher t = Database.loginTeacher(JsonUtil.getStr(p, "id"), JsonUtil.getStr(p, "password"));
        return t == null ? JsonUtil.err("Invalid credentials") : JsonUtil.ok(teacherToJson(t));
    }

    private String handleTeacherRegister(Map<String, Object> p) {
        boolean ok = Database.registerTeacher(JsonUtil.getStr(p,"fullName"),
                JsonUtil.getStr(p,"email"), JsonUtil.getStr(p,"password"));
        return ok ? JsonUtil.ok() : JsonUtil.err("Email already registered");
    }

    private String handleStudentLogin(Map<String, Object> p) {
        Student s = Database.loginStudent(JsonUtil.getStr(p,"id"), JsonUtil.getStr(p,"password"));
        return s == null ? JsonUtil.err("Invalid credentials") : JsonUtil.ok(studentToJson(s));
    }

    private String handleStudentRegister(Map<String, Object> p) {
        boolean ok = Database.registerStudent(JsonUtil.getStr(p,"studentId"), JsonUtil.getStr(p,"name"),
                JsonUtil.getStr(p,"email"), JsonUtil.getStr(p,"password"));
        return ok ? JsonUtil.ok() : JsonUtil.err("Student ID already registered");
    }

    private String handleTeachersLoad() {
        return JsonUtil.ok(toJsonArray(Database.loadAllTeachers(), this::teacherToJson));
    }

    private String handleStudentsLoad() {
        return JsonUtil.ok(toJsonArray(Database.loadAllStudents(), this::studentToJson));
    }

    private String handleQuestionSave(Map<String, Object> p) {
        Question q = questionFromMap(p);
        int id = Database.saveQuestion(q);
        return id < 0 ? JsonUtil.err("Failed to save question") : JsonUtil.ok(String.valueOf(id));
    }

    private String handleQuestionUpdate(Map<String, Object> p) {
        Question q = questionFromMap(p); q.setDbId(JsonUtil.getInt(p, "dbId"));
        return Database.updateQuestion(q) ? JsonUtil.ok() : JsonUtil.err("Update failed");
    }

    private String handleQuestionDelete(Map<String, Object> p) {
        Question q = questionFromMap(p); q.setDbId(JsonUtil.getInt(p, "dbId"));
        Database.deleteQuestion(q); return JsonUtil.ok();
    }

    private String handleQuestionsLoad() {
        return JsonUtil.ok(toJsonArray(Database.loadAllQuestions(), this::questionToJson));
    }

    private String handleExamSave(Map<String, Object> p) {
        Exam exam = examFromMap(p);

        boolean ok = Database.saveExam(exam);
        if (!ok) return JsonUtil.err("Exam save failed");

        EduExamServer.broadcastExamEvent(examToJson(exam));

        return JsonUtil.ok(String.valueOf(exam.getDbId()));
    }

    private String handleExamDelete(Map<String, Object> p) {
        int dbId = JsonUtil.getInt(p, "dbId");
        Database.deleteExam(dbId);
        String tombstone = JsonUtil.obj()
            .put("dbId",      dbId)
            .put("deleted",   true)
            .put("isLive",    false)
            .put("examCode",  "")
            .put("subject",   "")
            .put("title",     "")
            .build();
        EduExamServer.broadcastExamEvent(tombstone);
        return JsonUtil.ok();
    }

    private String handleExamsLoad() {
        return JsonUtil.ok(toJsonArray(Database.loadAllExams(), this::examToJson));
    }

    private String handleResultSave(Map<String, Object> p) {
        Database.saveResult(resultFromMap(p)); return JsonUtil.ok();
    }
    private String handleResultsLoadStudent(Map<String, Object> p) {
        return JsonUtil.ok(resultsToJsonArray(Database.loadResultsForStudent(JsonUtil.getStr(p,"studentId"))));
    }
    private String handleResultsLoadExam(Map<String, Object> p) {
        return JsonUtil.ok(resultsToJsonArray(Database.loadResultsForExam(JsonUtil.getInt(p,"examId"))));
    }
    private String handleResultsLoadAll() {
        return JsonUtil.ok(resultsToJsonArray(Database.loadAllResults()));
    }
    private String handleResultHas(Map<String, Object> p) {
        boolean has = Database.hasResult(JsonUtil.getStr(p,"studentId"),
                JsonUtil.getInt(p,"examId"), JsonUtil.getStr(p,"examCode"));
        return JsonUtil.ok(String.valueOf(has));
    }
    private String handleResultHasAny(Map<String, Object> p) {
        boolean has = Database.hasAnyResult(JsonUtil.getStr(p,"studentId"), JsonUtil.getInt(p,"examId"));
        return JsonUtil.ok(String.valueOf(has));
    }
    private String handleResultLoadSingle(Map<String, Object> p) {
        ExamResult r = Database.loadSingleResult(JsonUtil.getStr(p,"studentId"), JsonUtil.getInt(p,"examId"));
        return r == null ? JsonUtil.ok("null") : JsonUtil.ok(resultToJson(r));
    }

    private String handleProgressSave(Map<String, Object> p) {
        String sid = JsonUtil.getStr(p,"studentId"); int eid = JsonUtil.getInt(p,"examId");
        String answersRaw = JsonUtil.getStr(p,"answers");
        String flaggedRaw = JsonUtil.getStr(p,"flagged");
        Map<Integer,String> answers = new HashMap<>();
        if (!answersRaw.isBlank()) {
            for (String pair : answersRaw.split("\\|\\|")) {
                int c = pair.indexOf(':');
                if (c > 0) try { answers.put(Integer.parseInt(pair.substring(0,c)), pair.substring(c+1)); }
                           catch (NumberFormatException ignored) {}
            }
        }
        Set<Integer> flagged = new HashSet<>();
        if (!flaggedRaw.isBlank())
            for (String s : flaggedRaw.split(","))
                try { flagged.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {}
        Database.saveInProgress(sid, eid, answers, flagged);
        return JsonUtil.ok();
    }

    private String handleProgressLoad(Map<String, Object> p) {
        Map<Integer,String> answers = Database.loadInProgressAnswers(
                JsonUtil.getStr(p,"studentId"), JsonUtil.getInt(p,"examId"));
        if (answers == null) return JsonUtil.ok("null");
        StringBuilder sb = new StringBuilder();
        for (var e : answers.entrySet()) {
            if (sb.length()>0) sb.append("||");
            sb.append(e.getKey()).append(':').append(e.getValue());
        }
        return JsonUtil.ok("\"" + sb + "\"");
    }

    private String handleProgressFlagged(Map<String, Object> p) {
        Set<Integer> flagged = Database.loadInProgressFlagged(
                JsonUtil.getStr(p,"studentId"), JsonUtil.getInt(p,"examId"));
        StringBuilder sb = new StringBuilder("\"");
        for (int i : flagged) { if (sb.length()>1) sb.append(','); sb.append(i); }
        return JsonUtil.ok(sb.append('"').toString());
    }

    private String handleProgressHas(Map<String, Object> p) {
        return JsonUtil.ok(String.valueOf(Database.hasInProgress(
                JsonUtil.getStr(p,"studentId"), JsonUtil.getInt(p,"examId"))));
    }

    private String handleProgressClear(Map<String, Object> p) {
        Database.clearInProgress(JsonUtil.getStr(p,"studentId"), JsonUtil.getInt(p,"examId"));
        return JsonUtil.ok();
    }

    private String handleExamCodeSave(Map<String, Object> p) {
        Database.saveStudentExamCode(JsonUtil.getStr(p,"studentId"),
                JsonUtil.getInt(p,"examId"), JsonUtil.getStr(p,"status"));
        return JsonUtil.ok();
    }

    private String handleExamCodeLoad(Map<String, Object> p) {
        List<int[]> list = Database.loadStudentExamCodes(JsonUtil.getStr(p,"studentId"));
        StringBuilder arr = new StringBuilder("[");
        boolean first = true;
        for (int[] row : list) {
            if (!first) arr.append(','); first = false;
            arr.append('[').append(row[0]).append(',').append(row[1]).append(']');
        }
        return JsonUtil.ok(arr.append(']').toString());
    }

    private String handleExamCodeRemove(Map<String, Object> p) {
        Database.removeStudentExamCode(JsonUtil.getStr(p,"studentId"), JsonUtil.getInt(p,"examId"));
        return JsonUtil.ok();
    }

    private String handleAnnounceSave(Map<String, Object> p) {
        Announcement a = new Announcement(JsonUtil.getStr(p,"title"),
                JsonUtil.getStr(p,"body"), JsonUtil.getStr(p,"color"));
        a.expireAt = JsonUtil.getLong(p,"expireAt");
        Database.saveAnnouncement(a);
        return JsonUtil.ok(String.valueOf(a.id));
    }
    private String handleAnnounceDelete(Map<String, Object> p) {
        Database.deleteAnnouncement(JsonUtil.getInt(p,"id")); return JsonUtil.ok();
    }
    private String handleAnnounceLoad() {
        return JsonUtil.ok(toJsonArray(Database.loadAnnouncements(), this::announcementToJson));
    }
    private String handleAnnouncePurge() {
        Database.deleteExpiredAnnouncements(); return JsonUtil.ok();
    }

    @FunctionalInterface interface Serialiser<T> { String apply(T t); }

    private <T> String toJsonArray(List<T> list, Serialiser<T> fn) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (T item : list) { if (!first) sb.append(','); first = false; sb.append(fn.apply(item)); }
        return sb.append(']').toString();
    }

    private String teacherToJson(Teacher t) {
        return JsonUtil.obj().put("fullName",t.getUser()).put("email",t.getEmail()).put("password",t.getPassword()).build();
    }

    private String studentToJson(Student s) {
        return JsonUtil.obj().put("studentId",s.getID()).put("name",s.getName())
                .put("email",s.getEmail()).put("password",s.getPassword()).build();
    }

    String questionToJson(Question q) {
        JsonUtil.Builder b = JsonUtil.obj()
            .put("dbId", q.getDbId()).put("subject", q.getSubject())
            .put("grade", q.getGrade()).put("questionText", q.getQuestionText());
        if (q instanceof MCQ mcq) {
            b.put("type","MCQ").put("options", String.join("|", mcq.getOptions()))
             .put("correctIndex", mcq.getCorrectIndex());
        } else if (q instanceof TextQuestion tq) {
            b.put("type","TEXT").put("answer", tq.getAnswer());
        } else if (q instanceof RangeQuestion rq) {
            b.put("type","RANGE").put("minVal", rq.getMin()).put("maxVal", rq.getMax());
        }
        return b.build();
    }

    String examToJson(Exam e) {
        List<Map<String, Object>> qList = new ArrayList<>();
        for (var entry : e.getQuestionsMap().entrySet()) {
            Map<String, Object> qEntry = new LinkedHashMap<>();
            qEntry.put("questionJson", questionToJson(entry.getKey()));
            qEntry.put("marks", entry.getValue());
            qList.add(qEntry);
        }

        return JsonUtil.obj()
            .put("dbId",                 e.getDbId())
            .put("subject",              nvl(e.getSubject()))
            .put("grade",                e.getGrade())
            .put("totalMarks",           e.getTotalMarks())
            .put("duration",             nvl(e.getDuration()))
            .put("title",                nvl(e.getTitle()))
            .put("description",          nvl(e.getDescription()))
            .put("examCode",             nvl(e.getExamCode()))
            .put("isLive",               e.isLive())
            .put("liveWindow",           nvl(e.getLiveWindow()))
            .put("scheduleDetails",      nvl(e.getScheduleDetails()))
            .put("liveEndMillis",        e.getLiveEndMillis())
            .put("scheduledStartMillis", e.getScheduledStartMillis())
            .put("scheduledEndMillis",   e.getScheduledEndMillis())
            .put("questionsJson",        qList)
            .build();
    }

    private String resultToJson(ExamResult r) {
        return JsonUtil.obj()
            .put("id",r.id).put("studentId",r.studentId).put("examId",r.examId)
            .put("examCode",nvl(r.examCode)).put("examTitle",nvl(r.examTitle))
            .put("examSubject",nvl(r.examSubject)).put("examGrade",r.examGrade)
            .put("score",r.score).put("totalMarks",r.totalMarks)
            .put("correct",r.correct).put("totalQ",r.totalQ).put("takenAt",r.takenAt)
            .build();
    }

    private String resultsToJsonArray(List<ExamResult> list) {
        return toJsonArray(list, this::resultToJson);
    }

    private String announcementToJson(Announcement a) {
        return JsonUtil.obj()
            .put("id",a.id).put("title",a.title).put("body",a.body)
            .put("color",a.color).put("createdAt",a.createdAt).put("expireAt",a.expireAt)
            .build();
    }

    private Question questionFromMap(Map<String, Object> p) {
        String type = JsonUtil.getStr(p,"type"), sub = JsonUtil.getStr(p,"subject");
        int grade = JsonUtil.getInt(p,"grade"); String text = JsonUtil.getStr(p,"questionText");
        return switch (type) {
            case "MCQ" -> {
                String raw = JsonUtil.getStr(p,"options");
                yield new MCQ(sub, grade, text, raw.isEmpty() ? new String[0] : raw.split("\\|"),
                        JsonUtil.getInt(p,"correctIndex"));
            }
            case "TEXT"  -> new TextQuestion(sub, grade, text, JsonUtil.getDbl(p,"answer"));
            case "RANGE" -> new RangeQuestion(sub, grade, text,
                    JsonUtil.getDbl(p,"minVal"), JsonUtil.getDbl(p,"maxVal"));
            default -> throw new IllegalArgumentException("Unknown question type: " + type);
        };
    }

    private Exam examFromMap(Map<String, Object> p) {
        List<Object> qEntries = JsonUtil.getList(p, "questionsJson");
        HashMap<Question, Double> qMap = new HashMap<>();
        for (Object entry : qEntries) {
            if (entry instanceof Map<?,?> rawEntry) {
                @SuppressWarnings("unchecked") Map<String,Object> em = (Map<String,Object>) rawEntry;
                Map<String,Object> qMap2 = JsonUtil.parse(JsonUtil.getStr(em,"questionJson"));
                Question q = questionFromMap(qMap2); q.setDbId(JsonUtil.getInt(qMap2,"dbId"));
                qMap.put(q, JsonUtil.getDbl(em,"marks"));
            }
        }
        Exam e = new Exam(JsonUtil.getStr(p,"subject"), JsonUtil.getInt(p,"grade"),
                JsonUtil.getDbl(p,"totalMarks"), JsonUtil.getStr(p,"duration"), qMap);
        e.setDbId(JsonUtil.getInt(p,"dbId")); e.setTitle(JsonUtil.getStr(p,"title"));
        e.setDescription(JsonUtil.getStr(p,"description")); e.setExamCode(JsonUtil.getStr(p,"examCode"));
        e.setLive(JsonUtil.getBool(p,"isLive")); e.setLiveWindow(JsonUtil.getStr(p,"liveWindow"));
        e.setScheduleDetails(JsonUtil.getStr(p,"scheduleDetails"));
        e.setLiveEndMillis(JsonUtil.getLong(p,"liveEndMillis"));
        e.setScheduledStartMillis(JsonUtil.getLong(p,"scheduledStartMillis"));
        e.setScheduledEndMillis(JsonUtil.getLong(p,"scheduledEndMillis"));
        return e;
    }

    private ExamResult resultFromMap(Map<String, Object> p) {
        ExamResult r = new ExamResult();
        r.studentId = JsonUtil.getStr(p,"studentId"); r.examId = JsonUtil.getInt(p,"examId");
        r.examCode = JsonUtil.getStr(p,"examCode");   r.examTitle = JsonUtil.getStr(p,"examTitle");
        r.examSubject = JsonUtil.getStr(p,"examSubject"); r.examGrade = JsonUtil.getInt(p,"examGrade");
        r.score = JsonUtil.getDbl(p,"score");         r.totalMarks = JsonUtil.getDbl(p,"totalMarks");
        r.correct = JsonUtil.getInt(p,"correct");     r.totalQ = JsonUtil.getInt(p,"totalQ");
        r.takenAt = JsonUtil.getLong(p,"takenAt");
        return r;
    }

    private static String nvl(String s) { return s != null ? s : ""; }
}
