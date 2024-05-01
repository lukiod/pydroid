package com.py.pydroid_mohak;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class Profile extends AppCompatActivity {

    private TextView emailTextView, Name;
    private EditText usernameEditText;
    private Button updateUsernameButton, uploadImageButton;
    private ImageView profileImageView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference profileImagesRef;
    private FirebaseUser currentUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_profile );

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // Initialize profileImagesRef directly with the correct path
        profileImagesRef = FirebaseStorage.getInstance().getReference().child( "profile_images" );

        emailTextView = findViewById( R.id.emailTextView );
        usernameEditText = findViewById( R.id.usernameEditText );
        updateUsernameButton = findViewById( R.id.updateUsernameButton );
        uploadImageButton = findViewById( R.id.uploadImageButton );
        profileImageView = findViewById( R.id.profileImageView );
        Name = findViewById( R.id.name );

        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserDataAndUpdateUI();
            emailTextView.setText( currentUser.getEmail() );
        }

        updateUsernameButton.setOnClickListener( v -> updateUsername() );
        uploadImageButton.setOnClickListener( v -> uploadImage() );
    }

    private void loadUserDataAndUpdateUI() {
        DocumentReference userDocRef = db.collection("Users").document(currentUser.getUid());
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                hideProgressDialog(); // Hide progress dialog after loading

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String username = document.getString("FullName");
                        String profileImageUrl = document.getString("profileImageUrl");

                        // Update UI elements
                        Name.setText(username);
                        if (profileImageUrl != null) {
                            Glide.with(Profile.this)
                                    .load(profileImageUrl)
                                    .into(profileImageView);
                        }
                    }
                } else {
                    Toast.makeText(Profile.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUsername() {
        String newUsername = usernameEditText.getText().toString().trim();
        if (!newUsername.isEmpty()) {
            currentUser.updateProfile( new UserProfileChangeRequest.Builder()
                    .setDisplayName( newUsername )
                    .build() ).addOnCompleteListener( task -> {
                if (task.isSuccessful()) {
                    // Update username in Firestore
                    db.collection( "Users" ).document( currentUser.getUid() ).update( "FullName", newUsername )
                            .addOnSuccessListener( new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText( Profile.this, "Username updated successfully", Toast.LENGTH_SHORT ).show();
                                    usernameEditText.setText( "" );
                                    Name.setText( newUsername );
                                }
                            } )
                            .addOnFailureListener( new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText( Profile.this, "Failed to update username in database", Toast.LENGTH_SHORT ).show();
                                }
                            } );
                } else {
                    Toast.makeText( Profile.this, "Failed to update username", Toast.LENGTH_SHORT ).show();
                }
            } );
        }
    }

    private void uploadImage() {
        Intent intent = new Intent();
        intent.setType( "image/*" );
        intent.setAction( Intent.ACTION_GET_CONTENT );
        startActivityForResult( Intent.createChooser( intent, "Select Image" ), PICK_IMAGE_REQUEST );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();

            // Construct the StorageReference with the desired path and unique file name
            String uniqueFileName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = profileImagesRef.child( currentUser.getUid() + "/" + uniqueFileName );

            // Upload the image to Firebase Storage
            imageRef.putFile( imageUri )
                    .addOnSuccessListener( taskSnapshot -> {
                        // Image uploaded successfully, now get the download URL
                        imageRef.getDownloadUrl().addOnSuccessListener( uri -> {
                            String downloadUrl = uri.toString();

                            // Update the "profileImageUrl" field in Firestore with the download URL
                            DocumentReference userDocRef = db.collection( "Users" ).document( currentUser.getUid() );
                            userDocRef.update( "profileImageUrl", downloadUrl )
                                    .addOnSuccessListener( aVoid -> {
                                        // Update successful
                                        Toast.makeText( Profile.this, "Profile picture updated!", Toast.LENGTH_SHORT ).show();
                                        // You can also update the profileImageView here if needed
                                        Glide.with( Profile.this ).load( downloadUrl ).into( profileImageView );
                                    } )
                                    .addOnFailureListener( e -> {
                                        // Handle update failure
                                        Toast.makeText( Profile.this, "Failed to update profile picture", Toast.LENGTH_SHORT ).show();
                                    } );
                        } );
                    } )
                    .addOnFailureListener( e -> {
                        // Handle unsuccessful uploads
                        Toast.makeText( Profile.this, "Failed to upload image", Toast.LENGTH_SHORT ).show();
                    } );
        }
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog( this );
        progressDialog.setMessage( "Loading..." );
        progressDialog.setCancelable( false );
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}