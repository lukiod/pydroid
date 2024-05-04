package com.py.pydroid_mohak;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class search extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private List<Instructor> instructorList;
    private InstructorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);

        // Initialize RecyclerView and adapter
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        instructorList = new ArrayList<>();
        adapter = new InstructorAdapter(getContext(), instructorList);
        recyclerView.setAdapter(adapter);

        loadInstructors();

        return view;
    }

    private void loadInstructors() {
        instructorList.clear();
        db.collection("Users")
                .whereNotEqualTo("isUser", "1") // Filter out instructors whose isUser field is not "1"
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Instructor instructor = documentSnapshot.toObject(Instructor.class);
                        instructorList.add(instructor);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load instructors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}


