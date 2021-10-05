package com.adictic.client.ui;

import static android.content.Intent.ACTION_DIAL;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.User;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.client.R;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.jitsi.activities.OutgoingInvitationActivity;

import org.joda.time.DateTime;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BlockDeviceActivity extends AppCompatActivity {

    public static BlockDeviceActivity instance;
    private int retryCountAccessDisp;
    private final int TOTAL_RETRIES = 5;
    private AdicticApi mTodoService;
    private long idChild;
    private SharedPreferences sharedPreferences;

    private final BroadcastReceiver finishActivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTodoService = ((AdicticApp) getApplicationContext()).getAPI();
        instance = this;
        setContentView(R.layout.block_device_layout);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(finishActivityReceiver,
                new IntentFilter(Constants.NO_DEVICE_BLOCK_SCREEN));

        sharedPreferences = Funcions.getEncryptedSharedPreferences(BlockDeviceActivity.this);
        assert sharedPreferences != null;

        idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);
        if(idChild == -1)
            finish();

        setText();
        setCallButton();
        setAlarmButton();
        postIntentAccesDisp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Funcions.accessibilityServiceOn()) {
            if (!AccessibilityScreenService.instance.isDeviceBlocked())
                finish();
        }
        else
            finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void setAlarmButton() {

    }

    private void setCallButton() {
        ConstraintLayout CL_device_blocked_call = findViewById(R.id.CL_block_device_emergency_call);

        CL_device_blocked_call.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(getString(R.string.trucar_confirmacio_pares))
                .setMessage(getString(R.string.trucar_desc))
                .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                    mTodoService.callParents(idChild).enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            super.onResponse(call, response);
                            if (response.isSuccessful()) startCall(response.body());
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            super.onFailure(call, t);
                            Toast.makeText(getApplicationContext(), "No s'ha pogut connectar amb el servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel())
                .show());
    }

    private void startCall(String chatId) {
        Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
        intent.putExtra("userId", sharedPreferences.getLong(Constants.SHARED_PREFS_IDTUTOR, -1));
        intent.putExtra("childId", idChild);
        intent.putExtra("username", sharedPreferences.getString(Constants.SHARED_PREFS_USERNAME, ""));
        intent.putExtra("meetingRoom", chatId);
        intent.putExtra("parentCall", true);
        intent.putExtra("type", "video");
        startActivity(intent);
    }

    private void postIntentAccesDisp() {
        retryCountAccessDisp = 0;
        long now = DateTime.now().getMillis();

        Call<String> call = mTodoService.postIntentAccesDisp(idChild, now);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    private void setText() {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(BlockDeviceActivity.this);
        assert sharedPreferences != null;

        TextView TV_block_device_title = findViewById(R.id.TV_block_device_title);
        TV_block_device_title.setText(getString(R.string.locked_device));

        if(sharedPreferences.getInt(Constants.SHARED_PREFS_ACTIVE_EVENTS, 0) > 0){
            List<EventBlock> eventsList = Funcions.readFromFile(BlockDeviceActivity.this, Constants.FILE_EVENT_BLOCK, false);
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
