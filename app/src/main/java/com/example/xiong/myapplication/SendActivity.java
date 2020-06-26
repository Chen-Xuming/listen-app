package com.example.xiong.myapplication;

/*
            发布动态的页面，目前剩下网络部分未完成 -- 2020/6/26

 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiong.myapplication.util.PermissionUtil;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.AudioPickActivity;
import com.vincent.filepicker.filter.entity.AudioFile;

import java.util.ArrayList;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;
import nl.changer.audiowife.AudioWife;

import static android.os.Environment.DIRECTORY_MUSIC;
import static com.vincent.filepicker.activity.AudioPickActivity.IS_NEED_RECORDER;

public class SendActivity extends AppCompatActivity {

    private static final String TAG = SendActivity.class.getSimpleName();

    private PermissionUtil permissionUtil;


    /* 播放器 */
    private LinearLayout player;
    private Context mContext;
    private View mPlayMedia;
    private View mPauseMedia;
    private ImageView mDelete;
    private SeekBar mMediaSeekBar;
    private TextView mRunTime;
    private TextView mTotalTime;
    private TextView mPlaybackTime;
    private Uri mUri = null;
    private static final int INTENT_PICK_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_send);

        permissionUtil = new PermissionUtil(this);
        permissionUtil.requestPermission();

        Toolbar toolbar = (Toolbar) findViewById(R.id.send_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 点击返回箭头退出界面
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // 选择音频文件
        ImageButton imgbtn_select = findViewById(R.id.send_select_audio);
        imgbtn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickAudio();
            }
        });

        // 现场录音
        ImageButton imgbtn_create = findViewById(R.id.send_create_audio);
        imgbtn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordAudio();
            }
        });

        initPlayer();
    }

    /*
            初始化播放器
     */
    private void initPlayer(){
        mContext = this;

        // initialize the player controls
        player = findViewById(R.id.send_player_layout);
        mPlayMedia = findViewById(R.id.send_player_play);
        mPauseMedia = findViewById(R.id.send_player_pause);
        mMediaSeekBar = (SeekBar) findViewById(R.id.send_player_seekbar);
        mRunTime = (TextView) findViewById(R.id.send_player_runtime);
        mTotalTime = (TextView) findViewById(R.id.send_palyer_totaltime);
        mDelete = findViewById(R.id.send_audio_delete);

        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioWife.getInstance().pause();
                AudioWife.getInstance().release();
                mUri = null;
                player.setVisibility(View.GONE);
            }
        });

        player.setVisibility(View.GONE);
    }


    /*
            选择音频文件
     */
    private void pickAudio() {
        AudioWife.getInstance().pause();
        //AudioWife.getInstance().release();
        Intent intent3 = new Intent(SendActivity.this, AudioPickActivity.class);
        intent3.putExtra(IS_NEED_RECORDER, false);
        intent3.putExtra(Constant.MAX_NUMBER, 1);
        startActivityForResult(intent3, Constant.REQUEST_CODE_PICK_AUDIO);
    }


    /*
            现场录音
     */
    private void recordAudio(){

        AudioWife.getInstance().pause();

        String filePath = Environment.getExternalStoragePublicDirectory(DIRECTORY_MUSIC)
                .getAbsolutePath();
        int requestCode = 0;
        AndroidAudioRecorder.with(SendActivity.this)
                // Required
                .setFilePath(filePath)
                .setColor(Color.rgb(20,68,106))
                .setRequestCode(requestCode)

                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_48000)
                .setAutoStart(false)
                .setKeepDisplayOn(true)

                // Start recording
                .record();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
                选择音频完毕后，配置播放器
         */
        if(requestCode == Constant.REQUEST_CODE_PICK_AUDIO){
            if (resultCode == RESULT_OK) {
                ArrayList<AudioFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_AUDIO);
                AudioFile file = list.get(0);

                mUri = Uri.parse(file.getPath());

                setPlayer();
            }
        }

        /*
        *       录音完毕后，配置播放器
        * */
        if(requestCode == 0){
            if (resultCode == RESULT_OK) {
                String mp3 = data.getStringExtra("mp3_file_path");
                mUri = Uri.parse(mp3);
                Toast.makeText(this, "录音已保存至:\n" + mp3,
                        Toast.LENGTH_SHORT).show();

                setPlayer();

            } else if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
            }else if(resultCode == 2){
                Toast.makeText(this, "抱歉，录音保存失败。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*
            得到文件后，装载音频
     */
    private void setPlayer(){

        player.setVisibility(View.VISIBLE);

        AudioWife.getInstance().pause();
        AudioWife.getInstance().release();

        AudioWife.getInstance()
                .init(mContext, mUri)
                .setPlayView(mPlayMedia)
                .setPauseView(mPauseMedia)
                .setSeekBar(mMediaSeekBar)
                .setRuntimeView(mRunTime)
                .setTotalTimeView(mTotalTime);

        AudioWife.getInstance().addOnPlayClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), "Play", Toast.LENGTH_SHORT).show();
                // get-set-go. Lets dance.
                AudioWife.getInstance().play();
            }
        });

        AudioWife.getInstance().addOnPauseClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(getBaseContext(), "Pause", Toast.LENGTH_SHORT).show();
                // Your on audio pause stuff.
                AudioWife.getInstance().pause();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_send_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.send_send) {
            sendAudio();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
            发布音频:
            1. 检查内容是否填好、是否有音频
            2. 发送并回馈结果
     */
    private void sendAudio(){

        EditText edit_title = findViewById(R.id.send_title);
        EditText edit_description = findViewById(R.id.send_description);

        String str1 = edit_title.getText().toString().trim();
        String str2 = edit_description.getText().toString().trim();

        if(str1.equals("") || str2.equals("")){
            Toast.makeText(SendActivity.this, "请将内容填写完整。", Toast.LENGTH_SHORT).show();
            return;
        }

        if(mUri == null){
            Toast.makeText(SendActivity.this, "请选择音频。", Toast.LENGTH_SHORT).show();
            return;
        }

        /*
                网络部分





         */


        Toast.makeText(SendActivity.this, "发布成功！", Toast.LENGTH_SHORT).show();
    }
}
