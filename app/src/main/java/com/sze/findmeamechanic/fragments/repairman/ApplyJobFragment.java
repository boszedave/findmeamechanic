package com.sze.findmeamechanic.fragments.repairman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;

public class ApplyJobFragment extends Fragment implements View.OnClickListener {
    private final static String JOB_ID = "jobId";
    private final static String DOCUMENT_ID = "documentID";
    private String docID;
    private TextView jobName, jobTypeText, jobDescription, jobDeadlineText, jobLocationText, jobDateText, jobSenderName;
    private ImageView jobImage;
    private Button applyToJob;
    private FirestoreManager firestoreManager;
    private ProgressBar pBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apply_job, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreManager = new FirestoreManager();
        Bundle bundle = this.getArguments();
        docID = bundle.getString(JOB_ID);
        jobName = view.findViewById(R.id.textView_apply_job_name);
        jobTypeText = view.findViewById(R.id.textView_apply_job_type_text);
        jobDescription = view.findViewById(R.id.textView_apply_job_description);
        jobDeadlineText = view.findViewById(R.id.textView_apply_job_deadline_text);
        jobLocationText = view.findViewById(R.id.textView_apply_job_location_text);
        jobDateText = view.findViewById(R.id.textView_apply_job_date_text);
        jobImage = view.findViewById(R.id.imageView_apply_job_image);
        applyToJob = view.findViewById(R.id.button_apply_job_apply);
        jobSenderName = view.findViewById(R.id.textView_apply_job_sender_name);

        pBar = view.findViewById(R.id.pbar_apply_repairman);
        pBar.setVisibility(View.INVISIBLE);

        initJobDetails();
        checkIfAppliedToJobYet();
        applyToJob.setOnClickListener(this);
        jobLocationText.setOnClickListener(this);
    }

    private void initJobDetails() {
        firestoreManager.getActiveJobDetails(docID, new FirestoreManager.GetSnapshotCallback() {
            @Override
            public void onGetFieldCallback(DocumentSnapshot documentSnapshot) {
                jobName.setText(documentSnapshot.getString("jobName"));
                jobTypeText.setText(documentSnapshot.getString("jobType"));
                jobDescription.setText(documentSnapshot.getString("jobDescription"));
                jobDeadlineText.setText(documentSnapshot.getString("jobDeadline"));
                jobDateText.setText(documentSnapshot.getString("jobDate"));
                jobLocationText.setText(documentSnapshot.getString("jobLocation"));

                if ((documentSnapshot.getString("jobPictureUrl") != "" ||
                        documentSnapshot.getString("jobPictureUrl") != null) && getActivity() != null) {
                    Glide.with(getActivity())
                            .load(documentSnapshot.getString("jobPictureUrl"))
                            .fitCenter()
                            .error(ContextCompat.getDrawable(getActivity(), R.drawable.photo))
                            .into(jobImage);
                }

                firestoreManager.getJobSenderDetails(docID, new FirestoreManager.GetSnapshotCallback() {
                    @Override
                    public void onGetFieldCallback(DocumentSnapshot documentSnapshot) {
                        String name = documentSnapshot.getString("clientName");
                        final String id = documentSnapshot.getString("clientID");
                        jobSenderName.setText(name);
                        jobSenderName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putString(DOCUMENT_ID, id);
                                Fragment selectFragment = new ClientDetailsFragment();
                                selectFragment.setArguments(bundle);
                                getFragmentManager().beginTransaction().replace(R.id.main_repman_activity_container, selectFragment)
                                        .addToBackStack(null).commit();
                            }
                        });
                    }
                });

            }
        });
    }

    private void checkIfAppliedToJobYet() {
        firestoreManager.checkIfApplied(docID, new FirestoreManager.GetQueryCallback() {
            @Override
            public void onQueryCallback() {
                applyToJob.setText(R.string.text_button_apply_job);
                applyToJob.setEnabled(false);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_apply_job_apply:
                pBar.setVisibility(View.VISIBLE);
                applyToJob.setText("");
                firestoreManager.applyToJob(docID, new FirestoreManager.GetQueryCallback() {
                    @Override
                    public void onQueryCallback() {
                        applyToJob.setText(R.string.text_button_apply_job);
                        applyToJob.setEnabled(false);
                        pBar.setVisibility(View.GONE);
                    }
                });
                break;
            case R.id.textView_apply_job_location_text:
                openGoogleMap();
                break;
        }
    }

    private void openGoogleMap() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + jobLocationText.getText().toString());

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }
}
