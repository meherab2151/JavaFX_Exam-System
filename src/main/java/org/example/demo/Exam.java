package org.example.demo;
import java.util.ArrayList;
import java.util.HashMap;

public class Exam {
    private String subject;
    private int grade;
    private double totalMarks;
    private String duration;
    private String date;
    private ArrayList<Question> selectedQuestions;
    private HashMap<Question, Double> questionMarks; // Stores mark for each question

    public Exam(String subject, int grade, double totalMarks, String duration, String date,
                ArrayList<Question> selectedQuestions, HashMap<Question, Double> questionMarks) {
        this.subject = subject;
        this.grade = grade;
        this.totalMarks = totalMarks;
        this.duration = duration;
        this.date = date;
        this.selectedQuestions = selectedQuestions;
        this.questionMarks = questionMarks;
    }
}

// Global storage for Exams
class ExamBank {
    public static ArrayList<Exam> allExams = new ArrayList<>();
}