package com.example.adictic.ui.informe;

import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
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
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.ui.support.AdviceFragment;
import com.example.adictic.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

public class ResumFragment extends Fragment {

    private final long HORA_EN_MILLIS = 60 * 60 * 1000;

    private final List<GeneralUsage> appList;

    private TextView TV_intro, TV_appUsageInfo, TV_clickedBlockedApps, TV_resum;
    private Button BT_appsUsage, BT_clickedBlockedApps, BT_resum, BT_informacio;
    private ConstraintLayout CL_appsUsage, CL_clickedBlockedApps, CL_resum;

    private RecyclerView RV_abusedApps, RV_clickedBlockedApps;

    private final double mitjanaHoresDia;

    private final Map<String, Long> tempsApps = new HashMap<>();
    private final Map<String, Long> intentsAcces;

    private final int age;

    ResumFragment(Collection<GeneralUsage> col, long totalUsageT, int edat, Map<String, Long> map) {
        appList = new ArrayList<>(col);
        mitjanaHoresDia = round(10.0 * totalUsageT / (appList.size() * HORA_EN_MILLIS)) / 10.0;

//        /* Assegurem que l'edat no surt de rang **/
//        age = Math.min(Math.abs(edat), 29);
        age = edat;

        intentsAcces = map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.informe_resum, viewGroup, false);

        setViews(root);
        setMaps();
        setIntro();
        setUseApps(root);
        setAccessTries(root);
        setResum();

