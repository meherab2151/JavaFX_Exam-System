package org.example.demo;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;

public final class ServerClient {

    private static final ServerClient INSTANCE = new ServerClient();
    public static ServerClient get() { return INSTANCE; }

    private Socket        socket;
    private BufferedReader reader;
    private PrintWriter    writer;
    private Thread         pushThread;

    private final java.util.concurrent.SynchronousQueue<String> responseQueue =
            new java.util.concurrent.SynchronousQueue<>();

    private volatile Consumer<Exam> examEventListener;

    private ServerClient() {}

    public synchronized void connect() throws IOException {
        if (isConnected()) return;
        socket = new Socket(Protocol.HOST, Protocol.PORT);
        socket.setTcpNoDelay(true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),  Protocol.CHARSET));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), Protocol.CHARSET), true);
        startPushReader();
        System.out.println("[Client] Connected to EduExam Server at " + Protocol.HOST + ":" + Protocol.PORT);
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void disconnect() {
        try {
            if (writer != null) writer.println(buildRequest(Protocol.BYE, "{}"));
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        socket = null; reader = null; writer = null;
        if (pushThread != null) { pushThread.interrupt(); pushThread = null; }
    }

    public void setExamEventListener(Consumer<Exam> listener) {
        this.examEventListener = listener;
    }

    private void startPushReader() {
        pushThread = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    if (line.contains("\"push\":")) {
                        handlePush(line);
                    } else {
                        try {
                            responseQueue.put(line);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted())
                    System.err.println("[Client] Push-reader lost connection: " + e.getMessage());
            }
        }, "EduExam-push-reader");
        pushThread.setDaemon(true);
        pushThread.start();
    }

    private void handlePush(String line) {
        Map<String, Object> msg = JsonUtil.parse(line);
        String pushType = JsonUtil.getStr(msg, "push");
        if (Protocol.PUSH_EXAM_EVENT.equals(pushType)) {
            Consumer<Exam> listener = examEventListener;
            if (listener == null) return;

            Object dataObj = msg.get("data");
            Map<String, Object> examMap;
            if (dataObj instanceof Map<?,?> m) {
                @SuppressWarnings("unchecked") Map<String,Object> cast = (Map<String,Object>) m;
                examMap = cast;
            } else {
                examMap = JsonUtil.parse(dataObj == null ? "{}" : dataObj.toString());
            }

            if (JsonUtil.getBool(examMap, "deleted")) {
                Exam shell = new Exam("", 0, 0, "0", new HashMap<>());
                shell.setDbId(JsonUtil.getInt(examMap, "dbId"));
                shell.setLive(false);
                shell.setExamCode("");
                listener.accept(shell);
                return;
            }

            Exam exam = mapToExam(examMap);
            listener.accept(exam);
        }
    }

    private synchronized Map<String, Object> send(String action, String payloadJson) {
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                if (!isConnected()) connect();
                writer.println(buildRequest(action, payloadJson));
                String line = responseQueue.poll(10, java.util.concurrent.TimeUnit.SECONDS);
                if (line == null) throw new IOException("Response timeout");
                return JsonUtil.parse(line);
            } catch (IOException | InterruptedException e) {
                System.err.println("[Client] Send failed (attempt " + (attempt+1) + "): " + e.getMessage());
                try { if (socket != null) socket.close(); } catch (IOException ignored) {}
                socket = null; reader = null; writer = null;
            }
        }
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("ok", false); err.put("data", null); err.put("error", "Could not reach EduExam Server");
        return err;
    }

    private static String buildRequest(String action, String payloadJson) {
        return "{\"action\":\"" + action + "\",\"payload\":" + payloadJson + "}";
    }

    private Map<String, Object> getDataMap(Map<String, Object> resp) {
        return JsonUtil.getMap(resp, "data");
    }


    public Teacher teacherLogin(String id, String password) {
        Map<String,Object> resp = send(Protocol.TEACHER_LOGIN,
                JsonUtil.obj().put("id",id).put("password",password).build());
        return JsonUtil.getBool(resp,"ok") ? mapToTeacher(getDataMap(resp)) : null;
    }

    public boolean teacherRegister(String fullName, String email, String password) {
        return JsonUtil.getBool(send(Protocol.TEACHER_REGISTER,
                JsonUtil.obj().put("fullName",fullName).put("email",email).put("password",password).build()), "ok");
    }

    public Student studentLogin(String id, String password) {
        Map<String,Object> resp = send(Protocol.STUDENT_LOGIN,
                JsonUtil.obj().put("id",id).put("password",password).build());
        return JsonUtil.getBool(resp,"ok") ? mapToStudent(getDataMap(resp)) : null;
    }

    public boolean studentRegister(String studentId, String name, String email, String password) {
        return JsonUtil.getBool(send(Protocol.STUDENT_REGISTER,
                JsonUtil.obj().put("studentId",studentId).put("name",name)
                    .put("email",email).put("password",password).build()), "ok");
    }

    public boolean teacherEmailExists(String email) {
        return loadAllTeachers().stream().anyMatch(t -> t.getEmail().equalsIgnoreCase(email));
    }
    public boolean studentIdExists(String sid) {
        return loadAllStudents().stream().anyMatch(s -> s.getID().equals(sid));
    }

    public ArrayList<Teacher> loadAllTeachers() {
        Map<String,Object> resp = send(Protocol.TEACHERS_LOAD, "{}");
        ArrayList<Teacher> list = new ArrayList<>();
        if (JsonUtil.getBool(resp,"ok"))
            for (Object item : JsonUtil.getList(resp,"data"))
                if (item instanceof Map<?,?> m) list.add(mapToTeacher(cast(m)));
        return list;
    }

    public ArrayList<Student> loadAllStudents() {
        Map<String,Object> resp = send(Protocol.STUDENTS_LOAD, "{}");
        ArrayList<Student> list = new ArrayList<>();
        if (JsonUtil.getBool(resp,"ok"))
            for (Object item : JsonUtil.getList(resp,"data"))
                if (item instanceof Map<?,?> m) list.add(mapToStudent(cast(m)));
        return list;
    }

    public int questionSave(Question q) {
        Map<String,Object> resp = send(Protocol.QUESTION_SAVE, questionToJson(q));
        if (!JsonUtil.getBool(resp,"ok")) return -1;
        try { return Integer.parseInt(JsonUtil.getStr(resp,"data")); } catch (NumberFormatException e) { return -1; }
    }
    public boolean questionUpdate(Question q) {
        return JsonUtil.getBool(send(Protocol.QUESTION_UPDATE, questionToJson(q)),"ok");
    }
    public boolean questionDelete(Question q) {
        return JsonUtil.getBool(send(Protocol.QUESTION_DELETE,
                JsonUtil.obj().put("dbId",q.getDbId()).put("type",typeStr(q))
                    .put("subject",q.getSubject()).put("grade",q.getGrade())
                    .put("questionText",q.getQuestionText()).build()), "ok");
    }
    public ArrayList<Question> loadAllQuestions() {
        Map<String,Object> resp = send(Protocol.QUESTIONS_LOAD, "{}");
        ArrayList<Question> list = new ArrayList<>();
        if (JsonUtil.getBool(resp,"ok"))
            for (Object item : JsonUtil.getList(resp,"data"))
                if (item instanceof Map<?,?> m) { Question q = mapToQuestion(cast(m)); if(q!=null) list.add(q); }
        return list;
    }

    public int examSave(Exam exam) {
        Map<String,Object> resp = send(Protocol.EXAM_SAVE, examToJson(exam));
        if (!JsonUtil.getBool(resp,"ok")) return -1;
        try { return Integer.parseInt(JsonUtil.getStr(resp,"data")); } catch (NumberFormatException e) { return -1; }
    }
    public boolean examDelete(Exam exam) {
        return JsonUtil.getBool(send(Protocol.EXAM_DELETE,
                JsonUtil.obj().put("dbId",exam.getDbId()).build()),"ok");
    }
    public ArrayList<Exam> loadAllExams() {
        Map<String,Object> resp = send(Protocol.EXAMS_LOAD, "{}");
        ArrayList<Exam> list = new ArrayList<>();
        if (JsonUtil.getBool(resp,"ok"))
            for (Object item : JsonUtil.getList(resp,"data"))
                if (item instanceof Map<?,?> m) { Exam e = mapToExam(cast(m)); if(e!=null) list.add(e); }
        return list;
    }

    public void resultSave(ExamResult r) { send(Protocol.RESULT_SAVE, resultToJson(r)); }
    public List<ExamResult> loadResultsForStudent(String sid) {
        return parseResultList(send(Protocol.RESULTS_LOAD_STUDENT,
                JsonUtil.obj().put("studentId",sid).build()));
    }
    public List<ExamResult> loadResultsForExam(int examId) {
        return parseResultList(send(Protocol.RESULTS_LOAD_EXAM,
                JsonUtil.obj().put("examId",examId).build()));
    }
    public List<ExamResult> loadAllResults() {
        return parseResultList(send(Protocol.RESULTS_LOAD_ALL, "{}"));
    }
    public boolean hasResult(String sid, int examId, String code) {
        Map<String,Object> resp = send(Protocol.RESULT_HAS,
                JsonUtil.obj().put("studentId",sid).put("examId",examId).put("examCode",code).build());
        return JsonUtil.getBool(resp,"ok") && "true".equals(JsonUtil.getStr(resp,"data"));
    }
    public boolean hasAnyResult(String sid, int examId) {
        Map<String,Object> resp = send(Protocol.RESULT_HAS_ANY,
                JsonUtil.obj().put("studentId",sid).put("examId",examId).build());
        return JsonUtil.getBool(resp,"ok") && "true".equals(JsonUtil.getStr(resp,"data"));
    }
    public ExamResult loadSingleResult(String sid, int examId) {
        Map<String,Object> resp = send(Protocol.RESULT_LOAD_SINGLE,
                JsonUtil.obj().put("studentId",sid).put("examId",examId).build());
        if (!JsonUtil.getBool(resp,"ok")) return null;
        Map<String,Object> d = getDataMap(resp); return d.isEmpty() ? null : mapToResult(d);
    }

    public void saveInProgress(String sid, int eid, Map<Integer,String> answers, Set<Integer> flagged) {
        StringBuilder ans = new StringBuilder();
        for (var e : answers.entrySet()) { if(ans.length()>0) ans.append("||"); ans.append(e.getKey()).append(':').append(e.getValue()); }
        StringBuilder fl = new StringBuilder();
        for (int i : flagged) { if(fl.length()>0) fl.append(','); fl.append(i); }
        send(Protocol.PROGRESS_SAVE, JsonUtil.obj().put("studentId",sid).put("examId",eid)
                .put("answers",ans.toString()).put("flagged",fl.toString()).build());
    }
    public Map<Integer,String> loadInProgressAnswers(String sid, int eid) {
        Map<String,Object> resp = send(Protocol.PROGRESS_LOAD,
                JsonUtil.obj().put("studentId",sid).put("examId",eid).build());
        if (!JsonUtil.getBool(resp,"ok")) return null;
        String raw = JsonUtil.getStr(resp,"data");
        if ("null".equals(raw) || raw.isBlank()) return null;
        Map<Integer,String> map = new HashMap<>();
        for (String pair : raw.split("\\|\\|")) {
            int c = pair.indexOf(':');
            if (c>0) try { map.put(Integer.parseInt(pair.substring(0,c)), pair.substring(c+1)); } catch (NumberFormatException ignored) {}
        }
        return map;
    }
    public Set<Integer> loadInProgressFlagged(String sid, int eid) {
        Map<String,Object> resp = send(Protocol.PROGRESS_FLAGGED,
                JsonUtil.obj().put("studentId",sid).put("examId",eid).build());
        Set<Integer> set = new HashSet<>();
        if (!JsonUtil.getBool(resp,"ok")) return set;
        String raw = JsonUtil.getStr(resp,"data");
        if (!raw.isBlank() && !"null".equals(raw))
            for (String s : raw.split(",")) try { set.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {}
        return set;
    }
    public boolean hasInProgress(String sid, int eid) {
        Map<String,Object> resp = send(Protocol.PROGRESS_HAS,
                JsonUtil.obj().put("studentId",sid).put("examId",eid).build());
        return JsonUtil.getBool(resp,"ok") && "true".equals(JsonUtil.getStr(resp,"data"));
    }
    public void clearInProgress(String sid, int eid) {
        send(Protocol.PROGRESS_CLEAR, JsonUtil.obj().put("studentId",sid).put("examId",eid).build());
    }

    public void saveStudentExamCode(String sid, int examId, String status) {
        send(Protocol.EXAM_CODE_SAVE, JsonUtil.obj().put("studentId",sid).put("examId",examId).put("status",status).build());
    }
    public List<int[]> loadStudentExamCodes(String sid) {
        Map<String,Object> resp = send(Protocol.EXAM_CODE_LOAD,
                JsonUtil.obj().put("studentId",sid).build());
        List<int[]> list = new ArrayList<>();
        if (!JsonUtil.getBool(resp,"ok")) return list;
        for (Object item : JsonUtil.getList(resp,"data"))
            if (item instanceof List<?> row && row.size()>=2)
                list.add(new int[]{((Number)row.get(0)).intValue(), ((Number)row.get(1)).intValue()});
        return list;
    }
    public void removeStudentExamCode(String sid, int examId) {
        send(Protocol.EXAM_CODE_REMOVE, JsonUtil.obj().put("studentId",sid).put("examId",examId).build());
    }

    public int saveAnnouncement(Announcement a) {
        Map<String,Object> resp = send(Protocol.ANNOUNCE_SAVE,
                JsonUtil.obj().put("title",a.title).put("body",a.body)
                    .put("color",a.color).put("expireAt",a.expireAt).build());
        if (!JsonUtil.getBool(resp,"ok")) return -1;
        try { return Integer.parseInt(JsonUtil.getStr(resp,"data")); } catch (NumberFormatException e) { return -1; }
    }
    public void deleteAnnouncement(int id) {
        send(Protocol.ANNOUNCE_DELETE, JsonUtil.obj().put("id",id).build());
    }
    public List<Announcement> loadAnnouncements() {
        Map<String,Object> resp = send(Protocol.ANNOUNCE_LOAD, "{}");
        List<Announcement> list = new ArrayList<>();
        if (JsonUtil.getBool(resp,"ok"))
            for (Object item : JsonUtil.getList(resp,"data"))
                if (item instanceof Map<?,?> m) list.add(mapToAnnouncement(cast(m)));
        return list;
    }
    public void purgeExpiredAnnouncements() { send(Protocol.ANNOUNCE_PURGE, "{}"); }

    public int saveAnnouncementQuestion(AnnouncementQuestion q) {
        Map<String,Object> resp = send(Protocol.ANNOUNCE_Q_SAVE, JsonUtil.obj()
                .put("announcementId", q.announcementId)
                .put("studentId", q.studentId)
                .put("studentName", q.studentName)
                .put("question", q.question)
                .put("teacherAnswer", nvl(q.teacherAnswer))
                .put("answerTeacherName", nvl(q.answerTeacherName))
                .put("createdAt", q.createdAt)
                .put("answeredAt", q.answeredAt)
                .build());
        if (!JsonUtil.getBool(resp, "ok")) return -1;
        try { return Integer.parseInt(JsonUtil.getStr(resp, "data")); } catch (NumberFormatException e) { return -1; }
    }

    public List<AnnouncementQuestion> loadAnnouncementQuestions(int announcementId) {
        Map<String,Object> resp = send(Protocol.ANNOUNCE_Q_LOAD, JsonUtil.obj().put("announcementId", announcementId).build());
        List<AnnouncementQuestion> list = new ArrayList<>();
        if (JsonUtil.getBool(resp, "ok"))
            for (Object item : JsonUtil.getList(resp, "data"))
                if (item instanceof Map<?,?> m) list.add(mapToAnnouncementQuestion(cast(m)));
        return list;
    }

    public void answerAnnouncementQuestion(int id, String answer, String teacherName) {
        send(Protocol.ANNOUNCE_Q_ANSWER, JsonUtil.obj().put("id", id).put("answer", answer).put("teacherName", teacherName).build());
    }

    public int saveDirectMessage(DirectMessage m) {
        Map<String,Object> resp = send(Protocol.DM_SAVE, JsonUtil.obj()
                .put("teacherEmail", m.teacherEmail)
                .put("teacherName", m.teacherName)
                .put("studentId", m.studentId)
                .put("studentName", m.studentName)
                .put("senderRole", m.senderRole)
                .put("senderName", m.senderName)
                .put("body", m.body)
                .put("createdAt", m.createdAt)
                .build());
        if (!JsonUtil.getBool(resp, "ok")) return -1;
        try { return Integer.parseInt(JsonUtil.getStr(resp, "data")); } catch (NumberFormatException e) { return -1; }
    }

    public List<DirectMessage> loadConversation(String teacherEmail, String studentId) {
        Map<String,Object> resp = send(Protocol.DM_LOAD_CONVERSATION,
                JsonUtil.obj().put("teacherEmail", teacherEmail).put("studentId", studentId).build());
        return parseDirectMessageList(resp);
    }

    public List<DirectMessage> loadConversationPreviewsForStudent(String studentId) {
        return parseDirectMessageList(send(Protocol.DM_LOAD_PREVIEW_STUDENT, JsonUtil.obj().put("studentId", studentId).build()));
    }

    public List<DirectMessage> loadConversationPreviewsForTeacher(String teacherEmail) {
        return parseDirectMessageList(send(Protocol.DM_LOAD_PREVIEW_TEACHER, JsonUtil.obj().put("teacherEmail", teacherEmail).build()));
    }

    public void markConversationReadForStudent(String teacherEmail, String studentId) {
        send(Protocol.DM_MARK_READ_STUDENT, JsonUtil.obj().put("teacherEmail", teacherEmail).put("studentId", studentId).build());
    }

    public void markConversationReadForTeacher(String teacherEmail, String studentId) {
        send(Protocol.DM_MARK_READ_TEACHER, JsonUtil.obj().put("teacherEmail", teacherEmail).put("studentId", studentId).build());
    }

    public int countUnreadConversationMessagesForStudent(String studentId) {
        Map<String,Object> resp = send(Protocol.DM_UNREAD_STUDENT, JsonUtil.obj().put("studentId", studentId).build());
        try { return JsonUtil.getBool(resp, "ok") ? Integer.parseInt(JsonUtil.getStr(resp, "data")) : 0; }
        catch (NumberFormatException e) { return 0; }
    }

    public int countUnreadConversationMessagesForTeacher(String teacherEmail) {
        Map<String,Object> resp = send(Protocol.DM_UNREAD_TEACHER, JsonUtil.obj().put("teacherEmail", teacherEmail).build());
        try { return JsonUtil.getBool(resp, "ok") ? Integer.parseInt(JsonUtil.getStr(resp, "data")) : 0; }
        catch (NumberFormatException e) { return 0; }
    }

    public void deleteDirectMessage(int id) {
        send(Protocol.DM_DELETE, JsonUtil.obj().put("id", id).build());
    }

    public void markAnnouncementRead(String studentId, int announcementId) {
        send(Protocol.ANNOUNCE_MARK_READ, JsonUtil.obj().put("studentId", studentId).put("announcementId", announcementId).build());
    }

    public int countUnreadAnnouncementsForStudent(String studentId) {
        Map<String,Object> resp = send(Protocol.ANNOUNCE_UNREAD_STUDENT, JsonUtil.obj().put("studentId", studentId).build());
        try { return JsonUtil.getBool(resp, "ok") ? Integer.parseInt(JsonUtil.getStr(resp, "data")) : 0; }
        catch (NumberFormatException e) { return 0; }
    }

    public boolean ping() { return JsonUtil.getBool(send(Protocol.PING, "{}"), "ok"); }

    private String questionToJson(Question q) {
        JsonUtil.Builder b = JsonUtil.obj()
            .put("dbId",q.getDbId()).put("subject",q.getSubject())
            .put("grade",q.getGrade()).put("questionText",q.getQuestionText())
            .put("type",typeStr(q));
        if (q instanceof MCQ mcq) { b.put("options",String.join("|",mcq.getOptions())).put("correctIndex",mcq.getCorrectIndex()); }
        else if (q instanceof TextQuestion tq) { b.put("answer",tq.getAnswer()); }
        else if (q instanceof RangeQuestion rq) { b.put("minVal",rq.getMin()).put("maxVal",rq.getMax()); }
        return b.build();
    }

    private String examToJson(Exam e) {
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
            .put("studentId",r.studentId).put("examId",r.examId).put("examCode",nvl(r.examCode))
            .put("examTitle",nvl(r.examTitle)).put("examSubject",nvl(r.examSubject)).put("examGrade",r.examGrade)
            .put("score",r.score).put("totalMarks",r.totalMarks).put("correct",r.correct)
            .put("totalQ",r.totalQ).put("takenAt",r.takenAt).build();
    }

    private Teacher mapToTeacher(Map<String,Object> m) {
        return new Teacher(JsonUtil.getStr(m,"fullName"), JsonUtil.getStr(m,"email"), JsonUtil.getStr(m,"password"));
    }
    private Student mapToStudent(Map<String,Object> m) {
        return new Student(JsonUtil.getStr(m,"studentId"), JsonUtil.getStr(m,"name"),
                JsonUtil.getStr(m,"email"), JsonUtil.getStr(m,"password"));
    }
    private Question mapToQuestion(Map<String,Object> m) {
        String type = JsonUtil.getStr(m,"type"), sub = JsonUtil.getStr(m,"subject");
        int grade = JsonUtil.getInt(m,"grade"); String text = JsonUtil.getStr(m,"questionText");
        int dbId = JsonUtil.getInt(m,"dbId");
        Question q = switch (type) {
            case "MCQ" -> { String raw = JsonUtil.getStr(m,"options");
                yield new MCQ(sub,grade,text, raw.isEmpty()?new String[0]:raw.split("\\|"), JsonUtil.getInt(m,"correctIndex")); }
            case "TEXT"  -> new TextQuestion(sub,grade,text, JsonUtil.getDbl(m,"answer"));
            case "RANGE" -> new RangeQuestion(sub,grade,text, JsonUtil.getDbl(m,"minVal"), JsonUtil.getDbl(m,"maxVal"));
            default -> null;
        };
        if (q!=null) q.setDbId(dbId);
        return q;
    }
    Exam mapToExam(Map<String,Object> m) {
        List<Object> qEntries = JsonUtil.getList(m,"questionsJson");
        HashMap<Question,Double> qMap = new HashMap<>();
        for (Object entry : qEntries)
            if (entry instanceof Map<?,?> rawEntry) {
                Map<String,Object> em = cast(rawEntry);
                Question q = mapToQuestion(JsonUtil.parse(JsonUtil.getStr(em,"questionJson")));
                if (q!=null) qMap.put(q, JsonUtil.getDbl(em,"marks"));
            }
        Exam e = new Exam(JsonUtil.getStr(m,"subject"), JsonUtil.getInt(m,"grade"),
                JsonUtil.getDbl(m,"totalMarks"), JsonUtil.getStr(m,"duration"), qMap);
        e.setDbId(JsonUtil.getInt(m,"dbId")); e.setTitle(JsonUtil.getStr(m,"title"));
        e.setDescription(JsonUtil.getStr(m,"description")); e.setExamCode(JsonUtil.getStr(m,"examCode"));
        e.setLive(JsonUtil.getBool(m,"isLive")); e.setLiveWindow(JsonUtil.getStr(m,"liveWindow"));
        e.setScheduleDetails(JsonUtil.getStr(m,"scheduleDetails"));
        e.setLiveEndMillis(JsonUtil.getLong(m,"liveEndMillis"));
        e.setScheduledStartMillis(JsonUtil.getLong(m,"scheduledStartMillis"));
        e.setScheduledEndMillis(JsonUtil.getLong(m,"scheduledEndMillis"));
        return e;
    }
    private ExamResult mapToResult(Map<String,Object> m) {
        ExamResult r = new ExamResult();
        r.id=JsonUtil.getInt(m,"id"); r.studentId=JsonUtil.getStr(m,"studentId");
        r.examId=JsonUtil.getInt(m,"examId"); r.examCode=JsonUtil.getStr(m,"examCode");
        r.examTitle=JsonUtil.getStr(m,"examTitle"); r.examSubject=JsonUtil.getStr(m,"examSubject");
        r.examGrade=JsonUtil.getInt(m,"examGrade"); r.score=JsonUtil.getDbl(m,"score");
        r.totalMarks=JsonUtil.getDbl(m,"totalMarks"); r.correct=JsonUtil.getInt(m,"correct");
        r.totalQ=JsonUtil.getInt(m,"totalQ"); r.takenAt=JsonUtil.getLong(m,"takenAt");
        return r;
    }
    private Announcement mapToAnnouncement(Map<String,Object> m) {
        Announcement a = new Announcement();
        a.id=JsonUtil.getInt(m,"id"); a.title=JsonUtil.getStr(m,"title");
        a.body=JsonUtil.getStr(m,"body"); a.color=JsonUtil.getStr(m,"color");
        a.createdAt=JsonUtil.getLong(m,"createdAt"); a.expireAt=JsonUtil.getLong(m,"expireAt");
        return a;
    }
    private AnnouncementQuestion mapToAnnouncementQuestion(Map<String,Object> m) {
        AnnouncementQuestion q = new AnnouncementQuestion();
        q.id = JsonUtil.getInt(m, "id");
        q.announcementId = JsonUtil.getInt(m, "announcementId");
        q.studentId = JsonUtil.getStr(m, "studentId");
        q.studentName = JsonUtil.getStr(m, "studentName");
        q.question = JsonUtil.getStr(m, "question");
        q.teacherAnswer = JsonUtil.getStr(m, "teacherAnswer");
        q.answerTeacherName = JsonUtil.getStr(m, "answerTeacherName");
        q.createdAt = JsonUtil.getLong(m, "createdAt");
        q.answeredAt = JsonUtil.getLong(m, "answeredAt");
        return q;
    }
    private DirectMessage mapToDirectMessage(Map<String,Object> m) {
        DirectMessage dm = new DirectMessage();
        dm.id = JsonUtil.getInt(m, "id");
        dm.teacherEmail = JsonUtil.getStr(m, "teacherEmail");
        dm.teacherName = JsonUtil.getStr(m, "teacherName");
        dm.studentId = JsonUtil.getStr(m, "studentId");
        dm.studentName = JsonUtil.getStr(m, "studentName");
        dm.senderRole = JsonUtil.getStr(m, "senderRole");
        dm.senderName = JsonUtil.getStr(m, "senderName");
        dm.body = JsonUtil.getStr(m, "body");
        dm.createdAt = JsonUtil.getLong(m, "createdAt");
        dm.readByRecipient = JsonUtil.getBool(m, "readByRecipient");
        return dm;
    }
    private List<ExamResult> parseResultList(Map<String,Object> resp) {
        List<ExamResult> list = new ArrayList<>();
        if (JsonUtil.getBool(resp,"ok"))
            for (Object item : JsonUtil.getList(resp,"data"))
                if (item instanceof Map<?,?> m) list.add(mapToResult(cast(m)));
        return list;
    }
    private List<DirectMessage> parseDirectMessageList(Map<String,Object> resp) {
        List<DirectMessage> list = new ArrayList<>();
        if (JsonUtil.getBool(resp, "ok"))
            for (Object item : JsonUtil.getList(resp, "data"))
                if (item instanceof Map<?,?> m) list.add(mapToDirectMessage(cast(m)));
        return list;
    }

    private static String typeStr(Question q) {
        if (q instanceof MCQ)           return "MCQ";
        if (q instanceof TextQuestion)  return "TEXT";
        if (q instanceof RangeQuestion) return "RANGE";
        return "UNKNOWN";
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> cast(Map<?,?> m) { return (Map<String,Object>) m; }
    private static String nvl(String s) { return s != null ? s : ""; }
}
