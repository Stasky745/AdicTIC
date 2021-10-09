package com.adictic.client.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.common.entity.EventBlock;
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
        setEmergencyCallButton();
        setAlertParentsButton();
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
    protected void onStop() {
        super.onStop();
        finish();
    }

    private void createCall(){
        mTodoService.callParents(idChild).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if (response.isSuccessful()) {
                    startCall(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Toast.makeText(getApplicationContext(), "No s'ha pogut connectar amb el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAlertParentsButton() {
        ConstraintLayout CL_alertParents = findViewById(R.id.CL_block_device_alert);

        CL_alertParents.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(getString(R.string.trucar_confirmacio_pares))
                .setMessage(getString(R.string.trucar_desc))
                .setPositiveButton(R.string.accept, (dialogInterface, i) -> checkPermissionsEnabled())
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel())
                .show());
    }

    private void checkPermissionsEnabled() {
        boolean cameraPermissionEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean micPermissionEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (cameraPermissionEnabled && micPermissionEnabled) {
            // You can use the API that requires the permission.
            createCall();
        } else {
            if(!cameraPermissionEnabled) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
                    new AlertDialog.Builder(this)
                            .setTitle(getString(com.adictic.common.R.string.permission_camera))
                            .setMessage(getString(com.adictic.common.R.string.permission_camera_text))
                            .setPositiveButton(getString(com.adictic.common.R.string.accept), (dialogInterface, i) -> requestPermissionLauncher.launch(Manifest.permission.CAMERA))
                            .setNegativeButton(getString(com.adictic.common.R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel())
                            .show();
                } else{
                    // You can directly ask for the permission.
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            }
            else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)){
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
                    new AlertDialog.Builder(this)
                            .setTitle(getString(com.adictic.common.R.string.permission_mic))
                            .setMessage(getString(com.adictic.common.R.string.permission_mic_text))
                            .setPositiveButton(getString(com.adictic.common.R.string.accept), (dialogInterface, i) -> requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO))
                            .setNegativeButton(getString(com.adictic.common.R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel())
                            .show();
                } else{
                    // You can directly ask for the permission.
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                }
            }
        }
    }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkPermissionsEnabled();
                }
            });


    private void setEmergencyCallButton() {
        ConstraintLayout CL_emergencyCall = findViewById(R.id.CL_block_device_emergency_call);

        CL_emergencyCall.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:112"));
            startActivity(intent);
        });
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
