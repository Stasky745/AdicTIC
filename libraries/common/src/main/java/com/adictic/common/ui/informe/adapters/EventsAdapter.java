package com.adictic.common.ui.informe.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.CanvisEvents;
import com.adictic.common.entity.EventBlock;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder>{
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM yyyy");
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm");

    private final ArrayList<CanvisEvents> eventsList;
    private final LayoutInflater mInflater;

    public EventsAdapter(List<CanvisEvents> list, Context c){
        mInflater = LayoutInflater.from(c);
        eventsList = new ArrayList<>(list);

        eventsList.sort((o1, o2) -> o2.data.compareTo(o1.data));
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.informe_events_item, parent, false);

        return new EventsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsViewHolder holder, int position) {
        CanvisEvents canvisEvents = eventsList.get(position);
        holder.TV_data.setText(dateFormatter.format(canvisEvents.data));

        if(canvisEvents.eventAntic == null){
            holder.TV_eventVellTitol.setVisibility(View.GONE);
            holder.TV_eventVellDies.setVisibility(View.GONE);
            holder.TV_eventVellHorari.setText(R.string.new_event);
            holder.TV_eventVellHorari.setTextColor(Color.GREEN);
        }
        else{
            holder.TV_eventVellTitol.setText(canvisEvents.eventAntic.name);

            String dies = crearStringDies(canvisEvents.eventAntic);
            holder.TV_eventVellDies.setText(dies);

            String inici = timeFormatter.format(new DateTime().withMillisOfDay(canvisEvents.eventAntic.startEvent));
            String fi = timeFormatter.format(new DateTime().withMillisOfDay(canvisEvents.eventAntic.endEvent));
            String horari = inici+" - "+fi;
            holder.TV_eventVellHorari.setText(horari);
        }

        if(canvisEvents.eventNou == null){
            holder.TV_eventNouTitol.setVisibility(View.GONE);
            holder.TV_eventNouDies.setVisibility(View.GONE);
            holder.TV_eventNouHorari.setText(R.string.delete_event);
            holder.TV_eventNouHorari.setTextColor(Color.RED);
        }
        else{
            holder.TV_eventNouTitol.setText(canvisEvents.eventNou.name);

            String dies = crearStringDies(canvisEvents.eventNou);
            holder.TV_eventNouDies.setText(dies);

            String inici = timeFormatter.format(new DateTime().withMillisOfDay(canvisEvents.eventNou.startEvent));
            String fi = timeFormatter.format(new DateTime().withMillisOfDay(canvisEvents.eventNou.endEvent));
            String horari = inici+" - "+fi;
            holder.TV_eventNouHorari.setText(horari);

            if(canvisEvents.actiu)
                holder.TV_eventActiu.setVisibility(View.VISIBLE);
            else
                holder.TV_eventActiu.setVisibility(View.GONE);

        }
    }

    private String crearStringDies(EventBlock event){
        StringBuilder eventDaysString = new StringBuilder();

        if(event.monday) eventDaysString.append(R.string.monday).append(" ");
        if(event.tuesday) eventDaysString.append(R.string.tuesday).append(" ");
        if(event.wednesday) eventDaysString.append(R.string.wednesday).append(" ");
        if(event.thursday) eventDaysString.append(R.string.thursday).append(" ");
        if(event.friday) eventDaysString.append(R.string.friday).append(" ");
        if(event.saturday) eventDaysString.append(R.string.saturday).append(" ");
        if(event.sunday) eventDaysString.append(R.string.sunday).append(" ");

        return eventDaysString.toString();
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    public static class EventsViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView TV_data, TV_eventVellTitol, TV_eventNouTitol, TV_eventVellHorari, TV_eventNouHorari, TV_eventVellDies, TV_eventNouDies, TV_eventActiu;

        public EventsViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView;

            TV_data = root.findViewById(R.id.TV_canvisEventData);
            TV_eventVellTitol = root.findViewById(R.id.TV_eventVellTitle);
            TV_eventNouTitol = root.findViewById(R.id.TV_eventNouTitle);
            TV_eventVellHorari = root.findViewById(R.id.TV_eventVellHorari);
            TV_eventNouHorari = root.findViewById(R.id.TV_eventNouHorari);
            TV_eventVellDies = root.findViewById(R.id.TV_eventVellDies);
            TV_eventNouDies = root.findViewById(R.id.TV_eventNouDies);
            TV_eventActiu = root.findViewById(R.id.TV_eventActiu);
        }
    }
}
