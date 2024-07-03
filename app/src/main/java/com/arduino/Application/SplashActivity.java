package com.arduino.Application;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        //splash 1초 동안 뜨게 함.
        final Handler hd = new Handler();
        hd.postDelayed(new splashHandler(), 2000);//1000은 1초다. 2초로 설정.
       // postDelayed 매소드를 통해 2초 뒤에 splashHandler 작동하도록 설정

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    //splashHandler 클래스 생성
    private class splashHandler implements Runnable {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            SplashActivity.this.finish();


        }
    }
}
