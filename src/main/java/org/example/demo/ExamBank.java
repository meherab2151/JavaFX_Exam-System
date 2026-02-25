package org.example.demo;

import java.util.ArrayList;

public class ExamBank {
    public static ArrayList<Exam> allExams = new ArrayList<>();

    public static ArrayList<Exam> getLiveExams() {
        ArrayList<Exam> liveOnes = new ArrayList<>();
        for (Exam e : allExams) {
            if (e.isLive()) liveOnes.add(e);
        }
        return liveOnes;
    }
}