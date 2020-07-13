package com.example.xiong.myapplication.record;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.xiong.myapplication.R;
import com.example.xiong.myapplication.UserCenterActivity;
import com.example.xiong.myapplication.UserManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
        用于“我的作品”的只有删除操作的adapter
 */
public class RecordRecyclerDeleteAdapter extends RecyclerView.Adapter<RecordRecyclerDeleteAdapter.RecordViewHolder>
        implements MediaPlayerUtils.Listener{


    private Context context;
    private ArrayList<Record> record_list;      // 音频信息

    public MediaPlayerUtils mediaPlayerUtils;

    // 图标
    private Drawable drawable_play;
    private Drawable drawable_pause;

    public RecordRecyclerDeleteAdapter(Context context, ArrayList<Record> list){
        this.context = context;
        this.record_list = list;

        drawable_play = ContextCompat.getDrawable(context, R.drawable.play);
        drawable_pause = ContextCompat.getDrawable(context, R.drawable.pause);

        mediaPlayerUtils = new MediaPlayerUtils();
        mediaPlayerUtils.listener = this;
    }

    @NonNull
    @Override
    public RecordRecyclerDeleteAdapter.RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.cardview_delete, parent, false);
        return new RecordRecyclerDeleteAdapter.RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecordRecyclerDeleteAdapter.RecordViewHolder holder, final int position) {
        final Record record = record_list.get(position);

        holder.tv_username.setText(record.getUsername());
        holder.tv_title.setText(record.getTitle());
        holder.tv_description.setText(record.getDescription());
        holder.tv_create_time.setText(record.getCreate_time());

        int run_time = record.getCurrent_position();
        int total_time = record.getDuration();

        String run_str = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) run_time),
                TimeUnit.MILLISECONDS.toSeconds((long) run_time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) run_time)));

        String total_str = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) total_time),
                TimeUnit.MILLISECONDS.toSeconds((long) total_time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) total_time)));

        holder.tv_current_process.setText(run_str);
        holder.tv_sound_length.setText(total_str);


        // 如果没有头像，就用内置头像
        RequestOptions options = new RequestOptions()
                .transform(new CircleCrop())
                .error(R.drawable.fox);
        if (record.getHeadpic_url() != null) {
            Glide.with(this.context).load(record.getHeadpic_url())
                    .apply(options).into(holder.img_headpic);
        } else {
            Glide.with(this.context).load(R.drawable.fox)
                    .apply(options).into(holder.img_headpic);
        }
        // 点击头像进入Ta的主页
        holder.img_headpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                    goto activity
                 */
                Intent intent = new Intent(context, UserCenterActivity.class);
                intent.putExtra("username", record.getUsername());
                intent.putExtra("headpic_url", record.getHeadpic_url() == null ? "" :
                        record.getHeadpic_url());


                if(UserManager.getCurrentUser() == null){
                    intent.putExtra("is_follow", 0);
                    context.startActivity(intent);
                }else{
                    queryFollowAndGo(record.getUsername(), record.getHeadpic_url());
                }
            }
        });


        /*
                删除
         */
        holder.img_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                removeItem(position);

            }
        });



        /*
            进度条
        */
        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) mediaPlayerUtils.applySeekBarValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        holder.seekBar.setMax(record.getPlayer_status() == 0 ?
                1 : record.getMediaPlayer().getDuration());         // max = 0也会引发下面的问题
        if(record.getCurrent_position() == 0){
            holder.seekBar.setProgress(1);      // 若progress = 0， 将不会调用onProgressChange，而引发错误
        }
        holder.seekBar.setProgress(record.getCurrent_position());
        holder.seekBar.setEnabled(record.getPlay_button() == 0);


        // 根据播放状态设置播放按钮
        if(record.getPlay_button() == 0){
            holder.img_play.setImageDrawable(drawable_pause);
        }else{
            holder.img_play.setImageDrawable(drawable_play);
        }

        /*
                播放按钮点击事件
         */
        holder.img_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("click", "position" + position);

                // 1. 点击播放一条声音， 该声音还没有进行加载， 则进行加载。 停止上一条声音的播放并做好信息记录。
                // 2. 点击播放一条声音， 该声音已经加载过了， 则进行播放。   停止上一条声音的播放并做好信息记录。
                // 3. 点击暂停音乐。

                /*
                        点击的和上一条位置不同， 则暂停上一首
                 */
                if(mediaPlayerUtils.mediaPlayer != null && mediaPlayerUtils.mediaPlayer.isPlaying()){
                    //mediaPlayer.pause();
                    record_list.get(mediaPlayerUtils.old_position).setCurrent_position(mediaPlayerUtils.mediaPlayer.getCurrentPosition());
                    mediaPlayerUtils.pauseMediaPlayer();

                    if(mediaPlayerUtils.mediaPlayer == record.getMediaPlayer()) {
                        record.setPlay_button(1);
                        holder.img_play.setImageDrawable(drawable_play);
                        return;
                    }

                    record_list.get(mediaPlayerUtils.old_position).setPlay_button(1);
                    notifyItemChanged(mediaPlayerUtils.old_position, 1);
                }

                mediaPlayerUtils.old_position = holder.getAdapterPosition();
                mediaPlayerUtils.mediaPlayer = record.getMediaPlayer();
                holder.img_play.setImageDrawable(drawable_pause);
                record.setPlay_button(0);

                holder.seekBar.setEnabled(true);

                /*
                        未加载则加载
                 */
                if(record.getPlayer_status() == 0){
                    record.getMediaPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            holder.seekBar.setMax(record.getMediaPlayer().getDuration());
                            record.setPlayer_status(2);
                            //Toast.makeText(context, "准备好了！", Toast.LENGTH_SHORT).show();

                            int total_time = record.getMediaPlayer().getDuration();

                            record.setDuration(total_time);

                            String total_str = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) total_time),
                                    TimeUnit.MILLISECONDS.toSeconds((long) total_time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) total_time)));
                            holder.tv_sound_length.setText(total_str);

                            if(record.getPlay_button() == 0){
                                mediaPlayerUtils.playMediaPlayer();
                            }
                        }
                    });


                    record.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mediaPlayerUtils.playMediaPlayer();
                        }
                    });

                    try {
                        if(record.getSound_url() != null){
                            record.getMediaPlayer().reset();
                            record.getMediaPlayer().setDataSource(record.getSound_url());
                            record.getMediaPlayer().prepareAsync();
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                /*
                        已加载则播放
                 */
                else{
                    mediaPlayerUtils.playMediaPlayer();
                }
            }
        });
    }

    /*
            item的局部刷新
     */
    @Override
    public void onBindViewHolder(@NonNull RecordRecyclerDeleteAdapter.RecordViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            int change = (int)payloads.get(0);
            if(change == 1) {
                holder.img_play.setImageDrawable(drawable_play);
                holder.seekBar.setEnabled(false);
            }
            if(change == 2){
                if(holder.seekBar.isEnabled()) {
                    holder.seekBar.setProgress(mediaPlayerUtils.mediaPlayer.getCurrentPosition());

                    int run_time = mediaPlayerUtils.mediaPlayer.getCurrentPosition();
                    String run_str = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) run_time),
                            TimeUnit.MILLISECONDS.toSeconds((long) run_time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) run_time)));
                    holder.tv_current_process.setText(run_str);

                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return record_list.size();
    }

    /*
            更新Seekbar
     */
    @Override
    public void onAudioUpdate(int item_position){
        notifyItemChanged(item_position, 2);
    }


    public void pausePlayer(){
        mediaPlayerUtils.pauseMediaPlayer();
        if(mediaPlayerUtils.old_position != -1){
            record_list.get(mediaPlayerUtils.old_position).setCurrent_position(mediaPlayerUtils.mediaPlayer.getCurrentPosition());
            record_list.get(mediaPlayerUtils.old_position).setPlay_button(1);
            notifyItemChanged(mediaPlayerUtils.old_position, 1);
        }
    }


    class RecordViewHolder extends RecyclerView.ViewHolder{
        ImageView img_headpic;
        TextView tv_username;
        TextView tv_title;
        TextView tv_description;
        TextView tv_create_time;
        ImageView img_play;
        TextView tv_current_process;
        TextView tv_sound_length;
        SeekBar seekBar;

        ImageView img_delete;

        public RecordViewHolder(View itemView) {
            super(itemView);
            tv_username = itemView.findViewById(R.id.username);
            img_headpic = itemView.findViewById(R.id.ProfilePhoto);
            tv_title = itemView.findViewById(R.id.CompositionName);
            tv_description = itemView.findViewById(R.id.introduction);
            tv_create_time = itemView.findViewById(R.id.ReleaseTime);
            img_play = itemView.findViewById(R.id.play);
            tv_current_process = itemView.findViewById(R.id.CurrentProcess);
            tv_sound_length = itemView.findViewById(R.id.SoundLength);
            seekBar = itemView.findViewById(R.id.Process);
            img_delete = itemView.findViewById(R.id.record_delete);
        }
    }

    /*
            删除
     */
    private void removeItem(final int position){
        String url = "http://129.204.242.63:8080/listen/mainServlet?action=deleteRecord";

        final Record record = record_list.get(position);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(record.getId()))
                .build();
        Request request = new Request.Builder()
                .url(url).post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(context, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                if(jsonObject.get("code").getAsInt() == 1){

                    record_list.remove(position);

                    Message message = new Message();
                    message.what = 1;
                    Bundle b = new Bundle();
                    b.putInt("position", position);
                    message.setData(b);
                    mHandler.sendMessage(message);

                }else{
                    Looper.prepare();
                    Toast.makeText(context, "服务器出错，请重试。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }


    /*

            进入用户主页
    */
    private void queryFollowAndGo(final String tartget_username, final String headpic_url){
        String current_username = UserManager.getCurrentUser().getUsername();

        String url = "http://129.204.242.63:8080/listen/mainServlet?action=isFollow";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("current_username", current_username)
                .add("target_username", tartget_username)
                .build();
        Request request = new Request.Builder()
                .url(url).post(body)
                .build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(context, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                Intent intent = new Intent(context, UserCenterActivity.class);
                intent.putExtra("username", tartget_username);
                intent.putExtra("headpic_url", headpic_url == null ? "" : headpic_url);
                intent.putExtra("is_follow", jsonObject.get("code").getAsInt());
                context.startActivity(intent);
            }
        });
    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                // 删除
                case 1:
                    int position = msg.getData().getInt("position");
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, record_list.size() - position);
                    break;
            }

            return false;
        }
    });
}
