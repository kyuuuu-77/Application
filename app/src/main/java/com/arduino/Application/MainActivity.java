package com.arduino.Application;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.arduino.Application.ui.alert.AlertViewModel;
import com.arduino.Application.ui.find.FindViewModel;
import com.arduino.Application.ui.home.HomeViewModel;
import com.arduino.Application.ui.info.InfoViewModel;
import com.arduino.Application.ui.weight.WeightViewModel;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.arduino.Application.databinding.ActivityMainBinding;

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

    // 블루투스 관련 변수
    BluetoothAdapter mBluetoothAdapter;
    BluetoothManager mBluetoothManager;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;
    BluetoothDevice mBluetoothDevice;

    // 블루투스 BLE 관련 변수
    private static final UUID SERVICE_UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB");
    private static final UUID WRITE_CHAR_UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB");
    private static final UUID READ_CHAR_UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB");
    private BluetoothGatt bluetoothGatt;        //Gatt = Generic Attribute Profile
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private long writeStartTime;

    // 블루투스 통신에 사용되는 final 변수들
    final int BT_REQUEST_ENABLE = 1;
    final int BT_REQUEST_DISABLE = 3;
    final int REQUEST_LOCATION_PERMISSION = 123;
    private static final int SINGLE_PERMISSION = 1004;

    // viewModel 사용을 위한 변수
    private HomeViewModel viewModel_home;
    private FindViewModel viewModel_find;
    private WeightViewModel viewModel_weight;
    private AlertViewModel viewModel_alert;
    private InfoViewModel viewModel_info;

    // 프로그램 동작을 위한 전역 변수
    private Boolean isDialogShowing = false;
    private boolean isSuitcase = false;
    private boolean onAutoSearch = true;
    private boolean rssiSignal = false;
    private boolean security = false;
    private boolean alreadyConnected = false;
    private boolean checkDialog = false;
    private double[] weight = {0.0, 0.0};   // weight, tps
    private String data;

    // 윈도우 및 툴바 관련 변수
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

        setSupportActionBar(toolbar);  // 액티비티의 App Bar로 지정
        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_find, R.id.nav_weight, R.id.nav_info, R.id.nav_alert)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // 퍼미션 리스트 배열
        String[] permission_list = {
                Manifest.permission.BLUETOOTH_CONNECT,      // 블루투스 연결 권한
                Manifest.permission.BLUETOOTH_SCAN,         // 블루투스 검색 권한
                Manifest.permission.BLUETOOTH_ADMIN,        // 블루투스 어드민 권한
                Manifest.permission.ACCESS_COARSE_LOCATION  // 위치 검색 권한
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        // 블루투스 어댑터, 매니저 초기화
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // 각 Fragment의 viewModel 정의
        viewModel_home = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel_weight = new ViewModelProvider(this).get(WeightViewModel.class);
        viewModel_find = new ViewModelProvider(this).get(FindViewModel.class);
        viewModel_alert = new ViewModelProvider(this).get(AlertViewModel.class);
        viewModel_info = new ViewModelProvider(this).get(InfoViewModel.class);

        // 윈도우 생성
        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
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

    // 퍼미션(권한)을 체크하는 메서드
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, SINGLE_PERMISSION);
        } else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    // 블루투스를 켜는 메서드 -> SDK 31 이상 (안드로이드 12 이상)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void BT_on() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기종입니다.", Toast.LENGTH_SHORT).show();
            viewModel_home.setBluetoothStatus("지원하지 않음");
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
                viewModel_home.setBluetoothStatus("블루투스 활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                viewModel_home.setBluetoothStatus("블루투스 활성화 중");
                checkPermission();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    // 블루투스를 켜는 메서드 -> (레거시용)
    @SuppressLint("MissingPermission")
    public void BT_on_Legacy() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기종입니다", Toast.LENGTH_SHORT).show();
            viewModel_home.setBluetoothStatus("지원하지 않음");
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화되어 있습니다", Toast.LENGTH_SHORT).show();
                viewModel_home.setBluetoothStatus("블루투스 활성화");
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.enable();
                viewModel_home.setBluetoothStatus("블루투스 활성화 중");
            }
        }
    }

    // 블루투스를 끄는 메서드 -> SDK 31 이상 (안드로이드 12 이상)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void BT_off() {
        if (mBluetoothAdapter.isEnabled()) {
            checkPermission();
            // 안드로이드 정책 관리 변경으로 API 33이상부터 사용자의 퍼미션 없이 블루투스를 끄는 것이 불가능해짐
            // 즉, 이전처럼 BluetoothAdapter.disable()을 사용할 수 없다는 것
            // Intent를 통한 새로운 방식을 사용
            Intent intentBluetoothDisable = new Intent("android.bluetooth.adapter.action.REQUEST_DISABLE");
            startActivityForResult(intentBluetoothDisable, BT_REQUEST_DISABLE);
            stopRSSIMeasurement();          // RSSI 측정 중지
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 블루투스를 끄는 메서드 -> (레거시용)
    @SuppressLint("MissingPermission")
    public void BT_off_Legacy() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            stopRSSIMeasurement();          // RSSI 측정 중지
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 블루투스 활성화 시 동작 수행 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BT_REQUEST_ENABLE) {
            // 블루투스 활성화 확인을 클릭하였다면
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_SHORT).show();
                onAutoSearch = true;
            } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화 취소를 클릭하였다면
                Toast.makeText(getApplicationContext(), "취소됨", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == BT_REQUEST_DISABLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "블루투스 비활성화", Toast.LENGTH_SHORT).show();
                viewModel_home.setBluetoothStatus("블루투스 비활성화");
                viewModel_home.setHomeText("");
                onAutoSearch = false;
            } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화 취소를 클릭하였다면
                Toast.makeText(getApplicationContext(), "취소됨", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 블루투스 디바이스 목록을 보여주는 메서드
    public void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if (!mPairedDevices.isEmpty()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");
                mListPairedDevices = new ArrayList<>();

                for (BluetoothDevice device : mPairedDevices) {
                    if (device.getName()==null)
                        continue;
                    mListPairedDevices.add(device.getName());
                }

                CharSequence[] items = mListPairedDevices.toArray(new CharSequence[0]);
                mListPairedDevices.toArray(new CharSequence[0]);

                // 선택된 블루투스 디바이스를 연결하는 메서드
                builder.setItems(items, (dialog, item) -> connectSelectedDevice(items[item].toString()));
                Toast.makeText(getApplicationContext(), "스마트 캐리어와 연결하려면 FB301(73F06C)를 선택하세요.", Toast.LENGTH_SHORT).show();
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(),"페어링 된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else{
            Toast.makeText(getApplicationContext(),"블루투스가 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 자동으로 검색후 스마트 캐리어에 연결하는 메서드
    private void Auto_onnectSelectedDevice(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission();
        }
        bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "디바이스에 연결 시도 중...", Toast.LENGTH_SHORT).show());
        isSuitcase = true;
    }

    // 블루투스 디바이스에 연결하는 메서드
    public void connectSelectedDevice(String selectedDeviceName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission();
        }
        for (BluetoothDevice device : mPairedDevices) {
            if (selectedDeviceName.equals(device.getName())) {
                mBluetoothDevice = device;
                break;
            }
        }
        // 연결한 디바이스가 스마트 캐리어인지 확인
        if (Objects.equals(selectedDeviceName, "FB301(73F06C)")){
            isSuitcase = true;
        }
        bluetoothGatt = mBluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
    }

    // 주변의 BLE 디바이스를 스캔
    public void startLeScan() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Toast.makeText(this, "블루투스가 꺼져 있어 자동 검색을 수행할 수 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            Toast.makeText(this, "BluetoothLeScanner 초기화 오류.", Toast.LENGTH_LONG).show();
            return;
        }

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                mBluetoothDevice = result.getDevice();
//                int rssi = result.getRssi();
//                Log.d("rssi", String.valueOf(rssi));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }

                if (!isDialogShowing && !checkDialog){
                    if (onAutoSearch && mBluetoothDevice.getName() != null && mBluetoothDevice.getName().equals("FB301(73F06C)")) {     // 스마트 캐리어를 발견 했으면
                        bluetoothLeScanner.stopScan(scanCallback);
                        showConnectionDialog(mBluetoothDevice);
                        isDialogShowing = true;
                        checkDialog = true;
                    }
                }

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e("ScanFailed", "스캔 실패, 에러코드 : " + errorCode);
            }
        };

        // BLE 스캔 시작
        bluetoothLeScanner.startScan(scanCallback);
    }

    // 스캔 중지
    private void stopLeScan() {
        if (bluetoothLeScanner != null && scanCallback != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    // 스마트 캐리어를 자동으로 발견했을때 연결 시도 다이얼로그를 띄우는 메서드
    private void showConnectionDialog(final BluetoothDevice device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("스마트 캐리어 발견")
                .setMessage("스마트 캐리어를 자동으로 발견했습니다. 연결하시겠습니까?")
                .setPositiveButton("연결", (dialog, which) -> {
                    isDialogShowing = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Auto_onnectSelectedDevice(device);
                        stopLeScan();
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    isDialogShowing = false;
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                    stopLeScan();
                    onAutoSearch = false;
                })
                .setCancelable(false).show();
    }

    // 메뉴 번호를 저장하고 아두이노에 송신하는 메서드
    public void setMenuNum(int num){
        if (writeCharacteristic != null){
            sendData(String.valueOf(num));
            writeStartTime = System.currentTimeMillis();
            Log.d("setMenuNum", "메뉴 값 전송 완료");
        } else {
            Log.d("setMenuNum", "메뉴 값 전송 실패");
        }
    }

    // Handler로 1초마다 RSSI 측정하는 Handler와 Runnable
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

    // RSSI 측정 시작 메서드
    public void startRSSIMeasurement(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermission();
        }
        if (bluetoothGatt == null) {
            bluetoothGatt = mBluetoothDevice.connectGatt(this, true, bluetoothGattCallback);
        }
        handler_RSSI.postDelayed(runnable_RSSI, 1000);
        rssiSignal = true;
    }

    // RSSI 측정 중지 메서드
    public void stopRSSIMeasurement(){
        if(bluetoothGatt != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
            handler_RSSI.removeCallbacks(runnable_RSSI);
            rssiSignal = false;
        }
    }

    // 5초마다 블루투스 재접속을 시도하는 Handler와 Runnable
    private final Handler reconnectHandler = new Handler();
    private final Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (bluetoothGatt != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }
                Log.d("연결 재시도", "블루투스 연결 재시도 중...");
                bluetoothGatt.connect();
            }
            reconnectHandler.postDelayed(this, 5000);
        }
    };

    // BLE 통신을 위한 BluetoothGatt 객체 생성
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {        // 블루투스 디바이스와 연결 된 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }
                runOnUiThread(() -> {
                    if (isSuitcase){
                        viewModel_home.setHomeText("스마트 캐리어에 연결됨");
                        Toast.makeText(getApplicationContext(), "스마트 캐리어에 연결됨", Toast.LENGTH_SHORT).show();
                        alreadyConnected = true;
                        checkDialog = true;
                        reconnectHandler.removeCallbacks(reconnectRunnable);
                        createNotif("connect", "캐리어와 연결됨", "스마트 캐리어와 연결되었습니다!");
                        window.setStatusBarColor(Color.parseColor("#3F51B5"));
                        toolbar.setBackgroundColor(Color.parseColor("#3F51B5"));
                    } else {
                        viewModel_home.setHomeText("잘못된 디바이스에 연결됨");
                        Toast.makeText(getApplicationContext(), "연결된 디바이스는 스마트 캐리어가 아닙니다.", Toast.LENGTH_SHORT).show();
                        window.setStatusBarColor(Color.parseColor("#4CAF50"));
                        toolbar.setBackgroundColor(Color.parseColor("#4CAF50"));
                    }
                });
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {      // 블루투스 디바이스와 연결이 끊긴 경우
                if (mBluetoothAdapter.isEnabled() && alreadyConnected){
                    handler_RSSI.removeCallbacks(runnable_RSSI);
                    bluetoothGatt.disconnect();
                    runOnUiThread(() -> {
                        viewModel_home.setHomeText("디바이스와의 연결이 끊어졌습니다");
                        Toast.makeText(getApplicationContext(), "디바이스와의 연결이 끊어졌습니다", Toast.LENGTH_SHORT).show();
                        createNotif("disconnect", "캐리어와 연결 끊김", "스마트 캐리어와 연결이 끊겼습니다.");
                        window.setStatusBarColor(Color.parseColor("#FF9800"));
                        toolbar.setBackgroundColor(Color.parseColor("#FF9800"));
                    });
                    reconnectHandler.postDelayed(reconnectRunnable, 5000);
                    alreadyConnected = false;
                }
            }
        }

        // 블루투스의 연결 상태가 바뀌면
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        checkPermission();
                    }
                    writeCharacteristic = service.getCharacteristic(WRITE_CHAR_UUID);
                    readCharacteristic = service.getCharacteristic(READ_CHAR_UUID);
                    gatt.setCharacteristicNotification(readCharacteristic, true);
                    BluetoothGattDescriptor descriptor = readCharacteristic.getDescriptor(
                            UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
                    startRSSIMeasurement();
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    } else {
                        Log.d("onServicesDiscovered", "Descriptor Not Found");
                    }
                } else {
                    Log.d("onServicesDiscovered", "Service Not Found");
                }
            }
        }

        // 데이터를 수신
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(READ_CHAR_UUID)) {
                byte[] value = characteristic.getValue();
                String receivedData = new String(value);
                data = receivedData;
                Log.d("Received data", "받은 데이터 : "+ receivedData);
            }
        }

        // 데이터를 송신
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                long writeEndTime = System.currentTimeMillis();
                long delay = writeEndTime - writeStartTime;
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "전송 Delay : " + delay + "ms", Toast.LENGTH_SHORT).show());
                Log.d("Send data", "데이터 송신 성공");
            }
        }

        // RSSI 값 측정
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            handler_RSSI.post(() -> viewModel_info.setRssi("RSSI: " + rssi + " dBm"));
        }
    };

    // 데이터 송신 메서드
    private void sendData(String data) {
        if (writeCharacteristic != null) {
            byte[] bytes = data.getBytes();
            writeCharacteristic.setValue(bytes);
            if (bluetoothGatt != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }
                bluetoothGatt.writeCharacteristic(writeCharacteristic);
            }
        }
    }

    // 도난방지 ON 메서드
    public boolean security_ON(){
        security = true;
        Toast.makeText(getApplicationContext(), "도난방지가 켜졌습니다.", Toast.LENGTH_SHORT).show();
        return security;
    }

    // 도난방지 OFF 메서드
    public boolean security_OFF(){
        security = false;
        Toast.makeText(getApplicationContext(), "도난방지가 꺼졌습니다.", Toast.LENGTH_SHORT).show();
        return security;
    }

    // 무게를 측정하는 메서드
    @SuppressLint({"DefaultLocale", "HandlerLeak"})
    public double measureWeight(double maxTps) {
        if (writeCharacteristic != null) {
            // 무게값 초기화
            data = null;

            // 동작값을 먼저 전송 -> 캐리어에서 값 인식 후 무게값 전달
            sendData("menu 3");

            int cnt = 0;
            while (true) {
                cnt ++;
                SystemClock.sleep(10);
                if (data != null) {
                    Log.d("받은 데이터", data);
                    break;
                } else if (cnt >= 300){
                    Log.d("받은 데이터", "수신 실패");
                    break;
                }
            }
            // 무게값을 받지 못했으면
            if (data == null) {
                return -1;
            }

            double tmp_weight = Double.parseDouble(data);
            weight[0] = tmp_weight;
            weight[1] = maxTps;
            viewModel_weight.setWeightNow(String.format("%.1f", weight[0]) +" Kg");

            if (weight[0] > 32){                    // 32kg 초과시
                viewModel_weight.setLooseWeight("32Kg을 초과했습니다.");
            } else if (weight[0] > weight[1]){      // 허용무게 초과시
                viewModel_weight.setLooseWeight(String.format("%.1f", weight[0]-weight[1]) + " Kg  초과했습니다.");
            } else {                                // 무게 초과하지 않은 경우
                viewModel_weight.setLooseWeight("허용 무게를 초과하지 않았습니다.");
            }

            viewModel_weight.setWeightBtn("무게 다시 측정");
            return weight[0];
        } else {
            viewModel_weight.setLooseWeight("무게 측정에 실패하였습니다.");
            viewModel_weight.setWeightBtn("무게 측정 실패");
            return -1;
        }
    }

    // RSSI 측정 여부를 전달하는 메서드
    public boolean checkRssi() {
        if (!rssiSignal){
            viewModel_info.setRssi("RSSI 측정 불가");
        }
        return rssiSignal;
    }

    // 자동 검색 여부를 전달하는 메서드
    public boolean checkAutoSearch() {
        if (onAutoSearch){
            viewModel_info.setAutoSearch("자동 검색 사용중");
        } else {
            viewModel_info.setAutoSearch("자동 검색 꺼짐");
        }
        return onAutoSearch;
    }

    // 도난방지 여부를 전달하는 메서드
    public boolean checkSecurity() {
        if (security){
            viewModel_info.setSecurity("도난방지 켜짐");
        } else {
            viewModel_info.setSecurity("도난방지 꺼짐");
        }
        return security;
    }

    // 연결 상태를 전달하는 메서드
    public int checkConnection() {
        if (mBluetoothAdapter == null){
            viewModel_info.setInfoText("블루투스를 지원 X");
            return -2;
        } else if (!mBluetoothAdapter.isEnabled()){                // 블루투스가 꺼져 있음
            viewModel_info.setInfoText("블루투스가 꺼짐");
            return -1;
        } else if (bluetoothGatt == null) {     // 캐리어에 연결되어 있지 않음
            viewModel_info.setInfoText("연결되지 않음");
            return 0;
        } else if (writeCharacteristic == null || readCharacteristic == null){      // 연결이 되어 있으나 송수신 불가
            viewModel_info.setInfoText("송수신 불가능");
            return 1;
        } else {    // 캐리어에 연결되어 있음 <- 수정 필요
            viewModel_info.setInfoText("정상적으로 연결됨");
            return 9;
        }
    }

    // 무게 설정을 전달하는 메서드
    public double[] checkWeightSetting(){
        return weight;  // weight, tps
    }

    // 알람을 띄우는 메서드
    private void createNotif(String channel_id, String big, String summary) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel(channel_id);
        if (channel == null) {
            channel = new NotificationChannel(channel_id, "Channel Title", NotificationManager.IMPORTANCE_HIGH);
            // 채널 설정
            channel.setDescription("[Channel description]");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 1000, 200, 340});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(getApplicationContext(), NotificationActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSmallIcon(R.drawable.splash)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.splash))
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .setBigContentTitle(big)
                        .setSummaryText(summary));
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
            return;
        }

        m.notify(1, builder.build());
    }

    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);

        Log.d("MainActivity", "MainActivity-onResume()");

        if (mBluetoothAdapter.isEnabled()) {
            window.setStatusBarColor(Color.parseColor("#4CAF50"));
            toolbar.setBackgroundColor(Color.parseColor("#4CAF50"));
        } else if (!mBluetoothAdapter.isEnabled()) {
            window.setStatusBarColor(Color.parseColor("#FF9800"));
            toolbar.setBackgroundColor(Color.parseColor("#FF9800"));
        }
        startLeScan();
    }

    protected void onDestroy(){
        super.onDestroy();
        Log.d("MainActivity", "MainActivity-onDestroy()");

        stopRSSIMeasurement();          // RSSI 측정 중지
        stopLeScan();
    }
}