package com.example.xiong.myapplication;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.widget.ImageView;

public class Moments {
    private Drawable Profile_Photo;
    private String user_name, composition_name,Composition_introduction,relerase_time;
    public MediaPlayer mediaPlayer;
    public ImageView playOrPause;
    public Moments(Drawable Profile_Photo, String user_name, String composition_name,
                   String Composition_introduction,String relerase_time,MediaPlayer mediaPlayer)
    {
        System.out.println("Moments");
        this.Profile_Photo=Profile_Photo;
        this.user_name=user_name;
        this.composition_name=composition_name;
        this.Composition_introduction=Composition_introduction;
        this.relerase_time=relerase_time;
        this.mediaPlayer=mediaPlayer;
        this.playOrPause=null;
    }

    public Drawable getProfile_Photo()
    {
        return this.Profile_Photo;
    }
    public String getuser_name()
    {
        return this.user_name;
    }
    public String getcomposition_name()
    {
        return this.composition_name;
    }
    public String getcomposition_introduction()
    {
        return this.Composition_introduction;
    }
    public String getrelerase_time()
    {
        return this.relerase_time;
    }
    public MediaPlayer getmediaPlayer()
    {
        return this.mediaPlayer;
    }
//        public void changePicToPlayOrPause(Drawable d){this.playOrPause.setImageDrawable(d);}
//        public void setPlayOrPause(ImageView iv){this.playOrPause=iv;}

}
