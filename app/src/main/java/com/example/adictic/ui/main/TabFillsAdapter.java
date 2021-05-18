package com.example.adictic.ui.main;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.adictic.entity.FillNom;
import com.example.adictic.util.Funcions;

import java.util.ArrayList;
import java.util.Comparator;

public class TabFillsAdapter extends FragmentStateAdapter {
    private final ArrayList<FillNom> fills;
    private final Context ctx;
    private long lastId;

    public TabFillsAdapter(Fragment fa, Context context, ArrayList<FillNom> list) {
        super(fa);
        fills = list;

        // Per prova de vídeo per concurs Yvonne
        fills.sort((fillNom, t1) -> Long.compare(fillNom.idChild, t1.idChild));

        ctx = context;
        lastId = -1;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ctx);

        if (position > fills.size() - 1 || position < 0) {
            throw new IllegalStateException("Unexpected position TabProfileAdapter (getItem): " + position);
        } else {
            lastId = fills.get(position).idChild;
            if (sharedPreferences.getBoolean("isTutor",false)) Funcions.askChildForLiveApp(ctx, lastId, true);

            return new MainParentFragment(fills.get(position));
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