        return root;
    }

    private void setResum() {
        if (tempsApps.isEmpty() && intentsAcces.isEmpty() && mitjanaHoresDia <= Constants.AGE_TIMES[Math.min(age, 20)])
            TV_resum.setText(getString(R.string.resum_bo));
        else {
            String resum_final = getString(R.string.resum);
            if (mitjanaHoresDia > Constants.AGE_TIMES[Math.min(age, 20)])
                resum_final += getString(R.string.resum_us_device);
            if (!tempsApps.isEmpty()) resum_final += getString(R.string.resum_us_apps);
            if (!intentsAcces.isEmpty()) resum_final += getString(R.string.resum_intents_acces);
            TV_resum.setText(resum_final);
        }
    }

    private void setAccessTries(View root) {
        if (intentsAcces.isEmpty()) {
            TextView TV_label = root.findViewById(R.id.TV_clickedBlockedAppsLabel);
            TV_label.setVisibility(View.GONE);
            RV_clickedBlockedApps.setVisibility(View.GONE);
            TV_clickedBlockedApps.setText(getString(R.string.intents_acces_buit));
        } else {
            TV_clickedBlockedApps.setText(getString(R.string.intents_acces));
            setRecyclerView(RV_clickedBlockedApps, intentsAcces, 0);
        }
    }

    private void setUseApps(View root) {
        if (tempsApps.isEmpty()) {
            TV_appUsageInfo.setText(getString(R.string.us_apps_buit));
            TextView TV_appUsageLabel = root.findViewById(R.id.TV_overuseAppsLabel);
            TV_appUsageLabel.setVisibility(View.GONE);
            RV_abusedApps.setVisibility(View.GONE);
        } else {
            TV_appUsageInfo.setText(getString(R.string.us_apps));
            setRecyclerView(RV_abusedApps, tempsApps, 1);
        }
    }

    private void setRecyclerView(RecyclerView rv, Map<String, Long> map, int t) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);

        //rv.setHasFixedSize(true);

        RecyclerView.Adapter mAdapter = new SimpleAdapter(map, t, getActivity());
        rv.setAdapter(mAdapter);
    }

    private void setMaps() {
        for (GeneralUsage gu : appList) {
            List<AppUsage> appUsageList = new ArrayList<>(gu.usage);
            for (AppUsage au : appUsageList) {
                if (tempsApps.get(au.app.appName) == null)
                    tempsApps.put(au.app.appName, au.totalTime);
                else tempsApps.put(au.app.appName, tempsApps.get(au.app.appName) + au.totalTime);
            }
        }

        Map<String, Long> auxTempsApps = new HashMap<>(tempsApps);
        for (Map.Entry<String, Long> entry : auxTempsApps.entrySet()) {
            long time = entry.getValue() / appList.size();
            if (time < Constants.AGE_TIMES_MILLIS[age]) tempsApps.remove(entry.getKey());
            else tempsApps.put(entry.getKey(), time);
        }

        Map<String, Long> auxIntentsAcces = new HashMap<>(intentsAcces);
        for (Map.Entry<String, Long> entry : auxIntentsAcces.entrySet()) {
            intentsAcces.put(entry.getKey(), entry.getValue() / appList.size());
        }
    }

    private void setIntro() {
        String tempsRecomanat;
        if (Constants.AGE_TIMES[Math.min(age, 20)] != 1.5)
            tempsRecomanat = String.valueOf(Math.round(Constants.AGE_TIMES[Math.min(age, 20)]));
        else tempsRecomanat = String.valueOf(Constants.AGE_TIMES[Math.min(age, 20)]);

        String mitj;
        if (10 * mitjanaHoresDia % 10 == 0) mitj = String.valueOf(Math.round(mitjanaHoresDia));
        else mitj = String.valueOf(mitjanaHoresDia);

        if (mitjanaHoresDia <= Constants.AGE_TIMES[Math.min(age, 20)])
            TV_intro.setText(getString(R.string.intro_bona, age, tempsRecomanat, mitj));
        else TV_intro.setText(getString(R.string.intro_dolenta, age, tempsRecomanat, mitj));
    }

    private void setButtons() {
        BT_appsUsage.setOnClickListener(v -> {
            if (CL_appsUsage.getVisibility() == View.VISIBLE)
                CL_appsUsage.setVisibility(View.GONE);
            else CL_appsUsage.setVisibility(View.VISIBLE);
        });

        BT_clickedBlockedApps.setOnClickListener(v -> {
            if (CL_clickedBlockedApps.getVisibility() == View.VISIBLE)
                CL_clickedBlockedApps.setVisibility(View.GONE);
            else CL_clickedBlockedApps.setVisibility(View.VISIBLE);
        });

        BT_resum.setOnClickListener(v -> {
            if (CL_resum.getVisibility() == View.VISIBLE) CL_resum.setVisibility(View.GONE);
            else CL_resum.setVisibility(View.VISIBLE);
        });

        BT_informacio.setOnClickListener(v -> {
            AdviceFragment adviceFragment = new AdviceFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(((ViewGroup) getView().getParent()).getId(), adviceFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setViews(View root) {
        TV_intro = root.findViewById(R.id.TV_intro);
        TV_appUsageInfo = root.findViewById(R.id.TV_appUsageInfo);
        TV_clickedBlockedApps = root.findViewById(R.id.TV_clickedBlockedAppsInfo);
        TV_resum = root.findViewById(R.id.TV_resum);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TV_intro.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            TV_appUsageInfo.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            TV_clickedBlockedApps.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            TV_resum.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        BT_appsUsage = root.findViewById(R.id.BT_appsUsage);
        BT_clickedBlockedApps = root.findViewById(R.id.BT_clickedBlockedApps);
        BT_resum = root.findViewById(R.id.BT_resum);
        BT_informacio = root.findViewById(R.id.BT_informacio);

        setButtons();

        CL_appsUsage = root.findViewById(R.id.CL_appsUsage);
        CL_clickedBlockedApps = root.findViewById(R.id.CL_clickedBlockedApps);
        CL_resum = root.findViewById(R.id.CL_resum);

        RV_abusedApps = root.findViewById(R.id.RV_abusedApps);
        RV_clickedBlockedApps = root.findViewById(R.id.RV_clickedBlockedApps);

    }
}
