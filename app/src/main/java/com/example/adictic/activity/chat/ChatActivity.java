package com.example.adictic.activity.chat;


import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.adapters.ChatsAdapter;
import com.example.adictic.entity.ChatsMain;
import com.example.adictic.rest.TodoApi;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    TabLayout _tabChat;
    ViewPager2 _vpChats;
    Boolean _hasClosedChats;
    long _chatObert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        _chatObert = getIntent().getLongExtra("userId",-1);
        TodoApi mTodoService = ((TodoApp)getApplicationContext()).getAPI();

        _vpChats = (ViewPager2) findViewById(R.id.VP_chats);
        _tabChat = (TabLayout) findViewById(R.id.TABL_chats);

        Call<ChatsMain> call = mTodoService.getChatsInfo();
        call.enqueue(new Callback<ChatsMain>() {
            @Override
            public void onResponse(Call<ChatsMain> call, Response<ChatsMain> response) {
                if(response.isSuccessful()){
                    ChatsAdapter adapter = new ChatsAdapter(ChatActivity.this, getBaseContext(),response.body());
                    _vpChats.setAdapter(adapter);

                    new TabLayoutMediator(_tabChat, _vpChats,
                            (tab, position) -> tab.setText(adapter.getPageTitle(position))
                    ).attach();

                    if(adapter.getItemCount()>1){
                        _tabChat.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatsMain> call, Throwable t) {

            }
        });


    }
}