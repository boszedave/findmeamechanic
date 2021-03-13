package com.sze.findmeamechanic.models;

public class Client {
    String clientID, clientName, clientEmail, clientPhoneNr, pathToImage;

    public Client() {
    }

    public Client(String clientID, String clientName, String clientEmail, String clientPhoneNr, String pathToImage) {
        this.clientID = clientID;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.clientPhoneNr = clientPhoneNr;
        this.pathToImage = pathToImage;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getPathToImage() {
        return pathToImage;
    }

    public void setPathToImage(String pathToImage) {
        this.pathToImage = pathToImage;
    }

    public String getClientPhoneNr() {
        return clientPhoneNr;
    }

    public void setClientPhoneNr(String clientPhoneNr) {
        this.clientPhoneNr = clientPhoneNr;
    }
}
