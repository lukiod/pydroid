package com.py.pydroid_mohak.instructor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    }

    static class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.ViewHolder> {

        private static List<Course> courses;

        CourseAdapter(List<Course> courses) {
            this.courses = courses;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
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

        static class ViewHolder extends RecyclerView.ViewHolder {

            TextView courseNameTextView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                courseNameTextView = itemView.findViewById(R.id.courseNameTextView);
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Course course = courses.get(position);
                        showSubitemsDialog(course.getId(), v.getContext());
                    }
                });
            }

            void bind(Course course) {
                courseNameTextView.setText(course.getName());
            }
        }

        private static void showSubitemsDialog(String courseId, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Subitems");

            // Fetch subitems from Firestore using courseId
            FirebaseFirestore.getInstance()
                    .collection("courses")
                    .document(courseId)
                    .collection("subitems")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Subitem> subitems = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Subitem subitem = document.toObject(Subitem.class);
                            subitems.add(subitem);
                        }

                        // Initialize a StringBuilder to build the message
                        StringBuilder stringBuilder = new StringBuilder();
                        // Add the existing subitems to the message
                        for (Subitem subitem : subitems) {
                            stringBuilder.append(subitem.getName()).append("\n");
                        }
                        String subitemsText = stringBuilder.toString().trim();

                        builder.setMessage(subitemsText);

                        // Add a button to add subitems
                        builder.setPositiveButton("Add Subitem", (dialog, which) -> {
                            // Call a method to handle adding subitems
                            openAddSubitemDialog(courseId, context);
                        });

                        // Add a button to close the dialog
                        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                        builder.create().show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to retrieve subitems: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        private static void openAddSubitemDialog(String courseId, Context context) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Add Subitem");

            // Create new EditTexts for subitem name and link
            final EditText subitemNameEditText = new EditText(context);
            final EditText subitemLinkEditText = new EditText(context);
            subitemNameEditText.setHint("Subitem Name");
            subitemLinkEditText.setHint("Subitem Link");

            // Add the EditTexts to the dialog
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(subitemNameEditText);
            layout.addView(subitemLinkEditText);
            builder.setView(layout);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String subitemName = subitemNameEditText.getText().toString().trim();
                String subitemLink = subitemLinkEditText.getText().toString().trim();
                if (!subitemName.isEmpty() && !subitemLink.isEmpty()) {
                    addSubitemToFirestore(courseId, subitemName, subitemLink, context);
                } else {
                    Toast.makeText(context, "Please enter both subitem name and link", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        }

        private static void addSubitemToFirestore(String courseId, String subitemName, String subitemLink, Context context) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> subitemData = new HashMap<>();
            subitemData.put("name", subitemName);
            subitemData.put("link", subitemLink);

            db.collection("courses")
                    .document(courseId)
                    .collection("subitems")
                    .add(subitemData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(context, "Subitem added successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to add subitem: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
