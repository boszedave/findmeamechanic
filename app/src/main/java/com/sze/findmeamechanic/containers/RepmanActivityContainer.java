package com.sze.findmeamechanic.containers;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.fragments.client.RepairmanDetailsFragment;
import com.sze.findmeamechanic.fragments.repairman.FinishedJobsFragment;
import com.sze.findmeamechanic.fragments.repairman.HomeFragment;
import com.sze.findmeamechanic.fragments.repairman.JobsInProgressFragment;
import com.sze.findmeamechanic.fragments.repairman.SettingsFragment;
import com.sze.findmeamechanic.managers.FirestoreManager;

public class RepmanActivityContainer extends AppCompatActivity {

    BottomNavigationView bottomNav;
   // FirebaseAuth fAuth;
   // FirebaseUser loggedInUser;

    FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_activity_repman);

        firestoreManager = new FirestoreManager();

        // fAuth = FirebaseAuth.getInstance();
        //  loggedInUser = fAuth.getCurrentUser();
        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        if (firestoreManager.getLoggedInUser() != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.main_repman_activity_container, new HomeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.nav_repman_home_jobs:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_repman_active_jobs:
                            selectedFragment = new JobsInProgressFragment();
                            break;
                       case R.id.nav_repman_closed_jobs:
                            selectedFragment = new FinishedJobsFragment();
                            break;
                        case R.id.nav_repman_settings:
                            selectedFragment = new SettingsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_repman_activity_container, selectedFragment).commit();

                    return true;
                }
            };
}
