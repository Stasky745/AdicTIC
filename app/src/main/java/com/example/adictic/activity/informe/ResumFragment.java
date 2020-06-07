package com.example.adictic.activity.informe;

import android.os.Build;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic.R;
import com.example.adictic.adapters.SimpleAdapter;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.fragment.AdviceFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

public class ResumFragment extends Fragment {

    private long HORES_A_MILLIS = 60*60*1000;

    private List<GeneralUsage> appList;

    private TextView TV_intro, TV_appUsageInfo, TV_clickedBlockedApps, TV_resum;
    private Button BT_appsUsage, BT_clickedBlockedApps, BT_resum, BT_informacio;
    private ConstraintLayout CL_appsUsage, CL_clickedBlockedApps, CL_resum;

    private RecyclerView RV_abusedApps, RV_clickedBlockedApps;

    private long totalPossibleTime, totalUsageTime;

    private double mitjanaHoresDia;

    private Map<String,Long> tempsApps = new HashMap<>();
    private Map<String,Long> intentsAcces;

    private long[] ageTimesMillis = new long[30];
    {
        Arrays.fill(ageTimesMillis,2*HORES_A_MILLIS);
        for(int i = 0; i < 2; i++) ageTimesMillis[i] = 0;
        for(int i = 2; i < 12; i++) ageTimesMillis[i] = HORES_A_MILLIS;
        for(int i = 12; i < 15; i++) ageTimesMillis[i] = round(1.5*HORES_A_MILLIS);
    }

    private double[] ageTimes = new double[30];
    {
        Arrays.fill(ageTimes,2);
        for(int i = 0; i < 2; i++) ageTimes[i] = 0;
        for(int i = 2; i < 12; i++) ageTimes[i] = 1;
        for(int i = 12; i < 15; i++) ageTimes[i] = 1.5;
    }

    private int age;

    ResumFragment(Collection<GeneralUsage> col, long totalTime, long totalUsageT, int a, Map<String,Long> map){
        appList = new ArrayList<>(col);
        totalPossibleTime = totalTime;
        totalUsageTime = totalUsageT;
        mitjanaHoresDia = round(10.0*totalUsageT / (appList.size()*HORES_A_MILLIS))/10.0;

        /** Assegurem que l'edat no surt de rang **/
        age = Math.min(Math.abs(a),20);

        intentsAcces = map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.informe_resum,viewGroup,false);

        setViews(root);
        setMaps();
        setIntro();
        setUseApps(root);
        setAccessTries(root);
        setResum();

