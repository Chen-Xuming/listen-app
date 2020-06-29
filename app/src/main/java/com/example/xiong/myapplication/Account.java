package com.example.xiong.myapplication;

public class Account {
    private String name;
    private int image;
    public boolean is_followed = true;
    public Account(String n,int i,boolean f){
        this.name=n;
        this.image =i;
        this.is_followed = f;
    }

    public String getName() {
        return name;
    }

    public boolean isIs_followed() {
        return is_followed;
    }

    public void setIs_followed(boolean is_followed) {
        this.is_followed = is_followed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }
}
