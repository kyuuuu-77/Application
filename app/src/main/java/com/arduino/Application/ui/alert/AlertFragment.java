package com.arduino.Application.ui.alert;

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
import com.arduino.Application.databinding.FragmentAlertBinding;

public class AlertFragment extends Fragment {

    private FragmentAlertBinding binding;

    Button alertBtn;
    TextView alert;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlertViewModel alertViewModel =
                new ViewModelProvider(requireActivity()).get(AlertViewModel.class);

        binding = FragmentAlertBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Alert Fragment", "Alert Fragment-onCreatedView()");

        // 버튼 및 텍스트 뷰 선언
        alertBtn = root.findViewById(R.id.alert_bag);
        alert = root.findViewById(R.id.text_alert);

        // 버튼 이벤트 리스너
        alertBtn.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");
            alert.setText("버튼이 눌렸습니다!");
        });

        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Alert Fragment", "Alert Fragment-onResume()");

        int menuNum = 4;
        setMenuNum(menuNum);
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

        Log.d("Alert Fragment", "Alert Fragment-onDestroyView()");
    }
}