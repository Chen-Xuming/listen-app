package com.example.xiong.myapplication.record;

import android.media.MediaPlayer;

import java.io.IOException;

public class Record {

    private int id;

    private String username;
    private String title;
    private String description;
    private String create_time;

    private int duration;
    private String headpic_url;
    private String sound_url;

    private int is_like;        // 是否已经点赞
    private int is_collect;     // 是否已经收藏
    private int is_download;    // 是否已下载


    /*
            播放器采用懒加载的方式：当用户点击播放按钮时，才进行资源装载
     */
    private MediaPlayer mediaPlayer;
    private int current_position = 0;       // 音乐播放进度

    private int player_status;      // 0:未缓冲 1：正在缓冲  2：已经缓冲完毕

    private int play_button;        // 0: 显示的是pause样式， 1：显示的是play样式

    public Record(int id, String username, String title, String description, String create_time,
                  int duration, String headpic_url, String sound_url,
                  int is_like, int is_collect){
        this.username = username;
        this.title = title;
        this.description = description;
        this.create_time = create_time;
        this.duration = duration;
        this.headpic_url = headpic_url;
        this.sound_url = sound_url;

        this.is_like = is_like;
        this.is_collect = is_collect;
        is_download = 0;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true);

        player_status = 0;
        play_button = 1;

        this.id = id;
    }

    /*
            getter/setter
     */
    public String getUsername() {
        return username;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCreate_time() {
        return create_time;
    }

    public int getDuration() {
        return duration;
    }

    public String getHeadpic_url() {
        return headpic_url;
    }

    public String getSound_url() {
        return sound_url;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getPlayer_status() {
        return player_status;
    }

    public void setPlay_button(int style){
        play_button = style;
    }

    public int getPlay_button(){return play_button;}

    public void setPlayer_status(int player_status) {
        this.player_status = player_status;
    }

    public int getCurrent_position() {
        return player_status == 0 ? 0 : mediaPlayer.getCurrentPosition();
    }

    public void setCurrent_position(int current_position) {
        this.current_position = current_position;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int isLike() {
        return is_like;
    }

    public void setIs_like(int is_like) {
        this.is_like = is_like;
    }

    public int isCollect() {
        return is_collect;
    }

    public void setIs_collect(int is_collect) {
        this.is_collect = is_collect;
    }

    public int isDownload() {
        return is_download;
    }

    public void setIs_download(int is_download) {
        this.is_download = is_download;
    }

    public int getId(){
        return id;
    }
}
