package com.py.pydroid_mohak;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.py.pydroid_mohak.instructor.Course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class home extends Fragment {

    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courses;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.fragment_home, container, false);

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
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        courses.clear();
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Course course = document.toObject(Course.class);
                            courses.add(course);

                            // Count total number of subitems for each course and store in the database
                            countAndStoreTotalSubitems(course.getId());
                        }
                        adapter.setCourses(courses);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getActivity(), "Failed to retrieve courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void countAndStoreTotalSubitems(String courseId) {
        FirebaseFirestore.getInstance()
                .collection("courses")
                .document(courseId)
                .collection("subitems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalSubitems = queryDocumentSnapshots.size();
                    // Store totalSubitems in the database under the corresponding course document
                    storeTotalSubitemsInDatabase(courseId, totalSubitems);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to count total subitems: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void storeTotalSubitemsInDatabase(String courseId, int totalSubitems) {
        FirebaseFirestore.getInstance()
                .collection("courses")
                .document(courseId)
                .update("totalSubitems", totalSubitems)
                .addOnSuccessListener(aVoid -> {
                    // Total subitems stored in the database successfully
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to store total subitems in database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
            Button enrollmentButton;
            private Set<String> openedSubitemIds = new HashSet<>();

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                courseNameTextView = itemView.findViewById(R.id.courseNameTextView);
                subitemsLayout = itemView.findViewById(R.id.subitemsLayout);
                enrollmentButton = itemView.findViewById(R.id.enrollmentButton);
            }

            void bind(Course course) {
                courseNameTextView.setText(course.getName());
                itemView.setOnClickListener(v -> {
                    if (subitemsLayout.getVisibility() == View.GONE) {
                        subitemsLayout.setVisibility(View.VISIBLE);
                        FirebaseFirestore.getInstance()
                                .collection("courses")
                                .document(course.getId())
                                .collection("subitems")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    subitemsLayout.removeAllViews();
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        String subitemName = document.getString("name");
                                        String subitemLink = document.getString("link");

                                        TextView subitemTextView = new TextView(itemView.getContext());
                                        subitemTextView.setText(subitemName);
                                        subitemTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                                        subitemTextView.setPadding(40, 20, 0, 20);
                                        subitemTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT));

                                        subitemTextView.setOnClickListener(subitemView -> {
                                            String subitemId = document.getId();
                                            if (!openedSubitemIds.contains(subitemId)) {
                                                openedSubitemIds.add(subitemId); // Add the ID to the Set
                                                updateSubitemCountAndCheckCompletion(course.getId(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                                            }

                                            // Proceed to open the subitem link
                                            if (subitemLink != null && !subitemLink.isEmpty()) {
                                                // Open the link
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(subitemLink));
                                                itemView.getContext().startActivity(browserIntent);
                                            } else {
                                                Toast.makeText(itemView.getContext(), "No link available for this subitem", Toast.LENGTH_SHORT).show();
                                            }
                                        });

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

                enrollmentButton.setOnClickListener(v -> {
                    // Hide the button after clicking
                    enrollmentButton.setVisibility(View.GONE);

                    // Update pending and completed courses in Firestore
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        FirebaseFirestore.getInstance()
                                .collection("Users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        int pendingCourses = documentSnapshot.getLong("pendingCourses") != null ? documentSnapshot.getLong("pendingCourses").intValue() : 0;
                                        int completedCourses = documentSnapshot.getLong("completedCourses") != null ? documentSnapshot.getLong("completedCourses").intValue() : 0;

                                        // Increment pending courses count
                                        pendingCourses++;

                                        // Update Firestore
                                        FirebaseFirestore.getInstance()
                                                .collection("Users")
                                                .document(userId)
                                                .update("pendingCourses", pendingCourses)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(itemView.getContext(), "Course enrolled successfully!", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(itemView.getContext(), "Failed to enroll in course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });

                                        // Check if the number of totalSubitems matches completed subitems
                                        checkCourseCompletion(course.getId(), userId);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(itemView.getContext(), "Failed to enroll in course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                });
            }

            private void updateSubitemCountAndCheckCompletion(String courseId, String userId) {
                FirebaseFirestore.getInstance()
                        .collection("userCourseProgress")
                        .document(userId)
                        .collection("courses")
                        .document(courseId)
                        .update("completedSubitems", FieldValue.increment(1))
                        .addOnSuccessListener(aVoid -> {
                            // Document updated successfully, check course completion
                            checkCourseCompletion(courseId, userId);
                        })
                        .addOnFailureListener(e -> {
                            // If the document doesn't exist, create it and then update the count
                            if (e.getMessage().contains("No document to update")) {
                                createDocumentAndUpdateCount(courseId, userId);
                            } else {
                                // Other failure reasons
                                Toast.makeText(itemView.getContext(), "Failed to update subitem count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            private void createDocumentAndUpdateCount(String courseId, String userId) {
                Map<String, Object> data = new HashMap<>();
                data.put("completedSubitems", 1); // Initial count

                FirebaseFirestore.getInstance()
                        .collection("userCourseProgress")
                        .document(userId)
                        .collection("courses")
                        .document(courseId)
                        .set(data)
                        .addOnSuccessListener(aVoid -> {
                            // Document created successfully, update the count
                            updateSubitemCountAndCheckCompletion(courseId, userId);
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure to create document
                            Toast.makeText(itemView.getContext(), "Failed to create document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            private void checkCourseCompletion(String courseId, String userId) {
                FirebaseFirestore.getInstance()
                        .collection("userCourseProgress")
                        .document(userId)
                        .collection("courses")
                        .document(courseId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                int completedSubitems = documentSnapshot.getLong("completedSubitems").intValue();
                                int totalSubitems = documentSnapshot.getLong("totalSubitems").intValue();
                                if (completedSubitems == totalSubitems) {
                                    // All subitems are completed
                                    // Mark the course as completed
                                    markCourseAsCompleted(courseId, userId);
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(itemView.getContext(), "Failed to check course completion status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            private void markCourseAsCompleted(String courseId, String userId) {
                FirebaseFirestore.getInstance()
                        .collection("userCourseProgress")
                        .document(userId)
                        .collection("courses")
                        .document(courseId)
                        .update("completed", true)
                        .addOnSuccessListener(aVoid -> {
                            // Course marked as completed
                            Toast.makeText(itemView.getContext(), "Congratulations! You have completed this course.", Toast.LENGTH_SHORT).show();

                            // Increment completed courses count
                            FirebaseFirestore.getInstance()
                                    .collection("Users")
                                    .document(userId)
                                    .update("completedCourses", FieldValue.increment(1))
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(itemView.getContext(), "Failed to increment completed courses count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(itemView.getContext(), "Failed to mark course as completed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }
}
