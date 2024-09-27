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
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.arduino.Application.ui.bagDrop.BagDropViewModel;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.arduino.Application.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Calendar;
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
    private BluetoothGatt bluetoothGatt;        // Gatt = Generic Attribute Profile
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;

    // 블루투스 통신 요청에 사용되는 final 변수들
    final int BT_REQUEST_ENABLE = 1;
    final int BT_REQUEST_DISABLE = 3;
    final int REQUEST_LOCATION_PERMISSION = 123;
    private static final int SINGLE_PERMISSION = 1004;

    // viewModel 사용을 위한 변수
    private HomeViewModel viewModel_home;
    private FindViewModel viewModel_find;
    private WeightViewModel viewModel_weight;
    private BagDropViewModel viewModel_bagDrop;
    private InfoViewModel viewModel_info;

    // 프로그램 동작을 위한 전역 변수
    private Boolean isDialogShowing = false;
    private boolean isSuitcase = false;
    private boolean onAutoSearch = true;
    private boolean rssiSignal = false;
    private boolean security = false;
    private boolean alreadyConnected = false;
    private boolean checkDialog = false;
    private boolean isFirstRssi = true;
    private boolean ignoreSecurity = false;
    private boolean backDropMode = false;
    private boolean bleAlreadyChecked = false;
    private final double[] weight = {0.0, 0.0};   // weight, set
    private String data;
    private String deviceName = null;
    private int BLE_status = 0;
    private int rssi_global = 99;
    private int firstRssi = 99;
    private int setHourMin = -1;

    // 윈도우 및 툴바 관련 변수
    Window window;
    Toolbar toolbar;
    Menu appMenu;

    // 오버레이
    private static final int REQUEST_OVERLAY_PERMISSION = 1;
    private WindowManager windowManager;
    private View overlayView;
    private Boolean isOverlayShowing = false;

    // 기능 메서드중에 usages가 1인 메서드 (즉 하나의 프레그먼트에서만 사용되는 메서드)인 경우에는
    // Fragment에 기능을 구현할 수 있도록 노력

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint({"HandlerLeak", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "MainActivity-onCreate()");

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 툴바 및 윈도우 설정
        toolbar = findViewById(R.id.toolbar);
        window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // 액티비티의 앱바(App Bar)로 지정
        setSupportActionBar(toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // 앱서랍 설정
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_find, R.id.nav_weight, R.id.nav_bagdrop, R.id.nav_info)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        appMenu = navigationView.getMenu();

        // 필수 퍼미션 리스트 배열
        String[] permission_list = {
                Manifest.permission.BLUETOOTH_CONNECT,      // 블루투스 연결 권한
                Manifest.permission.BLUETOOTH_SCAN,         // 블루투스 검색 권한
                Manifest.permission.BLUETOOTH_ADMIN,        // 블루투스 어드민 권한
                Manifest.permission.ACCESS_COARSE_LOCATION  // 위치 검색 권한
        };
        ActivityCompat.requestPermissions(MainActivity.this, permission_list, 1);

        // 블루투스 어댑터 및 매니저 선언
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // 각 Fragment의 viewModel 정의
        viewModel_home = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel_weight = new ViewModelProvider(this).get(WeightViewModel.class);
        viewModel_find = new ViewModelProvider(this).get(FindViewModel.class);
        viewModel_bagDrop = new ViewModelProvider(this).get(BagDropViewModel.class);
        viewModel_info = new ViewModelProvider(this).get(InfoViewModel.class);

        checkOverlayPermission();
        checkAlertPermission();
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
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    // 오버레이 퍼미션을 체크하는 메서드
    private void checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "오버레이 권한이 없음", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        }
    }

    // 알림 퍼미션을 체크하는 메서드
    private void checkAlertPermission() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    // toolbar(자동검색,앱정보)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id==R.id.action_settings) {
            showAutoSearchDialog();
        } else if (id == R.id.app_info) {
            String version = getString(R.string.app_version);
            String date = getString(R.string.app_date);
            String update = getString(R.string.app_update_log);
            String message = String.format("애플리케이션 버전 -> %s\n버전 날짜 -> %s\n업데이트 내역 -> %s", version, date, update);
            new AlertDialog.Builder(this)
                    .setTitle("앱 정보")
                    .setMessage(message)
                    .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 자동검색 팝업창을 띄우는 메서드
    private void showAutoSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("자동 검색")
                .setMessage("자동 검색을 사용할까요?")
                .setPositiveButton("사용", (dialog, which) -> {
                    onAutoSearch = true;
                    viewModel_info.setAutoSearch(true);
                    Toast.makeText(this, "자동 검색이 켜졌습니다!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("사용안함", ((dialog, which) -> {
                    onAutoSearch = false;
                    viewModel_info.setAutoSearch(false);
                    Toast.makeText(this, "자동 검색이 꺼집니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }))
                .show();
    }

    // 블루투스를 켜는 메서드 -> SDK 31 이상 (안드로이드 12 이상)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void BT_on() {
        if (mBluetoothAdapter == null) {
            viewModel_home.setBluetoothStatus("블루투스 사용 불가능");
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기종입니다.", Toast.LENGTH_SHORT).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                viewModel_home.setBluetoothStatus("블루투스 활성화");
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화되어 있습니다", Toast.LENGTH_SHORT).show();
            } else {
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
            viewModel_home.setBluetoothStatus("블루투스 사용 불가능");
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기종입니다", Toast.LENGTH_SHORT).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                viewModel_home.setBluetoothStatus("블루투스 활성화");
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화되어 있습니다", Toast.LENGTH_SHORT).show();
            } else {
                mBluetoothAdapter.enable();

                window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));
                whenBTOn();
            }
        }
    }

    // 블루투스를 끄는 메서드 -> SDK 31 이상 (안드로이드 12 이상)
    @RequiresApi(api = Build.VERSION_CODES.S)
    public void BT_off() {
        if (mBluetoothAdapter.isEnabled()) {
            checkPermission();
            Intent intentBluetoothDisable = new Intent("android.bluetooth.adapter.action.REQUEST_DISABLE");
            startActivityForResult(intentBluetoothDisable, BT_REQUEST_DISABLE);
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

            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
            toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
            whenBTOff();
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 블루투스가 켜졌을 때 동장
    private void whenBTOn() {
        onAutoSearch = true;

        runOnUiThread(() -> {
            viewModel_home.setBluetoothStatus("블루투스 활성화");
            viewModel_home.setHomeText("캐리어에 연결되지 않음");
            viewModel_home.setBtBtn("블루투스 끄기");
            viewModel_home.setConnectBtn("연결");
            viewModel_info.setAutoSearch(true);
            Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_SHORT).show();
        });
    }

    // 블루투스가 꺼졌을 때 동작
    private void whenBTOff() {
        onAutoSearch = false;
        BLE_status = 0;
        deviceName = null;

        runOnUiThread(() -> {
            viewModel_home.setBluetoothStatus("블루투스 비활성화");
            viewModel_home.setHomeText("캐리어에 연결되지 않음");
            viewModel_home.setBtBtn("블루투스 켜기");
            viewModel_home.setConnectBtn("연결 불가");
            viewModel_info.setdeviceName("X");
            viewModel_info.setAutoSearch(true);
            viewModel_info.setBleStatus(0);
            Toast.makeText(getApplicationContext(), "블루투스 비활성화", Toast.LENGTH_SHORT).show();
        });
    }

    // 블루투스 조작 및 오버레이 권한 요청시 동작 수행 메서드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 블루투스 활성화의 경우
        if (requestCode == BT_REQUEST_ENABLE) {
            if (resultCode == RESULT_OK) {
                whenBTOn();
                setUIColor();
            } else if (resultCode == RESULT_CANCELED) {
                runOnUiThread(() -> {
                    viewModel_home.setBluetoothStatus("블루투스 비활성화");
                    viewModel_home.setBtBtn("블루투스 켜기");
                    viewModel_home.setConnectBtn("연결 불가");
                    Toast.makeText(getApplicationContext(), "취소됨", Toast.LENGTH_SHORT).show();
                });
            }
        } else if (requestCode == BT_REQUEST_DISABLE) {     // 블루투스 비활성화의 경우
            if (resultCode == RESULT_OK) {
                stopRSSIMeasurement();          // RSSI 측정 중지

                whenBTOff();
                setUIColor();
            } else if (resultCode == RESULT_CANCELED) {
                runOnUiThread(() -> {
                    viewModel_home.setBluetoothStatus("블루투스 활성화");
                    viewModel_home.setBtBtn("블루투스 끄기");
                    viewModel_home.setConnectBtn("연결");
                    Toast.makeText(getApplicationContext(), "취소됨", Toast.LENGTH_SHORT).show();
                });
            }
        }

        // 오버레이 허가 요청의 경우
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "오버레이 권한이 부여됨", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "오버레이 권한이 없음", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 오버레이를 표시하는 메서드
    @SuppressLint("InflateParams")
    private void showOverlay() {
        if (!isOverlayShowing && !ignoreSecurity) {
            checkOverlayPermission();

            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            // 오버레이 레이아웃 설정
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,    // 오버레이 타입 (API 26 이상)
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,          // 포커스 불가능 설정
                    PixelFormat.TRANSLUCENT     // 투명도 설정
            );

            // 오버레이로 표시할 레이아웃을 인플레이트
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            overlayView = inflater.inflate(R.layout.overlay_main, null);

            // 윈도우 매니저를 사용하여 오버레이 뷰 추가
            windowManager.addView(overlayView, layoutParams);
            isOverlayShowing = true;

            // 오버레이의 버튼 초기화 및 선언
            Button overlayBtnCheck = overlayView.findViewById(R.id.overlay_check);
            Button overlayBtnIgnore = overlayView.findViewById(R.id.overlay_ignore);

            // 확인 버튼을 누른 경우
            overlayBtnCheck.setOnClickListener(v -> {
                checkOverlayPermission();
                if (Settings.canDrawOverlays(MainActivity.this)) {
                    ringBell(false);
                    Toast.makeText(this, "캐리어를 계속 확인합니다.", Toast.LENGTH_SHORT).show();
                    removeOverlay();
                }
            });

            // 무시 버튼을 누른 경우
            overlayBtnIgnore.setOnClickListener(v -> {
                checkOverlayPermission();
                if (Settings.canDrawOverlays(MainActivity.this)) {
                    ringBell(false);
                    ignoreSecurity = true;
                    viewModel_find.setIgnoreText("무시");
                    Toast.makeText(this, "도난방지 경고를 무시합니다.", Toast.LENGTH_SHORT).show();
                    removeOverlay();
                }
            });
        }
    }

    // 오버레이를 지우는 메서드
    private void removeOverlay() {
        if (overlayView != null) {
            windowManager.removeView(overlayView);      // 오버레이 제거

            overlayView = null;
            isOverlayShowing = false;
        }
    }

    // 블루투스 디바이스 목록을 보여주는 메서드
    public void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {        // 블루투스가 켜져 있을때
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if (!mPairedDevices.isEmpty()) {        // 페어링 가능한 장치가 있을때
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("디바이스 선택");
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
            } else {        // 페어링 가능한 장치가 없을때
                Toast.makeText(getApplicationContext(),"페어링 된 디바이스가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {            // 블루투스가 꺼져 있을때
            Toast.makeText(getApplicationContext(),"블루투스가 비활성화되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 자동 검색 후 스마트 캐리어에 연결하는 메서드
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

        isSuitcase = Objects.equals(selectedDeviceName, "FB301(73F06C)");
        bluetoothGatt = mBluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
    }

    // 주변의 BLE 디바이스를 스캔
    public void startLeScan() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()){
            Toast.makeText(this, "블루투스가 꺼져있어 자동 검색을 수행할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        // 리스캐너 사용할 수 없을때
        if (bluetoothLeScanner == null) {
            Toast.makeText(this, "BluetoothLeScanner 초기화 오류", Toast.LENGTH_LONG).show();
            return;
        }

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                mBluetoothDevice = result.getDevice();

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
                Log.e("ScanFailed", "스캔 실패 -> 에러코드 : " + errorCode);
            }
        };

        // BLE 스캔 시작
        bluetoothLeScanner.startScan(scanCallback);
    }

    // BLE 스캔 중지
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
                    Auto_onnectSelectedDevice(device);
                    stopLeScan();
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    isDialogShowing = false;
                    onAutoSearch = false;
                    viewModel_info.setAutoSearch(false);
                    dialog.dismiss();
                    stopLeScan();
                })
                .setCancelable(false).show();
    }

    // 백드랍 모드의 다이얼로그 메서드
    private void showBagDropDialog() {
        while(true) {
            int tmpBell = ringBell(true);
            if (tmpBell == 1) break;
            SystemClock.sleep(500);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("캐리어가 근처에 있습니다!")
                .setMessage("캐리어에 알람이 울리고 있습니다.\n캐리어를 육안으로 확인한 후 확인 버튼을 누르세요.")
                .setPositiveButton("확인", (dialog, which) -> {
                    ringBell(false);
                    runOnUiThread(() -> {
                        backDropMode = false;
                        viewModel_home.setHomeText("스마트 캐리어에 연결됨");
                        viewModel_bagDrop.setBagDropText("백드랍 비활성화");
                        viewModel_bagDrop.setBagDropBtnText("백드랍 모드 시작");
                        viewModel_bagDrop.setRemainTimeText("null");
                        createNotif("bagdrop", "백드랍 모드 종료", "스마트 캐리어와 연결되었습니다!\n이제 백드랍 모드를 종료합니다.");
                        Toast.makeText(MainActivity.this, "캐리어와 다시 연결되었으므로 백드랍 모드를 종료합니다.", Toast.LENGTH_SHORT).show();
                    });
                })
                .setCancelable(false).show();
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
            handler_RSSI.removeCallbacks(runnable_RSSI);
            bluetoothGatt = null;
            rssiSignal = false;
        }
    }

    // 5초마다 BLE 재연결을 시도하는 Handler와 Runnable
    private final Handler reconnectHandler = new Handler();
    private final Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (bluetoothGatt != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }
                bluetoothGatt.connect();
            }
            reconnectHandler.postDelayed(this, 5000);
        }
    };

    // 스마트 캐리어와 연결 되었을 때
    private void whenBTConnect() {
        alreadyConnected = true;
        checkDialog = true;
        BLE_status = BluetoothGatt.STATE_CONNECTED;

        runOnUiThread(() -> {
            createNotif("connect", "캐리어와 연결됨", "스마트 캐리어와 연결되었습니다!");
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.indigo_500));
            toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.indigo_500));

            viewModel_home.setHomeText("스마트 캐리어에 연결됨");
            viewModel_home.setConnectBtn("연결됨");
            viewModel_bagDrop.setConnectText("연결됨");
            viewModel_info.setBleStatus(9);
            Toast.makeText(getApplicationContext(), "스마트 캐리어에 연결됨", Toast.LENGTH_SHORT).show();
        });
    }

    // 스마트 캐리어와 연결이 해제 되었을 때
    private void whenBTDisconnect() {
        BLE_status = BluetoothGatt.STATE_DISCONNECTED;
        rssiSignal = false;
        deviceName = null;
        security = false;
        alreadyConnected = false;

        runOnUiThread(() -> {
            createNotif("disconnect", "캐리어와 연결 끊김", "스마트 캐리어와 연결이 끊겼습니다.");
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));
            toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));

            // 뷰모델 초기화
            viewModel_home.setHomeText("캐리어와 연결이 끊어졌습니다");
            viewModel_home.setConnectBtn("연결");
            viewModel_find.setAlertStatus("도난방지 꺼짐");
            viewModel_find.setAlertBtnText("도난방지 사용불가");
            viewModel_find.setDistance("캐리어와의 거리");
            viewModel_weight.setWeightBtn("무게 측정 불가");
            viewModel_bagDrop.setConnectText("연결되지 않음");
            viewModel_info.setdeviceName("X");
            viewModel_info.setRssi(999);
            viewModel_info.setSecurity(false);
            viewModel_info.setBleStatus(1);
            Toast.makeText(getApplicationContext(), "디바이스와의 연결이 끊어졌습니다", Toast.LENGTH_SHORT).show();
        });
    }

    // BLE 통신을 위한 BluetoothGatt 객체 생성
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {        // 블루투스 디바이스와 연결된 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkPermission();
                }
                if (backDropMode) {     // 백드롭 모드일 때
                    bagDropHandler.removeCallbacks(bagDropRunnable);

                    bleAlreadyChecked = false;
                    runOnUiThread(() -> {
                        appMenu.findItem(R.id.nav_find).setEnabled(true);
                        appMenu.findItem(R.id.nav_weight).setEnabled(true);
                        appMenu.findItem(R.id.nav_info).setEnabled(true);
                        showBagDropDialog();
                    });
                } else if (isSuitcase) {        // 스마트 캐리어일 때
                    reconnectHandler.removeCallbacks(reconnectRunnable);

                    whenBTConnect();
                } else {        // 스마트 캐리어가 아닐때
                    BLE_status = -1;
                    runOnUiThread(() -> {
                        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));
                        toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));

                        viewModel_home.setHomeText("잘못된 디바이스에 연결됨");
                        viewModel_home.setConnectBtn("연결");
                        Toast.makeText(getApplicationContext(), "연결된 디바이스는 스마트 캐리어가 아닙니다.", Toast.LENGTH_SHORT).show();
                    });
                }

                deviceName = gatt.getDevice().getName();
                runOnUiThread(() -> viewModel_info.setdeviceName(deviceName));
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {      // 블루투스 디바이스와 연결이 끊긴 경우
                if (backDropMode) {     // 백드랍 모드일 경우
                    if (!bleAlreadyChecked) {
                        bleAlreadyChecked = true;
                        security = false;
                        handler_RSSI.removeCallbacks(runnable_RSSI);
                        bluetoothGatt.disconnect();
                        runOnUiThread(() -> {
                            createNotif("bagdrop", "백드랍 모드 동작중", "백드랍 모드가 동작중입니다.\n도착 예정시각까지 캐리어와 연결을 끊습니다.");
                            viewModel_home.setHomeText("백드랍 모드 사용중");
                            Toast.makeText(getApplicationContext(), "백드랍 모드가 계속 동작중입니다!", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else if (mBluetoothAdapter.isEnabled() && alreadyConnected) {
                    handler_RSSI.removeCallbacks(runnable_RSSI);
                    bluetoothGatt.disconnect();
                    reconnectHandler.postDelayed(reconnectRunnable, 5000);
                    whenBTDisconnect();
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
                        Log.d("onServicesDiscovered", "Descriptor가 null값을 가지고 있음");
                    }
                } else {
                    Log.d("onServicesDiscovered", "서비스가 null값을 가지고 있음");
                }
            }
        }

        // 데이터 수신 메서드
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(READ_CHAR_UUID)) {
                byte[] value = characteristic.getValue();
                String receivedData = new String(value);
                data = receivedData;
                Log.d("Received data", "받은 데이터 : "+ receivedData);
            }
        }

        // 데이터 송신 확인 메서드
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Send data", "데이터 송신 성공");
            }
        }

        // RSSI 값 측정 메서드
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            rssi_global = rssi;
            if (isFirstRssi) {      // RSSI값이 다르게 측정되는 기기를 위해서
                isFirstRssi = false;
                firstRssi = rssi_global;
            }

            if (!security) {        // 도난 방지가 꺼져 있으면
                runOnUiThread(() -> viewModel_find.setDistance("캐리어와의 거리"));
            } else {                // 도난 방지가 켜져 있으면
                if (firstRssi > -12) {
                    runOnUiThread(() -> {
                        if (rssi_global > -5) {
                            viewModel_find.setDistance("캐리어와 매우 가까움");
                        } else if (rssi_global > -15) {
                            viewModel_find.setDistance("캐리어와 가까움");
                        } else if (rssi_global > -25) {
                            viewModel_find.setDistance("캐리어와 떨어져 있음");
                        } else {
                            viewModel_find.setDistance("캐리어와 멂");
                            if (!ignoreSecurity) {
                                ringBell(true);
                                showOverlay();
                            }
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        if (rssi_global > -50) {
                            viewModel_find.setDistance("캐리어와 매우 가까움");
                        } else if (rssi_global > -75) {
                            viewModel_find.setDistance("캐리어와 가까움");
                        } else if (rssi_global > -95) {
                            viewModel_find.setDistance("캐리어와 떨어져 있음");
                        } else {
                            viewModel_find.setDistance("캐리어와 멂");
                            if (!ignoreSecurity) {
                                ringBell(true);
                                showOverlay();
                            }
                        }
                    });
                }
            }
            handler_RSSI.post(() -> viewModel_info.setRssi(rssi_global));
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

    // 배터리 정보(잔량 및 전압)를 확인하는 메서드
    public void checkBattery() {
        if (writeCharacteristic != null && checkBLE() == BluetoothGatt.STATE_CONNECTED) {
            // 데이터 초기화
            data = null;
            sendData("menu 4");

            checkData();

            runOnUiThread(() -> {
                // 배터리 정보를 받지 못했으면
                if (data == null) {
                    viewModel_info.setBattery(-1);
                    Toast.makeText(getApplicationContext(), "배터리 정보 취득 실패", Toast.LENGTH_SHORT).show();
                } else {    // 수신 데이터 -> "45" = 배터리 잔량이 45%, "45+" 배터리 잔량이 45%이고 충전중임. "100+" 완충됨
                    data = data.trim();
                    if (data.charAt(data.length() - 1) == '+') {      // 충전중이면
                        viewModel_info.setBattery(999);
                    } else {        // 방전중이면
                        viewModel_info.setBattery(Integer.parseInt(data));
                    }
                }
            });

            data = null;
            sendData("menu 5");

            checkData();
            runOnUiThread(() -> {
                if (data == null) {
                    viewModel_info.setBatteryVolt(-1);
                } else {
                    viewModel_info.setBatteryVolt(Double.parseDouble(data));
                }
            });
        } else {
            runOnUiThread(() -> {
                viewModel_info.setBattery(-1);
                viewModel_info.setBatteryVolt(-1);
                Toast.makeText(getApplicationContext(), "배터리 정보 취득 실패", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // 무게를 측정하는 메서드
    @SuppressLint({"DefaultLocale", "HandlerLeak"})
    public double measureWeight(double maxSet) {
        if (writeCharacteristic != null) {
            // 무게값 초기화
            data = null;

            // 동작값을 먼저 전송 -> 캐리어에서 값 인식 후 무게값 전달
            sendData("menu 3");

            checkData();

            // 무게값을 받지 못했으면
            if (data == null) {
                return -1;
            }

            double tmp_weight = Double.parseDouble(data);
            weight[0] = tmp_weight;
            weight[1] = maxSet;
            runOnUiThread(() -> viewModel_weight.setWeightNow(String.format("%.1f", weight[0]) +" Kg"));

            runOnUiThread(() -> {
                if (weight[0] > 32) {                    // 32kg 초과시
                    viewModel_weight.setWeightInfo("32Kg을 초과했습니다.");
                } else if (weight[0] > weight[1]) {      // 허용무게 초과시
                    viewModel_weight.setWeightInfo(String.format("%.1f", weight[0] - weight[1]) + " Kg  초과했습니다.");
                } else {                                // 무게 초과하지 않은 경우
                    viewModel_weight.setWeightInfo("허용 무게를 초과하지 않았습니다.");
                }
            });

            runOnUiThread(() -> viewModel_weight.setWeightBtn("무게 다시 측정"));
            return weight[0];
        } else {
            runOnUiThread(() -> {
                viewModel_weight.setWeightInfo("무게 측정에 실패하였습니다.");
                viewModel_weight.setWeightBtn("무게 측정 실패");
            });
            return -1;
        }
    }

    // RSSI 측정 여부를 확인하는 메서드
    public void checkRssi() {
        runOnUiThread(() -> {
            if (!rssiSignal) {
                viewModel_info.setRssi(999);
            }
        });
    }

    // 자동 검색 여부를 확인하는 메서드
    public void checkAutoSearch() {
        runOnUiThread(() -> {
            viewModel_info.setAutoSearch(onAutoSearch);
        });
    }

    // 도난방지 여부를 확인하는 메서드
    public boolean checkSecurity() {
        runOnUiThread(() -> {
            if (security) {
                viewModel_find.setAlertStatus("도난방지 켜짐");
                viewModel_info.setSecurity(true);
            } else {
                viewModel_find.setAlertStatus("도난방지 꺼짐");
                viewModel_info.setSecurity(false);
            }
        });
        return security;
    }

    // 도난방지 ON 메서드
    public boolean security_ON() {
        checkSecurity();
        security = true;
        createNotif("security", "도난방지 동작", "도난방지 모드가 동작합니다.\n캐리어와 멀어지는 경우 알림을 받을 수 있습니다.");
        Toast.makeText(getApplicationContext(), "도난방지가 켜졌습니다.", Toast.LENGTH_SHORT).show();
        return security;
    }

    // 도난방지 OFF 메서드
    public boolean security_OFF() {
        checkSecurity();

        security = false;
        createNotif("security", "도난방지 동작 안함", "도난방지 모드가 동작하지 않습니다.\n캐리어와 멀어지는 경우 알림을 받을 수 없습니다.");
        Toast.makeText(getApplicationContext(), "도난방지가 꺼졌습니다.", Toast.LENGTH_SHORT).show();
        return security;
    }

    // 도난방지 무시 여부 체크 메서드
    public void checkIgnore() {
        if (!ignoreSecurity) {     // 도난방지 무시가 꺼진 경우
            viewModel_find.setIgnoreText("알림");
        } else {
            viewModel_find.setIgnoreText("무시");
        }
    }

    // 도난방지 무시 설정 메서드
    public void ignoreAlert() {
        ignoreSecurity = !ignoreSecurity;
    }

    // 데이터 전송 여부를 확인하는 메서드
    private void checkData() {
        int cnt = 0;
        while (true) {
            cnt ++;
            SystemClock.sleep(10);
            if (data != null) {
                Log.d("받은 데이터", data);
                break;
            } else if (cnt >= 500){
                Log.d("받은 데이터", "수신 실패");
                break;
            }
        }
    }

    // 벨 울리는 메서드
    public int ringBell(boolean onOff) {
        if (writeCharacteristic != null) {      // 통신이 가능할 때
            if (onOff) {      // 벨 울리기 시작
                // 데이터 값 초기화
                data = null;
                sendData("menu 1");

                checkData();

                // 벨 울리기 실패
                if (data == null) {
                    runOnUiThread(() -> viewModel_find.setAlertText("벨 울리기 실패\n통신 상태를 확인하세요."));
                    return -1;
                } else if (data.trim().equals("ring_suc")) {   // 벨 울리기 성공
                    data = "ring_suc";
                    runOnUiThread(() -> viewModel_find.setAlertText("벨 울리기 성공!"));
                    return 1;
                } else {    // 잘못된 값을 받은 경우
                    runOnUiThread(() -> viewModel_find.setAlertText("벨 울리기 실패\n잘못된 인자값이 전달되었습니다."));
                    return -1;
                }
            } else {   // 벨 울리기 중지 동작
                data = null;
                sendData("menu 2");

                checkData();

                if (data == null) {
                    return -1;
                }
                else if (data.trim().equals("ring_stop")) {   // 벨 울리기 성공
                    data = null;

                    runOnUiThread(() -> viewModel_find.setAlertText("도난방지 기능으로\n캐리어를 안전하게 보관하세요!"));
                    return 2;
                } else {
                    return -1;
                }
            }
        } else {        // 통신이 불가능할 때
            runOnUiThread(() -> viewModel_find.setAlertText("벨 울리기 실패\n통신 상태를 확인하세요."));
            return -1;
        }
    }

    // 연결 상태를 전달하는 메서드
    public int checkConnection() {
        if (mBluetoothAdapter == null) {        // 블루투스를 지원하지 않는 디바이스
            runOnUiThread(() -> viewModel_info.setBleStatus(-1));
            return -2;
        } else if (!mBluetoothAdapter.isEnabled()) {     // 블루투스가 꺼져 있음
            runOnUiThread(() -> viewModel_info.setBleStatus(0));
            return -1;
        } else if (bluetoothGatt == null) {     // 캐리어에 연결되어 있지 않음
            runOnUiThread(() -> viewModel_info.setBleStatus(1));
            return 0;
        } else if (writeCharacteristic == null || readCharacteristic == null) {      // 연결이 되어 있으나 송수신 불가
            runOnUiThread(() -> viewModel_info.setBleStatus(2));
            return 1;
        } else if (checkBLE() == BluetoothGatt.STATE_CONNECTED) {    // 캐리어에 제대로 연결되어 있음
            runOnUiThread(() -> viewModel_info.setBleStatus(9));
            return 9;
        } else {    // 그 외의 경우
            return 0;
        }
    }

    // BLE 연결 상태를 체크하는 메서드
    public int checkBLE() {
        if (BLE_status == BluetoothGatt.STATE_CONNECTED) {
            return BLE_status;
        } else {
            return 0;
        }
    }

    // 무게 설정을 전달하는 메서드
    public double[] checkWeightSetting() {
        return weight;      // weight, set
    }

    // 상단바와 툴바의 색상을 변경하는 메서드
    public void setUIColor() {
        runOnUiThread(() -> {
            if (BLE_status == BluetoothGatt.STATE_CONNECTED) {      // 페어링이 된 경우
                window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.indigo_500));
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.indigo_500));
            } else if (mBluetoothAdapter.isEnabled()) {             // 블루투스가 켜져 있는 경우
                window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green_500));
            } else if (!mBluetoothAdapter.isEnabled()) {            // 블루투스가 꺼져 있는 경우
                window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_500));
            }
        });
    }

    // 설정을 초기화 하는 메서드
    public void resetSettings() {
        // 자동검색, 도난방지, 방해금지, 무게 옵션, 무게 측정값, 시각 설정 초기화
        onAutoSearch = true;
        security = false;
        ignoreSecurity = false;
        setHourMin = -1;
        weight[0] = 0;
        weight[1] = 0;

        viewModel_weight.setWeightNow("-- Kg");
        viewModel_weight.setWeightInfo("무게 초과 여부 표시");
        viewModel_info.setAutoSearch(false);
    }

    // 시간 설정을 저장하는 메서드
    public void setTime(int hour, int min) {
        setHourMin = hour * 100 + min;
    }

    // 시간 설정을 체크하는 메서드
    public int checkTime() {
        return setHourMin;
    }

    // 백드랍 모드를 체크하는 메서드
    public boolean checkBagDrop() {
        return backDropMode;
    }

    // 백드랍 모드를 설정하는 메서드
    public void setBagDrop(boolean onOff) {
        backDropMode = onOff;

        if (backDropMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                checkPermission();
            }
            bluetoothGatt.disconnect();     // 블루투스 연결을 끊고
            bagDropHandler.postDelayed(bagDropRunnable, 10000);     // 핸들러로 동작
            appMenu.findItem(R.id.nav_find).setEnabled(false);
            appMenu.findItem(R.id.nav_weight).setEnabled(false);
            appMenu.findItem(R.id.nav_info).setEnabled(false);
        } else {
            bluetoothGatt.connect();
            bagDropHandler.removeCallbacks(bagDropRunnable);
            appMenu.findItem(R.id.nav_find).setEnabled(true);
            appMenu.findItem(R.id.nav_weight).setEnabled(true);
            appMenu.findItem(R.id.nav_info).setEnabled(true);
        }
    }

    // 백드랍 모드의 동작을 동작시키는 핸들러
    private final Handler bagDropHandler = new Handler();
    private final Runnable bagDropRunnable = new Runnable() {
        @Override
        public void run() {
            // 현재 시각 불러오기
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMin = calendar.get(Calendar.MINUTE);
            int setHour = setHourMin / 100;
            int setMin = setHourMin % 100;

            int currentTime = currentHour * 60 + currentMin;
            int setTime = setHour * 60 + setMin;
            int remain;

            if (currentTime > setTime) {        // 혹시나 도착이 다음날이면 -> 도착 0:05 , 현재 17:50
                remain = setTime + 1440 - currentTime;
            } else {
                remain = setTime - currentTime;
            }

            if (remain / 60 > 0) {      // 1시간 이상 남으면
                viewModel_bagDrop.setRemainTimeText(remain/60 + "시간 " + remain%60 + "분");

            } else {        // 1시간 이하인 경우
                viewModel_bagDrop.setRemainTimeText(remain%60 + "분");
            }

            if (remain <= 10) {     // 10분 전 부터 캐리어 찾기를 시도
                if (bluetoothGatt != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        checkPermission();
                    }
                    bluetoothGatt.connect();
                }
            }
            bagDropHandler.postDelayed(this, 10000);        // 10초마다 반복 동작
        }
    };

    // 알람을 띄우는 메서드
    private void createNotif(String channel_id, String big, String summary) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel(channel_id);
        if (channel == null) {
            channel = new NotificationChannel(channel_id, "캐리어 연결 알림", NotificationManager.IMPORTANCE_HIGH);
            // 채널 설정
            channel.setDescription("캐리어 " + channel_id + " 상태를 알려줍니다.");
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
                .setContentTitle(big).setContentText(summary).setStyle(new NotificationCompat.BigTextStyle().bigText(summary))
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());

        checkAlertPermission();

        m.notify(1, builder.build());
    }

    protected void onResume() {
        super.onResume();
        setSupportActionBar(toolbar);

        Log.d("MainActivity", "MainActivity-onResume()");

        setUIColor();
        startLeScan();
    }

    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "MainActivity-onDestroy()");

        stopRSSIMeasurement();          // RSSI 측정 중지
        stopLeScan();       // 리스캔 중지
        removeOverlay();    // 오버레이 닫기
    }
}