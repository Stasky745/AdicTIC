package com.example.adictic.ui.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.adictic.R;
import com.example.adictic.entity.ChatInfo;
import com.example.adictic.entity.UserMessage;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.AdminProfileActivity;
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

public class ChatFragment extends Fragment {

    public static Long adminUserId;
    RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private TodoApi mTodoService;
    private boolean active;
    private boolean access;
    private ChatInfo chatInfo;
    private Long myId;
    private View view;

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            UserMessage um = new UserMessage();
            um.message = intent.getStringExtra("message");
            um.createdAt = new Date();
            um.senderId = intent.getLongExtra("senderId", 0);
            mMessageAdapter.add(um);
            if (!active) {
                EditText chatbox = view.findViewById(R.id.edittext_chatbox);
                chatbox.setEnabled(true);
                chatbox.setHint("Enter message");
            }
        }
    };
    private final BroadcastReceiver closeChatReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            closeChat();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.chat_layout, container, false);
        Activity activity = getActivity();
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(activity);

        assert getArguments() != null;
        access = getArguments().getBoolean("access");
        chatInfo = getArguments().getParcelable("chat");
        active = getArguments().getBoolean("active");

        adminUserId = chatInfo.admin.idUser;

        assert activity != null;
        mTodoService = ((TodoApp) activity.getApplication()).getAPI();

        // Agafem la nostra id
        myId = sharedPreferences.getLong("userId", -1);

        setViews();
        setRecyclerView();
        setButtons(activity);
        setBroadcastManagers();

        return view;
    }

    private void setViews() {
        TextView TV_nomXat = view.findViewById(R.id.TV_nomXat);
        TV_nomXat.setText(chatInfo.admin.name);
        if (!active) closeChat();
    }

    private void setBroadcastManagers() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(messageReceiver,
                new IntentFilter("NewMessage"));

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(closeChatReceiver,
                new IntentFilter("CloseChat"));
    }

    private void setButtons(Activity activity) {
        ImageView sendButton = view.findViewById(R.id.IV_send);
        sendButton.setClickable(true);
        sendButton.setOnClickListener(v -> {
            EditText chatbox = view.findViewById(R.id.edittext_chatbox);
            if (!chatbox.getText().toString().isEmpty()) {
                UserMessage um = new UserMessage();
                um.createdAt = new Date();
                um.message = chatbox.getText().toString();
                um.senderId = myId;
                chatbox.setText("");
                long userId = chatInfo.admin.idUser;
                Call<String> postCall = mTodoService.sendMessageToUser(Long.toString(userId), um);
                postCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> postCall, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            mMessageAdapter.add(um);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> postCall, @NonNull Throwable t) {
                        Toast.makeText(activity.getBaseContext(), "An error occurred! Try again later", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        TextView TV_profileName = view.findViewById(R.id.TV_nomXat);
        TV_profileName.setClickable(true);
        TV_profileName.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AdminProfileActivity.class);
            intent.putExtra("adminProfile", chatInfo.admin);
            startActivity(intent);
        });

        setCloseButton();
        setAccess();
    }

    private void setAccess() {
        ImageView IV_acces = view.findViewById(R.id.IV_acces);
        setAccessButtonImage(IV_acces);
        IV_acces.setClickable(true);
        IV_acces.setOnClickListener(view -> {
            if (access) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.treure_acces_titol)
                        .setMessage(R.string.treure_acces_desc)
                        .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                            Call<String> call = mTodoService.giveAccess(false);
                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    if (response.isSuccessful()) {
                                        access = !access;
                                        setAccessButtonImage(IV_acces);
                                    } else
                                        Toast.makeText(getContext(), R.string.error_sending_data, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                    Toast.makeText(getContext(), R.string.error_sending_data, Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                        .show();
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.donar_acces_titol)
                        .setMessage(R.string.donar_acces_desc)
                        .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                            Call<String> call = mTodoService.giveAccess(true);
                            call.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                    if (response.isSuccessful()) {
                                        access = !access;
                                        setAccessButtonImage(IV_acces);
                                    } else
                                        Toast.makeText(getContext(), R.string.error_sending_data, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                    Toast.makeText(getContext(), R.string.error_sending_data, Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel())
                        .show();
            }
        });
    }

    private void setAccessButtonImage(ImageView IV_acces) {
        if (!access) {
            IV_acces.setImageResource(R.drawable.ic_donar_acces);
            DrawableCompat.setTint(
                    DrawableCompat.wrap(IV_acces.getDrawable()),
                    ContextCompat.getColor(requireContext(), R.color.black_overlay)
            );
        } else {
            IV_acces.setImageResource(R.drawable.ic_treure_acces);
            DrawableCompat.setTint(
                    DrawableCompat.wrap(IV_acces.getDrawable()),
                    ContextCompat.getColor(requireContext(), R.color.vermell)
            );
        }
    }

    private void setCloseButton() {
        ImageView IV_closeChat = view.findViewById(R.id.IV_closeChat);
        IV_closeChat.setClickable(true);
        IV_closeChat.setOnClickListener(view -> new AlertDialog.Builder(getContext())
                .setTitle(R.string.close_chat)
                .setMessage(R.string.chat_close_desc)
                .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                    Call<String> call = mTodoService.closeChat(chatInfo.admin.idUser);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful()) closeChat();
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Toast.makeText(getContext(), R.string.error_sending_data, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel())
                .show());
    }

    private void setRecyclerView() {
        mMessageRecycler = view.findViewById(R.id.RV_chat);
        mMessageAdapter = new MessageListAdapter();
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void closeChat() {
        active = false;
        EditText chatbox = view.findViewById(R.id.edittext_chatbox);
        chatbox.setEnabled(false);
        chatbox.setHint(R.string.closed_chat);
        chatbox.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getMessages();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void getMessages() {
        mMessageAdapter.clear();
        Call<List<UserMessage>> call = mTodoService.getMyMessagesWithUser(chatInfo.admin.idUser.toString());
        call.enqueue(new Callback<List<UserMessage>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserMessage>> call, @NonNull Response<List<UserMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().isEmpty()) mMessageAdapter.addAll(response.body());
                } else {
                    Toast.makeText(getContext(), R.string.error_server_read, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserMessage>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), R.string.error_server_read, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class MessageListAdapter extends Adapter {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

        private List<UserMessage> mMessageList = new ArrayList<>();

        public MessageListAdapter() {
        }

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

        class SentMessageHolder extends RecyclerView.ViewHolder {
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
                String time = "";
                if (dateTime.getHourOfDay() < 10) time += "0";
                time += dateTime.getHourOfDay() + ":";
                if (dateTime.getMinuteOfDay() < 10) time += "0";
                time += dateTime.getMinuteOfDay();
                timeText.setText(time);
            }
        }

        class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText;
            TextView timeText;

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