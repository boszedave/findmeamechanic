package com.sze.findmeamechanic.fragments.client;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.activities.LoginActivity;
import com.sze.findmeamechanic.activities.MainActivity;
import com.sze.findmeamechanic.managers.FirestoreManager;

import org.w3c.dom.Text;

public class SettingsFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String SWITCH_STATE = "SWITCH_STATE_2";
    private static final String SWITCH_KEY = "SWITCH_KEY_2";
    private boolean switchState;
    private View view;
    private FirestoreManager firestoreManager;
    private TextView modifyProfile, logout, problemReporter;
    private SwitchCompat notify;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_client_settings, container, false);
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
        problemReporter = view.findViewById(R.id.settings_report_problem);

        logout.setOnClickListener(this);
        modifyProfile.setOnClickListener(this);
        //set state of switch made earlier
        notify.setChecked(switchState);
        notify.setOnCheckedChangeListener(this);
        problemReporter.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_profile_log_out:
                signOut();
                break;
            case R.id.settings_profile_edit:
                Fragment selectFragment = new ProfileModifyFragment();
                getFragmentManager().beginTransaction().replace(R.id.main_client_activity_container, selectFragment)
                        .addToBackStack(null).commit();
                break;
            case R.id.settings_report_problem:
                showReportDialog();
                break;
        }
    }

    private void showReportDialog() {
        LayoutInflater layout = getLayoutInflater();
        View popup = layout.inflate(R.layout.problem_report_dialog, null);
        TextView reportUser = popup.findViewById(R.id.report_user);
        TextView reportBug = popup.findViewById(R.id.report_bug);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Hibabejelentés");
        builder.setView(popup);

        reportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserReportEmail();
            }
        });

        reportBug.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sendBugReportEmail();
            }
        });

        builder.setNegativeButton("Mégsem", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void sendUserReportEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"dev@findmeamechanic.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Felhasználó bejelentése");
        emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.user_report_email_text));
        startActivity(emailIntent);
    }

    private void sendBugReportEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"dev@findmeamechanic.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Általános hibabejelentés");
        emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.bug_report_email_text));
        startActivity(emailIntent);
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
}
