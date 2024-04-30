package com.py.pydroid_mohak;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;

public class setting extends Fragment {
    private FirebaseAuth mAuth;
    private Button profileButton;
    // ... other button declarations ...
    private Button signOutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // Find view references
        profileButton = view.findViewById(R.id.profileButton);
        // ... other findViewById calls ...
        signOutButton = view.findViewById(R.id.signout); // Make sure the ID matches your button in XML

        mAuth = FirebaseAuth.getInstance();

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
}