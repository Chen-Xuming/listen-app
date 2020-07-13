package com.example.xiong.myapplication.account;

public class Account {

    private String name;
    private String headpic_url;
    public boolean is_followed = true;
    public Account(String username, String headpic_url, boolean is_followed){
        this.name = username;
        this.headpic_url = headpic_url;
        this.is_followed = is_followed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadpic_url() {
        return headpic_url;
    }

    public void setHeadpic_url(String headpic_url) {
        this.headpic_url = headpic_url;
    }

    public boolean isIs_followed() {
        return is_followed;
    }

    public void setIs_followed(boolean is_followed) {
        this.is_followed = is_followed;
    }


}
