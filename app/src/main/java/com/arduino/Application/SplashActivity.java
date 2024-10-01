package com.arduino.Application;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    ImageView imageView;
    TextView textView;
    Animation imanim, teanim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        imageView = findViewById(R.id.imageView3);
        textView = findViewById(R.id.textView4);

        imanim = AnimationUtils.loadAnimation(this, R.anim.imageanim);
        teanim = AnimationUtils.loadAnimation(this, R.anim.textanim);

        imageView.setAnimation(imanim);
        textView.setAnimation(teanim);

        // 필요한 권한 확인 및 요청
        checkPermissions();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // 권한 확인 및 요청 메서드
    private void checkPermissions() {
        // 필요한 권한 리스트를 만들고 확인
        String[] permissionList;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList = new String[]{     // 권한 리스트 (메인 액티비티에도 동일)
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionList = new String[]{     // 권한 리스트 (메인 액티비티에도 동일)
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            permissionList = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        }

        if (!hasPermissionsGranted(permissionList)) {
            ActivityCompat.requestPermissions(this, permissionList, PERMISSION_REQUEST_CODE);
        } else {
            proceedWithSplash();
        }
    }

    // 권한 확인 메서드
    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // 권한 요청 결과 처리 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            // 모든 권한이 허가 되었으면 진행, 아니면 강제 종료
            if (allPermissionsGranted) {
                proceedWithSplash();
            } else {
                Toast.makeText(this, "필수 권한이 허가되지 않았으므로 앱을 강제 종료합니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // 스플래시 화면 -> 메인 액티비티
    // 스플래시 2초 동안 뜨게 함
    private void proceedWithSplash() {
        final Handler hd = new Handler();
        hd.postDelayed(new splashHandler(), 2000); // 2초 후에 splashHandler 작동
    }

    private class splashHandler implements Runnable {
        @Override
        public void run() {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            SplashActivity.this.finish();
        }
    }
}
