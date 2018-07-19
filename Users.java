package com.example.user.nasrinchatapp;

/**
 * Created by user on 6/19/2018.
 */

public class Users {

    String name;
    String Status;
    public String image;

    public Users(){

    }
    public Users(String name, String status, String image
    ) {
        this.name = name;
        this.Status = status;
        this.image=image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }
}
