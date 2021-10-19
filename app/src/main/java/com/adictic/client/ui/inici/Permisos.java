package com.adictic.client.ui.inici;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.adictic.client.R;
import com.adictic.client.util.Funcions;
import com.adictic.common.util.Constants;
import com.judemanutd.autostarter.AutoStartPermissionHelper;

import org.joda.time.DateTime;

import java.util.Objects;

public class Permisos extends AppCompatActivity {
    private boolean accessibilityPerm, usagePerm, adminPerm, overlayPerm, locationPerm, batteryPerm, autostartPerm;
    private final String TAG = "Permisos";

    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK) {
                SwitchCompat SW_permiso_admin_status = findViewById(R.id.SW_permiso_admin);
                adminPerm = Funcions.isAdminPermissionsOn(Permisos.this);
                SW_permiso_admin_status.setChecked(adminPerm);
            }
        }
    );

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permissions_layout);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.permisos));

        TextView TV_permisosDesc = findViewById(R.id.TV_permisos_desc);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            TV_permisosDesc.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        Button BT_finish = findViewById(R.id.BT_permisos_continuar);
        BT_finish.setOnClickListener(view -> {
            if(totsPermisosActivats())
                acabarActivitat();
            else{
                new androidx.appcompat.app.AlertDialog.Builder(Permisos.this)
                        .setTitle(getString(R.string.falten_perm_titol))
                        .setMessage(getString(R.string.falten_perm_desc))
                        .setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> acabarActivitat())
                        .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }
        });
    }

    private void acabarActivitat(){
        if(usagePerm) {
            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(Permisos.this);
            assert  sharedPreferences != null;

            long sixDaysAgo = DateTime.now().minusDays(6).getMillis();

            sharedPreferences.edit().putLong(Constants.SHARED_PREFS_LAST_DAY_SENT_DATA, sixDaysAgo).apply();

            Funcions.startAppUsageWorker24h(Permisos.this);
        }

        if(Funcions.isMIUI())
            Permisos.this.startActivity(new Intent(Permisos.this, PermisosMIUI.class));
        else
            Permisos.this.startActivity(new Intent(Permisos.this, AppLock.class));
        Permisos.this.finish();
    }
    
    private boolean totsPermisosActivats(){
        return accessibilityPerm && usagePerm && adminPerm && overlayPerm && locationPerm && batteryPerm; //&& autostartPerm;
    }

    @Override
    protected void onResume() {
        super.onResume();

        setAccessibilityLayout();
        setUsageLayout();
        setAdminLayout();
        setOverlayLayout();
        setLocationLayout();
        setBatteryLayout();
        setAutoStartLayout();
    }

    private void setAutoStartLayout() {
        ConstraintLayout CL_auto_start = findViewById(R.id.CL_auto_start);
        boolean autoStartAvailable = AutoStartPermissionHelper.Companion.getInstance().isAutoStartPermissionAvailable(this, true);

        if (!autoStartAvailable) {
            CL_auto_start.setVisibility(View.GONE);
            autostartPerm = true;
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Permisos.this)
                    .setTitle(getString(R.string.auto_start))
                    .setMessage(getString(R.string.auto_start_info))
                    .setPositiveButton(getString(R.string.configurar), (dialogInterface, i) -> autostartPerm = AutoStartPermissionHelper.Companion.getInstance().getAutoStartPermission(this,true, false))
                    .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());

            CL_auto_start.setOnClickListener(view -> alertDialog.show());
        }
    }

    private void setBatteryLayout() {
        ConstraintLayout CL_battery_optimisation = findViewById(R.id.CL_battery_optimisation);
        SwitchCompat SW_permiso_battery_optimisation_status = findViewById(R.id.SW_permiso_battery_optimisation);
        PowerManager mPm = (PowerManager) getSystemService(POWER_SERVICE);

        batteryPerm = mPm.isIgnoringBatteryOptimizations(getPackageName());
        SW_permiso_battery_optimisation_status.setChecked(batteryPerm);

        @SuppressLint("BatteryLife") AlertDialog.Builder alertDialog = new AlertDialog.Builder(Permisos.this)
                .setTitle(getString(R.string.battery_optimisation))
                .setMessage(getString(R.string.battery_optimisation_info))
                .setPositiveButton(getString(R.string.configurar), (dialogInterface, i) -> {
                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                    dialogInterface.dismiss();
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    SW_permiso_battery_optimisation_status.setChecked(batteryPerm);
                    dialogInterface.dismiss();
                });

        CL_battery_optimisation.setOnClickListener(view -> alertDialog.show());
        SW_permiso_battery_optimisation_status.setOnClickListener(view -> alertDialog.show());
    }

    private void setLocationLayout() {
        ConstraintLayout CL_location = findViewById(R.id.CL_location);
        SwitchCompat SW_permiso_location_status = findViewById(R.id.SW_permiso_location);

        locationPerm = Funcions.isBackgroundLocationPermissionOn(Permisos.this);
        SW_permiso_location_status.setChecked(locationPerm);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Permisos.this)
                .setTitle(getString(R.string.perm_background_loc_title))
                .setMessage(getString(R.string.perm_back_location_desc))
                .setPositiveButton(getString(R.string.configurar), (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    SW_permiso_location_status.setChecked(locationPerm);
                    dialogInterface.dismiss();
                });

        CL_location.setOnClickListener(view -> alertDialog.show());
        SW_permiso_location_status.setOnClickListener(view -> alertDialog.show());
    }

    private void setOverlayLayout() {
        ConstraintLayout CL_overlay = findViewById(R.id.CL_overlay);
        SwitchCompat SW_permiso_overlay_status = findViewById(R.id.SW_permiso_overlay);

        overlayPerm = Settings.canDrawOverlays(this);
        SW_permiso_overlay_status.setChecked(overlayPerm);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Permisos.this)
                .setTitle(getString(R.string.overlay_title))
                .setMessage(getString(R.string.overlay_info))
                .setPositiveButton(getString(R.string.configurar), (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    SW_permiso_overlay_status.setChecked(overlayPerm);
                    dialogInterface.dismiss();
                });

        CL_overlay.setOnClickListener(view -> alertDialog.show());
        SW_permiso_overlay_status.setOnClickListener(view -> alertDialog.show());
    }

    private void setAdminLayout() {
        ConstraintLayout CL_admin = findViewById(R.id.CL_admin);
        SwitchCompat SW_permiso_admin_status = findViewById(R.id.SW_permiso_admin);

        adminPerm = Funcions.isAdminPermissionsOn(this);
        SW_permiso_admin_status.setChecked(adminPerm);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Permisos.this)
                .setTitle(getString(R.string.admin_pem_title))
                .setMessage(getString(R.string.admin_pem_info))
                .setPositiveButton(getString(R.string.configurar), (dialogInterface, i) -> {
                    ComponentName mDPAdmin = new ComponentName(Permisos.this,
                            MyDevicePolicyReceiver.class);
                    Intent intent = new Intent(
                            DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(
                            DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                            mDPAdmin);
                    intent.putExtra(
                            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            getString(R.string.admin_pem_intent));
                    activityResult.launch(intent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    SW_permiso_admin_status.setChecked(adminPerm);
                    dialogInterface.dismiss();
                });

        CL_admin.setOnClickListener(view -> alertDialog.show());
        SW_permiso_admin_status.setOnClickListener(view -> alertDialog.show());
    }

    private void setUsageLayout() {
        ConstraintLayout CL_usage = findViewById(R.id.CL_usage);
        SwitchCompat SW_permiso_usage_status = findViewById(R.id.SW_permiso_usage);

        usagePerm = Funcions.isAppUsagePermissionOn(this);
        SW_permiso_usage_status.setChecked(usagePerm);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Permisos.this)
                .setTitle(getString(R.string.appusage_pem_title))
                .setMessage(getString(R.string.appusage_pem_info))
                .setPositiveButton(getString(R.string.configurar), (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    SW_permiso_usage_status.setChecked(usagePerm);
                    dialogInterface.dismiss();
                });

        CL_usage.setOnClickListener(view -> alertDialog.show());
        SW_permiso_usage_status.setOnClickListener(view -> alertDialog.show());
    }

    private void setAccessibilityLayout() {
        ConstraintLayout CL_accessibility = findViewById(R.id.CL_accessibility);
        SwitchCompat SW_accessibility_status = findViewById(R.id.SW_permiso_accessibility);

        accessibilityPerm = Funcions.isAccessibilitySettingsOn(this);
        SW_accessibility_status.setChecked(accessibilityPerm);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Permisos.this)
                .setTitle(getString(R.string.accessibility_pem_title))
                .setMessage(getString(R.string.accessibility_pem_info))
                .setPositiveButton(getString(R.string.configurar), (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    SW_accessibility_status.setChecked(accessibilityPerm);
                    dialogInterface.dismiss();
                });

        CL_accessibility.setOnClickListener(view -> alertDialog.show());
        SW_accessibility_status.setOnClickListener(view -> alertDialog.show());
    }

    public static class MyDevicePolicyReceiver extends DeviceAdminReceiver {
        @Override
        public void onDisabled(Context context, Intent intent) {
            Toast.makeText(context, "AdicTIC's Device Admin Disabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnabled(Context context, Intent intent) {
            Toast.makeText(context, "AdicTIC's Device Admin is now enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return "Requesting to disable Device Admin";
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("MyDevicePolicyReciever",
                    "Received: " + intent.getAction());
            super.onReceive(context, intent);
        }


    }
}
