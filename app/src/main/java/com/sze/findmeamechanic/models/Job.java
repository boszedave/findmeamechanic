package com.sze.findmeamechanic.models;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;

public class Job {
    String jobSenderID, jobID, jobName, jobType, jobDescription, jobDeadline, jobDate, jobPictureUrl = "-", jobLocation;
    ArrayList<Double> jobLocationCoordinates;


    public Job() {
    }

    public Job(FirestoreRecyclerOptions<Job> options) {
    }

    public Job(String jobSenderID, String jobID, String jobName, String jobType, String jobDescription, String jobDeadline,
               String jobDate, String jobPictureUrl, ArrayList<Double> jobLocationCoordinates, String jobLocation) {
        this.jobSenderID = jobSenderID;
        this.jobID = jobID;
        this.jobName = jobName;
        this.jobType = jobType;
        this.jobDescription = jobDescription;
        this.jobDeadline = jobDeadline;
        this.jobDate = jobDate;
        this.jobPictureUrl = jobPictureUrl;
        this.jobLocationCoordinates = jobLocationCoordinates;
        this.jobLocation = jobLocation;
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

  /*  public ArrayList<Double> getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(ArrayList<Double> jobLocation) {
        this.jobLocation = jobLocation;
    } */

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

    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public ArrayList<Double> getJobLocationCoordinates() {
        return jobLocationCoordinates;
    }

    public void setJobLocationCoordinates(ArrayList<Double> jobLocationCoordinates) {
        this.jobLocationCoordinates = jobLocationCoordinates;
    }
}