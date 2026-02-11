package org.example.demo;

public class Student {
    private String studentID;
    private String name;
    private String email;
    private String password;

    public Student(String studentID, String name, String email, String password) {
        this.studentID = studentID;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getID() { return studentID; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}