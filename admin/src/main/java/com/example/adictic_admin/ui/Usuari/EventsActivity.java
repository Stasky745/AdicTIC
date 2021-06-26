package com.example.adictic_admin.ui.Usuari;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic_admin.App;
import com.example.adictic_admin.R;
import com.example.adictic_admin.entity.EventBlock;
import com.example.adictic_admin.entity.EventsAPI;
import com.example.adictic_admin.rest.Api;
import com.example.adictic_admin.util.Funcions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity {
    Api mTodoService;
    long idChild;

    int canvis;

    EventsAPI events;

    EventBlock selectedEvent;

    RV_Adapter RVadapter;

    RecyclerView RV_eventList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_event);
        mTodoService = ((App) getApplication()).getAPI();

        idChild = getIntent().getLongExtra("idChild", -1);
        events = null;
        canvis = 0;

        selectedEvent = null;

        setLayouts();
        getHoraris();
    }

    private void getHoraris() {
        Call<EventsAPI> call = mTodoService.getEvents(idChild);

        call.enqueue(new Callback<EventsAPI>() {
            @Override
            public void onResponse(@NonNull Call<EventsAPI> call, @NonNull Response<EventsAPI> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        events = response.body();
                        if(events.events == null)
                            events.events = new ArrayList<>();

                        RVadapter = new RV_Adapter(EventsActivity.this, events.events);

                        RV_eventList.setAdapter(RVadapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventsAPI> call, @NonNull Throwable t) {

            }
        });
    }

    private void setLayouts() {
        RV_eventList = findViewById(R.id.RV_events);
        RV_eventList.setLayoutManager(new LinearLayoutManager(this));
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
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
