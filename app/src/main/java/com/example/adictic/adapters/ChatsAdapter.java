package com.example.adictic.adapters;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.adictic.R;
import com.example.adictic.activity.chat.ChatFragment;
import com.example.adictic.activity.chat.ChatsClosedFragment;
import com.example.adictic.activity.chat.NoChatFragment;
import com.example.adictic.entity.ChatsMain;

import java.util.ArrayList;

public class ChatsAdapter extends FragmentStateAdapter {
    private final Context ctx;
    private final ChatsMain chatsMain;

    public ChatsAdapter(@NonNull FragmentActivity fragmentActivity, Context context, ChatsMain chats) {
        super(fragmentActivity);
        ctx = context;
        chatsMain = chats;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (chatsMain.obert == null) {
            if (position == 0) return new NoChatFragment();
            else {
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) chatsMain.tancats);

                Fragment fragment = new ChatsClosedFragment();
                fragment.setArguments(bundle);
                return fragment;
            }
        }
        if (position == 0) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("chat",chatsMain.obert);
            bundle.putBoolean("access",chatsMain.hasAccess);
            bundle.putBoolean("active",true);

            Fragment fragment = new ChatFragment();
            fragment.setArguments(bundle);

            return fragment;
        } else {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) chatsMain.tancats);

            Fragment fragment = new ChatsClosedFragment();
            fragment.setArguments(bundle);
            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        if(chatsMain.tancats.isEmpty()) return 1;
        else return 2;
    }

    public CharSequence getPageTitle(int position) {
        if (chatsMain.obert == null) {
            if (position == 0) return ctx.getString(R.string.crear_dbute);
            else return ctx.getString(R.string.chats_tancats);
        }
        if (position == 0) return ctx.getString(R.string.chat_obert);
        else return ctx.getString(R.string.chats_tancats);

    }
}
