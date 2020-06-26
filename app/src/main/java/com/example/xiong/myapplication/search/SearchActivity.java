package com.example.xiong.myapplication.search;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiong.myapplication.R;

import java.util.ArrayList;


public class SearchActivity extends AppCompatActivity implements IOnItemClickListener, View.OnClickListener {

    private RecyclerView rvSearchHistory;
    private TextView tvSearchClean;
    private SearchView searchView;

    //历史搜索记录
    private ArrayList<String> allHistorys = new ArrayList<>();
    private ArrayList<String> historys = new ArrayList<>();
    //适配器
    private SearchHistoryAdapter searchHistoryAdapter;
    //数据库
    private SearchHistoryDB searchHistoryDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);

        // 点击返回箭头退出界面
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rvSearchHistory = findViewById(R.id.search_music_history);
        tvSearchClean = findViewById(R.id.tv_search_clean);

        rvSearchHistory.setVisibility(View.GONE);
        tvSearchClean.setVisibility(View.GONE);


        //实例化数据库
        searchHistoryDB = new SearchHistoryDB(SearchActivity.this, SearchHistoryDB.DB_NAME, null, 1);

        allHistorys = searchHistoryDB.queryAllHistory();
        setAllHistorys();
        //初始化recyclerView
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(SearchActivity.this));//list类型
        searchHistoryAdapter = new SearchHistoryAdapter(SearchActivity.this, historys);
        rvSearchHistory.setAdapter(searchHistoryAdapter);

        //设置删除单个记录的监听
        searchHistoryAdapter.setOnItemClickListener(this);

        // 删除全部
        tvSearchClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchHistoryDB.deleteAllHistory();
                historys.clear();
                allHistorys.clear();
                //searchUnderline.setVisibility(View.GONE);
                searchHistoryAdapter.notifyDataSetChanged();

                tvSearchClean.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_view, menu);

        //找到searchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setIconifiedByDefault(false);    //默认为true在框内，设置false则在框外

        // 去掉放大镜图标
        ImageView searchIcon = (ImageView)searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        searchIcon.setImageDrawable(null);

        searchView.setQueryHint("搜索声音/用户");     //设置默认无内容时的文字提示

        searchView.setInputType(InputType.TYPE_CLASS_TEXT);

        // 设置文字颜色
        SearchView.SearchAutoComplete mSearchAutoComplete =
                (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setHintTextColor(getResources().getColor(android.R.color.white));//设置提示文字颜色
        mSearchAutoComplete.setTextColor(getResources().getColor(android.R.color.white));//设置内容文字颜色


        searchView.setSubmitButtonEnabled(false);   //不显示提交按钮

        // 焦点
        searchView.setFocusable(true);
        searchView.setFocusableInTouchMode(true);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /*
                    提交按钮的点击事件
                 */

                Toast.makeText(SearchActivity.this, query, Toast.LENGTH_SHORT).show();

                searchView.clearFocus();


                // 对query进行搜索

                //插入到数据库
                if(searchHistoryDB.insertHistory(query)){
                    allHistorys.add(0, query);
                    if(allHistorys.size() >= 6){
                        allHistorys.remove(5);
                    }
                    //searchHistoryAdapter.notifyDataSetChanged();
                }


                //...（网络部分）

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //当输入框内容改变的时候回调
                if (TextUtils.isEmpty(newText.trim())) {
                    setAllHistorys();
                    searchHistoryAdapter.notifyDataSetChanged();
                } else {
                    setKeyWordHistorys(newText);
                }
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    //Toast.makeText(SearchActivity.this, "有焦点", Toast.LENGTH_SHORT).show();
                    rvSearchHistory.setVisibility(View.VISIBLE);
                    if(allHistorys.size() > 0) {
                        tvSearchClean.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    //Toast.makeText(SearchActivity.this, "没有焦点", Toast.LENGTH_SHORT).show();
                    rvSearchHistory.setVisibility(View.GONE);
                    tvSearchClean.setVisibility(View.GONE);
                }
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 点击单个搜索记录
     */
    @Override
    public void onItemClick(String keyword) {
        // 对keyword进行搜索

        Toast.makeText(SearchActivity.this, keyword, Toast.LENGTH_SHORT).show();

        searchView.setQuery(keyword, false);
        searchView.clearFocus();

        //...（网络部分）
    }

    /**
     * 删除单个搜索记录
     */
    @Override
    public void onItemDeleteClick(String keyword) {
        searchHistoryDB.deleteHistory(keyword);
        historys.remove(keyword);
        checkHistorySize();
        searchHistoryAdapter.notifyDataSetChanged();
    }

    private void checkHistorySize() {
        if (historys.size() < 1) {
            //searchUnderline.setVisibility(View.GONE);
            tvSearchClean.setVisibility(View.GONE);
        } else {
            //searchUnderline.setVisibility(View.VISIBLE);
            tvSearchClean.setVisibility(View.VISIBLE);
        }
    }

    private void setAllHistorys() {
        historys.clear();
        historys.addAll(allHistorys);
        //checkHistorySize();
    }

    private void setKeyWordHistorys(String keyword) {
        historys.clear();
        for (String string : allHistorys) {
            if (string.contains(keyword)) {
                historys.add(string);
            }
        }
        searchHistoryAdapter.notifyDataSetChanged();
        checkHistorySize();
    }

    @Override
    public void onClick(View view){

    }
}
