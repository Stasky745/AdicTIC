package com.example.adictic.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic.R;
import com.example.adictic.entity.AdminProfile;
import com.example.adictic.entity.ChatInfo;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.TodoApp;

import java.util.ArrayList;
import java.util.List;

public class ChatsClosedFragment extends Fragment {

    TodoApi mTodoService;

    RecyclerView mRecyclerView;
    List<ChatInfo> chatsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_chat_closed, container, false);
    }

    @Override
    public void onStart() {

        super.onStart();
        mTodoService = ((TodoApp) this.requireActivity().getApplication()).getAPI();

        assert getArguments() != null;
        chatsList = getArguments().getParcelableArrayList("list");

        mRecyclerView = requireView().findViewById(R.id.RV_chats_closed);
        ClosedChatsListAdapter mAdapter = new ClosedChatsListAdapter(this.requireActivity().getApplication());
        mAdapter.setList(chatsList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
    }

    static class ChatInfoViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView message;
        View view;

        ChatInfoViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            name = itemView.findViewById(R.id.TV_chat_info);
            message = itemView.findViewById(R.id.TV_lastMessage);
        }
    }

    static class ClosedChatsListAdapter extends RecyclerView.Adapter<ChatInfoViewHolder> {

        List<ChatInfo> list = new ArrayList<>();
        Context context;

        public ClosedChatsListAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ChatInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_info, parent, false);

            return new ChatInfoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ChatInfoViewHolder holder, final int position) {
            ChatInfo chatInfo = list.get(position);
            holder.name.setText(chatInfo.admin.name);
            holder.message.setText(chatInfo.lastMessage);

            holder.view.setOnClickListener(view -> {
                AdminProfile adminProfile = list.get(position).admin;
                Intent intent = new Intent(view.getContext(),ClosedChatActivity.class);
                intent.putExtra("chat",adminProfile);
                view.getContext().startActivity(intent);
            });

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {

            super.onAttachedToRecyclerView(recyclerView);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(ChatInfo data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

        public void setList(List<ChatInfo> chats) {
            list = chats;
            this.notifyDataSetChanged();
        }

        public void add(ChatInfo t) {
            list.add(t);
            this.notifyItemInserted(list.size() - 1);
        }

        public void clear() {
            int size = list.size();
            list.clear();
            this.notifyItemRangeRemoved(0, size);
        }
    }
}
