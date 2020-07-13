/*
        用户主页
 */

package com.example.xiong.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.xiong.myapplication.account.Account;
import com.example.xiong.myapplication.account.AccountActivity;
import com.example.xiong.myapplication.record.RecordActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leon.lib.settingview.LSettingItem;
import com.wyp.avatarstudio.AvatarStudio;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserCenterActivity extends AppCompatActivity {

    private ImageView img_background;
    private ImageView img_headPic;
    private Button btn_follow;
    private TextView tv_username;
    private LSettingItem item_creat;
    private LSettingItem item_collect;
    private LSettingItem item_follow;
    private LSettingItem item_fans;

    String headpic_url;
    String username;
    int isFollow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_center);

        img_background = findViewById(R.id.user_bg);
        img_headPic = findViewById(R.id.user_head);
        btn_follow = findViewById(R.id.user_btn_follow);
        tv_username = findViewById(R.id.user_username);
        item_creat = findViewById(R.id.user_creat);
        item_collect = findViewById(R.id.user_collect);
        item_fans = findViewById(R.id.user_fans);
        item_follow = findViewById(R.id.user_follow);


        username = getIntent().getStringExtra("username");
        headpic_url = getIntent().getStringExtra("headpic_url");

        isFollow = getIntent().getIntExtra("is_follow", -1);


        Log.d("isFollow", String.valueOf(isFollow));

        tv_username.setText(username);

        btn_follow.setText(isFollow == 0 ? "+ 关注" : "取消关注");

        // 加载图片
        init_pictures();


        // 发布页
        item_creat.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                Intent intent = new Intent(UserCenterActivity.this, RecordActivity.class);
                intent.putExtra("activity_type", 2);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        // 收藏页
        item_collect.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                Intent intent = new Intent(UserCenterActivity.this, RecordActivity.class);
                intent.putExtra("activity_type", 3);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        // 关注页
        item_follow.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                Intent intent = new Intent(UserCenterActivity.this, AccountActivity.class);
                intent.putExtra("activity_type", 2);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        // 粉丝页
        item_fans.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                Intent intent = new Intent(UserCenterActivity.this, AccountActivity.class);
                intent.putExtra("activity_type", 3);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        // 关注 / 取消关注
        btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(UserManager.getCurrentUser() == null){
                    Intent intent = new Intent(UserCenterActivity.this, login.class);
                    startActivity(intent);
                    return;
                }


                followOrNot();
//                isFollow = isFollow == 0 ? 1 : 0;
//                btn_follow.setText(isFollow == 0 ? "+ 关注" : "取消关注");

            }
        });

    }

    /*
            初始化加载图片
     */
    private void init_pictures(){
        // 圆形头像, 并加白边框
        RequestOptions options = new RequestOptions()
                .transform(new CropCircleWithBorderTransformation(5, Color.WHITE))
                .error(R.drawable.fox);
        Glide.with(this)
                .load(headpic_url.isEmpty() ? R.drawable.fox : headpic_url)
                .apply(options)
                .into(img_headPic);

        // 模糊背景图，模糊度15/25
        RequestOptions options1 = new RequestOptions()
                .transform(new BlurTransformation(15,4))
                .error(R.drawable.fox);
        Glide.with(this)
                .load(headpic_url.isEmpty() ? R.drawable.fox : headpic_url)
                .apply(options1)
                .into(img_background);
    }

    private void followOrNot(){
        String url = "http://129.204.242.63:8080/listen/mainServlet?action=addFollow";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("follow_username", username)
                .add("current_username", UserManager.getCurrentUser().getUsername())
                .add("canel", String.valueOf(isFollow == 0 ? 1 : 0))
                .build();
        Request request = new Request.Builder()
                .url(url).post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(UserCenterActivity.this, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                if(jsonObject.get("code").getAsInt() == 1){

                    isFollow = isFollow == 0 ? 1 : 0;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btn_follow.setText(isFollow == 0 ? "+ 关注" : "取消关注");
                        }
                    });

                }else{
                    Looper.prepare();
                    Toast.makeText(UserCenterActivity.this, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }


}
