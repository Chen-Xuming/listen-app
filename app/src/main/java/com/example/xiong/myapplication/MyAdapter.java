package com.example.xiong.myapplication;
import com.example.xiong.myapplication.Moments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MyAdapter extends BaseAdapter {
    public int thisPosition = -1;
    public MediaPlayer mMediaPlayer = new MediaPlayer();
    public boolean isPlay = true;
    public boolean isFiring = false;
    public int playintNo;
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Moments> Mdata ;
    private SimpleDateFormat format;
    //添加数据
    public MyAdapter(Context context, ArrayList<Moments> mdata){
        super();
        this.context=context;
        Mdata=mdata;
        inflater=LayoutInflater.from(context);
        playintNo=-1;
        format = new SimpleDateFormat("mm:ss");
    }

    @Override
    public int getCount() { return Mdata==null?0: Mdata.size(); }

    @Override
    public Object getItem(int position) {return Mdata.get(position);}

    @Override
    public long getItemId(int position) { return position;}

    private Timer timer;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final RecordViewHold holder;
        final int sign = position;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cardview, null);
            holder = new RecordViewHold();
            holder.Profile_Photo = convertView.findViewById(R.id.ProfilePhoto);
            holder.Composition_introduction = convertView.findViewById(R.id.introduction);
            holder.composition_name = convertView.findViewById(R.id.CompositionName);
            holder.relerase_time = convertView.findViewById(R.id.ReleaseTime);
            holder.seek_bar = convertView.findViewById(R.id.Process);
            holder.user_name = convertView.findViewById(R.id.username);
            holder.Play = convertView.findViewById(R.id.play);
            holder.Play.setTag(sign);
            holder.record_time = convertView.findViewById(R.id.CurrentProcess);
            holder.sound_length = convertView.findViewById(R.id.SoundLength);
            convertView.setTag(holder);
        } else holder = (RecordViewHold) convertView.getTag();


        //获取数据
        Moments momment = Mdata.get(position);

        //设置对应的item位置
        Mdata.get(position).playOrPause = holder.Play;
        if (Mdata.get(position).playOrPause == holder.Play)
            System.out.println("equal");

        /*修改头像*/
        holder.Profile_Photo.setImageDrawable(momment.getProfile_Photo());
        holder.user_name.setText(momment.getuser_name());
        System.out.println(momment.getuser_name());
        holder.composition_name.setText(momment.getcomposition_name());
        holder.Composition_introduction.setText(momment.getcomposition_introduction());
        holder.relerase_time.setText(momment.getrelerase_time());

        holder.seek_bar.setOnSeekBarChangeListener(new MySeekBar(Mdata.get(position).getmediaPlayer()));
        holder.seek_bar.setProgress(0);
        holder.seek_bar.setMax(Mdata.get(sign).mediaPlayer.getDuration());


        final Date date = new Date(Mdata.get(sign).mediaPlayer.getDuration());
        String time = format.format(date);
        holder.sound_length.setText(time);


        holder.Play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("id::" + v.getId());
                final int tag = ((Integer) v.getTag()).intValue();
                Log.i("jjf", "tag的值" + tag);
                Log.i("jjf", "sign的值" + sign);
                //根据点击的item中的值判断是否是同一个按钮
                if (playintNo != -1)//有播放
                {
                    if (playintNo != tag)//播放的音乐与点击的按钮冲突
                    {
                        System.out.println("22222 ");
                        Mdata.get(playintNo).mediaPlayer.pause();

                        Mdata.get(playintNo).playOrPause.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.play));

                        //holder.Play.setImageDrawable(getDrawable(R.drawable.play));
                        isPlay = true;
                        isFiring = false;
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                    } else {
                        System.out.println("11111 ");
                    }
                }

                if (isPlay) {
                    System.out.println("33333 ");
                    playintNo = tag;
                    //播放音乐
                    isFiring = true;

                    Mdata.get(playintNo).mediaPlayer.start();
                    holder.Play.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.pause));
                    timer = new Timer();
                    final Handler handler = new Handler();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (isFiring) {
                                        //seek_bar 运动
                                        holder.seek_bar.setProgress(Mdata.get(tag).mediaPlayer.getCurrentPosition());
                                        //播放时长更新
                                        holder.record_time.setText(format.format(Mdata.get(tag).mediaPlayer.getCurrentPosition()));

                                    }
                                }
                            });
                        }
                    }, 100, 100);
                    Mdata.get(tag).mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            System.out.println("55555 ");
                            holder.Play.setBackgroundResource(R.drawable.play);
                            isPlay = true;
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                        }
                    });

                    holder.Play.setBackgroundResource(R.drawable.play);
                    isPlay = false;
                } else {
                    System.out.println("44444 ");
                    Mdata.get(tag).mediaPlayer.pause();
                    //Mdata.get(tag).changePicToPlayOrPause(getDrawable(R.drawable.play));

                    holder.Play.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.play));
                    //holder.Play.setBackgroundResource(R.drawable.play);
                    isPlay = true;
                    isFiring = false;
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                }
                //按钮点击发生变化重绘item
                // notifyDataSetChanged();
            }
        });

        holder.seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    holder.seek_bar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        return convertView;
        /*进度条处理*/
        }

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
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarChanging = true;
                System.out.println("开始拖动");
            }

            /*滑动结束后，重新设置值*/
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarChanging = false;
                mediaPlayer.seekTo(seekBar.getProgress());
                System.out.println("滑动结束后，重新设置值");
            }

//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                if (isPlaying &&player.isPlaying()){
//                    seekBar.setTag(player.isPlaying());
//                    isPlaying =false;
//                    player.pause();
//                    playButton.setActivated(false);
//                }else {
//                    seekBar.setTag(false);
//                }
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                if ((boolean)seekBar.getTag()){
//                    playButton.setActivated(true);
//                    player.seekTo(seekBar.getProgress());
//                    player.start();
//                    Log.i("MediaPlayer", "onStopTrackingTouch: " +player.getCurrentPosition());
//                    thread =new MyThread();
//                    thread.start();
//                    isPlaying =true;
//                }else {
//                    player.seekTo(seekBar.getProgress());
//                }
//            }
        }
}

