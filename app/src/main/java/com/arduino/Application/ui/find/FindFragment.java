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
        });

        final TextView textView = binding.textFind;
        findViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Find Fragment", "Find Fragment-onResume()");

        int menuNum = 2;
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

        Log.d("Find Fragment", "Find Fragment-onDestroyView()");
    }
}