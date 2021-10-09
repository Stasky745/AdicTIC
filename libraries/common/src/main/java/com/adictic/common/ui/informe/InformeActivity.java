package com.adictic.common.ui.informe;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.adictic.common.R;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.IntentsAccesApp;
import com.adictic.common.entity.YearEntity;
import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;
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
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class InformeActivity extends AppCompatActivity {

    private long idChild;
    private int age;
    private Api mTodoService;
    private TabsAdapter tabsAdapter;
    private ViewPager2 viewPager;

    private List<Integer> yearList;
    private List<Integer> monthList;

    private int currentYear;
    private int currentMonth;

    private Button dateButton;

    private TextView TV_error;

    private Map<Integer, Map<Integer, List<Integer>>> daysMap;

    private TextView TV_percentageUsage;
    private TextView TV_totalUsage;
    private double percentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.informe_tabs);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.informe));
        mTodoService = ((App) getApplication()).getAPI();

        idChild = getIntent().getLongExtra("idChild", -1);

        TV_error = findViewById(R.id.TV_error);
        TV_percentageUsage = findViewById(R.id.TV_usePercentage);
        TV_totalUsage = findViewById(R.id.TV_deviceUsage);

        TV_percentageUsage.setOnClickListener(v -> {
            String deviceTime = TV_totalUsage.getText().toString().substring(0, TV_totalUsage.getText().toString().indexOf('/') - 2);
            DecimalFormat decimalFormat = new DecimalFormat("###.##");

            AlertDialog dialog = new AlertDialog.Builder(InformeActivity.this).create();
            if(percentage < 100)
                dialog.setMessage(Funcions.getSpannedText(getApplicationContext(), getString(R.string.percentage_info_bo, deviceTime, Constants.AGE_TIMES_STRING[age], decimalFormat.format(percentage))));
            else
                dialog.setMessage(Funcions.getSpannedText(getApplicationContext(), getString(R.string.percentage_info_dolent, deviceTime, Constants.AGE_TIMES_STRING[age], decimalFormat.format(percentage))));
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), (dialog1, which) -> dialog1.dismiss());
            dialog.show();
        });

        final TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = findViewById(R.id.VP_viewPager);
        tabsAdapter = new TabsAdapter(getSupportFragmentManager(), getLifecycle());
        tabsAdapter.setChildId(idChild);

        getAge();
        getTimesBlockedMap();

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

        dateButton = findViewById(R.id.BT_monthPicker);
        dateButton.setOnClickListener(v -> {
            if (yearList.size() == 1) {
                currentYear = yearList.get(0);
                btnMonth();
            } else {
                btnYear();
            }
        });

        getMonthYearLists();
    }

    private void getAge() {
        Call<Integer> call = mTodoService.getAge(idChild);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                    super.onResponse(call, response);
                if (response.isSuccessful() && response.body() != null) {
                    age = response.body();

                    /* Assegurem que l'edat no surt de rang **/
                    age = Math.min(Math.abs(age), 29);

                    tabsAdapter.setAge(age);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                    super.onFailure(call, t);

            }
        });
    }

    private void getTimesBlockedMap() {
        Call<List<IntentsAccesApp>> call = mTodoService.getIntentAccesApp(idChild);
        call.enqueue(new Callback<List<IntentsAccesApp>>() {
            @Override
            public void onResponse(@NonNull Call<List<IntentsAccesApp>> call, @NonNull Response<List<IntentsAccesApp>> response) {
                super.onResponse(call, response);
                if (response.isSuccessful() && response.body() != null)
                    setTimesBlockedMap(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<List<IntentsAccesApp>> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    private void setTimesBlockedMap(List<IntentsAccesApp> list) {
        Map<String, Long> map = new HashMap<>();
        for (IntentsAccesApp ata : list) {
            Long times = map.getOrDefault(ata.pkgName, (long)0);
            map.put(ata.pkgName, ++times);
        }
        tabsAdapter.setTimesBlockedMap(map);
    }

    private void getStats(int month, int year) {
        String dataInicial = (month + 1) + "-" + year;
        Call<Collection<GeneralUsage>> call = mTodoService.getGenericAppUsage(idChild, dataInicial, dataInicial);
        call.enqueue(new Callback<Collection<GeneralUsage>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GeneralUsage>> call, @NonNull Response<Collection<GeneralUsage>> response) {
                    super.onResponse(call, response);
                if (response.isSuccessful() && response.body() != null) {
                    Collection<GeneralUsage> generalUsages = response.body();
                    Funcions.canviarMesosDeServidor(generalUsages);
                    tabsAdapter.setGenericAppUsage(generalUsages);
                    viewPager.setAdapter(tabsAdapter);
                    for(int i=0; i < viewPager.getChildCount(); i++){
                        viewPager.getChildAt(i).setOverScrollMode(View.OVER_SCROLL_NEVER);
                    }

                    setPercentages(generalUsages);
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GeneralUsage>> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                showError();
            }
        });
    }

    private void setPercentages(Collection<GeneralUsage> col) {
        long totalUsageTime = 0;
        for (GeneralUsage gu : col) {
            totalUsageTime += gu.totalTime;
        }

        tabsAdapter.setTimes(totalUsageTime);
        viewPager.setAdapter(tabsAdapter);
        for(int i=0; i < viewPager.getChildCount(); i++){
            viewPager.getChildAt(i).setOverScrollMode(View.OVER_SCROLL_NEVER);
        }

        long totalRecomanat = col.size() * Constants.AGE_TIMES_MILLIS[age];
        percentage = totalUsageTime * 100.0f / totalRecomanat;
        if(percentage>100)
            TV_percentageUsage.setTextColor(getColor(R.color.vermell));
        TV_percentageUsage.setText(getString(R.string.percentage, Math.round(percentage)));

        Pair<Integer, Integer> usagePair = Funcions.millisToString(totalUsageTime);
        Pair<Integer, Integer> totalTimePair = Funcions.millisToString(totalRecomanat);

        String first, second;
        if (usagePair.first == 0)
            first = getString(R.string.mins, usagePair.second);
        else
            first = getString(R.string.hours_minutes, usagePair.first, usagePair.second);

        if (totalTimePair.second == 0)
            second = getString(R.string.hrs, totalTimePair.first);
        else
            second = getString(R.string.hours_minutes, totalTimePair.first, totalTimePair.second); //Pot ser que s'hagi de posar 30 com a valor predefinit en els minuts

        TV_totalUsage.setText(getString(R.string.comp, first, second));
    }

    private void showError() {
        viewPager.setVisibility(View.GONE);
        TV_error.setVisibility(View.VISIBLE);
    }

    private void btnYear() {
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(this,
                (selectedMonth, selectedYear) -> {
                    currentYear = selectedYear;
                    monthList.clear();
                    monthList.addAll(Objects.requireNonNull(daysMap.get(currentYear)).keySet());
                    monthList.sort(Collections.reverseOrder());
                    currentMonth = Collections.min(monthList);

                    setButtonText();

                    btnMonth();
                }, currentYear, currentMonth);

        if (yearList.size() == 1) builder.showMonthOnly();

        int startYear = Collections.min(yearList);
        int endYear = Collections.max(yearList);

        builder.showYearOnly()
                .setActivatedYear(currentYear)
                .setTitle(getString(R.string.choose_year))
                .setYearRange(startYear, endYear)
                .build()
                .show();
    }

    private void setButtonText() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, currentMonth);
        String monthName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG_STANDALONE, Locale.getDefault());
        else
            monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());

        String buttonTag = monthName + " " + currentYear;
        dateButton.setText(buttonTag);
    }

    private void btnMonth() {
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(this,
                (selectedMonth, selectedYear) -> {
                    currentMonth = selectedMonth;
                    getStats(currentMonth, currentYear);
                    setButtonText();
                }, currentYear, currentMonth);

        int minMonth = Collections.min(monthList);
        int maxMonth = Collections.max(monthList);

        builder.showMonthOnly()
                .setActivatedMonth(currentMonth)
                .setTitle(getString(R.string.choose_month))
                .setMonthRange(minMonth, maxMonth)
                .build()
                .show();
    }

    private void getMonthYearLists() {
        Call<List<YearEntity>> call = mTodoService.getDaysWithData(idChild);

        call.enqueue(new Callback<List<YearEntity>>() {
            @Override
            public void onResponse(@NonNull Call<List<YearEntity>> call, @NonNull Response<List<YearEntity>> response) {
                    super.onResponse(call, response);
                if (response.isSuccessful() && response.body() != null) {
                    /* Agafem les dades de response i convertim en map **/
                    List<YearEntity> yEntityList = response.body();
                    Funcions.canviarMesosDeServidor(yEntityList);
                    if (yEntityList.isEmpty()) showError();
                    else {
                        daysMap = Funcions.convertYearEntityToMap(yEntityList);

                        yearList.addAll(daysMap.keySet());
                        yearList.sort(Collections.reverseOrder());

                        currentYear = Collections.max(yearList);

                        monthList.addAll(Objects.requireNonNull(daysMap.get(currentYear)).keySet());
                        monthList.sort(Collections.reverseOrder());

                        currentMonth = Collections.max(monthList);

                        setButtonText();

                        getStats(currentMonth, currentYear);
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<YearEntity>> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                showError();
            }
        });
    }
}
