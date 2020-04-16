package com.example.adictic.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.adictic.R;

import java.util.List;

public class BlockActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_layout);

        ActivityManager manager =  (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> listOfProcesses = manager.getRunningAppProcesses();
        manager.killBackgroundProcesses("com.android.chrome");
        for (ActivityManager.RunningAppProcessInfo process : listOfProcesses)
        {
            if (process.processName.contains("com.android.chrome"))
            {
                android.os.Process.killProcess(process.pid);
                android.os.Process.sendSignal(process.pid, android.os.Process.SIGNAL_KILL);
                manager.killBackgroundProcesses(process.processName);
                break;
            }
        }


        Button btn1 = findViewById(R.id.btn_sortir);
        btn1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent startHomescreen = new Intent(Intent.ACTION_MAIN);
                startHomescreen.addCategory(Intent.CATEGORY_HOME);
                startHomescreen.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(startHomescreen);
                BlockActivity.this.finish();
            }
        });
    }
}
