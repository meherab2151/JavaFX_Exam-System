package org.example.demo;

abstract class Question {
    protected String subject;
    protected int grade;
    protected String questionText;

    public Question(String subject, int grade, String questionText) {
        this.subject = subject;
        this.grade = grade;
        this.questionText = questionText;
    }
}

class MCQ extends Question {
    private String[] options;
    private int correctIndex;

    public MCQ(String subject, int grade, String questionText, String[] options, int correctIndex) {
        super(subject, grade, questionText);
        this.options = options;
        this.correctIndex = correctIndex;
    }
}

class TextQuestion extends Question {
    private double answer; // Changed to double

    public TextQuestion(String subject, int grade, String questionText, double answer) {
        super(subject, grade, questionText);
        this.answer = answer;
    }
}

class RangeQuestion extends Question {
    private double min, max; // Changed to double

    public RangeQuestion(String subject, int grade, String questionText, double min, double max) {
        super(subject, grade, questionText);
        this.min = min;
        this.max = max;
    }
}