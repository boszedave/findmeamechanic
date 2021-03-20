package com.sze.findmeamechanic.fragments.repairman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;

public class InProgressJobDetailsFragment extends Fragment implements View.OnClickListener {
    private final static String JOB_ID = "jobId";
    private final static String DOCUMENT_ID = "documentID";
    private String docID;
    private TextView jobName, jobTypeText, jobDescription, jobDeadlineText, jobLocationText, jobDateText, jobSenderName;
    private ImageView jobImage;
    private Button finishJob, chat;
    private FirestoreManager firestoreManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_in_progress_job_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreManager = new FirestoreManager();
        Bundle bundle = this.getArguments();
        docID = bundle.getString(JOB_ID);
        jobName = view.findViewById(R.id.textView_in_progress_job_name);
        jobTypeText = view.findViewById(R.id.textView_in_progress_job_type_text);
        jobDescription = view.findViewById(R.id.textView_in_progress_job_description);
        jobDeadlineText = view.findViewById(R.id.textView_in_progress_job_deadline_text);
        jobLocationText = view.findViewById(R.id.textView_in_progress_job_location_text);
        jobDateText = view.findViewById(R.id.textView_in_progress_job_date_text);
        jobImage = view.findViewById(R.id.imageView_in_progress_job_image);
        jobSenderName = view.findViewById(R.id.textView_in_progress_job_sender_name);
        finishJob = view.findViewById(R.id.button_in_progress_job_close);
        chat = view.findViewById(R.id.button_repman_chat);

        initJobDetails();
        finishJob.setOnClickListener(this);
        jobLocationText.setOnClickListener(this);
        chat.setOnClickListener(this);
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

                if ((!documentSnapshot.getString("jobPictureUrl").isEmpty())) {
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

    public void openGoogleMap() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + jobLocationText.getText().toString());

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        if (mapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_in_progress_job_close:
                Bundle bundle = new Bundle();
                bundle.putString(JOB_ID, docID);
                Fragment selectFragment = new JobSheetFragment();
                selectFragment.setArguments(bundle);
                getFragmentManager().popBackStack();
                getFragmentManager().beginTransaction().replace(R.id.main_repman_activity_container, selectFragment).addToBackStack(null).commit();
                break;
            case R.id.button_repman_chat:
                Bundle bundle2 = new Bundle();
                bundle2.putString(JOB_ID, docID);
                bundle2.putString("client_name", jobSenderName.getText().toString());
                Fragment chatFragment = new ChatFragment();
                chatFragment.setArguments(bundle2);
                getFragmentManager().beginTransaction().replace(R.id.main_repman_activity_container, chatFragment).addToBackStack(null).commit();
                break;
            case R.id.textView_in_progress_job_location_text:
                openGoogleMap();
                break;
        }
    }
}
