package com.example.adictic.ui.events;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.adictic.R;
import com.example.adictic.entity.EventBlock;
import com.example.adictic.util.Funcions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.joda.time.DateTime;

import java.util.Objects;

public class EventFragment extends DialogFragment {
    private final EventBlock event;
    private final EventBlock oldEvent;

    private EditText ET_eventName, ET_eventStart, ET_eventEnd;
    private ChipGroup CG_eventDays;
    private Chip CH_Monday, CH_Tuesday, CH_Wednesday, CH_Thursday, CH_Friday, CH_Saturday, CH_Sunday;

    private Button BT_accept, BT_cancel, BT_delete;

    private IEventDialog mCallback;

    public EventFragment(EventBlock he) {
        event = he;
        oldEvent = new EventBlock(he);
    }

    public static EventFragment newInstance(String title, EventBlock horarisEvent) {
        EventFragment frag = new EventFragment(horarisEvent);
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mCallback = (IEventDialog) context;
        } catch (ClassCastException e) {
            Log.d("HorarisEventFragment", "L'activitat no implementa la interfície IEventDialog");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.horaris_event_fragment, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ET_eventName = view.findViewById(R.id.ET_eventName);
        ET_eventName.setText(event.name);
        ET_eventStart = view.findViewById(R.id.ET_eventStart);
        ET_eventStart.setText(
                Funcions.millisOfDay2String(event.startEvent));
        ET_eventEnd = view.findViewById(R.id.ET_eventEnd);
        ET_eventEnd.setText(
                Funcions.millisOfDay2String(event.endEvent));

        CG_eventDays = view.findViewById(R.id.CG_eventDays);
        CH_Monday = view.findViewById(R.id.CH_monday);
        CH_Tuesday = view.findViewById(R.id.CH_tuesday);
        CH_Wednesday = view.findViewById(R.id.CH_wednesday);
        CH_Thursday = view.findViewById(R.id.CH_thursday);
        CH_Friday = view.findViewById(R.id.CH_friday);
        CH_Saturday = view.findViewById(R.id.CH_saturday);
        CH_Sunday = view.findViewById(R.id.CH_sunday);

        setDaysChecked();

        BT_accept = view.findViewById(R.id.BT_accept);
        BT_cancel = view.findViewById(R.id.BT_cancel);
        BT_delete = view.findViewById(R.id.BT_delete);

        setButtons();

        assert getArguments() != null;
        String title = getArguments().getString("title", getString(R.string.events));
        Objects.requireNonNull(getDialog()).setTitle(title);
    }

    private void setDaysChecked() {
        if (event.monday) CH_Monday.setChecked(true);
        if (event.tuesday) CH_Tuesday.setChecked(true);
        if (event.wednesday) CH_Wednesday.setChecked(true);
        if (event.thursday) CH_Thursday.setChecked(true);
        if (event.friday) CH_Friday.setChecked(true);
        if (event.saturday) CH_Saturday.setChecked(true);
        if (event.sunday) CH_Sunday.setChecked(true);
    }

    public void setButtons() {
        CH_Monday.setOnCheckedChangeListener((buttonView, isChecked) ->
                event.monday = isChecked);

        CH_Tuesday.setOnCheckedChangeListener((buttonView, isChecked) ->
                event.tuesday = isChecked);

        CH_Wednesday.setOnCheckedChangeListener((buttonView, isChecked) ->
                event.wednesday = isChecked);

        CH_Thursday.setOnCheckedChangeListener((buttonView, isChecked) ->
                event.thursday = isChecked);

        CH_Friday.setOnCheckedChangeListener((buttonView, isChecked) ->
                event.friday = isChecked);

        CH_Saturday.setOnCheckedChangeListener((buttonView, isChecked) ->
                event.saturday = isChecked);

        CH_Sunday.setOnCheckedChangeListener((buttonView, isChecked) ->
                event.sunday = isChecked);

        ET_eventStart.setOnClickListener(v -> {
            Pair<Integer, Integer> start = Funcions.stringToTime(ET_eventStart.getText().toString());

            final TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.datePicker, (view, hourOfDay, minute) -> {
                DateTime dateTime = new DateTime()
                        .withHourOfDay(hourOfDay)
                        .withMinuteOfHour(minute);

                event.startEvent = dateTime.getMillisOfDay();
                ET_eventStart.setText(Funcions.formatHora(hourOfDay,minute));
            }, start.first, start.second, true);

            timePickerDialog.show();
        });

        ET_eventEnd.setOnClickListener(v -> {
            Pair<Integer, Integer> finish = Funcions.stringToTime(ET_eventEnd.getText().toString());

            final TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.datePicker, (view, hourOfDay, minute) -> {
                String hour;
                String min;

                if (hourOfDay < 10) hour = "0" + hourOfDay;
                else hour = Integer.toString(hourOfDay);

                if (minute < 10) min = "0" + minute;
                else min = Integer.toString(minute);

                DateTime dateTime = new DateTime()
                        .withHourOfDay(hourOfDay)
                        .withMinuteOfHour(minute);

                event.endEvent = dateTime.getMillisOfDay();
                ET_eventEnd.setText(String.format("%s:%s", hour, min));
            }, finish.first, finish.second, true);

            timePickerDialog.show();
        });

        BT_accept.setOnClickListener(v -> {
            Pair<Integer, Integer> startTime = Funcions.stringToTime(ET_eventStart.getText().toString());
            Pair<Integer, Integer> finishTime = Funcions.stringToTime(ET_eventEnd.getText().toString());

            if (ET_eventName.getText() == null || ET_eventName.getText().toString().equals("")) {
                Toast.makeText(getContext(), getString(R.string.error_no_event_name), Toast.LENGTH_LONG).show();
            } else if (CG_eventDays.getCheckedChipIds().isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.error_no_event_days), Toast.LENGTH_LONG).show();
            } else if (finishTime.first < startTime.first || (finishTime.first.equals(startTime.first) && finishTime.second <= startTime.second)) {
                Toast.makeText(getContext(), getString(R.string.error_incorrect_times), Toast.LENGTH_LONG).show();
            } else {
                event.name = ET_eventName.getText().toString();

                mCallback.onSelectedData(event, false);
                dismiss();
            }
        });

        BT_cancel.setOnClickListener(v -> {
            event.startEvent = oldEvent.startEvent;
            event.endEvent = oldEvent.endEvent;
            event.name = oldEvent.name;

            event.monday = oldEvent.monday;
            event.tuesday = oldEvent.tuesday;
            event.wednesday = oldEvent.wednesday;
            event.thursday = oldEvent.thursday;
            event.friday = oldEvent.friday;
            event.saturday = oldEvent.saturday;
            dismiss();
        });

        if(oldEvent.id > 0) {
            BT_delete.setVisibility(View.VISIBLE);
            BT_delete.setOnClickListener(v -> {
                mCallback.onSelectedData(event, true);
                dismiss();
            });
        }
        else
            BT_delete.setVisibility(View.GONE);
    }
}
