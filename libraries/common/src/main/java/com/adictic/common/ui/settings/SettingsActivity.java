package com.adictic.common.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String fragmentType = getIntent().getStringExtra("fragment");
        Objects.requireNonNull(getSupportActionBar()).setTitle(getIntent().getStringExtra("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(fragmentType!=null) {
            try {
                Class<? extends PreferenceFragmentCompat> c = (Class<? extends PreferenceFragmentCompat>) Class.forName(fragmentType);
                PreferenceFragmentCompat fragmentCompat = c.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .add(android.R.id.content, fragmentCompat)
                        .commit();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
                finish();
            }
        } else finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
