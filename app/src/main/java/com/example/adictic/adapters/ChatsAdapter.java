package com.example.adictic.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.adictic.R;
import com.example.adictic.fragment.ChatFragment;
import com.example.adictic.fragment.ChatsClosedFragment;
import com.example.adictic.fragment.NoChatFragment;

public class ChatsAdapter extends FragmentStateAdapter {

    private final boolean _hasClosedChats;
    private final long _typeChat; // 0 = no chat / 1 = chat / -1 = illegal
    private final Context ctx;

    public ChatsAdapter(@NonNull FragmentActivity fragmentActivity, Context context, boolean hasClosedChats, long type) {
        super(fragmentActivity);
        _hasClosedChats = hasClosedChats;
        _typeChat = type;
        ctx = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (_typeChat == 0) {
            if (position == 0) {
                return new NoChatFragment();
            } else {
                return new ChatsClosedFragment();
            }
        }
        if (position == 0) {
            return new ChatFragment();
        } else {
            return new ChatsClosedFragment();
        }
    }

    @Override
    public int getItemCount() {
        if(!_hasClosedChats) return 1;
        else return 2;
    }

    public CharSequence getPageTitle(int position) {
        if (_typeChat == 0) {
            if (position == 0) return ctx.getString(R.string.crear_dbute);
            else return ctx.getString(R.string.chats_tancats);
        }
        if (position == 0) return ctx.getString(R.string.chat_obert);
        else return ctx.getString(R.string.chats_tancats);

    }
}
