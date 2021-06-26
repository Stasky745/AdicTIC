package com.example.adictic_admin.ui.Usuari;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.adictic_admin.App;
import com.example.adictic_admin.R;
import com.example.adictic_admin.entity.HorarisAPI;
import com.example.adictic_admin.entity.HorarisNit;
import com.example.adictic_admin.rest.Api;
import com.example.adictic_admin.util.Funcions;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorarisActivity extends AppCompatActivity {
    private Api mTodoService;
    private long idChild;

    private TextView ET_wakeMon, ET_wakeTue, ET_wakeWed, ET_wakeThu, ET_wakeFri, ET_wakeSat, ET_wakeSun;
    private TextView ET_sleepMon, ET_sleepTue, ET_sleepWed, ET_sleepThu, ET_sleepFri, ET_sleepSat, ET_sleepSun;
    private TextView ET_wakeGeneric;
    private TextView ET_sleepGeneric;
    private TextView ET_wakeWeekday, ET_wakeWeekend;
    private TextView ET_sleepWeekday, ET_sleepWeekend;

    private ScrollView SV_horariDiari;
    private ConstraintLayout CL_horariGeneric;
    private ConstraintLayout CL_horariSetmana;

    private HorarisAPI horarisNits;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_layout);
        mTodoService = ((App) getApplication()).getAPI();

        idChild = getIntent().getLongExtra("idChild", -1);

        setViews();
        setViewsTutor();

        getHoraris();
    }

    private void setViewsTutor() {
        ET_wakeMon.setClickable(false);
        ET_wakeTue.setClickable(false);
        ET_wakeWed.setClickable(false);
        ET_wakeThu.setClickable(false);
        ET_wakeFri.setClickable(false);
        ET_wakeSat.setClickable(false);
        ET_wakeSun.setClickable(false);

        ET_sleepMon.setClickable(false);
        ET_sleepTue.setClickable(false);
        ET_sleepWed.setClickable(false);
        ET_sleepThu.setClickable(false);
        ET_sleepFri.setClickable(false);
        ET_sleepSat.setClickable(false);
        ET_sleepSun.setClickable(false);

        ET_wakeGeneric.setClickable(false);
        ET_sleepGeneric.setClickable(false);

        ET_wakeWeekday.setClickable(false);
        ET_wakeWeekend.setClickable(false);

        ET_sleepWeekday.setClickable(false);
        ET_sleepWeekend.setClickable(false);
    }

    @SuppressLint("ResourceType")
    private void setViews() {
        SV_horariDiari = findViewById(R.id.SV_horariDiari);
        CL_horariGeneric = findViewById(R.id.CL_horariGeneric);
        CL_horariSetmana = findViewById(R.id.CL_horariSetmana);

        ET_wakeMon = findViewById(R.id.ET_wakeMon);
        ET_wakeTue = findViewById(R.id.ET_wakeTue);
        ET_wakeWed = findViewById(R.id.ET_wakeWed);
        ET_wakeThu = findViewById(R.id.ET_wakeThu);
        ET_wakeFri = findViewById(R.id.ET_wakeFri);
        ET_wakeSat = findViewById(R.id.ET_wakeSat);
        ET_wakeSun = findViewById(R.id.ET_wakeSun);

        ET_sleepMon = findViewById(R.id.ET_sleepMon);
        ET_sleepTue = findViewById(R.id.ET_sleepTue);
        ET_sleepWed = findViewById(R.id.ET_sleepWed);
        ET_sleepThu = findViewById(R.id.ET_sleepThu);
        ET_sleepFri = findViewById(R.id.ET_sleepFri);
        ET_sleepSat = findViewById(R.id.ET_sleepSat);
        ET_sleepSun = findViewById(R.id.ET_sleepSun);

        ET_wakeGeneric = findViewById(R.id.ET_wakeGeneric);
        ET_sleepGeneric = findViewById(R.id.ET_sleepGeneric);

        ET_wakeWeekday = findViewById(R.id.ET_wakeWeekday);
        ET_wakeWeekend = findViewById(R.id.ET_wakeWeekend);

        ET_sleepWeekday = findViewById(R.id.ET_sleepWeekday);
        ET_sleepWeekend = findViewById(R.id.ET_sleepWeekend);
    }

    private void getHoraris() {
        Call<HorarisAPI> call = mTodoService.getHoraris(idChild);

        call.enqueue(new Callback<HorarisAPI>() {
            @Override
            public void onResponse(@NonNull Call<HorarisAPI> call, @NonNull Response<HorarisAPI> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        horarisNits = response.body();
                        setTexts(horarisNits.tipus);
                    }
                } else
                    Toast.makeText(HorarisActivity.this, getString(R.string.error_noData), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<HorarisAPI> call, @NonNull Throwable t) {
                Toast.makeText(HorarisActivity.this, getString(R.string.error_noData), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setTexts(int tipus) {
        int TIPUS_HORARIS_DIARIS = 1;
        int TIPUS_HORARIS_SETMANA = 2;
        if(tipus == TIPUS_HORARIS_DIARIS){
            SV_horariDiari.setVisibility(View.VISIBLE);
            CL_horariGeneric.setVisibility(View.GONE);
            CL_horariSetmana.setVisibility(View.GONE);
        }
        else if(tipus == TIPUS_HORARIS_SETMANA){
            SV_horariDiari.setVisibility(View.GONE);
            CL_horariGeneric.setVisibility(View.GONE);
            CL_horariSetmana.setVisibility(View.VISIBLE);
        }
        else{
            SV_horariDiari.setVisibility(View.GONE);
            CL_horariGeneric.setVisibility(View.VISIBLE);
            CL_horariSetmana.setVisibility(View.GONE);
        }

        for (HorarisNit horarisNit : horarisNits.horarisNit){
            if(horarisNit.dia == Calendar.MONDAY){
                if(horarisNit.despertar != -1){
                    ET_wakeMon.setText(Funcions.millisOfDay2String(horarisNit.despertar));
                    ET_wakeGeneric.setText(Funcions.millisOfDay2String(horarisNit.despertar));
                    ET_wakeWeekday.setText(Funcions.millisOfDay2String(horarisNit.despertar));
                }
                if(horarisNit.dormir != -1) {
                    ET_sleepMon.setText(Funcions.millisOfDay2String(horarisNit.dormir));
                    ET_sleepGeneric.setText(Funcions.millisOfDay2String(horarisNit.dormir));
                    ET_sleepWeekday.setText(Funcions.millisOfDay2String(horarisNit.dormir));
                }
            }
            else if(horarisNit.dia == Calendar.TUESDAY){
                if(horarisNit.despertar != -1)
                    ET_wakeTue.setText(Funcions.millisOfDay2String(horarisNit.despertar));

                if(horarisNit.dormir != -1)
                    ET_sleepTue.setText(Funcions.millisOfDay2String(horarisNit.dormir));
            }
            else if(horarisNit.dia == Calendar.WEDNESDAY){
                if(horarisNit.despertar != -1)
                    ET_wakeWed.setText(Funcions.millisOfDay2String(horarisNit.despertar));

                if(horarisNit.dormir != -1)
                    ET_sleepWed.setText(Funcions.millisOfDay2String(horarisNit.dormir));
            }
            else if(horarisNit.dia == Calendar.THURSDAY){
                if(horarisNit.despertar != -1)
                    ET_wakeThu.setText(Funcions.millisOfDay2String(horarisNit.despertar));

                if(horarisNit.dormir != -1)
                    ET_sleepThu.setText(Funcions.millisOfDay2String(horarisNit.dormir));
            }
            else if(horarisNit.dia == Calendar.FRIDAY){
                if(horarisNit.despertar != -1)
                    ET_wakeFri.setText(Funcions.millisOfDay2String(horarisNit.despertar));

                if(horarisNit.dormir != -1)
                    ET_sleepFri.setText(Funcions.millisOfDay2String(horarisNit.dormir));
            }
            else if(horarisNit.dia == Calendar.SATURDAY){
                if(horarisNit.despertar != -1)
                    ET_wakeSat.setText(Funcions.millisOfDay2String(horarisNit.despertar));

                if(horarisNit.dormir != -1)
                    ET_sleepSat.setText(Funcions.millisOfDay2String(horarisNit.dormir));
            }
            else if(horarisNit.dia == Calendar.SUNDAY){
                if(horarisNit.despertar != -1){
                    ET_wakeSun.setText(Funcions.millisOfDay2String(horarisNit.despertar));
                    ET_wakeWeekend.setText(Funcions.millisOfDay2String(horarisNit.despertar));
                }

                if(horarisNit.dormir != -1) {
                    ET_sleepSun.setText(Funcions.millisOfDay2String(horarisNit.dormir));
                    ET_sleepWeekend.setText(Funcions.millisOfDay2String(horarisNit.dormir));
                }
            }
        }
    }
}
