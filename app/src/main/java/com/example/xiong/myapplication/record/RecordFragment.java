package com.example.xiong.myapplication.record;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.xiong.myapplication.Moments;
import com.example.xiong.myapplication.MyAdapter;
import com.example.xiong.myapplication.MyListView;
import com.example.xiong.myapplication.R;
import com.example.xiong.myapplication.UserManager;
import com.example.xiong.myapplication.login;
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



public class RecordFragment extends Fragment {
    public ArrayList<Record> recordArray;
    private Context context;
    public RecyclerView recyclerView;
    public RecordRecyclerAdapter recordRecyclerAdapter;

    private TwinklingRefreshLayout refreshLayout;
    private ProgressLayout progressLayout;          // header
    private BallPulseView ballPulseView;            // footer


    private LinearLayout linearLayout;

    // 首次进入时加载数据
    private boolean mIsDataInited;

    private int fragment_type = 0;  // 1: 推荐  2：热门   3： 搜索

    // 各个页面对应的api
    String apis[] = {
            "http://129.204.242.63:8080/listen/mainServlet?action=getDongTai",
            "http://129.204.242.63:8080/listen/mainServlet?action=getHot",
            "http://129.204.242.63:8080/listen/mainServlet?action=searchRecord"
    };

    private int offset = 0;
    private boolean dont_request = false;

