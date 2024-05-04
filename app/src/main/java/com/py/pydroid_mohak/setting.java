package com.py.pydroid_mohak;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class setting extends Fragment {
    private FirebaseAuth mAuth;
    private Button profileButton;
    // ... other button declarations ...
    private Button signOutButton;
    private Button delete;
    private Button bi;
    private Button instructorButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Find view references
        profileButton = view.findViewById(R.id.profileButton);
        // ... other findViewById calls ...
        signOutButton = view.findViewById(R.id.signout); // Make sure the ID matches your button in XML
        delete=view.findViewById( R.id.deleteAccountButton);
        bi=view.findViewById( R.id.becomeInstructorButton );
        instructorButton=view.findViewById( R.id.instructorButton );
        checkIsUser();


        mAuth = FirebaseAuth.getInstance();
        bi.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(View v) {
                openWebLink( "https://forms.gle/avYDKpjhk7smLEXn6" );
            }

        });
        instructorButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), InstructorActivity.class);
                startActivity(intent);
            }
        } );


        // Set onClickListener for profile button
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Profile activity
                Intent intent = new Intent(getActivity(), Profile.class);
                startActivity(intent);
            }
        });

        // Set onClickListener for sign-out button
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutUser();
            }
        });
        delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserAccount();
            }
        } );

        return view;
    }

    private void signOutUser() {
        mAuth.signOut();
        // Navigate the user to the login screen or another relevant activity
        Intent intent = new Intent(getActivity(), Login.class); // Replace with your LoginActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Optionally, finish the current activity
        getActivity().finish();
    }
    private void deleteUserAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter your current password");

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                if (!password.isEmpty()) {
                    // Call the method to delete the user account with the entered password
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), password);
                        // Proceed with re-authentication and account deletion
                        currentUser.reauthenticate(credential)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // User successfully re-authenticated
                                        // Proceed with account deletion
                                        deleteAccountAfterReAuthentication();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Re-authentication failed
                                        // Handle the error
                                        Toast.makeText(getActivity(), "Re-authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // User is not logged in
                        Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please enter your password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void openWebLink(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    private void checkIsUser() {
        // Get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the user document in Firestore
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(userId);

        // Retrieve the user document from Firestore
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get the value of the "isUser" field
                String isUser = documentSnapshot.getString("isUser");

                // Check if the user is an instructor (isUser != "1")
                if (!"1".equals(isUser)) {
                    // User is an instructor, show the instructorButton
                    instructorButton.setVisibility(View.VISIBLE);
                } else {
                    // User is not an instructor, hide the instructorButton
                    instructorButton.setVisibility(View.GONE);
                }
            } else {
                // User document does not exist
                Log.d(TAG, "User document does not exist");
            }
        }).addOnFailureListener(e -> {
            // Failed to retrieve user document
            Log.e(TAG, "Failed to retrieve user document", e);
        });
    }

    private void deleteAccountAfterReAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Delete Firestore data
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users").document(currentUser.getUid())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Firestore data deleted successfully
                            // Now, delete the user account
                            currentUser.delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // User account deleted successfully
                                            // Navigate to the login screen or another relevant activity
                                            Intent intent = new Intent(getActivity(), Login.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            getActivity().finish(); // Finish the current activity
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Failed to delete user account
                                            // Handle the error
                                            Toast.makeText(getActivity(), "Failed to delete account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to delete Firestore data
                            // Handle the error
                            Toast.makeText(getActivity(), "Failed to delete user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}