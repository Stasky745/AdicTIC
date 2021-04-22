package com.example.adictic.ui.events;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic.R;
import com.example.adictic.entity.Horaris;
import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.entity.WakeSleepLists;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.roomdb.EventBlock;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity implements IEventDialog {
    TodoApi mTodoService;
    long idChild;

    int canvis;

    EventBlock selectedEvent;

    RV_Adapter RVadapter;

    WakeSleepLists wakeSleepList;
    List<EventBlock> eventList;

    Button BT_acceptarHoraris;
    Button BT_modificarEvent;
    Button BT_afegirEvent;
    Button BT_esborrarEvent;

    RecyclerView RV_eventList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_general_layout);
        mTodoService = ((TodoApp) getApplication()).getAPI();

        idChild = getIntent().getLongExtra("idChild", -1);
        eventList = new ArrayList<>();
        canvis = 0;

        selectedEvent = null;


        setLayouts();
        getHoraris();
        if (TodoApp.getTutor() == 1) setButtons();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                wakeSleepList = data.getParcelableExtra("wakeSleepList");

                if (canvis == 0) canvis = data.getIntExtra("canvis", 0);
            }
        }
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
        Call<Horaris> call = mTodoService.getHoraris(idChild);

        call.enqueue(new Callback<Horaris>() {
            @Override
            public void onResponse(@NonNull Call<Horaris> call, @NonNull Response<Horaris> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        wakeSleepList = response.body().wakeSleepList;
                        eventList = response.body().events;

                        RVadapter = new RV_Adapter(EventsActivity.this, eventList);

                        RV_eventList.setAdapter(RVadapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Horaris> call, @NonNull Throwable t) {

            }
        });
    }

    private void setButtons() {

        BT_acceptarHoraris.setVisibility(View.VISIBLE);
        BT_modificarEvent.setVisibility(View.VISIBLE);
        BT_afegirEvent.setVisibility(View.VISIBLE);
        BT_esborrarEvent.setVisibility(View.VISIBLE);

        BT_acceptarHoraris.setOnClickListener(view -> {
            Horaris horaris = new Horaris();
            horaris.events = eventList;

            if (wakeSleepList == null) {
                wakeSleepList = new WakeSleepLists();
            }

            horaris.wakeSleepList = wakeSleepList;

            Call<String> call = mTodoService.postHoraris(idChild, horaris);

            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    finish();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                }
            });
        });

        BT_esborrarEvent.setOnClickListener(view -> {
            if (selectedEvent != null) {
                new AlertDialog.Builder(EventsActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.esborrar_event))
                        .setMessage(getString(R.string.esborrar_event_text, selectedEvent.name))
                        .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                            boolean trobat = false;
                            int count = 0;
                            while (count < eventList.size() && !trobat) {
                                if (eventList.get(count).name.equals(selectedEvent.name)) {
                                    trobat = true;
                                    eventList.remove(count);
                                } else count++;
                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            } else {
                Toast.makeText(EventsActivity.this, getString(R.string.no_event_selected), Toast.LENGTH_LONG).show();
            }
        });

        BT_afegirEvent.setOnClickListener(view -> {
            EventBlock newEvent = new EventBlock();

            FragmentManager fm = getSupportFragmentManager();
            EventFragment horarisEventFragment = EventFragment.newInstance(getString(R.string.events), newEvent);
            horarisEventFragment.show(fm, "fragment_create_event");
        });

        BT_modificarEvent.setOnClickListener(view -> {
            if (selectedEvent != null) {
                FragmentManager fm = getSupportFragmentManager();
                EventFragment horarisEventFragment = EventFragment.newInstance(getString(R.string.events), selectedEvent);
                horarisEventFragment.show(fm, "fragment_edit_event");
            } else {
                Toast.makeText(EventsActivity.this, getString(R.string.no_event_selected), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLayouts() {
        BT_acceptarHoraris = findViewById(R.id.BT_acceptarHoraris);
        BT_modificarEvent = findViewById(R.id.BT_modificarEvent);
        BT_afegirEvent = findViewById(R.id.BT_afegirEvent);
        BT_esborrarEvent = findViewById(R.id.BT_esborrarEvent);

        BT_acceptarHoraris.setVisibility(View.GONE);
        BT_modificarEvent.setVisibility(View.GONE);
        BT_afegirEvent.setVisibility(View.GONE);
        BT_esborrarEvent.setVisibility(View.GONE);

        RV_eventList = findViewById(R.id.RV_events);
        RV_eventList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onSelectedData(EventBlock newEvent) {
        if (newEvent.id != 0) {
            int i = 0;
            boolean found = false;
            while (i < eventList.size() && !found) {
                if (eventList.get(i).id == (newEvent.id)) {
                    eventList.remove(i);
                    found = true;
                }
                i++;
            }
        }
        eventList.add(newEvent);

        RVadapter = new RV_Adapter(EventsActivity.this, eventList);

        RV_eventList.setAdapter(RVadapter);
    }

    public class RV_Adapter extends RecyclerView.Adapter<RV_Adapter.MyViewHolder> {
        Context mContext;
        LayoutInflater mInflater;
        List<EventBlock> eventAdapterList;

        RV_Adapter(Context context, List<EventBlock> list) {
            mContext = context;
            eventAdapterList = list;
            mInflater = LayoutInflater.from(mContext);
        }

        @NonNull
        @Override
        public RV_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = mInflater.inflate(R.layout.horaris_event_item, parent, false);


            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            if (selectedEvent != null)
                holder.itemView.setActivated(selectedEvent.name.equals(eventAdapterList.get(position).name));
            else holder.itemView.setActivated(false);
            if (holder.itemView.isActivated())
                holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.background_activity));
            else holder.itemView.setBackgroundColor(Color.TRANSPARENT);

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
//            for (int i : event.days) {
//                String day = new DateFormatSymbols().getWeekdays()[i];
//                String s1 = day.substring(0, 1).toUpperCase();
//                //eventDaysString += s1 + day.substring(1) + " ";
//                eventDaysString.append(s1).append(day.substring(1)).append(" ");
//            }
            holder.TV_eventDays.setText(eventDaysString.toString());
            String eventTimesString = Funcions.millisOfDay2String(event.startEvent)
                    + "\n" + Funcions.millisOfDay2String(event.endEvent);
            holder.TV_eventTimes.setText(eventTimesString);

            holder.mRootView.setOnClickListener(view -> {
                if (selectedEvent == null || !selectedEvent.name.equals(event.name)) {
                    selectedEvent = event;
                } else {
                    selectedEvent = null;
                }
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return eventAdapterList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            protected View mRootView;
            TextView TV_eventName, TV_eventDays, TV_eventTimes;


            MyViewHolder(@NonNull View itemView) {
                super(itemView);

                mRootView = itemView;

                TV_eventName = itemView.findViewById(R.id.TV_eventName);
                TV_eventName.setSelected(true);
                TV_eventDays = itemView.findViewById(R.id.TV_eventDays);
                TV_eventDays.setSelected(true);
                TV_eventTimes = itemView.findViewById(R.id.TV_eventTimes);
                TV_eventTimes.setSelected(true);
            }
        }
    }
}
