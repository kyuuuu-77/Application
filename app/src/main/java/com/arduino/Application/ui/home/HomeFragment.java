package com.arduino.Application.ui.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentHomeBinding;

import java.util.Objects;

public class HomeFragment extends Fragment {

    // 버튼 및 텍스트 뷰 변수 초기화
    Button mBtnBT;
    Button mBtnBT_Connect;
    
    TextView mTvBT_Status;
    TextView homeText;
    TextView connectText;

    Drawable Btn_blue;
    Drawable Btn_red;
    Drawable connect_blue;
    Drawable connect_red;
    Drawable connect_fin;

    Window window;

    HomeViewModel homeViewModel;

    private BluetoothAdapter mBluetoothAdapter;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Home Fragment", "Home Fragment-onCreatedView()");

        // 버튼 및 텍스트뷰 선언
        mBtnBT = root.findViewById(R.id.btnBT);                     // 블루투스를 켜는 버튼 ID
        mBtnBT_Connect = root.findViewById(R.id.btnBT_Connect);     // 연결 버튼

        connectText = root.findViewById(R.id.connectText);      // 연결 버튼 텍스트 뷰
        mTvBT_Status = root.findViewById(R.id.BT_Status);       // 블루투스 상태 텍스트 뷰
        homeText = root.findViewById(R.id.text_home);           // 홈 텍스트 뷰

        // Drawable 선언
        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);
        connect_blue = ContextCompat.getDrawable(requireContext(), R.drawable.home_connect_on);
        connect_red = ContextCompat.getDrawable(requireContext(), R.drawable.home_connect_off);
        connect_fin = ContextCompat.getDrawable(requireContext(), R.drawable.home_connect_fin);

        window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // 블루투스 어뎁터 초기화
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ViewModel 선언
        homeViewModel.getBluetoothStatusLiveData().observe(getViewLifecycleOwner(), bluetoothStatus -> mTvBT_Status.setText(bluetoothStatus));
        homeViewModel.getHomeTextLiveData().observe(getViewLifecycleOwner(), text -> homeText.setText(text));
        homeViewModel.getBtBtnLiveData().observe(getViewLifecycleOwner(), text -> {
            if (Objects.equals(text, "블루투스 사용불가")) {
                mBtnBT.setBackground(Btn_red);
                mBtnBT.setEnabled(false);
            } else if (Objects.equals(text, "블루투스 켜기")) {      // 블루투스 켜기 버튼 구성
                mBtnBT.setBackground(Btn_blue);
            } else {        // 블루투스 끄기 버튼 구성
                mBtnBT.setBackground(Btn_red);
            }
            mBtnBT.setText(text);
        });
        homeViewModel.getconnectBtnLiveData().observe(getViewLifecycleOwner(), text -> {
            if (Objects.equals(text, "연결됨")) {          // 블루투스 디바이스와 페어링 된 경우 (연결됨)
                mBtnBT_Connect.setEnabled(false);
                mBtnBT_Connect.setBackground(connect_fin);
            } else if (Objects.equals(text, "연결")) {    // 블루투스가 켜져 있는 경우 (연결)
                mBtnBT_Connect.setEnabled(true);
                mBtnBT_Connect.setBackground(connect_blue);
            } else {                                         // 블루투스가 꺼진 경우 (연결 불가)
                mBtnBT_Connect.setEnabled(false);
                mBtnBT_Connect.setBackground(connect_red);
            }
            connectText.setText(text);
        });

        // 버튼 이벤트 리스너들
        // 블루투스 전원 버튼
        mBtnBT.setOnClickListener(view -> {
            if (!mBluetoothAdapter.isEnabled()) {        // 블루투스가 꺼져있는 경우 -> 켜기
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BT_on();
                    setUIColor();
                } else {    // 레거시
                    BT_on_Legacy();
                    homeViewModel.setBtBtn("블루투스 끄기");
                    homeViewModel.setConnectBtn("연결");
                }
            } else {        // 블루투스가 켜져있는 경우 -> 끄기
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    BT_off();
                    setUIColor();
                } else {    // 레거시
                    BT_off_Legacy();
                    homeViewModel.setBtBtn("블루투스 켜기");
                    homeViewModel.setConnectBtn("연결 불가");
                }
            }
        });

        // 연결(페어링) 버튼
        mBtnBT_Connect.setOnClickListener(view -> listPairedDevices());

        return root;
    }

    // 블루투스를 켜는 메서드
    @SuppressLint("NewApi")
    private void BT_on() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.BT_on();
        }
    }

    // 블루투스를 켜는 메서드 (레거시)
    private void BT_on_Legacy() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.BT_on_Legacy();
        }
    }

    // 블루투스를 끄는 메서드
    @SuppressLint("NewApi")
    private void BT_off() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.BT_off();
        }
    }

    // 블루투스를 끄는 메서드 (레거시)
    private void BT_off_Legacy() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.BT_off_Legacy();
        }
    }

    // 페어링 가능한 디바이스 리스트를 보여주는 메서드 -> 연결 수행
    @SuppressLint("NewApi")
    private void listPairedDevices() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.listPairedDevices();
        }
    }

    // BLE 연결 상태를 확인하는 메서드
    private int checkBLE() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkBLE();
        } else {
            return 0;
        }
    }

    // 상단바와 툴바의 색상을 변경하는 메서드
    private void setUIColor() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setUIColor();
        }
    }

    public void onResume(){
        super.onResume();
        Log.d("Home Fragment", "Home Fragment-onResume()");

        if (mBluetoothAdapter == null) {        // 블루투스를 지원하지 않는 경우
            homeViewModel.setBluetoothStatus("블루투스 사용 불가능");
            homeViewModel.setBtBtn("블루투스 사용불가");
            homeViewModel.setConnectBtn("연결 불가");
        } else {
            if (checkBLE() == 2) {     // 블루투스가 켜져 있고 페어링 까지 된 경우
                homeViewModel.setBluetoothStatus("블루투스 활성화");
                homeViewModel.setBtBtn("블루투스 끄기");
                homeViewModel.setConnectBtn("연결됨");
            } else if (mBluetoothAdapter.isEnabled()) {     // 블루투스가 켜져 있는 경우
                homeViewModel.setBluetoothStatus("블루투스 활성화");
                homeViewModel.setBtBtn("블루투스 끄기");
                homeViewModel.setConnectBtn("연결");
            } else {        // 블루투스가 꺼져 있는 경우
                homeViewModel.setBluetoothStatus("블루투스 비활성화");
                homeViewModel.setBtBtn("블루투스 켜기");
                homeViewModel.setConnectBtn("연결 불가");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Home Fragment", "Home Fragment-onDestroyView()");

        binding = null;
    }
}