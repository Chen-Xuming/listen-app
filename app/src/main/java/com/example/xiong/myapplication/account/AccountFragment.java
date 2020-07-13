package com.example.xiong.myapplication.account;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.xiong.myapplication.R;
import com.example.xiong.myapplication.UserManager;
import com.example.xiong.myapplication.record.Record;
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

/*
    AccountFragment 只用于搜索界面的用户列表
 */

public class AccountFragment extends Fragment {

    public ArrayList<Account> accountArray;
    private Context context;
    private ListView listView;
    private AccountAdapter accountAdapter;

    private TwinklingRefreshLayout refreshLayout;
    private ProgressLayout progressLayout;
    private BallPulseView ballPulseView;

    private LinearLayout linearLayout;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {

        View view =inflater.inflate(R.layout.listview_account,null);

        listView = view.findViewById(R.id.account_list);

        accountArray = new ArrayList<>();

        accountAdapter = new AccountAdapter(context, R.layout.account_item, accountArray);
        listView.setAdapter(accountAdapter);


        linearLayout = view.findViewById(R.id.account_fragment_empty);

        /*
                上拉下滑：加载/刷新
         */
        refreshLayout = (TwinklingRefreshLayout) view.findViewById(R.id.account_refreshLayout);

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

                        loadMoreData();
                        refreshLayout.finishLoadmore();
                    }
                },1000);
            }
        });

        return view;
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
            if(keyword != null){
                requestDataForSearch();
            }
        }
    }


    /*
            “搜索”页的请求
     */

    private String keyword = null;
    private int offset = 0;
    private boolean dont_request = false;


    public void search(String keyword){
        this.keyword = keyword;
        offset = 0;
        dont_request = false;
        accountArray.clear();
        requestDataForSearch();
    }

    private void requestDataForSearch(){

        String url = "http://129.204.242.63:8080/listen/mainServlet?action=searchUser";

        String current_username = UserManager.getCurrentUser() == null ?
                "" : UserManager.getCurrentUser().getUsername();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("current_username", current_username)
                .add("target_username", keyword)
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
                Toast.makeText(context, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                /*
                        找到用户
                 */
                if(jsonObject.get("code").getAsInt() == 1){
                    String data_string = jsonObject.get("data").getAsString();
                    JsonArray data = JsonParser.parseString(data_string).getAsJsonArray();

                    if(linearLayout.getVisibility() == View.VISIBLE){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linearLayout.setVisibility(View.GONE);
                                listView.setVisibility(View.VISIBLE);
                            }
                        });
                    }

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


                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            accountAdapter.notifyDataSetChanged();
                        }
                    });
                }
                /*
                        没找到相关用户
                 */
                else{
                    dont_request = true;

                    if(offset == 0){

                        getActivity().runOnUiThread(new Runnable() {
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
