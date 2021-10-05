package com.adictic.jitsi.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
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