package com.example.firebasesocialmediaapp;

public class Post {
    private String pDesc;
    private String pImage;

    public Post(String pDesc, String pImage) {
        this.pDesc = pDesc;
        this.pImage = pImage;
    }

    public String getpDesc() {
        return pDesc;
    }

    public String getpImage() {
        return pImage;
    }
}
