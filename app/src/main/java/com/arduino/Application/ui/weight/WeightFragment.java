package com.arduino.Application.ui.weight;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentWeightBinding;
import com.google.android.material.navigation.NavigationView;

public class WeightFragment extends Fragment {

    // UI 요소 선언
    private TextView weightNow;
    private TextView weightTps;
    private TextView looseWeight;
    private Button mBtnWeight;
    private BluetoothAdapter mBluetoothAdapter;
    private FragmentWeightBinding binding;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private double[] weight = {0, 0};   // weight, tps

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // ViewModel 설정
        WeightViewModel weightViewModel =
                new ViewModelProvider(requireActivity()).get(WeightViewModel.class);

        // 바인딩을 통해 레이아웃을 인플레이트합니다.
        binding = FragmentWeightBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Log.d("Weight Fragment", "Weight Fragment-onCreateView()");

        // UI 요소 초기화
        weightNow = root.findViewById(R.id.text_weightnow);         // 현재 무게정보 텍스트뷰
        weightTps = root.findViewById(R.id.text_weightps);          // 허용 무게 텍스트 뷰
        looseWeight = root.findViewById(R.id.text_looseweight);     // 초과 무게 텍스트 뷰
        mBtnWeight = root.findViewById(R.id.weight_btn);            // 무게 측정 시작 버튼

        // DrawerLayout과 NavigationView 설정
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout_weight);
        navigationView = requireActivity().findViewById(R.id.nav_view_weight);

        // ViewModel과 UI 요소 바인딩
        weightViewModel.getWeightNowLiveData().observe(getViewLifecycleOwner(), weight -> weightNow.setText(weight));
        weightViewModel.getWeightTpsLiveData().observe(getViewLifecycleOwner(), tps -> weightTps.setText(tps));
        weightViewModel.getLooseWeightLiveData().observe(getViewLifecycleOwner(), loose -> looseWeight.setText(loose));
        weightViewModel.getWeightBtnLiveData().observe(getViewLifecycleOwner(), btn -> mBtnWeight.setText(btn));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 메뉴 버튼 클릭 이벤트 리스너 설정
        ImageView menuButton = root.findViewById(R.id.menu);
        menuButton.setOnClickListener(view -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });

        // 무게 측정 버튼 클릭 이벤트 리스너 설정
        mBtnWeight.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");
            measureWeight();
        });

        // 메뉴 항목 클릭 리스너 설정
        setupNavigationViewMenu();

        return root;
    }

    @SuppressLint("SetTextI18n")
    private void measureWeight() {
        MainActivity mainActivity = (MainActivity) getActivity();
        double maxTps = 20.0;
        weight[1] = maxTps;
        if (mainActivity != null) {
            weight[0] = mainActivity.measureWeight(maxTps);
            mBtnWeight.setBackgroundColor(Color.parseColor("#2196F3"));
            if (weight[0] == -1) {
                showCustomDialog(2);
                mBtnWeight.setBackgroundColor(Color.parseColor("#D32F2F"));
            } else {
                if (weight[0] > 32.0) {
                    showCustomDialog(3);
                    weightNow.setTextColor(Color.parseColor("#D32F2F"));
                } else if (weight[0] > maxTps) {
                    weightNow.setTextColor(Color.parseColor("#F57C00"));
                } else {
                    weightNow.setTextColor(Color.parseColor("#319EF2"));
                }
            }
        }
    }

    private double[] checkWeightSetting() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkWeightSetting();
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    public void onResume() {
        super.onResume();
        Log.d("Weight Fragment", "Weight Fragment-onResume()");

        int menuNum = 3;
        setMenuNum(menuNum);

        weight = checkWeightSetting();

        if (!mBluetoothAdapter.isEnabled()) {
            showCustomDialog(1);
            mBtnWeight.setEnabled(false);
        } else {
            mBtnWeight.setEnabled(true);
        }

        if (weight != null && weight[0] != 0 && weight[0] != -1) {
            double maxTps = weight[1];
            mBtnWeight.setBackgroundColor(Color.parseColor("#2196F3"));
            if (weight[0] > 32.0) {             // 32kg을 초과한 경우
                showCustomDialog(3);
                weightNow.setTextColor(Color.parseColor("#D32F2F"));
            } else if (weight[0] > maxTps) {    // 허용 무게를 초과한 경우
                weightNow.setTextColor(Color.parseColor("#F57C00"));
            } else {                            // 무게를 초과하지 않은 경우
                weightNow.setTextColor(Color.parseColor("#319EF2"));
            }
        } else if (weight != null && weight[0] == -1){  // 무게 측정 실패한 경우
            mBtnWeight.setBackgroundColor(Color.parseColor("#D32F2F"));
        }
    }

    private void setMenuNum(int num) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setMenuNum(num);
        }
    }

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

        // 텍스트, 이미지, 버튼 설정
        switch (status) {
            case 3:
                iconView.setImageResource(R.drawable.dialog_warning);
                titleView.setText("무게 초과!");
                messageTextView.setText("무게가 32Kg을 초과하였습니다.\nIATA 규정으로 인하여 32Kg 이상의 수하물은 항공기에 위탁 수하물로 맡길 수 없습니다.");
                messageImageView.setImageResource(R.drawable.baggage_over32);
                retryBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                break;
            case 2:
                iconView.setImageResource(R.drawable.dialog_error);
                titleView.setText("무게 측정 실패!");
                messageTextView.setText("무게 측정에 실패했습니다.\n스마트 캐리어와 연결되어 있고 통신 상태가 양호한지 확인 후 다시 시도하세요.");
                messageImageView.setImageResource(R.drawable.connection_error);
                checkBtn.setVisibility(View.GONE);
                break;
            case 1:
                iconView.setImageResource(R.drawable.info_bt_off);
                titleView.setText("무게 측정 비활성화");
                messageTextView.setText("블루투스가 꺼져 있어 무게 측정을 할 수 없습니다.\n블루투스를 켠 후에 다시 시도하세요.");
                messageImageView.setImageResource(R.drawable.connection);
                retryBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                break;
        }

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        retryBtn.setOnClickListener(v -> {
            measureWeight();
            dialog.dismiss();
        });
        checkBtn.setOnClickListener(v -> dialog.dismiss());
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
    }

    private void setupNavigationViewMenu() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                // 메뉴 항목 클릭 시, 체크 상태를 변경합니다.
                item.setChecked(!item.isChecked());
                return true;
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d("Weight Fragment", "Weight Fragment-onDestroyView()");
    }
}
