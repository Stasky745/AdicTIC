package com.adictic.jitsi.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;
import com.adictic.jitsi.R;
import com.adictic.jitsi.utilities.Constants;
import com.adictic.jitsi.utilities.JitsiFuncions;

import org.jitsi.meet.sdk.JitsiMeetActivity;

import retrofit2.Call;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    private String name;
    private Long id;

    private Api api;

    private boolean parentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_invitation);

        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        imageMeetingType.setImageResource(R.drawable.ic_video);

        TextView textUserName = findViewById(R.id.textUserName);
        ImageView adminPhoto = findViewById(R.id.IV_incom_admin_picture);

        parentCall = getIntent().getBooleanExtra("parentCall", false);

        if(parentCall){
            name = getIntent().getStringExtra("child_name");
            id = Long.parseLong(getIntent().getStringExtra("child_id"));
        } else {
            name = getIntent().getStringExtra("admin_name");
            id = Long.parseLong(getIntent().getStringExtra("admin_id"));
        }

        textUserName.setText(name);

        api = ((App) getApplication()).getAPI();

        ImageView imageAcceptInvitation = findViewById(R.id.imageAcceptInvitation);
        imageAcceptInvitation.setOnClickListener(view -> sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_ACCEPTED));

        ImageView imageRejectInvitation = findViewById(R.id.imageRejectInvitation);
        imageRejectInvitation.setOnClickListener(view -> sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_REJECTED));


    }

    private void sendInvitationResponse(String type) {
        int notifId = getIntent().getIntExtra("notification_id",-1);
        if(notifId!=-1) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.cancel(notifId);
        }

        Call<String> call;
        if(parentCall) call = api.answerCallParents(id, type);
        else call = api.answerCallOfAdmin(id, type);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                    super.onResponse(call, response);
                if (response.isSuccessful()) {
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                        try {
                            JitsiMeetActivity.launch(IncomingInvitationActivity.this, JitsiFuncions.createJitsiMeetConferenceOptionsBuilder(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM)).build());
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(IncomingInvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(IncomingInvitationActivity.this, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
                finish();
            }
        });
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)) {
                    Toast.makeText(context, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissionsEnabled();
    }

    private void checkPermissionsEnabled() {
        boolean cameraPermissionEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean micPermissionEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if (cameraPermissionEnabled && micPermissionEnabled) {
            // You can use the API that requires the permission.
            registerResponseReceiver();
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
                            .setNegativeButton(getString(com.adictic.common.R.string.cancel), (dialogInterface, i) -> {
                                finish();
                                dialogInterface.cancel();
                            })
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
                            .setNegativeButton(getString(com.adictic.common.R.string.cancel), (dialogInterface, i) -> {
                                finish();
                                dialogInterface.cancel();
                            })
                            .show();
                } else {
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

    private void registerResponseReceiver(){
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendInvitationResponse(Constants.REMOTE_MSG_INVITATION_REJECTED);
    }
}