package com.py.pydroid_mohak.instructor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.py.pydroid_mohak.R;

import java.util.ArrayList;
import java.util.List;

public class CourseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);

        recyclerView = findViewById(R.id.recyclerView);
        db = FirebaseFirestore.getInstance();

        getCoursesForCurrentUser();
    }

    private void getCoursesForCurrentUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("courses")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Course> courses = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Course course = document.toObject(Course.class);
                            courses.add(course);
                        }
                        displayCourses(courses);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CourseActivity.this, "Failed to retrieve courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void displayCourses(List<Course> courses) {
        CourseAdapter adapter = new CourseAdapter(courses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener((position, view) -> {
            Course course = courses.get(position);
            showSubitemsDialog(course.getSubitems());
        });
    }

    private void showSubitemsDialog(List<Subitem> subitems) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Subitems");

        StringBuilder stringBuilder = new StringBuilder();
        for (Subitem subitem : subitems) {
            stringBuilder.append(subitem.getName()).append("\n");
        }
        String subitemsText = stringBuilder.toString().trim();

        builder.setMessage(subitemsText);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

        private List<Course> courses;
        private static OnItemClickListener listener;

        CourseAdapter(List<Course> courses) {
            this.courses = courses;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.item_course, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Course course = courses.get(position);
            holder.bind(course);
        }

        @Override
        public int getItemCount() {
            return courses.size();
        }

        void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        interface OnItemClickListener {
            void onItemClick(int position, View view);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView courseNameTextView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                courseNameTextView = itemView.findViewById(R.id.courseNameTextView);
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position, v);
                        }
                    }
                });
            }

            void bind(Course course) {
                courseNameTextView.setText(course.getName());
            }
        }
    }
}
