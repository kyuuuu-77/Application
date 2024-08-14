package com.arduino.Application.ui.alert;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentFindBinding;

public class AlertFragment extends Fragment {

    private FragmentFindBinding binding;

    private int menuNum;

    Button findBtn;
    TextView textFind;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlertViewModel findViewModel =
                new ViewModelProvider(requireActivity()).get(AlertViewModel.class);

        binding = FragmentFindBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Alert Fragment", "Alert Fragment-onCreatedView()");

        // 버튼 및 텍스트 뷰 선언
        findBtn = root.findViewById(R.id.find_bag);
        textFind = root.findViewById(R.id.text_find);

        // ViewModel 설정

        // 버튼 이벤트 리스너
        findBtn.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");
            textFind.setText("버튼이 눌렸습니다!");
        });

        final TextView textView = binding.textFind;
        findViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Alert Fragment", "Alert Fragment-onResume()");

        menuNum = 4;
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