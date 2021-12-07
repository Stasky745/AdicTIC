package com.adictic.client.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.adictic.client.R;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.adictic.jitsi.activities.OutgoingInvitationActivity;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class BlockDeviceActivity extends AppCompatActivity {

    public static BlockDeviceActivity instance;
    private AdicticApi mTodoService;
    private long idChild;
    private SharedPreferences sharedPreferences;
    private AlertDialog alertDialog = null;

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

        String message = getIntent().getStringExtra("message");

        setText(message);
        setEmergencyCallButton();
        setAlertParentsButton();
        setUnlockButton();
        postIntentAccesDisp();
    }

    private void setUnlockButton() {
        Button BT_desbloquejar = findViewById(R.id.BT_desbloqueig);
        BT_desbloquejar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final View dialogLayout = getLayoutInflater().inflate(R.layout.desbloqueig_dialog, null);
            builder.setView(dialogLayout);
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            // Posem el text adequat al dialog
            TextView TV_unlock_text = dialogLayout.findViewById(R.id.TV_unlock_text);
            TV_unlock_text.setText(getString(R.string.unlock_freeuse_text));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                TV_unlock_text.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }

            TextView TV_pwd_error = dialogLayout.findViewById(R.id.TV_pwd_error);
            TV_pwd_error.setVisibility(View.INVISIBLE);

            alertDialog = builder.show();

            Button BT_unlock = dialogLayout.findViewById(R.id.BT_dialog_unlock);
            BT_unlock.setOnClickListener(v1 -> {
                TV_pwd_error.setVisibility(View.INVISIBLE);

                EditText ET_unlock_pwd = dialogLayout.findViewById(R.id.ET_unlock_pwd);
                String pwd = Crypt.getSHA256(ET_unlock_pwd.getText().toString());

                UserLogin userLogin = new UserLogin();
                userLogin.password = pwd;
                userLogin.username = sharedPreferences.getString(Constants.SHARED_PREFS_USERNAME, "");
                userLogin.token = "";
                userLogin.tutor = -1;

                Call<String> call = mTodoService.checkPassword(userLogin);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if(response.isSuccessful() && response.body() != null){
                            boolean valid = response.body().equals("ok");
                            if(valid)
                                sharedPreferences.edit().putString(Constants.SHARED_PREFS_PASSWORD, pwd).apply();

                            unlockDevice(valid, pwd, TV_pwd_error);
                        }
                        else
                            unlockDevice(false, pwd, TV_pwd_error);
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        unlockDevice(false, pwd, TV_pwd_error);
                    }
                });
            });
        });
    }

    private void unlockDevice(boolean valid, String pwd, TextView TV_pwd_error){
        // Si la contrasenya és correcta la guardem
        if(!valid)
            valid = sharedPreferences.getString(Constants.SHARED_PREFS_PASSWORD, "").equals(pwd);

        if(valid){
            // Tanquem el Dialog perquè no peti quan es tanqui l'activitat
            alertDialog.dismiss();

            // Actualitzem les dades del servei d'accessibilitat
            if(Funcions.accessibilityServiceOn(getApplicationContext())) {
                AccessibilityScreenService.instance.setBlockDevice(false);
                AccessibilityScreenService.instance.setFreeUse(true);
                AccessibilityScreenService.instance.updateDeviceBlock();
            }

            sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BLOCKEDDEVICE, false).apply();
            sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_FREEUSE, true).apply();

            Long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            Call<String> call2 = mTodoService.freeUse(idChild, true);
            call2.enqueue(new Callback<String>() {
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
        else
            TV_pwd_error.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Funcions.accessibilityServiceOn(getApplicationContext())) {
            if (!AccessibilityScreenService.instance.isDeviceBlocked())
                finish();
        }
        else
            finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
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

    private void setText(String message) {
        // Si no hi ha missatge posem el text per defecte
        if(message == null || message.equals(""))
            message = getString(R.string.locked_device);
        // Si hi ha un event actiu busquem el nom i hora
        else if(message.equals("activeEvent")){
            // Posem el text per defecte en cas que no vagi bé
            message = getString(R.string.locked_device);

            // Agafem la llista d'events actius
            List<EventBlock> list = Funcions.readFromFile(getApplicationContext(), Constants.FILE_EVENT_BLOCK, false);
            if(list != null && !list.isEmpty()) {
//                List<EventBlock> list2 = list.stream()
//                        .filter(Funcions::eventBlockIsActive)
//                        .collect(Collectors.toList());

                // Agafem l'event actiu que acabi més tard
                EventBlock event = list.stream()
                        .filter(Funcions::eventBlockIsActive)
                        .collect(Collectors.toList())
                        .stream()
                        .max(Comparator.comparing(eventBlock -> eventBlock.endEvent))
                        .orElse(null);

                if(event != null)
                    message = event.name + "\n" + new DateTime().withMillisOfDay(event.startEvent).toString("HH:ss", Locale.getDefault()) + " - " + new DateTime().withMillisOfDay(event.endEvent).toString("HH:ss", Locale.getDefault());
            }
        }

        TextView TV_block_device_title = findViewById(R.id.TV_block_device_title);
        TV_block_device_title.setText(message);
    }
}
