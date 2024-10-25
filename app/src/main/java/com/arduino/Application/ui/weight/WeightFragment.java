package com.arduino.Application.ui.weight;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.airbnb.lottie.LottieAnimationView;
import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentWeightBinding;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeightFragment extends Fragment {

    // 버튼 요소 및 텍스트 뷰 초기화
    TextView Text_weightNow;
    TextView Text_weightSet;
    TextView Text_weightInfo;

    Button Btn_weight;
    Button Btn_menu;

    Drawable Btn_blue;
    Drawable Btn_red;
    Drawable menu_blue;
    Drawable menu_red;

    // 앱서랍 선언
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    // Weight Fragment의 전역 변수
    private double[] weight = {0, 0};   // weight, set
    private MenuItem lastCheckedBaggage;
    private MenuItem lastCheckedWeight;
    private BluetoothAdapter mBluetoothAdapter;

    View root;
    private FragmentWeightBinding binding;
    private WeightViewModel weightViewModel;
    MainActivity mainActivity;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        weightViewModel = new ViewModelProvider(requireActivity()).get(WeightViewModel.class);
        binding = FragmentWeightBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        mainActivity = (MainActivity) getActivity();

        // 텍스트 뷰, 버튼 및 Drawable 초기화
        Text_weightNow = root.findViewById(R.id.weightNow);         // 현재 무게정보 텍스트뷰
        Text_weightSet = root.findViewById(R.id.weightSet);          // 허용 무게 텍스트 뷰
        Text_weightInfo = root.findViewById(R.id.weightInfo);     // 초과 무게 텍스트 뷰

        Btn_weight = root.findViewById(R.id.weight_btn);            // 무게 측정 시작 버튼
        Btn_menu = root.findViewById(R.id.menu);             // 무게 설정 메뉴 버튼

        Btn_blue = ContextCompat.getDrawable(requireContext(), R.drawable.button_round);
        Btn_red = ContextCompat.getDrawable(requireContext(), R.drawable.button_round_off);
        menu_blue = ContextCompat.getDrawable(requireContext(), R.drawable.weight_menu_on);
        menu_red = ContextCompat.getDrawable(requireContext(), R.drawable.weight_menu_off);

        // DrawerLayout과 NavigationView 설정
        drawerLayout = root.findViewById(R.id.drawer_layout_weight_fragment);
        navigationView = root.findViewById(R.id.nav_view_weight_fragment);

        // ViewModel 선언
        // 측정된 무게 값
        weightViewModel.getWeightNowLiveData().observe(getViewLifecycleOwner(), weight -> {
            if (weight == -1) {
                Text_weightNow.setText("-- Kg");
            } else {
                Text_weightNow.setText(weight + " Kg");
            }
        });

        // 무게 설정 상태
        weightViewModel.getWeightSetLiveData().observe(getViewLifecycleOwner(), target -> {
            weight[1] = target;
            Text_weightSet.setText("허용 무게 : " + target + " Kg");
        });

        // 무게 초과 여부
        weightViewModel.getWeightInfoLiveData().observe(getViewLifecycleOwner(), over -> {
            if (over == -999) {
                Text_weightInfo.setText("32 Kg을 초과하였습니다");
                Text_weightInfo.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_500));
            } else if (over == -1) {
                Text_weightInfo.setText("무게를 측정하지 않았습니다");
                Text_weightInfo.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black));
            } else if (over == 0) {
                Text_weightInfo.setText("무게를 초과하지 않았습니다!");
                Text_weightInfo.setTextColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
            } else if (over == -2) {
                Text_weightInfo.setText("무게 측정에 실패하였습니다");
                Text_weightInfo.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_500));
            } else {
                Text_weightInfo.setText(String.format("%.1f", over) + " Kg 초과하였습니다.");
                Text_weightInfo.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange_500));
            }
        });

        // 측정 버튼 상태
        weightViewModel.getWeightBtnLiveData().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case 1:
                    Btn_weight.setText("무게 측정 시작");
                    Btn_menu.setBackground(menu_blue);
                    Btn_menu.setEnabled(true);
                    Btn_weight.setBackground(Btn_blue);
                    Btn_weight.setEnabled(true);
                    break;
                case 2:
                    Btn_weight.setText("무게 다시 측정");
                    Btn_menu.setBackground(menu_blue);
                    Btn_menu.setEnabled(true);
                    Btn_weight.setBackground(Btn_blue);
                    Btn_weight.setEnabled(true);
                    break;
                case -1:
                    Btn_weight.setText("무게 측정 실패");
                    Btn_menu.setBackground(menu_blue);
                    Btn_menu.setEnabled(true);
                    Btn_weight.setBackground(Btn_red);
                    Btn_weight.setEnabled(true);
                    break;
                case 0:
                    Btn_weight.setText("무게 측정 불가");
                    Btn_menu.setBackground(menu_red);
                    Btn_menu.setEnabled(false);
                    Btn_weight.setBackground(Btn_red);
                    Btn_weight.setEnabled(false);
                    break;
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 설정 리셋 버튼 클릭 이벤트 리스너 설정
        Button Btn_reset = root.findViewById(R.id.reset_button);        // 무게 설정 초기화 버튼
        Btn_reset.setOnClickListener(view -> {
            Toast.makeText(getActivity(), "무게 설정이 초기화 되었습니다!", Toast.LENGTH_SHORT).show();
            resetWeightSetting();
            updateWeight();
        });

        // 메뉴 버튼 클릭 이벤트 리스너 설정
        Btn_menu.setOnClickListener(view -> {
            if (drawerLayout != null) {
                if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                } else {
                    drawerLayout.openDrawer(GravityCompat.END);
                }
            }
        });

        // 무게 측정 버튼 클릭 이벤트 리스너 설정
        Btn_weight.setOnClickListener(view -> {
            Toast.makeText(getActivity(), "무게를 측정합니다!", Toast.LENGTH_SHORT).show();
            measureWeight();
        });

        // 메뉴 항목 클릭 리스너 설정
        setupNavigationViewMenu();

        return root;
    }

    private void measureWeight() {
        // 로딩 애니메이션 (로티 애니메이션) 및 비동기 처리 구문
        LottieAnimationView lottieView = root.findViewById(R.id.lottieView);
        View loadingOverlay = root.findViewById(R.id.loading_overlay);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        double maxSet = weight[1];
        if (mainActivity != null) {
            handler.post(() -> {
                loadingOverlay.setVisibility(View.VISIBLE);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();
                Btn_weight.setEnabled(false);
            });
            executorService.execute(() -> {
                // 백그라운드 작업 처리
                weight[0] = mainActivity.measureWeight(maxSet);
                handler.post(() -> {
                    if (weight[0] == -1) {
                        showCustomDialog(2);
                        weightViewModel.setWeightBtn(-1);
                    } else {
                        if (weight[0] > 32.0) {
                            showCustomDialog(3);
                            Text_weightNow.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_500));
                        } else if (weight[0] > maxSet) {
                            Text_weightNow.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange_500));
                        } else {
                            Text_weightNow.setTextColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    handler.post(() -> Toast.makeText(getActivity(), "로드중 에러 발생", Toast.LENGTH_SHORT).show());
                }

                handler.post(() -> {
                    loadingOverlay.setVisibility(View.GONE);
                    lottieView.cancelAnimation();
                    lottieView.setVisibility(View.GONE);
                    Btn_weight.setEnabled(true);
                });
            });
        }
    }

    // 무게 설정을 확인하는 메서드
    private double[] checkWeightSetting() {
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

        weightViewModel.setWeightNow((double) -1);
        Text_weightNow.setTextColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
        weightViewModel.setWeightSet(15);
        weightViewModel.setWeightInfo(-1);
    }

    // 무게 화면을 업데이트 하는 메서드
    private void updateWeight() {
        if (weight != null && weight[0] != 0 && weight[0] != -1) {
            double maxSet = weight[1];
            if (!mBluetoothAdapter.isEnabled()) {
                weightViewModel.setWeightBtn(0);
            } else if (mBluetoothAdapter.isEnabled() && checkBLE() == 2) {
                weightViewModel.setWeightBtn(2);
            } else {
                weightViewModel.setWeightBtn(0);
            }

            if (weight[0] > 32.0) {             // 32kg을 초과한 경우
                showCustomDialog(3);
                Text_weightNow.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red_500));
            } else if (weight[0] > maxSet) {    // 허용 무게를 초과한 경우
                Text_weightNow.setTextColor(ContextCompat.getColor(requireActivity(), R.color.orange_500));
            } else {                            // 무게를 초과하지 않은 경우
                Text_weightNow.setTextColor(ContextCompat.getColor(requireActivity(), R.color.indigo_500));
            }
        } else if (weight != null && weight[0] == -1) {  // 무게 측정 실패한 경우
            weightViewModel.setWeightBtn(-1);
        }
    }

    // BLE 연결 여부를 체크하는 메서드
    private int checkBLE() {
        if (mainActivity != null) {
            return mainActivity.checkBLE();
        } else {
            return -1;
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
                iconView.setImageResource(R.drawable.dialog_warning);
                titleView.setText("무게 초과!");
                messageTextView.setText("무게가 32Kg을 초과하였습니다.\nIATA 규정으로 인하여 32Kg 이상의 수하물은 항공기에 위탁 수하물로 맡길 수 없습니다.");
                messageImageView.setImageResource(R.drawable.baggage_over32);
                retryBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                break;
            case 2:
                lottieView.setAnimation(R.raw.network_error1);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();

                iconView.setImageResource(R.drawable.dialog_error);
                titleView.setText("무게 측정 실패!");
                messageTextView.setText("무게 측정에 실패했습니다.\n스마트 캐리어와 연결되어 있고 통신 상태가 양호한지 확인 후 다시 시도하세요.");
                messageImageView.setVisibility(View.GONE);
                checkBtn.setVisibility(View.GONE);
                break;
            case 1:
                lottieView.setAnimation(R.raw.bluetooth_on);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();

                iconView.setImageResource(R.drawable.info_bt_off);
                titleView.setText("무게 측정 비활성화");
                messageTextView.setText("블루투스가 꺼져 있어 무게 측정을 할 수 없습니다.\n블루투스를 켠 후에 다시 시도하세요.");
                messageImageView.setVisibility(View.GONE);
                retryBtn.setVisibility(View.GONE);
                cancelBtn.setVisibility(View.GONE);
                break;
            case 0:
                lottieView.setAnimation(R.raw.bluetooth_on);
                lottieView.setVisibility(View.VISIBLE);
                lottieView.playAnimation();

                iconView.setImageResource(R.drawable.info_bt_on);
                titleView.setText("무게 측정 비활성화");
                messageTextView.setText("스마트 캐리어에 연결되지 않아 무게 측정 기능을 사용할 수 없습니다.\n연결 후에 다시 시도하세요.");
                messageImageView.setVisibility(View.GONE);
                retryBtn.setVisibility(View.GONE);
                checkBtn.setVisibility(View.GONE);
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
                } else if (itemId == R.id.weight_5 || itemId == R.id.weight_7 || itemId == R.id.weight_10 ||
                        itemId == R.id.weight_15 || itemId == R.id.weight_20 || itemId == R.id.weight_23 ||
                        itemId == R.id.weight_32) {
                    String title = (String) item.getTitle();
                    weightViewModel.setWeightSet(Integer.parseInt(title != null ? title.replace("kg", "") : null));
                    Btn_weight.setEnabled(true);

                    if (lastCheckedWeight != null) {
                        lastCheckedWeight.setChecked(false);
                    }
                    item.setChecked(true);
                    lastCheckedWeight = item;
                    drawerLayout.closeDrawers();
                }
                return true;
            });
        }
    }

    public void onResume() {
        super.onResume();

        weight = checkWeightSetting();

        if (!mBluetoothAdapter.isEnabled()) {
            showCustomDialog(1);
            Btn_menu.setEnabled(false);
            Btn_menu.setBackground(menu_red);
            weightViewModel.setWeightBtn(0);
        } else if (checkBLE() == 2) {
            Btn_menu.setEnabled(true);
            weightViewModel.setWeightBtn(1);
        } else {
            showCustomDialog(0);
            Btn_menu.setEnabled(false);
            Btn_menu.setBackground(menu_red);
            weightViewModel.setWeightBtn(0);
        }
        if (weight[1] == 0) {
            Btn_weight.setEnabled(false);
        }
        updateWeight();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}