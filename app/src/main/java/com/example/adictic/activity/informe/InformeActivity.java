package com.example.adictic.activity.informe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.AppTimesAccessed;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.TimesAccessedDay;
import com.example.adictic.entity.YearEntity;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.google.android.material.tabs.TabLayout;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformeActivity extends AppCompatActivity {

    private TodoApi mTodoService;
    long idChild;

    private TabsAdapter tabsAdapter;
    private ViewPager viewPager;

    private List<Integer> yearList;
    private List<Integer> monthList;

    private int currentYear;
    private int currentMonth;

    private Button dateButton;

    private TextView TV_error;

    private Map<Integer, Map<Integer,List<Integer>>> daysMap;

    private long totalUsageTime, totalTime;

    private TextView TV_percentageUsage;
    private TextView TV_totalUsage;
    private double percentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informe_tabs);
        mTodoService = ((TodoApp) getApplication()).getAPI();

        idChild = getIntent().getLongExtra("idChild",-1);

        TV_error = (TextView) findViewById(R.id.TV_error);
        TV_percentageUsage = (TextView) findViewById(R.id.TV_usePercentage);
        TV_totalUsage = (TextView) findViewById(R.id.TV_deviceUsage);

        TV_percentageUsage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceTime = TV_totalUsage.getText().toString().substring(0,TV_totalUsage.getText().toString().indexOf('/')-2);
                String totalTime = TV_totalUsage.getText().toString().substring(TV_totalUsage.getText().toString().indexOf('/')+2,TV_totalUsage.getText().length()-1);
                DecimalFormat decimalFormat = new DecimalFormat("###.##");

                AlertDialog dialog = new AlertDialog.Builder(InformeActivity.this).create();
                dialog.setMessage(getResources().getString(R.string.percentage_info,deviceTime,totalTime,decimalFormat.format(percentage)));
                dialog.setButton(AlertDialog.BUTTON_POSITIVE,getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.VP_viewPager);
        tabsAdapter = new TabsAdapter(getSupportFragmentManager(),tabLayout.getTabCount());
        tabsAdapter.setChildId(idChild);

        getAge();
        getTimesBlockedMap();

        //viewPager.setAdapter(tabsAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position);
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        yearList = new ArrayList<>();
        monthList = new ArrayList<>();

        dateButton = (Button) findViewById (R.id.BT_monthPicker);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMonthYear();
            }
        });

        getMonthYearLists();
    }

    private void getAge(){
        Call<Integer> call = mTodoService.getAge(idChild);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.isSuccessful()){
                    tabsAdapter.setAge(response.body());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });
    }

    private void getTimesBlockedMap(){
        Call<List<AppTimesAccessed>> call = mTodoService.getAccessBlocked(idChild);
        call.enqueue(new Callback<List<AppTimesAccessed>>() {
            @Override
            public void onResponse(Call<List<AppTimesAccessed>> call, Response<List<AppTimesAccessed>> response) {
                if(response.isSuccessful() && response.body() != null) setTimesBlockedMap(new ArrayList<>(response.body()));
            }

            @Override
            public void onFailure(Call<List<AppTimesAccessed>> call, Throwable t) {

            }
        });
    }

    private void setTimesBlockedMap(ArrayList<AppTimesAccessed> list){
        Map<String,Long> map = new HashMap<>();
        for(AppTimesAccessed ata : list){
            String pkgName = ata.app;
            int times = 0;
            for(TimesAccessedDay tad : ata.times){
                times += tad.times;
            }
            map.put(pkgName, (long) times);
        }
        tabsAdapter.setTimesBlockedMap(map);
    }

    private void getStats(int month, int year){
        Call<Collection<GeneralUsage>> call = mTodoService.getGenericAppUsage(idChild,month+"-"+year,month+"-"+year);
        call.enqueue(new Callback<Collection<GeneralUsage>>() {
            @Override
            public void onResponse(Call<Collection<GeneralUsage>> call, Response<Collection<GeneralUsage>> response) {
                if (response.isSuccessful()) {
                    tabsAdapter.setGenericAppUsage(response.body());
                    viewPager.setAdapter(tabsAdapter);

                    setPercentages(response.body());
                } else {
                    System.out.println("3");
                    showError();
                }
            }

            @Override
            public void onFailure(Call<Collection<GeneralUsage>> call, Throwable t) {
                System.out.println("4");
                showError();
            }
        });
    }

    private void setPercentages(Collection<GeneralUsage> col){
        totalTime = col.size()*24*60*60*1000;
        totalUsageTime = 0;
        for(GeneralUsage gu : col){
            totalUsageTime += gu.totalTime;
        }

        tabsAdapter.setTimes(totalTime,totalUsageTime);
        viewPager.setAdapter(tabsAdapter);

        percentage = totalUsageTime*100.0f/totalTime;
        TV_percentageUsage.setText(getString(R.string.percentage,Math.round(percentage)));

        Pair<Integer,Integer> usagePair = Funcions.millisToString(totalUsageTime);
        Pair<Integer,Integer> totalTimePair = Funcions.millisToString(totalTime);

        if(usagePair.first == 0){
            String first = getString(R.string.mins,usagePair.second);
            String second = getString(R.string.hrs,totalTimePair.first);
            TV_totalUsage.setText(getString(R.string.comp,first,second));
        }
        else{
            String first = getString(R.string.hours_minutes,usagePair.first,usagePair.second);
            String second = getString(R.string.hrs,totalTimePair.first);
            TV_totalUsage.setText(getString(R.string.comp,first,second));
        }
    }

    private void showError(){
        viewPager.setVisibility(View.GONE);
        TV_error.setVisibility(View.VISIBLE);
    }

    public void btnMonthYear(){
        //Si no va, treu final
        final MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(this,
                new MonthPickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(int selectedMonth, int selectedYear) {
                        currentMonth = selectedMonth;
                        currentYear = selectedYear;
                        getStats(currentMonth+1,currentYear);
                        dateButton.setText(getResources().getStringArray(R.array.month_names)[currentMonth+1]+" "+currentYear);
                    }
                }, currentYear, currentMonth);

        monthList.addAll(daysMap.get(yearList.get(0)).keySet());
        Collections.sort(monthList,Collections.reverseOrder());

        if(yearList.size()==1) builder.showMonthOnly();

        builder .setActivatedMonth(currentMonth)
                .setActivatedYear(currentYear)
                .setTitle("Tria el mes per veure l'informe")
                .setMonthAndYearRange(monthList.get(monthList.size()-1)-1, monthList.get(0)-1, yearList.get(yearList.size()-1), yearList.get(0))
                .setOnYearChangedListener(new MonthPickerDialog.OnYearChangedListener() {
                    @Override
                    public void onYearChanged(int selectedYear) { // on year selected
                        monthList.addAll(daysMap.get(yearList.get(selectedYear)).keySet());
                        Collections.sort(monthList,Collections.reverseOrder());
                        builder.setMonthRange(monthList.get(monthList.size()-2), monthList.get(0)-1);

                    }
                })
                .build()
                .show();
    }

    private void getMonthYearLists(){
        Call<List<YearEntity>> call = mTodoService.getDaysWithData(idChild);

        call.enqueue(new Callback<List<YearEntity>>() {
            @Override
            public void onResponse(Call<List<YearEntity>> call, Response<List<YearEntity>> response) {
                if(response.isSuccessful()){
                    /** Agafem les dades de response i convertim en map **/
                    List<YearEntity> yEntityList = response.body();
                    if(yEntityList.isEmpty()) showError();
                    else {
                        daysMap = Funcions.convertYearEntityToMap(yEntityList);

                        yearList.addAll(daysMap.keySet());
                        Collections.sort(yearList, Collections.reverseOrder());

                        currentYear = yearList.get(0);

                        monthList.addAll(daysMap.get(yearList.get(0)).keySet());
                        Collections.sort(monthList, Collections.reverseOrder());

                        currentMonth = monthList.get(0) - 1;
                        dateButton.setText(getResources().getStringArray(R.array.month_names)[currentMonth + 1] + " " + currentYear);

                        getStats(currentMonth + 1, currentYear);
                    }
                }
                else{
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<YearEntity>> call, Throwable t) {
                showError();
            }
        });
    }
}
