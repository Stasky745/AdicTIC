package com.adictic.client.ui.inici;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.adictic.client.ui.main.NavActivity;

public class AppLock extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_app_layout);

        Button BT_continue = findViewById(R.id.BT_app_lock_continue);
        BT_continue.setOnClickListener(view -> {
            AppLock.this.startActivity(new Intent(AppLock.this, NavActivity.class));
            AppLock.this.finish();
        });
    }
}
