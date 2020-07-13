package com.example.xiong.myapplication;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class MusiceService extends Service {
    private static final String TAG="MusicService";
    MediaPlayer mediaPlayer;
    static boolean ispaly;

    @Override
    public void onCreate()
    {
        //mediaPlayer=MediaPlayer.create(this,R.raw.joy_of_love);
        System.out.println("onCreate()");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        if(!mediaPlayer.isPlaying())
        {
            mediaPlayer.start();
            ispaly=mediaPlayer.isPlaying();
        }
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy()
    {
        mediaPlayer.stop();
        ispaly=mediaPlayer.isPlaying();
        mediaPlayer.release();//释放资源
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
