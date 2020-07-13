package com.example.xiong.myapplication.record;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.xiong.myapplication.R;
import com.example.xiong.myapplication.UserManager;
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

public class RecordActivity extends AppCompatActivity {

    public ArrayList<Record> recordArray;

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecordRecyclerAdapter recordRecyclerAdapter = null;
    private RecordRecyclerDeleteAdapter recordRecyclerDeleteAdapter = null;

    private TwinklingRefreshLayout refreshLayout;
    private ProgressLayout progressLayout;          // header
    private BallPulseView ballPulseView;            // footer

    private LinearLayout linearLayout;

    private String toolbar_titles [] = {
            "我的作品",
            "我的收藏",
            "Ta的作品",
            "Ta的收藏"
    };

    /*
            activity_type指定还哪个页面
            0~3 分别对应toolbar_titles各个标题
     */
    int activity_type;

    String apis[] = {
            "http://129.204.242.63:8080/listen/mainServlet?action=getOtherRecord",
            "http://129.204.242.63:8080/listen/mainServlet?action=getUserCollect"
    };

    String current_username;
    String query_username;

    private int offset = 0;
    private boolean dont_request = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_record);

        activity_type = getIntent().getIntExtra("activity_type", -1);

        toolbar = findViewById(R.id.record_activity_toolbar);

        toolbar.setTitle(toolbar_titles[activity_type]);

        setSupportActionBar(toolbar);
        // 点击返回箭头退出界面
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        linearLayout = findViewById(R.id.record_activity_empty);

        recyclerView = findViewById(R.id.record_activity_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(RecordActivity.this));
        recordArray = new ArrayList<>();

        if(activity_type == 0){
            recordRecyclerDeleteAdapter = new RecordRecyclerDeleteAdapter(RecordActivity.this, recordArray);
            recyclerView.setAdapter(recordRecyclerDeleteAdapter);
        }else{
            recordRecyclerAdapter = new RecordRecyclerAdapter(RecordActivity.this, recordArray);
            recyclerView.setAdapter(recordRecyclerAdapter);
        }



        /*
                上拉下滑：加载/刷新
         */
        refreshLayout = findViewById(R.id.record_activity_refreshLayout);

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
                        //Toast.makeText(RecordActivity.this, "refresh", Toast.LENGTH_SHORT).show();
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
                        //Toast.makeText(RecordActivity.this, "loadmore", Toast.LENGTH_SHORT).show();
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

        if(activity_type == 0){
            recordRecyclerDeleteAdapter.pausePlayer();
        }else{
            recordRecyclerAdapter.pausePlayer();
        }

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
            recordArray.clear();

            if(activity_type == 0){
                recordRecyclerDeleteAdapter.notifyDataSetChanged();
            }else{
                recordRecyclerAdapter.notifyDataSetChanged();
            }

            if(activity_type == 0){
                recordRecyclerDeleteAdapter.mediaPlayerUtils.reset();
            }else{
                recordRecyclerAdapter.mediaPlayerUtils.reset();
            }

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
                .add(activity_type % 2 == 0 ? "author_username" : "collect_username",
                        query_username)
                .add("offset", String.valueOf(offset))
                .build();
        Request request = new Request.Builder()
                .url(url).post(body)
                .build();
        Call call = client.newCall(request);

        Log.d("request", "current_username:" + current_username + "  "
                    + (activity_type % 2 == 0 ? "author_username" : "collect_username")
                    + query_username + "  " + "offset" + String.valueOf(offset));

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(RecordActivity.this, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                Log.d("response", result);

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                if(jsonObject.get("code").getAsInt() == 1){
                    String data_string = jsonObject.get("data").getAsString();
                    JsonArray data = JsonParser.parseString(data_string).getAsJsonArray();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    });

                    offset += data.size();
                    if(data.size() < 10){
                        dont_request = true;
                    }

                    for(JsonElement record : data){
                        JsonObject single_record = record.getAsJsonObject();
                        String username = single_record.get("username").getAsString();
                        String title = single_record.get("title").getAsString();
                        String description = single_record.get("description").getAsString();
                        String soundFileUrl = single_record.get("soundFileUrl").getAsString();
                        int duration = single_record.get("duration").getAsInt();

                        String create_time = single_record.get("create_time").getAsString();
                        create_time = create_time.substring(0, create_time.length() - 5);


                        int like = single_record.get("like").getAsInt();
                        int collect = single_record.get("collect").getAsInt();
                        int id = single_record.get("id").getAsInt();

                        String headPic = null;
                        if(single_record.get("headPic") != null){
                            headPic = single_record.get("headPic").getAsString();
                        }

                        Record add_record = new Record(id, username, title, description, create_time,
                                duration, headPic, soundFileUrl, like, collect);
                        recordArray.add(add_record);

                        //recordArray.add(0, add_record);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(activity_type == 0){
                                recordRecyclerDeleteAdapter.notifyDataSetChanged();
                            }else{
                                recordRecyclerAdapter.notifyDataSetChanged();
                            }
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
                                recyclerView.setVisibility(View.GONE);
                            }
                        });

                    }
                }
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        if(activity_type == 0){
            recordRecyclerDeleteAdapter.pausePlayer();
        }else{
            recordRecyclerAdapter.pausePlayer();
        }
    }

}
