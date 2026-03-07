package org.example.demo;
import java.util.HashMap;

public class Exam {
    private String subject;
    private int grade;
    private double totalMarks;
    private String duration; // e.g., "20"
    private HashMap<Question, Double> questions;
    private String examCode = "";

    // Status Logic
    private boolean isLive = false;
    private String liveWindow = ""; // e.g., "1h 23m"
    private String scheduleDetails = ""; // e.g., "2026-05-10 | 10:00-12:00"

    public Exam(String subject, int grade, double totalMarks, String duration, HashMap<Question, Double> questions) {
        this.subject = subject;
        this.grade = grade;
        this.totalMarks = totalMarks;
        this.duration = duration;
        this.questions = questions;
    }

    // Getters and Setters
    public String getSubject() { return subject; }
    public int getGrade() { return grade; }
    public double getTotalMarks() { return totalMarks; }
    public String getDuration() { return duration; }
    public boolean isLive() { return isLive; }
    public void setLive(boolean live) { isLive = live; }
    public String getLiveWindow() { return liveWindow; }
    public void setLiveWindow(String liveWindow) { this.liveWindow = liveWindow; }
    public String getScheduleDetails() { return scheduleDetails; }
    public void setScheduleDetails(String details) { this.scheduleDetails = details; }
    public HashMap<Question, Double> getQuestionsMap() {
        return questions;
    }
    public java.util.Collection<Question> getQuestions() {
        return questions.keySet();
    }
    public String getExamCode() { return examCode; }
    public void setExamCode(String examCode) { this.examCode = examCode; }
    public void generateCode() {
        if (this.examCode == null || this.examCode.isEmpty()) {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            java.util.Random rnd = new java.util.Random();
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            this.examCode = sb.toString();
        }
    }
}