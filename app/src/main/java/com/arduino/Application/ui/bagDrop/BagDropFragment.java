package com.arduino.Application.ui.bagDrop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentBagdropBinding;

public class BagDropFragment extends Fragment {

    // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 초기화
    LinearLayout Linear_remain;
    LinearLayout Linear_connect;
    LinearLayout Linear_weight;
    LinearLayout Linear_time;

    TextView Text_remainTime;
    TextView Text_bagDrop;
    TextView Text_checkConnect;
    TextView Text_checkWeight;
    TextView Text_checkTime;

    ImageView Icon_connect;
    ImageView Icon_weight;
    ImageView Icon_time;

    Button Btn_time;
    Button Btn_bagDrop;

    Drawable layout_indigo;
    Drawable layout_orange;
    Drawable layout_red;
    Drawable Btn_blue;
    Drawable Btn_red;

    private FragmentBagdropBinding binding;
    private BagDropViewModel bagDropViewModel;
    MainActivity mainActivity;

    // 선택 된 시간과 분을 저장
    private int selectedHour;
    private int selectedMin;

    // 백드랍 Fragment 전역 변수
    private int arriveTime = -1;
    private boolean bagDropMode = false;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bagDropViewModel = new ViewModelProvider(requireActivity()).get(BagDropViewModel.class);

