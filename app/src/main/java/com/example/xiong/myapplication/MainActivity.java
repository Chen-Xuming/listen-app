package com.example.xiong.myapplication;


import android.app.FragmentTransaction;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.icu.text.SymbolTable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.design.widget.TabLayout;
//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.os.Handler;

import com.example.xiong.myapplication.search.SearchActivity;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.AEADBadTagException;

public class MainActivity extends AppCompatActivity {

    public Context context;
    private TabLayout tabLayout;

    private Dynamic dynamicFragment;
    private Hot hotFragment;
    private List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;
        tabLayout=findViewById(R.id.tablayout);
        tabLayout.addTab(tabLayout.newTab().setText("动态"));
        tabLayout.addTab(tabLayout.newTab().setText("热门"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0)
                {
                    if(dynamicFragment==null)
                    {
                        dynamicFragment=new Dynamic();
                        dynamicFragment.setContext(context);
                    }
                    hotFragment=null;
                    addFragment(dynamicFragment);
                    showFragment(dynamicFragment);
                }
                else
                {
                    if(hotFragment==null)
                    {
                        hotFragment=new Hot();
                        hotFragment.setContext(context);
                    }
                    dynamicFragment=null;
                    addFragment(hotFragment);
                    showFragment(hotFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                System.out.println( "B:"+tab.getPosition());
                if(tab.getPosition()==0)
                {
                    dynamicFragment.stopPlay();
                }
                else {
                    hotFragment.stopPlay();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                System.out.println( "C:"+tab.getPosition());
            }
        });

        dynamicFragment=new Dynamic();
        dynamicFragment.setContext(this);

        hotFragment=new Hot();
        hotFragment.setContext(this);

        fragmentList=new ArrayList<>();
        initFragment();


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
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        // 个人中心
        fab_mypage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                floating_menu.close(true);

                /*
                        已登录则跳到个人中心，未登录则跳到登录页面

                 */
                Intent intent = new Intent(MainActivity.this, MyCenterActivity.class);
                startActivity(intent);
            }
        });

        // 发布动态
        fab_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floating_menu.close(true);
                Intent intent = new Intent(MainActivity.this, SendActivity.class);
                startActivity(intent);
            }
        });
        /* ************************************************ */




//        format = new SimpleDateFormat("mm:ss");
//        musCur=findViewById(R.id.CurrentProcess);
//        musLength=findViewById(R.id.SoundLength);
//        mommentsArray1=new ArrayList<>();
//        mommentsArray2=new ArrayList<>();
//        mListView=findViewById(R.id.MomentListDynamic);
//
//        mListView.setOnDeleteListener(new MyListView.OnDeleteListener() {
//            @Override
//            public void onDelete(int index2) {
//
//            }
//        });
//        mediaPlayer1=MediaPlayer.create(this,R.raw.joy_of_love);
//        //mediaPlayer1.setLooping(true);
//        mediaPlayer2=MediaPlayer.create(this,R.raw.liebesleid);
//        //mediaPlayer2.setLooping(true);
//        Moments a=new Moments(getDrawable(R.drawable.pic),"红豆",
//                "清澈的歌声","简介","12分钟前",mediaPlayer1);
//        Moments b=new Moments(getDrawable(R.drawable.pic),"绿豆",
//                "好听的歌声","希望你们会喜欢","20分钟前",mediaPlayer2);
//        mommentsArray1.add(a);
//        mommentsArray1.add(b);
//
//        Context c=this;
//        System.out.println("size:::"+mommentsArray1.size());
//        mListView.setAdapter(new MyAdapter(c,mommentsArray1));
//
//        mommentsArray2.add(a);
//        mommentsArray2.add(b);
//
//        System.out.println("size:::"+mommentsArray2.size());
//        mListView.setAdapter(new MyAdapter(c,mommentsArray2));
    }

    private void initFragment()
    {
        addFragment(dynamicFragment);
        showFragment(dynamicFragment);
    }

    private void addFragment(Fragment fragment)
    {
        if(!fragment.isAdded())
        {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment,fragment).commit();
            fragmentList.add(fragment);
        }
    }

    private void showFragment(Fragment fragment)
    {
        for(Fragment f:fragmentList)
        {
            if(f!=fragment)
            {
                getSupportFragmentManager().beginTransaction().hide(f).commit();

            }
        }
        getSupportFragmentManager().beginTransaction().show(fragment).commit();
    }






