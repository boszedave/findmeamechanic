package com.sze.findmeamechanic.fragments.client;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.adapters.MessageAdapter;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.models.Message;

public class ChatFragment extends Fragment implements View.OnClickListener {
    private final static String JOB_ID = "jobId";
    FirestoreManager firestoreManager;
    private FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder> adapter;
    private EditText input;
    private String userId, userName, docID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        docID = bundle.getString(JOB_ID);
        userName = bundle.getString("username");

        firestoreManager = new FirestoreManager();
        FloatingActionButton sendMsg = view.findViewById(R.id.floatingButton_client_chat);
        input = view.findViewById(R.id.edittext_client_chat);

        sendMsg.setOnClickListener(this);
        firestoreManager.getMessages(docID);
        final FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>()
                .setQuery(firestoreManager.getMessages(docID), Message.class)
                .build();

        adapter = new MessageAdapter(options, firestoreManager.getUserID(), userName);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_client_chat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floatingButton_client_chat:
                String message = input.getText().toString();
                if (!TextUtils.isEmpty(message))
                    firestoreManager.sendMessages(docID, message);
                input.getText().clear();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
