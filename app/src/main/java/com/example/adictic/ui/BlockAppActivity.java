package com.example.adictic.ui;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adictic.common.entity.IntentsAccesApp;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.example.adictic.R;
import com.example.adictic.rest.AdicticApi;
import com.example.adictic.util.AdicticApp;
import com.example.adictic.util.Funcions;

import org.joda.time.DateTime;

import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class BlockAppActivity extends AppCompatActivity {
    private int retryCountAccessApp;
    private final int TOTAL_RETRIES = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_layout);

        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        String pkgName = getIntent().getStringExtra("pkgName");
        String appName = getIntent().getStringExtra("appName");

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
