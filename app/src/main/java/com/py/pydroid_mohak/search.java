package com.py.pydroid_mohak;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.py.pydroid_mohak.instructor.Course;
import com.py.pydroid_mohak.instructor.CourseAdapter;

import java.util.ArrayList;
import java.util.List;

public class search extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private List<Course> courseList;
    private CourseAdapter adapter;
    private EditText searchEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);

        // Initialize RecyclerView and adapter
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        courseList = new ArrayList<>();
        adapter = new CourseAdapter(getContext(), courseList);
        recyclerView.setAdapter(adapter);

        loadAllCourses();

        // Set up search functionality
        view.findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchTerm = searchEditText.getText().toString().trim();
                if (!searchTerm.isEmpty()) {
                    searchCourses(searchTerm);
                } else {
                    loadAllCourses();
                }
            }
        });

        return view;
    }

    private void loadAllCourses() {
        courseList.clear();
        db.collection("courses")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Course course = documentSnapshot.toObject(Course.class);
                            courseList.add(course);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to load courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchCourses(String searchTerm) {
        courseList.clear();
        db.collection("courses")
                .whereEqualTo("name", searchTerm)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Course course = documentSnapshot.toObject(Course.class);
                            courseList.add(course);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to search courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
