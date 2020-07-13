package com.example.xiong.myapplication.record;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
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
import com.example.xiong.myapplication.login;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import static android.os.Environment.DIRECTORY_MUSIC;


public class RecordRecyclerAdapter extends RecyclerView.Adapter<RecordRecyclerAdapter.RecordViewHolder>
                                                implements MediaPlayerUtils.Listener{

    private Context context;
    private ArrayList<Record> record_list;      // 音频信息

    public MediaPlayerUtils mediaPlayerUtils;

    // 图标
    private Drawable drawable_play;
    private Drawable drawable_pause;
    private Drawable drawable_like_1;
    private Drawable drawable_like_2;
    private Drawable drawable_collect_1;
    private Drawable drawable_collect_2;

    public RecordRecyclerAdapter(Context context, ArrayList<Record> list){
        this.context = context;
        this.record_list = list;

        drawable_play = ContextCompat.getDrawable(context, R.drawable.play);
        drawable_pause = ContextCompat.getDrawable(context, R.drawable.pause);
        drawable_like_1 = ContextCompat.getDrawable(context, R.drawable.like_empty);        // 空心赞
        drawable_like_2 = ContextCompat.getDrawable(context, R.drawable.like_orange);
        drawable_collect_1 = ContextCompat.getDrawable(context, R.drawable.collect_empty);   // 空心收藏
        drawable_collect_2 = ContextCompat.getDrawable(context, R.drawable.collect_orange);

        mediaPlayerUtils = new MediaPlayerUtils();
        mediaPlayerUtils.listener = this;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.cardview, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecordViewHolder holder, final int position) {
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

        // 点赞
        holder.img_like.setImageDrawable(
                record.isLike() == 1 ? drawable_like_2 : drawable_like_1
        );
        holder.img_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点赞/取消点赞

                // 只有登录了才能点赞
                if(UserManager.getCurrentUser() == null){
                    Intent intent = new Intent(context, login.class);
                    context.startActivity(intent);
                    return;
                }

                likeOrNot(position);

            }
        });

        // 收藏
        holder.img_collect.setImageDrawable(
                record.isCollect() == 1 ? drawable_collect_2 : drawable_collect_1
        );
        holder.img_collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 收藏/取消收藏

                // 只有登录了才能点赞
                if(UserManager.getCurrentUser() == null){
                    Intent intent = new Intent(context, login.class);
                    context.startActivity(intent);
                    return;
                }

                collectOrNot(position);
            }
        });


        // 下载
        holder.img_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(record.isDownload() == 0){
                    /*
                        网络请求
                    */
                    Toast.makeText(context, "已加入下载队列，请稍等。", Toast.LENGTH_SHORT).show();
                    download(position, record.getTitle());
                }
                else{
                    Toast.makeText(context, "已经下载了。", Toast.LENGTH_SHORT).show();
                }
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
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position, @NonNull List<Object> payloads) {
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

        ImageView img_like;
        ImageView img_collect;
        ImageView img_download;

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
            img_like = itemView.findViewById(R.id.record_like);
            img_collect = itemView.findViewById(R.id.record_collect);
            img_download = itemView.findViewById(R.id.record_download);
        }
    }

    /*
            点赞网络请求
     */
    private void likeOrNot(final int position){
        String url = "http://129.204.242.63:8080/listen/mainServlet?action=addLikes";

        final Record record = record_list.get(position);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("record_id", String.valueOf(record.getId()))
                .add("current_username", UserManager.getCurrentUser().getUsername())
                .add("canel", String.valueOf(record.isLike() ==  0 ? 1 : 0))
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
                    record.setIs_like(record.isLike() == 0 ? 1 : 0);
                    //notifyItemChanged(position);

                    Message message = new Message();
                    message.what = 1;
                    Bundle b = new Bundle();
                    b.putInt("position", position);
                    message.setData(b);
                    mHandler.sendMessage(message);

                }else{
                    Looper.prepare();
                    Toast.makeText(context, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    /*
            收藏
     */
    private void collectOrNot(final int position){
        String url = "http://129.204.242.63:8080/listen/mainServlet?action=addCollect";

        final Record record = record_list.get(position);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("record_id", String.valueOf(record.getId()))
                .add("current_username", UserManager.getCurrentUser().getUsername())
                .add("canel", String.valueOf(record.isCollect() ==  0 ? 1 : 0))
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
                    record.setIs_collect(record.isCollect() == 0 ? 1 : 0);

                    Message message = new Message();
                    message.what = 2;
                    Bundle b = new Bundle();
                    b.putInt("position", position);
                    message.setData(b);
                    mHandler.sendMessage(message);

                }else{
                    Looper.prepare();
                    Toast.makeText(context, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    /*
            下载
     */
    private void download(final int position, final String title){

        final String sound_url = record_list.get(position).getSound_url();

        if(sound_url == null) return;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(sound_url)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(context, "下载失败, 请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                Log.d("download", "download1");


                InputStream inputStream = response.body().byteStream();
                String dir = Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC)
                        .getAbsolutePath();

                //String filename = sound_url.replace("http://129.204.242.63:8080/music/","");

                String filename = title + ".mp3";


                File file = new File(dir, filename);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] temp = new byte[128];
                int length;
                while((length = inputStream.read(temp)) != -1){
                    fileOutputStream.write(temp, 0, length);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();

                Log.d("download", "download2");

                record_list.get(position).setIs_download(1);
                Message message = new Message();
                message.what = 3;
                Bundle b = new Bundle();
                b.putInt("position", position);
                message.setData(b);
                mHandler.sendMessage(message);

                Looper.prepare();
                Toast.makeText(context, dir + "/" + filename +  "下载成功！",
                        Toast.LENGTH_LONG).show();
                Looper.loop();
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
                // 点赞, 收藏, 下载
                case 1:
                case 2:
                case 3:
                    notifyItemChanged(msg.getData().getInt("position"));
                    break;
            }

            return false;
        }
    });




}
