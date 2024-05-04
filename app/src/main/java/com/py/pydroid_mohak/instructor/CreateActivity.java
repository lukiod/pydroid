package com.py.pydroid_mohak.instructor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.py.pydroid_mohak.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateActivity extends AppCompatActivity {

    private EditText courseNameEditText, courseIdEditText, courseDescriptionEditText;
    private Button addSubitemButton, saveCourseButton;
    private List<Subitem> subitemsList;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        courseNameEditText = findViewById(R.id.courseNameEditText);
        courseIdEditText = findViewById(R.id.courseIdEditText);
        courseDescriptionEditText = findViewById(R.id.courseDescriptionEditText);
        addSubitemButton = findViewById(R.id.addSubitemButton);
        saveCourseButton = findViewById(R.id.saveCourseButton);

        subitemsList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        addSubitemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddSubitemDialog();
            }
        });

        saveCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCourseToFirestore();
            }
        });
    }

    private void openAddSubitemDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Subitem");

        // Create new EditTexts for subitem name and link
        final EditText subitemNameEditText = new EditText(this);
        final EditText subitemLinkEditText = new EditText(this);
        subitemNameEditText.setHint("Subitem Name");
        subitemLinkEditText.setHint("Subitem Link");

        // Add the EditTexts to the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(subitemNameEditText);
        layout.addView(subitemLinkEditText);
        builder.setView(layout);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String subitemName = subitemNameEditText.getText().toString().trim();
                String subitemLink = subitemLinkEditText.getText().toString().trim();
                if (!subitemName.isEmpty() && !subitemLink.isEmpty()) {
                    Subitem subitem = new Subitem(subitemName, subitemLink);
                    subitemsList.add(subitem);
                    Toast.makeText(CreateActivity.this, "Subitem added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateActivity.this, "Please enter both subitem name and link", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }



// Inside your activity class

    private void saveCourseToFirestore() {
        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in
            Toast.makeText(CreateActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        // Get input values from EditTexts
        String courseName = courseNameEditText.getText().toString().trim();
        String courseId = courseIdEditText.getText().toString().trim();
        String courseDescription = courseDescriptionEditText.getText().toString().trim();

        // Check if any field is empty
        if (courseName.isEmpty() || courseId.isEmpty() || courseDescription.isEmpty()) {
            Toast.makeText(CreateActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map to store course data
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("name", courseName);
        courseData.put("id", courseId);
        courseData.put("description", courseDescription);
        courseData.put("userId", userId); // Add current user ID

        // Create a list to store subitems
        List<Map<String, Object>> subitemsData = new ArrayList<>();
        for (Subitem subitem : subitemsList) {
            Map<String, Object> subitemData = new HashMap<>();
            subitemData.put("name", subitem.getName());
            subitemData.put("link", subitem.getLink());
            subitemsData.add(subitemData);
        }
        courseData.put("subitems", subitemsData);

        // Add course data to Firestore
        db.collection("courses")
                .add(courseData)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CreateActivity.this, "Course created successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Finish the activity
                        } else {
                            Toast.makeText(CreateActivity.this, "Error creating course: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
