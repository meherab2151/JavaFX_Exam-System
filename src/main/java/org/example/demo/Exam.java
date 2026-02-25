package org.example.demo;
import java.util.HashMap;

public class Exam {
    private String subject;
    private int grade;
    private double totalMarks;
    private String duration; // e.g., "20"
    private HashMap<Question, Double> questions;

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
}