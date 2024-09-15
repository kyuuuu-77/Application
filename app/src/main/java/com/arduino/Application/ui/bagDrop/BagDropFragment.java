package com.arduino.Application.ui.bagDrop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
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
import java.util.Objects;

public class BagDropFragment extends Fragment {

    // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 초기화
    LinearLayout linearRemain;
    LinearLayout linearConnect;
    LinearLayout linearWeight;
    LinearLayout linearTime;

    TextView remainTime;
    TextView bagDropText;
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
    Drawable Btn_blue;
    Drawable Btn_red;

    private FragmentBagdropBinding binding;
    private BagDropViewModel bagDropViewModel;

    private int arriveTime = -1;
    private boolean bagDropMode = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bagDropViewModel = new ViewModelProvider(requireActivity()).get(BagDropViewModel.class);

        binding = FragmentBagdropBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("BagDrop Fragment", "BagDrop Fragment-onCreatedView()");

        // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 선언
        linearRemain = root.findViewById(R.id.remain_time);
        linearConnect = root.findViewById(R.id.linear_connect);
        linearWeight = root.findViewById(R.id.linear_weight);
        linearTime = root.findViewById(R.id.linear_time);

        remainTime = root.findViewById(R.id.time_left);
        bagDropText = root.findViewById(R.id.textBagDrop);
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
        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);

        // ViewModel 선언
        bagDropViewModel.getRemainTimeTextLiveData().observe(getViewLifecycleOwner(), time -> {
            if (Objects.equals(time, "null")) {
                linearRemain.setVisibility(View.INVISIBLE);
                checkBagDrop();
            } else {
                linearRemain.setVisibility(View.VISIBLE);
                remainTime.setText(time);
            }
        });
        bagDropViewModel.getBagDropTextLiveData().observe(getViewLifecycleOwner(), text -> {
            if (Objects.equals(text, "백드랍 활성화")) {
                bagDropText.setTextColor(Color.parseColor("#3F51B5"));
            } else if(Objects.equals(text, "백드랍 비활성화")) {
                bagDropText.setTextColor(Color.parseColor("#FF9800"));
            }
            bagDropText.setText(text);
        });
        bagDropViewModel.getConnectTextLiveData().observe(getViewLifecycleOwner(), connect -> checkConnect.setText(connect));
        bagDropViewModel.getWeightTextLiveData().observe(getViewLifecycleOwner(), rssi -> checkWeight.setText(rssi));
        bagDropViewModel.getTimeTextLiveData().observe(getViewLifecycleOwner(), time -> checkTime.setText(time));
        bagDropViewModel.getBagDropBtnTextLiveData().observe(getViewLifecycleOwner(), btnText -> {
            if (Objects.equals(btnText, "백드랍 모드 시작")) {
                bagDropBtn.setBackground(Btn_blue);
            } else if (Objects.equals(btnText, "백드랍 모드 중지")) {
                bagDropBtn.setBackground(Btn_red);
            }
            bagDropBtn.setText(btnText);
        });

        // 버튼 이벤트 리스너
        // 시각 설정 버튼
        timeBtn.setOnClickListener(view -> showCustomDialog());

        // 백드랍 모드 시작 버튼
        bagDropBtn.setOnClickListener(view -> {
            checkBagDrop();

            if (bagDropMode) {      // 백드랍 모드 켜짐 -> 꺼짐
                setBagDrop(false);
                bagDropViewModel.setBagDropBtnText("백드랍 모드 시작");
                bagDropViewModel.setBagDropText("백드랍 비활성화");
                Toast.makeText(getActivity(), "백드랍 모드가 중지됩니다.", Toast.LENGTH_SHORT).show();
                linearRemain.setVisibility(View.INVISIBLE);
            } else {                // 백드랍 모드 꺼짐 -> 켜짐
                setBagDrop(true);
                bagDropViewModel.setBagDropBtnText("백드랍 모드 중지");
                bagDropViewModel.setBagDropText("백드랍 활성화");
                Toast.makeText(getActivity(), "백드랍 모드가 시작됩니다!", Toast.LENGTH_SHORT).show();
            }
        });

        if (!bagDropMode) {
            linearRemain.setVisibility(View.INVISIBLE);
        } else {
            linearRemain.setVisibility(View.VISIBLE);
        }
        return root;
    }

    // 커스텀 다이얼로그를 표시하는 메서드
    @SuppressLint("DefaultLocale")
    private void showCustomDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.custom_dialog_time, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // 커스텀 다이얼로그의 버튼, 텍스트 뷰 초기화 및 선언
        Button checkBtn = dialogView.findViewById(R.id.confirm);
        Button cancelBtn = dialogView.findViewById(R.id.cancel);
        TextView textHour = dialogView.findViewById(R.id.selectedHour);
        TextView textMin = dialogView.findViewById(R.id.selectedMin);

        // 시간과 분을 선택하는 스피너
        final Spinner hourSpinner = dialogView.findViewById(R.id.hour_spinner);
        final Spinner minuteSpinner = dialogView.findViewById(R.id.minute_spinner);

        // 리스트 설정
        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.format("%d", i));
        }
        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.format("%d", i));
        }

        // 어뎁터 설정 (시간)
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, hours);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);

        // 어뎁터 설정 (분)
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, minutes);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteSpinner.setAdapter(minuteAdapter);

        // 아이템 선택 리스너 (시간)
        hourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedHour = hourSpinner.getSelectedItem().toString();
                textHour.setText(selectedHour + "시");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 아이템 선택 리스너 (분)
        minuteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMin = minuteSpinner.getSelectedItem().toString();
                textMin.setText(selectedMin + "분");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        // 확인 버튼을 누르면 -> 설정 시각 전달
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
            checkCanUseBagDrop();

            dialog.dismiss();
        });
        // 취소 버튼을 누르면 -> 저장 안하고 종료
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

    // 시간 설정을 하는 메서드
    private void setTime(int hour, int min) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setTime(hour, min);
        }
    }

    // 백드랍 모드를 체크하는 메서드
    private void checkBagDrop() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            bagDropMode = mainActivity.checkBagDrop();
        }
    }

    // 백드랍 모드를 설정하는 메서드
    private void setBagDrop(boolean onOff) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setBagDrop(onOff);
        }
    }

    // 백드랍 모드 설정 가능여부 체크
    private void checkCanUseBagDrop() {
        if (checkConnection() == 9 && checkWeight() != 0 && arriveTime != -1) {
            Toast.makeText(getActivity(), "백드랍 모드를 사용할 수 있습니다!", Toast.LENGTH_SHORT).show();
            bagDropBtn.setEnabled(true);
        } else {
            bagDropBtn.setEnabled(false);
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

        // 도착 예정시각 설정 확인
        checkTime();

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

        // 백드랍 모드 확인
        checkBagDrop();
        if (bagDropMode) {
            // 백드랍 모드가 켜져 있으면
            bagDropBtn.setBackground(Btn_red);
            bagDropViewModel.setBagDropBtnText("백드랍 모드 중지");
            bagDropViewModel.setBagDropText("백드랍 활성화");
        } else {
            // 백드랍 모드가 꺼져 있으면
            bagDropBtn.setBackground(Btn_blue);
            bagDropViewModel.setBagDropBtnText("백드랍 모드 시작");
            bagDropViewModel.setBagDropText("백드랍 비활성화");
        }

        checkCanUseBagDrop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("BagDrop Fragment", "BagDrop Fragment-onDestroyView()");
    }
}