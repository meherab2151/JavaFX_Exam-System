package org.example.demo;

import java.util.ArrayList;
import java.util.HashMap;


abstract class Question {
    protected int dbId = -1;
    protected String subject;
    protected int grade;
    protected String questionText;

    public Question(String subject, int grade, String questionText) {
        this.subject = subject;
        this.grade = grade;
        this.questionText = questionText;
    }
    public void setQuestionText(String t) { this.questionText = t; }
    public String getSubject()            { return subject; }
    public int    getGrade()              { return grade; }
    public String getQuestionText()       { return questionText; }
    public int  getDbId()       { return dbId; }
    public void setDbId(int id) { this.dbId = id; }
}

class MCQ extends Question {
    private String[] options;
    private int correctIndex;
    public MCQ(String subject, int grade, String text, String[] options, int correctIndex) {
        super(subject, grade, text);
        this.options = options;
        this.correctIndex = correctIndex;
    }
    public String[] getOptions()            { return options; }
    public void     setOptions(String[] o)  { this.options = o; }
    public int      getCorrectIndex()       { return correctIndex; }
    public void     setCorrectIndex(int i)  { this.correctIndex = i; }
}

class TextQuestion extends Question {
    private double answer;
    public TextQuestion(String subject, int grade, String text, double answer) {
        super(subject, grade, text);
        this.answer = answer;
    }
    public double getAnswer()        { return answer; }
    public void   setAnswer(double a){ this.answer = a; }
}

class RangeQuestion extends Question {
    private double min, max;
    public RangeQuestion(String subject, int grade, String text, double min, double max) {
        super(subject, grade, text);
        this.min = min;
        this.max = max;
    }
    public double getMin()        { return min; }
    public void   setMin(double m){ this.min = m; }
    public double getMax()        { return max; }
    public void   setMax(double m){ this.max = m; }
}

class Exam {
    private int dbId = -1;
    private String  subject;
    private int     grade;
    private double  totalMarks;
    private String  duration;
    private HashMap<Question, Double> questions;

    private String  title           = "";
    private String  description     = "";
    private String  questionsText   = "";

    private String  examCode        = "";
    private boolean isLive          = false;
    private String  liveWindow      = "";
    private String  scheduleDetails = "";

    private long    liveEndMillis        = 0;
    private long    scheduledStartMillis = 0;
    private long    scheduledEndMillis   = 0;

    public Exam(String subject, int grade, double totalMarks, String duration,
                HashMap<Question, Double> questions) {
        this.subject    = subject;
        this.grade      = grade;
        this.totalMarks = totalMarks;
        this.duration   = duration;
        this.questions  = questions;
    }

    public String  getSubject()                { return subject; }
    public int     getGrade()                  { return grade; }
    public double  getTotalMarks()             { return totalMarks; }
    public String  getDuration()               { return duration; }
    public boolean isLive()                    { return isLive; }
    public void    setLive(boolean live)       { isLive = live; }
    public String  getLiveWindow()             { return liveWindow; }
    public void    setLiveWindow(String w)     { liveWindow = w; }
    public String  getScheduleDetails()        { return scheduleDetails; }
    public void    setScheduleDetails(String d){ scheduleDetails = d; }
    public HashMap<Question, Double> getQuestionsMap() { return questions; }
    public java.util.Collection<Question> getQuestions(){ return questions.keySet(); }
    public String  getExamCode()               { return examCode; }
    public void    setExamCode(String c)       { examCode = c; }
    public int  getDbId()       { return dbId; }
    public void setDbId(int id) { this.dbId = id; }

    public String  getTitle()                  { return title; }
    public void    setTitle(String t)          { this.title = t; }
    public String  getDescription()            { return description; }
    public void    setDescription(String d)    { this.description = d; }
    public String  getQuestionsText()          { return questionsText; }
    public void    setQuestionsText(String q)  { this.questionsText = q; }

    public long    getLiveEndMillis()           { return liveEndMillis; }
    public void    setLiveEndMillis(long ms)    { this.liveEndMillis = ms; }

