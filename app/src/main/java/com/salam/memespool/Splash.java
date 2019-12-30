package com.salam.memespool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends AppCompatActivity {

    SharedPreferences sd;
    SharedPreferences.Editor editor;
    String userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sd = getSharedPreferences("memespool", Context.MODE_PRIVATE);
        editor = sd.edit();


        userData = sd.getString("userData","");


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!userData.equalsIgnoreCase("")){
                    startActivity(new Intent(Splash.this,Feed.class));
                    finish();
                }else {
                    startActivity(new Intent(Splash.this,Login.class));
                    finish();
                }
            }
        }, 3000);
    }
}
