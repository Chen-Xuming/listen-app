package com.example.xiong.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class register extends AppCompatActivity implements View.OnClickListener {
    private EditText account;
    private EditText passOne;
    private EditText passTow;
    private TextView warn;
    public Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        account = findViewById(R.id.login_name1);
        passOne = findViewById(R.id.psw1);
        passTow = findViewById(R.id.psw2);
        register = findViewById(R.id.register);
        warn = findViewById(R.id.register_warn);
        register.setOnClickListener(this);
        passOne.setOnClickListener(this);
        passTow.setOnClickListener(this);
        passTow.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    passOne.setBackground(getDrawable(R.drawable.register_input));
                    passTow.setBackground(getDrawable(R.drawable.register_input));
                    warn.setVisibility(View.INVISIBLE);
                }
            }
        });
        passOne.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    passTow.setBackground(getDrawable(R.drawable.register_input));
                    passOne.setBackground(getDrawable(R.drawable.register_input));
                    warn.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register:
                String p1 = passOne.getText().toString();
                String p2 = passTow.getText().toString();
                String aName = account.getText().toString();
                if(p1.equals("")||p2.equals("")||aName.equals("")){
                    Toast toast = Toast.makeText(register.this,null,Toast.LENGTH_SHORT);
                    toast.setText("请输入完整信息！");
                    toast.show();
                }
                else if(p1.equals(p2)){
                    Toast toast=Toast.makeText(register.this,null,Toast.LENGTH_SHORT);
                    toast.setText("注册成功");
                    toast.show();
                    Intent intent = new Intent(register.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //密码不一致
                    Drawable a = getDrawable(R.drawable.red_radius_input);
                    passOne.setBackground(a);
                    passTow.setBackground(a);
                    warn.setVisibility(View.VISIBLE);
                }
                break;
            default:
                break;
        }
    }
}