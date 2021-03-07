package com.example.adictic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.adictic.R;
import com.example.adictic.activity.OficinesActivity;
import com.example.adictic.activity.PreguntesFrequents;

public class AdviceFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_advice, container, false);
        ConstraintLayout cl1 = (ConstraintLayout) root.findViewById(R.id.constraint_advice);
        ConstraintLayout cl2 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_2);
        ConstraintLayout cl5 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_5);
        ConstraintLayout cl6 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_6);



        cl2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PreguntesFrequents.class));
            }
        });

        cl6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), OficinesActivity.class));
            }
        });
        return root;
    }
}
