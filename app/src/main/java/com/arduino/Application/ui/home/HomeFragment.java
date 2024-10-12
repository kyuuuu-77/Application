package com.arduino.Application.ui.home;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    // 버튼 및 텍스트 뷰 변수 초기화
    Button BtnBT;
    Button Btn_auth;
    Button BtnBT_connect;

    TextView Text_BTStatus;
    TextView Text_home;
    TextView Text_auth;
    TextView Text_connect;

    Drawable Btn_blue;
    Drawable Btn_red;
    Drawable connect_blue;
    Drawable connect_red;
    Drawable connect_fin;
    Drawable lock_blue;
    Drawable lock_red;

    Window window;

    private BluetoothAdapter mBluetoothAdapter;

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    MainActivity mainActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity) getActivity();

        // 버튼 및 텍스트뷰 선언
        BtnBT = root.findViewById(R.id.btnBT);                     // 블루투스를 켜는 버튼 ID
        Btn_auth = root.findViewById(R.id.authenticate);           // 인증 버튼
        BtnBT_connect = root.findViewById(R.id.btnBT_Connect);     // 연결 버튼

        Text_auth = root.findViewById(R.id.text_auth);           // 인증 버튼 텍스트 뷰
        Text_connect = root.findViewById(R.id.connectText);      // 연결 버튼 텍스트 뷰
        Text_BTStatus = root.findViewById(R.id.BT_Status);       // 블루투스 상태 텍스트 뷰
        Text_home = root.findViewById(R.id.text_home);           // 홈 텍스트 뷰

        // Drawable 선언
        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);
        connect_blue = ContextCompat.getDrawable(requireContext(), R.drawable.home_connect_on);
        connect_red = ContextCompat.getDrawable(requireContext(), R.drawable.home_connect_off);
        connect_fin = ContextCompat.getDrawable(requireContext(), R.drawable.home_connect_fin);
        lock_blue = ContextCompat.getDrawable(requireContext(), R.drawable.home_lock_off);
        lock_red = ContextCompat.getDrawable(requireContext(), R.drawable.home_lock_on);

        window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // 블루투스 어뎁터 초기화
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ViewModel 선언
        homeViewModel.getAuthenticateLiveData().observe(getViewLifecycleOwner(), auth -> {
            if (auth) {         // 인증을 했으면
                Btn_auth.setBackground(lock_blue);
                Btn_auth.setEnabled(false);
                Text_auth.setText("인증됨");
            } else {        // 인증을 안 했으면
                Btn_auth.setBackground(lock_red);
                Btn_auth.setEnabled(true);
                Text_auth.setText("인증 필요");
            }
        });
        homeViewModel.getconnectBtnLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status == -1) {        // 연결 불가능한 경우(연결 불가)
                Text_connect.setText("연결 불가");
                BtnBT_connect.setEnabled(false);
                BtnBT_connect.setBackground(connect_red);
            } else if (status == 0) {   // 블루투스가 켜져 있는 경우 (연결)
                Text_connect.setText("연결");
                BtnBT_connect.setEnabled(true);
                BtnBT_connect.setBackground(connect_blue);
            } else {        // 블루투스 디바이스와 페어링 된 경우 (연결됨)
                Text_connect.setText("연결됨");
                BtnBT_connect.setEnabled(false);
                BtnBT_connect.setBackground(connect_fin);
            }
        });
        homeViewModel.getBluetoothStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status == -1) {     // 블루투스를 사용할 수 없는 경우
                Text_BTStatus.setText("블루투스를 사용할 수 없습니다");
                Text_BTStatus.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_500));
            } else if (status == 0) {       // 블루투스가 꺼져 있는 경우
                Text_BTStatus.setText("블루투스 비활성화");
            } else {    // 블루투스가 켜져 있는 경우
                Text_BTStatus.setText("블루투스 활성화");
            }
        });
        homeViewModel.getHomeTextLiveData().observe(getViewLifecycleOwner(), text -> Text_home.setText(text));
        homeViewModel.getBtBtnLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status == -1) {         // 블루투스를 사용할 수 없는 경우
                BtnBT.setText("블루투스 사용불가");
                BtnBT.setBackground(Btn_red);
                BtnBT.setEnabled(false);
            } else if (status == 0) {   // 블루투스가 꺼진 경우 (블루투스 켜기 표시)
                BtnBT.setText("블루투스 켜기");
                BtnBT.setBackground(Btn_blue);
                BtnBT.setEnabled(true);
            } else {        // 블루투스가 켜진 경우 (블루투스 끄기 표시)
                BtnBT.setText("블루투스 끄기");
                BtnBT.setBackground(Btn_red);
                BtnBT.setEnabled(true);
            }
        });

        // 버튼 이벤트 리스너들
        // 블루투스 전원 버튼
        BtnBT.setOnClickListener(view -> {
            if (!mBluetoothAdapter.isEnabled()) {        // 블루투스가 꺼져있는 경우 -> 켜기
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BT_on();
                } else {    // 레거시
                    BT_on_Legacy();
                }
            } else {        // 블루투스가 켜져있는 경우 -> 끄기
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BT_off();
                } else {    // 레거시
                    BT_off_Legacy();
                }
            }
        });

        // 연결(페어링) 버튼
        BtnBT_connect.setOnClickListener(view -> listPairedDevices());

        // 인증(패스워드) 버튼
        Btn_auth.setOnClickListener(view -> getAuth());

        return root;
    }

    // 인증을 수행하는 메서드
    private void getAuth() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.password_main, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // 다이얼로그 안에 이미지, 텍스트 뷰, 버튼 초기화 및 선언
        EditText getPassword = dialogView.findViewById(R.id.password_input);
        Button checkBtn = dialogView.findViewById(R.id.confirm);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.show();

        checkBtn.setOnClickListener(v -> {
            if (mainActivity != null) {
                try {
                    String password = getPassword.getText().toString();
                    if (password.isEmpty()) {
                        Toast.makeText(getActivity(), "입력 없음", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), password + " 입력 확인!", Toast.LENGTH_SHORT).show();
                        mainActivity.getAuth(password);
                    }
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), e + "에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            dialog.dismiss();
        });
    }

    // 블루투스를 켜는 메서드
    @SuppressLint("NewApi")
    private void BT_on() {
        if (mainActivity != null) {
            mainActivity.BT_on();
        }
    }

    // 블루투스를 켜는 메서드 (레거시)
    private void BT_on_Legacy() {
        if (mainActivity != null) {
            mainActivity.BT_on_Legacy();
        }
    }

    // 블루투스를 끄는 메서드
    @SuppressLint("NewApi")
    private void BT_off() {
        if (mainActivity != null) {
            mainActivity.BT_off();
        }
    }

    // 블루투스를 끄는 메서드 (레거시)
    private void BT_off_Legacy() {
        if (mainActivity != null) {
            mainActivity.BT_off_Legacy();
        }
    }

    // 페어링 가능한 디바이스 리스트를 보여주는 메서드 -> 연결 수행
    @SuppressLint("NewApi")
    private void listPairedDevices() {
        if (mainActivity != null) {
            mainActivity.listPairedDevices();
        }
    }

    // BLE 연결 상태를 확인하는 메서드
    private int checkBLE() {
        if (mainActivity != null) {
            return mainActivity.checkBLE();
        } else {
            return 0;
        }
    }

    // 인증 상태를 확인하는 메서드
    private void checkAuth() {
        if (mainActivity != null) {
            mainActivity.checkAuth();
        }
    }

    public void onResume() {
        super.onResume();

        checkAuth();

        if (mBluetoothAdapter == null) {        // 블루투스를 지원하지 않는 경우
            homeViewModel.setBluetoothStatus(-1);
            homeViewModel.setBtBtn(-1);
            homeViewModel.setConnectBtn(-1);
        } else {
            if (checkBLE() == 2) {     // 블루투스가 켜져 있고 페어링 까지 된 경우
                homeViewModel.setBluetoothStatus(1);
                homeViewModel.setBtBtn(1);
                homeViewModel.setConnectBtn(1);
            } else if (mBluetoothAdapter.isEnabled()) {     // 블루투스가 켜져 있는 경우
                homeViewModel.setBluetoothStatus(1);
                homeViewModel.setBtBtn(1);
                homeViewModel.setConnectBtn(0);
            } else {        // 블루투스가 꺼져 있는 경우
                homeViewModel.setBluetoothStatus(0);
                homeViewModel.setBtBtn(0);
                homeViewModel.setConnectBtn(-1);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}