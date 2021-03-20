package com.sze.findmeamechanic.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Message {
    //private String messageUserName;
    private String messageText;
    private String messageUserId;
    private String messageTime;

    public Message() {
    }

    public Message(String messageText, String messageUserId) {
        Date dateTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yy/MM/dd HH:mm", Locale.getDefault());
        //this.messageUserName = messageUserName;
        this.messageText = messageText;
        messageTime = dateFormat.format(dateTime);

        this.messageUserId = messageUserId;
    }

   /* public String getMessageUserName() {
        return messageUserName;
    }

    public void setMessageUserName(String messageUserName) {
        this.messageUserName = messageUserName;
    }*/

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageUserId() {
        return messageUserId;
    }

    public void setMessageUserId(String messageUserId) {
        this.messageUserId = messageUserId;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }
}