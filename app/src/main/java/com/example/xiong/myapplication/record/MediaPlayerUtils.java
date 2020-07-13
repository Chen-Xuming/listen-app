package com.example.xiong.myapplication.record;

/*
        操控播放器和进度条
 */

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

public class MediaPlayerUtils {

    public MediaPlayer mediaPlayer;
    public MediaPlayerUtils.Listener listener;
    public Handler mHandler;
    public int old_position;        // 最后点击的 item 位置

    public MediaPlayerUtils(){
        mediaPlayer = null;
        listener = null;
        mHandler = new Handler();
        old_position = -1;
    }

    public void pauseMediaPlayer() {

        if (mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

    public void playMediaPlayer() {
        mediaPlayer.start();
        mHandler.postDelayed(mRunnable, 1000);
    }

    public void applySeekBarValue(int selectedValue) {
        mediaPlayer.seekTo(selectedValue);
        mHandler.postDelayed(mRunnable, 1000);
    }


    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (isPlaying()) {
                    mHandler.postDelayed(mRunnable, 1000);

                    listener.onAudioUpdate(old_position);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void reset(){
        mediaPlayer = null;
        mHandler = new Handler();
        old_position = -1;
    }

    interface Listener {
        //void onAudioComplete();
        void onAudioUpdate(int item_position);     // 进度；item号
    }
}
