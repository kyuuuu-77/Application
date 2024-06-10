package com.arduino.Application;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
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
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;


public class MainActivity extends AppCompatActivity {
    /* 안드로이드 애플리케이션 생명주기!
     * onCreate()->onStart()->onResume()             ->onDestroy()
     *                                   <->onPause
     *                                   <->onStop()*/

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    //Edited
    Button mBtnBT_on;
    Button mBtnBT_off;
    Button mBtnBT_Connect;
    Button mBtnSendData;
    TextView mTvBT_Status;
    TextView mTvBT_Receive0;
    TextView mTvBT_Receive1;
    TextView mTv_SendData;
    TextView homeText;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothManager mBluetoothManager;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    final int BT_REQUEST_ENABLE = 1;
    final int BT_MESSAGE_READ = 2;
    final int BT_CONNECTING_STATUS = 3;
    final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Window window;
    Toolbar toolbar;

    private BluetoothGatt bluetoothGatt;
    private TextView rssiTextView;
    private Handler handler = new Handler();

    private static final int SINGLE_PERMISSION = 1004;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"HandlerLeak", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mBtnBT_on = findViewById(R.id.btnBT_On);
        mBtnBT_off = findViewById(R.id.btnBT_Off);
        mBtnBT_Connect = findViewById(R.id.btnBT_Connect);
        mBtnSendData = findViewById(R.id.btnSendData);
        mTvBT_Status = findViewById(R.id.BT_Status);
        mTvBT_Receive0 = findViewById(R.id.BT_Receive0);
        mTvBT_Receive1 = findViewById(R.id.BT_Receive1);
        mTv_SendData = findViewById(R.id.tvSendData);
        homeText = findViewById(R.id.text_home);
        toolbar = findViewById (R.id.toolbar);

        rssiTextView = findViewById(R.id.rssi);

        setSupportActionBar (toolbar);  //액티비티의 App Bar로 지정
        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "아직 버튼을 구성하지 않았습니다.", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_find, R.id.nav_weight, R.id.nav_info)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


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

        window = getWindow();   //윈도우 생성하는 함수
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        if (mBluetoothAdapter == null) {
            mTvBT_Status.setText("지원하지 않음");
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

        //버튼 이벤트 리스너
        //블루투스 ON 버튼
        mBtnBT_on.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BT_on();
                }
            }
        });
        //블루투스 OFF 버튼
        mBtnBT_off.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BT_off();
                }
            }
        });
        //연결 버튼
        mBtnBT_Connect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });

        //전송 버튼
        mBtnSendData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mThreadConnectedBluetooth != null) {
                    String cmdText = mTv_SendData.getText().toString();
                    for (int i = 0; i < cmdText.length(); i++){
                        mThreadConnectedBluetooth.write(cmdText.substring(i,i+1));
                    }
                    mTv_SendData.setText("");
                }
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
                    //mTvBT_Receive.setText(readMessage);
                    String[] array = readMessage.split(",", 3);
                    mTvBT_Receive0.setText(array[0]);
                    mTvBT_Receive1.setText(array[1]);
                }
            }
        };
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @SuppressLint("HandlerLeak")
    protected void onResume() {     //동적으로 실행되는 onResume 메서드, onCreate와 다르게  앱 기동중에 계속 실행된다.
        super.onResume();
        setSupportActionBar (toolbar);

        if (mBluetoothAdapter.isEnabled()) {
            window.setStatusBarColor(Color.parseColor("#1976D2"));
            toolbar.setBackgroundColor(Color.parseColor("#2196F3"));
            mBtnBT_on.setEnabled(false);
            mBtnBT_off.setEnabled(true);
            mBtnBT_Connect.setEnabled(true);
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
    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, SINGLE_PERMISSION);
        }
    }

    //블루투스를 켜는 함수
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
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                checkPermission();
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    //블루투스를 끄는 함수
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void BT_off() {
        if (mBluetoothAdapter.isEnabled()) {
            checkPermission();
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            mTvBT_Status.setText("비활성화");
            window.setStatusBarColor(Color.parseColor("#F57C00"));
            toolbar.setBackgroundColor(Color.parseColor("#FF9800"));
            mBtnBT_on.setEnabled(true);
            mBtnBT_off.setEnabled(false);
            mBtnBT_Connect.setEnabled(false);
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_SHORT).show();
                    mTvBT_Status.setText("활성화");
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
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
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) { //선택된 블루투스 디바이스(장치)를 연결하는 메서드
                        connectSelectedDevice(items[item].toString());
                    }
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
            }
            else{
                homeText.setText("페어링 된 디바이스는 스마트 캐리어가 아닙니다");
            }
        } catch (IOException e) {   //연결에 실패하면 에러 표시
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류 발생!", Toast.LENGTH_SHORT).show();
            homeText.setText("연결에 실패 하였습니다 IOException");
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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
}