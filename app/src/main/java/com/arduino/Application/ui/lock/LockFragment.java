package com.arduino.Application.ui.lock;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentLockBinding;

public class LockFragment extends Fragment {

    // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 초기화
    Drawable layout_indigo;
    Drawable layout_orange;
    Drawable Btn_blue;
    Drawable Btn_red;

    private FragmentLockBinding binding;
    private LockViewModel lockViewModel;
    MainActivity mainActivity;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        lockViewModel = new ViewModelProvider(requireActivity()).get(LockViewModel.class);

        binding = FragmentLockBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity) getActivity();

        // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 선언
        layout_indigo = ContextCompat.getDrawable(requireContext(), R.drawable.background_indigo);
        layout_orange = ContextCompat.getDrawable(requireContext(), R.drawable.background_orange);
        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);

        // ViewModel 선언


        // 버튼 이벤트 리스너


        return root;
    }

    @SuppressLint("ResourceAsColor")
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}