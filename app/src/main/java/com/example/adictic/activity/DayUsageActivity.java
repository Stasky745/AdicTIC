package com.example.adictic.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayUsageActivity extends AppCompatActivity {

    TodoApi mTodoService;

    long idChild;

    ChipGroup chipGroup;
    Chip CH_singleDate;
    Chip CH_rangeDates;

    TextView TV_initialDate;
    TextView TV_finalDate;

    Button BT_initialDate;
    Button BT_finalDate;

    int initialDay;
    int initialMonth;
    int initialYear;

    int finalDay;
    int finalMonth;
    int finalYear;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage_stats_layout);
        mTodoService = ((TodoApp) getApplication()).getAPI();

        CH_singleDate = (Chip) findViewById(R.id.CH_singleDate);
        CH_rangeDates = (Chip) findViewById(R.id.CH_rangeDates);

        TV_initialDate = (TextView) findViewById(R.id.TV_initialDate);
        TV_finalDate = (TextView) findViewById(R.id.TV_finalDate);

        BT_initialDate = (Button) findViewById(R.id.BT_initialDate);
        BT_finalDate = (Button) findViewById(R.id.BT_finalDate);

        chipGroup = (ChipGroup) findViewById(R.id.CG_dateChips);

        idChild = getIntent().getLongExtra("idChild",-1);

        int day = getIntent().getIntExtra("day",-1);
        if(day==-1){
            Calendar cal = Calendar.getInstance();
            finalDay = initialDay = cal.get(Calendar.DAY_OF_MONTH);
            finalMonth = initialMonth = cal.get(Calendar.MONTH+1);
            finalYear = initialYear = cal.get(Calendar.YEAR);
        }
        else{
            finalDay = initialDay = day;
            finalMonth = initialMonth = getIntent().getIntExtra("month",Calendar.getInstance().get(Calendar.MONTH));
            finalYear = initialYear = getIntent().getIntExtra("year",Calendar.getInstance().get(Calendar.YEAR));
        }

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if(CH_singleDate.isChecked()){
                    BT_finalDate.setVisibility(View.INVISIBLE);
                    TV_finalDate.setVisibility(View.INVISIBLE);
                    TV_initialDate.setText(getResources().getString(R.string.date));
                    BT_initialDate.setText(getResources().getString(R.string.date_format,initialDay,getResources().getStringArray(R.array.month_names)[initialMonth],initialYear));

                    getStats();
                }
                else{
                    BT_finalDate.setVisibility(View.VISIBLE);
                    TV_finalDate.setVisibility(View.VISIBLE);
                    TV_initialDate.setText(getResources().getString(R.string.initial_date));
                    BT_initialDate.setText(getResources().getString(R.string.date_format,finalDay,getResources().getStringArray(R.array.month_names)[finalMonth],finalYear));

                    getStats();
                }
            }
        });

        chipGroup.clearCheck();
        chipGroup.check(CH_singleDate.getId());
    }

    private void getStats(){
        String initialDate = getResources().getString(R.string.informal_date_format,initialDay,initialMonth,initialYear);
        String finalDate = getResources().getString(R.string.informal_date_format,finalDay,finalMonth,finalYear);

        Call<Collection<GeneralUsage>> call = mTodoService.getGenericAppUsage(idChild,initialDate,finalDate);

        call.enqueue(new Callback<Collection<GeneralUsage>>() {
            @Override
            public void onResponse(Call<Collection<GeneralUsage>> call, Response<Collection<GeneralUsage>> response) {
                if(response.isSuccessful()){

                }
                else{

                }
            }

            @Override
            public void onFailure(Call<Collection<GeneralUsage>> call, Throwable t) {

            }
        });
    }
}
