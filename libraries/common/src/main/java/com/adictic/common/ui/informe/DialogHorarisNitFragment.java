package com.adictic.common.ui.informe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.adictic.common.R;
import com.adictic.common.entity.HorarisAPI;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.util.Funcions;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

public class DialogHorarisNitFragment extends Fragment {

    private final int TIPUS_HORARIS_DIARIS = 1;
    private final int TIPUS_HORARIS_SETMANA = 2;
    private final int TIPUS_HORARIS_GENERICS = 3;

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

    public DialogHorarisNitFragment(HorarisAPI horarisAPI){
        horarisNits = horarisAPI;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.horaris_layout, container, false);

        setViews(root);
        setViewsTutor();
        setTexts();

        return root;
    }

    private void setViews(View root) {
        TV_info = root.findViewById(R.id.TV_tipusHorari);
        TV_info.setVisibility(View.GONE);

        ChipGroup chipGroup = root.findViewById(R.id.CG_tipusHorari);
        chipGroup.setVisibility(View.GONE);

        SV_horariDiari = root.findViewById(R.id.SV_horariDiari);
        CL_horariGeneric = root.findViewById(R.id.CL_horariGeneric);
        CL_horariSetmana = root.findViewById(R.id.CL_horariSetmana);

        ET_wakeMon = root.findViewById(R.id.ET_wakeMon);
        ET_wakeTue = root.findViewById(R.id.ET_wakeTue);
        ET_wakeWed = root.findViewById(R.id.ET_wakeWed);
        ET_wakeThu = root.findViewById(R.id.ET_wakeThu);
        ET_wakeFri = root.findViewById(R.id.ET_wakeFri);
        ET_wakeSat = root.findViewById(R.id.ET_wakeSat);
        ET_wakeSun = root.findViewById(R.id.ET_wakeSun);

        ET_sleepMon = root.findViewById(R.id.ET_sleepMon);
        ET_sleepTue = root.findViewById(R.id.ET_sleepTue);
        ET_sleepWed = root.findViewById(R.id.ET_sleepWed);
        ET_sleepThu = root.findViewById(R.id.ET_sleepThu);
        ET_sleepFri = root.findViewById(R.id.ET_sleepFri);
        ET_sleepSat = root.findViewById(R.id.ET_sleepSat);
        ET_sleepSun = root.findViewById(R.id.ET_sleepSun);

        ET_wakeGeneric = root.findViewById(R.id.ET_wakeGeneric);
        ET_sleepGeneric = root.findViewById(R.id.ET_sleepGeneric);

        ET_wakeWeekday = root.findViewById(R.id.ET_wakeWeekday);
        ET_wakeWeekend = root.findViewById(R.id.ET_wakeWeekend);

        ET_sleepWeekday = root.findViewById(R.id.ET_sleepWeekday);
        ET_sleepWeekend = root.findViewById(R.id.ET_sleepWeekend);

        BT_sendHoraris = root.findViewById(R.id.BT_sendHoraris);
        BT_sendHoraris.setVisibility(View.GONE);

        BT_clearHoraris = root.findViewById(R.id.BT_clearHoraris);
        BT_clearHoraris.setVisibility(View.GONE);
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

    public void setTexts() {
        int tipus = horarisNits.tipus;
        switch (tipus) {
            case TIPUS_HORARIS_DIARIS:
                SV_horariDiari.setVisibility(View.VISIBLE);
                CL_horariGeneric.setVisibility(View.GONE);
                CL_horariSetmana.setVisibility(View.GONE);
                break;
            case TIPUS_HORARIS_SETMANA:
                SV_horariDiari.setVisibility(View.GONE);
                CL_horariGeneric.setVisibility(View.GONE);
                CL_horariSetmana.setVisibility(View.VISIBLE);
                break;
            case TIPUS_HORARIS_GENERICS:
                SV_horariDiari.setVisibility(View.GONE);
                CL_horariGeneric.setVisibility(View.VISIBLE);
                CL_horariSetmana.setVisibility(View.GONE);
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
}
