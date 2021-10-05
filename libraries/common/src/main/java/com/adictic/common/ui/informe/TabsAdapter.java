package com.adictic.common.ui.informe;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.adictic.common.entity.GeneralUsage;

import java.util.Collection;
import java.util.Map;

public class TabsAdapter extends FragmentStateAdapter {

    private long childId;
    private Collection<GeneralUsage> genericAppUsage;
    private Map<String, Long> timesBlockedMap;

    private long totalUsageTime;

    private int age;

    TabsAdapter(@NonNull FragmentManager fm, Lifecycle behavior) {
        super(fm, behavior);
    }

    void setChildId(long id) {
        childId = id;
    }

    void setAge(int a) {
        age = a;
    }

    void setGenericAppUsage(Collection<GeneralUsage> col) {
        genericAppUsage = col;
    }

    void setTimesBlockedMap(Map<String, Long> map) {
        timesBlockedMap = map;
    }

    void setTimes(long tUT) {
        totalUsageTime = tUT;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0)
            return InformeGraphsFragment.newInstance(childId, genericAppUsage);
        else
            return InformeDetallatFragment.newInstance(genericAppUsage, totalUsageTime, age, childId);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
