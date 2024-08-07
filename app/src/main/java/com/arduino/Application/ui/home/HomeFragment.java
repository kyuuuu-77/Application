package com.arduino.Application.ui.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
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
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    /* Fragment 생명주기 참고할 것 !!!
     * onAttach()->onCreate()->onCreateView()->onViewCreated()->onViewStateRestored()->
     * onStart()->onResume()->onPause()->onStop()->onDestoryView->onDestroy->onDetach()
     * */

    // 버튼 및 텍스트뷰 변수 초기화
    Button mBtnBT_on;
    Button mBtnBT_off;
    Button mBtnBT_Connect;
    Button mBtnAlert_on;
    Button mBtnAlert_off;
    
    TextView mTvBT_Status;
    TextView Alert_Status;
    TextView homeText;

    Window window;
    Toolbar toolbar;

    private BluetoothAdapter mBluetoothAdapter;

    private FragmentHomeBinding binding;

    private int menuNum;
    private int security = 0;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Home Fragment", "Home Fragment-onCreatedView()");

        // 버튼 및 텍스트뷰 선언
        mBtnBT_on = root.findViewById(R.id.btnBT_On);           // 블루투스를 켜는 버튼 ID
        mBtnBT_off = root.findViewById(R.id.btnBT_Off);         // 블루투스를 끄는 버튼 ID
        mBtnBT_Connect = root.findViewById(R.id.btnBT_Connect); // 연결 버튼
        mBtnAlert_on = root.findViewById(R.id.btnAlert_On);     // 도난방지 켜는 버튼
        mBtnAlert_off = root.findViewById(R.id.btnAlert_Off);   // 도난방지 끄는 버튼
        
        mTvBT_Status = root.findViewById(R.id.BT_Status);       // 블루투스 상태 텍스트 뷰
        Alert_Status = root.findViewById(R.id.Alert_Status);    // 도난방지 상태 텍스트 뷰
        homeText = root.findViewById(R.id.text_home);           // 홈 텍스트 뷰
        
        window = requireActivity().getWindow();
        toolbar = root.findViewById(R.id.toolbar);   // 툴바
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ViewModel 선언
        homeViewModel.getBluetoothStatusLiveData().observe(getViewLifecycleOwner(), bluetoothStatus -> mTvBT_Status.setText(bluetoothStatus));
        homeViewModel.getAlertStatusLiveData().observe(getViewLifecycleOwner(), alert -> Alert_Status.setText(alert));
        homeViewModel.getHomeTextLiveData().observe(getViewLifecycleOwner(), text -> homeText.setText(text));

        // 버튼 이벤트 리스너들
        // 블루투스 ON 버튼
        mBtnBT_on.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Fragment_BT_on();
            } else {
                Fragment_BT_on_Legacy();
                mBtnBT_on.setEnabled(false);
                mBtnBT_off.setEnabled(true);
                window.setStatusBarColor(Color.parseColor("#1976D2"));
                //toolbar.setBackgroundColor(Color.parseColor("#2196F3"));
            }

            if (mBluetoothAdapter.isEnabled()){
                if (security == 1){
                    mBtnAlert_on.setEnabled(false);
                    mBtnAlert_off.setEnabled(true);
                } else if (security == 0){
                    mBtnAlert_on.setEnabled(true);
                    mBtnAlert_off.setEnabled(false);
                }
            }
        });

        // 블루투스 OFF 버튼
        mBtnBT_off.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Fragment_BT_off();
            } else {
                Fragment_BT_off_Legacy();
                mBtnBT_on.setEnabled(true);
                mBtnBT_off.setEnabled(false);
                mBtnAlert_on.setEnabled(false);
                mBtnAlert_off.setEnabled(false);
                window.setStatusBarColor(Color.parseColor("#F57C00"));
                //toolbar.setBackgroundColor(Color.parseColor("#FF9800"));
            }

            if (!mBluetoothAdapter.isEnabled()){
                Fragment_security_OFF();
            }
        });

        // 연결 버튼
        mBtnBT_Connect.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            Fragment_listPairedDevices();
        });

        // 도난방지 ON 버튼
        mBtnAlert_on.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            Fragment_security_ON();
        });

        // 도난방지 OFF 버튼
        mBtnAlert_off.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            Fragment_security_OFF();
        });

        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Home Fragment", "Home Fragment-onResume()");

        menuNum = 1;
        setMenuNum(menuNum);

        if (mBluetoothAdapter == null) {
            mTvBT_Status.setText("블루투스 지원하지 않음");
            window.setStatusBarColor(Color.parseColor("#D32F2F"));
            mBtnBT_on.setEnabled(false);
            mBtnBT_off.setEnabled(false);
            mBtnBT_Connect.setEnabled(false);
            mBtnAlert_on.setEnabled(false);
            mBtnAlert_off.setEnabled(false);
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                mTvBT_Status.setText("블루투스 활성화");
                window.setStatusBarColor(Color.parseColor("#1976D2"));
                mBtnBT_on.setEnabled(false);
                mBtnBT_off.setEnabled(true);
                mBtnBT_Connect.setEnabled(true);

                if (security == 1){
                    mBtnAlert_on.setEnabled(false);
                    mBtnAlert_off.setEnabled(true);
                } else if (security == 0){
                    mBtnAlert_on.setEnabled(true);
                    mBtnAlert_off.setEnabled(false);
                }
            } else {
                mTvBT_Status.setText("블루투스 비활성화");
                window.setStatusBarColor(Color.parseColor("#F57C00"));
                mBtnBT_on.setEnabled(true);
                mBtnBT_off.setEnabled(false);
                mBtnBT_Connect.setEnabled(false);
                mBtnAlert_on.setEnabled(false);
                mBtnAlert_off.setEnabled(false);
            }
        }

        // MainActivity를 통해 캐리어 자동 검색
        Auto_startBluetoothDiscovery();
    }

    private void setMenuNum(int num){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setMenuNum(num);
        }
    }

    @SuppressLint("NewApi")
    public void Fragment_BT_on() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.BT_on();
        }
    }

    public void Fragment_BT_on_Legacy() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.BT_on_Legacy();
        }
    }

    @SuppressLint("NewApi")
    public void Fragment_BT_off() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.BT_off();
        }
    }

    public void Fragment_BT_off_Legacy() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.BT_off_Legacy();
        }
    }

    @SuppressLint("NewApi")
    public void Fragment_listPairedDevices(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.listPairedDevices();
        }
    }

    public void Auto_startBluetoothDiscovery(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.startBluetoothDiscovery();
        }
    }

    public void Fragment_security_ON(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.security_ON();
            mBtnAlert_on.setEnabled(false);
            mBtnAlert_off.setEnabled(true);
        }
    }
    public void Fragment_security_OFF(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.security_OFF();
            mBtnAlert_on.setEnabled(true);
            mBtnAlert_off.setEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Home Fragment", "Home Fragment-onDestroyView()");

        binding = null;
    }
}