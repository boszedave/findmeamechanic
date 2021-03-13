package com.sze.findmeamechanic.models;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class Repairman {
    String repID, repName, repEmail,
            repPhoneNr, repTaxNr, repProfession,
            repCompanyName, repCompanyAddress, pathToImage;

    public Repairman() {
    }

    public Repairman(FirestoreRecyclerOptions<Repairman> options) {
    }

    public Repairman(String repID, String repName, String repEmail, String repPhoneNr, String repTaxNr, String repProfession,
                     String repCompanyName, String repCompanyAddress, String pathToImage) {
        this.repID = repID;
        this.repName = repName;
        this.repEmail = repEmail;
        this.repPhoneNr = repPhoneNr;
        this.repTaxNr = repTaxNr;
        this.repProfession = repProfession;
        this.repCompanyName = repCompanyName;
        this.repCompanyAddress = repCompanyAddress;
        this.pathToImage = pathToImage;
    }

    public String getRepID() {
        return repID;
    }

    public void setRepID(String repID) {
        this.repID = repID;
    }

    public String getRepName() {
        return repName;
    }

    public void setRepName(String repName) {
        this.repName = repName;
    }

    public String getRepEmail() {
        return repEmail;
    }

    public void setRepEmail(String repEmail) {
        this.repEmail = repEmail;
    }

    public String getRepPhoneNr() {
        return repPhoneNr;
    }

    public void setRepPhoneNr(String repPhoneNr) {
        this.repPhoneNr = repPhoneNr;
    }

    public String getRepTaxNr() {
        return repTaxNr;
    }

    public void setRepTaxNr(String repTaxNr) {
        this.repTaxNr = repTaxNr;
    }

    public String getRepProfession() {
        return repProfession;
    }

    public void setRepProfession(String repProfession) {
        this.repProfession = repProfession;
    }

    public String getRepCompanyName() {
        return repCompanyName;
    }

    public void setRepCompanyName(String repCompanyName) {
        this.repCompanyName = repCompanyName;
    }

    public String getRepCompanyAddress() {
        return repCompanyAddress;
    }

    public void setRepCompanyAddress(String repCompanyAddress) {
        this.repCompanyAddress = repCompanyAddress;
    }

    public String getPathToImage() {
        return pathToImage;
    }

    public void setPathToImage(String pathToImage) {
        this.pathToImage = pathToImage;
    }
}
