/*
      我的主页（当前用户主页）
 */

package com.example.xiong.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.xiong.myapplication.account.AccountActivity;
import com.example.xiong.myapplication.record.RecordActivity;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.wyp.avatarstudio.AvatarStudio;
import com.leon.lib.settingview.LSettingItem;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyCenterActivity extends AppCompatActivity {

    private ImageView img_background;
    private ImageView img_headPic;
    private TextView tv_username;
    private LSettingItem item_creat;
    private LSettingItem item_collect;
    private LSettingItem item_follow;
    private LSettingItem item_fans;
    private LSettingItem item_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_center);

        img_background = findViewById(R.id.mycenter_bg);
        img_headPic = findViewById(R.id.mycenter_head);
        tv_username = findViewById(R.id.mycenter_username);
        item_creat = findViewById(R.id.mycenter_mycreat);
        item_collect = findViewById(R.id.mycenter_mycollect);
        item_fans = findViewById(R.id.mycenter_myfans);
        item_follow = findViewById(R.id.mycenter_myfollow);
        item_logout = findViewById(R.id.mycenter_logout);

        UserManager.init(this);
        tv_username.setText(UserManager.getCurrentUser().getUsername());

        // 加载图片
        init_pictures();

        // 换头像
        img_headPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePicture();
            }
        });

        // 发布页
        item_creat.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                Intent intent = new Intent(MyCenterActivity.this, RecordActivity.class);
                intent.putExtra("activity_type", 0);
                startActivity(intent);
            }
        });

        // 收藏页
        item_collect.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                Intent intent = new Intent(MyCenterActivity.this, RecordActivity.class);
                intent.putExtra("activity_type", 1);
                startActivity(intent);
            }
        });

        // 关注页
        item_follow.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                Intent intent = new Intent(MyCenterActivity.this, AccountActivity.class);
                intent.putExtra("activity_type", 0);
                startActivity(intent);
            }
        });

        // 粉丝页
        item_fans.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                Intent intent = new Intent(MyCenterActivity.this, AccountActivity.class);
                intent.putExtra("activity_type", 1);
                startActivity(intent);
            }
        });

        // 退出
        item_logout.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                UserManager.getCurrentUser().logout(MyCenterActivity.this);
                Intent intent = new Intent(MyCenterActivity.this, login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /*
            初始化加载图片
     */
    private void init_pictures(){
        String headpic = UserManager.getCurrentUser().getHeadPic_url();

        // 圆形头像, 并加白边框
        RequestOptions options = new RequestOptions()
                .transform(new CropCircleWithBorderTransformation(5, Color.WHITE))
                .error(R.drawable.fox);
        Glide.with(this)
                .load(headpic == null ? R.drawable.fox : headpic)
                .apply(options)
                .into(img_headPic);

        // 模糊背景图，模糊度15/25
        RequestOptions options1 = new RequestOptions()
                .transform(new BlurTransformation(15,4))
                .error(R.drawable.fox);
        Glide.with(this)
                .load(headpic == null ? R.drawable.fox : headpic)
                .apply(options1)
                .into(img_background);
    }

    /*
            换头像
     */
    private void changePicture(){
        new AvatarStudio.Builder(MyCenterActivity.this)
                .needCrop(true)                             //是否裁剪，默认裁剪
                .setTextColor(Color.BLUE)
                .dimEnabled(true)                           //背景是否dim 默认true
                .setAspect(1, 1)            //裁剪比例 默认1：1
                .setOutput(200, 200)        //裁剪大小 默认200*200
                .setText("打开相机", "从相册中选取", "取消")
                .show(new AvatarStudio.CallBack() {
                    @Override
                    public void callback(String uri) {
                        upLoadFile(uri);
                    }
                });
    }

    private void upLoadFile(final String uri){

        File file = new File(uri);
        String filename = file.getName();

        Log.d("create_record", "filePath:" + uri + "\nfilename:" + filename);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", filename,
                        RequestBody.create(MediaType.parse("multipart/form-data"),
                                new File(uri)))
                .build();

        Request request = new Request.Builder()
                .url("http://129.204.242.63:8080/listen/mainServlet?action=getIconUrl")
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        final KProgressHUD hud =
                KProgressHUD.create(MyCenterActivity.this)
                        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setLabel("请稍等")
                        .setCancellable(false)
                        .setAnimationSpeed(2)
                        .setDimAmount(0.5f)
                        .show();

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                hud.dismiss();
                Looper.prepare();
                Toast.makeText(MyCenterActivity.this, "更改失败，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                if(jsonObject.get("code").getAsInt() == 1){
                    String response_url = jsonObject.get("data").getAsString();
                    sendIcon(response_url, uri);
                    hud.dismiss();
                }else{
                    hud.dismiss();
                    Looper.prepare();
                    Toast.makeText(MyCenterActivity.this, "更改失败，请重试。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }

    private void sendIcon(final String response_url, final String local_uri){
        String api = "http://129.204.242.63:8080/listen/mainServlet?action=changeIcon";

        String headPicUrl = "http://129.204.242.63:8080/icon/" + response_url;

        String username = UserManager.getCurrentUser().getUsername();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("headPicUrl", headPicUrl)
                .build();
        Request request = new Request.Builder()
                .url(api).post(body)
                .build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(MyCenterActivity.this, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                if(jsonObject.get("code").getAsInt() == 1){

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //uri为图片路径
                            // 圆形头像, 并加白边框
                            RequestOptions options = new RequestOptions()
                                    .transform(new CropCircleWithBorderTransformation(5, Color.WHITE))
                                    .error(R.drawable.fox);
                            Glide.with(MyCenterActivity.this)
                                    .load(local_uri)
                                    .apply(options)
                                    .into(img_headPic);

                            // 模糊背景图，模糊度15/25
                            RequestOptions options1 = new RequestOptions()
                                    .transform(new BlurTransformation(15,4))
                                    .error(R.drawable.fox);
                            Glide.with(MyCenterActivity.this)
                                    .load(local_uri)
                                    .apply(options1)
                                    .into(img_background);
                        }
                    });
                }
                else{
                    Looper.prepare();
                    Toast.makeText(MyCenterActivity.this, "抱歉，请重试。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });

    }
}
