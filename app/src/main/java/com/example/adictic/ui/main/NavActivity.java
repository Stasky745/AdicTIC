package com.example.adictic.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.adictic.R;
import com.example.adictic.entity.FillNom;
import com.example.adictic.entity.LiveApp;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class NavActivity extends AppCompatActivity {

    public final Integer tempsPerActu = 5 * 60 * 1000;

    public TreeMap<Long, Long> mainParent_lastAppUsedUpdate = new TreeMap<>();
    public TreeMap<Long, LiveApp> mainParent_lastAppUsed = new TreeMap<>();;
    public TreeMap<Long, Long> mainParent_lastUsageChartUpdate = new TreeMap<>();;
    public TreeMap<Long, Map<String, Long>> mainParent_usageChart = new TreeMap<>();;
    public TreeMap<Long, Long> mainParent_totalUsageTime = new TreeMap<>();;

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
    }
}
