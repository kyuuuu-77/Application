package com.arduino.Application;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.arduino.Application.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;

public class MainActivity extends AppCompatActivity {
    /* Activty 생명주기 참고할 것 !!!
     * onCreate()->onStart()->onResume()
     *                                  <->[onPause()->onStop()->onRestart()]
     *                                                              ->onDestroy()
     * */

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    //버튼 변수 초기화
    Button mBtnBT_on;
    Button mBtnBT_off;
    Button mBtnBT_Connect;
    Button mBtnSendData;
    TextView mTvBT_Status;
    TextView mTv_SendData;
    TextView homeText;
    TextView rssiTextView;

    
    //블루투스 관련 변수
    BluetoothAdapter mBluetoothAdapter;
    BluetoothManager mBluetoothManager;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    BluetoothDevice mBluetoothDevice;
    BluetoothGatt bluetoothGatt;
    BluetoothSocket mBluetoothSocket;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;

    final int BT_REQUEST_ENABLE = 1;
    final int BT_REQUEST_DISABLE = 3;
    final int BT_MESSAGE_READ = 2;
    final int BT_CONNECTING_STATUS = 3;
    final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    Window window;
    Toolbar toolbar;

    private static final int SINGLE_PERMISSION = 1004;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"HandlerLeak", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mBtnBT_on = findViewById(R.id.btnBT_On);    //블루투스를 켜는 버튼 ID
        mBtnBT_off = findViewById(R.id.btnBT_Off);  //블루투스를 끄는 버튼 ID
        mBtnBT_Connect = findViewById(R.id.btnBT_Connect);  //연결 버튼
        mBtnSendData = findViewById(R.id.btnSendData);  //전송 버튼
        mTvBT_Status = findViewById(R.id.BT_Status);    //블루투스 상태 텍스트 뷰
        homeText = findViewById(R.id.text_home);        //홈 텍스트 표시 (나중에 제거 예정)
        rssiTextView = findViewById(R.id.rssi); //RSSI 상태 텍스트 뷰
        toolbar = findViewById(R.id.toolbar);   //툴바

        Log.d("Activity Main", "Activity Main-onCreate()");

