package com.py.pydroid_mohak;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.py.pydroid_mohak.instructor.Course;

import java.util.ArrayList;
import java.util.List;

public class home extends Fragment {

    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courses;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CourseAdapter();
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        courses = new ArrayList<>();

        getCoursesForCurrentUser();

        return view;
    }

    private void getCoursesForCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("courses")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        courses.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Course course = document.toObject(Course.class);
                            courses.add(course);
                        }
                        adapter.setCourses(courses);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to retrieve courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

        private List<Course> courses;

        @SuppressLint("NotifyDataSetChanged")
        void setCourses(List<Course> courses) {
            this.courses = courses;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_button, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Course course = courses.get(position);
            holder.bind(course);
        }

        @Override
        public int getItemCount() {
            return courses == null ? 0 : courses.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView courseNameTextView;
            LinearLayout subitemsLayout;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                courseNameTextView = itemView.findViewById(R.id.courseNameTextView);
                subitemsLayout = itemView.findViewById(R.id.subitemsLayout);
            }

            void bind(Course course) {
                courseNameTextView.setText(course.getName());
                itemView.setOnClickListener(v -> {
                    if (subitemsLayout.getVisibility() == View.GONE) {
                        subitemsLayout.setVisibility(View.VISIBLE);
                        // Fetch and display subitems from Firestore
                        FirebaseFirestore.getInstance()
                                .collection("courses")
                                .document(course.getId()) // courseId should be accessible here
                                .collection("subitems")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    subitemsLayout.removeAllViews(); // Clear existing subitems
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        String subitemName = document.getString("name");
                                        // Create a TextView for each subitem and add it to the layout
                                        TextView subitemTextView = new TextView(itemView.getContext());
                                        subitemTextView.setText(subitemName);
                                        subitemsLayout.addView(subitemTextView);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(itemView.getContext(), "Failed to retrieve subitems: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        subitemsLayout.setVisibility(View.GONE);
                    }
                });
            }
        }
    }
}
