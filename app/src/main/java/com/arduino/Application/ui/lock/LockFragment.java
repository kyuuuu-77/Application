package com.arduino.Application.ui.lock;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    View root;
    MainActivity mainActivity;

    private boolean lockStatus = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LockViewModel lockViewModel = new ViewModelProvider(requireActivity()).get(LockViewModel.class);

        binding = FragmentLockBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        mainActivity = (MainActivity) getActivity();

        // 리니어 레이아웃, 텍스트 뷰, 이미지 뷰, 아이콘, 버튼, Drawable 선언
        Text_lock = root.findViewById(R.id.text_lock);
        Btn_lock = root.findViewById(R.id.lock_btn);

        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);

        // 로디 애니메이션 설정
        LottieAnimationView lottieLock = root.findViewById(R.id.lock_lottie);

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
                lottieLock.playAnimation();
                lottieLock.setRepeatCount(1);
            }
        });

        // 버튼 이벤트 리스너
        Btn_lock.setOnClickListener(view -> lockControl(!lockStatus));

        return root;
    }

    // 캐리어 잠금 여부를 확인하는 메서드
    private void checkLock() {
        LottieAnimationView lottieView = root.findViewById(R.id.lottieView);
        View loadingOverlay = root.findViewById(R.id.loading_overlay);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            loadingOverlay.setVisibility(View.VISIBLE);
            lottieView.setVisibility(View.VISIBLE);
            lottieView.playAnimation();
            Btn_lock.setEnabled(false);
        });
        executorService.execute(() -> {
            // 백그라운드 작업 처리
            if (mainActivity != null) {
                lockStatus = mainActivity.checkLock();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                handler.post(() -> Toast.makeText(getActivity(), "로드중 에러 발생", Toast.LENGTH_SHORT).show());
            }

            handler.post(() -> {
                loadingOverlay.setVisibility(View.GONE);
                lottieView.cancelAnimation();
                lottieView.setVisibility(View.GONE);
                Btn_lock.setEnabled(true);
            });
        });
    }

    // 캐리어를 잠그거나 잠금 해제하는 메서드
    private void lockControl(Boolean onOff) {
        LottieAnimationView lottieView = root.findViewById(R.id.lottieView);
        View loadingOverlay = root.findViewById(R.id.loading_overlay);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(() -> {
            loadingOverlay.setVisibility(View.VISIBLE);
            lottieView.setVisibility(View.VISIBLE);
            lottieView.playAnimation();
            Btn_lock.setEnabled(false);
        });
        executorService.execute(() -> {
            // 백그라운드 작업 처리
            if (mainActivity != null) {
                if (onOff) {        // 잠그는 메서드
                    mainActivity.setLock();
                } else {            // 잠금 해제 메서드
                    mainActivity.setUnlock();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                handler.post(() -> Toast.makeText(getActivity(), "로드중 에러 발생", Toast.LENGTH_SHORT).show());
            }

            handler.post(() -> {
                loadingOverlay.setVisibility(View.GONE);
                lottieView.cancelAnimation();
                lottieView.setVisibility(View.GONE);
                Btn_lock.setEnabled(true);
            });
        });
    }

    private int checkConnection() {
        if (mainActivity != null) {
            return mainActivity.checkConnection();
        } else {
            return -1;
        }
    }

    public void onResume() {
        super.onResume();

        checkLock();
        if (checkConnection() != 9) {
            Toast.makeText(getActivity(), "캐리어와 연결되지 않아 잠금 모드를 사용할 수 없습니다", Toast.LENGTH_SHORT).show();
            Btn_lock.setEnabled(false);
        } else {
            Btn_lock.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}