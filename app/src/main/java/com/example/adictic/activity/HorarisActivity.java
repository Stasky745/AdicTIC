package com.example.adictic.activity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.TimeDay;
import com.example.adictic.entity.WakeSleepLists;
import com.example.adictic.rest.TodoApi;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorarisActivity extends AppCompatActivity {

    TodoApi mTodoService;

    long idChild;

    static ChipGroup chipGroup;
    static Chip CH_horariGeneric;
    static Chip CH_horariDiari;
    static Chip CH_horariSetmana;

    ScrollView SV_horariDiari;
    ConstraintLayout CL_horariGeneric;
    ConstraintLayout CL_horariSetmana;
    
    TextView ET_wakeMon,ET_wakeTue,ET_wakeWed,ET_wakeThu,ET_wakeFri,ET_wakeSat,ET_wakeSun;
    TextView ET_sleepMon,ET_sleepTue,ET_sleepWed,ET_sleepThu,ET_sleepFri,ET_sleepSat,ET_sleepSun;
    TextView ET_wakeGeneric;
    TextView ET_sleepGeneric;
    TextView ET_wakeWeekday, ET_wakeWeekend;
    TextView ET_sleepWeekday, ET_sleepWeekend;

    Button BT_sendHoraris;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_layout);
        mTodoService = ((TodoApp) getApplication()).getAPI();

        idChild = getIntent().getLongExtra("idChild",-1);

        chipGroup = (ChipGroup) findViewById(R.id.CG_tipusHorari);
        CH_horariDiari = (Chip) findViewById(R.id.CH_diariHorari);
        CH_horariSetmana = (Chip) findViewById(R.id.CH_setmanaHorari);
        CH_horariGeneric = (Chip) findViewById(R.id.CH_genericHorari);

        SV_horariDiari = (ScrollView) findViewById(R.id.SV_horariDiari);
        CL_horariGeneric = (ConstraintLayout) findViewById(R.id.CL_horariGeneric);
        CL_horariSetmana = (ConstraintLayout) findViewById(R.id.CL_horariSetmana);

        ET_wakeMon = (TextView) findViewById(R.id.ET_wakeMon);
        ET_wakeTue = (TextView) findViewById(R.id.ET_wakeTue);
        ET_wakeWed = (TextView) findViewById(R.id.ET_wakeWed);
        ET_wakeThu = (TextView) findViewById(R.id.ET_wakeThu);
        ET_wakeFri = (TextView) findViewById(R.id.ET_wakeFri);
        ET_wakeSat = (TextView) findViewById(R.id.ET_wakeSat);
        ET_wakeSun = (TextView) findViewById(R.id.ET_wakeSun);

        ET_sleepMon = (TextView) findViewById(R.id.ET_sleepMon);
        ET_sleepTue = (TextView) findViewById(R.id.ET_sleepTue);
        ET_sleepWed = (TextView) findViewById(R.id.ET_sleepWed);
        ET_sleepThu = (TextView) findViewById(R.id.ET_sleepThu);
        ET_sleepFri = (TextView) findViewById(R.id.ET_sleepFri);
        ET_sleepSat = (TextView) findViewById(R.id.ET_sleepSat);
        ET_sleepSun = (TextView) findViewById(R.id.ET_sleepSun);

        ET_wakeGeneric = (TextView) findViewById(R.id.ET_wakeGeneric);
        ET_sleepGeneric = (TextView) findViewById(R.id.ET_sleepGeneric);

        ET_wakeWeekday = (TextView) findViewById(R.id.ET_wakeWeekday);
        ET_wakeWeekend = (TextView) findViewById(R.id.ET_wakeWeekend);

        ET_sleepWeekday = (TextView) findViewById(R.id.ET_sleepWeekday);
        ET_sleepWeekend = (TextView) findViewById(R.id.ET_sleepWeekend);

        BT_sendHoraris = (Button) findViewById(R.id.BT_sendHoraris);

        setChipGroup();
        setButton();
        setLayout();
    }

    public void setLayout(){
        Call<WakeSleepLists> call = mTodoService.getHoraris(idChild);

        call.enqueue(new Callback<WakeSleepLists>() {
            @Override
            public void onResponse(Call<WakeSleepLists> call, Response<WakeSleepLists> response) {
                if(response.isSuccessful()){
                    if(response.body() != null){
                        setTexts(response.body());
                    }
                }
            }

            @Override
            public void onFailure(Call<WakeSleepLists> call, Throwable t) {

            }
        });
    }

    public void setTexts(WakeSleepLists list){
        switch(list.tipus){
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

    private void openTimePicker(final TextView et){
        int hour, minute;

        System.out.println("TEXT: "+et.getText().toString());
        if(et.getText().equals("") || et.getText() == null){
            hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            minute = Calendar.getInstance().get(Calendar.MINUTE);
        }
        else{
            String[] time = et.getText().toString().split(":");
            hour = Integer.parseInt(time[0]);
            minute = Integer.parseInt(time[1]);
        }

        TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                et.setText(hourOfDay+":"+minute);
            }
        };

        TimePickerDialog timePicker = new TimePickerDialog(this,R.style.datePicker,timeListener,hour,minute,true);
        timePicker.show();
    }

    private void setChipGroup(){
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if(checkedId == CH_horariDiari.getId()){
                    SV_horariDiari.setVisibility(View.VISIBLE);
                    CL_horariGeneric.setVisibility(View.GONE);
                    CL_horariSetmana.setVisibility(View.GONE);
                }
                else if(checkedId == CH_horariSetmana.getId()){
                    SV_horariDiari.setVisibility(View.GONE);
                    CL_horariGeneric.setVisibility(View.GONE);
                    CL_horariSetmana.setVisibility(View.VISIBLE);
                }
                else{
                    SV_horariDiari.setVisibility(View.GONE);
                    CL_horariGeneric.setVisibility(View.VISIBLE);
                    CL_horariSetmana.setVisibility(View.GONE);
                }
            }
        });
        chipGroup.clearCheck();
        chipGroup.check(CH_horariDiari.getId());
        chipGroup.setSelectionRequired(true);
    }
    
    private void setButton(){
        BT_sendHoraris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedId = chipGroup.getCheckedChipId();

                WakeSleepLists wakeSleepLists = setWakeSleepLists(checkedId);

                Call<String> call = mTodoService.postHoraris(idChild,wakeSleepLists);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if(response.isSuccessful()){
                            Context context = getApplicationContext();
                            CharSequence text = getResources().getString(R.string.successful_entry);
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context,text,duration);
                            toast.show();
                        }
                        else {

                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });

            }
        });
    }

    private WakeSleepLists setWakeSleepLists(int checkedId){
        WakeSleepLists wakeSleepLists = new WakeSleepLists();
        TimeDay wakeTimeDay = new TimeDay();
        TimeDay sleepTimeDay = new TimeDay();

        if(checkedId == CH_horariDiari.getId()){
            /** Wake up */
            wakeTimeDay.monday = ET_wakeMon.getText().toString();
            wakeTimeDay.tuesday = ET_wakeTue.getText().toString();
            wakeTimeDay.wednesday = ET_wakeWed.getText().toString();
            wakeTimeDay.thursday = ET_wakeThu.getText().toString();
            wakeTimeDay.friday = ET_wakeFri.getText().toString();
            wakeTimeDay.saturday = ET_wakeSat.getText().toString();
            wakeTimeDay.sunday = ET_wakeSun.getText().toString();

            wakeSleepLists.wake = wakeTimeDay;

            /** Sleep */
            sleepTimeDay.monday = ET_sleepMon.getText().toString();
            sleepTimeDay.tuesday = ET_sleepTue.getText().toString();
            sleepTimeDay.wednesday = ET_sleepWed.getText().toString();
            sleepTimeDay.thursday = ET_sleepThu.getText().toString();
            sleepTimeDay.friday = ET_sleepFri.getText().toString();
            sleepTimeDay.saturday = ET_sleepSat.getText().toString();
            sleepTimeDay.sunday = ET_sleepSun.getText().toString();

            wakeSleepLists.sleep = sleepTimeDay;

            wakeSleepLists.tipus = 1;
        }
        else if(checkedId == CH_horariSetmana.getId()){
            /** Wake up */
            wakeTimeDay.monday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.tuesday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.wednesday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.thursday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.friday = ET_wakeWeekday.getText().toString();
            wakeTimeDay.saturday = ET_wakeWeekend.getText().toString();
            wakeTimeDay.sunday = ET_wakeWeekend.getText().toString();

            wakeSleepLists.wake = wakeTimeDay;

            /** Sleep */
            sleepTimeDay.monday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.tuesday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.wednesday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.thursday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.friday = ET_sleepWeekday.getText().toString();
            sleepTimeDay.saturday = ET_sleepWeekend.getText().toString();
            sleepTimeDay.sunday = ET_sleepWeekend.getText().toString();

            wakeSleepLists.sleep = sleepTimeDay;

            wakeSleepLists.tipus = 2;
        }
        else{
            /** Wake up */
            wakeTimeDay.monday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.tuesday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.wednesday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.thursday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.friday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.saturday = ET_wakeGeneric.getText().toString();
            wakeTimeDay.sunday = ET_wakeGeneric.getText().toString();

            wakeSleepLists.wake = wakeTimeDay;

            /** Sleep */
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
