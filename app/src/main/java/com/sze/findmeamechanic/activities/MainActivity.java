package com.sze.findmeamechanic.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.containers.ClientActivityContainer;
import com.sze.findmeamechanic.containers.RepmanActivityContainer;
import com.sze.findmeamechanic.managers.FirestoreManager;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private FirestoreManager fManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fManager = new FirestoreManager();

        //check if user logged in, if not, go to login activity
        if (fManager.getLoggedInUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        } else {
            //if the user id exists in 'Clients' collection, then start client activity, else the user is a repairman
            fManager.getClientName().addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            startActivity(new Intent(getApplicationContext(), ClientActivityContainer.class));
                        } else {
                            startActivity(new Intent(getApplicationContext(), RepmanActivityContainer.class));
                        }
                        finish();
                    }
                }
            });
        }
    }
}