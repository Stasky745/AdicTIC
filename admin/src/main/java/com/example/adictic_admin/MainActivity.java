package com.example.adictic_admin;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.adictic_admin.entity.AdminProfile;
import com.example.adictic_admin.entity.WebLink;
import com.example.adictic_admin.ui.profile.ISubmitWeblink;
import com.example.adictic_admin.util.Constants;
import com.example.adictic_admin.util.Funcions;
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
        if(App.newFeatures.length == 0){
            dialogView.findViewById(R.id.TV_newFeatures).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_newFeaturesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + App.newFeatures[0];
            for(int i = 1; i < App.newFeatures.length; i++){
                string += "\n· " + App.newFeatures[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_newFeaturesList)).setText(string);
        }

        if(App.fixes.length == 0){
            dialogView.findViewById(R.id.TV_fixes).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_fixesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + App.fixes[0];
            for(int i = 1; i < App.fixes.length; i++){
                string += "\n· " + App.fixes[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_fixesList)).setText(string);
        }

        if(App.changes.length == 0){
            dialogView.findViewById(R.id.TV_changes).setVisibility(View.GONE);
            dialogView.findViewById(R.id.TV_changesList).setVisibility(View.GONE);
        }
        else{
            String string = "· " + App.changes[0];
            for(int i = 1; i < App.changes.length; i++){
                string += "\n· " + App.changes[i];
            }
            ((TextView) dialogView.findViewById(R.id.TV_changesList)).setText(string);
        }
    }

    @Override
    public void onSelectedData(WebLink webLink) {

    }
}