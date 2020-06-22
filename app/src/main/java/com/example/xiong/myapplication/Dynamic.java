package com.example.xiong.myapplication;
import com.example.xiong.myapplication.MainActivity;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class Dynamic extends Fragment {
    public ArrayList<Moments> mommentsArray1;
    private Context context;
    public MediaPlayer mediaPlayer1;
    private MyListView myListView;

    public void setContext(Context context) {
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState)
    {
        View view =inflater.inflate(R.layout.listviewdynamic,null);
        myListView=view.findViewById(R.id.MomentListDynamic);

        mommentsArray1=new ArrayList<>();
        mediaPlayer1=MediaPlayer.create(context,R.raw.joy_of_love);
        //mediaPlayer2.setLooping(true);
        Moments a=new Moments(ContextCompat.getDrawable(context,R.drawable.pic),"红豆",
                "清澈的歌声","简介","12分钟前",mediaPlayer1);
        mommentsArray1.add(a);
        myListView.setAdapter(new MyAdapter(context,mommentsArray1));
        return view;
    }

    public void stopPlay()
    {
        if(mediaPlayer1.isPlaying())
            mediaPlayer1.stop();
        //mommentsArray1.get(0).playOrPause.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.play));
    }


}
