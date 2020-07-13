package com.example.xiong.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {

    private String username;
    private String password;
    private String headPic_url;

    private static UserManager userManager;

    private UserManager(String name, String pw, String head){
        username = name;
        password = pw;
        headPic_url = head;
    }

    /*
            查看本地是否有账号密码
     */
    public static void init(Context context){
        SharedPreferences sp = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String name=sp.getString("username", null);

        if(name == null){
            return;
        }
        else{
            String password=sp.getString("password", null);
            String headPic = sp.getString("headpic", null);
            userManager = new UserManager(name, password, headPic);
        }

    }

    public static UserManager getCurrentUser(){
        /*
                -null: 未登录
                -not null： 已登录
         */
        return userManager;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHeadPic_url() {
        return headPic_url;
    }



    /*
            登录
            仅在未登录时调用
     */
    public static void saveAccount(Context context, String name, String pw, String headPic_url){

        SharedPreferences sharedPre = context.getSharedPreferences("user", context.MODE_PRIVATE);
        //获取Editor对象
        SharedPreferences.Editor editor=sharedPre.edit();
        //设置参数
        editor.putString("username", name);
        editor.putString("password", pw);
        editor.putString("headpic", headPic_url);
        //提交
        editor.apply();

        userManager = new UserManager(name, pw, headPic_url);

    }

    /*
            退出， 仅在已登录时调用
     */
    public void logout(Context context){
        username = null;
        password = null;
        headPic_url = null;

        userManager = null;

        SharedPreferences sp = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }
}
