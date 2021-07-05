package com.example.adictic.ui;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;

import java.util.List;

public class BlockScreenActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_layout);

        ActivityManager manager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        String pkgName = getIntent().getStringExtra("pkgName");

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
            BlockScreenActivity.this.finish();
        });
    }
}
