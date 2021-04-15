package com.example.adictic.ui.informe;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.adictic.entity.GeneralUsage;

import java.util.Collection;
import java.util.Map;

public class TabsAdapter extends FragmentStatePagerAdapter {

    private long childId;
    private Collection<GeneralUsage> genericAppUsage;
    private Map<String, Long> timesBlockedMap;

    private long totalTime, totalUsageTime;

    private int age;

    TabsAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    void setChildId(long id){ childId = id; }

    void setAge(int a){ age = a; }

    void setGenericAppUsage(Collection<GeneralUsage> col){ genericAppUsage = col; }

    void setTimesBlockedMap(Map<String,Long> map){ timesBlockedMap = map; }

    void setTimes(long tT, long tUT){
        totalTime = tT;
        totalUsageTime = tUT;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) return new GraphsFragment(childId,genericAppUsage);
        else return new ResumFragment(genericAppUsage, totalTime, totalUsageTime, age, timesBlockedMap);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
