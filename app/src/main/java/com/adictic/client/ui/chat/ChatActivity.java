package com.adictic.client.ui.chat;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.common.entity.ChatsMain;
import com.adictic.common.rest.Api;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.client.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    TabLayout _tabChat;
    ViewPager2 _vpChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Funcions.closeKeyboard(findViewById(R.id.main_parent), this);


        Api mTodoService = ((AdicticApp) getApplicationContext()).getAPI();

        _vpChats = findViewById(R.id.VP_chats);
        _tabChat = findViewById(R.id.TABL_chats);

        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ChatActivity.this);
        assert sharedPreferences != null;
        long idChild = -1L;
        if(!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false))
            idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);

        Call<ChatsMain> call = mTodoService.getChatsInfo(idChild);
        call.enqueue(new Callback<ChatsMain>() {
            @Override
            public void onResponse(@NonNull Call<ChatsMain> call, @NonNull Response<ChatsMain> response) {
                    super.onResponse(call, response);
                if (response.isSuccessful()) {
                    ChatsMain chatMain = response.body();
                    ChatsAdapter adapter = new ChatsAdapter(ChatActivity.this, getBaseContext(), chatMain);
                    _vpChats.setAdapter(adapter);

                    new TabLayoutMediator(_tabChat, _vpChats,
                            (tab, position) -> tab.setText(adapter.getPageTitle(position))
                    ).attach();

                    if (adapter.getItemCount() > 1) {
                        _tabChat.setVisibility(View.VISIBLE);
                    }
                    for(int i=0; i < _vpChats.getChildCount(); i++){
                        _vpChats.getChildAt(i).setOverScrollMode(View.OVER_SCROLL_NEVER);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatsMain> call, @NonNull Throwable t) {
                    super.onFailure(call, t);

            }
        });


    }
}