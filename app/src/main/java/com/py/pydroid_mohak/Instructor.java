package com.py.pydroid_mohak;

public class Instructor {
    private String name;
    private String email;
    private String course;

    public Instructor() {
        // Default constructor required for Firestore
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
