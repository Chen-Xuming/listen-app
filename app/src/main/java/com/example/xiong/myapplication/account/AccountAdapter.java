package com.example.xiong.myapplication.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.xiong.myapplication.R;
import com.example.xiong.myapplication.UserCenterActivity;
import com.example.xiong.myapplication.UserManager;
import com.example.xiong.myapplication.account.Account;
import com.example.xiong.myapplication.login;
import com.example.xiong.myapplication.record.Record;
import com.example.xiong.myapplication.record.RecordActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleWithBorderTransformation;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountAdapter extends ArrayAdapter<Account> {
    private int resourceId;

    private ImageView acImage;
    private TextView acName;
    private TextView follow;

    private Context context1;

    ArrayList<Account> account_list;

    public AccountAdapter(Context context, int itemResourceId, ArrayList<Account> obj){
        super(context,itemResourceId,obj);
        context1 = context;
        resourceId = itemResourceId;
        account_list = obj;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final Account ac = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        acImage = view.findViewById(R.id.account_image);
        acName = view.findViewById(R.id.account_name);
        follow = view.findViewById(R.id.follow);

        acName.setText(ac.getName());

        init_pictures(view, position);

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
                if(UserManager.getCurrentUser() == null){
                    Intent intent = new Intent(context1, login.class);
                    context1.startActivity(intent);
                    return;
                }

                followOrNot(position);
            }
        });


        acImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                    goto activity
                 */

                Intent intent = new Intent(context1, UserCenterActivity.class);
                intent.putExtra("username", ac.getName());
                intent.putExtra("headpic_url", ac.getHeadpic_url() == null ? "" :
                        ac.getHeadpic_url());


                /*
                        isFollow
                 */


                if(UserManager.getCurrentUser() == null){

                    Log.d("isUserNull", "yes");

                    intent.putExtra("is_follow", 0);
                    context1.startActivity(intent);
                }else{
                    Log.d("isUserNull", "no");

                    queryFollowAndGo(ac.getName(), ac.getHeadpic_url());
                }

            }
        });

        return view;
    }

    private void init_pictures(View view, int position){
        RequestOptions options = new RequestOptions()
                .transform(new CropCircleWithBorderTransformation(5,Color.WHITE))
                .error(R.drawable.fox);
        Glide.with(view)
                .load(account_list.get(position).getHeadpic_url() == null ?
                        R.drawable.fox : account_list.get(position).getHeadpic_url())
                .apply(options)
                .into(acImage);
    }

    private void followOrNot(final int position){
        String url = "http://129.204.242.63:8080/listen/mainServlet?action=addFollow";

        final Account account = account_list.get(position);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("follow_username", account.getName())
                .add("current_username", UserManager.getCurrentUser().getUsername())
                .add("canel", String.valueOf(account.isIs_followed() ? 0 : 1))
                .build();
        Request request = new Request.Builder()
                .url(url).post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(context1, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                if(jsonObject.get("code").getAsInt() == 1){

                    account.setIs_followed(!account.isIs_followed());

                    Message message = new Message();
                    message.what = 1;
                    mHandler.sendMessage(message);

                }else{
                    Looper.prepare();
                    Toast.makeText(context1, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                    Looper.loop();
                }
            }
        });
    }


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

        Log.d("isFollow_request", "cur_name: " + current_username
                                + "  tar_name: " + tartget_username);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(context1, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                Log.d("isFollow_result", result);

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                int is_follow = jsonObject.get("code").getAsInt();

                Intent intent = new Intent(context1, UserCenterActivity.class);
                intent.putExtra("username", tartget_username);
                intent.putExtra("headpic_url", headpic_url == null ? "" : headpic_url);
                intent.putExtra("is_follow", is_follow);

                Log.d("isFollow_response", String.valueOf(is_follow));

                context1.startActivity(intent);
            }
        });
    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    notifyDataSetChanged();
                    break;
            }

            return false;
        }
    });
}
