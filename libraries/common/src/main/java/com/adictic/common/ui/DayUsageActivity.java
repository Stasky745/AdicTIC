package com.adictic.common.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.AppInfo;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.YearEntity;
import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayUsageActivity extends AppCompatActivity {
    // Constants defining order for display order
    private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
    private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
    private static final int _DISPLAY_ORDER_APP_NAME = 2;

    private final SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private Api mTodoService;

    private final String TAG = "DayUsageActivity";
    private long idChild;
    private Spinner SP_sort;
    private TextView TV_error;
    private TextView TV_dates;
    private int initialDay;
    private int initialMonth;
    private int initialYear;
    private int finalDay;
    private int finalMonth;
    private int finalYear;
    private RecyclerView listView;
    private Map<Integer, Map<Integer, List<Integer>>> daysMap;
    private List<Integer> yearList;
    private List<Integer> monthList;
    private int xDays;
    private UsageStatsAdapter mAdapter;

    private void setSpinner(){
        // Creem la llista dels elements
        List<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(getString(R.string.time_span));
        spinnerArray.add(getString(R.string.last_time_used));
        spinnerArray.add(getString(R.string.alfabeticament));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.support_simple_spinner_dropdown_item,
                spinnerArray);

        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        SP_sort.setAdapter(adapter);

        SP_sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = SP_sort.getSelectedItem().toString();
                if(selected.equals(getString(R.string.time_span)))
                    mAdapter.sortList(_DISPLAY_ORDER_USAGE_TIME);
                else if(selected.equals(getString(R.string.last_time_used)))
                    mAdapter.sortList(_DISPLAY_ORDER_LAST_TIME_USED);
                else
                    mAdapter.sortList(_DISPLAY_ORDER_APP_NAME);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        SP_sort.setSelection(0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage_stats_layout);
        mTodoService = ((App) getApplication()).getAPI();

        listView = findViewById(R.id.pkg_list);

        SP_sort = findViewById(R.id.typeSpinner);

        TV_dates = findViewById(R.id.TV_dates);

        TV_error = findViewById(R.id.TV_emptyList);
        TV_error.setVisibility(View.GONE);

        Button BT_pickDates = findViewById(R.id.BT_pickDates);
        BT_pickDates.setOnClickListener(view -> setupRangePickerDialog());

        daysMap = new HashMap<>();
        yearList = new ArrayList<>();
        monthList = new ArrayList<>();

        idChild = getIntent().getLongExtra("idChild", -1);

        int day = getIntent().getIntExtra("day", -1);
        if (day == -1) {
            Calendar cal = Calendar.getInstance();
            finalDay = initialDay = cal.get(Calendar.DAY_OF_MONTH);
            finalMonth = initialMonth = cal.get(Calendar.MONTH);
            finalYear = initialYear = cal.get(Calendar.YEAR);

            String text = formatterDate.format(cal.getTimeInMillis());
            TV_dates.setText(text);
        } else {
            finalDay = initialDay = day;
            finalMonth = initialMonth = getIntent().getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH));
            finalYear = initialYear = getIntent().getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR));

            Calendar calendar = Calendar.getInstance();
            calendar.set(finalYear, finalMonth, finalDay);
            String text = formatterDate.format(calendar.getTimeInMillis());
            TV_dates.setText(text);
        }

        getMonthYearLists();
    }

    private void getStats() {
        //checkFutureDates();
        String initialDate = getResources().getString(R.string.informal_date_format, initialDay, initialMonth + 1, initialYear);
        String finalDate = getResources().getString(R.string.informal_date_format, finalDay, finalMonth + 1, finalYear);

        Call<Collection<GeneralUsage>> call = mTodoService.getGenericAppUsage(idChild, initialDate, finalDate);

        call.enqueue(new Callback<Collection<GeneralUsage>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GeneralUsage>> call, @NonNull Response<Collection<GeneralUsage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Collection<GeneralUsage> generalUsages = response.body();
                    Funcions.canviarMesosDeServidor(generalUsages);
                    makeList(generalUsages);
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GeneralUsage>> call, @NonNull Throwable t) {
                showError();
            }
        });
    }

    private void makeList(Collection<GeneralUsage> gul) {
        xDays = gul.size();
        long totalTime = 0;

        // Si hi ha diferents dies amb les mateixes aplicacions, sumem els temps
        List<AppUsage> appList = new ArrayList<>();
        for (GeneralUsage gu : gul) {
            for (AppUsage au : gu.usage) {
                int index = appList.indexOf(au);

                totalTime += au.totalTime;

                if (index != -1) {
                    AppUsage current = appList.remove(index);
                    AppUsage res = new AppUsage();
                    res.app = new AppInfo();
                    res.app.appName = au.app.appName;
                    res.app.pkgName = au.app.pkgName;
                    res.totalTime = au.totalTime + current.totalTime;
                    if (current.lastTimeUsed > au.lastTimeUsed)
                        res.lastTimeUsed = current.lastTimeUsed;
                    else
                        res.lastTimeUsed = au.lastTimeUsed;

                    appList.add(res);
                } else {
                    appList.add(au);
                }
            }
        }

        // Actualitzem el TV
        updateTotalTimeTV(totalTime);

        mAdapter = new UsageStatsAdapter(appList, DayUsageActivity.this);
        listView.setAdapter(mAdapter);
        //setSpinner ha d'anar després de l'adapter
        setSpinner();
    }

    private void updateTotalTimeTV(long totalTime) {
        TextView TV_totalUse = findViewById(R.id.TV_totalUseVar);

        // Set colours according to total time spent
        if (totalTime <= xDays * Constants.CORRECT_USAGE_DAY)
            TV_totalUse.setTextColor(getColor(R.color.colorPrimary));
        else if (totalTime > xDays * Constants.DANGEROUS_USAGE_DAY)
            TV_totalUse.setTextColor(Color.RED);
        else
            TV_totalUse.setTextColor(Color.rgb(255, 128, 64));

        // Canviar format de HH:mm:ss a "Dies Hores Minuts"
        long elapsedDays = totalTime / Constants.TOTAL_MILLIS_IN_DAY;
        totalTime %= Constants.TOTAL_MILLIS_IN_DAY;

        long elapsedHours = totalTime / Constants.HOUR_IN_MILLIS;
        totalTime %= Constants.HOUR_IN_MILLIS;

        long elapsedMinutes = totalTime / (60*1000);

        String text;
        if (elapsedDays == 0) {
            if (elapsedHours == 0) {
                text = elapsedMinutes + getString(R.string.minutes);
            } else {
                text = elapsedHours + getString(R.string.hours) + elapsedMinutes + getString(R.string.minutes);
            }
        } else {
            text = elapsedDays + getString(R.string.days) + elapsedHours + getString(R.string.hours) + elapsedMinutes + getString(R.string.minutes);
        }
        TV_totalUse.setText(text);
    }

    private void setupRangePickerDialog(){
        Calendar initialDate = Calendar.getInstance();
        initialDate.set(initialYear,initialMonth,initialDay);
        long initialMillis = initialDate.getTimeInMillis();

        Calendar finalDate = Calendar.getInstance();
        finalDate.set(finalYear,finalMonth,finalDay);
        long finalMillis = finalDate.getTimeInMillis();

        MaterialDatePicker<Pair<Long, Long>> pickerRange = MaterialDatePicker.Builder.dateRangePicker()
                .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                .setCalendarConstraints(limitRange().build())
                .setTitleText(getString(R.string.choose_date_range))
                .setSelection(new Pair<>(initialMillis, finalMillis))
                .build();

        pickerRange.addOnPositiveButtonClickListener(selection -> {
            if(selection != null && selection.first != null && selection.second != null) {
                Calendar firstDate = Calendar.getInstance();
                firstDate.setTimeInMillis(selection.first);
                Calendar finalDate1 = Calendar.getInstance();
                finalDate1.setTimeInMillis(selection.second);

                initialDay = firstDate.get(Calendar.DAY_OF_MONTH);
                initialMonth = firstDate.get(Calendar.MONTH);
                initialYear = firstDate.get(Calendar.YEAR);

                finalDay = finalDate1.get(Calendar.DAY_OF_MONTH);
                finalMonth = finalDate1.get(Calendar.MONTH);
                finalYear = finalDate1.get(Calendar.YEAR);

                getStats();


                String text;
                if(firstDate.get(Calendar.DAY_OF_YEAR) == finalDate1.get(Calendar.DAY_OF_YEAR))
                    text = formatterDate.format(selection.first);
                else
                    text = formatterDate.format(selection.first) + " - " + formatterDate.format(selection.second);
                TV_dates.setText(text);
            }
        });
        pickerRange.show(getSupportFragmentManager(), pickerRange.toString());
    }

    private CalendarConstraints.Builder limitRange(){
        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
        Calendar calendarStart = Calendar.getInstance();

        int firstYear = Collections.min(yearList);
        int firstMonth = Collections.min(daysMap.get(firstYear).keySet());
        List<Integer> month = daysMap.get(firstYear).get(firstMonth);
        int firstDay = Collections.min(month);

        calendarStart.set(firstYear, firstMonth, firstDay);
        long startMillis = calendarStart.getTimeInMillis();
        long maxDateMillis = Calendar.getInstance().getTimeInMillis();

        constraintsBuilderRange.setStart(startMillis);
        constraintsBuilderRange.setEnd(maxDateMillis);

        CalendarConstraints.DateValidator dateValidator = new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                return !(startMillis > date || maxDateMillis < date);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel parcel, int i) {

            }
        };

        constraintsBuilderRange.setValidator(dateValidator);

        return constraintsBuilderRange;
    }

    public void getMonthYearLists() {
        Call<List<YearEntity>> call = mTodoService.getDaysWithData(idChild);

        call.enqueue(new Callback<List<YearEntity>>() {
            @Override
            public void onResponse(@NonNull Call<List<YearEntity>> call, @NonNull Response<List<YearEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    /* Agafem les dades de response i convertim en map **/
                    List<YearEntity> yEntityList = response.body();
                    Funcions.canviarMesosDeServidor(yEntityList);
                    if (yEntityList.isEmpty()) showError();
                    else {
                        daysMap = Funcions.convertYearEntityToMap(yEntityList);

                        yearList.addAll(daysMap.keySet());
                        yearList.sort(Collections.reverseOrder());

                        monthList.addAll(daysMap.get(yearList.get(0)).keySet());
                        monthList.sort(Collections.reverseOrder());

                        getStats();
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<YearEntity>> call, @NonNull Throwable t) {
                showError();
            }
        });
    }

    private void showError() {
        TV_error.setVisibility(View.VISIBLE);
        TV_error.setTextColor(Color.RED);
    }

    public static class AppNameComparator implements Comparator<AppUsage> {
        @Override
        public final int compare(AppUsage a, AppUsage b) {
            return a.app.appName.compareTo(b.app.appName);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<AppUsage> {
        @Override
        public final int compare(AppUsage a, AppUsage b) {
            // return by descending order
            return (int) (b.lastTimeUsed - a.lastTimeUsed);
        }
    }

    public static class UsageTimeComparator implements Comparator<AppUsage> {
        @Override
        public final int compare(AppUsage a, AppUsage b) {
            return (int) (b.totalTime - a.totalTime);
        }
    }

    class UsageStatsAdapter extends RecyclerView.Adapter<UsageStatsAdapter.MyViewHolder> {
        private final LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private final UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
        private final AppNameComparator mAppLabelComparator = new AppNameComparator();
        private final ArrayList<AppUsage> mPackageStats;
        private final Context mContext;
        //private final ArrayMap<String, Drawable> mIcons = new ArrayMap<>();
        private int mDisplayOrder;
        private final LayoutInflater mInflater;

        UsageStatsAdapter(List<AppUsage> appList, Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(c);
            mPackageStats = new ArrayList<>(appList);

            // Sort list
            sortList();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.usage_stats_item, parent,false);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            AppUsage pkgStats = mPackageStats.get(position);
            if (pkgStats != null && pkgStats.totalTime > Constants.HOUR_IN_MILLIS / 60) {
                Funcions.setIconDrawable(mContext, pkgStats.app.pkgName, holder.icon);
                String label = pkgStats.app.appName;
                holder.pkgName.setText(label);

                long now = Calendar.getInstance().getTimeInMillis();
                if(now - pkgStats.lastTimeUsed < Constants.TOTAL_MILLIS_IN_DAY)
                    holder.lastTimeUsed.setText(formatterTime.format(pkgStats.lastTimeUsed));
                else
                    holder.lastTimeUsed.setText(formatterDate.format(pkgStats.lastTimeUsed));
                // Change format from HH:dd:ss to "X Days Y Hours Z Minutes"
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long hoursInMilli = minutesInMilli * 60;
                long daysInMilli = hoursInMilli * 24;

                long totalTime = pkgStats.totalTime;

                long elapsedDays = totalTime / daysInMilli;
                totalTime %= daysInMilli;

                long elapsedHours = totalTime / hoursInMilli;
                totalTime %= hoursInMilli;

                long elapsedMinutes = totalTime / minutesInMilli;

                String time;
                if (elapsedDays == 0) {
                    if (elapsedHours == 0) {
                        time = elapsedMinutes + getString(R.string.minutes_tag);
                    } else {
                        time = elapsedHours + getString(R.string.hours_tag) + elapsedMinutes + getString(R.string.minutes_tag);
                    }
                } else {
                    time = elapsedDays + getString(R.string.days_tag) + elapsedHours + getString(R.string.hours_tag) + elapsedMinutes + getString(R.string.minutes_tag);
                }
                holder.usageTime.setText(time);

//                holder.usageTime.setText(
//                        DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
                double usageTimeInt = pkgStats.totalTime / (double) 3600000;

                if (usageTimeInt <= xDays * Constants.CORRECT_USAGE_APP)
                    holder.usageTime.setTextColor(getColor(R.color.colorPrimary));
                else if (usageTimeInt > xDays * Constants.DANGEROUS_USAGE_APP)
                    holder.usageTime.setTextColor(Color.RED);
                else holder.usageTime.setTextColor(Color.rgb(255, 128, 64));
            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() { return mPackageStats.size(); }

        public void sortList(int sortOrder) {
            if (mDisplayOrder == sortOrder) {
                notifyDataSetChanged();
                return;
            }
            mDisplayOrder = sortOrder;
            sortList();
        }

        private void sortList() {
            if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
                Log.i(TAG, "Sorting by usage time");
                mPackageStats.sort(mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
                Log.i(TAG, "Sorting by last time used");
                mPackageStats.sort(mLastTimeUsedComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                Log.i(TAG, "Sorting by application name");
                mPackageStats.sort(mAppLabelComparator);
            }
            notifyDataSetChanged();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView pkgName, lastTimeUsed, usageTime;
            ImageView icon;

            protected View mRootView;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);

                mRootView = itemView;

                pkgName = mRootView.findViewById(R.id.package_name);
                lastTimeUsed = mRootView.findViewById(R.id.last_time_used);
                usageTime = mRootView.findViewById(R.id.usage_time);
                icon = mRootView.findViewById(R.id.usage_icon);
            }
        }
    }

}
