package com.adictic.client.ui.setting;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String fragmentType = getIntent().getStringExtra("fragment");
        Objects.requireNonNull(getSupportActionBar()).setTitle(getIntent().getStringExtra("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(fragmentType==null) finish();
        switch(fragmentType){
            case "security":
                getSupportFragmentManager().beginTransaction()
                        .add(android.R.id.content, new SecuritySettings())
                        .commit();
                break;
            default:
                Log.e(TAG, "Unknown fragment type: "+fragmentType);
                finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
