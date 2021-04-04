package com.example.adictic.adapters;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.FillNom;
import com.example.adictic.fragment.MainParentFragment;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TabFillsAdapter extends FragmentStateAdapter {
    private final ArrayList<FillNom> fills;
    private final Context ctx;
    private long lastId;

    public TabFillsAdapter(Fragment fa,Context context, ArrayList<FillNom> list) {
        super(fa);
        fills = list;
        ctx = context;
        lastId = -1;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position > fills.size()-1 || position < 0){
            throw new IllegalStateException("Unexpected position TabProfileAdapter (getItem): " + position);
        }
        else{
            lastId = fills.get(position).idChild;
            if(TodoApp.getTutor() == 1) Funcions.askChildForLiveApp(ctx,lastId, true);

            return new MainParentFragment(fills.get(position));
        }
    }

    public CharSequence getPageTitle(int position) {
        if(position > fills.size()-1 || position < 0){
            throw new IllegalStateException("Unexpected position TabProfileAdapter (getItem): " + position);
        }
        else return fills.get(position).deviceName;
    }

    @Override
    public int getItemCount() {
        return fills.size();
    }

}