        setSupportActionBar(toolbar);  //액티비티의 App Bar로 지정
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "아직 버튼을 구성하지 않았습니다.", Snackbar.LENGTH_SHORT)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show());
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_find, R.id.nav_weight, R.id.nav_info)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //퍼미션 리스트 배열
        String[] permission_list = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        //장치가 블루투스 기능을 지원하는지 확인하는 메서드
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        //윈도우 생성하는 함수
        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        if (mBluetoothAdapter == null) {
            mTvBT_Status.setText("블루투스 지원하지 않음");
            window.setStatusBarColor(Color.parseColor("#D32F2F"));
            toolbar.setBackgroundColor(Color.parseColor("#F44336"));
            mBtnBT_on.setEnabled(false);
            mBtnBT_off.setEnabled(false);
            mBtnBT_Connect.setEnabled(false);
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                mTvBT_Status.setText("활성화");
                window.setStatusBarColor(Color.parseColor("#1976D2"));
                toolbar.setBackgroundColor(Color.parseColor("#2196F3"));
                mBtnBT_on.setEnabled(false);
                mBtnBT_off.setEnabled(true);
                mBtnBT_Connect.setEnabled(true);
            } else {
                mTvBT_Status.setText("비활성화");
                window.setStatusBarColor(Color.parseColor("#F57C00"));
                toolbar.setBackgroundColor(Color.parseColor("#FF9800"));
                mBtnBT_on.setEnabled(true);
                mBtnBT_off.setEnabled(false);
                mBtnBT_Connect.setEnabled(false);
            }
        }

        //버튼 이벤트 리스너들 (람다 함수로 수정됨)
        //블루투스 ON 버튼
        mBtnBT_on.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                BT_on();
            } else {
                BT_on_Legacy();
            }
        });
        //블루투스 OFF 버튼
        mBtnBT_off.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                BT_off();
            } else {
                BT_off_Legacy();
            }
        });
        //연결 버튼
        mBtnBT_Connect.setOnClickListener(view -> listPairedDevices());
        mBtnSendData.setEnabled(false); //전송 버튼 필요 없으므로 임시 비활성화 -> 나중에 삭제 예정

        //전송 버튼
        mBtnSendData.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String cmdText = mTv_SendData.getText().toString();
                for (int i = 0; i < cmdText.length(); i++) {
                    mThreadConnectedBluetooth.write(cmdText.substring(i, i + 1));
                }
                mTv_SendData.setText("");
            }
        });

        //데이터 수신
        mBluetoothHandler = new Handler() {
            public void handleMessage(@NonNull android.os.Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }

                    String[] array = readMessage.split(",", 3);
//                    mTvBT_Receive0.setText(array[0]);
//                    mTvBT_Receive1.setText(array[1]);
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    protected void onRestart() {
        super.onRestart();

        Log.d("Activity Main", "Activity Main-onRestart()");
    }

    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);

        Log.d("Activity Main", "Activity Main-onResume()");

        if (mBluetoothAdapter.isEnabled()) {
            homeText.setText("연결 되어있습니다.");
            mTvBT_Status.setText("활성화");
            window.setStatusBarColor(Color.parseColor("#1976D2"));
            toolbar.setBackgroundColor(Color.parseColor("#2196F3"));
            mBtnBT_on.setEnabled(false);
            mBtnBT_off.setEnabled(true);
            mBtnBT_Connect.setEnabled(true);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            homeText.setText("연결이 해제 되어있습니다.");
            mTvBT_Status.setText("비활성화");
            window.setStatusBarColor(Color.parseColor("#F57C00"));
            toolbar.setBackgroundColor(Color.parseColor("#FF9800"));
            mBtnBT_on.setEnabled(true);
            mBtnBT_off.setEnabled(false);
            mBtnBT_Connect.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, SINGLE_PERMISSION);
        }
    }

    //블루투스를 켜는 함수 -> SDK 31 이상 (안드로이드 12 이상)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void BT_on() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기종입니다", Toast.LENGTH_SHORT).show();
            mTvBT_Status.setText("지원하지 않음");
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다", Toast.LENGTH_SHORT).show();
                mTvBT_Status.setText("활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_SHORT).show();

                checkPermission();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    //블루투스를 켜는 함수 -> (레거시용)
    @SuppressLint("MissingPermission")
    public void BT_on_Legacy() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기종입니다", Toast.LENGTH_SHORT).show();
            mTvBT_Status.setText("지원하지 않음");
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다", Toast.LENGTH_SHORT).show();
                mTvBT_Status.setText("활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.enable();
            }
        }
    }

    //블루투스를 끄는 함수 -> SDK 31 이상 (안드로이드 12 이상)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void BT_off() {
        if (mBluetoothAdapter.isEnabled()) {
            checkPermission();

            //안드로이드 정책 관리 변경으로 API 33이상부터 사용자의 퍼미션 없이 블루투스를 끄는 것이 불가능해짐
            //즉, 이전처럼 BluetoothAdapter.disable()을 사용할 수 없다는 것
            //Intent를 통한 새로운 방식을 사용
            Intent intentBluetoothDisable = new Intent("android.bluetooth.adapter.action.REQUEST_DISABLE");
            startActivityForResult(intentBluetoothDisable, BT_REQUEST_DISABLE);
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //블루투스를 끄는 함수 -> (레거시용)
    @SuppressLint("MissingPermission")
    public void BT_off_Legacy() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_SHORT).show();
                    mTvBT_Status.setText("활성화");
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), "취소됨", Toast.LENGTH_SHORT).show();
                    mTvBT_Status.setText("비활성화");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //블루투스 디바이스 목록을 보여주는 메서드
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            checkPermission();
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if (!mPairedDevices.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");
                mListPairedDevices = new ArrayList();
                checkPermission();
                for (BluetoothDevice device : mPairedDevices) {
                    if (device.getName()==null)
                        continue;
                    mListPairedDevices.add(device.getName());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[0]);
                mListPairedDevices.toArray(new CharSequence[0]);

                builder.setItems(items, (dialog, item) -> { //선택된 블루투스 디바이스(장치)를 연결하는 메서드
                    connectSelectedDevice(items[item].toString());
                });
                Toast.makeText(getApplicationContext(), "FB301(73F06C)를 선택하세요.", Toast.LENGTH_SHORT).show();
                AlertDialog alert = builder.create();
                alert.show();
            }
            else{
                Toast.makeText(getApplicationContext(),"페이링된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else{
            Toast.makeText(getApplicationContext(),"블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //블루투스 디바이스에 연결하는 메서드
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void connectSelectedDevice(String selectedDeviceName) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            checkPermission();
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
            if (Objects.equals(selectedDeviceName, "FB301(73F06C)")){
                homeText.setText("스마트 캐리어에 연결 되었습니다!");
                startRSSIMeasurement();
            }
            else{
                homeText.setText("페어링 된 디바이스는 스마트 캐리어가 아닙니다");
            }
        } catch (IOException e) {   //연결에 실패하면 에러 표시
            Toast.makeText(getApplicationContext(), "디바이스 연결 중 오류 발생!", Toast.LENGTH_SHORT).show();
            homeText.setText("연결에 실패 하였습니다");
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Toast.makeText(getApplicationContext(),"  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();
            }
        }
    };
    
    //데이터 수신 클래스(스레드 생성)
    private class ConnectedBluetoothThread extends Thread {
        //소켓을 통해 전송 처리
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류 발생!", Toast.LENGTH_SHORT).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        
        //받은 데이터가 존재한다면 데이터를 읽어옴
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류 발생!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //아래부터 RSSI 측정 관련 함수들
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            rssiTextView.setText("RSSI: " + rssi);
        }
    };

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            bluetoothGatt.readRemoteRssi();
            handler.postDelayed(this, 1000);

        }
    };

    private void startRSSIMeasurement(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission();
        }
        bluetoothGatt = mBluetoothDevice.connectGatt(this,false,bluetoothGattCallback);
        handler.post(runnable);
    }

    private void stopRSSIMeasurement(){
        if(bluetoothGatt != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        stopRSSIMeasurement();

        Log.d("Activity Main", "Activity Main-onDestroy()");
    }
}