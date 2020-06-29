package com.example.xiong.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toolbar;

//import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class Followed extends AppCompatActivity {
    private List<Account> accounts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followed);
        initAccount();
        AccountAdapter adapter = new AccountAdapter(Followed.this,R.layout.account_item,accounts);
        ListView list = findViewById(R.id.account_list);
        list.setAdapter(adapter);
    }
    private void initAccount(){
        Account a1 = new Account("it's user1",R.drawable.fox,true);
        accounts.add(a1);
        Account a2 = new Account("it's user2",R.drawable.fox,false);
        accounts.add(a2);
        Account a3 = new Account("it's user1",R.drawable.fox,true);
        accounts.add(a3);
        Account a4 = new Account("it's user2",R.drawable.fox,false);
        accounts.add(a4);
        accounts.add(a4);
        accounts.add(a4);
    }
}