package com.sze.findmeamechanic.fragments.repairman;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.adapters.JobsAdapter;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.models.Job;

public class JobsInProgressFragment extends Fragment {
    private final static String JOB_ID = "jobId";
    private FirestoreManager firestoreManager;
    private JobsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_repairman_jobs_in_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreManager = new FirestoreManager();
        firestoreManager.getRepairmanActiveJobs();
        initRecyclerView(view);
    }

    private void initRecyclerView(final View view) {
        //display active jobs
        FirestoreRecyclerOptions<Job> options = new FirestoreRecyclerOptions.Builder<Job>()
                .setQuery(firestoreManager.getRepairmanActiveJobs(), Job.class).build();
        adapter = new JobsAdapter(options);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_active_jobs_repman2);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        selectRecyclerViewItem();
    }

    private void selectRecyclerViewItem() {
        adapter.setOnItemClickListener(new JobsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                String jobId = documentSnapshot.getId();
                Bundle bundle = new Bundle();
                bundle.putString(JOB_ID, jobId);
                Fragment selectFragment = new InProgressJobDetailsFragment();
                selectFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.main_repman_activity_container, selectFragment)
                        .addToBackStack(null).commit();
            }
        });
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