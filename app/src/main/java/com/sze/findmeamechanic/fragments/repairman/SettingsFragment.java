package com.sze.findmeamechanic.fragments.repairman;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.firestore.ListenerRegistration;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.activities.MainActivity;
import com.sze.findmeamechanic.managers.FirestoreManager;

import java.util.Random;

public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String SWITCH_STATE = "SWITCH_STATE";
    private static final String SWITCH_KEY = "SWITCH_KEY";
    private boolean switchState;
    private View view;
    private FirestoreManager firestoreManager;
    private TextView modifyProfile;
    private TextView logout;
    private SwitchCompat notify;
    private ListenerRegistration listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_repairman_settings, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences settings = getActivity().getSharedPreferences(SWITCH_STATE, 0);
        switchState = settings.getBoolean(SWITCH_KEY, false);

        firestoreManager = new FirestoreManager();
        modifyProfile = view.findViewById(R.id.settings_profile_edit);
        logout = view.findViewById(R.id.settings_profile_log_out);
        notify = view.findViewById(R.id.switch_settings_notify_job);

        logout.setOnClickListener(this);
        modifyProfile.setOnClickListener(this);
        //set state of switch made earlier
        notify.setChecked(switchState);
        notify.setOnCheckedChangeListener(this);

        listener = firestoreManager.notifyAboutNewApplicant(new FirestoreManager.GetFieldCallback() {
            @Override
            public void onTaskResultCallback(String str) {
                //create notification
                String title = "Új jelentkezés";
                String content = "A(z) " + str + " elnevezésű munkádnál új jelentkezés történt!";
                String CHANNEL_ID =Integer.toString(new Random().nextInt(100));

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_profile_log_out:
                signOut();
                break;
            case R.id.settings_profile_edit:
                Fragment selectFragment = new ProfileModifyFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_repman_activity_container, selectFragment)
                        .addToBackStack(null).commit();
                break;
        }
    }

    private void signOut() {
        firestoreManager.getFAuth().signOut();
        GoogleSignIn.getClient(getContext(), new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut();
        startActivity(new Intent(getActivity(), MainActivity.class));
        getFragmentManager().popBackStack();
        getActivity().finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences settings = getActivity().getSharedPreferences(SWITCH_STATE, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(SWITCH_KEY, isChecked);
        editor.apply();
    }

    @Override
    public void onStop() {
        super.onStop();
        listener.remove();
    }
}
