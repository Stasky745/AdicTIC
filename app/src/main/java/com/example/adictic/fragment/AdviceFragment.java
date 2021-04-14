package com.example.adictic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.activity.chat.ChatActivity;
import com.example.adictic.activity.OficinesActivity;
import com.example.adictic.activity.PreguntesFrequents;
import com.example.adictic.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdviceFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_advice, container, false);

        ConstraintLayout CL_info = (ConstraintLayout) root.findViewById(R.id.CL_info);
        ConstraintLayout CL_infoButtons = (ConstraintLayout) root.findViewById(R.id.CL_infoButtons);
        ConstraintLayout CL_suport = (ConstraintLayout) root.findViewById(R.id.CL_suport);
        ConstraintLayout CL_suportButtons = (ConstraintLayout) root.findViewById(R.id.CL_suportButtons);

        Button BT_ContingutInformatiu = (Button) root.findViewById(R.id.BT_ContingutInformatiu);
        Button BT_faqs = (Button) root.findViewById(R.id.BT_faqs);
        Button BT_ConsultaPrivada = (Button) root.findViewById(R.id.BT_ConsultaPrivada);
        Button BT_oficines = (Button) root.findViewById(R.id.BT_oficines);

        BT_ConsultaPrivada.setOnClickListener(v -> startActivity(new Intent(getActivity(),ChatActivity.class)));

        CL_info.setOnClickListener(v -> {
            if(CL_infoButtons.getVisibility()==View.GONE){
                CL_infoButtons.setVisibility(View.VISIBLE);

                ImageView IV_openInfo = (ImageView) root.findViewById(R.id.IV_openInfo);
                IV_openInfo.setImageResource(R.drawable.ic_arrow_close);
            }
            else{
                CL_infoButtons.setVisibility(View.GONE);

                ImageView IV_openInfo = (ImageView) root.findViewById(R.id.IV_openInfo);
                IV_openInfo.setImageResource(R.drawable.ic_arrow_open);
            }
        });

        CL_suport.setOnClickListener(v -> {
            if(CL_suportButtons.getVisibility()==View.GONE){
                CL_suportButtons.setVisibility(View.VISIBLE);

                ImageView IV_openSuport = (ImageView) root.findViewById(R.id.IV_openSuport);
                IV_openSuport.setImageResource(R.drawable.ic_arrow_close);
            }
            else{
                CL_suportButtons.setVisibility(View.GONE);

                ImageView IV_openSuport = (ImageView) root.findViewById(R.id.IV_openSuport);
                IV_openSuport.setImageResource(R.drawable.ic_arrow_open);
            }
        });

        BT_ContingutInformatiu.setOnClickListener(v -> startActivity(new Intent(getActivity(),PreguntesFrequents.class)));
        BT_faqs.setOnClickListener(v -> startActivity(new Intent(getActivity(),PreguntesFrequents.class)));
        BT_oficines.setOnClickListener(v -> startActivity(new Intent(getActivity(),OficinesActivity.class)));

        return root;
    }
}
