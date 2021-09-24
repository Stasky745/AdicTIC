package com.adictic.common.ui.events;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.EventsAPI;
import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity implements IEventDialog {
    private Api mTodoService;
    private SharedPreferences sharedPreferences;
    private long idChild;

    private int canvis;

    private EventsAPI events;

    private RV_Adapter RVadapter;

    private Button BT_acceptarHoraris;

    private RecyclerView RV_eventList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_event);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.horaris));
        mTodoService = ((App) getApplication()).getAPI();
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        idChild = getIntent().getLongExtra("idChild", -1);
        events = null;
        canvis = 0;

        setLayouts();
        getHoraris();
        assert sharedPreferences != null;
        if (sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false))
            setButtons();

    }

    @Override
    public void onBackPressed() {
        if (canvis == 0) {
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.closing_activity))
                    .setMessage(getString(R.string.exit_without_save))
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> EventsActivity.super.onBackPressed())
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }

    private void getHoraris() {
        Call<EventsAPI> call = mTodoService.getEvents(idChild);

        call.enqueue(new Callback<EventsAPI>() {
            @Override
            public void onResponse(@NonNull Call<EventsAPI> call, @NonNull Response<EventsAPI> response) {
                    super.onResponse(call, response);
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        events = response.body();
                        RVadapter = new RV_Adapter(EventsActivity.this, events.events);

                        RV_eventList.setAdapter(RVadapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventsAPI> call, @NonNull Throwable t) {
                    super.onFailure(call, t);

            }
        });
    }

    private void setButtons() {
        BT_acceptarHoraris.setVisibility(View.VISIBLE);

        BT_acceptarHoraris.setOnClickListener(view -> {
            Call<String> call = mTodoService.postEvents(idChild, events);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    super.onResponse(call, response);
                    finish();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    super.onFailure(call, t);

                }
            });
        });
    }

    private void setLayouts() {
        BT_acceptarHoraris = findViewById(R.id.BT_acceptarHoraris);
        BT_acceptarHoraris.setVisibility(View.GONE);

        RV_eventList = findViewById(R.id.RV_events);
        RV_eventList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onSelectedData(EventBlock newEvent, boolean delete) {
        events.events.stream()
                .filter(eb -> eb.id > 0 && eb.id == newEvent.id)
                .findAny()
                .ifPresent(eventBlock -> events.events.remove(eventBlock));
        canvis = 1;

        if(!delete)
            events.events.add(newEvent);

        RVadapter.refreshRV(events.events);
    }

    public class RV_Adapter extends RecyclerView.Adapter<RV_Adapter.MyViewHolder> {
        private final int EVENT_TYPE = 0;
        private final int EVENT_ADD_TYPE = 1;

        private final LayoutInflater mInflater;
        private List<EventBlock> eventAdapterList;

        RV_Adapter(Context context, List<EventBlock> list) {
            eventAdapterList = list;
            mInflater = LayoutInflater.from(context);
        }

        public void refreshRV(List<EventBlock> list){
            eventAdapterList = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RV_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            if(viewType == EVENT_TYPE) {
                // infalte the item Layout
                v = mInflater.inflate(R.layout.horaris_event_item, parent, false);
                v.setTag(EVENT_TYPE);
            }
            else{
                // infalte the item Layout
                v = mInflater.inflate(R.layout.horaris_event_nou_event_item, parent, false);
                v.setTag(EVENT_ADD_TYPE);
            }
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            switch (getItemViewType(position)){
                case EVENT_TYPE:
                    final EventBlock event = eventAdapterList.get(position);

                    holder.TV_eventName.setText(event.name);
                    StringBuilder eventDaysString = new StringBuilder();

                    if(event.monday) eventDaysString.append(getString(R.string.monday)).append(" ");
                    if(event.tuesday) eventDaysString.append(getString(R.string.tuesday)).append(" ");
                    if(event.wednesday) eventDaysString.append(getString(R.string.wednesday)).append(" ");
                    if(event.thursday) eventDaysString.append(getString(R.string.thursday)).append(" ");
                    if(event.friday) eventDaysString.append(getString(R.string.friday)).append(" ");
                    if(event.saturday) eventDaysString.append(getString(R.string.saturday)).append(" ");
                    if(event.sunday) eventDaysString.append(getString(R.string.sunday)).append(" ");

                    holder.TV_eventDays.setText(eventDaysString.toString());
                    String eventTimesString = Funcions.millisOfDay2String(event.startEvent)
                            + "\n" + Funcions.millisOfDay2String(event.endEvent);
                    holder.TV_eventTimes.setText(eventTimesString);

                    if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR,false)) {
                        holder.mRootView.setOnClickListener(view -> {
                            FragmentManager fm = getSupportFragmentManager();
                            EventFragment horarisEventFragment = EventFragment.newInstance(getString(R.string.events), eventAdapterList.get(position));
                            horarisEventFragment.show(fm, "fragment_edit_event");
                            notifyDataSetChanged();
                        });
                    }

                    break;

                case EVENT_ADD_TYPE:
                    holder.mRootView.setOnClickListener(v -> {
                        EventBlock newEvent = new EventBlock();
                        FragmentManager fm = getSupportFragmentManager();
                        EventFragment horarisEventFragment = EventFragment.newInstance(getString(R.string.events), newEvent);
                        horarisEventFragment.show(fm, "fragment_create_event");
                    });
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == eventAdapterList.size() ? EVENT_ADD_TYPE : EVENT_TYPE;
        }

        @Override
        public int getItemCount() {
            if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false))
                return eventAdapterList.size() + 1;
            else
                return eventAdapterList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            protected View mRootView;
            private final TextView TV_eventName;
            private final TextView TV_eventDays;
            private final TextView TV_eventTimes;


            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                mRootView = itemView;

                if(itemView.getTag().equals(EVENT_TYPE)) {
                    TV_eventName = itemView.findViewById(R.id.TV_eventName);
                    TV_eventName.setSelected(true);
                    TV_eventDays = itemView.findViewById(R.id.TV_eventDays);
                    TV_eventDays.setSelected(true);
                    TV_eventTimes = itemView.findViewById(R.id.TV_eventTimes);
                    TV_eventTimes.setSelected(true);
                }
                else{
                    TV_eventName = null;
                    TV_eventDays = null;
                    TV_eventTimes = null;
                }
            }
        }
    }
}
