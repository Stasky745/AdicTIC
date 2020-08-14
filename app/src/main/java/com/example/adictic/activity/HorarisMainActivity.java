package com.example.adictic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.WakeSleepLists;
import com.example.adictic.rest.TodoApi;

import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HorarisMainActivity extends AppCompatActivity {
    TodoApi mTodoService;
    long idChild;

    WakeSleepLists wakeSleepLists;

    TextView TV_horarisDormir;

    Button BT_canviHorari;
    Button BT_acceptarHoraris;
    Button BT_modificarEvent;
    Button BT_afegirEvent;
    Button BT_esborrarEvent;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.horaris_general_layout);
        mTodoService = ((TodoApp) getApplication()).getAPI();

        idChild = getIntent().getLongExtra("idChild",-1);

        getWakeSleepLists();
        setLayouts();
        setCurrentSleepTimes();
        setButtons();

    }

    private void getWakeSleepLists(){
        Call<WakeSleepLists> call = mTodoService.getHoraris(idChild);

        call.enqueue(new Callback<WakeSleepLists>() {
            @Override
            public void onResponse(Call<WakeSleepLists> call, Response<WakeSleepLists> response) {
                if(response.isSuccessful()){
                    if(response.body() != null){
                        wakeSleepLists = response.body();
                    }
                }
            }

            @Override
            public void onFailure(Call<WakeSleepLists> call, Throwable t) {

            }
        });
    }

    private void setButtons(){
        BT_canviHorari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HorarisMainActivity.this,HorarisActivity.class);
                i.putExtra("wakeSleepLists",wakeSleepLists);
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
    }

    private void setCurrentSleepTimes(){
        Calendar cal = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DATE,1);
        String avui = cal.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault());
        String dema = cal2.getDisplayName(Calendar.DAY_OF_WEEK,Calendar.LONG, Locale.getDefault());
        String dormirAvui = TodoApp.getSleepHoraris().get(cal.get(Calendar.DAY_OF_WEEK));
        String despertarDema = TodoApp.getSleepHoraris().get(cal2.get(Calendar.DAY_OF_WEEK));

        TV_horarisDormir.setText(getString(R.string.horari_actual,avui,dormirAvui,dema,despertarDema));
    }
}
