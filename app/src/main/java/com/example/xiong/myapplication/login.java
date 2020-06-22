package com.example.xiong.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class login extends AppCompatActivity implements View.OnClickListener {
    private EditText loginName;
    private EditText password;
    private Button login;
    private TextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginName=findViewById(R.id.login_name);
        password=findViewById(R.id.psw);
        login = findViewById(R.id.login);
        login.setOnClickListener(this);
        register = findViewById(R.id.go_register);
        register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login:
                String name = loginName.getText().toString();
                String pass = password.getText().toString();
                Intent intent = new Intent(login.this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.go_register:
                Intent intent1 = new Intent(login.this,register.class);
                startActivity(intent1);
                break;
            default:
                break;
        }


    }
}