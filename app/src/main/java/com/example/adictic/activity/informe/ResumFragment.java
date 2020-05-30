package com.example.adictic.activity.informe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.adictic.R;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResumFragment extends Fragment {

    private long HORES_A_MILLIS = 60*60*1000;

    private List<GeneralUsage> appList;

    private TextView TV_intro, TV_appUsageInfo, TV_clickedBlockedApps, TV_resum;
    private Button BT_appsUsage, BT_clickedBlockedApps, BT_resum;
    private ConstraintLayout CL_appsUsage, CL_clickedBlockedApps, CL_resum;

    private long totalPossibleTime, totalUsageTime;

    private Map<String,Long> tempsApps;
    private Map<Integer,Long> tempsCategoria;

    private long[] ageTimes = new long[30];
    {
        Arrays.fill(ageTimes,2*HORES_A_MILLIS);
        for(int i = 0; i < 2; i++) ageTimes[i] = 0;
        for(int i = 2; i < 12; i++) ageTimes[i] = HORES_A_MILLIS;
        for(int i = 12; i < 15; i++) ageTimes[i] = Math.round(1.5*HORES_A_MILLIS);
    }

    private int age;

    ResumFragment(Collection<GeneralUsage> col, long totalTime, long totalUsageT, int a){
        appList = new ArrayList<>(col);
        totalPossibleTime = totalTime;
        totalUsageTime = totalUsageT;
        age = a;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.informe_layout,viewGroup,false);

        setViews(root);
        setButtons();
        setMaps();
        setIntro();

        return root;
    }

    private void setMaps(){
        tempsApps = new HashMap<>();
        tempsCategoria = new HashMap<>();

        for(GeneralUsage gu : appList){
            List<AppUsage> appUsageList = new ArrayList<>(gu.usage);
            for(AppUsage au : appUsageList){
                if(tempsApps.get(au.app.pkgName) == null) tempsApps.put(au.app.pkgName,au.totalTime);
                else tempsApps.put(au.app.pkgName,tempsApps.get(au.app.pkgName)+au.totalTime);

                if(tempsCategoria.get(au.app.category) == null) tempsCategoria.put(au.app.category,au.totalTime);
                else tempsCategoria.put(au.app.category,tempsCategoria.get(au.app.category)+au.totalTime);
            }
        }

        for(Map.Entry<String,Long> entry : tempsApps.entrySet()){
            long time = entry.getValue() / appList.size();
            if(time < ageTimes[age]) tempsApps.remove(entry.getKey());
            else tempsApps.put(entry.getKey(),time);
        }

        for(Map.Entry<Integer,Long> entry : tempsCategoria.entrySet()){
            long time = entry.getValue() / appList.size();
            if(time < ageTimes[age]) tempsCategoria.remove(entry.getKey());
            else tempsCategoria.put(entry.getKey(),time);
        }
    }

    private void setIntro(){

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
    }

    private void setViews(View root){
        TV_intro = (TextView) root.findViewById(R.id.TV_intro);
        TV_appUsageInfo = (TextView) root.findViewById(R.id.TV_appUsageInfo);
        TV_clickedBlockedApps = (TextView) root.findViewById(R.id.TV_clickedBlockedAppsInfo);
        TV_resum = (TextView) root.findViewById(R.id.TV_resum);

        BT_appsUsage = (Button) root.findViewById(R.id.BT_appsUsage);
        BT_clickedBlockedApps = (Button) root.findViewById(R.id.BT_clickedBlockedApps);
        BT_resum = (Button) root.findViewById(R.id.BT_resum);

        CL_appsUsage = (ConstraintLayout) root.findViewById(R.id.CL_appsUsage);
        CL_clickedBlockedApps = (ConstraintLayout) root.findViewById(R.id.CL_clickedBlockedApps);
        CL_resum = (ConstraintLayout) root.findViewById(R.id.CL_resum);
    }
}
