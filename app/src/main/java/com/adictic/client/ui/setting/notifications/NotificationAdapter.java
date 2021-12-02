package com.adictic.client.ui.setting.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.adictic.client.R;
import com.adictic.client.entity.NotificationInformation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationAdapter extends FragmentStateAdapter {
    List<NotificationInformation> notifList;
    int nTabs;
    Context mCtx;

    List<NotificationInformation> criticalNotifs;
    List<NotificationInformation> informationNotifs;

    public NotificationAdapter(@NonNull @NotNull FragmentManager fragment, Lifecycle lifecycle, Context ctx, ArrayList<NotificationInformation> list) {
        super(fragment, lifecycle);
        mCtx = ctx;

        notifList = list;
        criticalNotifs = list.stream()
                .filter(notificationInformation -> notificationInformation.important)
                .collect(Collectors.toList());
        informationNotifs = list.stream()
                .filter(notificationInformation -> !notificationInformation.important)
                .collect(Collectors.toList());

        nTabs = 0;
        if(!criticalNotifs.isEmpty())
            nTabs++;
        if(!informationNotifs.isEmpty())
            nTabs++;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String tabName = getPageTitle(position);
        if(tabName.equals(mCtx.getString(R.string.critical)))
            return NotificationFragment.newInstance(criticalNotifs);
        else
            return NotificationFragment.newInstance(informationNotifs);
    }

    @Override
    public int getItemCount() {
        return nTabs;
    }

    public String getPageTitle(int position) {
        if(nTabs == 2){
            if(position == 0)
                return mCtx.getString(R.string.critical);
            else
                return mCtx.getString(R.string.informacio);
        }
        else{
            //return "";
            if(!criticalNotifs.isEmpty())
                return mCtx.getString(R.string.critical);
            else
                return mCtx.getString(R.string.informacio);
        }
    }
}
