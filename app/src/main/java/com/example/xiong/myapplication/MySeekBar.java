package com.example.xiong.myapplication;

import android.media.MediaPlayer;
import android.widget.SeekBar;

/*进度条处理*/
public class MySeekBar implements SeekBar.OnSeekBarChangeListener {

    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private MediaPlayer mediaPlayer;
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
    }
    public MySeekBar(MediaPlayer mediaPlayer)
    {
        this.mediaPlayer=mediaPlayer;
    }

    /*滚动时,应当暂停后台定时器*/
    public void onStartTrackingTouch(SeekBar seekBar) {
        isSeekBarChanging = true;
    }

    /*滑动结束后，重新设置值*/
    public void onStopTrackingTouch(SeekBar seekBar) {
        isSeekBarChanging = false;
        mediaPlayer.seekTo(seekBar.getProgress());
    }
}