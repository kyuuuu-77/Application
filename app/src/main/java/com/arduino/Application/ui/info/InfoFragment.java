package com.arduino.Application.ui.info;

import android.os.Bundle;
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

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentInfoBinding;

import java.util.Objects;

public class InfoFragment extends Fragment {

    // 버튼, 텍스트뷰 및 아이콘 초기화
    Button mBtn_charge;

    TextView deviceName;
    TextView battText;
    TextView rssiTextView;
    TextView auto_search_status;
    TextView security_status;
    TextView infoText;

    ImageView Bat_icon;
    ImageView RSSI_icon;
    ImageView Search_icon;
    ImageView Security_icon;
    ImageView BT_icon;

    InfoViewModel infoViewModel;

    private FragmentInfoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        infoViewModel = new ViewModelProvider(requireActivity()).get(InfoViewModel.class);

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Info Fragment", "Info Fragment-onCreatedView()");

        // 버튼, 텍스트뷰 및 아이콘 선언
        mBtn_charge = root.findViewById(R.id.reset);

        deviceName = root.findViewById(R.id.ble_device);
        battText = root.findViewById(R.id.batteryText);
        rssiTextView = root.findViewById(R.id.rssi_signal);
        auto_search_status = root.findViewById(R.id.auto_search);
        security_status = root.findViewById(R.id.security);
        infoText = root.findViewById(R.id.text_info);

        Bat_icon = root.findViewById(R.id.battery);
        RSSI_icon = root.findViewById(R.id.rssi_icon);
        Search_icon = root.findViewById(R.id.search_icon);
        Security_icon = root.findViewById(R.id.security_icon);
        BT_icon = root.findViewById(R.id.bt_icon);

        // ViewModel 선언
        infoViewModel.getdeviceNameLiveData().observe(getViewLifecycleOwner(), name -> deviceName.setText(name));
        infoViewModel.getbatteryTextLiveData().observe(getViewLifecycleOwner(), batt -> {
            switch (batt) {
                case "정상":     // 방전중일 경우
                    Bat_icon.setImageResource(R.drawable.info_bat_normal);
                    break;
                case "충전중":     // 충전중일 경우
                    Bat_icon.setImageResource(R.drawable.info_bat_charging);
                    break;
                case "충전됨":     // 완충된 경우
                    Bat_icon.setImageResource(R.drawable.info_bat_full);
                    break;
                case "정보없음":    // 정보 취득에 실패한 경우
                    Bat_icon.setImageResource(R.drawable.info_bat_unknown);
                    break;
            }
            battText.setText(batt);
        });
        infoViewModel.getRssiLiveData().observe(getViewLifecycleOwner(), rssi -> {
            if (Objects.equals(rssi, "RSSI 측정 불가")) {       // rssi 값을 측정할 수 없는 경우
                RSSI_icon.setImageResource(R.drawable.info_rssi_off);
            } else {    // rssi 값을 측정하고 있는 경우
                RSSI_icon.setImageResource(R.drawable.info_rssi_on);
            }
            rssiTextView.setText(rssi);
        });
        infoViewModel.getAutoSearchLiveData().observe(getViewLifecycleOwner(), search -> {
            if (Objects.equals(search, "자동 검색 사용중")) {      // 자동 검색이 켜져 있으면
                Search_icon.setImageResource(R.drawable.info_search_on);
            } else {        // 자동 검색이 꺼져 있으면
                Search_icon.setImageResource(R.drawable.info_search_off);
            }
            auto_search_status.setText(search);
        });
        infoViewModel.getSecurityLiveData().observe(getViewLifecycleOwner(), security -> {
            if (Objects.equals(security, "도난방지 켜짐")) {      // 도난 방지가 켜져 있으면
                Security_icon.setImageResource(R.drawable.info_security_on);
            } else {        // 도난 방지가 꺼져 있으면
                Security_icon.setImageResource(R.drawable.info_security_off);
            }
            security_status.setText(security);
        });
        infoViewModel.getInfoTextLiveData().observe(getViewLifecycleOwner(), text -> {
            switch (text) {
                case "블루투스를 지원 X":
                case "블루투스가 꺼짐":
                    BT_icon.setImageResource(R.drawable.info_bt_off);
                    break;
                case "연결되지 않음":
                case "송수신 불가능":
                    BT_icon.setImageResource(R.drawable.info_bt_on);
                    break;
                case "정상적으로 연결됨":
                    BT_icon.setImageResource(R.drawable.info_bt_connect);
                    break;
            }
            infoText.setText(text);
        });

        // 버튼 이벤트 리스너
        // 설정 초기화 버튼
        mBtn_charge.setOnClickListener(view -> {
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

        checkAll();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Info Fragment", "Info Fragment-onDestroyView()");
    }
}