package com.example.adictic.ui.chat;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.adictic.common.util.Constants;
import com.example.adictic.R;
import com.example.adictic.entity.ChatsMain;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    TabLayout _tabChat;
    ViewPager2 _vpChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Funcions.closeKeyboard(findViewById(R.id.main_parent), this);


        TodoApi mTodoService = ((TodoApp) getApplicationContext()).getAPI();

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
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatsMain> call, @NonNull Throwable t) {

            }
        });


    }
}