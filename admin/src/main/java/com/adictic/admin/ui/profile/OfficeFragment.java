package com.adictic.admin.ui.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.adictic.admin.R;
import com.adictic.admin.rest.AdminApi;
import com.adictic.admin.util.AdminApp;
import com.adictic.admin.util.Funcions;
import com.adictic.admin.util.hilt.AdminRepository;
import com.adictic.common.entity.Oficina;
import com.adictic.common.ui.OficinesActivity;
import com.adictic.common.util.Callback;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Response;

@AndroidEntryPoint
public class OfficeFragment extends Fragment {
    private static final String OFFICE_ARG = "office_arg";

    @Inject
    AdminRepository repository;

    private AdminApi mService;
    private View root;
    private Oficina oficina;

    private TextInputEditText TIET_oficinaNom, TIET_oficinaDesc, TIET_oficinaDireccio, TIET_oficinaPoblacio, TIET_oficinaTelf, TIET_oficinaWeb;
    private EditText ETND_oficinaLat, ETND_oficinaLong;

    public OfficeFragment() { }

    public static OfficeFragment newInstance(Oficina o){
        OfficeFragment officeFragment = new OfficeFragment();

        Bundle args = new Bundle(1);
        args.putParcelable(OFFICE_ARG, o);
        officeFragment.setArguments(args);

        return officeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(getArguments() != null)
            oficina = getArguments().getParcelable(OFFICE_ARG);

        root = inflater.inflate(R.layout.oficina_layout, container, false);
        Funcions.closeKeyboard(root.findViewById(R.id.main_parent), requireActivity());

        mService = repository.getApi();

        setViews();

        return root;
    }

