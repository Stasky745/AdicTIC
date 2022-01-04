package com.adictic.admin.ui.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adictic.admin.R;
import com.adictic.admin.util.Funcions;
import com.adictic.common.entity.Oficina;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class OfficeCreatorActivity extends AppCompatActivity {

    private TextInputEditText TIET_create_oficinaNom;
    private TextInputEditText TIET_create_oficinaDireccio;
    private TextInputEditText TIET_create_oficinaPoblacio;
    private TextInputEditText TIET_create_oficinaWeb;
    private TextInputEditText TIET_create_oficinaTelf;
    private EditText ETND_create_oficinaLat;
    private EditText ETND_create_oficinaLong;
    private TextInputEditText TIET_create_oficinaDesc;

    private Oficina oficina;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_office);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Oficina nova");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Funcions.closeKeyboard(findViewById(R.id.main_parent), this);

        TIET_create_oficinaNom = findViewById(R.id.TIET_create_oficinaNom);
        TIET_create_oficinaDireccio = findViewById(R.id.TIET_create_oficinaDireccio);
        TIET_create_oficinaPoblacio = findViewById(R.id.TIET_create_oficinaPoblacio);
        TIET_create_oficinaWeb = findViewById(R.id.TIET_create_oficinaWeb);
        TIET_create_oficinaTelf = findViewById(R.id.TIET_create_oficinaTelf);
        ETND_create_oficinaLat = findViewById(R.id.ETND_create_oficinaLat);
        ETND_create_oficinaLong = findViewById(R.id.ETND_create_oficinaLong);
        TIET_create_oficinaDesc = findViewById(R.id.TIET_create_oficinaDesc);

        oficina = getIntent().getParcelableExtra("oficina");
        assignValuesOfOffice(oficina);

        findViewById(R.id.IB_create_oficinaInfo).setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle("Agafar Longitud i Latitud")
                .setMessage("Per saber quina és la longitud i latitud de la teva oficina, busca-la a Google Maps i prem el botón dret amb l'ordinador: els números que surten, el primer és la latitud i el segon la longitud.")
                .setNeutralButton("D'acord", (dialogInterface, i) -> dialogInterface.dismiss())
                .show());

        findViewById(R.id.BT_create_guardar_oficina).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getValuesOfOffice();
                Intent _result = new Intent();
                _result.putExtra("oficina", oficina);
                setResult(Activity.RESULT_OK, _result);
                finish();
            }
        });
    }

    private void assignValuesOfOffice(Oficina oficina) {
        if(oficina==null) return;
        TIET_create_oficinaNom.setText(oficina.name);
        TIET_create_oficinaDireccio.setText(oficina.address);
        TIET_create_oficinaPoblacio.setText(oficina.ciutat);
        TIET_create_oficinaWeb.setText(oficina.website);
        TIET_create_oficinaTelf.setText(oficina.telf);
        if(oficina.latitude!=null)
            ETND_create_oficinaLat.setText(String.valueOf(oficina.latitude));
        if(oficina.longitude!=null)
            ETND_create_oficinaLong.setText(String.valueOf(oficina.longitude));
        TIET_create_oficinaDesc.setText(oficina.description);
    }

    private void getValuesOfOffice(){
        if(oficina==null) oficina = new Oficina();

        oficina.name = TIET_create_oficinaNom.getText()!=null ? TIET_create_oficinaNom.getText().toString().trim() : "";
        oficina.latitude = ETND_create_oficinaLat.getText().toString().isEmpty() ? null : Double.parseDouble(ETND_create_oficinaLat.getText().toString());
        oficina.longitude = ETND_create_oficinaLong.getText().toString().isEmpty() ? null : Double.parseDouble(ETND_create_oficinaLong.getText().toString());
        oficina.address = TIET_create_oficinaDireccio.getText()!=null ? TIET_create_oficinaDireccio.getText().toString().trim() : "";
        oficina.ciutat = TIET_create_oficinaPoblacio.getText()!=null ? TIET_create_oficinaPoblacio.getText().toString().trim() : "";
        oficina.description = TIET_create_oficinaDesc.getText()!=null ? TIET_create_oficinaDesc.getText().toString().trim() : "";
        oficina.telf = TIET_create_oficinaTelf.getText()!=null ? TIET_create_oficinaTelf.getText().toString().trim() : "";
        oficina.website = TIET_create_oficinaWeb.getText()!=null ? TIET_create_oficinaWeb.getText().toString().trim() : "";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