//    class  RecordViewHold {
//        ImageView Profile_Photo,Play;
//        TextView user_name, composition_name,Composition_introduction,relerase_time;
//        TextView record_time,sound_length;
//        SeekBar seek_bar;
//    }

//    class MyAdapter extends BaseAdapter {
//
//        public int thisPosition = -1;
//        public MediaPlayer mMediaPlayer = new MediaPlayer();
//        public boolean isPlay = true;
//        public boolean isFiring = false;
//        public int playintNo;
//        private Context context;
//        private LayoutInflater inflater;
//        private ArrayList<Moments> Mdata ;
//
//        //添加数据
//        public MyAdapter(Context context, ArrayList<Moments> mdata){
//            super();
//            this.context=context;
//            Mdata=mdata;
//            inflater=LayoutInflater.from(context);
//            playintNo=-1;
//        }
//
//        @Override
//        public int getCount() { return Mdata==null?0: Mdata.size(); }
//
//        @Override
//        public Object getItem(int position) {return Mdata.get(position);}
//
//        @Override
//        public long getItemId(int position) { return position;}
//
//        private Timer timer;
//
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//
//            final RecordViewHold  holder;
//            final int sign=position;
//
//            if(convertView==null)
//            {
//                convertView=inflater.inflate(R.layout.cardview,null);
//                holder=new RecordViewHold();
//                holder.Profile_Photo=convertView.findViewById(R.id.ProfilePhoto);
//                holder.Composition_introduction=convertView.findViewById(R.id.introduction);
//                holder.composition_name=convertView.findViewById(R.id.CompositionName);
//                holder.relerase_time=convertView.findViewById(R.id.ReleaseTime);
//                holder.seek_bar=convertView.findViewById(R.id.Process);
//                holder.user_name=convertView.findViewById(R.id.username);
//                holder.Play=convertView.findViewById(R.id.play);
//                holder.Play.setTag(sign);
//                holder.record_time=convertView.findViewById(R.id.CurrentProcess);
//                holder.sound_length=convertView.findViewById(R.id.SoundLength);
//                convertView.setTag(holder);
//            }
//            else holder=(RecordViewHold) convertView.getTag();
//
//
//            //获取数据
//            Moments momment=Mdata.get(position);
//
//            //设置对应的item位置
//            Mdata.get(position).playOrPause=holder.Play;
//            if(Mdata.get(position).playOrPause==holder.Play)
//                System.out.println("equal");
//
//            /*修改头像*/
//            holder.Profile_Photo.setImageDrawable(momment.getProfile_Photo());
//            holder.user_name.setText(momment.getuser_name());
//            System.out.println(momment.getuser_name());
//            holder.composition_name.setText(momment.getcomposition_name());
//            holder.Composition_introduction.setText(momment.getcomposition_introduction());
//            holder.relerase_time.setText(momment.getrelerase_time());
//            holder.seek_bar.setOnSeekBarChangeListener(new MySeekBar(momment.getmediaPlayer()));
//
//            holder.seek_bar.setMax(Mdata.get(sign).mediaPlayer.getDuration());
//
//            final SimpleDateFormat format = new SimpleDateFormat("mm:ss");
//            final Date date = new Date(Mdata.get(sign).mediaPlayer.getDuration());
//            String time = format.format(date);
//            holder.sound_length.setText(time);
//
//
//            holder.Play.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v)
//                {
//                    System.out.println("id::"+v.getId());
//                    final int tag=((Integer) v.getTag()).intValue();
//                    Log.i("jjf", "tag的值" + tag);
//                    Log.i("jjf", "sign的值" + sign);
//                    //根据点击的item中的值判断是否是同一个按钮
//                    if (playintNo!=-1)//有播放
//                    {
//                        if (playintNo != tag)//播放的音乐与点击的按钮冲突
//                        {
//                            System.out.println("22222 ");
//                            Mdata.get(playintNo).mediaPlayer.pause();
//                            Mdata.get(playintNo).playOrPause.setImageDrawable(getDrawable(R.drawable.play));
//
//                            //holder.Play.setImageDrawable(getDrawable(R.drawable.play));
//                            isPlay = true;
//                            isFiring = false;
//                            if (timer != null) {
//                                timer.cancel();
//                                timer = null;
//                            }
//                        }
//                        else{
//                            System.out.println("11111 ");
//                        }
//                    }
//
//                    if(isPlay)
//                    {
//                        System.out.println("33333 ");
//                        playintNo=tag;
//                        //播放音乐
//                        isFiring=true;
//                        Mdata.get(playintNo).mediaPlayer.start();
//                        holder.Play.setImageDrawable(getDrawable(R.drawable.pause));
//                        timer=new Timer();
//                        final Handler handler = new Handler();
//                        timer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    handler.post(new Runnable() {
//                                        @Override
//                                        public void run()
//                                        {
//                                            if (isFiring)
//                                            {
//                                                //seek_bar 运动
//                                                holder.seek_bar.setProgress(Mdata.get(tag).mediaPlayer.getCurrentPosition());
//                                                //播放时长更新
//                                                holder.record_time.setText(format.format(Mdata.get(tag).mediaPlayer.getCurrentPosition()));
//                                            }
//                                        }
//                                    });
//                                }
//                                }, 100, 100);
//                        Mdata.get(tag).mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                            @Override
//                            public void onCompletion(MediaPlayer mp) {
//                                System.out.println("55555 ");
//                                holder.Play.setBackgroundResource(R.drawable.play);
//                                isPlay = true;
//                                if (timer != null) {
//                                    timer.cancel();
//                                    timer = null;
//                                }
//                            }
//                        });
//
//                        holder.Play.setBackgroundResource(R.drawable.play);
//                        isPlay = false;
//                    }
//                    else {
//                        System.out.println("44444 ");
//                        Mdata.get(tag).mediaPlayer.pause();
//                        //Mdata.get(tag).changePicToPlayOrPause(getDrawable(R.drawable.play));
//
//                        holder.Play.setImageDrawable(getDrawable(R.drawable.play));
//                        //holder.Play.setBackgroundResource(R.drawable.play);
//                        isPlay = true;
//                        isFiring = false;
//                        if (timer != null) {
//                            timer.cancel();
//                            timer = null;
//                        }
//                    }
//                    //按钮点击发生变化重绘item
//                    // notifyDataSetChanged();
//                }
//            });
//
//            holder.seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                @Override
//                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    if (fromUser == true) {
//                         holder.seek_bar.setProgress(progress);
//                    }
//                }
//                @Override
//                public void onStartTrackingTouch(SeekBar seekBar) { }
//
//                @Override
//                public void onStopTrackingTouch(SeekBar seekBar) { }
//            });
//            return convertView;
//        }
//    }


