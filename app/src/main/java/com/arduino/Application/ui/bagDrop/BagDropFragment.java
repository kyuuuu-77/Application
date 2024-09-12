package com.arduino.Application.ui.bagDrop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentBagdropBinding;

import java.util.ArrayList;
import java.util.List;

public class BagDropFragment extends Fragment {

    // 리니어 레이아웃, 버튼, 이미지 뷰 및 텍스트 뷰 초기화
    LinearLayout linearConnect;
    LinearLayout linearWeight;
    LinearLayout linearTime;
    
    TextView checkConnect;
    TextView checkWeight;
    TextView checkTime;

    ImageView iconConnect;
    ImageView iconWeight;
    ImageView iconTime;

    Button timeBtn;
    Button bagDropBtn;

    Drawable layout_indigo;
    Drawable layout_orange;

    private FragmentBagdropBinding binding;
    private BagDropViewModel bagDropViewModel;

    private int arriveTime = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bagDropViewModel = new ViewModelProvider(requireActivity()).get(BagDropViewModel.class);

        binding = FragmentBagdropBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("BagDrop Fragment", "BagDrop Fragment-onCreatedView()");

        // 리니어 레이아웃, 버튼, 이미지 뷰 및 텍스트 뷰 선언
        linearConnect = root.findViewById(R.id.linear_connect);
        linearWeight = root.findViewById(R.id.linear_weight);
        linearTime = root.findViewById(R.id.linear_time);

        checkConnect = root.findViewById(R.id.checkConnect);
        checkWeight = root.findViewById(R.id.checkWeight);
        checkTime = root.findViewById(R.id.checkTime);

        iconConnect = root.findViewById(R.id.icon_connect);
        iconWeight = root.findViewById(R.id.icon_weight);
        iconTime = root.findViewById(R.id.icon_time);

        timeBtn = root.findViewById(R.id.setTime);
        bagDropBtn = root.findViewById(R.id.bagdrop_btn);

        layout_indigo = ContextCompat.getDrawable(requireContext(), R.drawable.background_indigo);
        layout_orange = ContextCompat.getDrawable(requireContext(), R.drawable.background_orange);

        // ViewModel 선언
        bagDropViewModel.getConnectTextLiveData().observe(getViewLifecycleOwner(), connect -> checkConnect.setText(connect));
        bagDropViewModel.getWeightTextLiveData().observe(getViewLifecycleOwner(), rssi -> checkWeight.setText(rssi));
        bagDropViewModel.getTimeTextLiveData().observe(getViewLifecycleOwner(), time -> checkTime.setText(time));
        bagDropViewModel.getBagDropBtnTextLiveData().observe(getViewLifecycleOwner(), btnText -> bagDropBtn.setText(btnText));

        // 버튼 이벤트 리스너
        // 시각 설정 버튼
        timeBtn.setOnClickListener(view -> {
            // 시간을 설정하는 동작
            showCustomDialog();
        });

        // 백드랍 모드 시작 버튼
        bagDropBtn.setOnClickListener(view -> {
            Toast.makeText(getActivity(), "백드랍 모드가 시작됩니다!", Toast.LENGTH_SHORT).show();
        });
        return root;
    }

    // 커스텀 다이얼로그를 표시하는 메서드
    @SuppressLint("DefaultLocale")
    private void showCustomDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.custom_dialog_time, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // 다이얼로그 안에 이미지, 텍스트 뷰, 버튼 초기화 및 선언
        Button checkBtn = dialogView.findViewById(R.id.confirm);
        Button cancelBtn = dialogView.findViewById(R.id.cancel);
        TextView textHour = dialogView.findViewById(R.id.selectedHour);
        TextView textMin = dialogView.findViewById(R.id.selectedMin);

        // 시간과 분을 선택할 수 있는 Spinner
        final Spinner hourSpinner = dialogView.findViewById(R.id.hour_spinner);
        final Spinner minuteSpinner = dialogView.findViewById(R.id.minute_spinner);

        // 시간과 분을 위한 리스트 설정
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%d", i));
        }
        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%d", i));
        }

        // Adapter 설정
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);

        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedHour = hourSpinner.getSelectedItem().toString();
                textHour.setText(selectedHour + "시");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        minuteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMin = minuteSpinner.getSelectedItem().toString();
                textMin.setText(selectedMin + "분");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, minutes);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        checkBtn.setOnClickListener(v -> {
            int hour = Integer.parseInt((String) hourSpinner.getSelectedItem());
            int min = Integer.parseInt((String) minuteSpinner.getSelectedItem());
            setTime(hour, min);

            arriveTime = checkTime();

            if (arriveTime != -1) {
                bagDropViewModel.setTimeText(arriveTime / 100 + "시 " + arriveTime % 100 + "분");
                linearTime.setBackground(layout_indigo);
                iconTime.setImageResource(R.drawable.bagdrop_checked);
            }

            dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
    }

    // 캐리어와 연결을 확인하는 메서드
    private int checkConnection() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkConnection();
        }
        return -1;
    }
    
    // 무게 측정 여부를 확인하는 메서드
    private double checkWeight() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkWeightSetting()[0];
        }
        return 0;
    }

    // 시간 설정 여부를 불러오는 메서드
    private int checkTime() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            arriveTime = mainActivity.checkTime();
            return arriveTime;
        }
        return -1;
    }

    // 시간 설정을 저장하는 메서드
    private void setTime(int hour, int min) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setTime(hour, min);
        }
    }

    @SuppressLint("ResourceAsColor")
    public void onResume() {
        super.onResume();
        Log.d("BagDrop Fragment", "BagDrop Fragment-onResume()");

        // 캐리어 연결 여부 확인
        if (checkConnection() == 9) {
            // 캐리어 연결되어 있으면
            bagDropViewModel.setConnectText("연결됨");
            linearConnect.setBackground(layout_indigo);
            iconConnect.setImageResource(R.drawable.bagdrop_checked);
        } else {
            // 캐리어 연결되지 않았으면
            bagDropViewModel.setConnectText("연결되지 않음");
            linearConnect.setBackground(layout_orange);
            iconConnect.setImageResource(R.drawable.bagdrop_not_checked);
        }
        
        // 캐리어 무게 측정 여부 확인
        if (checkWeight() != 0) {
            // 무게 측정결과가 0이 아니라면 -> 한마디로 무게를 측정했으면
            double weightTmp = checkWeight();
            bagDropViewModel.setWeightText(weightTmp + " Kg");
            linearWeight.setBackground(layout_indigo);
            iconWeight.setImageResource(R.drawable.bagdrop_checked);
        } else {
            // 무게를 측정하지 않았으면
            bagDropViewModel.setWeightText("측정되지 않음");
            iconWeight.setImageResource(R.drawable.bagdrop_not_checked);
        }

        arriveTime = checkTime();
        // 도착 예정시각 설정 확인
        if (arriveTime != -1) {
            // 도착 시각을 설정했으면
            bagDropViewModel.setTimeText(arriveTime / 100 + "시 " + arriveTime % 100 + "분");
            linearTime.setBackground(layout_indigo);
            iconTime.setImageResource(R.drawable.bagdrop_checked);
        } else {
            // 도착 시각을 설정하지 않았으면
            bagDropViewModel.setTimeText("설정되지 않음");
            linearTime.setBackground(layout_orange);
            iconTime.setImageResource(R.drawable.bagdrop_not_checked);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("BagDrop Fragment", "BagDrop Fragment-onDestroyView()");
    }
}