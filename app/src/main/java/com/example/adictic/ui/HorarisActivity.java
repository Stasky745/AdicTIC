package com.example.adictic.ui;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.adictic.R;
import com.example.adictic.entity.Horaris;
import com.example.adictic.entity.TimeDay;
import com.example.adictic.entity.WakeSleepLists;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.TodoApp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorarisActivity extends AppCompatActivity {

    static ChipGroup chipGroup;
    static Chip CH_horariGeneric;
    static Chip CH_horariDiari;
    static Chip CH_horariSetmana;
    TodoApi mTodoService;
    long idChild;
    int canvis;
    ScrollView SV_horariDiari;
    ConstraintLayout CL_horariGeneric;
    ConstraintLayout CL_horariSetmana;

    TextView ET_wakeMon, ET_wakeTue, ET_wakeWed, ET_wakeThu, ET_wakeFri, ET_wakeSat, ET_wakeSun;
    TextView ET_sleepMon, ET_sleepTue, ET_sleepWed, ET_sleepThu, ET_sleepFri, ET_sleepSat, ET_sleepSun;
    TextView ET_wakeGeneric;
    TextView ET_sleepGeneric;
    TextView ET_wakeWeekday, ET_wakeWeekend;
    TextView ET_sleepWeekday, ET_sleepWeekend;

    TextView TV_info;

    Button BT_sendHoraris;

    WakeSleepLists wakeSleepList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_layout);
        mTodoService = ((TodoApp) getApplication()).getAPI();

        wakeSleepList = new WakeSleepLists();

        canvis = 0;

        idChild = getIntent().getLongExtra("idChild", -1);

        setViews();

        if (TodoApp.getTutor() == 1) {
            TV_info.setVisibility(View.VISIBLE);
            setViewsTutor(true);

            setChipGroup();
            setButton();
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

    private void setViews() {
        TV_info = findViewById(R.id.TV_tipusHorari);
        TV_info.setVisibility(View.GONE);

        chipGroup = findViewById(R.id.CG_tipusHorari);
        CH_horariDiari = findViewById(R.id.CH_diariHorari);
        CH_horariSetmana = findViewById(R.id.CH_setmanaHorari);
        CH_horariGeneric = findViewById(R.id.CH_genericHorari);
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

        setViewsTutor(false);

        BT_sendHoraris = findViewById(R.id.BT_sendHoraris);
        BT_sendHoraris.setVisibility(View.GONE);
    }

    private void getHoraris() {
        Call<Horaris> call = mTodoService.getHoraris(idChild);

        call.enqueue(new Callback<Horaris>() {
            @Override
            public void onResponse(@NonNull Call<Horaris> call, @NonNull Response<Horaris> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        wakeSleepList = response.body().wakeSleepList;
                        if (wakeSleepList != null) setTexts(wakeSleepList);
                    }
                } else
                    Toast.makeText(HorarisActivity.this, getString(R.string.error_noData), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<Horaris> call, @NonNull Throwable t) {
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

    public void setTexts(WakeSleepLists list) {
        switch (list.tipus) {
            case 1:
                chipGroup.check(CH_horariDiari.getId());
            case 2:
                chipGroup.check(CH_horariSetmana.getId());
            case 3:
                chipGroup.check(CH_horariGeneric.getId());
        }

        ET_wakeMon.setText(list.wake.monday);
        ET_wakeTue.setText(list.wake.tuesday);
        ET_wakeWed.setText(list.wake.wednesday);
        ET_wakeThu.setText(list.wake.thursday);
        ET_wakeFri.setText(list.wake.friday);
        ET_wakeSat.setText(list.wake.saturday);
        ET_wakeSun.setText(list.wake.sunday);

        ET_sleepMon.setText(list.sleep.monday);
        ET_sleepTue.setText(list.sleep.tuesday);
        ET_sleepWed.setText(list.sleep.wednesday);
        ET_sleepThu.setText(list.sleep.thursday);
        ET_sleepFri.setText(list.sleep.friday);
        ET_sleepSat.setText(list.sleep.saturday);
        ET_sleepSun.setText(list.sleep.sunday);

        ET_wakeGeneric.setText(list.wake.monday);
        ET_sleepGeneric.setText(list.sleep.monday);

        ET_wakeWeekday.setText(list.wake.monday);
        ET_wakeWeekend.setText(list.wake.sunday);

        ET_sleepWeekday.setText(list.sleep.monday);
        ET_sleepWeekend.setText(list.sleep.sunday);
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
            String time = hourOfDay + ":" + minute1;
            if (!et.getText().equals(time)) {
                et.setText(time);
                canvis = 1;
            }
        };

        TimePickerDialog timePicker = new TimePickerDialog(this, R.style.datePicker, timeListener, hour, minute, true);
        timePicker.show();
    }

    private void setChipGroup() {
        chipGroup.setVisibility(View.VISIBLE);

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
        BT_sendHoraris.setOnClickListener(v -> {
            int checkedId = chipGroup.getCheckedChipId();

            wakeSleepList = setWakeSleepLists(checkedId);

            Horaris horaris = new Horaris();

            horaris.wakeSleepList = wakeSleepList;

            Call<String> call = mTodoService.postHoraris(idChild, horaris);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) finish();
                    else
                        Toast.makeText(HorarisActivity.this, getString(R.string.error_sending_data), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Toast.makeText(HorarisActivity.this, getString(R.string.error_sending_data), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private WakeSleepLists setWakeSleepLists(int checkedId) {
        WakeSleepLists wakeSleepLists = new WakeSleepLists();
        TimeDay wakeTimeDay = new TimeDay();
        TimeDay sleepTimeDay = new TimeDay();

        if (checkedId == CH_horariDiari.getId()) {
            /* Wake up */
            wakeTimeDay.monday = ET_wakeMon.getText().toString();
            wakeTimeDay.tuesday = ET_wakeTue.getText().toString();
            wakeTimeDay.wednesday = ET_wakeWed.getText().toString();
            wakeTimeDay.thursday = ET_wakeThu.getText().toString();
            wakeTimeDay.friday = ET_wakeFri.getText().toString();
            wakeTimeDay.saturday = ET_wakeSat.getText().toString();
            wakeTimeDay.sunday = ET_wakeSun.getText().toString();

            wakeSleepLists.wake = wakeTimeDay;

            /* Sleep */
            sleepTimeDay.monday = ET_sleepMon.getText().toString();
            sleepTimeDay.tuesday = ET_sleepTue.getText().toString();
            sleepTimeDay.wednesday = ET_sleepWed.getText().toString();
            sleepTimeDay.thursday = ET_sleepThu.getText().toString();
            sleepTimeDay.friday = ET_sleepFri.getText().toString();
            sleepTimeDay.saturday = ET_sleepSat.getText().toString();
            sleepTimeDay.sunday = ET_sleepSun.getText().toString();

            wakeSleepLists.sleep = sleepTimeDay;

            wakeSleepLists.tipus = 1;
        } else if (checkedId == CH_horariSetmana.getId()) {
            /* Wake up */
            wakeTimeDay.monday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.tuesday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.wednesday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.thursday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.friday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.saturday = ET_wakeWeekend.getText().toString();
            wakeTimeDay.sunday = ET_wakeWeekend.getText().toString();

            wakeSleepLists.wake = wakeTimeDay;

            /* Sleep */
            sleepTimeDay.monday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.tuesday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.wednesday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.thursday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.friday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.saturday = ET_sleepWeekend.getText().toString();
            sleepTimeDay.sunday = ET_sleepWeekend.getText().toString();

            wakeSleepLists.sleep = sleepTimeDay;

            wakeSleepLists.tipus = 2;
        } else {
            /* Wake up */
            wakeTimeDay.monday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.tuesday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.wednesday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.thursday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.friday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.saturday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.sunday = ET_wakeGeneric.getText().toString();

            wakeSleepLists.wake = wakeTimeDay;

            /* Sleep */
            sleepTimeDay.monday = ET_sleepGeneric.getText().toString();
            sleepTimeDay.tuesday = ET_sleepGeneric.getText().toString();
            sleepTimeDay.wednesday = ET_sleepGeneric.getText().toString();
            sleepTimeDay.thursday = ET_sleepGeneric.getText().toString();
            sleepTimeDay.friday = ET_sleepGeneric.getText().toString();
            sleepTimeDay.saturday = ET_sleepGeneric.getText().toString();
            sleepTimeDay.sunday = ET_sleepGeneric.getText().toString();

            wakeSleepLists.sleep = sleepTimeDay;

            wakeSleepLists.tipus = 3;
        }

        return wakeSleepLists;
    }
}
