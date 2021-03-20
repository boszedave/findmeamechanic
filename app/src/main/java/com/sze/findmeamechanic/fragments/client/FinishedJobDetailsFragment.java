package com.sze.findmeamechanic.fragments.client;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;

import java.util.Locale;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FinishedJobDetailsFragment extends Fragment implements View.OnClickListener {
    private final static String JOB_ID = "jobId";
    private final static String REPAIRMAN_ID = "repID";
    private final static String DOCUMENT_ID = "documentID";
    private TextView jobName, jobType, jobDescription, jobDeadline, jobLocation, jobDatePosted, jobDateFinished, repairmanName;
    private Button rateJob, downloadWorksheet;
    private ImageView jobImage;
    private CardView repairmanLayout;
    private String docID, downloadUrl;
    private Geocoder geocoder;
    private FirestoreManager firestoreManager;
    private BottomNavigationView bottomNav;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_finished_job_details, container, false);
        firestoreManager = new FirestoreManager();
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        Bundle bundle = this.getArguments();
        docID = bundle.getString(JOB_ID);
        bottomNav = getActivity().findViewById(R.id.bottom_nav);
        bottomNav.setVisibility(View.GONE);

        jobName = view.findViewById(R.id.textView_finished_job_details_name);
        jobType = view.findViewById(R.id.textView_finished_job_details_type_text);
        jobDescription = view.findViewById(R.id.textView_finished_job_details_description_text);
        jobImage = view.findViewById(R.id.imageView_finished_job_details_image);
        jobDeadline = view.findViewById(R.id.textView_finished_job_details_deadline_text);
        jobLocation = view.findViewById(R.id.textView_finished_job_details_location_text);
        jobDatePosted = view.findViewById(R.id.textView_finished_job_details_date_posted_text);
        jobDateFinished = view.findViewById(R.id.textView_finished_job_details_date_finished_text);
        repairmanLayout = view.findViewById(R.id.cardView_selected_repairman);
        repairmanName = view.findViewById(R.id.text_view_finished_job_applicant_name);
        rateJob = view.findViewById(R.id.button_finished_job_details_rate_repman);
        downloadWorksheet = view.findViewById(R.id.button_finished_job_details_download_worksheet);

        initJobDetails();
        rateJob.setOnClickListener(this);
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
                    downloadUrl = documentSnapshot.getString("jobSheetPdfUrl");

                    getRepairman();
                }
            }
        });
    }

    private void getRepairman() {
        //check if client has selected a final repairman for the job yet
        firestoreManager.getFinalRepairmanDetailsOfFinishedJob(docID, new FirestoreManager.GetSnapshotCallback() {
            @Override
            public void onGetFieldCallback(final DocumentSnapshot documentSnapshot) {
                repairmanName.setText(String.format("%s - %s", documentSnapshot.getString("repName"), documentSnapshot.getString("repProfession")));
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
    }

    private void rateJob() {
        LayoutInflater layout = getLayoutInflater();
        final RatingBar ratingBar;
        View popup = layout.inflate(R.layout.popup_rate_job, null);
        ratingBar = popup.findViewById(R.id.popup_ratingBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Munka értékelése");
        builder.setView(popup);
        builder.setPositiveButton("Értékelés", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //float rating = ratingBar.getRating();
                firestoreManager.rate(docID, ratingBar.getRating());
            }
        });
        builder.setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
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
            case R.id.button_finished_job_details_rate_repman:
                rateJob();
                break;
            case R.id.button_finished_job_details_download_worksheet:
                downloadFile(getActivity(), docID, ".pdf", DIRECTORY_DOWNLOADS, downloadUrl);
                break;
        }
    }
}