    public void setContext(Context context) {
        this.context = context;
    }
    public void setPageType(int pagetype){
        fragment_type = pagetype;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        //Toast.makeText(context, "in fragment", Toast.LENGTH_SHORT).show();

        View view =inflater.inflate(R.layout.listview_record,null);
        recyclerView=view.findViewById(R.id.sound_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        recordArray=new ArrayList<>();

        recordRecyclerAdapter = new RecordRecyclerAdapter(context, recordArray);
        recyclerView.setAdapter(recordRecyclerAdapter);

        linearLayout = view.findViewById(R.id.record_fragment_empty);

        /*
                上拉下滑：加载/刷新
         */
        refreshLayout = (TwinklingRefreshLayout) view.findViewById(R.id.search_refreshLayout);

        progressLayout = new ProgressLayout(context);
        refreshLayout.setHeaderView(progressLayout);

        ballPulseView = new BallPulseView(context);
        refreshLayout.setBottomView(ballPulseView);

        refreshLayout.setOnRefreshListener(new RefreshListenerAdapter(){
            @Override
            public void onRefresh(final TwinklingRefreshLayout refreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(context, "refresh", Toast.LENGTH_SHORT).show();
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
                        //Toast.makeText(context, "loadmore", Toast.LENGTH_SHORT).show();
                        loadMoreData();
                        refreshLayout.finishLoadmore();
                    }
                },1000);
            }
        });

        if (!mIsDataInited) {
            if (getUserVisibleHint()) {
                if(fragment_type == 1 || fragment_type == 2){
                    refreshLayout.startRefresh();
                }
                mIsDataInited = true;
            }
        }


        //refreshLayout.startRefresh();

        return view;
    }

    /*
            下拉刷新
     */
    private void refreshData(){
        recordRecyclerAdapter.pausePlayer();
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

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recordRecyclerAdapter.notifyDataSetChanged();
                }
            });

            recordRecyclerAdapter.mediaPlayerUtils.reset();
            offset = 0;
            dont_request = false;
        }


        if(!dont_request){
            switch (fragment_type){
                case 1:
                    requestDataForRecommend();
                    break;
                case 2:
                    requestDataForHot();
                    break;
                case 3:
                    if(keyword != null)
                        requestDataForSearch();
                    break;
            }
        }
    }


    /*
            “推荐”页的请求
     */
    private void requestDataForRecommend(){

        String current_username = UserManager.getCurrentUser() == null ?
                "" : UserManager.getCurrentUser().getUsername();


        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("current_username", current_username).build();
        Request request = new Request.Builder()
                .url(apis[0]).post(body)
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
                    String data_string = jsonObject.get("data").getAsString();
                    JsonArray data = JsonParser.parseString(data_string).getAsJsonArray();

                    for(JsonElement record : data){
                        JsonObject single_record = record.getAsJsonObject();
                        int id = single_record.get("id").getAsInt();
                        String username = single_record.get("username").getAsString();
                        String title = single_record.get("title").getAsString();
                        String description = single_record.get("description").getAsString();
                        String soundFileUrl = single_record.get("soundFileUrl").getAsString();
                        int duration = single_record.get("duration").getAsInt();


                        String create_time = single_record.get("create_time").getAsString();
                        create_time = create_time.substring(0, create_time.length() - 5);

                        int like = single_record.get("like").getAsInt();
                        int collect = single_record.get("collect").getAsInt();

                        String headPic = null;
                        if(single_record.get("headPic") != null){
                            headPic = single_record.get("headPic").getAsString();
                        }

                        Record add_record = new Record(id, username, title, description, create_time,
                                duration, headPic, soundFileUrl, like, collect);
                        recordArray.add(add_record);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recordRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                }
                else {
                    dont_request = true;
                }

            }
        });
    }


    /*
            “热门”页的请求
     */
    private void requestDataForHot(){
        String current_username = UserManager.getCurrentUser() == null ?
                "" : UserManager.getCurrentUser().getUsername();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("current_username", current_username)
                .add("offset", String.valueOf(offset))
                .build();
        Request request = new Request.Builder()
                .url(apis[1]).post(body)
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
                    String data_string = jsonObject.get("data").getAsString();
                    JsonArray data = JsonParser.parseString(data_string).getAsJsonArray();

                    offset += data.size();
                    if(data.size() < 10){
                        dont_request = true;
                    }

                    for(JsonElement record : data){
                        JsonObject single_record = record.getAsJsonObject();
                        int id = single_record.get("id").getAsInt();
                        String username = single_record.get("username").getAsString();
                        String title = single_record.get("title").getAsString();
                        String description = single_record.get("description").getAsString();
                        String soundFileUrl = single_record.get("soundFileUrl").getAsString();

                        int duration = single_record.get("duration").getAsInt();


                        String create_time = single_record.get("create_time").getAsString();
                        create_time = create_time.substring(0, create_time.length() - 5);

                        int like = single_record.get("like").getAsInt();
                        int collect = single_record.get("collect").getAsInt();

                        String headPic = null;
                        if(single_record.get("headPic") != null){
                            headPic = single_record.get("headPic").getAsString();
                        }

                        Record add_record = new Record(id, username, title, description, create_time,
                                duration, headPic, soundFileUrl, like, collect);
                        recordArray.add(add_record);
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recordRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                }else{
                    dont_request = true;
                }

            }
        });
    }


    /*
            “搜索”页的请求
     */

    private String keyword = null;

    public void search(String keyword){
        this.keyword = keyword;
        offset = 0;
        dont_request = false;
        recordArray.clear();
        recordRecyclerAdapter.mediaPlayerUtils.reset();
        requestDataForSearch();
    }

    private void requestDataForSearch(){
        String current_username = UserManager.getCurrentUser() == null ?
                "" : UserManager.getCurrentUser().getUsername();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("current_username", current_username)
                .add("offset", String.valueOf(offset))
                .add("keyword", keyword)
                .build();
        Request request = new Request.Builder()
                .url(apis[2]).post(body)
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
                    String data_string = jsonObject.get("data").getAsString();
                    JsonArray data = JsonParser.parseString(data_string).getAsJsonArray();

                    if(linearLayout.getVisibility() == View.VISIBLE){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linearLayout.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                    }


                    offset += data.size();
                    if(data.size() < 10){
                        dont_request = true;
                    }

                    for(JsonElement record : data){
                        JsonObject single_record = record.getAsJsonObject();
                        int id = single_record.get("id").getAsInt();
                        String username = single_record.get("username").getAsString();
                        String title = single_record.get("title").getAsString();
                        String description = single_record.get("description").getAsString();
                        String soundFileUrl = single_record.get("soundFileUrl").getAsString();
                        int duration = single_record.get("duration").getAsInt();

                        String create_time = single_record.get("create_time").getAsString();
                        create_time = create_time.substring(0, create_time.length() - 5);

                        int like = single_record.get("like").getAsInt();
                        int collect = single_record.get("collect").getAsInt();

                        String headPic = null;
                        if(single_record.get("headPic") != null){
                            headPic = single_record.get("headPic").getAsString();
                        }

                        Record add_record = new Record(id, username, title, description, create_time,
                                duration, headPic, soundFileUrl, like, collect);
                        recordArray.add(add_record);
                    }


                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recordRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                }else{
                    dont_request = true;

                    if(offset == 0){

                        getActivity().runOnUiThread(new Runnable() {
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isVisible() && !mIsDataInited) {
            refreshLayout.startRefresh();
            mIsDataInited = true;
        } else {
            if(recordRecyclerAdapter != null){
                recordRecyclerAdapter.pausePlayer();
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        recordRecyclerAdapter.pausePlayer();

    }

}
