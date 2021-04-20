package com.example.adictic.ui.chat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.adictic.R;
import com.example.adictic.entity.Dubte;
import com.example.adictic.entity.Localitzacio;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.TodoApp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoChatFragment extends Fragment {

    TodoApi mTodoService;
    TextInputEditText TIET_dubteTitol, TIET_dubteDesc;
    ChipGroup CG_localitats;
    Boolean hasDubte;

    public NoChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_chat_no, container, false);
        mTodoService = ((TodoApp) requireActivity().getApplication()).getAPI();

        assert getArguments() != null;
        hasDubte = getArguments().getBoolean("dubte");

        getLocalitzacions();

        setViews(root);
        setButton(root);

        return root;
    }

    private void setViews(View root) {
        TIET_dubteTitol = root.findViewById(R.id.TIET_dubteTitol);
        TIET_dubteDesc = root.findViewById(R.id.TIET_dubteDesc);
        CG_localitats = root.findViewById(R.id.CG_localitats);
        CG_localitats.setSelectionRequired(true);
        CG_localitats.setSingleSelection(false);
    }

    private void setButton(View root) {
        Button enviar_dubte = root.findViewById(R.id.BT_enviar_dubte);
        enviar_dubte.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.send_dubte);

            if (hasDubte) builder.setMessage(R.string.dubte_overwrite);
            else builder.setMessage(R.string.dubte_send);

            builder.setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                Dubte newDubte = new Dubte();
                newDubte.titol = Objects.requireNonNull(TIET_dubteTitol.getText()).toString();
                newDubte.descripcio = Objects.requireNonNull(TIET_dubteDesc.getText()).toString();
                newDubte.localitzacio = new ArrayList<>();

                for (Integer idInt : CG_localitats.getCheckedChipIds())
                    newDubte.localitzacio.add(Long.valueOf(idInt));

                Call<String> call = mTodoService.postDubte(newDubte);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.dubte_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast toast = Toast.makeText(getContext(), R.string.error_local, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast toast = Toast.makeText(getContext(), R.string.error_server_read, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                    .show();
        });
    }

    private void getLocalitzacions() {
        Call<Collection<Localitzacio>> call = mTodoService.getLocalitzacions();
        call.enqueue(new Callback<Collection<Localitzacio>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<Localitzacio>> call, @NonNull Response<Collection<Localitzacio>> response) {
                if (response.isSuccessful() && response.body() != null) setLocalitzacions(response.body());
                else {
                    Toast toast = Toast.makeText(getContext(), R.string.error_local, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<Localitzacio>> call, @NonNull Throwable t) {
                Toast toast = Toast.makeText(getContext(), R.string.error_server_read, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void setLocalitzacions(Collection<Localitzacio> localitzacions) {
        ((List<Localitzacio>) localitzacions).add(0, new Localitzacio((long) 0, "Online"));
        // Create an ArrayAdapter using the string array and a default spinner layout
        for (Localitzacio loc : localitzacions) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.single_chip, CG_localitats, false);
            chip.setText(loc.poblacio);
            chip.setId(loc.id.intValue());

            if (chip.getId() == 0) chip.setSelected(true);

            CG_localitats.addView(chip);
        }
    }
}
