package com.sze.findmeamechanic.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.managers.ImageUploadManager;
import com.sze.findmeamechanic.managers.ValidationManager;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements ValidationManager, View.OnClickListener {
    private Button registerButton;
    private TextInputLayout registerName, registerEmail, registerPhoneNumber, registerPassword, registerPasswordConf;
    private CardView selectRepManCard, selectClientCard, registerPictureUpload;
    private boolean isRepairman, isGoogleRegistration = false, isUserTypeSelected = false;
    private String pathToImage = "";
    private ImageView profilePicture;
    private ProgressDialog pDialog;
    private Bitmap imageAsBitmap;
    private GoogleSignInAccount googleAccount;
    private FirestoreManager firestoreManager;
    private ImageUploadManager imageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firestoreManager = new FirestoreManager();
        imageManager = new ImageUploadManager();

        registerName = findViewById(R.id.textField_user_name);
        registerEmail = findViewById(R.id.textField_email);
        registerPhoneNumber = findViewById(R.id.textField_phone_number);
        registerPassword = findViewById(R.id.textField_password);
        registerPasswordConf = findViewById(R.id.textField_password_confirm);
        selectRepManCard = findViewById(R.id.cardview_repman);
        selectClientCard = findViewById(R.id.cardview_client);
        registerPictureUpload = findViewById(R.id.cardview_register_image_upload);
        registerButton = findViewById(R.id.button_register);
        profilePicture = findViewById(R.id.imageview_profile_picture);
        googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        askForCameraPermission();
        //if user signed in through Google account, show them another layout
        if (googleAccount != null) {
            setElementsForGoogleRegistration();
        }
        setRole();
        registerButton.setOnClickListener(this);
        registerPictureUpload.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_register:
                final String email = registerEmail.getEditText().getText().toString().trim();
                final String phoneNumber = registerPhoneNumber.getEditText().getText().toString().trim();
                String password = registerPassword.getEditText().getText().toString().trim();
                final String fullName = registerName.getEditText().getText().toString();

                if (isInputValidated()) {
                    pDialog = new ProgressDialog(this);
                    //if user registrates without Google account, then create and save their data through Firestore Authentication
                    if (googleAccount == null) {
                        createUser(email, phoneNumber, password, fullName);
                    } else {
                        addNewUser(fullName, email, phoneNumber);
                    }
                }
                break;
            case R.id.cardview_register_image_upload:
                selectImage(RegisterActivity.this);
                break;
        }
    }

    private void askForCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
    }

    private void setElementsForGoogleRegistration() {
        isGoogleRegistration = true;
        final String email = firestoreManager.getFAuth().getCurrentUser().getEmail();
        final String fullName = firestoreManager.getFAuth().getCurrentUser().getDisplayName();
        registerName.getEditText().setText(fullName);
        registerEmail.getEditText().setText(email);
        registerPassword.setVisibility(View.GONE);
        registerPasswordConf.setVisibility(View.GONE);
    }

    private void setRole() {
        //user is repairman
        selectRepManCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRepairman = true;
                isUserTypeSelected = true;
                selectRepManCard.setCardBackgroundColor(getColor(R.color.buttonBlue));
                selectRepManCard.setCardElevation(15.0f);
                selectClientCard.setCardBackgroundColor(getColor(R.color.white));
                selectClientCard.setCardElevation(2.0f);
            }
        });

        //user is client
        selectClientCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRepairman = false;
                isUserTypeSelected = true;
                selectClientCard.setCardBackgroundColor(getColor(R.color.buttonBlue));
                selectClientCard.setCardElevation(15.0f);
                selectRepManCard.setCardBackgroundColor(getColor(R.color.white));
                selectRepManCard.setCardElevation(2.0f);
            }
        });
    }

    public void selectImage(final Context context) {
        final CharSequence[] options = {"Kamera", "Galéria", "Mégsem"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Kép kiválasztása");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Kamera")) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePicture.resolveActivity(context.getPackageManager()) != null) {
                        startActivityForResult(takePicture, 0);
                    }
                } else if (options[item].equals("Galéria")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);
                } else if (options[item].equals("Mégsem")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        try {
                            imageAsBitmap = (Bitmap) data.getExtras().get("data");
                            //jobImage.setImageBitmap(imageAsBitmap);
                            Glide.with(RegisterActivity.this)
                                    .asBitmap()
                                    .load(imageAsBitmap)
                                    .fitCenter()
                                    .error(ContextCompat.getDrawable(RegisterActivity.this, R.drawable.photo))
                                    .into(profilePicture);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri imageUri = data.getData();
                        try {
                            imageAsBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            Glide.with(RegisterActivity.this)
                                    .asBitmap()
                                    .load(imageAsBitmap)
                                    .fitCenter()
                                    .error(ContextCompat.getDrawable(RegisterActivity.this, R.drawable.photo))
                                    .into(profilePicture);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    private void createUser(final String email, final String phoneNumber, String password, final String fullName) {
        firestoreManager.getFAuth().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Toast.makeText(RegisterActivity.this, "Sikeres regisztráció!", Toast.LENGTH_SHORT).show();
                    addNewUser(fullName, email, phoneNumber);
                } else {
                    String regError = task.getException().toString();
                    Toast.makeText(RegisterActivity.this, "Hiba oka: " + regError, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addNewUser(final String fullName, final String email, final String phoneNumber) {
        //check if a picture is selected
        if (imageAsBitmap != null) {
            imageManager.profilePictureUploader(imageAsBitmap, firestoreManager, new ImageUploadManager.UploadUrlCallback() {
                @Override
                public void onUploadSuccessCallback(String imageUrl) {
                    pathToImage = imageUrl;
                    Snackbar.make(findViewById(android.R.id.content), "Kép sikeresen feltöltve!", Snackbar.LENGTH_LONG).show();
                    saveNewUserByRole(fullName, email, phoneNumber);
                }

                @Override
                public void onUploadFailedCallback() {
                    pDialog.dismiss();
                    Snackbar.make(findViewById(android.R.id.content), "Hiba a feltöltés során! Próbáld újra!", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onUploadProgressListener(double progress) {
                    if (pDialog != null) {
                        pDialog.setTitle("Feltöltés...");
                        pDialog.show();
                        pDialog.setMessage((int) progress + "%");
                    }
                }
            });
        } else {
            saveNewUserByRole(fullName, email, phoneNumber);
        }
    }

    private void saveNewUserByRole(String fullName, String email, String phoneNumber) {
        if (isRepairman) {
            firestoreManager.saveRepairman(fullName, email, phoneNumber, pathToImage);
            pDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), RegisterRepmanActivity.class));
        } else {
            firestoreManager.saveClient(fullName, email, phoneNumber, pathToImage);
            pDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pDialog != null)
            pDialog.dismiss();
    }

    @Override
    public boolean isInputValidated() {
        if (!isUserTypeSelected) {
            Toast.makeText(RegisterActivity.this, "Válassz egy felhasználói típust!", Toast.LENGTH_SHORT).show();
            return false;
        }

        //input validation of registration through google account
        final String email = registerEmail.getEditText().getText().toString().trim();
        final String fullName = registerName.getEditText().getText().toString();
        final String phoneNumber = registerPhoneNumber.getEditText().getText().toString();

        if (!isValidFullName(fullName)) {
            registerName.setError("Rossz formátum!");
            return false;
        } else {
            registerName.setError(null);
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            registerPhoneNumber.setError("Kötelező megadni!");
            return false;
        } else {
            registerPhoneNumber.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerEmail.setError("Rossz formátum!");
            return false;
        } else {
            registerEmail.setError(null);
        }

        //validation of normal registration
        String password = registerPassword.getEditText().getText().toString().trim();
        String confPassword = registerPasswordConf.getEditText().getText().toString().trim();

        if (!isGoogleRegistration) {
            if (!isValidPassword(password)) {
                registerPassword.setError("A jelszónak tartalmaznia kell:\n - Minimum 6 karaktert \n " +
                        "- 1 kis- és nagybetűt \n - 1 számot\n - 1 speciális karaktert");
                return false;
            } else {
                registerPassword.setError(null);
            }
            if (!TextUtils.equals(password, confPassword)) {
                registerPasswordConf.setError("A két jelszó nem egyezik!");
                return false;
            } else {
                registerPasswordConf.setError(null);
            }
        }
        return true;
    }

    @Override
    public boolean isValidPassword(String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }

    @Override
    public boolean isValidFullName(String fullName) {
        Pattern pattern;
        Matcher matcher;
        final String FULL_NAME_PATTERN = "^[\\p{L}\\p{M}]+([\\p{L}\\p{Pd}\\p{Zs}'.]*[\\p{L}\\p{M}])+$|^[\\p{L}\\p{M}]+$";
        pattern = Pattern.compile(FULL_NAME_PATTERN);
        matcher = pattern.matcher(fullName);

        return matcher.matches();
    }

    @Override
    public boolean isValidTaxNumber(String taxNumber) {
        return false;
    }
}