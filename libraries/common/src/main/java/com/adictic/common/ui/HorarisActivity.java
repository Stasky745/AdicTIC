package com.adictic.common.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.HtmlCompat;

import com.adictic.common.R;
import com.adictic.common.entity.HorarisAPI;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorarisActivity extends AppCompatActivity {

    private final int TIPUS_HORARIS_DIARIS = 1;
    private final int TIPUS_HORARIS_SETMANA = 2;
    private final int TIPUS_HORARIS_GENERICS = 3;

    private static ChipGroup chipGroup;
    private static Chip CH_horariGeneric;
    private static Chip CH_horariDiari;
    private static Chip CH_horariSetmana;
    private Api mTodoService;
    private long idChild;
    private int canvis;
    private ScrollView SV_horariDiari;
    private ConstraintLayout CL_horariGeneric;
    private ConstraintLayout CL_horariSetmana;

    private TextView ET_wakeMon, ET_wakeTue, ET_wakeWed, ET_wakeThu, ET_wakeFri, ET_wakeSat, ET_wakeSun;
    private TextView ET_sleepMon, ET_sleepTue, ET_sleepWed, ET_sleepThu, ET_sleepFri, ET_sleepSat, ET_sleepSun;
    private TextView ET_wakeGeneric;
    private TextView ET_sleepGeneric;
    private TextView ET_wakeWeekday, ET_wakeWeekend;
    private TextView ET_sleepWeekday, ET_sleepWeekend;

    private TextView TV_info;

    private Button BT_sendHoraris;
    private Button BT_clearHoraris;

    private HorarisAPI horarisNits;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_layout);
        mTodoService = ((App) getApplication()).getAPI();
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        canvis = 0;

        idChild = getIntent().getLongExtra("idChild", -1);

        setViews();

        assert sharedPreferences != null;
        if (sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false)) {
            TV_info.setVisibility(View.VISIBLE);
            setViewsTutor(true);

            setChipGroup(true);
            setButton();
        }
        else {
            setViewsTutor(false);
            setChipGroup(false);
        }

        getHoraris();
    }

    private void setViewsTutor(boolean b) {
        ET_wakeMon.setClickable(b);
        ET_wakeTue.setClickable(b);
        ET_wakeWed.setClickable(b);
        ET_wakeThu.setClickable(b);
        ET_wakeFri.setClickable(b);
        ET_wakeSat.setClickable(b);
        ET_wakeSun.setClickable(b);

        ET_sleepMon.setClickable(b);
        ET_sleepTue.setClickable(b);
        ET_sleepWed.setClickable(b);
        ET_sleepThu.setClickable(b);
        ET_sleepFri.setClickable(b);
        ET_sleepSat.setClickable(b);
        ET_sleepSun.setClickable(b);

        ET_wakeGeneric.setClickable(b);
        ET_sleepGeneric.setClickable(b);

        ET_wakeWeekday.setClickable(b);
        ET_wakeWeekend.setClickable(b);

        ET_sleepWeekday.setClickable(b);
        ET_sleepWeekend.setClickable(b);
    }

    @SuppressLint("ResourceType")
    private void setViews() {
        TV_info = findViewById(R.id.TV_tipusHorari);
        TV_info.setVisibility(View.GONE);

        chipGroup = findViewById(R.id.CG_tipusHorari);
        CH_horariDiari = findViewById(R.id.CH_diariHorari);
        CH_horariDiari.setId(1);
        CH_horariSetmana = findViewById(R.id.CH_setmanaHorari);
        CH_horariSetmana.setId(2);
        CH_horariGeneric = findViewById(R.id.CH_genericHorari);
        CH_horariGeneric.setId(3);
        chipGroup.setVisibility(View.GONE);

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

        BT_sendHoraris = findViewById(R.id.BT_sendHoraris);
        BT_sendHoraris.setVisibility(View.GONE);

        BT_clearHoraris = findViewById(R.id.BT_clearHoraris);
        BT_clearHoraris.setVisibility(View.GONE);
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

    @Override
    public void onBackPressed() {
        if (canvis == 0) { // && wakeSleepList.tipus == chipGroup.getCheckedChipId()){
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.closing_activity))
                    .setMessage(getString(R.string.exit_without_save))
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        Intent returnIntent = new Intent();
                        setResult(RESULT_CANCELED, returnIntent);
                        HorarisActivity.super.onBackPressed();
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }

    public void setTexts(int tipus) {
        switch (tipus) {
            case TIPUS_HORARIS_DIARIS:
                chipGroup.check(CH_horariDiari.getId());
                break;
            case TIPUS_HORARIS_SETMANA:
                chipGroup.check(CH_horariSetmana.getId());
                break;
            case TIPUS_HORARIS_GENERICS:
                chipGroup.check(CH_horariGeneric.getId());
                break;
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

    public void timeDialog(View v) {
        openTimePicker((TextView) v);
    }

    private void openTimePicker(final TextView et) {
        int hour, minute;

        if (et.getText().equals("") || et.getText() == null) {
            hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            minute = Calendar.getInstance().get(Calendar.MINUTE);
        } else {
            String[] time = et.getText().toString().split(":");
            hour = Integer.parseInt(time[0]);
            minute = Integer.parseInt(time[1]);
        }

        TimePickerDialog.OnTimeSetListener timeListener = (view, hourOfDay, minute1) -> {
            String time = Funcions.formatHora(hourOfDay, minute1);
            if (!et.getText().equals(time)) {
                et.setText(time);
                canvis = 1;
            }
        };

        TimePickerDialog timePicker = new TimePickerDialog(this, R.style.datePicker, timeListener, hour, minute, true);
        timePicker.show();
    }

    private void setChipGroup(boolean bool) {
        if (bool)
            chipGroup.setVisibility(View.VISIBLE);
        else
            chipGroup.setVisibility(View.GONE);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == CH_horariDiari.getId()) {
                SV_horariDiari.setVisibility(View.VISIBLE);
                CL_horariGeneric.setVisibility(View.GONE);
                CL_horariSetmana.setVisibility(View.GONE);
            } else if (checkedId == CH_horariSetmana.getId()) {
                SV_horariDiari.setVisibility(View.GONE);
                CL_horariGeneric.setVisibility(View.GONE);
                CL_horariSetmana.setVisibility(View.VISIBLE);
            } else {
                SV_horariDiari.setVisibility(View.GONE);
                CL_horariGeneric.setVisibility(View.VISIBLE);
                CL_horariSetmana.setVisibility(View.GONE);
            }
        });
        chipGroup.clearCheck();
        chipGroup.check(CH_horariDiari.getId());
        chipGroup.setSelectionRequired(true);
    }

    private void setButton() {
        BT_sendHoraris.setVisibility(View.VISIBLE);
        BT_sendHoraris.setOnClickListener(v -> sendHoraris());

        BT_clearHoraris.setVisibility(View.VISIBLE);
        BT_clearHoraris.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                .setTitle(getString(R.string.esborrar_horaris_title))
                .setMessage(getString(R.string.esborrar_horaris_desc))
                .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> clearHoraris())
                .setNegativeButton(getString(R.string.no), null)
                .show());
    }

    private void clearHoraris() {
        canvis = 1;
        ET_wakeMon.setText("");
        ET_wakeTue.setText("");
        ET_wakeWed.setText("");
        ET_wakeThu.setText("");
        ET_wakeFri.setText("");
        ET_wakeSat.setText("");
        ET_wakeSun.setText("");

        ET_sleepMon.setText("");
        ET_sleepTue.setText("");
        ET_sleepWed.setText("");
        ET_sleepThu.setText("");
        ET_sleepFri.setText("");
        ET_sleepSat.setText("");
        ET_sleepSun.setText("");

        ET_wakeGeneric.setText("");
        ET_sleepGeneric.setText("");

        ET_wakeWeekday.setText("");
        ET_wakeWeekend.setText("");

        ET_sleepWeekday.setText("");
        ET_sleepWeekend.setText("");
    }

    private void sendHoraris() {
        if(canvis == 0) {
            finish();
            return;
        }

        int checkedId = chipGroup.getCheckedChipId();

        List<HorarisNit> horarisNits = setWakeSleepLists(checkedId);

        if (horarisNits != null) {
            HorarisAPI horaris = new HorarisAPI();
            horaris.horarisNit = horarisNits;
            horaris.tipus = chipGroup.getCheckedChipId();

            Call<String> call = mTodoService.postHoraris(idChild, horaris);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful())
                        finish();
                    else
                        Toast.makeText(HorarisActivity.this, getString(R.string.error_sending_data), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(HorarisActivity.this, getString(R.string.error_sending_data), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private List<HorarisNit> setWakeSleepLists(int checkedId) {
        List<HorarisNit> res = new ArrayList<>();

        for(int i = 1; i <= 7; i++){
            HorarisNit horari = new HorarisNit();
            horari.dia = i;
            if(i == Calendar.MONDAY){
                if (checkedId == CH_horariDiari.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeMon.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepMon.getText().toString());
                }
                else if (checkedId == CH_horariSetmana.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeWeekday.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepWeekday.getText().toString());
                }
                else{
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeGeneric.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepGeneric.getText().toString());
                }
            }
            else if(i == Calendar.TUESDAY){
                if (checkedId == CH_horariDiari.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeTue.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepTue.getText().toString());
                }
                else if (checkedId == CH_horariSetmana.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeWeekday.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepWeekday.getText().toString());
                }
                else{
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeGeneric.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepGeneric.getText().toString());
                }
            }
            else if(i == Calendar.WEDNESDAY){
                if (checkedId == CH_horariDiari.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeWed.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepWed.getText().toString());
                }
                else if (checkedId == CH_horariSetmana.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeWeekday.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepWeekday.getText().toString());
                }
                else{
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeGeneric.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepGeneric.getText().toString());
                }
            }
            else if(i == Calendar.THURSDAY){
                if (checkedId == CH_horariDiari.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeThu.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepThu.getText().toString());
                }
                else if (checkedId == CH_horariSetmana.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeWeekday.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepWeekday.getText().toString());
                }
                else{
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeGeneric.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepGeneric.getText().toString());
                }
            }
            else if(i == Calendar.FRIDAY){
                if (checkedId == CH_horariDiari.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeFri.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepFri.getText().toString());
                }
                else if (checkedId == CH_horariSetmana.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeWeekday.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepWeekday.getText().toString());
                }
                else{
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeGeneric.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepGeneric.getText().toString());
                }
            }
            else if(i == Calendar.SATURDAY){
                if (checkedId == CH_horariDiari.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeSat.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepSat.getText().toString());
                }
                else if (checkedId == CH_horariSetmana.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeWeekend.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepWeekend.getText().toString());
                }
                else{
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeGeneric.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepGeneric.getText().toString());
                }
            }
            else {
                if (checkedId == CH_horariDiari.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeSun.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepSun.getText().toString());
                }
                else if (checkedId == CH_horariSetmana.getId()) {
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeWeekend.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepWeekend.getText().toString());
                }
                else{
                    horari.despertar = Funcions.string2MillisOfDay(ET_wakeGeneric.getText().toString());
                    horari.dormir = Funcions.string2MillisOfDay(ET_sleepGeneric.getText().toString());
                }
            }
            if(horari.despertar > horari.dormir) {
                errorHorarisDialog(i);
                return null;
            }
            res.add(horari);
        }

        return res;
    }

    // Passem el dia de la setmana
    private void errorHorarisDialog(int i) {
        if(chipGroup.getCheckedChipId() == TIPUS_HORARIS_DIARIS){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, i);
            String day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            Spanned body = HtmlCompat.fromHtml(getString(R.string.horaris_incorrectes_body, day), HtmlCompat.FROM_HTML_MODE_LEGACY);

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.horaris_incorrectes)
                    .setMessage(body)
                    .setNeutralButton(getString(R.string.accept), null)
                    .show();
        }
        else if(chipGroup.getCheckedChipId() == TIPUS_HORARIS_SETMANA){
            String day;

            if(i == 1 || i == 7)
                day = getString(R.string.weekend);
            else
                day = getString(R.string.weekday);

            Spanned body = HtmlCompat.fromHtml(getString(R.string.horaris_incorrectes_body, day), HtmlCompat.FROM_HTML_MODE_LEGACY);

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.horaris_incorrectes)
                    .setMessage(body)
                    .setNeutralButton(getString(R.string.accept), null)
                    .show();
        }
        else if(chipGroup.getCheckedChipId() == TIPUS_HORARIS_GENERICS){
            String day = "";

            Spanned body = HtmlCompat.fromHtml(getString(R.string.horaris_incorrectes_body, day), HtmlCompat.FROM_HTML_MODE_LEGACY);

            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.horaris_incorrectes)
                    .setMessage(body)
                    .setNeutralButton(getString(R.string.accept), null)
                    .show();
        }
    }
}
