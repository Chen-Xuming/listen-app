package com.example.xiong.myapplication.search;

/*
*       用于将Item参数传给Activity
* */
public interface IOnItemClickListener {

    void onItemClick(String keyword);

    void onItemDeleteClick(String keyword);

}