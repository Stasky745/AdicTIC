package com.example.adictic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.adictic.R;
import com.example.adictic.activity.EnviarDubte;

public class NoChatFragment extends Fragment {


    public NoChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_chat_no, container, false);

        Button enviar_dubte = root.findViewById(R.id.BT_enviar_dubte);

        enviar_dubte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EnviarDubte.class));
            }
        });

        return root;
    }
}
