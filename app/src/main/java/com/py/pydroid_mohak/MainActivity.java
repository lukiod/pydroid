package com.py.pydroid_mohak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        frameLayout = findViewById(R.id.frame1);

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.chat) {
                    loadFragment(new ChatFragment(), false);
                } else if (itemId == R.id.home1) {
                    loadFragment(new home(), false);
                } else if (itemId == R.id.sear) {
                    loadFragment(new search(), false);
                } else if (itemId == R.id.settin) {
                    loadFragment(new setting(), false);
                }
                // Removed the else block that was always loading 'home' fragment
                return true;
            }
        });

        // Initial fragment load (if needed)
        if (savedInstanceState == null) {
            loadFragment(new home(), true); // Or any other fragment you want to show initially
        }
    }

    public void loadFragment(Fragment fragment, boolean isAppInitialized) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (isAppInitialized) {
            transaction.add(R.id.frame1, fragment);
        } else {
            transaction.replace(R.id.frame1, fragment);
        }
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
    }
}