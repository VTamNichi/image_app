package com.hieplh.imageapp.model;

import android.graphics.Bitmap;

public class ImageModel {
    private int id;
    private Bitmap image;
    private String location;

    public ImageModel() {
        this.location = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
