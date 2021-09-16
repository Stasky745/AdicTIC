package com.adictic.admin.ui.Xats;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.adictic.admin.entity.ChatsMain;

import java.util.ArrayList;

public class ChatsAdapter extends FragmentStateAdapter {

    private final ChatsMain chatsMain;

    public ChatsAdapter(@NonNull FragmentActivity fragmentActivity, ChatsMain cm) {
        super(fragmentActivity);
        chatsMain = cm;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if(position == 0){
            if(chatsMain.oberts == null)
                chatsMain.oberts = new ArrayList<>();

            fragment = XatsLlistaFragment.newInstance(chatsMain.oberts, true);
        }
        else {
            if(chatsMain.tancats == null)
                chatsMain.tancats = new ArrayList<>();

            fragment = XatsLlistaFragment.newInstance(chatsMain.tancats, false);
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public CharSequence getPageTitle(int position){
        if(position == 0) return "Xats Oberts";
        else return "Xats tancats";
    }
}
