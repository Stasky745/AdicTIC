package com.example.adictic.activity.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.ChatInfo;
import com.example.adictic.rest.TodoApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatsClosedFragment extends Fragment {

    TodoApi mTodoService;

    RecyclerView mRecyclerView;
    List<ChatInfo> chatsList;
    private ClosedChatsListAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_chat_closed, container, false);
    }

    @Override
    public void onStart() {

        super.onStart();
        mTodoService = ((TodoApp) this.getActivity().getApplication()).getAPI();

        assert getArguments() != null;
        chatsList = getArguments().getParcelableArrayList("list");

        mRecyclerView = getView().findViewById(R.id.RV_chats_closed);
        mAdapter = new ClosedChatsListAdapter(this.getActivity().getApplication());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        updateClosedChatsList();
    }

    public void updateClosedChatsList() {
//        Call<List<ChatInfo>> call = null;
//        call = mTodoService.getMyClosedChats();
//        call.enqueue(new Callback<List<ChatInfo>>() {
//            @Override
//            public void onResponse(Call<List<ChatInfo>> call, Response<List<ChatInfo>> response) {
//                if (response.isSuccessful()) {
//                    mAdapter.clear();
//                    for (ChatInfo t : response.body()) {
//                        if(!t.chatActive) mAdapter.add(t);
//                    }
//                } else {
//                    Toast.makeText(ChatsClosedFragment.this.getContext(), "Error reading chats", Toast.LENGTH_LONG).show();
//                }
//            }
//            @Override
//            public void onFailure(Call<List<ChatInfo>> call, Throwable t) {}
//        });
    }

    static class ChatInfoViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        View view;

        ChatInfoViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            name = (TextView) itemView.findViewById(R.id.TV_chat_info);
        }
    }

    class ClosedChatsListAdapter extends RecyclerView.Adapter<ChatInfoViewHolder> {

        List<ChatInfo> list = new ArrayList<>();
        Context context;

        public ClosedChatsListAdapter(Context context) {
            this.context = context;
        }

        @Override
        public ChatInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_info, parent, false);
            ChatInfoViewHolder holder = new ChatInfoViewHolder(v);

            return holder;
        }

        @Override
        public void onBindViewHolder(ChatInfoViewHolder holder, final int position) {
            holder.name.setText(list.get(position).admin.nom);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Falta fer
                }
            });

        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {

            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView
        public void insert(int position, ChatInfo data) {
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(ChatInfo data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
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
