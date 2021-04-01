package com.example.adictic.activity.informe;

import android.app.AlertDialog;
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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

        TV_percentageUsage.setOnClickListener(v -> {
            String deviceTime = TV_totalUsage.getText().toString().substring(0,TV_totalUsage.getText().toString().indexOf('/')-2);
            String totalTime = TV_totalUsage.getText().toString().substring(TV_totalUsage.getText().toString().indexOf('/')+2,TV_totalUsage.getText().length()-1);
            DecimalFormat decimalFormat = new DecimalFormat("###.##");

            AlertDialog dialog = new AlertDialog.Builder(InformeActivity.this).create();
            dialog.setMessage(getString(R.string.percentage_info,deviceTime,totalTime,decimalFormat.format(percentage)));
            dialog.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.ok), (dialog1, which) -> dialog1.dismiss());
            dialog.show();
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
        dateButton.setOnClickListener(v -> btnMonthYear());

        getMonthYearLists();
    }

    private void getAge(){
        Call<Integer> call = mTodoService.getAge(idChild);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if(response.isSuccessful() && response.body() != null){
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
        String dataInicial = (month+1) + "-" + year;
        Call<Collection<GeneralUsage>> call = mTodoService.getGenericAppUsage(idChild,dataInicial,dataInicial);
        call.enqueue(new Callback<Collection<GeneralUsage>>() {
            @Override
            public void onResponse(Call<Collection<GeneralUsage>> call, Response<Collection<GeneralUsage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Collection<GeneralUsage> generalUsages = response.body();
                    Funcions.canviarMesosDeServidor(generalUsages);
                    tabsAdapter.setGenericAppUsage(generalUsages);
                    viewPager.setAdapter(tabsAdapter);

                    setPercentages(generalUsages);
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<Collection<GeneralUsage>> call, Throwable t) {
                showError();
            }
        });
    }

    private void setPercentages(Collection<GeneralUsage> col){
        long totalTime = col.size() * 24L * 60 * 60 * 1000;
        long totalUsageTime = 0;
        for(GeneralUsage gu : col){
            totalUsageTime += gu.totalTime;
        }

        tabsAdapter.setTimes(totalTime, totalUsageTime);
        viewPager.setAdapter(tabsAdapter);

        percentage = totalUsageTime *100.0f/ totalTime;
        TV_percentageUsage.setText(getString(R.string.percentage,Math.round(percentage)));

        Pair<Integer,Integer> usagePair = Funcions.millisToString(totalUsageTime);
        Pair<Integer,Integer> totalTimePair = Funcions.millisToString(totalTime);


        String first;
        if(usagePair.first == 0){
            first = getString(R.string.mins, usagePair.second);
        }
        else{
            first = getString(R.string.hours_minutes, usagePair.first, usagePair.second);
        }
        String second = getString(R.string.hrs,totalTimePair.first);
        TV_totalUsage.setText(getString(R.string.comp,first,second));
    }

    private void showError(){
        viewPager.setVisibility(View.GONE);
        TV_error.setVisibility(View.VISIBLE);
    }

    public void btnMonthYear(){
        //Si no va, treu final
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(this,
                (selectedMonth, selectedYear) -> {
                    currentMonth = selectedMonth;
                    currentYear = selectedYear;
                    getStats(currentMonth,currentYear);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.MONTH,currentMonth);
                    String monthName = calendar.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault());
                    String buttonTag = monthName+" "+currentYear;
                    dateButton.setText(buttonTag);
                }, currentYear, currentMonth);

        monthList.clear();
        monthList.addAll(daysMap.get(yearList.get(0)).keySet());
        monthList.sort(Collections.reverseOrder());

        if(yearList.size()==1) builder.showMonthOnly();

        int minMonth = monthList.get(monthList.size()-1);
        int maxMonth = monthList.get(0);
        int startYear = yearList.get(yearList.size()-1);
        int endYear = yearList.get(0);

        builder .setActivatedMonth(currentMonth)
                .setActivatedYear(currentYear)
                .setTitle(getString(R.string.choose_month))
                .setMonthAndYearRange(minMonth, maxMonth, startYear, endYear)
                .setOnYearChangedListener(selectedYear -> { // on year selected
                    currentYear = selectedYear;
                    monthList.clear();
                    monthList.addAll(daysMap.get(currentYear).keySet());
                    monthList.sort(Collections.reverseOrder());
                    currentMonth = monthList.get(0);
                    builder.setActivatedMonth(currentMonth);
                    builder.setMonthRange(monthList.get(monthList.size()-1), currentMonth);
                })
                .build()
                .show();
    }

    private void getMonthYearLists(){
        Call<List<YearEntity>> call = mTodoService.getDaysWithData(idChild);

        call.enqueue(new Callback<List<YearEntity>>() {
            @Override
            public void onResponse(Call<List<YearEntity>> call, Response<List<YearEntity>> response) {
                if(response.isSuccessful() && response.body() != null){
                    /* Agafem les dades de response i convertim en map **/
                    List<YearEntity> yEntityList = response.body();
                    Funcions.canviarMesosAServidor(yEntityList);
                    if(yEntityList.isEmpty()) showError();
                    else {
                        daysMap = Funcions.convertYearEntityToMap(yEntityList);

                        yearList.addAll(daysMap.keySet());
                        yearList.sort(Collections.reverseOrder());

                        currentYear = yearList.get(0);

                        monthList.addAll(daysMap.get(currentYear).keySet());
                        monthList.sort(Collections.reverseOrder());

                        currentMonth = monthList.get(0);
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.MONTH,currentMonth);
                        String monthName = calendar.getDisplayName(Calendar.MONTH,Calendar.LONG, Locale.getDefault());
                        String buttonTag = monthName+" "+currentYear;
                        dateButton.setText(buttonTag);

                        getStats(currentMonth, currentYear);
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
