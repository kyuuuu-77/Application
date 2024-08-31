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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentWeightBinding;

public class WeightFragment extends Fragment {

    // 버튼 및 텍스트뷰 초기화
    TextView weightNow;
    TextView weightTps;
    TextView looseWeight;
    Button mBtnWeight;

    private FragmentWeightBinding binding;
    private BluetoothAdapter mBluetoothAdapter;

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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 버튼 이벤트 리스너
        // 무게 측정 버튼
        mBtnWeight.setOnClickListener(view -> {
            Log.d("Button Click", "Button clicked!");
            measureWeight();
        });

        return root;
    }
    
    // 무게 측정 메서드
    @SuppressLint("SetTextI18n")
    private void measureWeight(){
        MainActivity mainActivity = (MainActivity) getActivity();
        double maxTps = 20.0;
        weight[1] = maxTps;
        if (mainActivity != null) {
            weight[0] = mainActivity.measureWeight(maxTps);
            mBtnWeight.setBackgroundColor(Color.parseColor("#2196F3"));
            if (weight[0] == -1){               // 측정에 실패한 경우
                showCustomDialog(2);
                mBtnWeight.setBackgroundColor(Color.parseColor("#D32F2F"));
            }
            else{
                if (weight[0] > 32.0) {             // 32kg을 초과한 경우
                    showCustomDialog(3);
                    weightNow.setTextColor(Color.parseColor("#D32F2F"));
                } else if (weight[0] > maxTps) {    // 허용 무게를 초과한 경우
                    weightNow.setTextColor(Color.parseColor("#F57C00"));
                } else {                            // 무게를 초과하지 않은 경우
                    weightNow.setTextColor(Color.parseColor("#319EF2"));
                }
            }
        }
    }
    
    // 측정한 무게와 허용 무게 세팅값을 불러오는 메서드
    private double[] checkWeightSetting(){
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

        if (!mBluetoothAdapter.isEnabled()){
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

    // 메뉴를 설정하는 메서드
    private void setMenuNum(int num){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setMenuNum(num);
        }
    }

    // 커스텀 다이얼로그 설정 메서드
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
        switch (status) {       // 1 -> 블루투스 꺼져 있을시 2 -> 무게 측정 실패시 3 -> 32kg 초과시
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

        retryBtn.setOnClickListener(v -> {      // 다시시도 버튼
            measureWeight();
            dialog.dismiss();
        });
        checkBtn.setOnClickListener(v -> {      // 확인 버튼
            dialog.dismiss();
        });
        cancelBtn.setOnClickListener(v -> {     // 취소 버튼
            dialog.dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Weight Fragment", "Weight Fragment-onDestroyView()");
    }
}
