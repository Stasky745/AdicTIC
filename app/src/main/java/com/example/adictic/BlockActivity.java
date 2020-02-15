package com.example.adictic;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class BlockActivity extends Activity {
    private static Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_layout);

        ActivityManager manager =  (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        manager.killBackgroundProcesses("com.android.chrome");

        List<ActivityManager.RunningAppProcessInfo> activityes = manager.getRunningAppProcesses();


        Button btn1 = (Button) findViewById(R.id.btn_sortir);
        btn1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent dialogIntent = new Intent(mContext, MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialogIntent);
                //finish();
            }
        });
    }
}