//    public class Moments {
//
//        private Drawable Profile_Photo;
//        private String user_name, composition_name,Composition_introduction,relerase_time;
//        private MediaPlayer mediaPlayer;
//        public ImageView playOrPause;
//        public Moments(Drawable Profile_Photo, String user_name, String composition_name,
//                 String Composition_introduction,String relerase_time,MediaPlayer mediaPlayer)
//        {
//            System.out.println("Moments");
//            this.Profile_Photo=Profile_Photo;
//            this.user_name=user_name;
//            this.composition_name=composition_name;
//            this.Composition_introduction=Composition_introduction;
//            this.relerase_time=relerase_time;
//            this.mediaPlayer=mediaPlayer;
//            this.playOrPause=null;
//        }
//
//        public Drawable getProfile_Photo()
//        {
//            return this.Profile_Photo;
//        }
//        public String getuser_name()
//        {
//            return this.user_name;
//        }
//        public String getcomposition_name()
//        {
//            return this.composition_name;
//        }
//        public String getcomposition_introduction()
//        {
//            return this.Composition_introduction;
//        }
//        public String getrelerase_time()
//        {
//            return this.relerase_time;
//        }
//        public MediaPlayer getmediaPlayer()
//        {
//            return this.mediaPlayer;
//        }
////        public void changePicToPlayOrPause(Drawable d){this.playOrPause.setImageDrawable(d);}
////        public void setPlayOrPause(ImageView iv){this.playOrPause=iv;}
//
//
//    }

//    /*进度条处理*/
//    public class MySeekBar implements SeekBar.OnSeekBarChangeListener {
//
//        private MediaPlayer mediaPlayer;
//        public void onProgressChanged(SeekBar seekBar, int progress,
//                                      boolean fromUser) {
//        }
//        public MySeekBar(MediaPlayer mediaPlayer)
//        {
//            this.mediaPlayer=mediaPlayer;
//        }
//
//        /*滚动时,应当暂停后台定时器*/
//        public void onStartTrackingTouch(SeekBar seekBar) {
//            isSeekBarChanging = true;
//        }
//
//        /*滑动结束后，重新设置值*/
//        public void onStopTrackingTouch(SeekBar seekBar) {
//            isSeekBarChanging = false;
//            mediaPlayer.seekTo(seekBar.getProgress());
//        }
//    }




}
