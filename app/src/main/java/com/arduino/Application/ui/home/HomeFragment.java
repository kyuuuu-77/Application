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

public class HomeFragment extends Fragment {
    /* Fragment 생명주기 참고할 것 !!!
     * onAttach()->onCreate()->onCreateView()->onViewCreated()->onViewStateRestored()->
     * onStart()->onResume()->onPause()->onStop()->onDestoryView->onDestroy()->onDetach()
     * */

    // 버튼 및 텍스트뷰 변수 초기화
    Button mBtnBT;
    Button mBtnBT_Connect;
    
    TextView mTvBT_Status;
    TextView homeText;

    Drawable Btn_blue;
    Drawable Btn_red;

    Window window;

    private BluetoothAdapter mBluetoothAdapter;

    private FragmentHomeBinding binding;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Home Fragment", "Home Fragment-onCreatedView()");

        // 버튼 및 텍스트뷰 선언
        mBtnBT = root.findViewById(R.id.btnBT);           // 블루투스를 켜는 버튼 ID
        mBtnBT_Connect = root.findViewById(R.id.btnBT_Connect); // 연결 버튼
        
        mTvBT_Status = root.findViewById(R.id.BT_Status);       // 블루투스 상태 텍스트 뷰
        homeText = root.findViewById(R.id.text_home);           // 홈 텍스트 뷰

        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);

        window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ViewModel 선언
        homeViewModel.getBluetoothStatusLiveData().observe(getViewLifecycleOwner(), bluetoothStatus -> mTvBT_Status.setText(bluetoothStatus));
        homeViewModel.getHomeTextLiveData().observe(getViewLifecycleOwner(), text -> homeText.setText(text));
        homeViewModel.getBtBtnLiveData().observe(getViewLifecycleOwner(), text -> mBtnBT.setText(text));

        // 버튼 이벤트 리스너들
        // 블루투스 버튼
        mBtnBT.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            if (!mBluetoothAdapter.isEnabled()) {        // 블루투스가 꺼져있는 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Fragment_BT_on();
                    Fragment_setUIColor();
                    if (mBluetoothAdapter.isEnabled()) {
                        mBtnBT.setBackground(Btn_red);
                    } else {
                        mBtnBT.setBackground(Btn_blue);
                    }
                } else {
                    Fragment_BT_on_Legacy();
                    Fragment_setUIColor();
                    mBtnBT.setBackground(Btn_red);
                }
            } else {        // 블루투스가 켜져있는 경우
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Fragment_BT_off();
                    Fragment_setUIColor();
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBtnBT.setBackground(Btn_blue);
                    }
                } else {
                    Fragment_BT_off_Legacy();
                    Fragment_setUIColor();
                    mBtnBT.setBackground(Btn_red);
                }
            }
        });

        // 연결 버튼
        mBtnBT_Connect.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            Fragment_listPairedDevices();
        });

        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Home Fragment", "Home Fragment-onResume()");

        if (mBluetoothAdapter == null) {
            mTvBT_Status.setText("블루투스 지원하지 않음");
            mBtnBT.setEnabled(false);
            mBtnBT.setText("블루투스 사용불가");
            mBtnBT.setBackground(Btn_red);
            mBtnBT_Connect.setEnabled(false);
        } else {
            if (Fragment_checkBLE() == 2) {
                mTvBT_Status.setText("블루투스 활성화");
                mBtnBT.setText("블루투스 끄기");
                mBtnBT.setBackground(Btn_red);
                mBtnBT_Connect.setEnabled(false);
            } else if (mBluetoothAdapter.isEnabled()) {
                mTvBT_Status.setText("블루투스 활성화");
                mBtnBT.setText("블루투스 끄기");
                mBtnBT.setBackground(Btn_red);
                mBtnBT_Connect.setEnabled(true);
            } else {
                mTvBT_Status.setText("블루투스 비활성화");
                mBtnBT.setText("블루투스 켜기");
                mBtnBT.setBackground(Btn_blue);
                mBtnBT_Connect.setEnabled(false);
            }
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
    public void Fragment_listPairedDevices() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.listPairedDevices();
        }
    }

    public int Fragment_checkBLE() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkBLE();
        } else {
            return 0;
        }
    }

    public void Fragment_setUIColor() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setUIColor();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Home Fragment", "Home Fragment-onDestroyView()");

        binding = null;
    }
}