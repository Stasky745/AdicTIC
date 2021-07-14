package com.example.adictic.ui.chat;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.adictic.common.entity.Dubte;
import com.adictic.common.entity.DubteLocalitzacions;
import com.adictic.common.entity.Localitzacio;
import com.adictic.common.util.Constants;
import com.example.adictic.R;
import com.example.adictic.rest.AdicticApi;
import com.example.adictic.util.AdicticApp;
import com.example.adictic.util.Funcions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoChatFragment extends Fragment {
    private HashMap<Long,Localitzacio> localitzacioMap;
    private AdicticApi mTodoService;
    private TextInputEditText TIET_dubteTitol, TIET_dubteDesc;
    private ChipGroup CG_localitats;
    private Boolean hasDubte;

    public NoChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_chat_no, container, false);
        mTodoService = ((AdicticApp) requireActivity().getApplication()).getAPI();

        Funcions.closeKeyboard(root.findViewById(R.id.mainLayout),getActivity());

        assert getArguments() != null;
        hasDubte = getArguments().getBoolean("dubte");

        setViews(root);
        getInfo();
        setButton(root);

        return root;
    }

    private void setViews(View root) {
        TIET_dubteTitol = root.findViewById(R.id.TIET_dubteTitol);
        TIET_dubteDesc = root.findViewById(R.id.TIET_dubteDesc);
        CG_localitats = root.findViewById(R.id.CG_localitats);
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
                    newDubte.localitzacio.add(localitzacioMap.get(idInt));

                SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(requireActivity());
                assert sharedPreferences != null;
                long idChild;
                if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false))
                    idChild = -1;
                else
                    idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -2);

                Call<String> call = mTodoService.postDubte(idChild, newDubte);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getActivity(), R.string.dubte_success, Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        } else {
                            Toast toast = Toast.makeText(getContext(), R.string.error_sending_data, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast toast = Toast.makeText(getContext(), R.string.error_sending_data, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            })
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                    .show();
        });
    }

    private void getInfo() {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(requireActivity());
        assert sharedPreferences != null;
        long idChild = -1L;
        if(!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false))
            idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);

        Call<DubteLocalitzacions> call = mTodoService.getLocalitzacionsAndOpenDubte(idChild);
        call.enqueue(new Callback<DubteLocalitzacions>() {
            @Override
            public void onResponse(@NonNull Call<DubteLocalitzacions> call, @NonNull Response<DubteLocalitzacions> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if(response.body().dubte!=null) {
                        TIET_dubteTitol.setText(response.body().dubte.titol);
                        TIET_dubteDesc.setText(response.body().dubte.descripcio);
                        setLocalitzacions(response.body().localitzacions, response.body().dubte.localitzacio);
                    }
                    else setLocalitzacions(response.body().localitzacions, Collections.emptyList());
                }
                else {
                    Toast toast = Toast.makeText(getContext(), R.string.error_local, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DubteLocalitzacions> call, @NonNull Throwable t) {
                Toast toast = Toast.makeText(getContext(), R.string.error_server_read, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void setLocalitzacions(Collection<Localitzacio> localitzacions, List<Localitzacio> enabledLocations) {
        localitzacioMap = new HashMap<>();
        for(Localitzacio loc : localitzacions){
            localitzacioMap.put(loc.id,loc);
        }

        // Create an ArrayAdapter using the string array and a default spinner layout
        for (Localitzacio loc : localitzacioMap.values()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.single_chip, CG_localitats, false);
            chip.setText(loc.poblacio);
            chip.setId(loc.id.intValue());

            CG_localitats.addView(chip);
        }
        if(enabledLocations.isEmpty())
            CG_localitats.getChildAt(0).performClick();
        else{
            try {
                for (Localitzacio loc : enabledLocations) {
                    Long locIds = loc.id;
                    CG_localitats.getChildAt(((Long)(locIds-1)).intValue()).performClick();
                }
            } catch(IndexOutOfBoundsException ex){ ex.printStackTrace(); }
        }
    }
}
