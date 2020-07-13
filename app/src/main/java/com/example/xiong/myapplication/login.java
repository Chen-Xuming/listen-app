package com.example.xiong.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

                loginRequest(name, pass);

                break;
            case R.id.go_register:
                Intent intent1 = new Intent(login.this,register.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    private void loginRequest(final String username, final String password){
        String url = "http://129.204.242.63:8080/listen/loginServlet?action=login";

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
                Toast.makeText(login.this, "网络不佳，请重试。", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(login.this, "账号或密码错误。", Toast.LENGTH_LONG).show();
                        Looper.loop();
                        break;
                    case 1:
                        //  登录成功

                        String headPic = null;
                        if(jsonObject.get("data") != null){
                            headPic = jsonObject.get("data").getAsString();
                        }

                        UserManager.saveAccount(login.this, username, password, headPic);

                        Looper.prepare();
                        Toast.makeText(login.this, "登录成功！", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(login.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        Looper.loop();
                        break;
                }
            }
        });
    }
}