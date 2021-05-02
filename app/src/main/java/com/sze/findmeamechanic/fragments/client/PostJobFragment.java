package com.sze.findmeamechanic.fragments.client;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.managers.ImageUploadManager;
import com.sze.findmeamechanic.managers.ValidationManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PostJobFragment extends Fragment implements ValidationManager, View.OnClickListener, LocationListener {
    private static final double MAX_DISTANCE_BETWEEN_LOCATIONS = 15000.0;
    private String name, description, timestamp, location, type, deadline, pathToImage;
    private boolean isLocationTooFar = false, isLocationChanged = false;
    private ArrayList<Double> gpsLocationCoordinates, inputLocationCoordinates;
    private Map<String, Object> geoHashLocation;
    private LocationManager locationManager;
    private TextInputLayout jobName, jobDescription, jobLocation, jobDeadline;
    private AutoCompleteTextView jobType;
    private ImageView jobPicture;
    private Bitmap imageAsBitmap;
    private ProgressDialog pDialog;
    private MaterialDatePicker jobDeadlinePicker;
    private CardView jobImageUpload;
    private Geocoder geocoder;
    private FirestoreManager firestoreManager;
    private ImageUploadManager imageManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_job, container, false);
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
        bottomNav.setVisibility(View.GONE);

        jobName = view.findViewById(R.id.textField_post_job_name);
        jobType = view.findViewById(R.id.autoComplete_job_type);
        jobDescription = view.findViewById(R.id.textField_post_job_description);
        jobDeadline = view.findViewById(R.id.textField_post_job_deadline);
        jobLocation = view.findViewById(R.id.textField_job_location);
        jobImageUpload = view.findViewById(R.id.cardview_post_job_image_upload);
        jobPicture = view.findViewById(R.id.imageview_job_picture);
        Button sendJob = view.findViewById(R.id.button_post_job);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        gpsLocationCoordinates = new ArrayList<>();
        imageManager = new ImageUploadManager();
        firestoreManager = new FirestoreManager();

        getGPSLocationPermission();
        sendJob.setOnClickListener(this);
        jobImageUpload.setOnClickListener(this);
        setCalendar();
        getProfessionList();
        jobLocation.getEditText().addTextChangedListener(textWatcher);
        showDatePicker();
        showSelectedDate();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_post_job:
                if (isInputValidated()) {
                    if (!isLocationTooFar) {
                        pDialog = new ProgressDialog(getActivity());
                        addNewJob();
                    } else {
                        Toast.makeText(getActivity(), "A munkavégzés helye nem lehet távolabb, mint 15 km!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.cardview_post_job_image_upload:
                selectImage(getActivity());
                break;
        }
    }

    private void getGPSLocationPermission() {
        locationManager = (LocationManager) Objects.requireNonNull(getActivity()).getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    private boolean hasGPSPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showSelectedDate() {
        jobDeadlinePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                jobDeadline.getEditText().setText(jobDeadlinePicker.getHeaderText());
            }
        });
    }

    private void showDatePicker() {
        jobDeadline.getEditText().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jobDeadlinePicker.show(getFragmentManager(), "DATE_PICKER");
            }
        });
    }

    public void selectImage(final Context context) {
        final CharSequence[] options = {"Kamera", "Galéria", "Mégsem"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Kép kiválasztása");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Kamera")) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePicture.resolveActivity(context.getPackageManager()) != null) {
                        startActivityForResult(takePicture, 0);
                    }
                } else if (options[item].equals("Galéria")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);
                } else if (options[item].equals("Mégsem")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void addNewJob() {
        name = jobName.getEditText().getText().toString();
        type = String.valueOf(jobType.getText());
        description = jobDescription.getEditText().getText().toString();
        deadline = String.valueOf(jobDeadline.getEditText().getText());
        timestamp = getTimeAsString();

        storeGeoLocationAsHash();

        //if a picture is selected, first upload it to Firebase Storage, then save the job, else just save it with an empty string on the image path
        if (imageAsBitmap != null) {
            imageManager.jobPictureUploader(imageAsBitmap, firestoreManager, new ImageUploadManager.UploadUrlCallback() {
                @Override
                public void onUploadSuccessCallback(String imageUrl) {
                    pathToImage = imageUrl;
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Munka sikeresen hozzáadva!", Snackbar.LENGTH_LONG).show();
                    firestoreManager.saveJob(name, type, description, deadline, timestamp, pathToImage, inputLocationCoordinates, geoHashLocation, reverseGeoCoding(inputLocationCoordinates));
                    firestoreManager.addProfession(type);
                    pDialog.dismiss();
                    getFragmentManager().popBackStack();
                }

                @Override
                public void onUploadFailedCallback() {
                    pDialog.dismiss();
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Hiba a feltöltés során! Próbáld újra!", Snackbar.LENGTH_LONG).show();
                }

                @Override
                public void onUploadProgressListener(double progress) {
                    if (pDialog != null) {
                        pDialog.setTitle("Feltöltés...");
                        pDialog.show();
                        pDialog.setMessage((int) progress + "%");
                    }
                }
            });
        } else {
            firestoreManager.saveJob(name, type, description, deadline, timestamp, pathToImage = "-", inputLocationCoordinates, geoHashLocation, reverseGeoCoding(inputLocationCoordinates));
            getFragmentManager().popBackStack();
        }
    }

    private void storeGeoLocationAsHash() {
        //store lat and long coordinates as a geohash
        double lat = inputLocationCoordinates.get(0);
        double lng = inputLocationCoordinates.get(1);
        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(lat, lng));
        geoHashLocation = new HashMap<>();
        geoHashLocation.put("geohash", hash);
    }

    private void setCalendar() {
        Calendar calendar = Calendar.getInstance();
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        //setting constraints: current system date as the start date, ending date within 1 year
        CalendarConstraints.Builder constBuilder = new CalendarConstraints.Builder();
        CalendarConstraints.DateValidator dateValidator = DateValidatorPointForward.now();
        constBuilder.setValidator(dateValidator);
        calendar.roll(Calendar.YEAR, 1);
        constBuilder.setEnd(calendar.getTimeInMillis());
        builder.setCalendarConstraints(constBuilder.build());
        builder.setTitleText("Határidő kiválasztása");
        jobDeadlinePicker = builder.build();
    }

    private void getProfessionList() {
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

    private void getUserInputLocation() {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        inputLocationCoordinates = new ArrayList<>();

        //get the long and lat coordinates of the address entered by the user
        try {
            List<Address> locationResults = geocoder.getFromLocationName(location, 1);
            while (locationResults.size() == 0) {
                locationResults = geocoder.getFromLocationName(location, 1);
            }
            Address locationAddress = locationResults.get(0);
            inputLocationCoordinates.add(locationAddress.getLatitude());
            inputLocationCoordinates.add(locationAddress.getLongitude());
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }

        //check if the distance (in metres) between the real location and input location is too large
        if (getDistance(inputLocationCoordinates.get(0), inputLocationCoordinates.get(1),
                gpsLocationCoordinates.get(0), gpsLocationCoordinates.get(1)) > MAX_DISTANCE_BETWEEN_LOCATIONS) {
            isLocationTooFar = true;
        } else {
            isLocationTooFar = false;
        }
    }

    public String reverseGeoCoding(ArrayList<Double> arrayList) {
        double lat = arrayList.get(0);
        double lon = arrayList.get(1);
        List<Address> addressList = null;
        StringBuilder result = new StringBuilder();

        try {
            addressList = geocoder.getFromLocation(lat, lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address address = addressList.get(0);
        //display address in a readable way converted from coordinates
        result.append(address.getLocality());
        result.append(", ");
        result.append(address.getThoroughfare());
        result.append(" ");
        result.append(address.getSubThoroughfare());
        result.append(".");

        return result.toString();
    }

    private float getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[2];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }

    private String getTimeAsString() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return simpleDateFormat.format(now);
    }

    //listening user input on job address to get the address
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            location = jobLocation.getEditText().getText().toString().trim();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != getActivity().RESULT_CANCELED) {
            switch (requestCode) {
                //camera
                case 0:
                    if (resultCode == getActivity().RESULT_OK && data != null) {
                        try {
                            imageAsBitmap = (Bitmap) data.getExtras().get("data");
                            jobPicture.setImageBitmap(imageAsBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                //gallery
                case 1:
                    if (resultCode == getActivity().RESULT_OK && data != null) {
                        Uri pictureUrl = data.getData();
                        try {
                            imageAsBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pictureUrl);
                            jobPicture.setImageBitmap(imageAsBitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        isLocationChanged = true;
        gpsLocationCoordinates.add(location.getLatitude());
        gpsLocationCoordinates.add(location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getActivity(), "Kérlek, engedélyezd a GPS hozzáférést!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pDialog != null)
            pDialog.dismiss();
        if (locationManager != null)
            locationManager.removeUpdates(this);
    }

    @Override
    public boolean isInputValidated() {
        String jobNameString = jobName.getEditText().getText().toString();
        String jobTypeString = jobType.getText().toString();
        String jobDeadlineString = jobDeadline.getEditText().getText().toString();
        String jobLocationString = jobLocation.getEditText().getText().toString();

        if (TextUtils.isEmpty(jobNameString)) {
            jobName.setError("Kötelező megadni!");
            return false;
        } else {
            jobName.setError(null);
        }
        if (TextUtils.isEmpty(jobTypeString)) {
            jobType.setError("Kötelező megadni!");
            return false;
        } else {
            jobType.setError(null);
        }
        if (TextUtils.isEmpty(jobDeadlineString)) {
            jobDeadline.setError("Kötelező megadni!");
            return false;
        } else {
            jobDeadline.setError(null);
        }
        if (TextUtils.isEmpty(jobLocationString)) {
            jobLocation.setError("Kötelező megadni!");
            return false;
        } else {
            jobLocation.setError(null);
        }

        if (!TextUtils.isEmpty(jobLocationString) && isLocationChanged) {
            getUserInputLocation();
        }
        return true;
    }

    @Override
    public boolean isValidPassword(String password) {
        return false;
    }

    @Override
    public boolean isValidFullName(String fullName) {
        return false;
    }

    @Override
    public boolean isValidTaxNumber(String taxNumber) {
        return false;
    }
}