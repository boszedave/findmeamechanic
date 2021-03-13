package com.sze.findmeamechanic.models;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FinishedJob {
    String jobSenderID, jobID, jobName, jobType, jobDescription, jobDeadline, jobDate, jobPictureUrl = "-",
            jobApplicantID, finishDate, locationText, jobSheetPdfUrl;

    public FinishedJob() {
    }

    public FinishedJob(FirestoreRecyclerOptions<FinishedJob> options) {
    }

    public FinishedJob(String jobSenderID, String jobID, String jobName, String jobType, String jobDescription, String jobDeadline,
                       String jobDate, String jobPictureUrl, String jobApplicantID, String finishDate, String locationText, String jobSheetPdfUrl) {
        this.jobSenderID = jobSenderID;
        this.jobID = jobID;
        this.jobName = jobName;
        this.jobType = jobType;
        this.jobDescription = jobDescription;
        this.jobDeadline = jobDeadline;
        this.jobDate = jobDate;
        this.jobPictureUrl = jobPictureUrl;
        this.jobApplicantID = jobApplicantID;
        this.finishDate = finishDate;
        this.locationText = locationText;
        this.jobSheetPdfUrl = jobSheetPdfUrl;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public String getJobSheetPdfUrl() {
        return jobSheetPdfUrl;
    }

    public void setJobSheetPdfUrl(String jobSheetPdfUrl) {
        this.jobSheetPdfUrl = jobSheetPdfUrl;
    }

    public String getJobSenderID() {
        return jobSenderID;
    }

    public void setJobSenderID(String jobSenderID) {
        this.jobSenderID = jobSenderID;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobDate() {
        return jobDate;
    }

    public void setJobDate(String jobDate) {
        this.jobDate = jobDate;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getJobDeadline() {
        return jobDeadline;
    }

    public void setJobDeadline(String jobDeadline) {
        this.jobDeadline = jobDeadline;
    }

    public String getJobPictureUrl() {
        return jobPictureUrl;
    }

    public void setJobPictureUrl(String jobPictureUrl) {
        this.jobPictureUrl = jobPictureUrl;
    }

    public String getJobApplicantID() {
        return jobApplicantID;
    }

    public void setJobApplicantID(String jobApplicantID) {
        this.jobApplicantID = jobApplicantID;
    }

    public String getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(String finishDate) {
        this.finishDate = finishDate;
    }
}