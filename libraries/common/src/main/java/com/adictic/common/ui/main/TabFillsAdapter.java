package com.adictic.common.ui.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.adictic.common.entity.FillNom;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;

public class TabFillsAdapter extends FragmentStateAdapter {
    private ArrayList<FillNom> fills;
    private Context ctx;
    private long lastId;

    public TabFillsAdapter(Fragment fa, Context context, ArrayList<FillNom> list) {
        super(fa);
        fills = list;

        // Per prova de vídeo per concurs Yvonne
        fills.sort(Comparator.comparingLong(fillNom -> fillNom.idChild));

        ctx = context;
        lastId = -1;
    }

    public TabFillsAdapter(@NonNull @NotNull FragmentManager fragment, Lifecycle lifecycle, ArrayList<FillNom> list) {
        super(fragment,lifecycle);
        fills = list;
    }

    public void updateFills(ArrayList<FillNom> list) { fills = list; }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position > fills.size() - 1 || position < 0) {
            throw new IllegalStateException("Unexpected position TabProfileAdapter (getItem): " + position);
        } else {
            lastId = fills.get(position).idChild;

            return MainParentFragment.newInstance(fills.get(position));
        }
    }

    public CharSequence getPageTitle(int position) {
        if (position > fills.size() - 1 || position < 0) {
            throw new IllegalStateException("Unexpected position TabProfileAdapter (getItem): " + position);
        } else return fills.get(position).deviceName;
    }

    @Override
    public int getItemCount() {
        return fills.size();
    }

}
