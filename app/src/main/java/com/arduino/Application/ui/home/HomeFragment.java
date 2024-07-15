package com.arduino.Application.ui.home;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

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

    //버튼 변수 초기화
    Button mBtnBT_on;
    Button mBtnBT_off;
    Button mBtnBT_Connect;
    Button mBtnSendData;
    TextView mTvBT_Status;
    TextView homeText;
    TextView rssiTextView;

    Window window;
    private Handler handler;
    private Runnable runnable;

    //Toolbar toolbar;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Home Fragment", "Home Fragment-onCreatedView()");

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //Edited-Start
        mBtnBT_on = root.findViewById(R.id.btnBT_On);    //블루투스를 켜는 버튼 ID
        mBtnBT_off = root.findViewById(R.id.btnBT_Off);  //블루투스를 끄는 버튼 ID
        mBtnBT_Connect = root.findViewById(R.id.btnBT_Connect);  //연결 버튼
        mBtnSendData = root.findViewById(R.id.btnSendData);  //전송 버튼
        mTvBT_Status = root.findViewById(R.id.BT_Status);    //블루투스 상태 텍스트 뷰
        homeText = root.findViewById(R.id.text_home);        //홈 텍스트 표시 (나중에 제거 예정)
        rssiTextView = root.findViewById(R.id.rssi); //RSSI 상태 텍스트 뷰

        window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //버튼 이벤트 리스너들 (람다 함수로 수정됨)
        //블루투스 ON 버튼
        mBtnBT_on.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Fragment_BT_on();
            } else {
                Fragment_BT_on_Legacy();
            }
        });
        //블루투스 OFF 버튼
        mBtnBT_off.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Fragment_BT_off();
            } else {
                Fragment_BT_off_Legacy();
            }
        });
        //연결 버튼
        mBtnBT_Connect.setOnClickListener(view -> Fragment_listPairedDevices());
        mBtnSendData.setEnabled(false); //전송 버튼 필요 없으므로 임시 비활성화 -> 나중에 삭제 예정

        //전송 버튼
        /*
        mBtnSendData.setOnClickListener(view -> {
            if (mThreadConnectedBluetooth != null) {
                String cmdText = mTv_SendData.getText().toString();
                for (int i = 0; i < cmdText.length(); i++) {
                    mThreadConnectedBluetooth.write(cmdText.substring(i, i + 1));
                }
                mTv_SendData.setText("");
            }
        });
        */
        mBtnBT_Connect.setEnabled(false);
        //Edited-End

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Bundle args = getArguments();
                if (args != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String BT_Status = args.getString("BT_Status");
                            String Home = args.getString("homeText");
                            String Rssi = args.getString("Rssi");

                            mTvBT_Status.setText(BT_Status);
                            homeText.setText(Home);
                            rssiTextView.setText(Rssi);
                        }
                    });
                }
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(runnable);

        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Home Fragment", "Home Fragment-onResume()");

        if (mBluetoothAdapter == null) {
            mTvBT_Status.setText("블루투스 지원하지 않음");
            window.setStatusBarColor(Color.parseColor("#D32F2F"));
            mBtnBT_on.setEnabled(false);
            mBtnBT_off.setEnabled(false);
            mBtnBT_Connect.setEnabled(false);
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                mTvBT_Status.setText("블루투스 활성화");
                window.setStatusBarColor(Color.parseColor("#1976D2"));
                mBtnBT_on.setEnabled(false);
                mBtnBT_off.setEnabled(true);
                mBtnBT_Connect.setEnabled(true);
            } else {
                mTvBT_Status.setText("블루투스 비활성화");
                window.setStatusBarColor(Color.parseColor("#F57C00"));
                mBtnBT_on.setEnabled(true);
                mBtnBT_off.setEnabled(false);
                mBtnBT_Connect.setEnabled(false);
            }
        }

        /*
        if (mBluetoothAdapter.isEnabled()) {
            homeText.setText("연결 되어있습니다.");
            mTvBT_Status.setText("활성화");
            window.setStatusBarColor(Color.parseColor("#1976D2"));
            //toolbar.setBackgroundColor(Color.parseColor("#2196F3"));
            mBtnBT_on.setEnabled(false);
            mBtnBT_off.setEnabled(true);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            homeText.setText("연결이 해제 되어있습니다.");
            mTvBT_Status.setText("비활성화");
            window.setStatusBarColor(Color.parseColor("#F57C00"));
            //toolbar.setBackgroundColor(Color.parseColor("#FF9800"));
            mBtnBT_on.setEnabled(true);
            mBtnBT_off.setEnabled(false);
        }
        */
    }

    public void TextView_BT_Status(String text){
        if(mTvBT_Status != null){
            mTvBT_Status.setText(text);
        }
    }

    public void TextView_Rssi(String text){
        if(rssiTextView != null){
            rssiTextView.setText(text);
        }
    }

    public void TextView_HomeText(String text){
        if(homeText != null){
            homeText.setText(text);
        }
    }

    public void setBluetoothComponent(BluetoothAdapter bluetoothAdapter, BluetoothManager bluetoothManager){
        mBluetoothAdapter = bluetoothAdapter;
        mBluetoothManager = bluetoothManager;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        handler.removeCallbacks(runnable);
        Log.d("Home Fragment", "Home Fragment-onDestroyView()");
    }
}