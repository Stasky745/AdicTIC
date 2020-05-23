package com.example.adictic.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.adictic.R;

public class AdviceFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_advice, container, false);
        ConstraintLayout cl1 = (ConstraintLayout) root.findViewById(R.id.constraint_advice);
        ConstraintLayout cl2 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_2);
        ConstraintLayout cl3 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_3);
        ConstraintLayout cl4 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_4);
        ConstraintLayout cl5 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_5);
        ConstraintLayout cl6 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_6);

        cl1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return root;
    }
}