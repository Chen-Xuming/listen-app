package com.example.xiong.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
                    registerRequest(aName, p1);
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

    /*
            注册
     */
    private void registerRequest(final String username, final String password){
        String url = "http://129.204.242.63:8080/listen/registServlet?action=regist";

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        final Request request = new Request.Builder()
                .url(url).post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(register.this, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
                Looper.loop();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();

                final JsonObject jsonObject  = JsonParser.parseString(result).getAsJsonObject();

                int code = jsonObject.get("code").getAsInt();

                switch (code){
                    case 0:
                        Looper.prepare();
                        Toast.makeText(register.this, "服务器发生错误，请重试。", Toast.LENGTH_LONG).show();
                        Looper.loop();
                        break;
                    case 2:
                        Looper.prepare();
                        Toast.makeText(register.this, "用户名已存在。", Toast.LENGTH_LONG).show();
                        Looper.loop();
                        break;
                    case 1:
                        //  注册成功
                        UserManager.saveAccount(register.this, username, password, null);

                        Looper.prepare();
                        Toast.makeText(register.this, "注册成功！", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(register.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Looper.loop();
                }
            }
        });
    }
}