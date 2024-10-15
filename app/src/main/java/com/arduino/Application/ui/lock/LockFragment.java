package com.arduino.Application.ui.lock;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentLockBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LockFragment extends Fragment {

    // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 초기화
    TextView Text_lock;
    Button Btn_lock;
    
    Drawable Btn_blue;
    Drawable Btn_red;

    private FragmentLockBinding binding;
    private LockViewModel lockViewModel;
    MainActivity mainActivity;
    
    private boolean lockStatus = false;

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        lockViewModel = new ViewModelProvider(requireActivity()).get(LockViewModel.class);

        binding = FragmentLockBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity) getActivity();

        // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 선언
        Text_lock = root.findViewById(R.id.text_lock);
        Btn_lock = root.findViewById(R.id.lock_btn);
        
        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);

        // 로디 애니메이션 설정
        LottieAnimationView lottieLock = root.findViewById(R.id.lock_lottie);

        // 비동기 설정
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        // ViewModel 선언
        lockViewModel.getLockStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            lockStatus = status;
            if (lockStatus) {   // 캐리어가 잠김 경우
                Text_lock.setText("잠금 상태");
                Text_lock.setTextColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
                Btn_lock.setText("캐리어 잠금 해제");
                Btn_lock.setBackground(Btn_red);

                lottieLock.setAnimation(R.raw.lock_on);
                lottieLock.playAnimation();
                lottieLock.setRepeatCount(LottieDrawable.INFINITE);
            } else {        // 캐리어가 잠기지 않은 경우
                Text_lock.setText("잠금 해제 상태");
                Text_lock.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange_500));
                Btn_lock.setText("캐리어 잠그기");
                Btn_lock.setBackground(Btn_blue);

                lottieLock.setAnimation(R.raw.lock_off);
                lottieLock.cancelAnimation();
            }
        });

        // 버튼 이벤트 리스너
        Btn_lock.setOnClickListener(view -> {
            if (lockStatus) {       // 잠긴 상태일 경우에 잠금해제
                lockControl(false);
            } else {        // 잠기지 않은 상태일 경우에 잠금
                lockControl(true);
            }
        });

        return root;
    }

    // 캐리어 잠금 여부를 확인하는 메서드
    private void checkLock() {
        if (mainActivity != null) {
            lockStatus = mainActivity.checkLock();
        }
    }
    
    private void lockControl(Boolean onOff) {
        if (mainActivity != null) {
            if (onOff) {        // 잠그는 메서드
                mainActivity.setLock();
            } else {            // 잠금 해제 메서드
                mainActivity.setUnlock();
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    public void onResume() {
        super.onResume();

        checkLock();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}