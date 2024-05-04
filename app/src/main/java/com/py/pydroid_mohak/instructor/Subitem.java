package com.py.pydroid_mohak.instructor;

public class Subitem {
    private String name;
    private String link;

    // Required empty constructor
    public Subitem() {
    }

    // Constructor with arguments
    public Subitem(String name, String link) {
        this.name = name;
        this.link = link;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
