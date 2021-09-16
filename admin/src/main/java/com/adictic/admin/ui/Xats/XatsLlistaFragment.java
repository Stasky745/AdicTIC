package com.adictic.admin.ui.Xats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.admin.entity.ChatInfo;
import com.adictic.admin.R;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class XatsLlistaFragment extends Fragment {
    private static final String ARG_LIST = "arg_list";
    private static final String ARG_BOOL = "arg_bool";

    private List<ChatInfo> chatsList;
    private static boolean active;

    public XatsLlistaFragment() { }

    public static XatsLlistaFragment newInstance(List<ChatInfo> list, boolean a){
        final XatsLlistaFragment xatsLlistaFragment = new XatsLlistaFragment();
        final Bundle args = new Bundle(2);

        args.putBoolean(ARG_BOOL, a);
        args.putParcelableArrayList(ARG_LIST, new ArrayList<>(list));
        xatsLlistaFragment.setArguments(args);

        return xatsLlistaFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        getBundle();
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    private void getBundle() {
        final Bundle arguments = getArguments();

        if(arguments == null)
            return;

        chatsList = arguments.getParcelableArrayList(ARG_LIST);
        active = arguments.getBoolean(ARG_BOOL);
    }

    @Override
    public void onStart() {
        super.onStart();

        chatsList.sort((chatInfo, t1) -> {
            DateTime dateTime1 = new DateTime(chatInfo.timeLastMessage);
            DateTime dateTime2 = new DateTime(t1.timeLastMessage);
            return Long.compare(dateTime2.getMillis(), dateTime1.getMillis());
        });

        RecyclerView mRecyclerView = requireView().findViewById(R.id.RV_chats_closed);
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
            holder.name.setText(chatInfo.username);
            holder.message.setText(chatInfo.lastMessage);

            holder.view.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(),XatActivity.class);
                intent.putExtra("chat",chatInfo);
                intent.putExtra("active", active);
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
