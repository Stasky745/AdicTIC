package com.example.adictic_admin.ui.Usuari;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.adictic_admin.entity.FillNom;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TabFillsAdapter extends FragmentStateAdapter {
    private final ArrayList<FillNom> fills;

    public TabFillsAdapter(@NonNull @NotNull FragmentManager fragment, Lifecycle lifecycle, ArrayList<FillNom> list) {
        super(fragment,lifecycle);
        fills = list;
    }

    public CharSequence getPageTitle(int position) {
        if (position > fills.size() - 1 || position < 0) {
            throw new IllegalStateException("Unexpected position TabProfileAdapter (getItem): " + position);
        } else return fills.get(position).deviceName;
    }

    @NonNull
    @NotNull
    @Override
    public Fragment createFragment(int position) {
        return new MainParentFragment(fills.get(position));
    }

    @Override
    public int getItemCount() {
        return fills.size();
    }
}
