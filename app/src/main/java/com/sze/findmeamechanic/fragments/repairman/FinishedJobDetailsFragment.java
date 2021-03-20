package com.sze.findmeamechanic.fragments.repairman;

import android.app.DownloadManager;
import android.content.Context;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FinishedJobDetailsFragment extends Fragment implements View.OnClickListener {
    private final static String JOB_ID = "jobId";
    private final static String DOCUMENT_ID = "documentID";
    private TextView jobName, jobType, jobDescription, jobDeadline, jobLocation, jobDatePosted, jobDateFinished, jobSenderName;
    private Button downloadWorksheet;
    private ImageView jobImage;
    private String docID, downloadUrl;
    private FirestoreManager firestoreManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repairman_finished_job_details, container, false);
        firestoreManager = new FirestoreManager();
        Bundle bundle = this.getArguments();
        docID = bundle.getString(JOB_ID);
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
        bottomNav.setVisibility(View.GONE);

        jobName = view.findViewById(R.id.textView_finished_job_details_name_repman);
        jobType = view.findViewById(R.id.textView_finished_job_details_type_text_repman);
        jobDescription = view.findViewById(R.id.textView_finished_job_details_description_text_repman);
        jobImage = view.findViewById(R.id.imageView_finished_job_details_image_repman);
        jobDeadline = view.findViewById(R.id.textView_finished_job_details_deadline_text_repman);
        jobLocation = view.findViewById(R.id.textView_finished_job_details_location_text_repman);
        jobDatePosted = view.findViewById(R.id.textView_finished_job_details_date_posted_text_repman);
        jobDateFinished = view.findViewById(R.id.textView_finished_job_details_date_finished_text_repman);
        jobSenderName = view.findViewById(R.id.textView_job_sender_name_repman);
        downloadWorksheet = view.findViewById(R.id.button_finished_job_details_download_worksheet_repman);

        initJobDetails();
        downloadWorksheet.setOnClickListener(this);

        return view;
    }

    private void initJobDetails() {
        firestoreManager.getFinishedJobDetails(docID, new FirestoreManager.GetSnapshotCallback() {
            @Override
            public void onGetFieldCallback(final DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    jobName.setText(documentSnapshot.getString("jobName"));
                    jobType.setText(documentSnapshot.getString("jobType"));
                    jobDescription.setText(documentSnapshot.getString("jobDescription"));

                    //image url cannot be null - needs to be checked
                    if ((!documentSnapshot.getString("jobPictureUrl").isEmpty())) {
                        Glide.with(getActivity())
                                .load(documentSnapshot.getString("jobPictureUrl"))
                                .fitCenter()
                                .error(ContextCompat.getDrawable(getActivity(), R.drawable.photo))
                                .into(jobImage);
                    }

                    jobDeadline.setText(documentSnapshot.getString("jobDeadline"));
                    jobLocation.setText(documentSnapshot.getString("jobLocation"));
                    jobDatePosted.setText(documentSnapshot.getString("jobDate"));
                    jobDateFinished.setText(documentSnapshot.getString("jobFinishDate"));

                    firestoreManager.getJobSenderName(docID, new FirestoreManager.GetSnapshotCallback() {
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

                    downloadUrl = documentSnapshot.getString("jobSheetPdfUrl");

                }
            }
        });
    }

    public long downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        return downloadmanager.enqueue(request);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_finished_job_details_download_worksheet_repman:
                downloadFile(getActivity(), docID, ".pdf", DIRECTORY_DOWNLOADS, downloadUrl);
                break;
        }
    }
}
