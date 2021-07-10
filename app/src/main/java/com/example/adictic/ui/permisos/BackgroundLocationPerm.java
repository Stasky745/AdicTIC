package com.example.adictic.ui.permisos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.adictic.R;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.util.Funcions;

import java.util.ArrayList;

public class BackgroundLocationPerm extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perm_background_location);

        if(Funcions.isBackgroundLocationPermissionOn(getApplicationContext())){
//            Funcions.runGeoLocWorker(getApplicationContext());
            startActivity(new Intent(BackgroundLocationPerm.this, NavActivity.class));
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            PackageManager packageManager = getPackageManager();
            packageManager.getBackgroundPermissionOptionLabel();
        }
        Button BT_okay = findViewById(R.id.BT_okBackLocationPerm);

        BT_okay.setOnClickListener(view -> {
            demanarPermisos();
        });
    }

    private void demanarPermisos() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            askPermissionsIfNecessary();
        }
        else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + getPackageName()));
            this.startActivityForResult(intent, 1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(Funcions.isBackgroundLocationPermissionOn(getApplicationContext())){
//                Funcions.runGeoLocWorker(getApplicationContext());
                startActivity(new Intent(BackgroundLocationPerm.this, NavActivity.class));
                finish();
            }
            else{
                new AlertDialog.Builder(this)
                        .setTitle("Continuar sense permís de Localització?")
                        .setMessage("Sense el permís de localització no es podrà tenir un seguiment de la geolocalització d'aquest dispositiu, segur que vols continuar?")
                        .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                            startActivity(new Intent(BackgroundLocationPerm.this, NavActivity.class));
                            finish();
                        })
                        .setNegativeButton(R.string.cancel, ((dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            demanarPermisos();
                        }))
                        .show();
            }
        }
    }

    private void askPermissionsIfNecessary() {
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean permissionsGranted = true;
            int i = 0;
            while (permissionsGranted && i < permissions.length) {
                if (ContextCompat.checkSelfPermission(this, permissions[i])
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    permissionsGranted = false;
                }
                i++;
            }

            if (permissionsGranted) {
//                Funcions.runGeoLocWorker(getApplicationContext());
                startActivity(new Intent(BackgroundLocationPerm.this, NavActivity.class));
                finish();
            } else {
                new AlertDialog.Builder(getApplicationContext())
                        .setTitle(R.string.dialog_title_perm_loc)
                        .setMessage(R.string.dialog_body_perm_loc)
                        .setPositiveButton(R.string.accept, (dialogInterface, x) -> {
                            startActivity(new Intent(BackgroundLocationPerm.this, NavActivity.class));
                            finish();
                        })
                        .setNegativeButton(R.string.cancel, ((dialogInterface, j) -> {
                            dialogInterface.dismiss();
                            demanarPermisos();
                        }))
                        .show();
            }
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    1);
        } else {
//            Funcions.runGeoLocWorker(getApplicationContext());
            startActivity(new Intent(BackgroundLocationPerm.this, NavActivity.class));
            finish();
        }
    }
}
