package com.example.xiong.myapplication;


import android.support.v4.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class Hot  extends Fragment {
    public ArrayList<Moments> mommentsArray2;
    private Context context;
    public MediaPlayer mediaPlayer2;
    private MyListView myListView;


    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState)
    {
        View view =inflater.inflate(R.layout.listviewhot,null);
        myListView=view.findViewById(R.id.MomentListHot);

        mommentsArray2=new ArrayList<>();
        //mediaPlayer2=MediaPlayer.create(context,R.raw.liebesleid);
        //mediaPlayer2.setLooping(true);
        Moments a=new Moments(ContextCompat.getDrawable(context,R.drawable.pic),"绿豆",
                "清澈的歌声","这是我自己唱的一首歌","33分钟前",mediaPlayer2);
        mommentsArray2.add(a);
        myListView.setAdapter(new MyAdapter(context,mommentsArray2));
        return view;
    }

    public void stopPlay()
    {
        if(mediaPlayer2.isPlaying())
            mediaPlayer2.stop();
        //mommentsArray2.get(0).playOrPause.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.play));
    }
}