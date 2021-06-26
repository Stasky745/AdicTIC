package com.example.adictic.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.adictic.common.util.Constants;
import com.example.adictic.BuildConfig;
import com.example.adictic.R;
import com.example.adictic.entity.FillNom;
import com.example.adictic.entity.LiveApp;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class NavActivity extends AppCompatActivity {

    public final Integer tempsPerActu = 5 * 60 * 1000; // 5 min
    public final Integer tempsPerActuLiveApp = 60 * 1000; // 1 minut

    public TreeMap<Long, Long> mainParent_lastAppUsedUpdate = new TreeMap<>();
    public TreeMap<Long, LiveApp> mainParent_lastAppUsed = new TreeMap<>();
    public TreeMap<Long, Long> mainParent_lastUsageChartUpdate = new TreeMap<>();
    public TreeMap<Long, Map<String, Long>> mainParent_usageChart = new TreeMap<>();
    public TreeMap<Long, Long> mainParent_totalUsageTime = new TreeMap<>();

    public ArrayList<FillNom> homeParent_childs = null;
    public Long homeParent_lastChildsUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
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
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(NavActivity.this);

        assert sharedPreferences != null;
        if(!BuildConfig.VERSION_NAME.equals(sharedPreferences.getString(Constants.SHARED_PREFS_PATCH_NOTES,""))){
            sharedPreferences.edit().putString(Constants.SHARED_PREFS_PATCH_NOTES, BuildConfig.VERSION_NAME).apply();
            AlertDialog.Builder builder = new AlertDialog.Builder(NavActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.patch_notes, null);
            setTexts(dialogView);
            builder.setView(dialogView)
                    .create()
                    .show();
        }
    }

    private void setTexts(View dialogView) {
        if(TodoApp.newFeatures.length == 0){
            dialogView.findViewById(R.id.TV_newFeatures).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_newFeaturesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + TodoApp.newFeatures[0];
            for(int i = 1; i < TodoApp.newFeatures.length; i++){
                string += "\n· " + TodoApp.newFeatures[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_newFeaturesList)).setText(string);
        }

        if(TodoApp.fixes.length == 0){
            dialogView.findViewById(R.id.TV_fixes).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_fixesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + TodoApp.fixes[0];
            for(int i = 1; i < TodoApp.fixes.length; i++){
                string += "\n· " + TodoApp.fixes[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_fixesList)).setText(string);
        }

        if(TodoApp.changes.length == 0){
            dialogView.findViewById(R.id.TV_changes).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_changesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + TodoApp.changes[0];
            for(int i = 1; i < TodoApp.changes.length; i++){
                string += "\n· " + TodoApp.changes[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_changesList)).setText(string);
        }
    }
}
