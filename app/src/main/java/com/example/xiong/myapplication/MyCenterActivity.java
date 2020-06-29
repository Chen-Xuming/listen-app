package com.example.xiong.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.wyp.avatarstudio.AvatarStudio;
import com.leon.lib.settingview.LSettingItem;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;

public class MyCenterActivity extends AppCompatActivity {

    private ImageView img_background;
    private ImageView img_headPic;
    private TextView tv_username;
    private LSettingItem followed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_center);

        img_background = findViewById(R.id.mycenter_bg);
        img_headPic = findViewById(R.id.mycenter_head);
        tv_username = findViewById(R.id.mycenter_username);
        followed = findViewById(R.id.item_3);

        // 加载图片
        init_pictures();

        // 换头像
        img_headPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePicture();
            }
        });
        followed.setmOnLSettingItemClick(new LSettingItem.OnLSettingItemClick() {
            @Override
            public void click(boolean isChecked) {
                goToFollowed();
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
                .load("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2102919628,431873309&fm=26&gp=0.jpg")
                .apply(options)
                .into(img_headPic);

        // 模糊背景图，模糊度15/25
        RequestOptions options1 = new RequestOptions()
                .transform(new BlurTransformation(15,4))
                .error(R.drawable.fox);
        Glide.with(this)
                .load("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2102919628,431873309&fm=26&gp=0.jpg")
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
                        //uri为图片路径
                        // 圆形头像, 并加白边框
                        RequestOptions options = new RequestOptions()
                                .transform(new CropCircleWithBorderTransformation(5, Color.WHITE))
                                .error(R.drawable.fox);
                        Glide.with(MyCenterActivity.this)
                                .load(uri)
                                .apply(options)
                                .into(img_headPic);

                        // 模糊背景图，模糊度15/25
                        RequestOptions options1 = new RequestOptions()
                                .transform(new BlurTransformation(15,4))
                                .error(R.drawable.fox);
                        Glide.with(MyCenterActivity.this)
                                .load(uri)
                                .apply(options1)
                                .into(img_background);
                    }
                });
    }

    //转入关注列表界面
    private void goToFollowed(){
        Intent intent = new Intent(MyCenterActivity.this,Followed.class);
        startActivity(intent);
    }
}
