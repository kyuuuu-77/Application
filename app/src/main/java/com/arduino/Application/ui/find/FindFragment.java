package com.arduino.Application.ui.find;

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
import com.arduino.Application.databinding.FragmentFindBinding;

public class FindFragment extends Fragment {

    private FragmentFindBinding binding;

    private int menuNum;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FindViewModel findViewModel =
                new ViewModelProvider(this).get(FindViewModel.class);

        binding = FragmentFindBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Log.d("Find Fragment", "Find Fragment-onCreatedView()");

        final TextView textView = binding.textFind;
        findViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    public void onResume(){
        super.onResume();
        Log.d("Find Fragment", "Find Fragment-onResume()");

        menuNum = 2;
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

        Log.d("Find Fragment", "Find Fragment-onDestroyView()");
    }
}