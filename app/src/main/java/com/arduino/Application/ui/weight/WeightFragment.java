package com.arduino.Application.ui.weight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.NotificationActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentWeightBinding;

public class WeightFragment extends Fragment {

    // 버튼 및 텍스트뷰 초기화
    TextView weightNow;
    TextView weightTps;
    TextView looseWeight;
    Button mBtnWeight;

    private FragmentWeightBinding binding;

    private int menuNum;
    private double[] weight = {0, 0};   // weight, tps

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WeightViewModel weightViewModel =
                new ViewModelProvider(requireActivity()).get(WeightViewModel.class);

        binding = FragmentWeightBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Weight Fragment", "Weight Fragment-onCreatedView()");

        // 버튼 및 텍스트뷰 선언
        weightNow = root.findViewById(R.id.text_weightnow);         // 현재 무게정보 텍스트뷰
        weightTps = root.findViewById(R.id.text_weightps);          // 허용 무게 텍스트 뷰
        looseWeight = root.findViewById(R.id.text_looseweight);     // 초과 무게 텍스트 뷰
        mBtnWeight = root.findViewById(R.id.weight_btn);            // 무게 측정 시작 버튼

        // ViewModel 설정
        weightViewModel.getWeightNowLiveData().observe(getViewLifecycleOwner(), weight -> weightNow.setText(weight));
        weightViewModel.getWeightTpsLiveData().observe(getViewLifecycleOwner(), tps -> weightTps.setText(tps));
        weightViewModel.getLooseWeightLiveData().observe(getViewLifecycleOwner(), loose -> looseWeight.setText(loose));
        weightViewModel.getWeightBtnLiveData().observe(getViewLifecycleOwner(), btn -> mBtnWeight.setText(btn));

        // 버튼 이벤트 리스너
        // 무게 측정 버튼
        mBtnWeight.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");
            measureWeight();
        });

        // 알림 버튼 설정
        //root.findViewById(R.id.weight_btn).setOnClickListener(v -> createNotif());

        return root;
    }

    @SuppressLint("SetTextI18n")
    private void measureWeight(){
        MainActivity mainActivity = (MainActivity) getActivity();
        double maxTps = 20.0;
        weight[1] = maxTps;
        if (mainActivity != null) {
            weight[0] = mainActivity.measureWeight(maxTps);
            if (weight[0] > 32.0) {
                showWarningDialog();
                looseWeight.setText("32Kg을 초과했습니다.");
                weightNow.setTextColor(Color.parseColor("#FF3700B3"));
            } else if (weight[0] > maxTps) {
                looseWeight.setText(maxTps+"Kg을 초과했습니다.");
                weightNow.setTextColor(Color.parseColor("#D32F2F"));
            } else if (weight[0] > maxTps - 2) {
                looseWeight.setText("허용 무게를 초과하지 않았습니다.");
                weightNow.setTextColor(Color.parseColor("#F57C00"));
            } else {
                looseWeight.setText("허용 무게를 초과하지 않았습니다.");
            }
        }
    }

    private double[] checkWeightSetting(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkWeightSetting();
        }
        return null;
    }

    private void showWarningDialog(){
        new AlertDialog.Builder(getContext())
                .setTitle("경고!").setMessage("무게가 32Kg을 초과하였습니다.\nIATA 규정으로 인하여 32Kg 이상의 수화물은 항공기에 위탁 수화물로 맡길 수 없습니다.")
                .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                .create().show();
    }

    @SuppressLint("SetTextI18n")
    public void onResume() {
        super.onResume();
        Log.d("Weight Fragment", "Weight Fragment-onResume()");

        menuNum = 3;
        setMenuNum(menuNum);

        weight = checkWeightSetting();

        if (weight != null && weight[0] != 0) {
            double maxTps = weight[1];
            if (weight[0] > 32.0) {
                showWarningDialog();
                looseWeight.setText("32Kg을 초과했습니다.");
                weightNow.setTextColor(Color.parseColor("#FF3700B3"));
            } else if (weight[0] > maxTps) {
                looseWeight.setText(maxTps+"Kg을 초과했습니다.");
                weightNow.setTextColor(Color.parseColor("#D32F2F"));
            } else if (weight[0] > maxTps - 2) {
                looseWeight.setText("허용 무게를 초과하지 않았습니다.");
                weightNow.setTextColor(Color.parseColor("#F57C00"));
            } else {
                looseWeight.setText("허용 무게를 초과하지 않았습니다.");
            }
        }
    }

    // 송신 메서드
    private void sendData_local(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            int data;
            data = mainActivity.sendData(301);
        }
    }

    // 수신 메서드
    private void receiveData_local(){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.receiveData();
        }
    }

    // 메뉴를 설정하는 메서드
    private void setMenuNum(int num){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setMenuNum(num);
        }
    }

    // 알람을 띄우는 메서드
    private void createNotif() {
        String id = "알림창입니다";
        NotificationManager manager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel(id);
        if (channel == null) {
            channel = new NotificationChannel(id, "Channel Title", NotificationManager.IMPORTANCE_HIGH);
            // 채널 설정
            channel.setDescription("[Channel description]");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 1000, 200, 340});
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            manager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(requireContext(), NotificationActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(requireContext(), 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), id)
                .setSmallIcon(R.drawable.splash)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.splash))
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .setBigContentTitle("Suitcase Genie")
                        .setSummaryText("Your text description"));
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(requireContext());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            return;
        }

        m.notify(1, builder.build());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Weight Fragment", "Weight Fragment-onDestroyView()");
    }
}
