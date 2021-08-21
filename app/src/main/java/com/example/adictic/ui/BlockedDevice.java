package com.example.adictic.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.adictic.common.entity.EventBlock;
import com.adictic.common.util.Constants;
import com.example.adictic.R;
import com.example.adictic.util.Funcions;

import java.util.List;

public class BlockedDevice extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_device_layout);

        setText();
        setCallButton();
        setAlarmButton();
    }

    private void setAlarmButton() {

    }

    private void setCallButton() {
        ConstraintLayout CL_device_blocked_call = findViewById(R.id.CL_block_device_emergency_call);
        CL_device_blocked_call.setOnClickListener(view -> {
            Uri number = Uri.parse("tel:" + 112);
            Intent dial = new Intent(Intent.ACTION_CALL, number);
            startActivity(dial);
        });
    }

    private void setText() {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(BlockedDevice.this);
        assert sharedPreferences != null;

        TextView TV_block_device_title = findViewById(R.id.TV_block_device_title);
        TV_block_device_title.setText(getString(R.string.locked_device));

        if(sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS, 0) > 0){
            List<EventBlock> eventsList = Funcions.readFromFile(BlockedDevice.this, Constants.FILE_EVENT_BLOCK, false);
            assert eventsList != null;

            EventBlock activeEvent = eventsList.stream()
                    .filter(Funcions::eventBlockIsActive)
                    .findFirst()
                    .orElse(null);

            if(activeEvent != null){
                String title = activeEvent.name + "\n";
                title += Funcions.millisOfDay2String(activeEvent.startEvent) + " - " + Funcions.millisOfDay2String(activeEvent.endEvent);
                TV_block_device_title.setText(title);
            }
        }

    }
}
