package com.sze.findmeamechanic.fragments.client;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.adapters.JobsAdapter;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.models.Job;

import java.util.Random;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private static final String SWITCH_STATE = "SWITCH_STATE_2";
    private static final String SWITCH_KEY = "SWITCH_KEY_2";
    private final static String JOB_ID = "jobId";
    private Button postJob;
    private boolean switchState;
    private BottomNavigationView bottomNav;
    private TextView noDataText;
    private JobsAdapter adapter;
    private FirestoreManager firestoreManager;
    private ListenerRegistration listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //hide top navigation bar
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View view = inflater.inflate(R.layout.fragment_client_home, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreManager = new FirestoreManager();
        postJob = view.findViewById(R.id.button_add_job);
        bottomNav = getActivity().findViewById(R.id.bottom_nav);
        bottomNav.setVisibility(View.VISIBLE);
        noDataText = view.findViewById(R.id.textView_no_active_job);

        initRecyclerView(view);
        postJob.setOnClickListener(this);

        SharedPreferences settings = getActivity().getSharedPreferences(SWITCH_STATE, 0);
        switchState = settings.getBoolean(SWITCH_KEY, false);
    }

    private void initRecyclerView(final View view) {
        //display active jobs
        FirestoreRecyclerOptions<Job> options = new FirestoreRecyclerOptions.Builder<Job>()
                .setQuery(firestoreManager.getClientActiveJobs(), Job.class).build();
        adapter = new JobsAdapter(options);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_active_jobs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        noDataText.setVisibility(View.VISIBLE);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                noDataText.setVisibility(View.GONE);
            }
        });
        selectRecyclerViewItem();
    }

    private void selectRecyclerViewItem() {
        adapter.setOnItemClickListener(new JobsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                String jobId = documentSnapshot.getId();
                Bundle bundle = new Bundle();
                bundle.putString(JOB_ID, jobId);
                Fragment selectFragment = new ActiveJobDetailsFragment();
                selectFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.main_client_activity_container, selectFragment)
                        .addToBackStack(null).commit();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_add_job:
                Fragment selectFragment = new PostJobFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_client_activity_container, selectFragment).addToBackStack(null).commit();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

        Log.d("switchstate", "onStart: " + switchState);
        if (switchState) {
            listener = firestoreManager.notifyAboutNewApplicant(new FirestoreManager.GetFieldCallback() {
                @Override
                public void onTaskResultCallback(String str) {
                    //create notification
                    String title = "Új jelentkezés";
                    String content = "A(z) " + str + " elnevezésű munkádnál új jelentkezés történt!";
                    String CHANNEL_ID = Integer.toString(new Random().nextInt(100));

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.support)
                            .setVibrate(new long[]{0, 500})
                            .setContentTitle(title)
                            .setContentText(content)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
                    notificationManager.notify(new Random().nextInt(), builder.build());
                }

                @Override
                public void onSuccessfulQueryCallback() {
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
        if (switchState)
            listener.remove();
    }
}