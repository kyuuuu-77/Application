package com.arduino.Application.ui.info;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {

    // 버튼 및 텍스트뷰 변수 초기화
    Button mBtn_charge;
    TextView rssiTextView;
    TextView infoText;

    private FragmentInfoBinding binding;

    private int menuNum;
    private int status;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel infoViewModel =
                new ViewModelProvider(requireActivity()).get(InfoViewModel.class);

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Info Fragment", "Info Fragment-onCreatedView()");

        // 버튼 및 텍스트뷰 선언
        mBtn_charge = root.findViewById(R.id.chrge);
        rssiTextView = root.findViewById(R.id.rssi_signal);
        infoText = root.findViewById(R.id.text_info);

        // ViewModel 선언
        infoViewModel.getRssiLiveData().observe(getViewLifecycleOwner(), rssi -> rssiTextView.setText(rssi));
        infoViewModel.getInfoTextLiveData().observe(getViewLifecycleOwner(), text -> infoText.setText(text));

        // 버튼 이벤트 리스너
        // 충전 상태 확인 버튼
        mBtn_charge.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            Toast.makeText(getActivity(), "버튼 동작 확인", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    // 연결 상태를 확인하는 메서드
    private int checkConnection() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkConnection();
        } else {
            return 0;
        }
    }

    // 메뉴를 설정하는 메서드
    private void setMenuNum(int num){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setMenuNum(num);
        }
    }

    // RSSI 값을 측정하는 메서드
    private void rssiMeasureStart(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.startRSSIMeasurement();
        }
    }

    public void onResume(){
        super.onResume();
        Log.d("Info Fragment", "Info Fragment-onResume()");

        menuNum = 5;
        setMenuNum(menuNum);

        status = checkConnection();
        if (status == 1){
            rssiMeasureStart();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        MainActivity mainActivity = (MainActivity) getActivity();

        // RSSI 값 측정 중지
        if (mainActivity != null) {
            mainActivity.stopRSSIMeasurement();
        }

        Log.d("Info Fragment", "Info Fragment-onDestroyView()");
    }
}