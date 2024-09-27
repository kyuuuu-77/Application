package com.arduino.Application.ui.info;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
    Button Btn_reset;

    TextView autoSearch;
    TextView battMain;
    TextView battSub;
    TextView rssiMain;
    TextView rssiSub;
    TextView securityMain;
    TextView securitySub;
    TextView bleMain;
    TextView bleSub;

    ImageView Search_icon;
    ImageView RSSI_icon;
    ImageView Bat_icon;
    ImageView Security_icon;
    ImageView BT_icon;

    InfoViewModel infoViewModel;
    private FragmentInfoBinding binding;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        infoViewModel = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 버튼, 텍스트뷰 및 아이콘 선언
        Btn_reset = root.findViewById(R.id.reset);

        autoSearch = root.findViewById(R.id.auto_search);
        battMain = root.findViewById(R.id.batteryText);
        battSub = root.findViewById(R.id.batteryVoltage);
        rssiMain = root.findViewById(R.id.rssi_signal);
        rssiSub = root.findViewById(R.id.rssi_sub);
        securityMain = root.findViewById(R.id.security);
        securitySub = root.findViewById(R.id.security_sub);
        bleMain = root.findViewById(R.id.text_info);
        bleSub = root.findViewById(R.id.ble_device);

        Bat_icon = root.findViewById(R.id.battery);
        RSSI_icon = root.findViewById(R.id.rssi_icon);
        Search_icon = root.findViewById(R.id.search_icon);
        Security_icon = root.findViewById(R.id.security_icon);
        BT_icon = root.findViewById(R.id.bt_icon);

        // ViewModel 선언
        // 자동검색 상태
        infoViewModel.getAutoSearchLiveData().observe(getViewLifecycleOwner(), search -> {
            if (search) {      // 자동 검색이 켜져 있으면
                Search_icon.setImageResource(R.drawable.info_search_on);
                autoSearch.setText("자동검색 켜짐");
            } else {        // 자동 검색이 꺼져 있으면
                Search_icon.setImageResource(R.drawable.info_search_off);
                autoSearch.setText("자동검색 꺼짐");
            }
        });

        // 신호세기
        infoViewModel.getRssiLiveData().observe(getViewLifecycleOwner(), rssi -> {
            if (rssi == 999) {       // rssi 값을 측정할 수 없는 경우
                rssiMain.setText("측정불가");
                rssiSub.setText("신호 없음");
                RSSI_icon.setImageResource(R.drawable.info_rssi_off);
            } else {    // rssi 값을 측정하고 있는 경우
                rssiMain.setText(rssi + " dBm");
                rssiSub.setText("신호 측정중");
                RSSI_icon.setImageResource(R.drawable.info_rssi_on);
            }
        });

        // 배터리 상태
        infoViewModel.getBatteryLiveData().observe(getViewLifecycleOwner(), batt -> {
            if (batt == -1) {
                battMain.setText("정보없음");
                Bat_icon.setImageResource(R.drawable.info_bat_unknown);
            } else {
                if (batt == 999) {
                    battMain.setText("충전중");
                    Bat_icon.setImageResource(R.drawable.info_bat_charging);
                } else {
                    battMain.setText(batt + " %");
                    if (batt >= 80) {
                        Bat_icon.setImageResource(R.drawable.info_bat_full);
                    } else if (batt >= 60) {
                        Bat_icon.setImageResource(R.drawable.info_bat_normal);
                    } else if (batt >= 40) {
                        Bat_icon.setImageResource(R.drawable.info_bat_low);
                    } else {
                        Bat_icon.setImageResource(R.drawable.info_bat_very_row);
                    }
                }
            }
        });
        infoViewModel.getbatteryVoltLiveData().observe(getViewLifecycleOwner(), voltage -> {
            if (voltage == -1) {
                battSub.setText("NULL V");
            } else {
                battSub.setText(voltage + " V");
            }
        });

        // 도난방지 상태
        infoViewModel.getSecurityLiveData().observe(getViewLifecycleOwner(), security -> {
            if (security) {      // 도난 방지가 켜져 있으면
                securityMain.setText("사용중");
                securitySub.setText("도난방지 켜짐");
                Security_icon.setImageResource(R.drawable.info_security_on);
            } else {        // 도난 방지가 꺼져 있으면
                securityMain.setText("사용안함");
                securitySub.setText("도난방지 꺼짐");
                Security_icon.setImageResource(R.drawable.info_security_off);
            }
        });

        // 블루투스 상태
        infoViewModel.getbleStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case -1:
                    bleMain.setText("사용불가");
                    BT_icon.setImageResource(R.drawable.info_bt_off);
                    break;
                case 0:
                    bleMain.setText("비활성화");
                    BT_icon.setImageResource(R.drawable.info_bt_off);
                    infoViewModel.setdeviceName("블루투스 꺼짐");
                    break;
                case 1:
                    bleMain.setText("활성화");
                    BT_icon.setImageResource(R.drawable.info_bt_on);
                    infoViewModel.setdeviceName("블루투스 켜짐");
                    break;
                case 2:
                    bleMain.setText("통신오류");
                    BT_icon.setImageResource(R.drawable.info_bt_on);
                    infoViewModel.setdeviceName("블루투스 켜짐");
                    break;
                case 9:
                    bleMain.setText("연결됨");
                    BT_icon.setImageResource(R.drawable.info_bt_connect);
                    break;
            }
        });
        infoViewModel.getdeviceNameLiveData().observe(getViewLifecycleOwner(), name -> bleSub.setText(name));

        // 버튼 이벤트 리스너
        // 설정 초기화 버튼
        Btn_reset.setOnClickListener(view -> {
            // 자동 검색, 도난방지, 도난방지 무시, 무게설정, 도착 시각 초기화
            resetSettings();
            checkAll();
            Toast.makeText(getActivity(), "설정 초기화 완료!", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    // 배터리 상태를 확인하는 메서드
    private void checkBattery() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.checkBattery();
        }
    }

    // RSSI 측정여부를 표시하는 메서드
    private void checkRssi() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.checkRssi();
        }
    }

    // 자동 검색 사용여부 표시하는 메서드
    private void checkAutoSearch() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.checkAutoSearch();
        }
    }

    // 도난방지 여부를 표시하는 메서드
    private void checkSecurity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.checkSecurity();
        }
    }

    // 연결 상태를 확인하는 메서드
    private void checkConnection() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.checkConnection();
        }
    }

    // 설정을 초기화하는 메서드
    private void resetSettings() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.resetSettings();
        }
    }

    // 캐리어 상태를 갱신하는 메서드
    private void checkAll() {
        checkBattery();
        checkRssi();
        checkAutoSearch();
        checkSecurity();
        checkConnection();
    }

    public void onResume(){
        super.onResume();
        Log.d("Info Fragment", "Info Fragment-onResume()");

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
            checkAll();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                handler.post(() -> Toast.makeText(getActivity(), "데이터 로드중 에러 발생", Toast.LENGTH_SHORT).show());
            }

            handler.post(() -> {
                loadingOverlay.setVisibility(View.GONE);
                lottieView.cancelAnimation();
                lottieView.setVisibility(View.GONE);
                Btn_reset.setEnabled(true);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Info Fragment", "Info Fragment-onDestroyView()");
    }
}