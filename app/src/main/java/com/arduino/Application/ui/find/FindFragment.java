package com.arduino.Application.ui.find;

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

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentFindBinding;

public class FindFragment extends Fragment {

    private FragmentFindBinding binding;

    private int menuNum;
    private int findDevice = 0;

    Button findBtn;
    TextView textFind;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FindViewModel findViewModel =
                new ViewModelProvider(this).get(FindViewModel.class);

        binding = FragmentFindBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Find Fragment", "Find Fragment-onCreatedView()");

        // 버튼 및 텍스트 뷰 선언
        findBtn = root.findViewById(R.id.find_bag);
        textFind = root.findViewById(R.id.text_find);

        // 버튼 이벤트 리스너
        findBtn.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");
            textFind.setText("버튼이 눌렸습니다!");
            if (findDevice == 1){
                findDevice = 0;
            } else{
                findDevice = 1;
            }
            sendData_local(findDevice);
        });

        final TextView textView = binding.textFind;
        findViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Find Fragment", "Find Fragment-onResume()");

        menuNum = 2;
        setMenuNum(menuNum);
    }

    //MainActivity를 통해서 블루투스 데이터 송수신
    // 송신 메서드
    private void sendData_local(int findDevice){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            int data;

            if (findDevice == 1){
                // 알람이 울리게 동작
                data = mainActivity.sendData();
                if (data == 1){
                    Log.d("Alert-ON", "알람 컨트롤 성공!");
                }else{
                    Log.d("Alert-ON", "알람 컨트롤 실패...");
                }
            } else{
                // 알람이 멈추게 동작
                data = mainActivity.sendData();
                if (data == 1){
                    Log.d("Alert-ON", "알람 컨트롤 성공!");
                }else{
                    Log.d("Alert-OFF", "알람 컨트롤 실패...");
                }
            }
        }
    }
    
    // 수신 메서드
    private void receiveData_local(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            double data;
            Log.d("receiveData", "데이터 수신 성공");
            data = mainActivity.receiveData();
        }
    }

    // 메뉴 설정 메서드
    private void setMenuNum(int num){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setMenuNum(num);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Find Fragment", "Find Fragment-onDestroyView()");
    }
}