package com.sze.findmeamechanic.fragments.repairman;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.managers.ValidationManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class JobSheetFragment extends Fragment implements ValidationManager, View.OnClickListener {
    private final static String JOB_ID = "jobId";
    private TextInputLayout itemName, itemQuantity, itemPrice, repairDescription;
    private TextView sheetNumber, sheetDate, repmanName, companyName, companyAddress, repmanPhoneNr, clientName, clientPhoneNr, totalPrice, itemInformationError;
    private LinearLayout itemLayout;
    private Button closeJob, addNewItem, itemFinalize;
    private String docID, clientID, repmanNameText, companyNameText, companyAddressText, repmanPhoneNrText, clientNameText, clientPhoneNrText, sheetID;
    private ImageView deleteItem;
    private int totalPriceData;
    private List<TextInputLayout> inputReferencesName, inputReferencesQuantity, inputReferencesPrice;
    private List<ImageView> itemDeleteReferences;
    private List<Integer> randomNumbers;
    private FirestoreManager firestoreManager;
    private View view;
    private ProgressBar pBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_job_sheet, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        docID = bundle.getString(JOB_ID);
        inputReferencesName = new ArrayList<>();
        inputReferencesQuantity = new ArrayList<>();
        inputReferencesPrice = new ArrayList<>();
        itemDeleteReferences = new ArrayList<>();
        randomNumbers = new ArrayList<>();
        sheetNumber = view.findViewById(R.id.textView_sheet_number_data);
        sheetDate = view.findViewById(R.id.textView_sheet_number_date_data);
        repmanName = view.findViewById(R.id.textView_sheet_repman_name_data);
        companyName = view.findViewById(R.id.textView_sheet_repman_company_data);
        companyAddress = view.findViewById(R.id.textView_sheet_repman_company_location_data);
        repmanPhoneNr = view.findViewById(R.id.textView_sheet_repman_phone_data);
        clientName = view.findViewById(R.id.textView_sheet_client_name_data);
        clientPhoneNr = view.findViewById(R.id.textView_sheet_client_phone_data);
        repairDescription = view.findViewById(R.id.textField_sheet_job_description);
        totalPrice = view.findViewById(R.id.textView_sheet_job_total_price_data);
        itemInformationError = view.findViewById(R.id.textView_item_information_error);
        closeJob = view.findViewById(R.id.button_close_job);
        addNewItem = view.findViewById(R.id.button_add_item);
        itemFinalize = view.findViewById(R.id.button_item_finalize);
        itemFinalize.setVisibility(View.INVISIBLE);
        itemLayout = view.findViewById(R.id.linearLayout_item_list);
        firestoreManager = new FirestoreManager();
        pBar = view.findViewById(R.id.pbar_upload);

        initData();
        addNewItem.setOnClickListener(this);
        closeJob.setOnClickListener(this);
        closeJob.setEnabled(false);
        itemFinalize.setOnClickListener(this);

    }

    private void initData() {
        totalPriceData = 0;
        firestoreManager.getRepairmanDetails(firestoreManager.getUserID(), new FirestoreManager.GetSnapshotCallback() {
            @Override
            public void onGetFieldCallback(final DocumentSnapshot documentSnapshotRepman) {
                firestoreManager.getJobSenderDetails(docID, new FirestoreManager.GetSnapshotCallback() {
                    @Override
                    public void onGetFieldCallback(DocumentSnapshot documentSnapshotClient) {
                        //get repairman data
                        repmanNameText = documentSnapshotRepman.getString("repName");
                        companyNameText = documentSnapshotRepman.getString("companyName");
                        companyAddressText = documentSnapshotRepman.getString("companyAddress");
                        repmanPhoneNrText = documentSnapshotRepman.getString("repPhoneNr");
                        if (companyNameText.isEmpty() && companyAddressText.isEmpty()) {
                            companyAddressText = "-";
                            companyNameText = "-";
                        }

                        //get client data
                        clientID = documentSnapshotClient.getString("clientID");
                        clientNameText = documentSnapshotClient.getString("clientName");
                        clientPhoneNrText = documentSnapshotClient.getString("clientPhoneNr");

                        //set layout
                        //sheet data
                        sheetID = docID.substring(0, 5) + "-" + getTimeAsString().substring(0, 4);
                        sheetNumber.setText(sheetID);
                        sheetDate.setText(getTimeAsString());
                        //repairman data
                        repmanName.setText(repmanNameText);
                        companyName.setText(companyNameText);
                        companyAddress.setText(companyAddressText);
                        repmanPhoneNr.setText(repmanPhoneNrText);
                        //client data
                        clientName.setText(clientNameText);
                        clientPhoneNr.setText(clientPhoneNrText);
                    }
                });
            }
        });
    }

    private void createPdf() {
        //default data
        final int PAGE_WIDTH = 595;
        final int PAGE_HEIGHT = 842;
        String repairDescriptionText = repairDescription.getEditText().getText().toString();
        //logo
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.icon_logo);
        Bitmap scaledLogo = Bitmap.createScaledBitmap(logo, 50, 50, false);

        //create pdf & build up the page design
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.PageInfo pageInfo2 = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        //set font types
        Paint paragraphPaint = new Paint();
        Paint titlePaint = new Paint();
        Paint headingPaint = new Paint();
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(25);
        headingPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        headingPaint.setTextSize(12);

        //repairman details
        canvas.drawText(repmanNameText, 50, 100, paragraphPaint);
        canvas.drawText(companyNameText, 50, 120, paragraphPaint);
        canvas.drawText(companyAddressText, 50, 140, paragraphPaint);
        canvas.drawText(repmanPhoneNrText, 50, 160, paragraphPaint);

        //default details
        //first section
        canvas.drawText(getString(R.string.sheet_title), 50, 50, titlePaint);
        canvas.drawText(getString(R.string.sheet_number), 450, 100, paragraphPaint);
        canvas.drawText(getString(R.string.sheet_date), 450, 140, paragraphPaint);
        canvas.drawText(sheetID, 450, 120, paragraphPaint);
        canvas.drawText(getTimeAsString(), 450, 160, paragraphPaint);
        canvas.drawBitmap(scaledLogo, 480, 20, paragraphPaint);
        canvas.drawLine(50, 200, 545, 200, paragraphPaint);
        //second section
        canvas.drawText(getString(R.string.sheet_client_data), 50, 240, headingPaint);
        canvas.drawText(getString(R.string.sheet_job_number), 400, 280, paragraphPaint);
        canvas.drawText(docID, 400, 300, paragraphPaint);
        canvas.drawLine(50, 340, 545, 340, paragraphPaint);

        //client details
        canvas.drawText(clientNameText, 50, 280, paragraphPaint);
        canvas.drawText(clientPhoneNrText, 50, 300, paragraphPaint);

        //job repair details
        //staticlayout is used for multi-line displaying
        TextPaint textPaint = new TextPaint();
        StaticLayout staticLayout = new StaticLayout(repairDescriptionText, textPaint, canvas.getWidth() - 50, Layout.Alignment.ALIGN_NORMAL,
                1.0f, 0.0f, false);
        canvas.save();
        canvas.translate(50f, 420f);
        staticLayout.draw(canvas);
        canvas.restore();

        //third section
        canvas.drawText(getString(R.string.sheet_job_description_title), 50, 380, headingPaint);
        //draw headers after the position of staticlayout
        int dynamicYCoordinate = (int) (420f + staticLayout.getHeight());
        canvas.drawText(getString(R.string.sheet_job_items), 50, dynamicYCoordinate + 40, paragraphPaint);
        canvas.drawText(getString(R.string.sheet_item_name), 50, dynamicYCoordinate + 80, headingPaint);
        canvas.drawText(getString(R.string.sheet_item_quantity), 200, dynamicYCoordinate + 80, headingPaint);
        canvas.drawText(getString(R.string.sheet_item_price), 350, dynamicYCoordinate + 80, headingPaint);
        PdfDocument.Page page2 = null;

        if (inputReferencesName.size() >= 1) {
            for (int i = 0; i < inputReferencesName.size(); i++) {
                canvas.drawText(inputReferencesName.get(i).getEditText().getText().toString(), 50, dynamicYCoordinate + 100, paragraphPaint);
                canvas.drawText(inputReferencesQuantity.get(i).getEditText().getText().toString(), 200, dynamicYCoordinate + 100, paragraphPaint);
                canvas.drawText(inputReferencesPrice.get(i).getEditText().getText().toString(), 350, dynamicYCoordinate + 100, paragraphPaint);
                dynamicYCoordinate += 20;

                //create second page if there are too many items on page 1
                if (dynamicYCoordinate > PAGE_HEIGHT - 50 /*400*/) {
                    document.finishPage(page);
                    page2 = document.startPage(pageInfo2);
                    canvas = page2.getCanvas();
                    //set coordinate so on the new page it start at Y=50
                    dynamicYCoordinate = -50;
                }
            }

            canvas.drawText(getString(R.string.sheet_total_price) + totalPriceData, 50, dynamicYCoordinate + 100, headingPaint);
            //if page 2 is created then page 1 is already finished and must finish page 2
            // if page 2 not created, finish page 1
            if (page2 != null) {
                document.finishPage(page2);
            } else {
                document.finishPage(page);
            }
        }

        //create and save file to internal storage
        savePDFToInternalStorage(document);
        document.close();
    }

    private void uploadJobSheet() {
        pBar.setVisibility(View.VISIBLE);
        firestoreManager.uploadJobSheet(getActivity(), sheetID + ".pdf", new FirestoreManager.UploadUrlCallback() {
            @Override
            public void onUploadSuccessCallback(final String imageUrl) {
                pBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Sikeres feltöltés!", Toast.LENGTH_SHORT).show();
                closeJob.setEnabled(false);

                //save job to FinishedJobs collection
                firestoreManager.getActiveJobDetails(docID, new FirestoreManager.GetSnapshotCallback() {
                    @Override
                    public void onGetFieldCallback(DocumentSnapshot documentSnapshot) {
                        firestoreManager.finishJob(clientID,
                                docID,
                                documentSnapshot.getString("jobName"),
                                documentSnapshot.getString("jobType"),
                                documentSnapshot.getString("jobDescription"),
                                documentSnapshot.getString("jobDeadline"),
                                documentSnapshot.getString("jobDate"),
                                documentSnapshot.getString("jobPictureUrl"),
                                documentSnapshot.getString("jobLocation"),
                                firestoreManager.getUserID(),
                                sheetDate.getText().toString(),
                                imageUrl);

                        //delete file from internal storage after upload
                        File file = new File(getActivity().getFilesDir(), sheetID + ".pdf");
                        boolean deletedFile = file.delete();
                    }
                });
            }


            @Override
            public void onUploadFailedCallback() {
                pBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Sikertelen feltöltés, próbáld újra!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePDFToInternalStorage(PdfDocument document) {
        File file = new File(getActivity().getFilesDir(), sheetID + ".pdf");
        try {
            document.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            Log.d("picupload", "savePDFToInternalStorage: " + e);
            e.printStackTrace();
        }
    }

    private String getTimeAsString() {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd.", Locale.getDefault());
        return simpleDateFormat.format(now);
    }

    private void addView() {
        itemFinalize.setVisibility(View.VISIBLE);
        final View itemView = getLayoutInflater().inflate(R.layout.job_sheet_item, null, false);
        itemName = itemView.findViewById(R.id.textField_sheet_item_name);
        itemQuantity = itemView.findViewById(R.id.textField_sheet_item_quantity);
        itemPrice = itemView.findViewById(R.id.textField_sheet_item_price);
        deleteItem = itemView.findViewById(R.id.imageView_item_delete);

        //generate random numbers for the views so they can be compared later on
        int id = new Random().nextInt(1000);
        do {
            randomNumbers.add(id);
            id = new Random().nextInt(1000);

        } while (randomNumbers.contains(id));

        deleteItem.setId(id);
        itemView.setId(id);

        //store the references of the newly added layout items
        inputReferencesName.add(itemName);
        inputReferencesQuantity.add(itemQuantity);
        inputReferencesPrice.add(itemPrice);
        itemDeleteReferences.add(deleteItem);

        itemLayout.addView(itemView);
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeView(itemView);
            }
        });
    }

    private void removeView(View v) {
        itemLayout.removeView(v);

        //get the id of the specific field and delete its references from the array lists
        for (int i = 0; i < itemDeleteReferences.size(); i++) {
            if (itemDeleteReferences.get(i).getId() == v.getId()) {
                itemDeleteReferences.remove(i);
                inputReferencesName.remove(i);
                inputReferencesPrice.remove(i);
                inputReferencesQuantity.remove(i);
            }
        }
    }

    private void totalPriceCount() {
        for (int i = 0; i < inputReferencesQuantity.size(); i++) {
            if (!inputReferencesQuantity.get(i).getEditText().getText().toString().isEmpty() && !inputReferencesPrice.get(i).getEditText().getText().toString().isEmpty())
                totalPriceData += Integer.parseInt(inputReferencesQuantity.get(i).getEditText().getText().toString()) * Integer.parseInt(inputReferencesPrice.get(i).getEditText().getText().toString());
        }
        totalPrice.setText(Integer.toString(totalPriceData));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_add_item:
                addView();
                break;
            case R.id.button_close_job:
                createPdf();
                uploadJobSheet();
                break;
            case R.id.button_item_finalize:
                if (isInputValidated()) {
                    totalPriceCount();
                    setFieldsAfterFinalizing();
                }
                break;
        }
    }

    private void setFieldsAfterFinalizing() {
        //disable item inputs
        for (int i = 0; i < inputReferencesName.size(); i++) {
            inputReferencesName.get(i).setEnabled(false);
            inputReferencesQuantity.get(i).setEnabled(false);
            inputReferencesPrice.get(i).setEnabled(false);
            itemDeleteReferences.get(i).setVisibility(View.GONE);
        }

        //disable buttons and text
        closeJob.setEnabled(true);
        itemInformationError.setVisibility(View.GONE);
        itemFinalize.setEnabled(false);
        addNewItem.setEnabled(false);
    }

    @Override
    public boolean isInputValidated() {
        String description = repairDescription.getEditText().getText().toString();

        if (TextUtils.isEmpty(description)) {
            repairDescription.setError("Kötelező adat!");
            return false;
        } else {
            repairDescription.setError(null);
        }

        for (int i = 0; i < inputReferencesName.size(); i++) {
            String itemNameValidation = inputReferencesName.get(i).getEditText().getText().toString();
            String itemQuantityValidation = inputReferencesQuantity.get(i).getEditText().getText().toString();
            String itemPriceValidation = inputReferencesPrice.get(i).getEditText().getText().toString();

            if (TextUtils.isEmpty(itemNameValidation)) {
                inputReferencesName.get(i).setError("Kötelező adat!");
                return false;
            } else {
                inputReferencesName.get(i).setError(null);
            }
            if (TextUtils.isEmpty(itemQuantityValidation)) {
                inputReferencesQuantity.get(i).setError("Kötelező adat");
                return false;
            } else {
                inputReferencesQuantity.get(i).setError(null);
            }
            if (TextUtils.isEmpty(itemPriceValidation)) {
                inputReferencesPrice.get(i).setError("Kötelező adat!");
                return false;
            } else {
                inputReferencesPrice.get(i).setError(null);
            }
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