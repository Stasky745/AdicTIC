package com.example.adictic_admin.ui.Xats;

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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.entity.UserMessage;
import com.adictic.common.util.Constants;
import com.developerspace.webrtcsample.RTCActivity;
import com.example.adictic_admin.R;
import com.example.adictic_admin.entity.ChatInfo;
import com.example.adictic_admin.rest.AdminApi;
import com.example.adictic_admin.ui.Usuari.MainUserActivity;
import com.example.adictic_admin.util.AdminApp;
import com.example.adictic_admin.util.Funcions;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class XatActivity extends AppCompatActivity {
    public static ChatInfo userProfile;
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private AdminApi mService;
    private boolean active;
    private TextView TV_profileName;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String TAG = "ChatActivity";

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            UserMessage um = new UserMessage();
            um.message = intent.getStringExtra("message");
            um.createdAt = new Date();
            um.userSenderId = intent.getLongExtra("senderId", 0);
            um.childSenderId = intent.getLongExtra("childId", -1);
            mMessageAdapter.add(um);
            if (!active) {
                EditText chatbox = findViewById(R.id.edittext_chatbox);
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
    private final BroadcastReceiver changeAccessReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            userProfile.hasAccess = intent.getBooleanExtra("hasAccess",false);
            setAccess();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        userProfile = getIntent().getParcelableExtra("chat");
        active = getIntent().getBooleanExtra("active",true);

        mService = ((AdminApp) getApplication()).getAPI();

        setViews();
        setRecyclerView();
        setButtons();
        setBroadcastManagers();
    }

    private void setViews() {
        TextView TV_nomXat = findViewById(R.id.TV_nomXat);
        TV_nomXat.setText(userProfile.username);
        if (!active) closeChat();
    }

    private void setBroadcastManagers() {
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(messageReceiver,
                new IntentFilter("NewMessage"));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(closeChatReceiver,
                new IntentFilter("CloseChat"));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(changeAccessReceiver,
                new IntentFilter("chatAccess"));
    }

    private void setButtons() {
        ImageView sendButton = findViewById(R.id.IV_send);
        sendButton.setClickable(true);
        sendButton.setOnClickListener(v -> {
            EditText chatbox = findViewById(R.id.edittext_chatbox);
            if (!chatbox.getText().toString().isEmpty()) {
                SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

                UserMessage um = new UserMessage();
                um.createdAt = new Date();
                um.message = chatbox.getText().toString();
                um.userSenderId = sharedPreferences.getLong(Constants.SHARED_PREFS_ID_USER,-1);
                chatbox.setText("");
                long userId = userProfile.userId;
                long childId = userProfile.childId;
                Call<String> postCall = mService.sendMessageToUser(userId, childId, um);
                postCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> postCall, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            mMessageAdapter.add(um);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> postCall, @NonNull Throwable t) {
                        Toast.makeText(getApplicationContext(), "An error occurred! Try again later", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        if(active) {
            TV_profileName = findViewById(R.id.TV_nomXat);
            TV_profileName.setClickable(true);
            TV_profileName.setOnClickListener(view -> {
                //mirar si es té accés i si es pot entrar a veure el perfil
                if(userProfile.hasAccess){
                    Intent intent = new Intent(XatActivity.this, MainUserActivity.class);
                    intent.putExtra("idTutor",userProfile.userId);
                    if(userProfile.childId == null)
                        intent.putExtra("idChild", -1);
                    intent.putExtra("idChild", userProfile.childId);
                    this.startActivity(intent);
                }
            });

            setCloseButton();
            setAccess();
            setCallButton();
        }
    }

    private void setCallButton() {
        ImageView IV_call = findViewById(R.id.IV_call);
        IV_call.setOnClickListener(view -> new AlertDialog.Builder(XatActivity.this)
                .setTitle(getString(R.string.trucar_confirmacio))
                .setMessage(getString(R.string.trucar_desc))
                .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                    Call<String> call;
                    if(userProfile.childId==null) call = mService.callOtherUser(userProfile.userId, (long)-1);
                    else call = mService.callOtherUser(userProfile.userId, userProfile.childId);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful()) startCall(response.body());
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Toast.makeText(getApplicationContext(), "No s'ha pogut connectar amb el servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel())
                .show());
    }

    private void startCall(String chatId) {
        db.collection("calls")
                .document(chatId)
                .get()
                .addOnSuccessListener(it ->  {
                    /*if (it.get("type") != null && (it.get("type").toString().equals("OFFER") || it.get("type").toString().equals("ANSWER") || it.get("type").toString().equals("END_CALL"))) {
                        Toast.makeText(this, "Error, sala ja existeix", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error, sala ja existeix");
                    } else {*/
                        Intent intent = new Intent(this, RTCActivity.class);
                        intent.putExtra("meetingID",chatId);
                        intent.putExtra("isJoin",false);
                        startActivity(intent);
                    //}
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void setAccess() {
        ImageView IV_acces = findViewById(R.id.IV_acces);
        setAccessButtonImage(IV_acces);
        IV_acces.setClickable(true);
        IV_acces.setFocusable(true);
        IV_acces.setOnClickListener(view -> {
            if (userProfile.hasAccess) {
                TV_profileName.performClick();
            } else {
                new AlertDialog.Builder(XatActivity.this)
                        .setTitle("No tens accés")
                        .setMessage("Demana-li que premi el botó d'accés per tal que tu puguis veure el contingut seu.")
                        .setPositiveButton(R.string.accept, (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }
        });
    }

    private void setAccessButtonImage(ImageView IV_acces) {
        if (!userProfile.hasAccess) {
            IV_acces.setImageResource(R.drawable.ic_donar_acces);
            DrawableCompat.setTint(
                    DrawableCompat.wrap(IV_acces.getDrawable()),
                    ContextCompat.getColor(getApplicationContext(), R.color.black_overlay)
            );
        } else {
            IV_acces.setImageResource(R.drawable.ic_treure_acces);
            DrawableCompat.setTint(
                    DrawableCompat.wrap(IV_acces.getDrawable()),
                    ContextCompat.getColor(getApplicationContext(), R.color.vermell)
            );
        }
    }

    private void setCloseButton() {
        ImageView IV_closeChat = findViewById(R.id.IV_closeChat);
        IV_closeChat.setClickable(true);
        IV_closeChat.setFocusable(true);
        IV_closeChat.setOnClickListener(view -> new AlertDialog.Builder(XatActivity.this)
                .setTitle("Tancar Xat")
                .setMessage("Vols tancar aquest xat? Si es tanca no es podrà tornar a obrir.")
                .setPositiveButton(R.string.accept, (dialogInterface, i) -> {
                    Call<String> call = mService.closeChat(userProfile.userId, userProfile.childId);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful()) closeChat();
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Toast.makeText(getApplicationContext(), "No s'ha pogut connectar amb el servidor", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel())
                .show());
    }

    private void setRecyclerView() {
        mMessageRecycler = findViewById(R.id.RV_chat);
        mMessageAdapter = new MessageListAdapter();
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    private void closeChat() {
        active = false;
        EditText chatbox = findViewById(R.id.edittext_chatbox);
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
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
        assert sharedPreferences!=null;
        long childId = userProfile.childId != null ? userProfile.childId : -1;
        Call<List<UserMessage>> call = mService.getMyMessagesWithUser(childId, sharedPreferences.getLong(Constants.SHARED_PREFS_ID_ADMIN,-1));
        call.enqueue(new Callback<List<UserMessage>>() {
            @Override
            public void onResponse(@NonNull Call<List<UserMessage>> call, @NonNull Response<List<UserMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!response.body().isEmpty()) {
                        mMessageAdapter.clear();
                        mMessageAdapter.addAll(response.body());
                    }
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

        public MessageListAdapter() {
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

            UserMessage message = mMessageList.get(position);

            if (message.userSenderId.equals(sharedPreferences.getLong(Constants.SHARED_PREFS_ID_USER,-1))) {
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

            void bind(UserMessage message) {
                messageText.setText(message.message);

                DateTime dateTime = new DateTime(message.createdAt);
                // Format the stored timestamp into a readable String using method.
                String time;
                DateTimeFormatter fmt;
                if(dateTime.getDayOfYear() == DateTime.now().getDayOfYear())
                    fmt = DateTimeFormat.forPattern("HH:mm");
                else
                    fmt = DateTimeFormat.forPattern("dd/MM");
                time = dateTime.toString(fmt);
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
                if(dateTime.getDayOfYear() == DateTime.now().getDayOfYear())
                    fmt = DateTimeFormat.forPattern("HH:mm");
                else
                    fmt = DateTimeFormat.forPattern("dd/MM");
                time = dateTime.toString(fmt);
                timeText.setText(time);
            }
        }
    }
}
