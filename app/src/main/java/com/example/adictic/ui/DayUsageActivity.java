package com.example.adictic.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.entity.AppInfo;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.entity.YearEntity;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayUsageActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TodoApi mTodoService;

    String TAG = "DayUsageActivity";
    long idChild;
    ChipGroup chipGroup;
    Chip CH_singleDate;
    Chip CH_rangeDates;
    Spinner SP_sort;
    TextView TV_initialDate;
    TextView TV_finalDate;
    TextView TV_error;
    Button BT_initialDate;
    Button BT_finalDate;
    int initialDay;
    int initialMonth;
    int initialYear;
    int finalDay;
    int finalMonth;
    int finalYear;
    ListView listView;
    Map<Integer, Map<Integer, List<Integer>>> daysMap;
    List<Integer> yearList;
    List<Integer> monthList;
    int xDays;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private final DatePickerDialog.OnDateSetListener initialDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int year, int month, int day) {
            initialYear = year;
            initialMonth = month;
            initialDay = day;

            if (CH_singleDate.isChecked()) {
                finalYear = year;
                finalMonth = month;
                finalDay = day;
            } else checkFutureDates();

            getStats();

            BT_initialDate.setText(getResources().getString(R.string.date_format, initialDay, getResources().getStringArray(R.array.month_names)[initialMonth + 1], initialYear));
        }
    };
    private final DatePickerDialog.OnDateSetListener finalDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int year, int month, int day) {
            finalYear = year;
            finalMonth = month;
            finalDay = day;

            getStats();

            BT_finalDate.setText(getResources().getString(R.string.date_format, finalDay, getResources().getStringArray(R.array.month_names)[finalMonth + 1], finalYear));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usage_stats_layout);
        mTodoService = ((TodoApp) getApplication()).getAPI();

        listView = findViewById(R.id.pkg_list);

        SP_sort = findViewById(R.id.typeSpinner);

        SP_sort.setOnItemSelectedListener(this);

        CH_singleDate = findViewById(R.id.CH_singleDate);
        CH_rangeDates = findViewById(R.id.CH_rangeDates);

        TV_initialDate = findViewById(R.id.TV_initialDate);
        TV_finalDate = findViewById(R.id.TV_finalDate);

        TV_error = findViewById(R.id.TV_emptyList);
        TV_error.setVisibility(View.GONE);

        BT_initialDate = findViewById(R.id.BT_initialDate);
        BT_finalDate = findViewById(R.id.BT_finalDate);

        chipGroup = findViewById(R.id.CG_dateChips);


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
        } else {
            finalDay = initialDay = day;
            finalMonth = initialMonth = getIntent().getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH));
            finalYear = initialYear = getIntent().getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR));
        }

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (CH_singleDate.isChecked()) {
                BT_finalDate.setVisibility(View.INVISIBLE);
                TV_finalDate.setVisibility(View.INVISIBLE);
                TV_initialDate.setText(getResources().getString(R.string.date));
                BT_initialDate.setText(getResources().getString(R.string.date_format, initialDay, getResources().getStringArray(R.array.month_names)[initialMonth + 1], initialYear));

                finalDay = initialDay;
                finalMonth = initialMonth;
                finalYear = initialYear;

            } else {
                BT_finalDate.setVisibility(View.VISIBLE);
                TV_finalDate.setVisibility(View.VISIBLE);
                TV_initialDate.setText(getResources().getString(R.string.initial_date));
                BT_initialDate.setText(getResources().getString(R.string.date_format, initialDay, getResources().getStringArray(R.array.month_names)[initialMonth + 1], initialYear));
                BT_finalDate.setText(getResources().getString(R.string.date_format, finalDay, getResources().getStringArray(R.array.month_names)[finalMonth + 1], finalYear));

            }
            getStats();
        });

        chipGroup.setSelectionRequired(false);
        chipGroup.clearCheck();
        chipGroup.check(CH_singleDate.getId());
        chipGroup.setSelectionRequired(true);

        getMonthYearLists();
    }

    private void getStats() {
        checkFutureDates();
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

        List<AppUsage> appList = new ArrayList<>();
        for (GeneralUsage gu : gul) {
            for (AppUsage au : gu.usage) {
                int index = appList.indexOf(au);
                if (index != -1) {
                    AppUsage current = appList.remove(index);
                    AppUsage res = new AppUsage();
                    res.app = new AppInfo();
                    res.app.appName = au.app.appName;
                    res.app.pkgName = au.app.pkgName;
                    res.totalTime = au.totalTime + current.totalTime;
                    if (current.lastTimeUsed > au.lastTimeUsed)
                        res.lastTimeUsed = current.lastTimeUsed;
                    else res.lastTimeUsed = au.lastTimeUsed;

                    appList.add(res);
                } else {
                    appList.add(au);
                }
            }
        }

        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mAdapter = new UsageStatsAdapter(appList, getApplicationContext());
        listView.setAdapter(mAdapter);
    }

    public void btnInitialDate(View view) {
        DatePickerDialog initialPicker = new DatePickerDialog(this, R.style.datePicker, initialDateListener, initialYear, initialMonth, initialDay);
        initialPicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());

        int firstYear = Collections.min(yearList);
        int firstMonth = Collections.min(daysMap.get(firstYear).keySet());
        List<Integer> month = daysMap.get(firstYear).get(firstMonth);
        int firstDay = Collections.min(month);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, firstDay);
        cal.set(Calendar.MONTH, firstMonth);
        cal.set(Calendar.YEAR, firstYear);

        initialPicker.getDatePicker().setMinDate(cal.getTimeInMillis());
        initialPicker.show();
    }

    public void btnFinalDate(View view) {
        DatePickerDialog finalPicker = new DatePickerDialog(this, R.style.datePicker, finalDateListener, finalYear, finalMonth, finalDay);
        finalPicker.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, initialDay);
        cal.set(Calendar.MONTH, initialMonth);
        cal.set(Calendar.YEAR, initialYear);

        finalPicker.getDatePicker().setMinDate(cal.getTimeInMillis());

        finalPicker.show();
    }

    private void checkFutureDates() {
        if (initialYear > finalYear) {
            finalYear = initialYear;
            finalMonth = initialMonth;
            finalDay = initialDay;

            BT_finalDate.setText(getResources().getString(R.string.date_format, finalDay, getResources().getStringArray(R.array.month_names)[finalMonth + 1], finalYear));
        } else if (initialYear == finalYear) {
            if (initialMonth > finalMonth) {
                finalMonth = initialMonth;
                finalDay = initialDay;

                BT_finalDate.setText(getResources().getString(R.string.date_format, finalDay, getResources().getStringArray(R.array.month_names)[finalMonth + 1], finalYear));
            } else if (initialMonth == finalMonth) {
                if (initialDay > finalDay) {
                    finalDay = initialDay;

                    BT_finalDate.setText(getResources().getString(R.string.date_format, finalDay, getResources().getStringArray(R.array.month_names)[finalMonth + 1], finalYear));
                }
            }
        }
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mAdapter != null) mAdapter.sortList(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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

    // View Holder used when displaying views
    static class AppViewHolder {
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
        ImageView icon;
    }

    class UsageStatsAdapter extends BaseAdapter {
        // Constants defining order for display order
        private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
        private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
        private static final int _DISPLAY_ORDER_APP_NAME = 2;
        private final LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private final UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
        private final AppNameComparator mAppLabelComparator;
        private final ArrayList<AppUsage> mPackageStats = new ArrayList<>();
        private final Context mContext;
        //private final ArrayMap<String, Drawable> mIcons = new ArrayMap<>();
        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private long totalTime = 0;

        UsageStatsAdapter(List<AppUsage> appList, Context c) {
            mContext = c;
            for (AppUsage app : appList) {
                //Drawable appIcon = null;
                //appIcon = getPackageManager().getApplicationIcon(app.pkgName);
                totalTime = totalTime + app.totalTime;

                int index = mPackageStats.indexOf(app);
                if (index == -1) {
                    mPackageStats.add(app);
                } else {
                    AppUsage newApp = mPackageStats.remove(index);
                    newApp.totalTime += app.totalTime;
                    if (app.lastTimeUsed > newApp.lastTimeUsed)
                        newApp.lastTimeUsed = app.lastTimeUsed;

                    mPackageStats.add(newApp);
                }
            }

            TextView TV_totalUse = findViewById(R.id.TV_totalUseVar);

            // Set colours according to total time spent
            if (totalTime <= xDays * TodoApp.CORRECT_USAGE_DAY)
                TV_totalUse.setTextColor(Color.GREEN);
            else if (totalTime > xDays * TodoApp.DANGEROUS_USAGE_DAY)
                TV_totalUse.setTextColor(Color.RED);
            else TV_totalUse.setTextColor(Color.rgb(255, 128, 64));

            // Change format from HH:dd:ss to "X Days Y Hours Z Minutes"
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = totalTime / daysInMilli;
            totalTime = totalTime % daysInMilli;

            long elapsedHours = totalTime / hoursInMilli;
            totalTime = totalTime % hoursInMilli;

            long elapsedMinutes = totalTime / minutesInMilli;
            totalTime = totalTime % minutesInMilli;

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

            // Sort list
            mAppLabelComparator = new AppNameComparator();
            sortList();
        }

        @Override
        public int getCount() {
            return mPackageStats.size();
        }

        @Override
        public Object getItem(int position) {
            return mPackageStats.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            AppViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.usage_stats_item, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new AppViewHolder();
                holder.pkgName = convertView.findViewById(R.id.package_name);
                holder.lastTimeUsed = convertView.findViewById(R.id.last_time_used);
                holder.usageTime = convertView.findViewById(R.id.usage_time);

                holder.icon = convertView.findViewById(R.id.usage_icon);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (AppViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder
            AppUsage pkgStats = mPackageStats.get(position);
            if (pkgStats != null) {
                Funcions.setIconDrawable(mContext, pkgStats.app.pkgName, holder.icon);
                String label = pkgStats.app.appName;
                holder.pkgName.setText(label);
                holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.lastTimeUsed,
                        System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
                // Change format from HH:dd:ss to "X Days Y Hours Z Minutes"
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long hoursInMilli = minutesInMilli * 60;
                long daysInMilli = hoursInMilli * 24;

                totalTime = pkgStats.totalTime;

                long elapsedDays = totalTime / daysInMilli;
                totalTime = totalTime % daysInMilli;

                long elapsedHours = totalTime / hoursInMilli;
                totalTime = totalTime % hoursInMilli;

                long elapsedMinutes = totalTime / minutesInMilli;
                totalTime = totalTime % minutesInMilli;

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

                if (usageTimeInt <= xDays * TodoApp.CORRECT_USAGE_APP)
                    holder.usageTime.setTextColor(Color.GREEN);
                else if (usageTimeInt > xDays * TodoApp.DANGEROUS_USAGE_APP)
                    holder.usageTime.setTextColor(Color.RED);
                else holder.usageTime.setTextColor(Color.rgb(255, 128, 64));
                //holder.icon.setImageDrawable(mIcons.get(pkgStats.getPackageName()));
            } else {
                Log.w(TAG, "No usage stats info for package:" + position);
            }
            return convertView;
        }

        void sortList(int sortOrder) {
            if (mDisplayOrder == sortOrder) {
                // do nothing
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
    }

}
