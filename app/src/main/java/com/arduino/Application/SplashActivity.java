package com.arduino.Application;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ImageView imageView;

    TextView textView;

    Animation imanim,teanim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        Log.d("SplashActivity", "SplashActivity-onCreate()");

        imageView = findViewById(R.id.imageView3);
        textView = findViewById(R.id.textView4);

        imanim = AnimationUtils.loadAnimation(this,R.anim.imageanim);
        teanim = AnimationUtils.loadAnimation(this,R.anim.textanim);

        imageView.setAnimation(imanim);
        textView.setAnimation(teanim);

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
