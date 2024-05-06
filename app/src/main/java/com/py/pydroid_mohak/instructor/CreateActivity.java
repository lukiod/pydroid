package com.py.pydroid_mohak.instructor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.py.pydroid_mohak.R;

import java.util.HashMap;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {

    private EditText courseNameEditText, courseIdEditText, courseDescriptionEditText;
    private Button saveCourseButton, deleteCourseButton; // Add delete course button
    private FirebaseFirestore db;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        courseNameEditText = findViewById(R.id.courseNameEditText);
        courseIdEditText = findViewById(R.id.courseIdEditText);
        courseDescriptionEditText = findViewById(R.id.courseDescriptionEditText);
        saveCourseButton = findViewById(R.id.saveCourseButton);
        deleteCourseButton = findViewById(R.id.deleteCourseButton); // Initialize delete course button

        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserID = currentUser.getUid();
        }

        saveCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCourseToFirestore();
            }
        });

        deleteCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String courseId = courseIdEditText.getText().toString().trim();
                if (!courseId.isEmpty()) {
                    deleteCourseFromFirestore(courseId);
                } else {
                    Toast.makeText(CreateActivity.this, "Please enter a course ID to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveCourseToFirestore() {
        String courseName = courseNameEditText.getText().toString().trim();
        String courseId = courseIdEditText.getText().toString().trim();
        String courseDescription = courseDescriptionEditText.getText().toString().trim();

        if (courseName.isEmpty() || courseId.isEmpty() || courseDescription.isEmpty()) {
            Toast.makeText(CreateActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> courseData = new HashMap<>();
        courseData.put("name", courseName);
        courseData.put("id", courseId);
        courseData.put("description", courseDescription);
        courseData.put("userId", currentUserID);

        db.collection("courses")
                .document(courseId)
                .set(courseData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateActivity.this, "Course created successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateActivity.this, "Error creating course: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteCourseFromFirestore(String courseId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("courses").document(courseId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Course deleted successfully
                        Toast.makeText(CreateActivity.this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                        // Optionally, update UI or perform any other actions
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to delete course
                        Toast.makeText(CreateActivity.this, "Failed to delete course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Optionally, handle the error or display an error message to the user
                    }
                });
    }
}
