package com.adictic.admin.ui.Xats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.adictic.admin.R;
import com.adictic.admin.entity.ChatsMain;
import com.adictic.admin.rest.AdminApi;
import com.adictic.admin.util.AdminApp;
import com.adictic.common.util.Callback;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Response;

public class XatsFragment extends Fragment {

    private AdminApi mService;
    private ViewPager2 _vpChats;
    private TabLayout _tabChat;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.chat_dashboard, container, false);
        mService = ((AdminApp) requireActivity().getApplicationContext()).getAPI();

        _vpChats = root.findViewById(R.id.VP_chats);
        _tabChat = root.findViewById(R.id.TABL_chats);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Call<ChatsMain> call = mService.getAllChats();
        call.enqueue(new Callback<ChatsMain>() {
            @Override
            public void onResponse(@NotNull Call<ChatsMain> call, @NotNull Response<ChatsMain> response) {
                    super.onResponse(call, response);
                if(response.isSuccessful() && response.body() != null){
                    ChatsMain chatsMain = response.body();
                    ChatsAdapter chatsAdapter = new ChatsAdapter(requireActivity(), chatsMain);

                    _vpChats.setAdapter(chatsAdapter);

                    new TabLayoutMediator(_tabChat, _vpChats,
                            (tab, position) -> tab.setText(chatsAdapter.getPageTitle(position))
                    ).attach();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ChatsMain> call, @NotNull Throwable t) {

            }
        });
    }
}