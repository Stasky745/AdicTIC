package com.example.adictic.ui.support;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;

public class PreguntesFrequents extends AppCompatActivity {

    private Button BT_QueEsAddiccio;
    private Button BT_defNovesTec;
    private Button BT_causesAddiccio;
    private Button BT_consquenciesUs;
    private Button BT_prevenirProblemes;
    private Button BT_actuacio;
    private Button BT_TractamentAddiccio;
    private Button BT_riscosNovesTec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contingut_informatiu);

        setViews();
        setButtons();
    }

    private void setButtons() {
        BT_QueEsAddiccio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                Intent i = new Intent(PreguntesFrequents.this, InformationActivity.class);
                i.putExtra("title", btn.getText().toString());
                i.putExtra("file", "PreguntesFrequents/queEsUnaAddiccioCAT.html");
                startActivity(i);
            }
        });

        BT_defNovesTec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                Intent i = new Intent(PreguntesFrequents.this, InformationActivity.class);
                i.putExtra("title", btn.getText().toString());
                i.putExtra("file", "PreguntesFrequents/addiccioNovesTecnoCAT.html");
                startActivity(i);
            }
        });

        BT_causesAddiccio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                Intent i = new Intent(PreguntesFrequents.this, InformationActivity.class);
                i.putExtra("title", btn.getText().toString());
                i.putExtra("file", "PreguntesFrequents/causesAddiccioCAT.html");
                startActivity(i);
            }
        });

        BT_consquenciesUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                Intent i = new Intent(PreguntesFrequents.this, InformationActivity.class);
                i.putExtra("title", btn.getText().toString());
                i.putExtra("file", "PreguntesFrequents/consequenciesCAT.html");
                startActivity(i);
            }
        });

        BT_prevenirProblemes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                Intent i = new Intent(PreguntesFrequents.this, InformationActivity.class);
                i.putExtra("title", btn.getText().toString());
                i.putExtra("file", "PreguntesFrequents/prevencioCAT.html");
                startActivity(i);
            }
        });

        BT_actuacio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                Intent i = new Intent(PreguntesFrequents.this, InformationActivity.class);
                i.putExtra("title", btn.getText().toString());
                i.putExtra("file", "PreguntesFrequents/actuarDubteCAT.html");
                startActivity(i);
            }
        });

        BT_TractamentAddiccio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                Intent i = new Intent(PreguntesFrequents.this, InformationActivity.class);
                i.putExtra("title", btn.getText().toString());
                i.putExtra("file", "PreguntesFrequents/tractamentCAT.html");
                startActivity(i);
            }
        });

        BT_riscosNovesTec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                Intent i = new Intent(PreguntesFrequents.this, InformationActivity.class);
                i.putExtra("title", btn.getText().toString());
                i.putExtra("file", "PreguntesFrequents/riscosCAT.html");
                startActivity(i);
            }
        });
    }

    private void setViews() {
        BT_QueEsAddiccio = (Button) findViewById(R.id.BT_QueEsAddiccio);
        BT_defNovesTec = (Button) findViewById(R.id.BT_defNovesTec);
        BT_causesAddiccio = (Button) findViewById(R.id.BT_causesAddiccio);
        BT_consquenciesUs = (Button) findViewById(R.id.BT_consequencies_us);
        BT_prevenirProblemes = (Button) findViewById(R.id.BT_prevenir_problemes);
        BT_actuacio = (Button) findViewById(R.id.BT_actuacio);
        BT_TractamentAddiccio = (Button) findViewById(R.id.BT_tractament_addiccio);
        BT_riscosNovesTec = (Button) findViewById(R.id.BT_riscos_noves_tec);
    }
}
