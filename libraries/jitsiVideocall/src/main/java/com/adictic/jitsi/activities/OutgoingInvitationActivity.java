package com.adictic.jitsi.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;
import com.adictic.common.util.hilt.Repository;
import com.adictic.jitsi.R;
import com.adictic.jitsi.models.JitsiUser;
import com.adictic.jitsi.utilities.Constants;
import com.adictic.jitsi.utilities.JitsiFuncions;

import org.jitsi.meet.sdk.JitsiMeetActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Response;

@AndroidEntryPoint
public class OutgoingInvitationActivity extends AppCompatActivity {

    @Inject
    Repository repository;

    private final String TAG = "OutgoingInvitationActivity";

    private String inviterToken = null;
    private String meetingRoom  = null;

    private TextView textFirstChar, textUsername, textEmail;

    private JitsiUser userProfile;

    private Api api;

    private boolean parentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_invitation);

        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        imageMeetingType.setImageResource(R.drawable.ic_video);

        textFirstChar  = findViewById(R.id.textFirstChar);
        textUsername   = findViewById(R.id.textUserName);
        textEmail      = findViewById(R.id.textEmail);

        userProfile = new JitsiUser();
        userProfile.userId = getIntent().getLongExtra("userId", -1);
        userProfile.childId = getIntent().getLongExtra("childId", -1);
        userProfile.username = getIntent().getStringExtra("username");
        textUsername.setText(userProfile==null ? "Unknown" : userProfile.username);

        parentCall = getIntent().getBooleanExtra("parentCall", false);

        meetingRoom = getIntent().getStringExtra("meetingRoom");

        ImageView imageStopInvitation = findViewById(R.id.imageStopInvitation);
        imageStopInvitation.setOnClickListener(view -> cancelInvitation());

        api = repository.getApi();
    }

    private void cancelInvitation() {
        Call<String> call;
        if(parentCall) call = api.cancelCallParents(userProfile.childId);
        else call = api.cancelCallToUser(userProfile.userId, userProfile.childId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                super.onResponse(call, response);
                Log.d(TAG,"CancelInvitation send successfully");
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
            }
        });
        finish();
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    try {
                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this, JitsiFuncions.createJitsiMeetConferenceOptionsBuilder(meetingRoom).build());
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(OutgoingInvitationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
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
        cancelInvitation();
    }
}