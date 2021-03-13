package com.sze.findmeamechanic.containers;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.fragments.client.FinishedJobsFragment;
import com.sze.findmeamechanic.fragments.client.HomeFragment;
import com.sze.findmeamechanic.fragments.client.SettingsFragment;

public class ClientActivityContainer extends AppCompatActivity {
    BottomNavigationView bottomNav;
    FirebaseAuth fAuth;
    FirebaseUser loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_activity_client);

        fAuth = FirebaseAuth.getInstance();
        loggedInUser = fAuth.getCurrentUser();
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (loggedInUser != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_client_activity_container, new HomeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_client_active_jobs:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_client_closed_jobs:
                            selectedFragment = new FinishedJobsFragment();
                            break;
                        case R.id.nav_client_settings:
                            selectedFragment = new SettingsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_client_activity_container, selectedFragment).commit();

                    return true;
                }
            };

}