package com.example.adictic.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DayUsageActivity extends AppCompatActivity {

    TodoApi mTodoService;

    String TAG = "DayUsageActivity";

    private LayoutInflater mInflater;

    long idChild;

    private UsageStatsAdapter mAdapter;

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

    int xDays;

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
            finalMonth = initialMonth = cal.get(Calendar.MONTH)+1;
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
                    makeList(response.body());
                }
                else{

                }
            }

            @Override
            public void onFailure(Call<Collection<GeneralUsage>> call, Throwable t) {

            }
        });
    }

    private void makeList(Collection<GeneralUsage> gul){
        List<AppUsage> appList = new ArrayList<>();
        for(GeneralUsage gu : gul){
            for(AppUsage au : gu.usage){
                int index = appList.indexOf(au);
                if(index!=-1){
                    AppUsage current = appList.remove(index);
                    AppUsage res = new AppUsage();
                    res.appTitle = au.appTitle;
                    res.pkgName = au.appTitle;
                    res.totalTime = au.totalTime + current.totalTime;
                    if(current.lastTimeUsed > au.lastTimeUsed) res.lastTimeUsed = current.lastTimeUsed;
                    else res.lastTimeUsed = au.lastTimeUsed;

                    appList.add(res);
                }
                else{
                    appList.add(au);
                }
            }
        }

        xDays = gul.size();

        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ListView listView = findViewById(R.id.pkg_list);
        mAdapter = new UsageStatsAdapter(appList);
        listView.setAdapter(mAdapter);
    }

    public static class AppNameComparator implements Comparator<AppUsage> {

        @Override
        public final int compare(AppUsage a, AppUsage b) {
            return a.appTitle.compareTo(b.appTitle);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<AppUsage> {
        @Override
        public final int compare(AppUsage a, AppUsage b) {
            // return by descending order
            return (int)(b.lastTimeUsed - a.lastTimeUsed);
        }
    }

    public static class UsageTimeComparator implements Comparator<AppUsage> {
        @Override
        public final int compare(AppUsage a, AppUsage b) {
            return (int)(b.totalTime - a.totalTime);
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

        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
        private AppNameComparator mAppLabelComparator;
        private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
        private final ArrayList<AppUsage> mPackageStats = new ArrayList<>();
        private final ArrayMap<String, Drawable> mIcons = new ArrayMap<>();

        private long totalTime = 0;

        UsageStatsAdapter(List<AppUsage> appList) {
            HashMap<String, AppUsage> map = new HashMap<>();
            for (AppUsage app : appList) {
                Drawable appIcon = null;
                //ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                //appIcon = getPackageManager().getApplicationIcon(app.pkgName);
                String label = app.appTitle;
                mAppLabelMap.put(app.pkgName, label);

                totalTime = totalTime + app.totalTime;

            }
            mPackageStats.addAll(map.values());

            TextView TV_totalUse = findViewById(R.id.TV_totalUseVar);

            // Set colours according to total time spent
            if(totalTime <= xDays*TodoApp.CORRECT_USAGE_DAY) TV_totalUse.setTextColor(Color.GREEN);
            else if(totalTime > xDays*TodoApp.DANGEROUS_USAGE_DAY) TV_totalUse.setTextColor(Color.RED);
            else TV_totalUse.setTextColor(Color.rgb(255,128,64));

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

            if(elapsedDays == 0){
                if(elapsedHours == 0){
                    TV_totalUse.setText(elapsedMinutes + getString(R.string.minutes));
                }
                else{
                    TV_totalUse.setText(elapsedHours + getString(R.string.hours) + elapsedMinutes + getString(R.string.minutes));
                }
            }
            else{
                TV_totalUse.setText(elapsedDays + getString(R.string.days) + elapsedHours + getString(R.string.hours) + elapsedMinutes + getString(R.string.minutes));
            }

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
                String label = mAppLabelMap.get(pkgStats.pkgName);
                holder.pkgName.setText(label);
                holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.lastTimeUsed,
                        System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));
//                holder.lastTimeUsed.setText(((Long)pkgStats.getLastTimeUsed()).toString());
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

                if(elapsedDays == 0){
                    if(elapsedHours == 0){
                        holder.usageTime.setText(elapsedMinutes + getString(R.string.minutes_tag));
                    }
                    else{
                        holder.usageTime.setText(elapsedHours + getString(R.string.hours_tag) + elapsedMinutes + getString(R.string.minutes_tag));
                    }
                }
                else{
                    holder.usageTime.setText(elapsedDays + getString(R.string.days_tag) + elapsedHours + getString(R.string.hours_tag) + elapsedMinutes + getString(R.string.minutes_tag));
                }

//                holder.usageTime.setText(
//                        DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
                Double usageTimeInt = pkgStats.totalTime/(double)3600000;

                if(usageTimeInt <= xDays*TodoApp.CORRECT_USAGE_APP) holder.usageTime.setTextColor(Color.GREEN);
                else if(usageTimeInt > xDays*TodoApp.DANGEROUS_USAGE_APP) holder.usageTime.setTextColor(Color.RED);
                else holder.usageTime.setTextColor(Color.rgb(255,128,64));
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
            mDisplayOrder= sortOrder;
            sortList();
        }
        private void sortList() {
            if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
                Log.i(TAG, "Sorting by usage time");
                Collections.sort(mPackageStats, mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
                Log.i(TAG, "Sorting by last time used");
                Collections.sort(mPackageStats, mLastTimeUsedComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                Log.i(TAG, "Sorting by application name");
                Collections.sort(mPackageStats, mAppLabelComparator);
            }
            notifyDataSetChanged();
        }
    }

}
