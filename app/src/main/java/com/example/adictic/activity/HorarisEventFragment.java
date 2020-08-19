package com.example.adictic.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.adictic.R;
import com.example.adictic.entity.HorarisEvents;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class HorarisEventFragment extends DialogFragment {
    private HorarisEvents event;

    private EditText ET_eventName, ET_eventStart, ET_eventEnd;
    private ChipGroup CG_eventDays;

    private Button BT_accept, BT_cancel;

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
        ET_eventStart = (EditText) view.findViewById(R.id.ET_eventStart);
        ET_eventEnd = (EditText) view.findViewById(R.id.ET_eventEnd);

        String title = getArguments().getString("title",getString(R.string.events));
        getDialog().setTitle(title);
    }
}
