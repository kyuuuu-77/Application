package com.arduino.Application.ui.find;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentFindBinding;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindFragment extends Fragment {

    // 버튼 및 텍스트 뷰 초기화
    TextView textIgnore;
    TextView textAlert;
    TextView alertStatus;
    TextView distance;

    Button ignoreBtn;
    Button bellBtn;
    Button securityBtn;

    Drawable Btn_blue;
    Drawable Btn_red;
    Drawable find_blue;
    Drawable find_red;
    Drawable ignore_blue;
    Drawable ignore_red;

    FindViewModel findViewModel;

    private boolean security = false;       // security

    private BluetoothAdapter mBluetoothAdapter;

    private FragmentFindBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        findViewModel = new ViewModelProvider(requireActivity()).get(FindViewModel.class);

        binding = FragmentFindBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Find Fragment", "Find Fragment-onCreatedView()");

        // 버튼 및 텍스트 뷰 선언
        textIgnore = root.findViewById(R.id.text_ignore);
        textAlert = root.findViewById(R.id.text_alert);
        alertStatus = root.findViewById(R.id.alert_status);
        distance = root.findViewById(R.id.find_distance);

        ignoreBtn = root.findViewById(R.id.ignore);
        bellBtn = root.findViewById(R.id.bell);
        securityBtn = root.findViewById(R.id.alertBtn);

        // Drawable 선언
        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);
        find_blue = ContextCompat.getDrawable(requireContext(), R.drawable.find_bell_on);
        find_red = ContextCompat.getDrawable(requireContext(), R.drawable.find_bell_off);
        ignore_blue = ContextCompat.getDrawable(requireContext(), R.drawable.find_ignore_off);
        ignore_red = ContextCompat.getDrawable(requireContext(), R.drawable.find_ignore_on);

        // ViewModel 선언
        findViewModel.getAlertTextLiveData().observe(getViewLifecycleOwner(), text -> textAlert.setText(text));
        findViewModel.getAlertStatusLiveData().observe(getViewLifecycleOwner(), status -> alertStatus.setText(status));
        findViewModel.getDistanceLiveData().observe(getViewLifecycleOwner(), bag_distance -> {
            if (Objects.equals(bag_distance, "캐리어와 떨어져 있음")) {
                distance.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange_500));
            } else if (Objects.equals(bag_distance, "캐리어와 멂")) {
                distance.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_500));
            } else if (Objects.equals(bag_distance, "캐리어와 매우 가까움")) {
                distance.setTextColor(ContextCompat.getColor(requireActivity(), R.color.blue_500));
            } else if (Objects.equals(bag_distance, "캐리어와 가까움")) {
                distance.setTextColor(ContextCompat.getColor(requireActivity(), R.color.green_500));
            } else {
                distance.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black));
            }
            distance.setText(bag_distance);
        });
        findViewModel.getIgnoreTextLiveData().observe(getViewLifecycleOwner(), ignore -> {
            if (Objects.equals(ignore, "알림")) {
                ignoreBtn.setBackground(ignore_blue);
            } else if(Objects.equals(ignore, "무시")) {
                ignoreBtn.setBackground(ignore_red);
            }
            textIgnore.setText(ignore);
        });
        findViewModel.getAlertBtntextLiveData().observe(getViewLifecycleOwner(), btnText -> {
            if (Objects.equals(btnText, "도난방지 사용불가")) {
                securityBtn.setBackground(Btn_red);
                securityBtn.setEnabled(false);
            } else if (Objects.equals(btnText, "도난방지 켜기")) {       // 도난방지 켜기
                securityBtn.setBackground(Btn_blue);
            } else {        // 도난방지 끄기
                securityBtn.setBackground(Btn_red);
            }
            securityBtn.setText(btnText);
        });

        // 블루투스 어뎁터 초기화
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 버튼 이벤트 리스너
        // 도난방지 무시 버튼
        ignoreBtn.setOnClickListener(view -> {
            ignoreAlert();
            checkIgnore();
        });

        // 벨 울리는 버튼
        bellBtn.setOnClickListener(view -> {
            findViewModel.setAlertText("벨 울리기 시도중...");
            Toast.makeText(getActivity(), "벨 울리기 시도중...", Toast.LENGTH_SHORT).show();
            ringBell(true);
        });

        // 도난방지 버튼
        securityBtn.setOnClickListener(view -> {
            if (security) {     // 도난방지가 켜져있는 경우 -> 끄기
                security_OFF();
                findViewModel.setAlertBtnText("도난방지 켜기");
                Toast.makeText(getActivity(), "도난방지를 사용하지 않습니다.", Toast.LENGTH_SHORT).show();
            } else {            // 도난방지가 꺼져있는 경우 -> 켜기
                security_ON();
                findViewModel.setAlertBtnText("도난방지 끄기");
                Toast.makeText(getActivity(), "도난방지를 사용합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    // 벨을 울리는 메서드
    private void ringBell(boolean onOff) {
        // 로딩 애니메이션 (로티 애니메이션) 및 비동기 처리 구문
        MainActivity mainActivity = (MainActivity) getActivity();
        View root = binding.getRoot();

        LottieAnimationView lottieView = root.findViewById(R.id.lottieView);
        View loadingOverlay = root.findViewById(R.id.loading_overlay);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        if (mainActivity != null) {
            handler.post(() -> {
                loadingOverlay.setVisibility(View.VISIBLE);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();
                securityBtn.setEnabled(false);
            });
            executorService.execute(() -> {
                // 백그라운드 작업 처리
                if (onOff) {   // 벨 울리기 동작
                    int status = mainActivity.ringBell(true);
                    if (status == 1) {  // 벨 울리기 성공한 경우
                        handler.post(() -> showCustomDialog(3));
                    } else {    // 벨 울리기 실패한 경우
                        handler.post(() -> showCustomDialog(2));
                    }
                } else {    // 벨 울리기 멈춤
                    while (true) {
                        int status = mainActivity.ringBell(false);
                        if (status != 2) {
                            SystemClock.sleep(5000);
                            Toast.makeText(getActivity(), "벨 중지에 실패했습니다. 5초후에 다시 시도합니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            break;
                        }
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    handler.post(() -> Toast.makeText(getActivity(), "데이터 로드중 에러 발생", Toast.LENGTH_SHORT).show());
                }

                handler.post(() -> {
                    loadingOverlay.setVisibility(View.GONE);
                    lottieView.cancelAnimation();
                    lottieView.setVisibility(View.GONE);
                    securityBtn.setEnabled(true);
                });
            });
        }
    }

    // BLE 연결 여부를 체크하는 메서드
    private int checkBLE() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkBLE();
        } else {
            return -1;
        }
    }

    // 도난방지 여부를 체크하는 메서드
    private void checkSecurity() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.checkSecurity();
        }
    }

    // 도난방지를 켜는 메서드
    private void security_ON() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.security_ON();
        }
    }

    // 도난방지를 끄는 메서드
    private void security_OFF() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            security = mainActivity.security_OFF();
        }
    }

    // 도난방지 경고 무시 설정 메서드
    private void ignoreAlert() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.ignoreAlert();
        }
    }

    // 도난방지 무시 여부 체크 메서드
    private void checkIgnore() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.checkIgnore();
        }
    }

    // 커스텀 다이얼로그를 표시하는 메서드
    private void showCustomDialog(int status) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        // 다이얼로그 안에 이미지, 텍스트 뷰, 버튼 초기화 및 선언
        ImageView iconView = dialogView.findViewById(R.id.dialog_icon);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageTextView = dialogView.findViewById(R.id.dialog_message_text);
        ImageView messageImageView = dialogView.findViewById(R.id.dialog_message_image);

        Button retryBtn = dialogView.findViewById(R.id.retry);
        Button checkBtn = dialogView.findViewById(R.id.confirm);
        Button cancelBtn = dialogView.findViewById(R.id.cancel);

        // 로티 애니메이션
        LottieAnimationView lottieView = dialogView.findViewById(R.id.dialog_message_lottie);

        // 텍스트, 이미지, 버튼 설정
        switch (status) {
            case 3:
                lottieView.setAnimation(R.raw.ring_bell);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();

                iconView.setImageResource(R.drawable.dialog_bell);
                titleView.setText("벨을 울리는 중...");
                messageTextView.setText("캐리어에서 벨이 울리고 있습니다.\n벨은 하단의 확인 버튼을 누를 때까지 계속 울립니다.");
                messageImageView.setVisibility(View.GONE);
                retryBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                break;
            case 2:
                lottieView.setAnimation(R.raw.network_error1);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();

                iconView.setImageResource(R.drawable.dialog_bell_error);
                titleView.setText("벨 울리기 실패!");
                messageTextView.setText("벨 울리기 동작에 실패했습니다.\n스마트 캐리어와 연결되어 있고 통신 상태가 양호한지 확인 후\n다시 시도하세요.");
                messageImageView.setVisibility(View.GONE);
                checkBtn.setVisibility(View.GONE);
                break;
            case 1:
                lottieView.setAnimation(R.raw.bluetooth_on);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();

                iconView.setImageResource(R.drawable.info_bt_off);
                titleView.setText("도난방지 및 찾기 비활성화");
                messageTextView.setText("블루투스가 꺼져 있어 도난방지 기능을 사용할 수 없습니다.\n블루투스를 켠 후에 다시 시도하세요.");
                messageImageView.setVisibility(View.GONE);
                retryBtn.setVisibility(View.GONE);
                checkBtn.setVisibility(View.GONE);
                break;
            case 0:
                lottieView.setAnimation(R.raw.bluetooth_on);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();

                iconView.setImageResource(R.drawable.info_bt_on);
                titleView.setText("도난방지 및 찾기 비활성화");
                messageTextView.setText("스마트 캐리어에 연결되지 않아 도난방지 기능을 사용할 수 없습니다.\n연결 후에 다시 시도하세요.");
                messageImageView.setVisibility(View.GONE);
                retryBtn.setVisibility(View.GONE);
                checkBtn.setVisibility(View.GONE);
                break;
        }
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        retryBtn.setOnClickListener(v -> {
            ringBell(true);
            dialog.dismiss();
        });
        checkBtn.setOnClickListener(v -> {
            ringBell(false);
            dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
    }

    public void onResume() {
        super.onResume();
        Log.d("Find Fragment", "Find Fragment-onResume()");

        checkSecurity();
        checkIgnore();

        if (!mBluetoothAdapter.isEnabled()) {
            showCustomDialog(1);
            bellBtn.setEnabled(false);
            bellBtn.setBackground(find_red);
            findViewModel.setAlertBtnText("도난방지 사용불가");
        } else if (checkBLE() == 2) {
            bellBtn.setEnabled(true);
            bellBtn.setBackground(find_blue);
            securityBtn.setEnabled(true);
            if (security) {     // 도난방지가 켜져있는 경우
                findViewModel.setAlertBtnText("도난방지 끄기");
            } else {            // 도난방지가 꺼져있는 경우
                findViewModel.setAlertBtnText("도난방지 켜기");
            }
        } else {
            showCustomDialog(0);
            bellBtn.setEnabled(false);
            bellBtn.setBackground(find_red);
            findViewModel.setAlertBtnText("도난방지 사용불가");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Find Fragment", "Find Fragment-onDestroyView()");
    }
}