package com.py.pydroid_mohak;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class forgotpwd extends AppCompatActivity {

    private EditText emailEditText; // Use descriptive variable names
    private Button forgotPwdBtn;
    private Button backtologin;
    private FirebaseAuth fAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpwd);

        // Find views by ID with error handling
        emailEditText = findViewById(R.id.loginEmail1);
        backtologin=findViewById(R.id.backtologin);
        if (emailEditText == null) {
            Log.e("forgotpwd", "Email EditText not found with ID loginEmail1");
            return;
        }
        forgotPwdBtn = findViewById(R.id.frgtpwd1);
        if (forgotPwdBtn == null) {
            Log.e("forgotpwd", "Forgot Password Button not found with ID frgtpwd1");
            return;
        }

        fAuth = FirebaseAuth.getInstance();
        backtologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        forgotPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = emailEditText.getText().toString().trim();

                if (TextUtils.isEmpty(enteredEmail)) {
                    emailEditText.setError("Please enter your email address");
                    return;
                }

                fAuth.sendPasswordResetEmail(enteredEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(forgotpwd.this, "Password reset instructions sent to your email", Toast.LENGTH_SHORT).show();
                            } else {
                                // Handle potential errors more specifically
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthException e) {
                                    Log.w("forgotpwd", "sendPasswordResetEmail:failure", e);
                                    Toast.makeText(forgotpwd.this, "Error sending password reset instructions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.w("forgotpwd", "Unexpected error:", e);
                                    Toast.makeText(forgotpwd.this, "An unexpected error occurred. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


    }
}
