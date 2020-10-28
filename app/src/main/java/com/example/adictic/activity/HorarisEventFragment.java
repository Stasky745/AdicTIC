package com.example.adictic.activity;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.adictic.R;
import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.util.Funcions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

public class HorarisEventFragment extends DialogFragment{
    private HorarisEvents event;

    private EditText ET_eventName, ET_eventStart, ET_eventEnd;
    private ChipGroup CG_eventDays;
    private Chip CH_Monday, CH_Tuesday, CH_Wednesday, CH_Thursday, CH_Friday, CH_Saturday, CH_Sunday;

    private Button BT_accept, BT_cancel;

    private IEventDialog mCallback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mCallback = (IEventDialog) context;
        }
        catch (ClassCastException e) {
            Log.d("HorarisEventFragment", "L'activitat no implementa la interfície IEventDialog");
        }
    }

    public HorarisEventFragment(HorarisEvents he){ event = he; }

    public static HorarisEventFragment newInstance(String title, HorarisEvents horarisEvent){
        HorarisEventFragment frag = new HorarisEventFragment(horarisEvent);
        Bundle args = new Bundle();
        args.putString("title",title);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.horaris_event,container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ET_eventName = (EditText) view.findViewById(R.id.ET_eventName);
        ET_eventName.setText(event.name);
        ET_eventStart = (EditText) view.findViewById(R.id.ET_eventStart);
        ET_eventStart.setText(event.start);
        ET_eventEnd = (EditText) view.findViewById(R.id.ET_eventEnd);
        ET_eventEnd.setText(event.finish);

        CG_eventDays = (ChipGroup) view.findViewById(R.id.CG_eventDays);
        CH_Monday = (Chip) view.findViewById(R.id.CH_monday);
        CH_Tuesday = (Chip) view.findViewById(R.id.CH_tuesday);
        CH_Wednesday = (Chip) view.findViewById(R.id.CH_wednesday);
        CH_Thursday = (Chip) view.findViewById(R.id.CH_thursday);
        CH_Friday = (Chip) view.findViewById(R.id.CH_friday);
        CH_Saturday = (Chip) view.findViewById(R.id.CH_saturday);
        CH_Sunday = (Chip) view.findViewById(R.id.CH_sunday);

        if(event.days.contains(Calendar.MONDAY)) CH_Monday.setChecked(true);
        if(event.days.contains(Calendar.TUESDAY)) CH_Tuesday.setChecked(true);
        if(event.days.contains(Calendar.WEDNESDAY)) CH_Wednesday.setChecked(true);
        if(event.days.contains(Calendar.THURSDAY)) CH_Thursday.setChecked(true);
        if(event.days.contains(Calendar.FRIDAY)) CH_Friday.setChecked(true);
        if(event.days.contains(Calendar.SATURDAY)) CH_Saturday.setChecked(true);
        if(event.days.contains(Calendar.SUNDAY)) CH_Sunday.setChecked(true);

        BT_accept = (Button) view.findViewById(R.id.BT_accept);
        BT_cancel = (Button) view.findViewById(R.id.BT_cancel);

        setButtons();

        String title = getArguments().getString("title",getString(R.string.events));
        getDialog().setTitle(title);
    }

    public void setButtons(){
        CH_Monday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) event.days.add(Calendar.MONDAY);
                else event.days.remove(Integer.valueOf(Calendar.MONDAY));
            }
        });

        CH_Tuesday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) event.days.add(Calendar.TUESDAY);
                else event.days.remove(Integer.valueOf(Calendar.TUESDAY));
            }
        });

        CH_Wednesday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) event.days.add(Calendar.WEDNESDAY);
                else event.days.remove(Integer.valueOf(Calendar.WEDNESDAY));
            }
        });

        CH_Thursday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) event.days.add(Calendar.THURSDAY);
                else event.days.remove(Integer.valueOf(Calendar.THURSDAY));
            }
        });

        CH_Friday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) event.days.add(Calendar.FRIDAY);
                else event.days.remove(Integer.valueOf(Calendar.FRIDAY));
            }
        });

        CH_Saturday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) event.days.add(Calendar.SATURDAY);
                else event.days.remove(Integer.valueOf(Calendar.SATURDAY));
            }
        });

        CH_Sunday.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) event.days.add(Calendar.SUNDAY);
                else event.days.remove(Integer.valueOf(Calendar.SUNDAY));
            }
        });

        ET_eventStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Integer,Integer> start = Funcions.stringToTime(ET_eventStart.getText().toString());

                final TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.datePicker, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hour;
                        String min;

                        if(hourOfDay<10) hour = "0"+hourOfDay;
                        else hour = Integer.toString(hourOfDay);

                        if(minute<10) min = "0"+minute;
                        else min = Integer.toString(minute);

                        String time = hour+":"+min;
                        ET_eventStart.setText(time);
                        event.start = time;
                    }
                }, start.first, start.second, true);

                timePickerDialog.show();
            }
        });

        ET_eventEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Integer,Integer> finish = Funcions.stringToTime(ET_eventEnd.getText().toString());

                final TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.datePicker, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String hour;
                        String min;

                        if(hourOfDay<10) hour = "0"+hourOfDay;
                        else hour = Integer.toString(hourOfDay);

                        if(minute<10) min = "0"+minute;
                        else min = Integer.toString(minute);

                        String time = hour+":"+min;
                        ET_eventEnd.setText(time);
                        event.finish = time;
                    }
                }, finish.first, finish.second, true);

                timePickerDialog.show();
            }
        });

        BT_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Pair<Integer,Integer> startTime = Funcions.stringToTime(ET_eventStart.getText().toString());
                Pair<Integer,Integer> finishTime = Funcions.stringToTime(ET_eventEnd.getText().toString());

                if(ET_eventName.getText() == null || ET_eventName.getText().toString().equals("")){
                    Toast.makeText(getContext(),getString(R.string.error_no_event_name),Toast.LENGTH_LONG).show();
                }
                else if(CG_eventDays.getCheckedChipIds().isEmpty()){
                    Toast.makeText(getContext(),getString(R.string.error_no_event_days),Toast.LENGTH_LONG).show();
                }
                else if(finishTime.first < startTime.first || (finishTime.first.equals(startTime.first) && finishTime.second <= startTime.second)){
                    Toast.makeText(getContext(),getString(R.string.error_incorrect_times),Toast.LENGTH_LONG).show();
                }
                else{
                    event.name = ET_eventName.getText().toString();
                    mCallback.onSelectedData(event);
                    dismiss();
                }
            }
        });

        BT_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }
}