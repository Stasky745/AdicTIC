package com.example.adictic.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.User;
import com.example.adictic.entity.UserMessage;
import com.example.adictic.rest.TodoApi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private TodoApi mTodoService;
    static public Long active;
    private Long myId;
    public boolean closed = false;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        Activity activity = getActivity();

        mTodoService = ((TodoApp) activity.getApplication()).getAPI();
        myId = TodoApp.getIDTutor();
        getOtherUser();

        mMessageRecycler = (RecyclerView) view.findViewById(R.id.RV_chat);
        mMessageAdapter = new MessageListAdapter(getContext());
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(getContext()));


        Button sendButton = (Button) view.findViewById(R.id.button_chatbox_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText chatbox = (EditText) view.findViewById(R.id.edittext_chatbox);
                if(!chatbox.getText().toString().isEmpty()){
                    UserMessage um = new UserMessage();
                    um.createdAt = new Date();
                    um.message=chatbox.getText().toString();
                    um.senderId = myId;
                    chatbox.setText("");
                    Long userId = activity.getIntent().getExtras().getLong("userId");
                    Call<String> postCall = mTodoService.sendMessageToUser(userId.toString(),um);
                    postCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> postCall, Response<String> response) {
                            if (response.isSuccessful()) {
                                if(response.body().equals("Closed")) closeChat();
                                else mMessageAdapter.add(um);
                            }
                        }
                        @Override
                        public void onFailure(Call<String> postCall, Throwable t) {
                            Toast.makeText(activity.getBaseContext(), "An error occurred! Try again later", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        ConstraintLayout constraint = (ConstraintLayout) view.findViewById(R.id.chat_constraint);
        constraint.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                /*Intent i = new Intent(getApplicationContext(),OtherUserProfile.class);
                i.putExtra("userId",getIntent().getLongExtra("userId",0));
                startActivity(i);*/
            }
        });

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(messageReceiver,
                new IntentFilter("NewMessage"));

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(closeChatReceiver,
                new IntentFilter("CloseChat"));


        return view;
    }

    private void closeChat(){
        if(!closed) {
            EditText chatbox = (EditText) view.findViewById(R.id.edittext_chatbox);
            chatbox.setEnabled(false);
            chatbox.setHint("Chat is closed!");
            chatbox.setText("");
            closed = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        active = getActivity().getIntent().getExtras().getLong("userId");
        this.getMessages();
        if(!getActivity().getIntent().getBooleanExtra("active",true)){
            closeChat();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        active = Long.valueOf(0);
    }

    private void getOtherUser(){
        /*active = getActivity().getIntent().getExtras().getLong("userId");
        Call<User> call = mTodoService.getUser(active.toString());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()){
                    User u = response.body();
                    ((TextView)findViewById(R.id.chat_username)).setText(u.name);
                    if (u.updatedImage)
                        showImage(u,findViewById(R.id.chat_userlogo));
                    else
                        new MessageListActivity.DownloadImageFromInternet((ImageView)findViewById(R.id.chat_userlogo)).execute(u.image);
                } else {
                    Toast.makeText(getBaseContext(), "Error reading messages", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });*/

    }

    private void showImage(User u, ImageView iv)
    {
        /*Call<ResponseBody> call = mTodoService.getImage(u.image);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Bitmap bi = BitmapFactory.decodeStream(response.body().byteStream());
                    iv.setImageBitmap(bi);
                } else {
                    Toast.makeText(getBaseContext(), "Error downloading profile image", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });*/
    }

    public void getMessages(){
        mMessageAdapter.clear();
        Call<List<UserMessage>> call = mTodoService.getMyMessagesWithUser(active.toString());
        call.enqueue(new Callback<List<UserMessage>>() {
            @Override
            public void onResponse(Call<List<UserMessage>> call, Response<List<UserMessage>> response) {
                if(response.isSuccessful()){
                    if(!response.body().isEmpty()) mMessageAdapter.addAll(response.body());
                } else {
                }
            }

            @Override
            public void onFailure(Call<List<UserMessage>> call, Throwable t) {
            }
        });
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            UserMessage um = new UserMessage();
            um.message = intent.getStringExtra("message");
            um.createdAt= new Date();
            um.senderId = intent.getLongExtra("senderId",0);
            mMessageAdapter.add(um);
            if(closed) {
                EditText chatbox = (EditText) view.findViewById(R.id.edittext_chatbox);
                chatbox.setEnabled(true);
                chatbox.setHint("Enter message");
                closed = false;
            }
        }
    };

    private BroadcastReceiver closeChatReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            closeChat();
        }
    };

    public class MessageListAdapter extends Adapter {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private Context mContext;
        private List<UserMessage> mMessageList = new ArrayList<>();

        public MessageListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            UserMessage message = (UserMessage) mMessageList.get(position);

            if (message.senderId.equals(myId)) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        // Inflates the appropriate layout according to the ViewType.
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageHolder(view);
            }

            return null;
        }

        // Passes the message object to a ViewHolder so that the contents can be bound to UI.
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserMessage message = (UserMessage) mMessageList.get(position);

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind(message);
            }
        }

        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            }

            void bind(UserMessage mes) {
                messageText.setText(mes.message);
                messageText.setTextColor(Color.BLACK);
                // Format the stored timestamp into a readable String using method.
                String time = "";
                if(mes.createdAt.getHours()<10) time+="0";
                time+=mes.createdAt.getHours()+":";
                if(mes.createdAt.getMinutes()<10) time+="0";
                time+=mes.createdAt.getMinutes();
                timeText.setText(time);
            }
        }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText, nameText;
            ImageView profileImage;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            }

            void bind(UserMessage message) {
                messageText.setText(message.message);
                messageText.setTextColor(Color.BLACK);
                // Format the stored timestamp into a readable String using method.
                String time = "";
                if(message.createdAt.getHours()<10) time+="0";
                time+=message.createdAt.getHours()+":";
                if(message.createdAt.getMinutes()<10) time+="0";
                time+=message.createdAt.getMinutes();
                timeText.setText(time);
            }
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(UserMessage userMessage) {
            int position = 0;
            while(mMessageList.get(position) != userMessage){
                position++;
            }
            mMessageList.remove(position);
            notifyItemRemoved(position);
        }

        public void add(UserMessage t) {
            mMessageList.add(t);
            this.notifyItemInserted(mMessageList.size() - 1);
            mMessageRecycler.smoothScrollToPosition(mMessageList.size()-1);
        }

        public void clear() {
            int size = mMessageList.size();
            mMessageList.clear();
            this.notifyItemRangeRemoved(0, size);
        }

        public void addAll(List<UserMessage> messages){
            mMessageList = messages;
            this.notifyItemRangeInserted(0,messages.size());
            mMessageRecycler.smoothScrollToPosition(mMessageList.size()-1);
        }
    }
}