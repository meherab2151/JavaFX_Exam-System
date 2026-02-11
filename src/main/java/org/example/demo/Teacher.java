package org.example.demo;

public class Teacher {
    private String fullName;
    private String email;
    private String password;

    public Teacher(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    // Getters for login check
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}