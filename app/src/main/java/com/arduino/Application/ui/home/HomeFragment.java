package com.arduino.Application.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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

        /*
        //Edited-Start
        mBtnBT_on = root.findViewById(R.id.btnBT_On);    //블루투스를 켜는 버튼 ID
        mBtnBT_off = root.findViewById(R.id.btnBT_Off);  //블루투스를 끄는 버튼 ID
        mBtnBT_Connect = root.findViewById(R.id.btnBT_Connect);  //연결 버튼
        mBtnSendData = root.findViewById(R.id.btnSendData);  //전송 버튼
        mTvBT_Status = root.findViewById(R.id.BT_Status);    //블루투스 상태 텍스트 뷰
        homeText = root.findViewById(R.id.text_home);        //홈 텍스트 표시 (나중에 제거 예정)
        rssiTextView = root.findViewById(R.id.rssi); //RSSI 상태 텍스트 뷰

        //Edited-End
         */

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Home Fragment", "Home Fragment-onDestroyView()");
    }
}