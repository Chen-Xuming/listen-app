package com.example.xiong.myapplication.account;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.xiong.myapplication.R;
import com.example.xiong.myapplication.UserManager;
import com.example.xiong.myapplication.record.Record;
import com.example.xiong.myapplication.record.RecordActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.footer.BallPulseView;
import com.lcodecore.tkrefreshlayout.header.progresslayout.ProgressLayout;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AccountActivity extends AppCompatActivity {

    public ArrayList<Account> accountArray;

    private Toolbar toolbar;

    private ListView listView;
    private AccountAdapter accountAdapter;

    private TwinklingRefreshLayout refreshLayout;
    private ProgressLayout progressLayout;          // header
    private BallPulseView ballPulseView;            // footer

    private LinearLayout linearLayout;

    private String toolbar_titles [] = {
            "我的关注",
            "我的粉丝",
            "Ta的关注",
            "Ta的粉丝"
    };

    /*
            activity_type指定还哪个页面
            0~3 分别对应toolbar_titles各个标题
     */
    int activity_type;

    String apis[] = {
            "http://129.204.242.63:8080/listen/mainServlet?action=searchUserFollow",    //关注列表
            "http://129.204.242.63:8080/listen/mainServlet?action=searchUserFan"        //粉丝列表
    };

    String current_username;
    String query_username;

    private int offset = 0;
    private boolean dont_request = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_account);

        activity_type = getIntent().getIntExtra("activity_type", -1);

        toolbar = findViewById(R.id.account_activity_toolbar);

        toolbar.setTitle(toolbar_titles[activity_type]);

        setSupportActionBar(toolbar);
        // 点击返回箭头退出界面
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView = findViewById(R.id.account_activity_list);
        accountArray = new ArrayList<>();
        accountAdapter = new AccountAdapter(this, R.layout.account_item, accountArray);
        listView.setAdapter(accountAdapter);


        linearLayout = findViewById(R.id.account_activity_empty);


        /*
                上拉下滑：加载/刷新
         */
        refreshLayout = findViewById(R.id.account_activity_refreshLayout);

        progressLayout = new ProgressLayout(this);
        refreshLayout.setHeaderView(progressLayout);

        ballPulseView = new BallPulseView(this);
        refreshLayout.setBottomView(ballPulseView);

        refreshLayout.setOnRefreshListener(new RefreshListenerAdapter(){
            @Override
            public void onRefresh(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(AccountActivity.this, "refresh", Toast.LENGTH_SHORT).show();
                        refreshData();
                        refreshLayout.finishRefreshing();
                    }
                },1000);
            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(AccountActivity.this, "loadmore", Toast.LENGTH_SHORT).show();
                        loadMoreData();
                        refreshLayout.finishLoadmore();
                    }
                },1000);
            }
        });

        current_username = UserManager.getCurrentUser() == null ? ""
                : UserManager.getCurrentUser().getUsername();

        query_username = activity_type >= 2 ? getIntent().getStringExtra("username")
                : current_username;

        refreshLayout.startRefresh();
    }


    /*
            下拉刷新
     */
    private void refreshData(){
        requestData(1);
    }

    /*
            上滑加载更多
     */
    private void loadMoreData(){
        requestData(2);
    }

    /*
            网络请求数据
            type 1: 刷新
                 2: 更多
     */
    private void requestData(int type){

        if (type == 1){
            accountArray.clear();
            offset = 0;
            dont_request = false;
        }

        if(!dont_request){
            requestDataReal();
        }
    }


    private void requestDataReal(){
        String url = activity_type % 2 == 0 ? apis[0] : apis[1];

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("current_username", current_username)
                .add("target_username", query_username)
                .add("offset", String.valueOf(offset))
                .build();
        Request request = new Request.Builder()
                .url(url).post(body)
                .build();
        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(AccountActivity.this, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                if(jsonObject.get("code").getAsInt() == 1){
                    String data_string = jsonObject.get("data").getAsString();
                    JsonArray data = JsonParser.parseString(data_string).getAsJsonArray();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                        }
                    });

                    offset += data.size();
                    if(data.size() < 20){
                        dont_request = true;
                    }

                    for(JsonElement account : data){

                        JsonObject single_account = account.getAsJsonObject();
                        String username = single_account.get("username").getAsString();
                        int is_follow = single_account.get("follow").getAsInt();

                        String headPic = single_account.get("headPic") == null ?
                                null : single_account.get("headPic").getAsString();

                        Account add_account = new Account(username, headPic, is_follow == 1);
                        accountArray.add(add_account);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            accountAdapter.notifyDataSetChanged();
                        }
                    });
                }
                else {
                    dont_request = true;

                    if(offset == 0){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linearLayout.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.GONE);
                            }
                        });

                    }
                }
            }
        });
    }
}
