package com.example.xiong.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;

public class AccountAdapter extends ArrayAdapter<Account> {
    private int resourceId;
    private ImageView acImage;
    private TextView acName;
    private TextView follow;
    private Context context1;
    public AccountAdapter(Context context, int itemResourceId, List<Account> obj){
        super(context,itemResourceId,obj);
        context1 = context;
        resourceId = itemResourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Account ac = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        acImage = view.findViewById(R.id.account_image);
        acName = view.findViewById(R.id.account_name);
        follow = view.findViewById(R.id.follow);
        acImage.setImageResource(ac.getImage());
        acName.setText(ac.getName());
        init_pictures(view);
        if(ac.is_followed==true){
            follow.setText("已关注");
            follow.setTextColor(context1.getResources().getColor(R.color.Gray));
            follow.setBackground(context1.getDrawable(R.drawable.follow2_btn));

        }else {
            follow.setText("+ 关注");
            follow.setTextColor(context1.getResources().getColor(R.color.Orange1));
            follow.setBackground(context1.getDrawable(R.drawable.follow_btn));
        }
        follow.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Toast.makeText(context1,"click follow",Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
    private void init_pictures(View view){
        RequestOptions options = new RequestOptions()
                .transform(new CropCircleWithBorderTransformation(5,Color.WHITE))
                .error(R.drawable.warning);
        Glide.with(view)
                .load("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2102919628,431873309&fm=26&gp=0.jpg")
                .apply(options)
                .into(acImage);
    }
}
