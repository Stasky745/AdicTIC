package com.adictic.client.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.adictic.client.BuildConfig;
import com.adictic.client.R;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.hilt.AdicticRepository;
import com.adictic.common.ui.main.MainActivityAbstractClass;
import com.adictic.common.util.Constants;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NavActivity extends MainActivityAbstractClass {

    @Inject
    AdicticRepository repository;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        sharedPreferences = repository.getEncryptedSharedPreferences();
        assert sharedPreferences != null;

        if(!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false))
            repository.startServiceWorker();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_advice, R.id.nav_mainParent, R.id.nav_settings)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        openPatchNotes();
    }

    private void openPatchNotes() {
        assert sharedPreferences != null;
        if(!BuildConfig.VERSION_NAME.equals(sharedPreferences.getString(Constants.SHARED_PREFS_PATCH_NOTES,""))){
            sharedPreferences.edit().putString(Constants.SHARED_PREFS_PATCH_NOTES, BuildConfig.VERSION_NAME).apply();
            if(hiHaPatchNotes()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(NavActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.patch_notes, null);
                setTexts(dialogView);
                builder.setView(dialogView)
                        .create()
                        .show();
            }
        }
    }

    private boolean hiHaPatchNotes() {
        return AdicticApp.newFeatures.length > 0 && AdicticApp.fixes.length > 0 && AdicticApp.changes.length > 0;
    }

    private void setTexts(View dialogView) {
        if(AdicticApp.newFeatures.length == 0){
            dialogView.findViewById(R.id.TV_newFeatures).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_newFeaturesList).setVisibility(View.GONE);
        }
        else{
            StringBuilder string = new StringBuilder("· " + AdicticApp.newFeatures[0]);
            for(int i = 1; i < AdicticApp.newFeatures.length; i++){
                string.append("\n· ").append(AdicticApp.newFeatures[i]);
            }
            ((TextView) dialogView.findViewById(R.id.TV_newFeaturesList)).setText(string.toString());
        }

        if(AdicticApp.fixes.length == 0){
            dialogView.findViewById(R.id.TV_fixes).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_fixesList).setVisibility(View.GONE);
        }
        else{
            StringBuilder string = new StringBuilder("· " + AdicticApp.fixes[0]);
            for(int i = 1; i < AdicticApp.fixes.length; i++){
                string.append("\n· ").append(AdicticApp.fixes[i]);
            }
            ((TextView) dialogView.findViewById(R.id.TV_fixesList)).setText(string.toString());
        }

        if(AdicticApp.changes.length == 0){
            dialogView.findViewById(R.id.TV_changes).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_changesList).setVisibility(View.GONE);
        }
        else{
            StringBuilder string = new StringBuilder("· " + AdicticApp.changes[0]);
            for(int i = 1; i < AdicticApp.changes.length; i++){
                string.append("\n· ").append(AdicticApp.changes[i]);
            }
            ((TextView) dialogView.findViewById(R.id.TV_changesList)).setText(string.toString());
        }
    }
}