    public long    getScheduledStartMillis()        { return scheduledStartMillis; }
    public void    setScheduledStartMillis(long ms) { this.scheduledStartMillis = ms; }
    public long    getScheduledEndMillis()          { return scheduledEndMillis; }
    public void    setScheduledEndMillis(long ms)   { this.scheduledEndMillis = ms; }

    public boolean isScheduled() {
        return !isLive && scheduledStartMillis > 0;
    }

    public boolean isDraft() {
        return !isLive && scheduledStartMillis == 0;
    }

    public boolean hasAutoSchedule() {
        return scheduledStartMillis > 0 && scheduledStartMillis > System.currentTimeMillis();
    }

    public String getStartCountdownFormatted() {
        if (scheduledStartMillis <= 0) return "";
        long rem = scheduledStartMillis - System.currentTimeMillis();
        if (rem <= 0) return "00:00:00";
        long sec = rem / 1000;
        long h = sec / 3600, m = (sec % 3600) / 60, s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public long getRemainingMillis() {
        if (!isLive) return 0;
        if (liveEndMillis == 0) return Long.MAX_VALUE;
        long rem = liveEndMillis - System.currentTimeMillis();
        return Math.max(rem, 0);
    }

    public boolean isExpired() {
        return isLive && liveEndMillis > 0 && System.currentTimeMillis() >= liveEndMillis;
    }

    public String getRemainingFormatted() {
        long ms = getRemainingMillis();
        if (ms == Long.MAX_VALUE) return "--:--:--";
        long sec = ms / 1000;
        long h   = sec / 3600;
        long m   = (sec % 3600) / 60;
        long s   = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public void generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        examCode = sb.toString();
    }

    public void resetToDraft() {
        isLive = false;
        liveWindow = "";
        scheduleDetails = "";
        liveEndMillis = 0;
        scheduledStartMillis = 0;
        scheduledEndMillis = 0;
        examCode = "";
    }
}

class Teacher {
    private String fullName, email, password;
    public Teacher(String fullName, String email, String password) {
        this.fullName = fullName; this.email = email; this.password = password;
    }
    public String getUser()    { return fullName; }
    public String getEmail()   { return email; }
    public String getPassword(){ return password; }
}

class Student {
    private String studentID, name, email, password;
    public Student(String studentID, String name, String email, String password) {
        this.studentID = studentID; this.name = name;
        this.email = email;         this.password = password;
    }
    public String getID()      { return studentID; }
    public String getName()    { return name; }
    public String getEmail()   { return email; }
    public String getPassword(){ return password; }
}

class ExamResult {
    public int    id;
    public String studentId;
    public int    examId;
    public String examCode;  
    public String examTitle;
    public String examSubject;
    public int    examGrade;
    public double score;
    public double totalMarks;
    public int    correct;
    public int    totalQ;
    public long   takenAt;

    public double pct() {
        return totalMarks > 0 ? (score / totalMarks) * 100 : 0;
    }

    public String grade() {
        double p = pct();
        return p >= 80 ? "A" : p >= 65 ? "B" : p >= 50 ? "C" : p >= 35 ? "D" : "F";
    }

    public String dateStr() {
        java.time.LocalDate d = java.time.Instant.ofEpochMilli(takenAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return d.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"));
    }
}

class Announcement {
    public int    id;
    public String title;
    public String body;
    public String color = "#2563eb";
    public long   createdAt;
    public long   expireAt = 0;

    public Announcement() {}
    public Announcement(String title, String body, String color) {
        this.title = title; this.body = body; this.color = color;
        this.createdAt = System.currentTimeMillis();
    }

    public String dateStr() {
        java.time.LocalDate d = java.time.Instant.ofEpochMilli(createdAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        return d.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"));
    }

    public boolean isExpired() {
        return expireAt > 0 && System.currentTimeMillis() >= expireAt;
    }

    public String expireStr() {
        if (expireAt <= 0) return "Never";
        java.time.LocalDateTime ldt = java.time.Instant.ofEpochMilli(expireAt)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return ldt.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy  HH:mm"));
    }
}
