package com.sze.findmeamechanic.fragments.repairman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.Objects;

public class ClientDetailsFragment extends Fragment {
    private final static String DOCUMENT_ID = "documentID";
    private TextView clientName, clientEmail, clientPhoneNr;
    private ImageView profilePicture;
    private Button callClient;
    private String docID;
    private FirestoreManager firestoreManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_client_details, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        docID = bundle.getString(DOCUMENT_ID);
        profilePicture = view.findViewById(R.id.imageView_profile_picture_client);
        clientName = view.findViewById(R.id.textView_client_name_profile);
        clientEmail = view.findViewById(R.id.textView_client_email);
        clientPhoneNr = view.findViewById(R.id.textView_client_phone);
        callClient = view.findViewById(R.id.btn_call_client);
        firestoreManager = new FirestoreManager();

        initDetails();
    }

    private void initDetails() {
        firestoreManager.getClientDetails(docID, new FirestoreManager.GetSnapshotCallback() {
            @Override
            public void onGetFieldCallback(final DocumentSnapshot documentSnapshot) {
                clientName.setText(documentSnapshot.getString("clientName"));
                clientEmail.setText(documentSnapshot.getString("clientEmail"));
                clientPhoneNr.setText(documentSnapshot.getString("clientPhoneNr"));

                if (documentSnapshot.getString("pathToImage") != null || !documentSnapshot.getString("pathToImage").isEmpty()) {
                    String pictureUrl = documentSnapshot.getString("pathToImage");
                    Glide.with(Objects.requireNonNull(getActivity()))
                            .load(pictureUrl)
                            .fitCenter()
                            .error(ContextCompat.getDrawable(getActivity(), R.drawable.follow))
                            .into(profilePicture);
                }

                callClient.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + documentSnapshot.getString("clientPhoneNr")));
                        startActivity(intent);
                    }
                });
            }
        });
    }
}

