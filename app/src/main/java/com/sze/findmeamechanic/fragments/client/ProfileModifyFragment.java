package com.sze.findmeamechanic.fragments.client;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.activities.LoginActivity;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.managers.ImageUploadManager;
import com.sze.findmeamechanic.managers.ValidationManager;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileModifyFragment extends Fragment implements ValidationManager, View.OnClickListener {

    private View view;
    private FirestoreManager firestoreManager;
    private ImageUploadManager imageManager;
    private TextInputLayout userName, userEmail, userPhoneNumber, userOldPassword, userPassword, userPasswordConf;
    private Button deleteProfile, modifyProfile;
    private String pathToImage = "";
    private ProgressDialog pDialog;
    private ImageView profilePicture;
    private Bitmap imageAsBitmap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_client_profile_modify, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreManager = new FirestoreManager();
        imageManager = new ImageUploadManager();
        userName = view.findViewById(R.id.textField_modify_user_name);
        userEmail = view.findViewById(R.id.textField_modify_email);
        userPhoneNumber = view.findViewById(R.id.textField_modify_phone_number);
        userOldPassword = view.findViewById(R.id.textField_modify_old_password);
        userPassword = view.findViewById(R.id.textField_modify_password);
        userPasswordConf = view.findViewById(R.id.textField_modify_password_confirm);
        profilePicture = view.findViewById(R.id.imageview_modify_profile_picture);
        deleteProfile = view.findViewById(R.id.button_delete_profile);
        modifyProfile = view.findViewById(R.id.button_modify_profile);

        initializeData();

        modifyProfile.setOnClickListener(this);
        profilePicture.setOnClickListener(this);
        deleteProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_modify_profile:
                if (isInputValidated()) {
                    pDialog = new ProgressDialog(getActivity());
                    modifyData();
                }
                break;
            case R.id.imageview_modify_profile_picture:
                selectImage(getActivity());
                break;
            case R.id.button_delete_profile:
                if (!userOldPassword.getEditText().getText().toString().isEmpty()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle("Profil törlése");
                    alert.setMessage("Biztosan törlöd a profilod?");
                    alert.setPositiveButton("Igen", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            firestoreManager.deleteUser(firestoreManager.getFAuth().getCurrentUser(), userOldPassword.getEditText().getText().toString(),
                                    new FirestoreManager.GetQueryCallback() {
                                        @Override
                                        public void onQueryCallback() {
                                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Sikeresen törölted a profilodat!", Snackbar.LENGTH_LONG).show();
                                            //redirect to login screen
                                            startActivity(new Intent(getActivity(), LoginActivity.class));
                                            getFragmentManager().popBackStack();
                                            getActivity().finish();
                                        }
                                    });
                        }
                    });
                    alert.setNegativeButton("Nem", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                } else {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "A profilod törléséhez add meg a jelszavad!", Snackbar.LENGTH_LONG).show();
                }
                break;
        }

    }

    private void updateCredentials() {
        String oldPassword = userOldPassword.getEditText().getText().toString().trim();
        String newPassword = userPassword.getEditText().getText().toString();
        String newEmail = userEmail.getEditText().getText().toString();

        firestoreManager.updateCredentials(firestoreManager.getFAuth().getCurrentUser(), oldPassword, newPassword, newEmail, new FirestoreManager.GetQueryCallback() {
            @Override
            public void onQueryCallback() {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Adatok sikeresen módosítva!", Snackbar.LENGTH_LONG).show();
                //clear password fields
                userOldPassword.getEditText().getText().clear();
                userPassword.getEditText().getText().clear();
                userPasswordConf.getEditText().getText().clear();
            }
        });
    }

    private void modifyData() {
        if (imageAsBitmap != null) {
            imageManager.jobPictureUploader(imageAsBitmap, firestoreManager, new ImageUploadManager.UploadUrlCallback() {
                @Override
                public void onUploadSuccessCallback(String imageUrl) {
                    pathToImage = imageUrl;
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Kép sikeresen feltöltve!", Snackbar.LENGTH_LONG).show();
                    firestoreManager.modifyClientData(userName.getEditText().getText().toString(), userPhoneNumber.getEditText().getText().toString(),
                            userEmail.getEditText().getText().toString(), pathToImage, new FirestoreManager.GetQueryCallback() {
                                @Override
                                public void onQueryCallback() {
                                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Adatok sikeresen módosítva!", Snackbar.LENGTH_LONG).show();
                                }
                            });
                    pDialog.dismiss();
                }

                @Override
                public void onUploadFailedCallback() {
                    pDialog.dismiss();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Hiba a feltöltés során! Próbáld újra!", Snackbar.LENGTH_LONG).show();
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
            firestoreManager.modifyClientData(userName.getEditText().getText().toString(), userPhoneNumber.getEditText().getText().toString(),
                    userEmail.getEditText().getText().toString(), pathToImage, new FirestoreManager.GetQueryCallback() {
                        @Override
                        public void onQueryCallback() {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Adatok sikeresen módosítva!", Snackbar.LENGTH_LONG).show();
                        }
                    });
        }

        //update email and/or password
        updateCredentials();
    }

    private void initializeData() {
        firestoreManager.getClientDetails(firestoreManager.getUserID(), new FirestoreManager.GetSnapshotCallback() {
            @Override
            public void onGetFieldCallback(DocumentSnapshot documentSnapshot) {
                userName.getEditText().setText(documentSnapshot.getString("clientName"));
                userEmail.getEditText().setText(documentSnapshot.getString("clientEmail"));
                userPhoneNumber.getEditText().setText(documentSnapshot.getString("clientPhoneNr"));

                if ((!documentSnapshot.getString("pathToImage").isEmpty())) {
                    Glide.with(getActivity())
                            .load(documentSnapshot.getString("pathToImage"))
                            .fitCenter()
                            .error(ContextCompat.getDrawable(getActivity(), R.drawable.photo))
                            .placeholder(R.drawable.photo)
                            .into(profilePicture);
                }
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != getActivity().RESULT_CANCELED) {
            switch (requestCode) {
                //camera
                case 0:
                    if (resultCode == getActivity().RESULT_OK && data != null) {
                        try {
                            imageAsBitmap = (Bitmap) data.getExtras().get("data");
                            profilePicture.setImageBitmap(imageAsBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                //gallery
                case 1:
                    if (resultCode == getActivity().RESULT_OK && data != null) {
                        Uri pictureUrl = data.getData();
                        try {
                            imageAsBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pictureUrl);
                            profilePicture.setImageBitmap(imageAsBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public boolean isInputValidated() {
        final String email = userEmail.getEditText().getText().toString().trim();
        final String fullName = userName.getEditText().getText().toString();
        final String phoneNumber = userPhoneNumber.getEditText().getText().toString();
        String oldPassword = userOldPassword.getEditText().getText().toString().trim();
        String newPassword = userPassword.getEditText().getText().toString().trim();
        String newPassWordConf = userPasswordConf.getEditText().getText().toString().trim();

        if (!isValidFullName(fullName) || TextUtils.isEmpty(fullName)) {
            userName.setError("Rossz formátum!");
            return false;
        } else {
            userName.setError(null);
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            userPhoneNumber.setError("Kötelező megadni!");
            return false;
        } else {
            userPhoneNumber.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || TextUtils.isEmpty(email)) {
            userEmail.setError("Rossz formátum!");
            return false;
        } else {
            userEmail.setError(null);
        }

        if (!oldPassword.isEmpty()) {
            if (TextUtils.isEmpty(newPassword)) {
                userPassword.setError("Kötelező megadni!");
                return false;
            } else {
                userPassword.setError(null);
            }
            if (!TextUtils.isEmpty(newPassword)) {
                if (!TextUtils.equals(newPassword, newPassWordConf)) {
                    userPasswordConf.setError("A két jelszó nem egyezik!");
                    return false;
                } else {
                    userPasswordConf.setError(null);
                }

                if (!isValidPassword(newPassword)) {
                    userPassword.setError("A jelszónak tartalmaznia kell:\n - Minimum 6 karaktert \n " +
                            "- 1 kis- és nagybetűt \n - 1 számot\n - 1 speciális karaktert");
                    return false;
                } else {
                    userPassword.setError(null);
                }
            } else if (TextUtils.isEmpty(newPassword)) {
                userPasswordConf.setError(null);
            } else {
                userPassword.setError(null);
            }

            if (TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(newPassWordConf)) {
                userPassword.setError("Kötelező megadni!");
                return false;
            } else {
                userPassword.setError(null);
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
        Pattern pattern;
        Matcher matcher;
        final String TAX_NUMBER_PATTERN = "^(\\d{11})$";
        pattern = Pattern.compile(TAX_NUMBER_PATTERN);
        matcher = pattern.matcher(taxNumber);

        return matcher.matches();
    }
}
