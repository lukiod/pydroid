package com.py.pydroid_mohak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.util.*;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {
    EditText email,password;
    Button loginBtn,gotoRegister,fgtpwd;
    ImageButton  pwd;
    boolean valid = true;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        gotoRegister = findViewById(R.id.gotoRegister);
        fgtpwd=findViewById(R.id.frgtpwd);
        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkField(email);
                checkField(password);
                if (valid) {
                    fAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user = authResult.getUser();
                                    if (user.isEmailVerified()) {
                                        // Email verified, proceed with checking access level
                                        Toast.makeText(Login.this, "Loggedin Successfully", Toast.LENGTH_SHORT).show();
                                        checkUserAccessLevel(user.getUid());
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, "Please verify your email to access the app.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Login.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        fgtpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),forgotpwd.class));
            }
        });

        gotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Register.class));
            }
        });

    }

    private void checkUserAccessLevel(String uid) {
        DocumentReference df =fStore.collection("Users").document(uid);
        //extract from  the docs
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("Tag", "OnSuccess: " + documentSnapshot.getData());
                String d=documentSnapshot.getString("isUser");


                if(d.equals("1")){
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                }
                else {
                    startActivity(new Intent(getApplicationContext(),Admin.class));
                    finish();
                }

            }
        });
    }


    public boolean checkField(EditText textField){
        if(textField.getText().toString().isEmpty()){
            textField.setError("Error");
            valid = false;
        }else {
            valid = true;
        }

        return valid;
    }
    @Override
    protected void onStart(){
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

    }
}
