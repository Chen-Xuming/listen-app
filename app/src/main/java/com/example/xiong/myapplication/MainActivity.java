package com.example.xiong.myapplication;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.TabLayout;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.xiong.myapplication.record.Record;
import com.example.xiong.myapplication.record.RecordFragment;
import com.example.xiong.myapplication.search.MySearchActivity;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public Context context;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ArrayList<String> tab_title_list = new ArrayList<>();//存放标签页标题
    private ArrayList<Fragment> fragment_list = new ArrayList<>();//存放ViewPager下的Fragment
    private RecordFragment fragment1, fragment2;
    private MyFragmentPagerAdapter adapter;//适配器


    private Dynamic dynamicFragment;
    private Hot hotFragment;
    private List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        /*
                tab + viewPager
         */
        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.main_viewpager);
        tab_title_list.add("推荐");
        tab_title_list.add("热门");
        tabLayout.addTab(tabLayout.newTab().setText(tab_title_list.get(0)));
        tabLayout.addTab(tabLayout.newTab().setText(tab_title_list.get(1)));
        fragment1 = new RecordFragment();
        ((RecordFragment) fragment1).setContext(this);
        ((RecordFragment) fragment1).setPageType(1);
        fragment2 = new RecordFragment();
        ((RecordFragment) fragment2).setContext(this);
        ((RecordFragment) fragment2).setPageType(2);
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


        // 读取本地账号
        UserManager.init(this);


        /*******************************************
         Floating Button
         *********************************************/
        final FloatingActionMenu floating_menu = findViewById(R.id.floating_menu);
        FloatingActionButton fab_search = findViewById(R.id.fab_search);
        FloatingActionButton fab_mypage = findViewById(R.id.fab_my_page);
        FloatingActionButton fab_create = findViewById(R.id.fab_create);

        // 搜索界面
        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_menu.close(true);
                Intent intent = new Intent(MainActivity.this, MySearchActivity.class);
                startActivity(intent);
            }
        });

        // 个人中心
        fab_mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_menu.close(true);

                /*
                        已登录则跳到个人中心，未登录则跳到登录页面
                 */

                if (UserManager.getCurrentUser() != null) {
                    Intent intent = new Intent(MainActivity.this, MyCenterActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, login.class);
                    startActivity(intent);
                }
            }
        });

        // 发布动态
        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_menu.close(true);

                /*
                        已登录则跳到发布页，未登录则跳到登录页面
                 */
                if (UserManager.getCurrentUser() != null) {
                    Intent intent = new Intent(MainActivity.this, SendActivity.class);
                    startActivityForResult(intent, 100);
                    //startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, login.class);
                    startActivity(intent);
                }
            }
        });
        /* ************************************************ */




    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {

        if(requestCode == 100) {
            if (resultCode == 1024) {
                String username = data.getStringExtra("username");
                String title = data.getStringExtra("title");
                String description = data.getStringExtra("description");
                String sound_url = data.getStringExtra("soundFileUrl");
                int duration = data.getIntExtra("duration", 0);
                String create_time = data.getStringExtra("create_time");
                int id = data.getIntExtra("id", -1);
                String headpic = data.getStringExtra("headpic");

                Record record = new Record(id, username, title, description, create_time, duration,
                        headpic, sound_url, 0, 0);

                Log.d("intent_get", "\nusername:" + username + "\ntitle:" + title
                        + "\ndescription:" + description + "\nsoundFileUrl:" + sound_url
                        + "\nduration:" + duration + "\ncreate_time:" + create_time);

                fragment1.recordArray.add(0, record);
                fragment1.recordRecyclerAdapter.notifyItemInserted(0);
                fragment1.recordRecyclerAdapter.notifyItemRangeChanged(0, fragment1.recordArray.size());
                fragment1.recyclerView.scrollToPosition(0);

                Toast.makeText(MainActivity.this, "AAAAAA", Toast.LENGTH_SHORT).show();
            }
        }

        //super.onActivityResult(requestCode, resultCode, data);
    }






}
