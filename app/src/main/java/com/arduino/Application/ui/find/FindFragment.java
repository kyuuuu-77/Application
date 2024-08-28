package com.arduino.Application.ui.find;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentFindBinding;

public class FindFragment extends Fragment {

    private FragmentFindBinding binding;

    // 버튼 및 텍스트 뷰 초기화
    TextView textAlert;
    TextView alertStatus;
    Button security;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FindViewModel findViewModel =
                new ViewModelProvider(requireActivity()).get(FindViewModel.class);

        binding = FragmentFindBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Find Fragment", "Find Fragment-onCreatedView()");

        // 버튼 및 텍스트 뷰 선언
        textAlert = root.findViewById(R.id.text_alert);
        alertStatus = root.findViewById(R.id.alert_status);
        security = root.findViewById(R.id.alertBtn);

        // ViewModel과 UI 요소 바인딩
        findViewModel.getAlertTextLiveData().observe(getViewLifecycleOwner(), text -> textAlert.setText(text));
        findViewModel.getAlertStatusLiveData().observe(getViewLifecycleOwner(), status -> security.setText(status));
        findViewModel.getAlertBtnLiveData().observe(getViewLifecycleOwner(), btn -> security.setText(btn));

        // 버튼 이벤트 리스너
        security.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");
            textAlert.setText("찾기 시도중...");
            Toast.makeText(getActivity(), "찾기 시도중...", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Find Fragment", "Find Fragment-onResume()");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Find Fragment", "Find Fragment-onDestroyView()");
    }
}