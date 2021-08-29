package com.example.adictic.ui.inici;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;

public class PermisosMIUI extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.miui_permisos_layout);

        Button BT_miui_perm = findViewById(R.id.BT_miui_perm);
        BT_miui_perm.setOnClickListener(view -> {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", getPackageName());
            startActivity(intent);
        });

        Button BT_miui_continue = findViewById(R.id.BT_miui_continue);
        BT_miui_continue.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(getString(R.string.permisos))
                .setMessage(getString(R.string.permisos_pregunta))
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
                    PermisosMIUI.this.startActivity(new Intent(PermisosMIUI.this, AppLock.class));
                    PermisosMIUI.this.finish();
                })
                .show());
    }
}
