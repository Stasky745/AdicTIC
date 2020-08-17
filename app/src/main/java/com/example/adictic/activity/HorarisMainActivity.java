package com.example.adictic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.Horaris;
import com.example.adictic.entity.HorarisEvents;
import com.example.adictic.entity.WakeSleepLists;
import com.example.adictic.rest.TodoApi;

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

        getHoraris();
        setLayouts();
        setCurrentSleepTimes();
        setButtons();

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
                startActivity(i);
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
}
