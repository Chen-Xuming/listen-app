package com.example.xiong.myapplication.search;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.xiong.myapplication.MyFragmentPagerAdapter;
import com.example.xiong.myapplication.R;
import com.example.xiong.myapplication.account.AccountFragment;
import com.example.xiong.myapplication.record.RecordFragment;

import java.util.ArrayList;
import java.util.List;

public class MySearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private SearchView.SearchAutoComplete searchViewOfKnowledge;        // 历史记录提示

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<String> tab_title_list = new ArrayList<>();//存放标签页标题
    private ArrayList<Fragment> fragment_list = new ArrayList<>();//存放ViewPager下的Fragment

    private RecordFragment fragment1;
    private AccountFragment fragment2;

    private MyFragmentPagerAdapter adapter;//适配器

    //数据库
    private SearchHistoryDB searchHistoryDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);



        setContentView(R.layout.activity_my_search);


        Toolbar toolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);

        // 点击返回箭头退出界面
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //实例化数据库
        searchHistoryDB = new SearchHistoryDB(MySearchActivity.this, SearchHistoryDB.DB_NAME, null, 1);




        /**********************************************************
                tab + viewPager
         ***********************************************************/
        tabLayout = findViewById(R.id.search_tablayout);
        viewPager = findViewById(R.id.search_viewpager);
        tab_title_list.add("声音");
        tab_title_list.add("用户");
        tabLayout.addTab(tabLayout.newTab().setText(tab_title_list.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(tab_title_list.get(1)));
        fragment1 = new RecordFragment();
        ((RecordFragment) fragment1).setContext(this);
        ((RecordFragment) fragment1).setPageType(3);
        fragment2 = new AccountFragment();
        ((AccountFragment) fragment2).setContext(this);
        fragment_list.add(fragment1);
        fragment_list.add(fragment2);
        adapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), tab_title_list, fragment_list);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        //tabLayout.setTabsFromPagerAdapter(adapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //选中了tab的逻辑
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //未选中tab的逻辑
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //再次选中tab的逻辑
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_view, menu);

        //找到searchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        initSearchView(searchItem);


        return super.onCreateOptionsMenu(menu);
    }

    private void initSearchView(final MenuItem item) {

        searchView = (SearchView) item.getActionView();

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

        // 焦点
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();

        searchView.setSubmitButtonEnabled(false);   //不显示提交按钮

        /*
                事件
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {

                if(newText.length() == 0) return false;

                Log.d("DB", "submit");
                searchHistoryDB.insertHistory(newText);

                /*
                        网络请求部分
                 */

                int tab_no = tabLayout.getSelectedTabPosition();
                if(tab_no == 0){
                    fragment1.search(newText);
                }else if(tab_no == 1){
                    fragment2.search(newText);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if(newText==null||newText.length()==0) {
                    initSearchView(item);
                }
                return false;
            }
        });

        searchViewOfKnowledge = searchView.findViewById(R.id.search_src_text);

        searchViewOfKnowledge.showDropDown();

        // 取出历史数据
        final List<String> arr = searchHistoryDB.queryAllHistory();
        searchViewOfKnowledge.setThreshold(0);

        HistoryAdapter adapter = new HistoryAdapter(MySearchActivity.this, R.layout.item_search_history, arr, searchView);

        searchViewOfKnowledge.setAdapter(adapter);

        searchViewOfKnowledge.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = view.findViewById(R.id.tv_item_search_history);
                //searchView.setQuery(arr.get(position), true);
                searchView.setQuery(tv.getText().toString(), true);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });


    }
}
