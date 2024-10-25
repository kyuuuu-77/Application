package com.arduino.Application.ui.info;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentInfoBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InfoFragment extends Fragment {

    // 버튼, 텍스트뷰 및 아이콘 초기화
    Button Btn_update;
    Button Btn_reset;

    TextView Text_autoSearch;
    TextView Text_battMain;
    TextView Text_battSub;
    TextView Text_rssiMain;
    TextView Text_rssiSub;
    TextView Text_securityMain;
    TextView Text_securitySub;
    TextView Text_bleMain;
    TextView Text_bleSub;

    ImageView Icon_search;
    ImageView Icon_RSSI;
    ImageView Icon_battery;
    ImageView Icon_security;
    ImageView Icon_BT;

    private FragmentInfoBinding binding;
    private InfoViewModel infoViewModel;
    MainActivity mainActivity;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        infoViewModel = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity) getActivity();

        // 버튼, 텍스트뷰 및 아이콘 선언
        Btn_update = root.findViewById(R.id.updateInfo);
        Btn_reset = root.findViewById(R.id.reset);

        Text_autoSearch = root.findViewById(R.id.auto_search);
        Text_battMain = root.findViewById(R.id.batteryText);
        Text_battSub = root.findViewById(R.id.batteryVoltage);
        Text_rssiMain = root.findViewById(R.id.rssi_signal);
        Text_rssiSub = root.findViewById(R.id.rssi_sub);
        Text_securityMain = root.findViewById(R.id.security);
        Text_securitySub = root.findViewById(R.id.security_sub);
        Text_bleMain = root.findViewById(R.id.text_info);
        Text_bleSub = root.findViewById(R.id.ble_device);

        Icon_battery = root.findViewById(R.id.battery);
        Icon_RSSI = root.findViewById(R.id.rssi_icon);
        Icon_search = root.findViewById(R.id.search_icon);
        Icon_security = root.findViewById(R.id.security_icon);
        Icon_BT = root.findViewById(R.id.bt_icon);

        // ViewModel 선언
        // 자동검색 상태 (Boolean)
        infoViewModel.getAutoSearchLiveData().observe(getViewLifecycleOwner(), search -> {
            if (search) {      // 자동 검색이 켜져 있으면
                Icon_search.setImageResource(R.drawable.info_search_on);
                Text_autoSearch.setText("자동검색 켜짐");
            } else {        // 자동 검색이 꺼져 있으면
                Icon_search.setImageResource(R.drawable.info_search_off);
                Text_autoSearch.setText("자동검색 꺼짐");
            }
        });

        // 신호세기 (Integer)
        infoViewModel.getRssiLiveData().observe(getViewLifecycleOwner(), rssi -> {
            if (rssi == 999) {       // rssi 값을 측정할 수 없는 경우
                Text_rssiMain.setText("측정불가");
                Text_rssiSub.setText("신호 없음");
                Icon_RSSI.setImageResource(R.drawable.info_rssi_off);
            } else {    // rssi 값을 측정하고 있는 경우
                Text_rssiMain.setText(rssi + " dBm");
                Text_rssiSub.setText("신호 측정중");
                Icon_RSSI.setImageResource(R.drawable.info_rssi_on);
            }
        });

        // 배터리 상태 (Integer)
        infoViewModel.getBatteryLiveData().observe(getViewLifecycleOwner(), batt -> {
            if (batt == -1) {
                Text_battMain.setText("정보없음");
                Icon_battery.setImageResource(R.drawable.info_bat_unknown);
            } else {
                if (batt == 999) {
                    Text_battMain.setText("충전중");
                    Icon_battery.setImageResource(R.drawable.info_bat_charging);
                } else {
                    Text_battMain.setText(batt + " %");
                    if (batt >= 80) {
                        Icon_battery.setImageResource(R.drawable.info_bat_full);
                    } else if (batt >= 60) {
                        Icon_battery.setImageResource(R.drawable.info_bat_normal);
                    } else if (batt >= 40) {
                        Icon_battery.setImageResource(R.drawable.info_bat_low);
                    } else {
                        showBatteryWarning();
                        Icon_battery.setImageResource(R.drawable.info_bat_very_row);
                    }
                }
            }
        });

        // 배터리 전압 상태를 표시 (Integer)
        infoViewModel.getbatteryVoltLiveData().observe(getViewLifecycleOwner(), voltage -> {
            if (voltage == -1) {
                Text_battSub.setText("NULL V");
            } else {
                Text_battSub.setText("전압 : " + String.format("%.2f", voltage) + " V");
            }
        });

        // 도난방지 상태 (Boolean)
        infoViewModel.getSecurityLiveData().observe(getViewLifecycleOwner(), security -> {
            if (security) {      // 도난 방지가 켜져 있으면
                Text_securityMain.setText("사용중");
                Text_securitySub.setText("도난방지 켜짐");
                Icon_security.setImageResource(R.drawable.info_security_on);
            } else {        // 도난 방지가 꺼져 있으면
                Text_securityMain.setText("사용안함");
                Text_securitySub.setText("도난방지 꺼짐");
                Icon_security.setImageResource(R.drawable.info_security_off);
            }
        });

        // 블루투스 상태 (Boolean)
        infoViewModel.getbleStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case -1:
                    Text_bleMain.setText("사용불가");
                    Icon_BT.setImageResource(R.drawable.info_bt_off);
                    break;
                case 0:
                    Text_bleMain.setText("비활성화");
                    Icon_BT.setImageResource(R.drawable.info_bt_off);
                    infoViewModel.setdeviceName("블루투스 꺼짐");
                    break;
                case 1:
                    Text_bleMain.setText("활성화");
                    Icon_BT.setImageResource(R.drawable.info_bt_on);
                    infoViewModel.setdeviceName("블루투스 켜짐");
                    break;
                case 2:
                    Text_bleMain.setText("통신오류");
                    Icon_BT.setImageResource(R.drawable.info_bt_on);
                    infoViewModel.setdeviceName("블루투스 켜짐");
                    break;
                case 9:
                    Text_bleMain.setText("연결됨");
                    Icon_BT.setImageResource(R.drawable.info_bt_connect);
                    break;
            }
        });

        // BLE 디바이스 이름 (String)
        infoViewModel.getdeviceNameLiveData().observe(getViewLifecycleOwner(), name -> Text_bleSub.setText(name));

        // 버튼 이벤트 리스너
        // 설정 초기화 버튼
        Btn_reset.setOnClickListener(view -> {
            // 자동 검색, 도난방지, 도난방지 무시, 무게설정, 도착 시각 초기화
            resetSettings();
            checkAll();
            Toast.makeText(getActivity(), "애플리케이션 설정 초기화 중", Toast.LENGTH_SHORT).show();
        });
        // 화면 갱신 버튼
        Btn_update.setOnClickListener(view -> {
            checkAll();
            Toast.makeText(getActivity(), "데이터 갱신중", Toast.LENGTH_SHORT).show();
        });
        return root;
    }

    // 배터리가 부족할 경우 경고 다이얼로그를 띄우는 메서드
    private void showBatteryWarning() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.custom_dialog_battery, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();
    }

    // 배터리 상태를 확인하는 메서드
    private void checkBattery() {
        if (mainActivity != null) {
            mainActivity.checkBattery();
        }
    }

    // RSSI 측정여부를 표시하는 메서드
    private void checkRssi() {
        if (mainActivity != null) {
            mainActivity.checkRssi();
        }
    }

    // 자동 검색 사용여부 표시하는 메서드
    private void checkAutoSearch() {
        if (mainActivity != null) {
            mainActivity.checkAutoSearch();
        }
    }

    // 도난방지 여부를 표시하는 메서드
    private void checkSecurity() {
        if (mainActivity != null) {
            mainActivity.checkSecurity();
        }
    }

    // 연결 상태를 확인하는 메서드
    private void checkConnection() {
        if (mainActivity != null) {
            mainActivity.checkConnection();
        }
    }

    // 설정을 초기화하는 메서드
    private void resetSettings() {
        if (mainActivity != null) {
            mainActivity.resetSettings();
        }
    }

    // 캐리어 상태를 갱신하는 메서드
    private void checkAll() {
        View root = binding.getRoot();
        // 로딩 애니메이션 (로티 애니메이션) 및 비동기 처리 구문
        LottieAnimationView lottieView = root.findViewById(R.id.lottieView);
        View loadingOverlay = root.findViewById(R.id.loading_overlay);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            loadingOverlay.setVisibility(View.VISIBLE);
            lottieView.setVisibility(View.VISIBLE);
            lottieView.playAnimation();
            Btn_reset.setEnabled(false);
        });
        executorService.execute(() -> {
            // 백그라운드 작업 처리
            checkBattery();
            checkRssi();
            checkAutoSearch();
            checkSecurity();
            checkConnection();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                handler.post(() -> Toast.makeText(getActivity(), "로드중 에러 발생", Toast.LENGTH_SHORT).show());
            }

            handler.post(() -> {
                loadingOverlay.setVisibility(View.GONE);
                lottieView.cancelAnimation();
                lottieView.setVisibility(View.GONE);
                Btn_reset.setEnabled(true);
            });
        });
    }

    public void onResume() {
        super.onResume();
        checkAll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}