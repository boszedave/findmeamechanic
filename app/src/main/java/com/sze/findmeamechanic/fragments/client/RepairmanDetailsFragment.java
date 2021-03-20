package com.sze.findmeamechanic.fragments.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;

import java.util.List;
import java.util.Objects;

public class RepairmanDetailsFragment extends Fragment implements View.OnClickListener {
    private final static String REPAIRMAN_ID = "repID";
    private final static String DOCUMENT_ID = "documentID";
    private TextView repairmanName, profession, companyName, companyAddress, repairmanEmail, repairmanPhone, finishedJobsCount;
    private Button selectRepairman;
    private RatingBar rating;
    private ImageView profilePicture;
    private String repID;
    private String docID;
    private FirestoreManager firestoreManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repairman_details, container, false);

        Bundle bundle = this.getArguments();
        repID = bundle.getString(REPAIRMAN_ID);
        docID = bundle.getString(DOCUMENT_ID);
        profilePicture = view.findViewById(R.id.imageView_profile_picture_repairman);
        repairmanName = view.findViewById(R.id.textView_repairman_name_profile);
        profession = view.findViewById(R.id.textView_profession_name);
        companyName = view.findViewById(R.id.textView_company_name);
        companyAddress = view.findViewById(R.id.textView_company_address);
        repairmanEmail = view.findViewById(R.id.textView_repman_email);
        repairmanPhone = view.findViewById(R.id.textView_repman_phone);
        finishedJobsCount = view.findViewById(R.id.textView_finished_jobs_number_count);
        rating = view.findViewById(R.id.ratingBar);
        selectRepairman = view.findViewById(R.id.select_repairman);
        firestoreManager = new FirestoreManager();

        initDetails();
        selectRepairman.setOnClickListener(this);

        return view;
    }

    private void initDetails() {
        firestoreManager.getRepairmanDetails(repID, new FirestoreManager.GetSnapshotCallback() {
            @Override
            public void onGetFieldCallback(final DocumentSnapshot documentSnapshot) {
                repairmanName.setText(documentSnapshot.getString("repName"));
                profession.setText(documentSnapshot.getString("repProfession"));
                if (!documentSnapshot.getString("repCompanyName").isEmpty()) {
                    companyName.setText(documentSnapshot.getString("repCompanyName"));
                }
                if (!documentSnapshot.getString("repCompanyAddress").isEmpty()) {
                    companyAddress.setText(documentSnapshot.getString("repCompanyAddress"));
                }
                repairmanEmail.setText(documentSnapshot.getString("repEmail"));
                repairmanPhone.setText(documentSnapshot.getString("repPhoneNr"));

                if (documentSnapshot.getString("pathToImage") != null || !documentSnapshot.getString("pathToImage").isEmpty()) {
                    String pictureUrl = documentSnapshot.getString("pathToImage");
                    Glide.with(Objects.requireNonNull(getActivity()))
                            .load(pictureUrl)
                            .fitCenter()
                            .error(ContextCompat.getDrawable(getActivity(), R.drawable.follow))
                            .into(profilePicture);
                }

                firestoreManager.getRepairmanJobsRatings(repID, new FirestoreManager.GetRatingCallback() {
                    @Override
                    public void onRatingCallback(List<Integer> list, int[] counter) {
                        float sumCount = 0;
                        for (int i = 0; i < list.size(); i++) {
                            sumCount += list.get(i);
                        }
                        float finalRating = sumCount / list.size();
                        rating.setRating(finalRating);
                        finishedJobsCount.setText(String.valueOf(counter[0]));
                    }
                });

                //check if this layout was opened through the final applicant cardview - if yes, then change the button function
                firestoreManager.checkForFinalApplicant(docID, new FirestoreManager.GetQueryCallback() {
                    @Override
                    public void onQueryCallback() {
                        selectRepairman.setText(R.string.text_call_repairman);
                        //open up dialer to call the number
                        selectRepairman.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + documentSnapshot.getString("repPhoneNr")));
                                startActivity(intent);
                            }
                        });
                    }
                });

                //check if this layout was opened through the finished job applicant cardview - if yes, then hide the button
                firestoreManager.checkIfJobFinished(docID, new FirestoreManager.GetQueryCallback() {
                    @Override
                    public void onQueryCallback() {
                        selectRepairman.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View v) {
        firestoreManager.selectRepairmanForJob(repID, docID, new FirestoreManager.GetQueryCallback() {
            @Override
            public void onQueryCallback() {
                Toast.makeText(getActivity(), "A szerelő munkavégzésre kiválasztva!", Toast.LENGTH_SHORT).show();
                getFragmentManager().popBackStack();
            }
        });
    }
}
