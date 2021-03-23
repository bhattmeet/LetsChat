package com.example.letschat.model;

public class Messages {

    private String from,message,type,time,to,messageId,name;

    public Messages() {

    }

    public Messages(String from, String message, String type, String time, String to, String messageId, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.time = time;
        this.to = to;
        this.messageId = messageId;
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
