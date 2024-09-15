package com.arduino.Application.ui.weight;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentWeightBinding;
import com.google.android.material.navigation.NavigationView;

public class WeightFragment extends Fragment {

    // 버튼 요소 및 텍스트 뷰 초기화
    private TextView weightNow;
    private TextView weightSet;
    private TextView weightInfo;
    private Button mBtnWeight;

    Drawable Btn_blue;
    Drawable Btn_red;

    // 앱서랍 선언
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // Weight Fragment의 전역 변수
    private double[] weight = {0, 0};   // weight, set
    private MenuItem lastCheckedBaggage;
    private MenuItem lastCheckedWeight;

    private BluetoothAdapter mBluetoothAdapter;

    private FragmentWeightBinding binding;

    WeightViewModel weightViewModel;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // ViewModel 설정
        weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);

        binding = FragmentWeightBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Log.d("Weight Fragment", "Weight Fragment-onCreateView()");

        // 버튼 요소 및 텍스트 뷰 초기화
        weightNow = root.findViewById(R.id.weightNow);         // 현재 무게정보 텍스트뷰
        weightSet = root.findViewById(R.id.weightSet);          // 허용 무게 텍스트 뷰
        weightInfo = root.findViewById(R.id.weightInfo);     // 초과 무게 텍스트 뷰
        mBtnWeight = root.findViewById(R.id.weight_btn);            // 무게 측정 시작 버튼
        Button resetBtn = root.findViewById(R.id.reset_button);        // 무게 설정 초기화 버튼

        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);

        // DrawerLayout과 NavigationView 설정
        drawerLayout = root.findViewById(R.id.drawer_layout_weight_fragment);
        navigationView = root.findViewById(R.id.nav_view_weight_fragment);

        // ViewModel과 UI 요소 바인딩
        weightViewModel.getWeightNowLiveData().observe(getViewLifecycleOwner(), weight -> weightNow.setText(weight));
        weightViewModel.getWeightSetLiveData().observe(getViewLifecycleOwner(), set -> {
            int selected_weight = Integer.parseInt(set.replace("kg", ""));
            weight[1] = selected_weight;
            weightSet.setText("허용 무게 : " + selected_weight + " Kg");
        });
        weightViewModel.getWeightInfoLiveData().observe(getViewLifecycleOwner(), info -> weightInfo.setText(info));
        weightViewModel.getWeightBtnLiveData().observe(getViewLifecycleOwner(), btn -> mBtnWeight.setText(btn));

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 설정 리셋 버튼 클릭 이벤트 리스너 설정
        resetBtn.setOnClickListener(view -> {
            Toast.makeText(getActivity(), "무게 설정이 초기화 되었습니다!", Toast.LENGTH_SHORT).show();
            resetWeightSetting();
            updateWeight();
        });

        // 메뉴 버튼 클릭 이벤트 리스너 설정
        Button menuButton = root.findViewById(R.id.menu);
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
            Toast.makeText(getActivity(), "무게를 측정합니다", Toast.LENGTH_SHORT).show();
            measureWeight();
        });

        // 메뉴 항목 클릭 리스너 설정
        setupNavigationViewMenu();

        return root;
    }

    @SuppressLint("SetTextI18n")
    private void measureWeight() {
        MainActivity mainActivity = (MainActivity) getActivity();
        double maxSet = weight[1];

        if (mainActivity != null) {
            weight[0] = mainActivity.measureWeight(maxSet);
            mBtnWeight.setBackground(Btn_blue);
            if (weight[0] == -1) {
                showCustomDialog(2);
                mBtnWeight.setBackground(Btn_red);
            } else {
                if (weight[0] > 32.0) {
                    showCustomDialog(3);
                    weightNow.setTextColor(Color.parseColor("#F44336"));
                } else if (weight[0] > maxSet) {
                    weightNow.setTextColor(Color.parseColor("#F57C00"));
                } else {
                    weightNow.setTextColor(Color.parseColor("#3F51B5"));
                }
            }
        }
    }

    // 무게 설정을 확인하는 메서드
    private double[] checkWeightSetting() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            return mainActivity.checkWeightSetting();
        }
        return null;
    }

    // 무게 설정을 초기화 하는 메서드
    private void resetWeightSetting() {
        weight[0] = 0;
        weight[1] = 0;
        lastCheckedWeight = null;
        lastCheckedBaggage = null;

        weightViewModel.setWeightNow("-- Kg");
        weightNow.setTextColor(Color.parseColor("#3F51B5"));
        weightViewModel.setWeightSet("15kg");
        weightViewModel.setWeightInfo("무게 초과 여부 표시");
    }

    // 무게 화면을 업데이트 하는 메서드
    private void updateWeight() {
        if (weight != null && weight[0] != 0 && weight[0] != -1) {
            double maxSet = weight[1];
            mBtnWeight.setBackground(Btn_blue);
            if (weight[0] > 32.0) {             // 32kg을 초과한 경우
                showCustomDialog(3);
                weightNow.setTextColor(Color.parseColor("#F44336"));
            } else if (weight[0] > maxSet) {    // 허용 무게를 초과한 경우
                weightNow.setTextColor(Color.parseColor("#F57C00"));
            } else {                            // 무게를 초과하지 않은 경우
                weightNow.setTextColor(Color.parseColor("#3F51B5"));
            }
        } else if (weight != null && weight[0] == -1){  // 무게 측정 실패한 경우
            mBtnWeight.setBackground(Btn_red);
        }
    }

    @SuppressLint("SetTextI18n")
    public void onResume() {
        super.onResume();
        Log.d("Weight Fragment", "Weight Fragment-onResume()");

        weight = checkWeightSetting();

        if (!mBluetoothAdapter.isEnabled()) {
            showCustomDialog(1);
            mBtnWeight.setEnabled(false);
            mBtnWeight.setBackground(Btn_red);
        } else {
            mBtnWeight.setEnabled(true);
        }
        if (weight[1] == 0) {
            mBtnWeight.setEnabled(false);
        }

        updateWeight();
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
                messageTextView.setText("무게 측정에 실패했습니다.\n스마트 캐리어와 연결되어 있고 통신 상태가 양호한지 확인 후\n다시 시도하세요.");
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

    // 앱서랍 클릭 리스너
    private void setupNavigationViewMenu() {
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                Menu menu = navigationView.getMenu();

                 if (itemId == R.id.carryon_baggage || itemId == R.id.checked_baggage) {
                     if (itemId == R.id.carryon_baggage) {      // 기내 수하물을 선택한 경우
                         menu.setGroupVisible(R.id.weight_group_carryon, true);
                         menu.setGroupVisible(R.id.weight_group_checked, false);
                     } else {       // 위탁 수하물을 선택한 경우
                         menu.setGroupVisible(R.id.weight_group_carryon, false);
                         menu.setGroupVisible(R.id.weight_group_checked, true);
                     }

                    if (lastCheckedBaggage != null) {
                        lastCheckedBaggage.setChecked(false);
                    }
                    item.setChecked(true);
                    lastCheckedBaggage = item;
                } else if (itemId == R.id.weight_5||itemId == R.id.weight_7 || itemId == R.id.weight_10 ||
                        itemId == R.id.weight_15 ||itemId == R.id.weight_20 || itemId == R.id.weight_23 ||
                        itemId == R.id.weight_32) {
                     weightViewModel.setWeightSet((String) item.getTitle());
                     mBtnWeight.setEnabled(true);

                    if (lastCheckedWeight != null) {
                        lastCheckedWeight.setChecked(false);
                    }
                    item.setChecked(true);
                    lastCheckedWeight = item;
                }
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