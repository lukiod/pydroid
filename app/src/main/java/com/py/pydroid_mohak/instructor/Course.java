package com.py.pydroid_mohak.instructor;

import java.util.List;

public class Course {
    private String id;
    private String name;
    private String description;
    private List<Subitem> subitems;
    private String userId;

    public Course() {
        // Required empty constructor for Firestore
    }

    public Course(String id, String name, String description, List<Subitem> subitems, String userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.subitems = subitems;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }

    public List<Subitem> getSubitems() {
        return subitems;
    }

    public String getUserId() {
        return userId;
    }
    public void setId(String id) {
        this.id = id;
    }
}
