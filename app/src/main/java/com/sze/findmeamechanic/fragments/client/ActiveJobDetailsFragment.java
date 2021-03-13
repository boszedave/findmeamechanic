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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.adapters.ApplicantsAdapter;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.managers.ImageUploadManager;
import com.sze.findmeamechanic.managers.ValidationManager;
import com.sze.findmeamechanic.models.Repairman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ActiveJobDetailsFragment extends Fragment implements ValidationManager, View.OnClickListener, ApplicantsAdapter.OnApplicantClickListener {
    private final static String REPAIRMAN_ID = "repID";
    private final static String DOCUMENT_ID = "documentID";
    private final static String JOB_ID = "jobId";
    private TextInputLayout jobDeadline;
    private TextView jobName, jobDatePosted, noDataText, jobLocation, jobType, repName, applicantsText;
    private CardView repairmanLayout;
    private TextInputEditText jobDescription;
    private ImageView jobImage;
    private Button deleteJob, modifyJob, startChat;
    private BottomNavigationView bottomNav;
    private ApplicantsAdapter adapter;
    private ImageUploadManager imageManager;
    private String docID, pathToImage;
    private FirestoreManager firestoreManager;
    private Bitmap imageAsBitmap;
    private ProgressDialog pDialog;
    private MaterialDatePicker jobDeadlinePicker;
    private RecyclerView recyclerView;
    private List<Repairman> applicantList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_job_details, container, false);
        firestoreManager = new FirestoreManager();
        imageManager = new ImageUploadManager();

        Bundle bundle = this.getArguments();
        docID = bundle.getString(JOB_ID);
        bottomNav = getActivity().findViewById(R.id.bottom_nav);
        bottomNav.setVisibility(View.GONE);
        jobName = view.findViewById(R.id.textView_job_details_name);
        jobType = view.findViewById(R.id.textView_job_details_type_text);
        jobDescription = view.findViewById(R.id.inputText_job_details_description);
        jobImage = view.findViewById(R.id.imageView_job_details_image);
        jobDeadline = view.findViewById(R.id.textView_job_details_deadline_text);
        jobLocation = view.findViewById(R.id.textView_job_details_location_text);
        jobDatePosted = view.findViewById(R.id.textView_job_details_date_posted_text);
        noDataText = view.findViewById(R.id.textView_job_details_no_applicant_text);
        applicantsText = view.findViewById(R.id.textView_job_details_applicants_text);
        deleteJob = view.findViewById(R.id.button_job_details_delete_job);
        modifyJob = view.findViewById(R.id.button_job_details_modify_job);
        startChat = view.findViewById(R.id.button_client_chat);
        recyclerView = view.findViewById(R.id.recyclerView_job_details_applicants);
        repairmanLayout = view.findViewById(R.id.cardView_applicant);
        repName = view.findViewById(R.id.text_view_applicant_name);
        applicantList = new ArrayList<>();
        repairmanLayout.setVisibility(View.INVISIBLE);

        initRecyclerView();
        initJobDetails();
        setCalendar();
        showDatePicker();
        showSelectedDate();
        setLayoutIfFinalApplicantExists();
        jobImage.setOnClickListener(this);
        deleteJob.setOnClickListener(this);
        modifyJob.setOnClickListener(this);

        return view;
    }

    private void setLayoutIfFinalApplicantExists() {
        //check if client has selected a final repairman for the job yet
        firestoreManager.checkForFinalApplicant(docID, new FirestoreManager.GetQueryCallback() {
            @Override
            public void onQueryCallback() {
                //if client has selected, then change the layout and get the repairman's details
                recyclerView.setVisibility(View.GONE);
                noDataText.setVisibility(View.GONE);
                repairmanLayout.setVisibility(View.VISIBLE);
                applicantsText.setText(R.string.text_final_applicant);
                modifyJob.setEnabled(false);
                firestoreManager.getFinalRepairmanDetails(docID, new FirestoreManager.GetSnapshotCallback() {
                    @Override
                    public void onGetFieldCallback(final DocumentSnapshot documentSnapshot) {
                        repName.setText(String.format("%s - %s", documentSnapshot.getString("repName"), documentSnapshot.getString("repProfession")));
                        repairmanLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //set onclick listener on the cardview and put ids into bundle
                                Bundle bundle = new Bundle();
                                bundle.putString(REPAIRMAN_ID, documentSnapshot.getString("repID"));
                                bundle.putString(DOCUMENT_ID, docID);
                                Fragment selectFragment = new RepairmanDetailsFragment();
                                selectFragment.setArguments(bundle);
                                getFragmentManager().beginTransaction().replace(R.id.main_client_activity_container, selectFragment)
                                        .addToBackStack(null).commit();
                            }
                        });
                    }
                });

                startChat.setVisibility(View.VISIBLE);
                startChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String valami = "asd";
                        if (!repName.getText().toString().isEmpty())
                            valami = repName.getText().toString();
                        Bundle bundle = new Bundle();
                        bundle.putString(JOB_ID, docID);
                        bundle.putString("username", valami);
                        Fragment chatFragment = new ChatFragment();
                        chatFragment.setArguments(bundle);
                        getFragmentManager().beginTransaction().replace(R.id.main_client_activity_container, chatFragment).addToBackStack(null).commit();
                    }
                });
            }
        });

    }

    private void initRecyclerView() {
        adapter = new ApplicantsAdapter(applicantList, this);
        firestoreManager.getApplicants(docID, new FirestoreManager.GetListCallback() {
            @Override
            public void onListCallback(List<DocumentSnapshot> list) {
                //make text gone if there is an applicant for the job
                noDataText.setVisibility(View.GONE);
                for (int i = 0; i < list.size(); i++) {
                    String repName = list.get(i).getString("repName");
                    String repId = list.get(i).getString("repID");
                    //display only the name
                    Repairman rep = new Repairman(repId, repName, "", "", "", "", "", "", "");
                    applicantList.add(rep);
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView_job_details_image:
                selectImage(getActivity());
                break;
            case R.id.button_job_details_delete_job:
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Munka törlése");
                alert.setMessage("Biztosan törlöd a munkát?");
                alert.setPositiveButton("Igen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        firestoreManager.deleteJob(docID);
                        getFragmentManager().popBackStack();
                    }
                });
                alert.setNegativeButton("Nem", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
                break;
            case R.id.button_job_details_modify_job:
                if (isInputValidated()) {
                    pDialog = new ProgressDialog(getActivity());
                    modifyJob();
                }
                break;
        }
    }

    private void modifyJob() {
        final String type = jobType.getText().toString();
        final String description = jobDescription.getEditableText().toString();
        final String deadline = jobDeadline.getEditText().getText().toString();

        if (imageAsBitmap != null) {
            imageManager.jobPictureUploader(imageAsBitmap, firestoreManager, new ImageUploadManager.UploadUrlCallback() {
                @Override
                public void onUploadSuccessCallback(String imageUrl) {
                    pathToImage = imageUrl;
                    firestoreManager.updateJobData(docID, type, description, pathToImage, deadline);
                    //add new job type if not in list
                    firestoreManager.addProfession(type);
                    pDialog.dismiss();
                    Toast.makeText(getActivity(), "Sikeres módosítás!", Toast.LENGTH_SHORT).show();
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
            firestoreManager.updateJobData(docID, type, description, pathToImage, deadline);
            Toast.makeText(getActivity(), "Sikeres módosítás!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initJobDetails() {
        firestoreManager.getActiveJobDetails(docID, new FirestoreManager.GetSnapshotCallback() {
            @Override
            public void onGetFieldCallback(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    jobName.setText(documentSnapshot.getString("jobName"));
                    jobType.setText(documentSnapshot.getString("jobType"));
                    jobDescription.setText(documentSnapshot.getString("jobDescription"));

                    //image url cannot be null - needs to be checked
                    if ((!documentSnapshot.getString("jobPictureUrl").equals("") ||
                            documentSnapshot.getString("jobPictureUrl") != null) && getActivity() != null) {
                        Glide.with(getActivity())
                                .load(documentSnapshot.getString("jobPictureUrl"))
                                .fitCenter()
                                .error(ContextCompat.getDrawable(getActivity(), R.drawable.photo))
                                .into(jobImage);
                        firestoreManager.getImageUrl(docID, new FirestoreManager.GetSnapshotCallback() {
                            @Override
                            public void onGetFieldCallback(DocumentSnapshot documentSnapshot) {
                                pathToImage = documentSnapshot.getString("jobPictureUrl");
                            }
                        });
                    }

                    jobDeadline.getEditText().setText(documentSnapshot.getString("jobDeadline"));
                    jobLocation.setText(documentSnapshot.getString("jobLocation"));
                    jobDatePosted.setText(documentSnapshot.getString("jobDate"));
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

    private void setCalendar() {
        Calendar calendar = Calendar.getInstance();
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        //setting constraints: current date as a start date, ending date within 1 year
        CalendarConstraints.Builder constBuilder = new CalendarConstraints.Builder();
        CalendarConstraints.DateValidator dateValidator = DateValidatorPointForward.now();
        constBuilder.setValidator(dateValidator);
        calendar.roll(Calendar.YEAR, 1);
        constBuilder.setEnd(calendar.getTimeInMillis());
        builder.setCalendarConstraints(constBuilder.build());
        builder.setTitleText("Határidő kiválasztása");
        jobDeadlinePicker = builder.build();
    }

    private void showSelectedDate() {
        jobDeadlinePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                jobDeadline.getEditText().setText(jobDeadlinePicker.getHeaderText());
            }
        });
    }

    private void showDatePicker() {
        jobDeadline.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobDeadlinePicker.show(getFragmentManager(), "DATE_PICKER");
            }
        });
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
                            Glide.with(Objects.requireNonNull(getActivity()))
                                    .asBitmap()
                                    .load(imageAsBitmap)
                                    .fitCenter()
                                    .error(ContextCompat.getDrawable(getActivity(), R.drawable.photo))
                                    .into(jobImage);
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
                            Glide.with(Objects.requireNonNull(getActivity()))
                                    .load(pictureUrl)
                                    .fitCenter()
                                    .error(ContextCompat.getDrawable(getActivity(), R.drawable.photo))
                                    .into(jobImage);
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
        String jobTypeString = jobType.getText().toString();
        String jobDeadlineString = jobDeadline.getEditText().getText().toString();

        if (TextUtils.isEmpty(jobTypeString)) {
            jobType.setError("Kötelező megadni!");
            return false;
        } else {
            jobType.setError(null);
        }
        if (TextUtils.isEmpty(jobDeadlineString)) {
            jobDeadline.setError("Kötelező megadni!");
            return false;
        } else {
            jobDeadline.setError(null);
        }
        return true;
    }

    @Override
    public boolean isValidPassword(String password) {
        return false;
    }

    @Override
    public boolean isValidFullName(String fullName) {
        return false;
    }

    @Override
    public boolean isValidTaxNumber(String taxNumber) {
        return false;
    }

    @Override
    public void onApplicantClick(int position) {
        String repId = applicantList.get(position).getRepID();
        Bundle bundle = new Bundle();
        bundle.putString(REPAIRMAN_ID, repId);
        bundle.putString(DOCUMENT_ID, docID);
        Fragment selectFragment = new RepairmanDetailsFragment();
        selectFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.main_client_activity_container, selectFragment)
                .addToBackStack(null).commit();
    }
}