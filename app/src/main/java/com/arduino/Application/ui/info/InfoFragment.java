package com.arduino.Application.ui.info;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.MainActivity;
import com.arduino.Application.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {

    private FragmentInfoBinding binding;

    private int menuNum;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel infoViewModel =
                new ViewModelProvider(this).get(InfoViewModel.class);

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Info Fragment", "Info Fragment-onCreatedView()");

        final TextView textView = binding.textInfo;
        infoViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Info Fragment", "Info Fragment-onResume()");

        menuNum = 4;
        setMenuNum(menuNum);
    }

    private void setMenuNum(int num){
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setMenuNum(num);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

        Log.d("Info Fragment", "Info Fragment-onDestroyView()");
    }
}