package com.arduino.Application.ui.find;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentFindBinding;

import java.util.Objects;

public class FindFragment extends Fragment {

    // 버튼 및 텍스트 뷰 초기화
    TextView textAlert;
    TextView alertStatus;
    TextView distance;

    Button bellBtn;
    Button securityBtn;

    Drawable Btn_blue;
    Drawable Btn_red;
    Drawable find_blue;
    Drawable find_red;

    private boolean security = false;       // security

    private BluetoothAdapter mBluetoothAdapter;

    private FragmentFindBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FindViewModel findViewModel =
                new ViewModelProvider(requireActivity()).get(FindViewModel.class);

        binding = FragmentFindBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Find Fragment", "Find Fragment-onCreatedView()");

        // 버튼 및 텍스트 뷰 선언
        textAlert = root.findViewById(R.id.text_alert);
        alertStatus = root.findViewById(R.id.alert_status);
        distance = root.findViewById(R.id.find_distance);

        bellBtn = root.findViewById(R.id.bell);
        securityBtn = root.findViewById(R.id.alertBtn);

        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);
        find_blue = ContextCompat.getDrawable(requireContext(), R.drawable.find_bell_on);
        find_red = ContextCompat.getDrawable(requireContext(), R.drawable.find_bell_off);

        // ViewModel과 UI 요소 바인딩
        findViewModel.getAlertTextLiveData().observe(getViewLifecycleOwner(), text -> textAlert.setText(text));
        findViewModel.getAlertStatusLiveData().observe(getViewLifecycleOwner(), status -> alertStatus.setText(status));
        findViewModel.getDistanceLiveData().observe(getViewLifecycleOwner(), bag_distance -> {
            if (Objects.equals(bag_distance, "캐리어와 떨어져 있음")) {
                distance.setTextColor(Color.parseColor("#F57C00"));
            } else if (Objects.equals(bag_distance, "캐리어와 멂")) {
                distance.setTextColor(Color.parseColor("#D32F2F"));
            } else if (Objects.equals(bag_distance, "캐리어와 매우 가까움")) {
                distance.setTextColor(Color.parseColor("#1976D2"));
            } else if (Objects.equals(bag_distance, "캐리어와 가까움")) {
                distance.setTextColor(Color.parseColor("#388E3C"));
            } else {
                distance.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black));
            }
            distance.setText(bag_distance);
        });
        findViewModel.getAlertBtnLiveData().observe(getViewLifecycleOwner(), btn -> securityBtn.setText(btn));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 버튼 이벤트 리스너
        // 벨 울리는 버튼
        bellBtn.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            textAlert.setText("벨 울리기 시도중...");
            Toast.makeText(getActivity(), "벨 울리기 시도중...", Toast.LENGTH_SHORT).show();

            ringBell(1);
        });

        // 도난방지 버튼
        securityBtn.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            if (security) {     // 도난방지가 켜져있는 경우
                Fragment_security_OFF();
                securityBtn.setText("도난방지 켜기");
                securityBtn.setBackground(Btn_blue);
                Toast.makeText(getActivity(), "도난방지를 사용하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {            // 도난방지가 꺼져있는 경우
                Fragment_security_ON();
                securityBtn.setText("도난방지 끄기");
                securityBtn.setBackground(Btn_red);
                Toast.makeText(getActivity(), "도난방지를 사용합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    // 벨을 울리는 메서드
    private void ringBell(int onOff) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            if (onOff == 1) {   // 벨 울리기 동작
                int status = mainActivity.ringBell();
                if (status == 1) {  // 벨 울리기 성공한 경우
                    showCustomDialog(3);
                    Toast.makeText(getActivity(), "벨을 울리고 있습니다.", Toast.LENGTH_SHORT).show();
                } else {    // 벨 울리기 실패한 경우
                    showCustomDialog(2);
                    Toast.makeText(getActivity(), "벨을 울리지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            } else if (onOff == 0) {    // 벨 울리기 멈춤
                while (true) {
                    int status = mainActivity.ringBell();
                    if (status != 2) {
                        SystemClock.sleep(2000);
                        Toast.makeText(getActivity(), "벨 중지에 실패했습니다. 2초후에 다시 시도합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        break;
                    }
                }
            }
        }
    }

    // 도난방지 여부를 표시
    private void Fragment_checkSecurity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.checkSecurity();
        }
    }

    private void Fragment_security_ON() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.security_ON();
        }
    }

    private void Fragment_security_OFF() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.security_OFF();
        }
    }

    private void showCustomDialog(int status) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // 다이얼로그 안에 이미지, 텍스트 뷰, 버튼 초기화 및 선언
        ImageView iconView = dialogView.findViewById(R.id.dialog_icon);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message_text);
        ImageView messageImageView = dialogView.findViewById(R.id.dialog_message_image);

        Button retryBtn = dialogView.findViewById(R.id.retry);
        Button checkBtn = dialogView.findViewById(R.id.confirm);
        Button cancelBtn = dialogView.findViewById(R.id.cancel);

        // 텍스트, 이미지, 버튼 설정
        switch (status) {
            case 3:
                iconView.setImageResource(R.drawable.dialog_bell);
                titleView.setText("벨을 울리는 중...");
                messageTextView.setText("캐리어에서 벨이 울리고 있습니다.\n벨은 하단의 확인 버튼을 누를 때까지 계속 울립니다.");
                messageImageView.setImageResource(R.drawable.bag_ring);
                retryBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                break;
            case 2:
                iconView.setImageResource(R.drawable.dialog_bell_error);
                titleView.setText("벨 울리기 실패!");
                messageTextView.setText("벨 울리기 동작에 실패했습니다.\n스마트 캐리어와 연결되어 있고 통신 상태가 양호한지 확인 후\n다시 시도하세요.");
                messageImageView.setImageResource(R.drawable.connection_error);
                checkBtn.setVisibility(View.GONE);
                break;
            case 1:
                iconView.setImageResource(R.drawable.info_bt_off);
                titleView.setText("도난방지 및 찾기 비활성화");
                messageTextView.setText("블루투스가 꺼져 있어 도난방지 기능을 사용할 수 없습니다.\n블루투스를 켠 후에 다시 시도하세요.");
                messageImageView.setImageResource(R.drawable.connection);
                retryBtn.setVisibility(View.GONE);
                checkBtn.setVisibility(View.GONE);
                break;
        }

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        retryBtn.setOnClickListener(v -> dialog.dismiss());
        checkBtn.setOnClickListener(v -> {
            ringBell(0);
            dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
    }

    public void onResume() {
        super.onResume();
        Log.d("Find Fragment", "Find Fragment-onResume()");

        Fragment_checkSecurity();

        if (!mBluetoothAdapter.isEnabled()) {
            showCustomDialog(1);
            bellBtn.setEnabled(false);
            bellBtn.setBackground(find_red);
            securityBtn.setEnabled(false);
            securityBtn.setBackground(Btn_red);
        } else {
            bellBtn.setEnabled(true);
            bellBtn.setBackground(find_blue);
            securityBtn.setEnabled(true);
            if (security) {     // 도난방지가 켜져있는 경우
                securityBtn.setText("도난방지 끄기");
                securityBtn.setBackground(Btn_red);
            } else {            // 도난방지가 꺼져있는 경우
                securityBtn.setText("도난방지 켜기");
                securityBtn.setBackground(Btn_blue);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Find Fragment", "Find Fragment-onDestroyView()");
    }
}