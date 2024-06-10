package com.arduino.Application.ui.info;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.arduino.Application.R;
import com.arduino.Application.databinding.FragmentInfoBinding;

public class InfoFragment extends Fragment {
    private FragmentInfoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        InfoViewModel infoViewModel =
                new ViewModelProvider(this).get(InfoViewModel.class);

        binding = FragmentInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textInfo;
        infoViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //Edited
        /*
        TextView view_signal = root.findViewById(R.id.rssi_signal);
        String signal = this.getArguments().getString("message");
        view_signal.setText(signal);
         */

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}