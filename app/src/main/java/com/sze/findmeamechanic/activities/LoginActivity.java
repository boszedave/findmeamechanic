package com.sze.findmeamechanic.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.WebClientHelper;
import com.sze.findmeamechanic.managers.FirestoreManager;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE = 1005;
    private TextInputLayout loginEmail, loginPassword, resetPasswordEmail;
    private String forgottenEmail;
    private Button loginButton;
    private TextView registerText, forgottenPassword;
    private ImageView signInWithGoogle;
    private GoogleSignInOptions googleSignInOptions;
    private GoogleSignInClient googleClient;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firestoreManager = new FirestoreManager();

        loginEmail = findViewById(R.id.textField_login_email);
        loginPassword = findViewById(R.id.textField_login_password);
        registerText = findViewById(R.id.textview_regText);
        forgottenPassword = findViewById(R.id.textview_login_forgotten_pass);
        loginButton = findViewById(R.id.button_login);
        signInWithGoogle = findViewById(R.id.imageview_login_google);
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(new WebClientHelper().getClientStuff())
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, googleSignInOptions);

        loginButton.setOnClickListener(this);
        registerText.setOnClickListener(this);
        forgottenPassword.setOnClickListener(this);
        signInWithGoogle.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageview_login_google:
                signInWithGoogle();
                break;
            case R.id.button_login:
                logInUser();
                break;
            case R.id.textview_regText:
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                break;
            case R.id.textview_login_forgotten_pass:
                resetPassword();
                break;
        }
    }

    private void logInUser() {
        String email = loginEmail.getEditText().getText().toString().trim();
        String password = loginPassword.getEditText().getText().toString().trim();

        if (email.length() != 0 && password.length() != 0) {
            firestoreManager.getFAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Hiba történt! Oka: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void resetPassword() {
        LayoutInflater layout = getLayoutInflater();
        View popup = layout.inflate(R.layout.popup_password_reset, null);
        resetPasswordEmail = popup.findViewById(R.id.textField_reset_pass_email);

        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Elfelejtett jelszó");
        builder.setView(popup);
        builder.setPositiveButton("Jelszó küldése", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                forgottenEmail = resetPasswordEmail.getEditText().getText().toString();
                if (forgottenEmail.length() != 0) {
                    if (Patterns.EMAIL_ADDRESS.matcher(forgottenEmail).matches()) {
                        firestoreManager.resetPassword(forgottenEmail, LoginActivity.this);
                        Toast.makeText(LoginActivity.this, "A jelszó emlékeztetőt kiküldtük az e-mail címedre!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Rossz e-mail-t adtál meg. Próbáld újra!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            final AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

            //create user and store their data in Firestore Authentication
            firestoreManager.getFAuth().signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        //check if user has been registered before
                        if (isNewUser) {
                            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                        } else {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                        finish();
                    }
                }
            });
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }
}