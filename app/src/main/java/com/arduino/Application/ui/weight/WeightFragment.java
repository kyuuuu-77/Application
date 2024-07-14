package com.arduino.Application.ui.weight;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.databinding.FragmentWeightBinding;

public class WeightFragment extends Fragment {

    private FragmentWeightBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WeightViewModel weightViewModel =
                new ViewModelProvider(this).get(WeightViewModel.class);

        binding = FragmentWeightBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Weight Fragment", "Weight Fragment-onCreatedView()");

        final TextView textView = binding.textWeight;
        weightViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}