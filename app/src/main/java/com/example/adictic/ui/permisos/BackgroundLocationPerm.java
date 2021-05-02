package com.example.adictic.ui.permisos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.adictic.R;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Funcions;

import java.security.Permission;
import java.util.ArrayList;

public class BackgroundLocationPerm extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perm_background_location);

        if(Funcions.isBackgroundLocationPermissionOn(getApplicationContext())){
            Funcions.runGeoLocWorker(getApplicationContext());
            startActivity(new Intent(BackgroundLocationPerm.this, NavActivity.class));
            finish();
        }

        PackageManager packageManager = getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            packageManager.getBackgroundPermissionOptionLabel();
        }
        Button BT_okay = findViewById(R.id.BT_okBackLocationPerm);

        BT_okay.setOnClickListener(view -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:"+getPackageName()));
            this.startActivityForResult(intent, 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(Funcions.isBackgroundLocationPermissionOn(getApplicationContext())){
                Funcions.runGeoLocWorker(getApplicationContext());
                startActivity(new Intent(BackgroundLocationPerm.this, NavActivity.class));
                finish();
            }
            else{
                // AlertDialog
            }
        }
    }
}
