package com.example.letschat.model;

public class Contacts {

    public String image,status,username;

    public Contacts(){

    }

    public Contacts(String image, String status, String username) {
        this.image = image;
        this.status = status;
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
