package com.py.pydroid_mohak;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import android.util.Patterns;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    EditText fullName,email,password,phone;
    Button registerBtn,goToLogin;
    boolean valid = true;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        fullName = findViewById(R.id.registerName);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        phone = findViewById(R.id.registerPhone);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkField(fullName);
                checkField(email);
                checkField(password);
                checkField(phone);
                if (valid) {
                    fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user = fAuth.getCurrentUser();
                                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(Register.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                                                DocumentReference df = fStore.collection("Users").document(user.getUid());
                                                Map<String, Object> userInfo = new HashMap<>();
                                                userInfo.put("FullName", fullName.getText().toString());
                                                userInfo.put("UserEmail", email.getText().toString());
                                                userInfo.put("PhoneNumber", phone.getText().toString());
                                                userInfo.put("isUser", "1");
                                                df.set(userInfo);
                                                finish();
                                            } else {
                                                Toast.makeText(Register.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                            }
                                            startActivity(new Intent(getApplicationContext(), Register.class));
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Register.this, "Failed to Create Account", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });
    }



    public boolean checkField(EditText textField){
        valid = true;  // Reset validation flag

        if (TextUtils.isEmpty(fullName.getText().toString())) {
            fullName.setError("Full name is required");
            valid = false;
        }

        if (TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
            email.setError("Invalid email format");
            valid = false;
        } else if (!email.getText().toString().contains("@")) {
            email.setError("Email must contain '@'");
            valid = false;
        }

        if (TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Password is required");
            valid = false;
        }

        if (TextUtils.isEmpty(phone.getText().toString())) {
            phone.setError("Phone number is required");
            valid = false;
        } else {
            // Phone number validation (12 digits with + sign)
            String phoneRegex = "^\\+\\d{12}$";
            Pattern phonePattern = Pattern.compile(phoneRegex);
            Matcher phoneMatcher = phonePattern.matcher(phone.getText().toString());

            if (!phoneMatcher.matches()) {
                phone.setError("Invalid phone number format (+913456789012)");
                valid = false;
            }
        }

        return valid;
    }
}