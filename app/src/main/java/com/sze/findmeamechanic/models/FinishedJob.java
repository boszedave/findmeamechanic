package com.sze.findmeamechanic.models;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class FinishedJob {
    String jobSenderID, jobID, jobName, jobType, jobDescription, jobDeadline, jobDate, jobPictureUrl = "-",
            jobApplicantID, jobFinishDate, jobLocation, jobSheetPdfUrl;

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    int rating;

    public FinishedJob() {
    }

    public FinishedJob(FirestoreRecyclerOptions<FinishedJob> options) {
    }

    public FinishedJob(String jobSenderID, String jobID, String jobName, String jobType, String jobDescription, String jobDeadline,
                       String jobDate, String jobPictureUrl, String jobApplicantID, String jobFinishDate, String jobLocation, String jobSheetPdfUrl, int rating) {
        this.jobSenderID = jobSenderID;
        this.jobID = jobID;
        this.jobName = jobName;
        this.jobType = jobType;
        this.jobDescription = jobDescription;
        this.jobDeadline = jobDeadline;
        this.jobDate = jobDate;
        this.jobPictureUrl = jobPictureUrl;
        this.jobApplicantID = jobApplicantID;
        this.jobFinishDate = jobFinishDate;
        this.jobLocation = jobLocation;
        this.jobSheetPdfUrl = jobSheetPdfUrl;
        this.rating = rating;
    }

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
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

    public String getJobFinishDate() {
        return jobFinishDate;
    }

    public void setJobFinishDate(String jobFinishDate) {
        this.jobFinishDate = jobFinishDate;
    }
}