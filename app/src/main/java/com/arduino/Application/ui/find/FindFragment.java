package com.arduino.Application.ui.find;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindFragment extends Fragment {

    // 이미지 뷰, 버튼, 텍스트 뷰 및 Drawable 초기화
    ImageView security_icon;
    ImageView distance_icon;

    TextView textIgnore;
    TextView securityMain;
    TextView securitySub;
    TextView distanceMain;
    TextView distanceSub;

    Button Btn_ignore;
    Button Btn_bell;
    Button Btn_security;

    Drawable Btn_blue;
    Drawable Btn_red;
    Drawable find_blue;
    Drawable find_red;
    Drawable ignore_blue;
    Drawable ignore_red;

    private LineChart lineChart;

    private boolean security = false;       // security

    private BluetoothAdapter mBluetoothAdapter;

    private FragmentFindBinding binding;
    FindViewModel findViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        findViewModel = new ViewModelProvider(requireActivity()).get(FindViewModel.class);

        binding = FragmentFindBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 이미지 뷰, 텍스트 뷰, 버튼 및 Drawable 선언
        security_icon = root.findViewById(R.id.security_icon);
        distance_icon = root.findViewById(R.id.distance_icon);

        textIgnore = root.findViewById(R.id.text_ignore);
        securityMain = root.findViewById(R.id.alert_status);
        securitySub = root.findViewById(R.id.security_sub);
        distanceMain = root.findViewById(R.id.find_distance);
        distanceSub = root.findViewById(R.id.distance_sub);

        Btn_ignore = root.findViewById(R.id.ignore);
        Btn_bell = root.findViewById(R.id.bell);
        Btn_security = root.findViewById(R.id.alertBtn);

        // Drawable 선언
        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);
        find_blue = ContextCompat.getDrawable(requireContext(), R.drawable.find_bell_on);
        find_red = ContextCompat.getDrawable(requireContext(), R.drawable.find_bell_off);
        ignore_blue = ContextCompat.getDrawable(requireContext(), R.drawable.find_ignore_off);
        ignore_red = ContextCompat.getDrawable(requireContext(), R.drawable.find_ignore_on);

        // 그래프 선언
        lineChart = root.findViewById(R.id.lineChart);

        // 그래프 정의
        List<Entry> entries = new ArrayList<>();

        int[] values = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};       // 신호세기 값을 저장할 배열 (나중에 전역변수로 변환 필요)
        for (int i = 0; i < values.length; i++) {
            entries.add(new Entry(i, values[i]));       // Entry 객체를 통해 x, y 값을 추가
        }

        LineDataSet dataSet = new LineDataSet(entries, "캐리어 신호세기");
        LineData lineData = new LineData(dataSet);

        Description description = new Description();
        description.setEnabled(false);      // 설명 라벨 제거
        lineChart.setDescription(description);

        dataSet.setLineWidth(2f);
        dataSet.setColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
        dataSet.setCircleColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true); // 그래프 아래 영역을 채우기
        dataSet.setFillColor(ContextCompat.getColor(requireActivity(), R.color.indigo_100)); // 채우기 색상 설정

        // 그래프 X축
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new CustomXAxisValueFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(10f);
        xAxis.setTextSize(12f);

        // 그래프 Y축
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setValueFormatter(new CustomYAxisValueFormatter());
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(6f);
        yAxis.setGranularity(1f);
        yAxis.setLabelCount(7, true);
        yAxis.setTextSize(12f);
        yAxis.setDrawGridLines(true);
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.setData(lineData);
        lineChart.invalidate();

        // 그래프를 1초마다 갱신하기 위한 Handler와 Runnable
        Handler signalGraph_handler = new Handler();
        Runnable signalGraph_handler_runnable = new Runnable() {
            @Override
            public void run() {
                entries.clear();

                // 0 ~ 10 까지, length = 11
                for (int i = values.length - 1; i > 0 ; i--) {
                    values[i] = values[i - 1];
                }
                values[0] = getRSSIStrength();      // 현재의 신호세기 값을 입력

                for (int i = 0; i < values.length; i++) {
                    entries.add(new Entry(i, values[i])); // Entry 객체를 통해 x, y 값을 추가
                }

                dataSet.notifyDataSetChanged(); // 데이터셋 변경 알림
                lineChart.notifyDataSetChanged(); // 차트 데이터 변경 알림
                lineChart.invalidate(); // 차트 다시 그리기

                signalGraph_handler.postDelayed(this, 1000);
            }
        };
        signalGraph_handler.postDelayed(signalGraph_handler_runnable, 1000);

        // ViewModel 선언
        // 알림 버튼 상태
        findViewModel.getIgnoreLiveData().observe(getViewLifecycleOwner(), ignore -> {
            if (ignore) {     // 무시 상태이면
                textIgnore.setText("무시");
                Btn_ignore.setBackground(ignore_red);
            } else {
                textIgnore.setText("알림");
                Btn_ignore.setBackground(ignore_blue);
            }
        });

        // 도난방지 상태
        findViewModel.getAlertStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status) {       // 도난방지가 켜져 있으면
                securityMain.setText("동작중");
                securitySub.setText("도난방지 켜짐");
                security_icon.setImageResource(R.drawable.info_security_on);
            } else {        // 도난방지가 꺼져 있으면
                securityMain.setText("사용안함");
                securitySub.setText("도난방지 꺼짐");
                security_icon.setImageResource(R.drawable.info_security_off);
            }
        });

        // 캐리어 거리
        findViewModel.getDistanceLiveData().observe(getViewLifecycleOwner(), bag_distance -> {
            switch (bag_distance) {
                case -1:
                    distanceMain.setText("정보없음");
                    distanceSub.setText("거리 측정 불가");
                    distanceMain.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black));
                    distanceSub.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black));
                    distance_icon.setImageResource(R.drawable.find_distance_off);
                    break;
                case 0:
                    distanceMain.setText("바로 앞");
                    distanceSub.setText("매우 가까움");
                    distanceMain.setTextColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
                    distanceSub.setTextColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
                    distance_icon.setImageResource(R.drawable.find_distance_on);
                    break;
                case 1:
                    distanceMain.setText("근처");
                    distanceSub.setText("가까움");
                    distanceMain.setTextColor(ContextCompat.getColor(requireActivity(), R.color.blue_500));
                    distanceSub.setTextColor(ContextCompat.getColor(requireActivity(), R.color.blue_500));
                    distance_icon.setImageResource(R.drawable.find_distance_on);
                    break;
                case 2:
                    distanceMain.setText("거리있음");
                    distanceSub.setText("거리가 있음");
                    distanceMain.setTextColor(ContextCompat.getColor(requireActivity(), R.color.green_500));
                    distanceSub.setTextColor(ContextCompat.getColor(requireActivity(), R.color.green_500));
                    distance_icon.setImageResource(R.drawable.find_distance_on);
                    break;
                case 3:
                    distanceMain.setText("떨어짐");
                    distanceSub.setText("약간 멂");
                    distanceMain.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange_500));
                    distanceSub.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange_500));
                    distance_icon.setImageResource(R.drawable.find_distance_on);
                    break;
                case 4:
                    distanceMain.setText("매우 멂");
                    distanceSub.setText("도난 위험 있음");
                    distanceMain.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_500));
                    distanceSub.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_500));
                    distance_icon.setImageResource(R.drawable.find_distance_on);
                    break;
            }
        });

        // 도난방지 버튼 상태
        findViewModel.getAlertBtnLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status == -1) {     // 도난방지 사용불가
                Btn_security.setText("도난방지 사용불가");      
                Btn_security.setBackground(Btn_red);
                Btn_security.setEnabled(false);
            } else if (status == 0) {       // 도난방지 켜기
                Btn_security.setText("도난방지 켜기");
                Btn_security.setBackground(Btn_blue);
            } else {        // 도난방지 끄기
                Btn_security.setText("도난방지 끄기");
                Btn_security.setBackground(Btn_red);
            }
        });

        // 블루투스 어뎁터 초기화
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 버튼 이벤트 리스너
        // 도난방지 무시 버튼
        Btn_ignore.setOnClickListener(view -> {
            ignoreAlert();
            checkIgnore();
        });

        // 벨 울리는 버튼
        Btn_bell.setOnClickListener(view -> {
            Toast.makeText(getActivity(), "벨 울리기 시도중...", Toast.LENGTH_SHORT).show();
            ringBell(true);
        });

        // 도난방지 버튼
        Btn_security.setOnClickListener(view -> {
            if (security) {     // 도난방지가 켜져있는 경우 -> 끄기
                security_OFF();
                findViewModel.setAlertBtn(0);
            } else {            // 도난방지가 꺼져있는 경우 -> 켜기
                security_ON();
                findViewModel.setAlertBtn(1);
            }
        });

        return root;
    }

    // 그래프 Y축 글자 설정
    public static class CustomYAxisValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            switch ((int) value) {
                case 1:
                    return "매우 낮음";
                case 2:
                    return "낮음";
                case 3:
                    return "중간";
                case 4:
                    return "높음";
                case 5:
                    return "매우 높음";
                default:
                    return "";
            }
        }
    }

    // 그래프 X축 글자 설정
    public static class CustomXAxisValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            if ((int) value == 0) {
                return "현재";
            }
            return (int) value + "초전";
        }
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
                Btn_security.setEnabled(false);
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
//                            Toast.makeText(getActivity(), "벨 중지에 실패했습니다. 5초후에 다시 시도합니다.", Toast.LENGTH_SHORT).show();
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
                    Btn_security.setEnabled(true);
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

    // rssi 신호값을 매인 액티비티에서 불러오는 메서드
    private int getRSSIStrength() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.getRSSIStrength();
        } else {
            return 0;
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

        checkSecurity();
        checkIgnore();

        if (!mBluetoothAdapter.isEnabled()) {
            showCustomDialog(1);
            Btn_bell.setEnabled(false);
            Btn_bell.setBackground(find_red);
            findViewModel.setAlertBtn(-1);
        } else if (checkBLE() == 2) {
            Btn_bell.setEnabled(true);
            Btn_bell.setBackground(find_blue);
            Btn_security.setEnabled(true);
            if (security) {     // 도난방지가 켜져있는 경우
                findViewModel.setAlertBtn(1);
            } else {            // 도난방지가 꺼져있는 경우
                findViewModel.setAlertBtn(0);
            }
        } else {
            showCustomDialog(0);
            Btn_bell.setEnabled(false);
            Btn_bell.setBackground(find_red);
            findViewModel.setAlertBtn(-1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}