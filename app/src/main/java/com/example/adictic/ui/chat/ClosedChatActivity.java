package com.example.adictic.ui.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic.R;
import com.example.adictic.entity.AdminProfile;
import com.example.adictic.entity.UserMessage;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.AdminProfileActivity;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClosedChatActivity extends AppCompatActivity {
    AdminProfile adminProfile;
    private SharedPreferences sharedPreferences;
    TodoApi mTodoService;
    RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private Long myId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        mTodoService = ((TodoApp) getApplicationContext()).getAPI();
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        // Agafem la nostra id
        myId = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);

        adminProfile = getIntent().getExtras().getParcelable("chat");
        setViews();
        setRecyclerView();
        getMessages();
        setButtons();
    }

    private void setViews() {
        TextView TV_nomXat = findViewById(R.id.TV_nomXat);
        TV_nomXat.setText(adminProfile.name);
        closeChat();
    }

    private void setButtons() {
        ImageView sendButton = findViewById(R.id.IV_send);
        sendButton.setVisibility(View.GONE);

        TextView TV_profileName = findViewById(R.id.TV_nomXat);
        TV_profileName.setClickable(true);
        TV_profileName.setOnClickListener(view -> {
            Intent intent = new Intent(ClosedChatActivity.this, AdminProfileActivity.class);
            intent.putExtra("adminProfile", adminProfile);
            startActivity(intent);
        });

        ImageView IV_acces = findViewById(R.id.IV_acces);
        ImageView IV_closeChat = findViewById(R.id.IV_closeChat);
        IV_acces.setVisibility(View.GONE);
        IV_closeChat.setVisibility(View.GONE);
    }

    private void setRecyclerView() {
        mMessageRecycler = findViewById(R.id.RV_chat);
        mMessageAdapter = new MessageListAdapter();
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    private void closeChat() {
        EditText chatbox = findViewById(R.id.edittext_chatbox);
        chatbox.setEnabled(false);
        chatbox.setHint(R.string.closed_chat);
        chatbox.setText("");
    }

    public void getMessages() {
        mMessageAdapter.clear();
        long idChild = -1L;
        if(!sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false))
            idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1);

        Call<List<UserMessage>> call = mTodoService.getMyMessagesWithUser(idChild, adminProfile.idUser);
        call.enqueue(new Callback<List<UserMessage>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserMessage>> call, @NonNull Response<List<UserMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().isEmpty()) mMessageAdapter.addAll(response.body());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.error_server_read, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserMessage>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.error_server_read, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class MessageListAdapter extends RecyclerView.Adapter {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private List<UserMessage> mMessageList = new ArrayList<>();

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            UserMessage message = mMessageList.get(position);

            if (message.senderId.equals(myId)) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        // Inflates the appropriate layout according to the ViewType.
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;

            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                return new MessageListAdapter.SentMessageHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                return new MessageListAdapter.ReceivedMessageHolder(view);
            }

            return null;
        }

        // Passes the message object to a ViewHolder so that the contents can be bound to UI.
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            UserMessage message = mMessageList.get(position);

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((ReceivedMessageHolder) holder).bind(message);
            }
        }

        // Remove a RecyclerView item containing the Data object
        public void remove(UserMessage userMessage) {
            int position = 0;
            while (mMessageList.get(position) != userMessage) {
                position++;
            }
            mMessageList.remove(position);
            notifyItemRemoved(position);
        }

        public void add(UserMessage t) {
            mMessageList.add(t);
            this.notifyItemInserted(mMessageList.size() - 1);
            mMessageRecycler.smoothScrollToPosition(mMessageList.size() - 1);
        }

        public void clear() {
            int size = mMessageList.size();
            mMessageList.clear();
            this.notifyItemRangeRemoved(0, size);
        }

        public void addAll(List<UserMessage> messages) {
            mMessageList = messages;
            this.notifyItemRangeInserted(0, messages.size());
            mMessageRecycler.smoothScrollToPosition(mMessageList.size() - 1);
        }

        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.text_message_body);
                timeText = itemView.findViewById(R.id.text_message_time);
            }

            void bind(UserMessage mes) {
                messageText.setText(mes.message);

                DateTime dateTime = new DateTime(mes.createdAt);

                // Format the stored timestamp into a readable String using method.
                String time = Funcions.formatHora(dateTime.getHourOfDay(),dateTime.getMinuteOfHour());
                timeText.setText(time);
            }
        }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.text_message_body);
                timeText = itemView.findViewById(R.id.text_message_time);
            }

            void bind(UserMessage message) {
                messageText.setText(message.message);

                DateTime dateTime = new DateTime(message.createdAt);
                // Format the stored timestamp into a readable String using method.
                String time;
                DateTimeFormatter fmt;
                if(dateTime.getDayOfYear() == DateTime.now().getDayOfYear()) {
                    fmt = DateTimeFormat.forPattern("HH:mm");
                }
                else{
                    fmt = DateTimeFormat.forPattern("dd/MM");
                }
                time = dateTime.toString(fmt);
                timeText.setText(time);
            }
        }
    }
}
