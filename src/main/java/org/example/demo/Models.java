package org.example.demo;

import java.util.ArrayList;
import java.util.HashMap;

// ═══════════════════════════════════════════════════════════
//  MODELS.java  –  All data-model classes in one place
//  Contains: Question (MCQ, TextQuestion, RangeQuestion),
//            Exam, Teacher, Student, QuestionBank, ExamBank
// ═══════════════════════════════════════════════════════════

// ─── Abstract base question ───────────────────────────────
abstract class Question {
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
}

// ─── MCQ ─────────────────────────────────────────────────
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

// ─── Text / Exact-answer question ────────────────────────
class TextQuestion extends Question {
    private double answer;

    public TextQuestion(String subject, int grade, String text, double answer) {
        super(subject, grade, text);
        this.answer = answer;
    }
    public double getAnswer()        { return answer; }
    public void   setAnswer(double a){ this.answer = a; }
}

// ─── Range-answer question ────────────────────────────────
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

// ─── Exam ─────────────────────────────────────────────────
class Exam {
    private String  subject;
    private int     grade;
    private double  totalMarks;
    private String  duration;
    private HashMap<Question, Double> questions;

    // Meta
    private String  title           = "";
    private String  description     = "";
    private String  questionsText   = "";   // raw text from uploaded .txt file

    // State
    private String  examCode        = "";
    private boolean isLive          = false;
    private String  liveWindow      = "";
    private String  scheduleDetails = "";

    // Live countdown – absolute epoch millis when exam window ends
    private long    liveEndMillis      = 0;

    // Auto-schedule: epoch millis for when exam should auto-start and auto-end
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

    // ── Core getters/setters ─────────────────────────────
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

    // ── New meta getters/setters ─────────────────────────
    public String  getTitle()                  { return title; }
    public void    setTitle(String t)          { this.title = t; }
    public String  getDescription()            { return description; }
    public void    setDescription(String d)    { this.description = d; }
    public String  getQuestionsText()          { return questionsText; }
    public void    setQuestionsText(String q)  { this.questionsText = q; }

    // ── Live countdown ───────────────────────────────────
    public long    getLiveEndMillis()           { return liveEndMillis; }
    public void    setLiveEndMillis(long ms)    { this.liveEndMillis = ms; }

    // ── Auto-schedule ────────────────────────────────────
    public long    getScheduledStartMillis()              { return scheduledStartMillis; }
    public void    setScheduledStartMillis(long ms)       { this.scheduledStartMillis = ms; }
    public long    getScheduledEndMillis()                { return scheduledEndMillis; }
    public void    setScheduledEndMillis(long ms)         { this.scheduledEndMillis = ms; }

    /** True if this exam has a future auto-start scheduled. */
    public boolean hasAutoSchedule() {
        return scheduledStartMillis > 0 && scheduledStartMillis > System.currentTimeMillis();
    }

    /** Formatted countdown to scheduled start HH:MM:SS, or "" if not scheduled. */
    public String getStartCountdownFormatted() {
        if (scheduledStartMillis <= 0) return "";
        long rem = scheduledStartMillis - System.currentTimeMillis();
        if (rem <= 0) return "00:00:00";
        long sec = rem / 1000;
        long h = sec / 3600, m = (sec % 3600) / 60, s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /** Remaining millis in live window; 0 if not live or expired. */
    public long    getRemainingMillis() {
        if (!isLive || liveEndMillis == 0) return 0;
        long rem = liveEndMillis - System.currentTimeMillis();
        return Math.max(rem, 0);
    }

    /** Formatted remaining time string HH:MM:SS */
    public String  getRemainingFormatted() {
        long ms  = getRemainingMillis();
        long sec = ms / 1000;
        long h   = sec / 3600;
        long m   = (sec % 3600) / 60;
        long s   = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public void generateCode() {
        if (examCode == null || examCode.isEmpty()) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            java.util.Random rnd = new java.util.Random();
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            examCode = sb.toString();
        }
    }
}

// ─── Teacher ──────────────────────────────────────────────
class Teacher {
    private String fullName, email, password;
    public Teacher(String fullName, String email, String password) {
        this.fullName = fullName; this.email = email; this.password = password;
    }
    public String getUser()    { return fullName; }
    public String getEmail()   { return email; }
    public String getPassword(){ return password; }
}

// ─── Student ──────────────────────────────────────────────
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

// ─── Shared static banks ─────────────────────────────────
class QuestionBank {
    public static ArrayList<Question> allQuestions = new ArrayList<>();
}

class ExamBank {
    public static ArrayList<Exam> allExams = new ArrayList<>();

    public static ArrayList<Exam> getLiveExams() {
        ArrayList<Exam> live = new ArrayList<>();
        for (Exam e : allExams) if (e.isLive()) live.add(e);
        return live;
    }
}