    @SuppressLint("SetTextI18n")
    private void setViews() {
        TIET_oficinaNom = root.findViewById(R.id.TIET_oficinaNom);
        TIET_oficinaDesc = root.findViewById(R.id.TIET_oficinaDesc);
        TIET_oficinaDireccio = root.findViewById(R.id.TIET_oficinaDireccio);
        TIET_oficinaPoblacio = root.findViewById(R.id.TIET_oficinaPoblacio);
        TIET_oficinaTelf = root.findViewById(R.id.TIET_oficinaTelf);
        TIET_oficinaWeb = root.findViewById(R.id.TIET_oficinaWeb);

        ETND_oficinaLat = root.findViewById(R.id.ETND_oficinaLat);
        ETND_oficinaLong = root.findViewById(R.id.ETND_oficinaLong);

        ImageButton IB_oficinaInfo = root.findViewById(R.id.IB_oficinaInfo);

        if(oficina != null) {
            TIET_oficinaNom.setText(oficina.name);
            TIET_oficinaDesc.setText(oficina.description);
            TIET_oficinaDireccio.setText(oficina.address);
            TIET_oficinaPoblacio.setText(oficina.ciutat);
            TIET_oficinaTelf.setText(oficina.telf);
            TIET_oficinaWeb.setText(oficina.website);

            ETND_oficinaLat.setText(Double.toString(oficina.latitude));
            ETND_oficinaLong.setText(Double.toString(oficina.longitude));
        }

        IB_oficinaInfo.setOnClickListener(view -> new AlertDialog.Builder(getContext())
                .setTitle("Agafar Longitud i Latitud")
                .setMessage("Per saber quina és la longitud i latitud de la teva oficina, busca-la a Google Maps i prem el botón dret amb l'ordinador: els números que surten, el primer és la latitud i el segon la longitud.")
                .setNeutralButton("D'acord", (dialogInterface, i) -> dialogInterface.dismiss())
                .show());

        Button BT_actualitzarOficina = root.findViewById(R.id.BT_actualitzarOficina);
        BT_actualitzarOficina.setOnClickListener(view -> {
            if(hiHaCampsBuits()) {
                Toast.makeText(getContext(), "Omple tots els camps", Toast.LENGTH_SHORT).show();
            }
            else {
                // Si no existeix l'oficina la creem
                Oficina novaOficina = new Oficina();
                if(oficina == null) {
                    asignarValorsOficinaNova(novaOficina);

                    Call<Long> call = mService.crearOficina(novaOficina);
                    call.enqueue(new Callback<Long>() {
                        @Override
                        public void onResponse(@NotNull Call<Long> call, @NotNull Response<Long> response) {
                            super.onResponse(call, response);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "L'oficina s'ha actualitzat correctament.", Toast.LENGTH_SHORT).show();

                                oficina = new Oficina(response.body(), novaOficina);
                                previewOffice();
                            } else
                                Toast.makeText(getContext(), "No s'ha pogut connectar bé amb el servidor.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(@NotNull Call<Long> call, @NotNull Throwable t) {
                            Toast.makeText(getContext(), "No s'ha pogut connectar bé amb el servidor.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                // Si existeix l'editem
                else {
                    asignarValorsOficina(novaOficina);

                    if(novaOficina.hiHaCanvis(oficina)) {
                        Call<String> call = mService.actualitzarOficina(novaOficina);
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                                super.onResponse(call, response);
                                if (response.isSuccessful()) {
                                    Toast.makeText(getContext(), "L'oficina s'ha actualitzat correctament.", Toast.LENGTH_SHORT).show();
                                    oficina = novaOficina;
                                    previewOffice();
                                } else
                                    Toast.makeText(getContext(), "No s'ha pogut connectar bé amb el servidor.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                                Toast.makeText(getContext(), "No s'ha pogut connectar bé amb el servidor.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                        previewOffice();
                }
            }
        });
    }

    private void previewOffice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(R.string.previsualitzar_titol);
        builder.setMessage(R.string.previsualitzar_desc);
        builder.setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
            Intent intent = new Intent(getContext(), OficinesActivity.class);
            intent.putExtra("idOficina", oficina.id);
            startActivity(intent);
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }

    private void asignarValorsOficina(Oficina novaOficina) {
        novaOficina.id = oficina.id;
        novaOficina.name = Objects.requireNonNull(TIET_oficinaNom.getText()).toString();
        novaOficina.address = Objects.requireNonNull(TIET_oficinaDireccio.getText()).toString();
        novaOficina.ciutat = Objects.requireNonNull(TIET_oficinaPoblacio.getText()).toString();
        novaOficina.description = Objects.requireNonNull(TIET_oficinaDesc.getText()).toString();
        novaOficina.latitude = Double.valueOf(ETND_oficinaLat.getText().toString());
        novaOficina.longitude = Double.valueOf(ETND_oficinaLong.getText().toString());
        novaOficina.telf = Objects.requireNonNull(TIET_oficinaTelf.getText()).toString();
        novaOficina.website = Objects.requireNonNull(TIET_oficinaWeb.getText()).toString();
    }

    private void asignarValorsOficinaNova(Oficina novaOficina) {
        novaOficina.name = Objects.requireNonNull(TIET_oficinaNom.getText()).toString();
        novaOficina.address = Objects.requireNonNull(TIET_oficinaDireccio.getText()).toString();
        novaOficina.ciutat = Objects.requireNonNull(TIET_oficinaPoblacio.getText()).toString();
        novaOficina.description = Objects.requireNonNull(TIET_oficinaDesc.getText()).toString();
        novaOficina.latitude = Double.valueOf(ETND_oficinaLat.getText().toString());
        novaOficina.longitude = Double.valueOf(ETND_oficinaLong.getText().toString());
        novaOficina.telf = Objects.requireNonNull(TIET_oficinaTelf.getText()).toString();
        novaOficina.website = Objects.requireNonNull(TIET_oficinaWeb.getText()).toString();
    }

    private boolean hiHaCampsBuits() {
        if (TIET_oficinaNom.getText() == null || TIET_oficinaNom.getText().length() == 0)
            return true;
        if (TIET_oficinaDesc.getText() == null || TIET_oficinaDesc.getText().length() == 0)
            return true;
        if (TIET_oficinaDireccio.getText() == null || TIET_oficinaDireccio.getText().length() == 0)
            return true;
        if (TIET_oficinaPoblacio.getText() == null || TIET_oficinaPoblacio.getText().length() == 0)
            return true;
        if (TIET_oficinaTelf.getText() == null || TIET_oficinaTelf.getText().length() == 0)
            return true;
        if (TIET_oficinaWeb.getText() == null || TIET_oficinaWeb.getText().length() == 0)
            return true;
        if (ETND_oficinaLat.getText() == null || ETND_oficinaLat.getText().length() == 0)
            return true;
        return ETND_oficinaLong.getText() == null || ETND_oficinaLong.getText().length() == 0;
    }
}
