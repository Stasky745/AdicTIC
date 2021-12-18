package com.adictic.common.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationSettings extends AppCompatActivity {
    private HashMap<String, String> initializeStringMapTutor() {
        HashMap<String, String> map = new HashMap<>();

        map.put(getString(R.string.notif_install_apps), Constants.NOTIF_SETTINGS_INSTALL_APPS);
        map.put(getString(R.string.notif_uninstall_apps), Constants.NOTIF_SETTINGS_UNINSTALL_APPS);
        map.put(getString(R.string.notif_accessibility_error), Constants.NOTIF_SETTINGS_ACCESSIBILITY_ERROR);
        map.put(getString(R.string.notif_admin_error), Constants.NOTIF_SETTINGS_DISABLE_ADMIN);
        map.put(getString(R.string.notif_chat), Constants.NOTIF_SETTINGS_CHAT);

        return map;
    }

    private HashMap<String, String> initializeStringMapChild() {
        HashMap<String, String> map = new HashMap<>();

        map.put(getString(R.string.notif_daily_limit), Constants.NOTIF_SETTINGS_DAILY_LIMIT);
        map.put(getString(R.string.notif_block_device), Constants.NOTIF_SETTINGS_BLOCK_DEVICE);
        map.put(getString(R.string.notif_freeuse), Constants.NOTIF_SETTINGS_FREE_USE);
        map.put(getString(R.string.notif_block_app), Constants.NOTIF_SETTINGS_BLOCK_APP);
        map.put(getString(R.string.notif_horaris), Constants.NOTIF_SETTINGS_HORARIS);
        map.put(getString(R.string.notif_events), Constants.NOTIF_SETTINGS_EVENTS);
        map.put(getString(R.string.notif_chat), Constants.NOTIF_SETTINGS_CHAT);

        return map;
    }

    private HashMap<String, String> initializeStringMapAdmin() {
        HashMap<String, String> map = new HashMap<>();

        map.put(getString(R.string.notif_chat), Constants.NOTIF_SETTINGS_CHAT);

        return map;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_settings);

        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(this);
        assert sharedPreferences != null;

        boolean admin = sharedPreferences.getLong(Constants.SHARED_PREFS_ID_ADMIN, -1L) > 0;
        boolean tutor = !admin && sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false);
        RecyclerView RV_notifList = findViewById(R.id.RV_notif_settings);
        RV_notifList.setLayoutManager(new LinearLayoutManager(this));
        Notif_Adapter notif_adapter;

        setAndroidButton();

        // Notificacions d'admin
        if(admin)
            notif_adapter = new Notif_Adapter(NotificationSettings.this, initializeStringMapAdmin());

        // Notificacions tutor
        else if(tutor)
            notif_adapter = new Notif_Adapter(NotificationSettings.this, initializeStringMapTutor());

        // Notificacions fill
        else
            notif_adapter = new Notif_Adapter(NotificationSettings.this, initializeStringMapChild());

        RV_notifList.setAdapter(notif_adapter);
    }

    private void setAndroidButton() {
        Button BT_notif_android = findViewById(R.id.BT_android_notif_settings);
        BT_notif_android.setOnClickListener(v -> {
            ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();

            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

            //for Android 5-7
            intent.putExtra("app_package", appInfo.packageName);
            intent.putExtra("app_uid", appInfo.uid);

            // for Android 8 and above
            intent.putExtra("android.provider.extra.APP_PACKAGE", appInfo.packageName);

            startActivity(intent);
        });
    }

    public class Notif_Adapter extends RecyclerView.Adapter<Notif_Adapter.NotifViewHolder> {
        private final Context mContext;
        private final HashMap<String, String> notifMap;
        private final LayoutInflater mInflater;
        private final List<String> stringList;
        private final SharedPreferences sharedPreferences;

        private final HashMap<String, String> infoMap = initializeInfoMap();

        private HashMap<String, String> initializeInfoMap() {
            HashMap<String, String> map = new HashMap<>();

            map.put(getString(R.string.notif_accessibility_error), getString(R.string.notif_accessibility_error_info));
            map.put(getString(R.string.notif_admin_error), getString(R.string.notif_admin_error_info));

            return map;
        }

        Notif_Adapter(Context ctx, HashMap<String, String> list) {
            mContext = ctx;
            notifMap = list;
            mInflater = LayoutInflater.from(mContext);
            stringList = new ArrayList<>(list.keySet());
            sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx);
            assert sharedPreferences != null;
        }

        @NonNull
        @Override
        public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = mInflater.inflate(R.layout.notification_settings_item, parent, false);

            return new NotifViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
            String notifText = stringList.get(position);
            String constantValue = notifMap.get(notifText);
            holder.TV_notif_text.setText(notifText);

            boolean actiu = sharedPreferences.getBoolean(constantValue, true);

            holder.SW_notif_enabled.setChecked(actiu);
            holder.SW_notif_enabled.setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferences.edit().putBoolean(constantValue, isChecked).apply());

            if(infoMap.containsKey(notifText)) {
                ImageView IV_notif_info = holder.IV_notif_info;
                IV_notif_info.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                            .setTitle(notifText)
                            .setNeutralButton(mContext.getString(R.string.accept), (dialog, which) -> dialog.dismiss());


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        TextView messageView = new TextView(mContext);
                        messageView.setText(infoMap.get(notifText));
                        messageView.setPadding(62, 20, 62, 20);
                        messageView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

                        builder.setView(messageView);
                    }
                    else
                        builder.setMessage(infoMap.get(notifText));

                    builder.show();
                });
            }
            else
                holder.IV_notif_info.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return notifMap.size();
        }

        public class NotifViewHolder extends RecyclerView.ViewHolder {
            TextView TV_notif_text;
            SwitchMaterial SW_notif_enabled;
            ImageView IV_notif_info;

            public NotifViewHolder(@NonNull View itemView) {
                super(itemView);

                TV_notif_text = itemView.findViewById(R.id.TV_notif_text);
                SW_notif_enabled = itemView.findViewById(R.id.SW_notif);
                IV_notif_info = itemView.findViewById(R.id.IV_notif_info);
            }
        }
    }
}
