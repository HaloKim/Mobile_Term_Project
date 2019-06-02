package com.example.term_project;

import android.graphics.Bitmap;

import java.io.Serializable;

public class item implements Serializable {
    Bitmap photo;

    public item(Bitmap photo) {
        this.photo = photo;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }
}