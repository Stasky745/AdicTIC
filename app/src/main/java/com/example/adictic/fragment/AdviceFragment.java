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
import com.example.adictic.TodoApp;
import com.example.adictic.activity.ChatActivity;
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
        ConstraintLayout cl1 = (ConstraintLayout) root.findViewById(R.id.constraint_advice);
        ConstraintLayout cl2 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_2);
        ConstraintLayout consulta_privada = (ConstraintLayout) root.findViewById(R.id.constraint_advice_5);
        ConstraintLayout cl6 = (ConstraintLayout) root.findViewById(R.id.constraint_advice_6);

        final TodoApi mTodoService = ((TodoApp)  getActivity().getApplication()).getAPI();
        long[] hasAnOpenChat = {-1}; // -1 = no connection, 0 = false, 1 = true
        Call<Long> call = mTodoService.hasAnOpenChat();
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) {
                    hasAnOpenChat[0] = response.body();
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
            }
        });

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

        consulta_privada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Comprovar si té algun chat obert
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("userId", hasAnOpenChat[0]);
                startActivity(intent);
            }
        });
        return root;
    }
}
