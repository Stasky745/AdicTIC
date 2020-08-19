package com.example.adictic.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.adictic.TodoApp;
import com.example.adictic.entity.Horaris;
import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.entity.WakeSleepLists;
import com.example.adictic.rest.TodoApi;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorarisMainActivity extends AppCompatActivity {
    TodoApi mTodoService;
    long idChild;

    int canvis;

    HorarisEvents selectedEvent;

    RV_Adapter RVadapter;

    WakeSleepLists wakeSleepList;
    List<HorarisEvents> eventList;

    TextView TV_horarisDormir;

    Button BT_canviHorari;
    Button BT_acceptarHoraris;
    Button BT_modificarEvent;
    Button BT_afegirEvent;
    Button BT_esborrarEvent;

    RecyclerView RV_eventList;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_general_layout);
        mTodoService = ((TodoApp) getApplication()).getAPI();

        idChild = getIntent().getLongExtra("idChild",-1);
        eventList = new ArrayList<>();
        canvis = 0;

        selectedEvent = null;

        setLayouts();
        getHoraris();
        setCurrentSleepTimes();
        setButtons();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                assert data != null;
                wakeSleepList = data.getParcelableExtra("wakeSleepList");

                if(canvis == 0) canvis = data.getIntExtra("canvis",0);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(canvis == 0){
            super.onBackPressed();
        }
        else{
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.closing_activity))
                    .setMessage(getString(R.string.exit_without_save))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            HorarisMainActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(getString(R.string.no),null)
                    .show();
        }
    }

    private void getHoraris(){
        Call<Horaris> call = mTodoService.getHoraris(idChild);

        call.enqueue(new Callback<Horaris>() {
            @Override
            public void onResponse(Call<Horaris> call, Response<Horaris> response) {
                if(response.isSuccessful()){
                    if(response.body() != null){
                        wakeSleepList = response.body().wakeSleepList;
                        eventList = response.body().events;
                        RVadapter = new RV_Adapter(HorarisMainActivity.this,eventList);

                        RV_eventList.setAdapter(RVadapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<Horaris> call, Throwable t) {

            }
        });
    }

    private void setButtons(){
        BT_canviHorari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HorarisMainActivity.this,HorarisActivity.class);
                i.putExtra("wakeSleepLists",wakeSleepList);
                startActivityForResult(i,1);
            }
        });

        BT_acceptarHoraris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Horaris horaris = new Horaris();
                horaris.events = eventList;
                horaris.wakeSleepList = wakeSleepList;

                Call<String> call = mTodoService.postHoraris(idChild,horaris);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
            }
        });

        BT_esborrarEvent.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View view) {
                if(selectedEvent != null){
                    new AlertDialog.Builder(HorarisMainActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(getString(R.string.esborrar_event))
                            .setMessage(getString(R.string.esborrar_event_text,selectedEvent.name))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    boolean trobat = false;
                                    int count = 0;
                                    while(count < eventList.size() && !trobat){
                                        if(eventList.get(count).name.equals(selectedEvent.name)){
                                            trobat = true;
                                            eventList.remove(count);
                                        }
                                        else count++;
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.no),null)
                            .show();
                }
                else{
                    Toast.makeText(HorarisMainActivity.this,getString(R.string.no_event_selected),Toast.LENGTH_LONG);
                }
            }
        });

        BT_afegirEvent.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View view) {
                if(selectedEvent != null){
                    FragmentManager fm = getSupportFragmentManager();
                    HorarisEventFragment horarisEventFragment = HorarisEventFragment.newInstance(getString(R.string.events),selectedEvent);
                    horarisEventFragment.show(fm,"fragment_create_event");
                }
                else{
                    Toast.makeText(HorarisMainActivity.this,getString(R.string.no_event_selected),Toast.LENGTH_LONG);
                }
            }
        });

        BT_modificarEvent.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View view) {
                if(selectedEvent != null){
                    FragmentManager fm = getSupportFragmentManager();
                    HorarisEventFragment horarisEventFragment = HorarisEventFragment.newInstance(getString(R.string.events),selectedEvent);
                    horarisEventFragment.show(fm,"fragment_edit_event");
                }
                else{
                    Toast.makeText(HorarisMainActivity.this,getString(R.string.no_event_selected),Toast.LENGTH_LONG);
                }
            }
        });
    }

    private void setLayouts(){
        TV_horarisDormir = (TextView) findViewById(R.id.TV_horarisDormir);

        BT_canviHorari = (Button) findViewById(R.id.BT_canviHorari);
        BT_acceptarHoraris = (Button) findViewById(R.id.BT_acceptarHoraris);
        BT_modificarEvent = (Button) findViewById(R.id.BT_modificarEvent);
        BT_afegirEvent = (Button) findViewById(R.id.BT_afegirEvent);
        BT_esborrarEvent = (Button) findViewById(R.id.BT_esborrarEvent);

        RV_eventList = (RecyclerView) findViewById(R.id.RV_events);
        RV_eventList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setCurrentSleepTimes(){
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DATE,1);
        String avui = cal.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault());
        String dema = cal2.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault());

        String dormirAvui = "";
        String despertarDema = "";
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
            dormirAvui = wakeSleepList.sleep.monday;
            despertarDema = wakeSleepList.sleep.tuesday;
        }
        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY){
            dormirAvui = wakeSleepList.sleep.tuesday;
            despertarDema = wakeSleepList.sleep.wednesday;
        }
        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY){
            dormirAvui = wakeSleepList.sleep.wednesday;
            despertarDema = wakeSleepList.sleep.thursday;
        }
        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY){
            dormirAvui = wakeSleepList.sleep.thursday;
            despertarDema = wakeSleepList.sleep.friday;
        }
        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
            dormirAvui = wakeSleepList.sleep.friday;
            despertarDema = wakeSleepList.sleep.saturday;
        }
        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
            dormirAvui = wakeSleepList.sleep.saturday;
            despertarDema = wakeSleepList.sleep.sunday;
        }
        else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            dormirAvui = wakeSleepList.sleep.sunday;
            despertarDema = wakeSleepList.sleep.monday;
        }

        TV_horarisDormir.setText(getString(R.string.horari_actual,avui,dormirAvui,dema,despertarDema));
    }

    public class RV_Adapter extends RecyclerView.Adapter<RV_Adapter.MyViewHolder>{
        Context mContext;
        LayoutInflater mInflater;
        List<HorarisEvents> eventAdapterList;

        public class MyViewHolder extends RecyclerView.ViewHolder{
            protected View mRootView;
            TextView TV_eventName, TV_eventDays, TV_eventTimes;


            MyViewHolder(@NonNull View itemView){
                super(itemView);

                mRootView = itemView;

                TV_eventName = (TextView) itemView.findViewById(R.id.TV_eventName);
                TV_eventName.setSelected(true);
                TV_eventDays = (TextView) itemView.findViewById(R.id.TV_eventDays);
                TV_eventDays.setSelected(true);
                TV_eventTimes = (TextView) itemView.findViewById(R.id.TV_eventTimes);
                TV_eventTimes.setSelected(true);
            }
        }

        RV_Adapter(Context context, List<HorarisEvents> list){
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
            holder.itemView.setActivated(selectedEvent.name.equals(eventAdapterList.get(position).name));
            if(holder.itemView.isActivated()) holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.background_activity));
            else holder.itemView.setBackgroundColor(Color.TRANSPARENT);

            final HorarisEvents event = eventAdapterList.get(position);

            holder.TV_eventName.setText(event.name);
            String eventDaysString = "";
            for(int i : event.days){
                eventDaysString += new DateFormatSymbols().getWeekdays()[i] + " ";
            }
            holder.TV_eventDays.setText(eventDaysString);
            String eventTimesString = event.start+"\\n"+event.finish;
            holder.TV_eventTimes.setText(eventTimesString);

            holder.mRootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(selectedEvent.name.equals(event.name)){
                        selectedEvent = null;
                        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    }
                    else{
                        selectedEvent = event;
                        holder.itemView.setBackgroundColor(ContextCompat.getColor(mContext,R.color.background_activity));
                    }
                }
            });
        }

        @Override
        public int getItemViewType(int position) { return position; }

        @Override
        public int getItemCount() { return eventAdapterList.size(); }
    }
}