        binding = FragmentBagdropBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity) getActivity();

        // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 선언
        Linear_remain = root.findViewById(R.id.remain_time);
        Linear_connect = root.findViewById(R.id.linear_connect);
        Linear_weight = root.findViewById(R.id.linear_weight);
        Linear_time = root.findViewById(R.id.linear_time);

        Text_remainTime = root.findViewById(R.id.time_left);
        Text_bagDrop = root.findViewById(R.id.textBagDrop);
        Text_checkConnect = root.findViewById(R.id.checkConnect);
        Text_checkWeight = root.findViewById(R.id.checkWeight);
        Text_checkTime = root.findViewById(R.id.checkTime);

        Icon_connect = root.findViewById(R.id.icon_connect);
        Icon_weight = root.findViewById(R.id.icon_weight);
        Icon_time = root.findViewById(R.id.icon_time);

        Btn_time = root.findViewById(R.id.setTime);
        Btn_bagDrop = root.findViewById(R.id.bagdrop_btn);

        layout_indigo = ContextCompat.getDrawable(requireContext(), R.drawable.background_indigo);
        layout_orange = ContextCompat.getDrawable(requireContext(), R.drawable.background_orange);
        layout_red = ContextCompat.getDrawable(requireContext(), R.drawable.background_red);
        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);

        // ViewModel 선언
        // 백드랍 모드 남은 시간 (Integer)
        bagDropViewModel.getRemainTimeLiveData().observe(getViewLifecycleOwner(), time -> {
            if (time == -1) {
                Linear_remain.setVisibility(View.INVISIBLE);
                checkBagDrop();
            } else {
                Linear_remain.setVisibility(View.VISIBLE);
                if (time >= 60) {
                    Text_remainTime.setText(time / 60 + "시간 " + time % 60 + "분");
                } else {
                    Text_remainTime.setText(time % 60 + "분");
                }
            }
        });

        // 백드랍 모드 동작 여부 (Boolean)
        bagDropViewModel.getBagDropStatusLiveData().observe(getViewLifecycleOwner(), bagDrop -> {
            if (bagDrop) {
                Text_bagDrop.setText("백드랍 활성화");
                Text_bagDrop.setTextColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
            } else {
                Text_bagDrop.setText("백드랍 비활성화");
                Text_bagDrop.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange_500));
            }
        });

        // 스마트 캐리어 연결 여부 (Boolean)
        bagDropViewModel.getConnectStatusLiveData().observe(getViewLifecycleOwner(), connect -> {
            if (connect) {
                Text_checkConnect.setText("연결됨");
                Linear_connect.setBackground(layout_indigo);
                Icon_connect.setImageResource(R.drawable.bagdrop_checked);
            } else {
                Text_checkConnect.setText("연결되지 않음");
                Linear_connect.setBackground(layout_orange);
                Icon_connect.setImageResource(R.drawable.bagdrop_not_checked);
            }
        });

        // 무게 측정 여부 (Double)
        bagDropViewModel.getWeightLiveData().observe(getViewLifecycleOwner(), weight -> {
            if (weight == -1) {     // 무게가 측정되지 않은 경우
                Text_checkWeight.setText("측정되지 않음");
                Linear_weight.setBackground(layout_orange);
                Icon_weight.setImageResource(R.drawable.bagdrop_not_checked);
            } else if (weight == -2) {
                Text_checkWeight.setText("잘못된 무게");
                Linear_weight.setBackground(layout_red);
                Icon_weight.setImageResource(R.drawable.bagdrop_not_checked);
            } else {        // 무게가 측정된 경우
                Text_checkWeight.setText(weight + " Kg");
                Linear_weight.setBackground(layout_indigo);
                Icon_weight.setImageResource(R.drawable.bagdrop_checked);
            }
        });

        // 시간 측정 여부 (Integer)
        bagDropViewModel.getTimeLiveData().observe(getViewLifecycleOwner(), time -> {
            if (time == -1) {
                Text_checkTime.setText("설정되지 않음");
                Linear_time.setBackground(layout_orange);
                Icon_time.setImageResource(R.drawable.bagdrop_not_checked);
            } else {
                if (time % 100 == 0) {
                    Text_checkTime.setText(time / 100 + "시 " + "정각");
                } else {
                    Text_checkTime.setText(time / 100 + "시 " + time % 100 + "분");
                }
                Linear_time.setBackground(layout_indigo);
                Icon_time.setImageResource(R.drawable.bagdrop_checked);
            }
        });

        // 백드랍 모드 버튼 상태 (Boolean)
        bagDropViewModel.getBagDropBtnLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status) {       // 백드랍 모드가 동작 중이면
                Btn_bagDrop.setText("백드랍 모드 중지");
                Btn_bagDrop.setBackground(Btn_red);
            } else {
                Btn_bagDrop.setText("백드랍 모드 시작");
                Btn_bagDrop.setBackground(Btn_blue);
            }
        });

        // 버튼 이벤트 리스너
        // 시각 설정 버튼
        Btn_time.setOnClickListener(view -> showCustomDialog());

        // 백드랍 모드 시작 버튼
        Btn_bagDrop.setOnClickListener(view -> {
            checkBagDrop();

            if (bagDropMode) {      // 백드랍 모드 켜짐 -> 꺼짐
                setBagDrop(false);
                bagDropViewModel.setBagDropBtn(false);
                bagDropViewModel.setBagDropStatus(false);
                Toast.makeText(getActivity(), "백드랍 모드가 중지됩니다", Toast.LENGTH_SHORT).show();
                Linear_remain.setVisibility(View.INVISIBLE);
            } else {                // 백드랍 모드 꺼짐 -> 켜짐
                setBagDrop(true);
                bagDropViewModel.setBagDropBtn(true);
                bagDropViewModel.setBagDropStatus(true);
                Toast.makeText(getActivity(), "백드랍 모드가 시작됩니다!", Toast.LENGTH_SHORT).show();
            }
        });

        if (!bagDropMode) {
            Linear_remain.setVisibility(View.INVISIBLE);
        } else {
            Linear_remain.setVisibility(View.VISIBLE);
        }
        return root;
    }

    // 커스텀 다이얼로그를 표시하는 메서드
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void showCustomDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.custom_dialog_time, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // 커스텀 다이얼로그의 버튼, 텍스트 뷰 초기화 및 선언
        Button checkBtn = dialogView.findViewById(R.id.confirm);
        Button cancelBtn = dialogView.findViewById(R.id.cancel);
        TextView text_hour = dialogView.findViewById(R.id.selectedHour);
        TextView text_min = dialogView.findViewById(R.id.selectedMin);

        // 시간과 분을 선택하는 타임피커
        TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMin = minute;
            text_hour.setText(selectedHour + " 시");
            text_min.setText(selectedMin + " 분");
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        // 확인 버튼을 누르면 -> 설정 시각 전달
        checkBtn.setOnClickListener(v -> {
            setTime(selectedHour, selectedMin);

            arriveTime = checkTime();

            if (arriveTime != -1) {
                bagDropViewModel.setTime(arriveTime);
            } else {
                bagDropViewModel.setTime(-1);
            }
            checkCanUseBagDrop();

            dialog.dismiss();
        });
        // 취소 버튼을 누르면 -> 저장 안하고 종료
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
    }

    // 캐리어와 연결을 확인하는 메서드
    private int checkConnection() {
        if (mainActivity != null) {
            return mainActivity.checkConnection();
        }
        return -1;
    }

    // 무게 측정 여부를 확인하는 메서드
    private double checkWeight() {
        if (mainActivity != null) {
            return mainActivity.checkWeightSetting()[0];
        }
        return 0;
    }

    // 시간 설정 여부를 불러오는 메서드
    private int checkTime() {
        if (mainActivity != null) {
            arriveTime = mainActivity.checkTime();
            return arriveTime;
        }
        return -1;
    }

    // 시간 설정을 하는 메서드
    private void setTime(int hour, int min) {
        if (mainActivity != null) {
            mainActivity.setTime(hour, min);
        }
    }

    // 백드랍 모드를 체크하는 메서드
    private void checkBagDrop() {
        if (mainActivity != null) {
            bagDropMode = mainActivity.checkBagDrop();
        }
    }

    // 백드랍 모드를 설정하는 메서드
    private void setBagDrop(boolean onOff) {
        if (mainActivity != null) {
            mainActivity.setBagDrop(onOff);
        }
    }

    // 백드랍 모드 설정 가능여부 체크
    private void checkCanUseBagDrop() {
        if (bagDropMode) {
            Toast.makeText(getActivity(), "백드랍 모드 동작중!", Toast.LENGTH_SHORT).show();
            Btn_bagDrop.setEnabled(true);
        }
        if (checkConnection() == 9 && checkWeight() > 0 && arriveTime != -1) {
            Toast.makeText(getActivity(), "백드랍 모드를 사용할 수 있습니다!", Toast.LENGTH_SHORT).show();
            Btn_bagDrop.setEnabled(true);
        } else {
            Btn_bagDrop.setEnabled(false);
        }
    }

    public void onResume() {
        super.onResume();

        // 캐리어 연결 여부 확인
        bagDropViewModel.setConnectStatus(checkConnection() == 9);

        // 캐리어 무게 측정 여부 확인
        if (checkWeight() < 0) {    //무게가 0보다 작은 경우 (잘못된 값)
            bagDropViewModel.setWeight((double) -2);
        } else if (checkWeight() != 0 && checkWeight() != -1) {
            // 무게 측정결과가 0이나 -1이 아니라면 -> 한마디로 무게를 측정했으면
            double weightTmp = checkWeight();
            bagDropViewModel.setWeight(weightTmp);
        } else {
            // 무게를 측정하지 않았으면
            bagDropViewModel.setWeight((double) -1);
        }

        // 도착 예정시각 설정 확인
        checkTime();

        if (arriveTime != -1) {
            // 도착 시각을 설정했으면
            bagDropViewModel.setTime(arriveTime);
        } else {
            // 도착 시각을 설정하지 않았으면
            bagDropViewModel.setTime(-1);
        }

        // 백드랍 모드 확인
        checkBagDrop();
        if (bagDropMode) {
            // 백드랍 모드가 켜져 있으면
            bagDropViewModel.setBagDropBtn(true);
            bagDropViewModel.setBagDropStatus(true);
        } else {
            // 백드랍 모드가 꺼져 있으면
            bagDropViewModel.setBagDropBtn(false);
            bagDropViewModel.setBagDropStatus(false);
        }
        checkCanUseBagDrop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}