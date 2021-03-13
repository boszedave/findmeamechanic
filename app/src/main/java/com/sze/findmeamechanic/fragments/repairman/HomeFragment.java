package com.sze.findmeamechanic.fragments.repairman;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.adapters.NearbyJobsAdapter;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.models.NearbyJob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class HomeFragment extends Fragment implements LocationListener, NearbyJobsAdapter.OnJobClickListener,
        View.OnClickListener, Slider.OnSliderTouchListener, Slider.OnChangeListener {

    private static final String SWITCH_STATE = "SWITCH_STATE";
    private static final String SWITCH_KEY = "SWITCH_KEY";
    private boolean switchState;
    private FirestoreManager firestoreManager;
    private NearbyJobsAdapter adapter;
    private List<NearbyJob> nearbyJobs;
    private LocationManager locationManager;
    private double lat, lng;
    private RecyclerView recyclerView;
    private Slider slider;
    private Spinner jobType;
    private int radius;
    private ProgressBar pBar;
    private ListenerRegistration listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_repairman_home, container, false);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreManager = new FirestoreManager();
        recyclerView = view.findViewById(R.id.recyclerView_active_jobs_repman);
        Button filterButton = view.findViewById(R.id.button_filter);
        slider = view.findViewById(R.id.seekBar);
        pBar = view.findViewById(R.id.pbar_repairman);
        nearbyJobs = new ArrayList<>();

        getGPSLocation();
        filterButton.setOnClickListener(this);
        slider.addOnChangeListener(this);
        slider.addOnSliderTouchListener(this);
        setSliderValue();

        SharedPreferences settings = getActivity().getSharedPreferences(SWITCH_STATE, 0);
        switchState = settings.getBoolean(SWITCH_KEY, false);
    }

    private void setSliderValue() {
        slider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                if (value == 120.0) {
                    return ">" + (int) value + " km";
                } else {
                    return (int) value + " km";
                }
            }
        });
        //set last position value of slider for radius
        radius = (int) slider.getValueFrom();
    }

    private void initRecyclerView() {
        pBar.setVisibility(View.VISIBLE);
        adapter = new NearbyJobsAdapter(nearbyJobs, this);
        //clear list so new changes won't be added to an existing result list
        nearbyJobs.clear();

        firestoreManager.queryLocationHashes(lat, lng, radius, new FirestoreManager.GetListCallback() {
            @Override
            public void onListCallback(List<DocumentSnapshot> list) {
                for (int i = 0; i < list.size(); i++) {
                    //add results to an object by getting the data from documentsnapshot and pass that object to the adapter
                    String name = list.get(i).getString("jobName");
                    String desc = list.get(i).getString("jobDescription");
                    String date = list.get(i).getString("jobDate");
                    String id = list.get(i).getString("jobID");
                    String type = list.get(i).getString("jobType");

                    NearbyJob job = new NearbyJob(name, desc, date, id, type);
                    nearbyJobs.add(job);
                    adapter.notifyDataSetChanged();
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setAdapter(adapter);
                pBar.setVisibility(View.GONE);
            }
        });
    }

    private void getGPSLocation() {
        locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    private void showFilters() {
        LayoutInflater layout = getLayoutInflater();
        View popup = layout.inflate(R.layout.popup_filter, null);
        jobType = popup.findViewById(R.id.popup_spinner_job_type);
        getProfessionList(jobType);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Szűrés munkatípus szerint");
        builder.setView(popup);
        builder.setPositiveButton("Szűrés", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                filterJobs();
            }
        });
        builder.setNegativeButton("Szűrő törlése", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                initRecyclerView();
            }
        });
        builder.show();
    }

    private void filterJobs() {
        List<NearbyJob> filteredJobs = new ArrayList<>();
        String type = jobType.getSelectedItem().toString();

        for (int i = 0; i < nearbyJobs.size(); i++) {
            if (nearbyJobs.get(i).getType().equals(type)) {
                filteredJobs.add(nearbyJobs.get(i));
            }
        }
        adapter = new NearbyJobsAdapter(filteredJobs, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    private void getProfessionList(Spinner jobType) {
        final List<String> professionList = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, professionList);

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        jobType.setAdapter(adapter);
        firestoreManager.getProfessionList(new FirestoreManager.GetFieldCallback() {
            @Override
            public void onTaskResultCallback(String professionName) {
                professionList.add(professionName);
            }

            @Override
            public void onSuccessfulQueryCallback() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        locationManager.removeUpdates(this);
        initRecyclerView();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onStart() {
        super.onStart();

        if (switchState) {
            listener = firestoreManager.notifyApplicant(firestoreManager.getUserID(), new FirestoreManager.GetFieldCallback() {
                @Override
                public void onTaskResultCallback(String str) {
                    //create notification
                    String title = "Új jelentkezés";
                    String content = "A(z) " + str + " nevű munkára elfogadtak/változás történt";
                    String CHANNEL_ID = Integer.toString(new Random().nextInt(100));

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.support)
                            .setVibrate(new long[]{0, 500})
                            .setContentTitle(title)
                            .setContentText(content)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

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
        listener.remove();
    }

    @Override
    public void onJobClick(int position) {
        //get data from specific item and display it in another fragment
        String jobId = nearbyJobs.get(position).getId();
        Bundle bundle = new Bundle();
        bundle.putString("jobId", jobId);
        Fragment selectFragment = new ApplyJobFragment();
        selectFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.main_repman_activity_container, selectFragment)
                .addToBackStack(null).commit();
    }

    @Override
    public void onClick(View v) {
        showFilters();
    }

    @Override
    public void onStartTrackingTouch(@NonNull Slider slider) {
    }

    @Override
    public void onStopTrackingTouch(@NonNull Slider slider) {
        initRecyclerView();
    }

    @Override
    public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
        radius = (int) value;
    }
}