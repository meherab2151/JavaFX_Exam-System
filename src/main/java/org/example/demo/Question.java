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

    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getSubject() { return subject; }
    public int getGrade() { return grade; }
    public String getQuestionText() { return questionText; }
}

class MCQ extends Question {
    private String[] options;
    private int correctIndex;

    public MCQ(String subject, int grade, String questionText, String[] options, int correctIndex) {
        super(subject, grade, questionText);
        this.options = options;
        this.correctIndex = correctIndex;
    }

    // Getters and Setters for UI
    public String[] getOptions() { return options; }
    public void setOptions(String[] options) { this.options = options; }
    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
}

class TextQuestion extends Question {
    private double answer;

    public TextQuestion(String subject, int grade, String questionText, double answer) {
        super(subject, grade, questionText);
        this.answer = answer;
    }

    // Getters and Setters for UI
    public double getAnswer() { return answer; }
    public void setAnswer(double answer) { this.answer = answer; }
}

class RangeQuestion extends Question {
    private double min, max;

    public RangeQuestion(String subject, int grade, String questionText, double min, double max) {
        super(subject, grade, questionText);
        this.min = min;
        this.max = max;
    }

    // Getters and Setters for UI
    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; }
    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }
}