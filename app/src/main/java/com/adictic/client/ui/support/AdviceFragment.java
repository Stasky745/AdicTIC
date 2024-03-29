package com.adictic.client.ui.support;

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

import com.adictic.client.R;
import com.adictic.client.ui.chat.ChatActivity;
import com.adictic.common.ui.OficinesActivity;

public class AdviceFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_advice, container, false);

        ConstraintLayout CL_info = root.findViewById(R.id.CL_info);
        ConstraintLayout CL_infoButtons = root.findViewById(R.id.CL_infoButtons);
        ConstraintLayout CL_suport = root.findViewById(R.id.CL_suport);
        ConstraintLayout CL_suportButtons = root.findViewById(R.id.CL_suportButtons);

        Button BT_ContingutInformatiu = root.findViewById(R.id.BT_ContingutInformatiu);
        Button BT_faqs = root.findViewById(R.id.BT_faqs);
        //Per prova pilot
        BT_faqs.setVisibility(View.GONE);

        Button BT_ConsultaPrivada = root.findViewById(R.id.BT_ConsultaPrivada);
        Button BT_oficines = root.findViewById(R.id.BT_oficines);

        BT_ConsultaPrivada.setOnClickListener(v -> startActivity(new Intent(getActivity(), ChatActivity.class)));

        CL_info.setOnClickListener(v -> {
            if (CL_infoButtons.getVisibility() == View.GONE) {
                CL_infoButtons.setVisibility(View.VISIBLE);

                ImageView IV_openInfo = root.findViewById(R.id.IV_openInfo);
                IV_openInfo.setImageResource(R.drawable.ic_arrow_close);
            } else {
                CL_infoButtons.setVisibility(View.GONE);

                ImageView IV_openInfo = root.findViewById(R.id.IV_openInfo);
                IV_openInfo.setImageResource(R.drawable.ic_arrow_open);
            }
        });

        CL_suport.setOnClickListener(v -> {
            if (CL_suportButtons.getVisibility() == View.GONE) {
                CL_suportButtons.setVisibility(View.VISIBLE);

                ImageView IV_openSuport = root.findViewById(R.id.IV_openSuport);
                IV_openSuport.setImageResource(R.drawable.ic_arrow_close);
            } else {
                CL_suportButtons.setVisibility(View.GONE);

                ImageView IV_openSuport = root.findViewById(R.id.IV_openSuport);
                IV_openSuport.setImageResource(R.drawable.ic_arrow_open);
            }
        });

        BT_ContingutInformatiu.setOnClickListener(v -> startActivity(new Intent(getActivity(), PreguntesFrequents.class)));
        BT_faqs.setOnClickListener(v -> startActivity(new Intent(getActivity(), PreguntesFrequents.class)));
        BT_oficines.setOnClickListener(v -> startActivity(new Intent(getActivity(), OficinesActivity.class)));

        /*Button BT_debug = root.findViewById(R.id.BT_advice_debug);
        if(BuildConfig.DEBUG) {
            BT_debug.setVisibility(View.VISIBLE);
            BT_debug.setOnClickListener(v -> startActivity(new Intent(getActivity(), JitsiMainActivity.class)));
        }*/

        return root;
    }
}