        return root;
    }

    private void setResum(){
        if(tempsApps.isEmpty() && intentsAcces.isEmpty() && mitjanaHoresDia <= ageTimes[age]) TV_resum.setText(getString(R.string.resum_bo));
        else{
            String resum_final = getString(R.string.resum);
            if(mitjanaHoresDia > ageTimes[age]) resum_final+=getString(R.string.resum_us_device);
            if (!tempsApps.isEmpty()) resum_final+=getString(R.string.resum_us_apps);
            if(!intentsAcces.isEmpty()) resum_final+=getString(R.string.resum_intents_acces);
            TV_resum.setText(resum_final);
        }
    }

    private void setAccessTries(View root){
        if(intentsAcces.isEmpty()){
            TextView TV_label = (TextView) root.findViewById(R.id.TV_clickedBlockedAppsLabel);
            TV_label.setVisibility(View.GONE);
            RV_clickedBlockedApps.setVisibility(View.GONE);
            TV_clickedBlockedApps.setText(getString(R.string.intents_acces_buit));
        }
        else{
            TV_clickedBlockedApps.setText(getString(R.string.intents_acces));
            setRecyclerView(RV_clickedBlockedApps,intentsAcces,0);
        }
    }

    private void setUseApps(View root){
        if(tempsApps.isEmpty()) {
            TV_appUsageInfo.setText(getString(R.string.us_apps_buit));
            TextView TV_appUsageLabel = (TextView) root.findViewById(R.id.TV_overuseAppsLabel);
            TV_appUsageLabel.setVisibility(View.GONE);
            RV_abusedApps.setVisibility(View.GONE);
        }
        else{
            TV_appUsageInfo.setText(getString(R.string.us_apps));
            setRecyclerView(RV_abusedApps,tempsApps,1);
        }
    }

    private void setRecyclerView(RecyclerView rv, Map<String,Long> map, int t){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);

        rv.setHasFixedSize(true);

        RecyclerView.Adapter mAdapter = new SimpleAdapter(map,t,getActivity());
        rv.setAdapter(mAdapter);
    }

    private void setMaps(){
        for(GeneralUsage gu : appList){
            List<AppUsage> appUsageList = new ArrayList<>(gu.usage);
            for(AppUsage au : appUsageList){
                if(tempsApps.get(au.app.appName) == null) tempsApps.put(au.app.appName,au.totalTime);
                else tempsApps.put(au.app.appName,tempsApps.get(au.app.appName)+au.totalTime);
            }
        }

        Map<String,Long> auxTempsApps = new HashMap<>(tempsApps);
        for(Map.Entry<String,Long> entry : auxTempsApps.entrySet()){
            long time = entry.getValue() / appList.size();
            if(time < ageTimesMillis[age]) tempsApps.remove(entry.getKey());
            else tempsApps.put(entry.getKey(),time);
        }

        Map<String,Long> auxIntentsAcces = new HashMap<>(intentsAcces);
        for(Map.Entry<String,Long> entry : auxIntentsAcces.entrySet()){
            intentsAcces.put(entry.getKey(),entry.getValue()/appList.size());
        }
    }

    private void setIntro(){
        String tempsRecomanat;
        if(ageTimes[age] != 1.5) tempsRecomanat = String.valueOf(Math.round(ageTimes[age]));
        else tempsRecomanat = String.valueOf(ageTimes[age]);

        String mitj;
        if(10*mitjanaHoresDia % 10 == 0) mitj = String.valueOf(Math.round(mitjanaHoresDia));
        else mitj = String.valueOf(mitjanaHoresDia);

        if(mitjanaHoresDia <= ageTimes[age]) TV_intro.setText(getString(R.string.intro_bona,age, tempsRecomanat,mitj));
        else TV_intro.setText(getString(R.string.intro_dolenta,age, tempsRecomanat,mitj));
    }

    private void setButtons(){
        BT_appsUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CL_appsUsage.getVisibility() == View.VISIBLE) CL_appsUsage.setVisibility(View.GONE);
                else CL_appsUsage.setVisibility(View.VISIBLE);
            }
        });

        BT_clickedBlockedApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CL_clickedBlockedApps.getVisibility() == View.VISIBLE) CL_clickedBlockedApps.setVisibility(View.GONE);
                else CL_clickedBlockedApps.setVisibility(View.VISIBLE);
            }
        });

        BT_resum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CL_resum.getVisibility() == View.VISIBLE) CL_resum.setVisibility(View.GONE);
                else CL_resum.setVisibility(View.VISIBLE);
            }
        });

        BT_informacio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdviceFragment adviceFragment = new AdviceFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(), adviceFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void setViews(View root){
        TV_intro = (TextView) root.findViewById(R.id.TV_intro);
        TV_appUsageInfo = (TextView) root.findViewById(R.id.TV_appUsageInfo);
        TV_clickedBlockedApps = (TextView) root.findViewById(R.id.TV_clickedBlockedAppsInfo);
        TV_resum = (TextView) root.findViewById(R.id.TV_resum);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            TV_intro.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            TV_appUsageInfo.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            TV_clickedBlockedApps.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
            TV_resum.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        BT_appsUsage = (Button) root.findViewById(R.id.BT_appsUsage);
        BT_clickedBlockedApps = (Button) root.findViewById(R.id.BT_clickedBlockedApps);
        BT_resum = (Button) root.findViewById(R.id.BT_resum);
        BT_informacio = (Button) root.findViewById(R.id.BT_informacio);

        setButtons();

        CL_appsUsage = (ConstraintLayout) root.findViewById(R.id.CL_appsUsage);
        CL_clickedBlockedApps = (ConstraintLayout) root.findViewById(R.id.CL_clickedBlockedApps);
        CL_resum = (ConstraintLayout) root.findViewById(R.id.CL_resum);

        RV_abusedApps = (RecyclerView) root.findViewById(R.id.RV_abusedApps);
        RV_clickedBlockedApps = (RecyclerView) root.findViewById(R.id.RV_clickedBlockedApps);

    }
}
