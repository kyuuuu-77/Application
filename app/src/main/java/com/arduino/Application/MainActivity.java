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
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.arduino.Application.ui.find.FindViewModel;
import com.arduino.Application.ui.home.HomeViewModel;
import com.arduino.Application.ui.info.InfoViewModel;
import com.arduino.Application.ui.weight.WeightViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
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
import java.nio.charset.StandardCharsets;
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

    //블루투스 관련 변수
    BluetoothAdapter mBluetoothAdapter;
    BluetoothManager mBluetoothManager;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    BluetoothDevice mBluetoothDevice;
    BluetoothGatt bluetoothGatt;
    BluetoothSocket mBluetoothSocket;
    ConnectedBluetoothThread mThreadConnectedBluetooth;

    //Handler 변수
    Handler mBluetoothHandler;

    //블루투스 통신에 사용되는 final 변수들
    final int BT_REQUEST_ENABLE = 1;
    final int BT_REQUEST_DISABLE = 3;
    final int BT_MESSAGE_READ = 2;
    final int BT_CONNECTING_STATUS = 3;
    final int REQUEST_LOCATION_PERMISSION = 123;
    final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int SINGLE_PERMISSION = 1004;

    //viewModel 사용을 위한 변수
    private HomeViewModel viewModel_home;
    private WeightViewModel viewModel_weight;
    private FindViewModel viewModel_find;
    private InfoViewModel viewModel_info;

    //프로그램 동작을 위한 전역 변수
    protected int menuNum_Global = 0;
    private Boolean isDialogShowing = false;
    private int security = 0;

    //윈도우 및 툴바 관련 변수
    Window window;
    Toolbar toolbar;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"HandlerLeak", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "MainActivity-onCreate()");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbar = findViewById(R.id.toolbar);   //툴바

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
                Manifest.permission.BLUETOOTH_CONNECT,      //블루투스 연결 권한
                Manifest.permission.BLUETOOTH_SCAN,         //블루투스 검색 권한
                Manifest.permission.BLUETOOTH_ADMIN,        //블루투스 어드민 권한
                Manifest.permission.ACCESS_COARSE_LOCATION  //위치 검색 권한
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        //장치가 블루투스 기능을 지원하는지 확인하는 메서드 (초기화)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        //각 Fragment의 viewModel 정의
        viewModel_home = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel_weight = new ViewModelProvider(this).get(WeightViewModel.class);
        viewModel_find = new ViewModelProvider(this).get(FindViewModel.class);
        viewModel_info = new ViewModelProvider(this).get(InfoViewModel.class);

        //윈도우를 생성하는 함수
        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        //데이터 수신 (아두이노->앱) Handler 사용
        mBluetoothHandler = new Handler() {
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    String readMessage;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);

                    //String[] array = readMessage.split(",", 3);
                    //mTvBT_Receive0.setText(array[0]);
                    //mTvBT_Receive1.setText(array[1]);
                }
            }
        };
        startBluetoothDiscovery();
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, SINGLE_PERMISSION);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    //블루투스를 켜는 함수 -> SDK 31 이상 (안드로이드 12 이상)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void BT_on() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기종입니다", Toast.LENGTH_SHORT).show();
            viewModel_home.setBluetoothStatus("지원하지 않음");
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다", Toast.LENGTH_SHORT).show();
                viewModel_home.setBluetoothStatus("블룰투스 활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                viewModel_home.setBluetoothStatus("블루투스 활성화중");
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
            viewModel_home.setBluetoothStatus("지원하지 않음");
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다", Toast.LENGTH_SHORT).show();
                viewModel_home.setBluetoothStatus("블루투스 활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.enable();
                viewModel_home.setBluetoothStatus("블루투스 활성화중");
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
            stopRSSIMeasurement();          //RSSI 측정 중지
            viewModel_home.setBluetoothStatus("블루투스 비활성화");
            viewModel_home.setHomeText("");
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //블루투스를 끄는 함수 -> (레거시용)
    @SuppressLint("MissingPermission")
    public void BT_off_Legacy() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            stopRSSIMeasurement();          //RSSI 측정 중지
            viewModel_home.setBluetoothStatus("블루투스 비활성화");
            viewModel_home.setHomeText("");
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BT_REQUEST_ENABLE) {
            if (resultCode == RESULT_OK) { // 블루투스 활성화 확인을 클릭하였다면
                Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화 취소를 클릭하였다면
                Toast.makeText(getApplicationContext(), "취소됨", Toast.LENGTH_SHORT).show();
            }
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
                mListPairedDevices = new ArrayList<>();
                checkPermission();

                for (BluetoothDevice device : mPairedDevices) {
                    if (device.getName()==null)
                        continue;
                    mListPairedDevices.add(device.getName());
                }

                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[0]);
                mListPairedDevices.toArray(new CharSequence[0]);

                //선택된 블루투스 디바이스를 연결하는 메서드
                builder.setItems(items, (dialog, item) -> {
                    connectSelectedDevice(items[item].toString());
                });
                Toast.makeText(getApplicationContext(), "스마트 캐리어와 연결하려면 FB301(73F06C)를 선택하세요.", Toast.LENGTH_SHORT).show();
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

    //자동으로 검색후 디바이스에 연결하는 메서드
    private void Auto_onnectSelectedDevice(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission();
        }
        // BluetoothSocket 생성 및 연결 시도
        //BluetoothSocket socket = null;
        try {
            // BluetoothSocket 생성
            //socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            mBluetoothSocket = device.createRfcommSocketToServiceRecord(BT_UUID);

            // 연결 시도 전에 블루투스 검색 취소
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            //BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }

            // Bluetooth 연결 시도
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothDevice = device;

            // 연결이 성공적으로 이루어졌을 때 추가 작업 수행
            Toast.makeText(getApplicationContext(), "디바이스와 연결되었습니다.", Toast.LENGTH_SHORT).show();
            viewModel_home.setHomeText("스마트 캐리어에 연결 되었습니다!");
            startRSSIMeasurement();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "디바이스 연결 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            try {
                if (mBluetoothSocket != null) {
                    mBluetoothSocket.close();
                }
            } catch (IOException closeException) {
                // 소켓 닫기 실패 처리
            }
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
                Toast.makeText(getApplicationContext(), "연결 성공!", Toast.LENGTH_SHORT).show();
                viewModel_home.setHomeText("스마트 캐리어에 연결 되었습니다!");
                startRSSIMeasurement();
            }
            else{
                viewModel_home.setHomeText("페어링 된 디바이스는 스마트 캐리어가 아닙니다!");
            }
        } catch (IOException e) {   //연결에 실패하면 에러 표시
            Toast.makeText(getApplicationContext(), "디바이스 연결 중 오류 발생!", Toast.LENGTH_SHORT).show();
            viewModel_home.setHomeText("연결에 실패 하였습니다");
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            //스마트 캐리어가 주변에 있으면
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("BroadcastReceiver", "Action Detected!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }
                assert device != null;

                if (device.getName() != null && device.getName().equals("FB301(73F06C)")) {
                    if (!isDialogShowing) {     // 다이얼로그가 표시 중인지를 나타내는 변수
                        if (bluetoothAdapter.isDiscovering()) {
                            bluetoothAdapter.cancelDiscovery();
                        }
                        showConnectionDialog(device);
                        isDialogShowing = true;
                    }
                }
            }

            //블루투스 장치와 연결된 경우
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }
                assert device != null;
                String deviceName = device.getName();
                Toast.makeText(context, deviceName + "와 연결 되었습니다.", Toast.LENGTH_LONG).show();
            }

            //블루투스 연결이 끊긴 경우
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }
                assert device != null;
                String deviceName = device.getName();
                Toast.makeText(context, deviceName + "와의 연결이 끊겼습니다.", Toast.LENGTH_LONG).show();
            }
        }
    };

    //상시 스마트 캐리어를 검색하는 메서드
    public void startBluetoothDiscovery() {
        // BluetoothAdapter 가져오기
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission();
        }

        // Bluetooth가 켜져 있는지 확인하고, 켜져 있지 않으면 Toast 표시
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "블루투스가 꺼져 있습니다.", Toast.LENGTH_SHORT).show();
        }

        // 기존에 진행 중인 디바이스 검색 취소
        assert bluetoothAdapter != null;
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // 디바이스 검색 시작
        bluetoothAdapter.startDiscovery();
    }

    // 스마트 캐리어를 발견했을때 연결을 시도하는 메세지를 띄우는 메서드
    private void showConnectionDialog(final BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("스마트 캐리어 발견")
                .setMessage("스마트 캐리어를 자동으로 발견했습니다. 연결하시겠습니까?")
                .setPositiveButton("연결", (dialog, which) -> {
                    // Bluetooth 연결 로직 수행
                    isDialogShowing = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Auto_onnectSelectedDevice(device);
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    isDialogShowing = false;
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
    
    //데이터 송수신 클래스 (스레드 사용)
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
        
        // 데이터 수신
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

        // 데이터 송신
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류 발생!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //메뉴 번호를 지정하는 메서드
    public void setMenuNum(int num){
        menuNum_Global = num;

        if (mThreadConnectedBluetooth != null) {
            String cmdText = String.valueOf(menuNum_Global);
            for (int i = 0; i < cmdText.length(); i++){
                mThreadConnectedBluetooth.write(cmdText.substring(i,i+1));
            }
            Log.d("sendData", "데이터 전송 성공!");
        }
    }

    //RSSI 측정 관련 메서드들
    private final Handler handler_RSSI = new Handler();
    private final Runnable runnable_RSSI = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            bluetoothGatt.readRemoteRssi();
            handler_RSSI.postDelayed(this, 1000);
        }
    };

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            handler_RSSI.post(() -> viewModel_home.setRssi("RSSI: " + rssi + " dBm"));
        }
    };

    private void startRSSIMeasurement(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission();
        }
        bluetoothGatt = mBluetoothDevice.connectGatt(this,false,bluetoothGattCallback);
        handler_RSSI.post(runnable_RSSI);
    }

    private void stopRSSIMeasurement(){
        if(bluetoothGatt != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            handler_RSSI.removeCallbacks(runnable_RSSI);
            viewModel_home.setRssi("RSSI");
        }
    }

    // 도난방기 동작 메서드
    public int security_ON(){
        security = 1;
        Toast.makeText(getApplicationContext(), "도난방지가 켜졌습니다.", Toast.LENGTH_SHORT).show();
        viewModel_home.setAlertStatus("도난방지 Enabled");
        return security;
    }

    public int security_OFF(){
        security = 0;
        Toast.makeText(getApplicationContext(), "도난방지가 꺼졌습니다.", Toast.LENGTH_SHORT).show();
        viewModel_home.setAlertStatus("도난방지 Disabled");
        return security;
    }

    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);

        Log.d("MainActivity", "MainActivity-onResume()");

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        
        registerReceiver(receiver, filter);     //브로드 캐스트 리시버 등록

        if (mBluetoothAdapter.isEnabled()) {
            window.setStatusBarColor(Color.parseColor("#1976D2"));
            toolbar.setBackgroundColor(Color.parseColor("#2196F3"));
        }
        if (!mBluetoothAdapter.isEnabled()) {
            window.setStatusBarColor(Color.parseColor("#F57C00"));
            toolbar.setBackgroundColor(Color.parseColor("#FF9800"));
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        Log.d("MainActivity", "MainActivity-onDestroy()");

        stopRSSIMeasurement();          //RSSI 측정 중지
        unregisterReceiver(receiver);   //브로드캐스트 리시버 해제
    }
}