package com.sze.findmeamechanic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.sze.findmeamechanic.R;
import com.sze.findmeamechanic.managers.FirestoreManager;
import com.sze.findmeamechanic.managers.ValidationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterRepmanActivity extends AppCompatActivity implements ValidationManager, View.OnClickListener {
    private TextInputLayout companyName, companyAddress, taxNumber;
    private AutoCompleteTextView profession;
    private Button registerButton;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_repman);
        firestoreManager = new FirestoreManager();

        profession = findViewById(R.id.autoComplete_job_type);
        registerButton = findViewById(R.id.button_register);
        companyName = findViewById(R.id.textField_repman_company_name);
        companyAddress = findViewById(R.id.textField_repman_company_address);
        taxNumber = findViewById(R.id.textField_repman_tax_number);

        getProfessionList();
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String professionInput = profession.getText().toString();
        String companyNameInput = companyName.getEditText().getText().toString();
        String companyAddressInput = companyAddress.getEditText().getText().toString();
        String taxNumberInput = taxNumber.getEditText().getText().toString();
        String newProfession = profession.getText().toString();

        if (isInputValidated()) {
            firestoreManager.updateRepairmanData(professionInput, companyNameInput, companyAddressInput, taxNumberInput);
            firestoreManager.addProfession(newProfession);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    private void getProfessionList() {
        final List<String> professionList = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, professionList);

        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        profession.setAdapter(adapter);

        firestoreManager.getProfessionList(new FirestoreManager.GetFieldCallback() {
            @Override
            public void onTaskResultCallback(String str) {
                professionList.add(str);
            }

            @Override
            public void onSuccessfulQueryCallback() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean isInputValidated() {
        String professionInput = profession.getText().toString();
        String companyNameInput = companyName.getEditText().getText().toString();
        String companyAddressInput = companyAddress.getEditText().getText().toString();
        String taxNumberInput = taxNumber.getEditText().getText().toString();

        if (TextUtils.isEmpty(professionInput)) {
            profession.setError("Kötelező megadni!");
            return false;
        } else {
            profession.setError(null);
        }
        if (!TextUtils.isEmpty(companyNameInput) && TextUtils.isEmpty(companyAddressInput)) {
            companyAddress.setError("Kötelező megadni!");
            return false;
        } else {
            companyAddress.setError(null);
        }
        if (!isValidTaxNumber(taxNumberInput)) {
            taxNumber.setError("Kötőjelek nélkül írja be a 11 számjegyű adószámot!");
            return false;
        } else {
            taxNumber.setError(null);
        }
        return true;
    }

    @Override
    public boolean isValidTaxNumber(String taxNumber) {
        Pattern pattern;
        Matcher matcher;
        final String TAX_NUMBER_PATTERN = "^(\\d{11})$";
        pattern = Pattern.compile(TAX_NUMBER_PATTERN);
        matcher = pattern.matcher(taxNumber);

        return matcher.matches();
    }

    @Override
    public boolean isValidPassword(String password) {
        return false;
    }

    @Override
    public boolean isValidFullName(String fullName) {
        return false;
    }
}