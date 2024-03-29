package com.adictic.client.ui;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adictic.client.R;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.IntentsAccesApp;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BlockAppActivity extends AppCompatActivity {
    private int retryCountAccessApp;
    private final int TOTAL_RETRIES = 6;
    private AlertDialog alertDialog = null;

    private final BroadcastReceiver finishActivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_layout);

        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(finishActivityReceiver,
                new IntentFilter(Constants.NO_APP_BLOCK_SCREEN));

        String pkgName = getIntent().getStringExtra("pkgName");
        String appName = getIntent().getStringExtra("appName");

        setIcon(pkgName, appName);

        setUnlockButton(pkgName);

        postIntentAccesApp(pkgName, appName);

        List<ActivityManager.RunningAppProcessInfo> listOfProcesses = manager.getRunningAppProcesses();
        manager.killBackgroundProcesses(pkgName);
        for (ActivityManager.RunningAppProcessInfo process : listOfProcesses) {
            if (process.processName.contains(pkgName)) {
                android.os.Process.killProcess(process.pid);
                android.os.Process.sendSignal(process.pid, android.os.Process.SIGNAL_KILL);
                manager.killBackgroundProcesses(process.processName);
                break;
            }
        }

        Button btn1 = findViewById(R.id.btn_sortir);
        btn1.setOnClickListener(v -> {
            Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
            startHomescreen.addCategory(Intent.CATEGORY_HOME);
            startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(startHomescreen);
            BlockAppActivity.this.finish();
        });
    }

    private void setUnlockButton(String pkgName) {
        Button BT_desbloquejar = findViewById(R.id.BT_unlock_app);
        BT_desbloquejar.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final View dialogLayout = getLayoutInflater().inflate(R.layout.desbloqueig_dialog, null);
            builder.setView(dialogLayout);
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            // Posem el text adequat al dialog
            TextView TV_unlock_text = dialogLayout.findViewById(R.id.TV_unlock_text);
            TV_unlock_text.setText(getString(R.string.unlock_app));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                TV_unlock_text.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }

            TextView TV_pwd_error = dialogLayout.findViewById(R.id.TV_pwd_error);
            TV_pwd_error.setVisibility(View.INVISIBLE);

            alertDialog = builder.show();

            Button BT_unlock = dialogLayout.findViewById(R.id.BT_dialog_unlock);
            BT_unlock.setOnClickListener(v1 -> {
                TV_pwd_error.setVisibility(View.INVISIBLE);

                SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
                assert sharedPreferences != null;
                AdicticApi api = ((AdicticApp) getApplicationContext()).getAPI();

                EditText ET_unlock_pwd = dialogLayout.findViewById(R.id.ET_unlock_pwd);
                String pwd = Crypt.getSHA256(ET_unlock_pwd.getText().toString());

                Funcions.isPasswordCorrect(BlockAppActivity.this, pwd, valid -> unlockApp(valid, sharedPreferences, pwd, pkgName, api, TV_pwd_error));
            });
        });
    }

    private void unlockApp(boolean valid, @NonNull SharedPreferences sharedPreferences, String pwd, String pkgName, AdicticApi mTodoService, TextView TV_pwd_error) {
        // Si la contrasenya és correcta la guardem
        if(!valid)
            valid = sharedPreferences.getString(Constants.SHARED_PREFS_PASSWORD, "").equals(pwd);

        if(valid){
            // Tanquem el Dialog perquè no peti quan es tanqui l'activitat
            alertDialog.dismiss();

            // Actualitzem les dades del servei d'accessibilitat
            if(Funcions.accessibilityServiceOn(getApplicationContext())) {
                AccessibilityScreenService.instance.removeAppLimitada(pkgName);
                AccessibilityScreenService.instance.removeBlockedApp(pkgName);
                AccessibilityScreenService.instance.isCurrentAppBlocked();
            }

            Long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            List<String> unlockApp = new ArrayList<>();
            unlockApp.add(pkgName);
            Call<String> call2 = mTodoService.unlockApps(idChild, unlockApp);
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

    private void setIcon(String pkgName, String appName) {
        ImageView IV_blocked_app_logo = findViewById(R.id.IV_blocked_app_logo);
        TextView TV_blocked_app_name = findViewById(R.id.TV_blocked_app_name);

        Funcions.setIconDrawable(this, pkgName, IV_blocked_app_logo);
        TV_blocked_app_name.setText(appName);
    }

    private void postIntentAccesApp(String pkgName, String appName) {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(BlockAppActivity.this);
        assert sharedPreferences != null;

        long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);
        if(idChild == -1)
            return;

        retryCountAccessApp = 0;
        long now = DateTime.now().getMillis();

        AdicticApi mTodoService = ((AdicticApp) getApplicationContext()).getAPI();

        IntentsAccesApp intentsAccesApp = new IntentsAccesApp();
        intentsAccesApp.pkgName = pkgName;
        intentsAccesApp.appName = appName;
        intentsAccesApp.data = now;

        Call<String> call = mTodoService.postIntentAccesApp(idChild, intentsAccesApp);
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
}
