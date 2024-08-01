package com.arduino.Application.ui.weight;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.NotificationActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentWeightBinding;

import java.util.Objects;

public class WeightFragment extends Fragment {

    private FragmentWeightBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WeightViewModel weightViewModel =
                new ViewModelProvider(this).get(WeightViewModel.class);

        binding = FragmentWeightBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Weight Fragment", "Weight Fragment-onCreatedView()");

        final TextView textView = binding.textWeight;
        weightViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // 알림 버튼 설정
        root.findViewById(R.id.weight_btn).setOnClickListener(v -> createNotif());

        return root;
    }

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
