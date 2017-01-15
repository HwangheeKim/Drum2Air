package com.example.q.drum2air;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreen extends AppCompatActivity {
    boolean drumActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        drumActivity = false;

        Thread timerThread = new Thread() {
            public void run(){
                try {
                    sleep(3000);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (!drumActivity) {
                        Intent intent = new Intent(getApplicationContext(), DrumActivity.class);
                        startActivity(intent);
                    }
                }
            }
        };
        timerThread.start();

        View view = findViewById(R.id.splash_layout);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent intent = new Intent(getApplicationContext(), DrumActivity.class);
                startActivity(intent);
                drumActivity = true;
                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }

}