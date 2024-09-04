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

    private FragmentInfoBinding binding;

    // Info 변수
    private boolean rssiSignal = false;     // rssiSignal
    private boolean autoSearch = false;     // onAutoSearch
    private boolean security = false;       // security
    private int connection = -2;            // connection

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel infoViewModel =
                new ViewModelProvider(requireActivity()).get(InfoViewModel.class);

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
        infoViewModel.getRssiLiveData().observe(getViewLifecycleOwner(), rssiLD -> rssiTextView.setText(rssiLD));
        infoViewModel.getAutoSearchLiveData().observe(getViewLifecycleOwner(), searchLD -> auto_search_status.setText(searchLD));
        infoViewModel.getSecurityLiveData().observe(getViewLifecycleOwner(), securityLD -> security_status.setText(securityLD));
        infoViewModel.getInfoTextLiveData().observe(getViewLifecycleOwner(), textLD -> infoText.setText(textLD));

        // 버튼 이벤트 리스너
        // 설정 초기화 버튼
        mBtn_charge.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            Toast.makeText(getActivity(), "버튼 동작 확인", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    // 배터리 상태를 확인하는 메서드
    private void checkBattery() {
        MainActivity mainActivity = (MainActivity) getActivity();
        int battery_data = -1;
        if (mainActivity != null) {
            battery_data = mainActivity.checkBattery();
        }
        switch (battery_data) {
            case 0:     // 방전중일 경우
                battText.setText("정상");
                Bat_icon.setImageResource(R.drawable.info_bat_normal);
                break;
            case 1:     // 충전중일 경우
                battText.setText("충전중");
                Bat_icon.setImageResource(R.drawable.info_bat_charging);
                break;
            case 2:     // 완충된 경우
                battText.setText("충전됨");
                Bat_icon.setImageResource(R.drawable.info_bat_full);
                break;
            default:    // 정보 취득에 실패한 경우
                battText.setText("정보없음");
                Bat_icon.setImageResource(R.drawable.info_bat_unknown);
                break;
        }
    }

    // RSSI 측정여부를 표시하는 메서드
    private void checkRssi() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            rssiSignal = mainActivity.checkRssi();
        }
        if (rssiSignal) {
            RSSI_icon.setImageResource(R.drawable.info_rssi_on);
        } else {
            RSSI_icon.setImageResource(R.drawable.info_rssi_off);
        }
    }

    // 자동 검색 사용여부 표시하는 메서드
    private void checkAutoSearch() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            autoSearch = mainActivity.checkAutoSearch();
        }
        if (autoSearch) {
            Search_icon.setImageResource(R.drawable.info_search_on);
        } else {
            Search_icon.setImageResource(R.drawable.info_search_off);
        }
    }

    // 도난방지 여부를 표시하는 메서드
    private void checkSecurity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.checkSecurity();
        }
        if (security) {
            Security_icon.setImageResource(R.drawable.info_security_on);
        } else {
            Security_icon.setImageResource(R.drawable.info_security_off);
        }
    }

    // 연결 상태를 확인하는 메서드
    private void checkConnection() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            connection = mainActivity.checkConnection();
        }
        switch (connection) {
            case -2:
            case -1:
                BT_icon.setImageResource(R.drawable.info_bt_off);
                break;
            case 0:
            case 1:
                BT_icon.setImageResource(R.drawable.info_bt_on);
                break;
            case 9:
                BT_icon.setImageResource(R.drawable.info_bt_connect);
                break;
        }
    }

    public void onResume(){
        super.onResume();
        Log.d("Info Fragment", "Info Fragment-onResume()");

        checkBattery();
        checkRssi();
        checkAutoSearch();
        checkSecurity();
        checkConnection();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Info Fragment", "Info Fragment-onDestroyView()");
    }
}