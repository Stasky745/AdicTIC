package com.adictic.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.adictic.admin.ui.profile.ISubmitWeblink;
import com.adictic.admin.util.AdminApp;
import com.adictic.admin.util.Funcions;
import com.adictic.common.entity.AdminProfile;
import com.adictic.common.entity.WebLink;
import com.adictic.common.util.Constants;
import com.adictic.admin.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements ISubmitWeblink {

    public AdminProfile yourAdminProfile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_home, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        openPatchNotes();
    }

    private void openPatchNotes() {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(MainActivity.this);

        assert sharedPreferences != null;
        if(!BuildConfig.VERSION_NAME.equals(sharedPreferences.getString(Constants.SHARED_PREFS_PATCH_NOTES,""))){
            sharedPreferences.edit().putString(Constants.SHARED_PREFS_PATCH_NOTES, BuildConfig.VERSION_NAME).apply();
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.patch_notes, null);
            setTexts(dialogView);
            builder.setView(dialogView)
                    .create()
                    .show();
        }
    }

    private void setTexts(View dialogView) {
        if(AdminApp.newFeatures.length == 0){
            dialogView.findViewById(R.id.TV_newFeatures).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_newFeaturesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + AdminApp.newFeatures[0];
            for(int i = 1; i < AdminApp.newFeatures.length; i++){
                string += "\n· " + AdminApp.newFeatures[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_newFeaturesList)).setText(string);
        }

        if(AdminApp.fixes.length == 0){
            dialogView.findViewById(R.id.TV_fixes).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_fixesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + AdminApp.fixes[0];
            for(int i = 1; i < AdminApp.fixes.length; i++){
                string += "\n· " + AdminApp.fixes[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_fixesList)).setText(string);
        }

        if(AdminApp.changes.length == 0){
            dialogView.findViewById(R.id.TV_changes).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_changesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + AdminApp.changes[0];
            for(int i = 1; i < AdminApp.changes.length; i++){
                string += "\n· " + AdminApp.changes[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_changesList)).setText(string);
        }
    }

    public void onSelectedData(WebLink webLink) {

    }
}