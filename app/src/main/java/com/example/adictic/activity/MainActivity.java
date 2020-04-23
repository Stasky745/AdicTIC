package com.example.adictic.activity;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.FillNom;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {
    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private GeneralUsageStatsAdapter mAdapter2;
    private PackageManager mPm;
    private int xDays = 1;
    private TodoApi mTodoService;

    private float CORRECT_USAGE_APP = 2;
    private float DANGEROUS_USAGE_APP = 4;

    private float CORRECT_USAGE_DAY = 3;
    private float DANGEROUS_USAGE_DAY = 6;

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            // return by descending order
            return (int)(b.getLastTimeUsed() - a.getLastTimeUsed());
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int)(b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
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
        private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();
        private final ArrayMap<String, Drawable> mIcons = new ArrayMap<>();

        private long totalTime = 0;

        UsageStatsAdapter() {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -xDays);
            cal.set(Calendar.HOUR_OF_DAY,23);
            cal.set(Calendar.MINUTE,59);
            cal.set(Calendar.SECOND,59);

            Calendar cal2 = Calendar.getInstance();
            cal2.add(Calendar.DAY_OF_YEAR, -xDays);
            cal2.set(Calendar.HOUR_OF_DAY,0);
            cal2.set(Calendar.MINUTE,0);
            cal2.set(Calendar.SECOND,0);

//            final List<UsageStats> stats =
//                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
//                            cal.getTimeInMillis(), System.currentTimeMillis());
//            if (stats == null) {
//                return;
//            }

            final List<UsageStats> stats =
                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                            cal2.getTimeInMillis(), cal.getTimeInMillis());
            if (stats == null) {
                return;
            }

            ArrayMap<String, UsageStats> map = new ArrayMap<>();
            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                final android.app.usage.UsageStats pkgStats = stats.get(i);

                // load application labels for each application
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    if(pkgStats.getTotalTimeInForeground()>5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        Drawable appIcon = getPackageManager().getApplicationIcon(pkgStats.getPackageName());
                        String label = appInfo.loadLabel(mPm).toString();
                        mAppLabelMap.put(pkgStats.getPackageName(), label);

                        totalTime = totalTime + pkgStats.getTotalTimeInForeground();

                        UsageStats existingStats =
                                map.get(pkgStats.getPackageName());
                        if (existingStats == null) {
                            map.put(pkgStats.getPackageName(), pkgStats);
                            mIcons.put(pkgStats.getPackageName(), appIcon);
                        } else {
                            existingStats.add(pkgStats);
                        }
                    }

                } catch (NameNotFoundException e) {
                    // This package may be gone.
                }
            }
            mPackageStats.addAll(map.values());

            TextView TV_totalUse = findViewById(R.id.TV_totalUseVar);

            // Set colours according to total time spent
            if(totalTime <= xDays*CORRECT_USAGE_DAY) TV_totalUse.setTextColor(Color.GREEN);
            else if(totalTime > xDays*DANGEROUS_USAGE_DAY) TV_totalUse.setTextColor(Color.RED);
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
            mAppLabelComparator = new AppNameComparator(mAppLabelMap);
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
            UsageStats pkgStats = mPackageStats.get(position);
            if (pkgStats != null) {
                String label = mAppLabelMap.get(pkgStats.getPackageName());
                holder.pkgName.setText(label);
                holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(),
                        System.currentTimeMillis(), DateFormat.MEDIUM, DateFormat.MEDIUM));

                // Change format from HH:dd:ss to "X Days Y Hours Z Minutes"
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long hoursInMilli = minutesInMilli * 60;
                long daysInMilli = hoursInMilli * 24;

                totalTime = pkgStats.getTotalTimeInForeground();

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
                Double usageTimeInt = pkgStats.getTotalTimeInForeground()/(double)3600000;

                if(usageTimeInt <= xDays*CORRECT_USAGE_APP) holder.usageTime.setTextColor(Color.GREEN);
                else if(usageTimeInt > xDays*DANGEROUS_USAGE_APP) holder.usageTime.setTextColor(Color.RED);
                else holder.usageTime.setTextColor(Color.rgb(255,128,64));
                holder.icon.setImageDrawable(mIcons.get(pkgStats.getPackageName()));
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
                if (localLOGV) Log.i(TAG, "Sorting by usage time");
                Collections.sort(mPackageStats, mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
                if (localLOGV) Log.i(TAG, "Sorting by last time used");
                Collections.sort(mPackageStats, mLastTimeUsedComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                if (localLOGV) Log.i(TAG, "Sorting by application name");
                Collections.sort(mPackageStats, mAppLabelComparator);
            }
            notifyDataSetChanged();
        }
    }

    class GeneralUsageStatsAdapter extends BaseAdapter {
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

        GeneralUsageStatsAdapter(List<GeneralUsage> stats) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -xDays);

            if (stats == null) {
                return;
            }

            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                for(int j = 0; j < stats.get(i).usage.size(); j++){
                    final AppUsage pkgStats = (AppUsage)((List)stats.get(i).usage).get(j);

                    // load application labels for each application
                    try {
                        ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.appName, 0);
                        if(pkgStats.totalTime>5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            Drawable appIcon = getPackageManager().getApplicationIcon(pkgStats.appName);
                            String label = appInfo.loadLabel(mPm).toString();
                            mAppLabelMap.put(pkgStats.appName, label);

                            totalTime = totalTime + pkgStats.totalTime;
                            mPackageStats.add(pkgStats);
                            mIcons.put(pkgStats.appName,appIcon);
                        }

                    } catch (NameNotFoundException e) {
                        // This package may be gone.
                    }
                }
            }

            TextView TV_totalUse = findViewById(R.id.TV_totalUseVar);

            // Set colours according to total time spent
            if(totalTime <= xDays*CORRECT_USAGE_DAY) TV_totalUse.setTextColor(Color.GREEN);
            else if(totalTime > xDays*DANGEROUS_USAGE_DAY) TV_totalUse.setTextColor(Color.RED);
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
            //mAppLabelComparator = new AppNameComparator(mAppLabelMap);
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
                String label = mAppLabelMap.get(pkgStats.appName);
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

                if(usageTimeInt <= xDays*CORRECT_USAGE_APP) holder.usageTime.setTextColor(Color.GREEN);
                else if(usageTimeInt > xDays*DANGEROUS_USAGE_APP) holder.usageTime.setTextColor(Color.RED);
                else holder.usageTime.setTextColor(Color.rgb(255,128,64));
                holder.icon.setImageDrawable(mIcons.get(pkgStats.appName));
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
            /*if (mDisplayOrder == _DISPLAY_ORDER_USAGE_TIME) {
                if (localLOGV) Log.i(TAG, "Sorting by usage time");
                Collections.sort(mPackageStats, mUsageTimeComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_LAST_TIME_USED) {
                if (localLOGV) Log.i(TAG, "Sorting by last time used");
                Collections.sort(mPackageStats, mLastTimeUsedComparator);
            } else if (mDisplayOrder == _DISPLAY_ORDER_APP_NAME) {
                if (localLOGV) Log.i(TAG, "Sorting by application name");
                Collections.sort(mPackageStats, mAppLabelComparator);
            }*/
            notifyDataSetChanged();
        }
    }

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.main_activity_stats);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        Spinner spinner = findViewById(R.id.SP_XDays);
        Resources res = getResources();
        String[] items = res.getStringArray(R.array.spinner_xDays);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        //requestPermissions();
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();

        Spinner typeSpinner = findViewById(R.id.typeSpinner);
        typeSpinner.setOnItemSelectedListener(this);

        ListView listView = findViewById(R.id.pkg_list);
//        mAdapter = new UsageStatsAdapter();
//        listView.setAdapter(mAdapter);

        final List<FillNom> fills = (List<FillNom>)getIntent().getSerializableExtra("llistaFills");
        if(fills != null && !fills.isEmpty()) {
            List<String> fillsEntries = new ArrayList<>();
            for (FillNom fill : fills) fillsEntries.add(fill.deviceName);
            Spinner fillsSpinner = findViewById(R.id.sonsSpinner);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fillsEntries);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fillsSpinner.setAdapter(arrayAdapter);
            fillsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    FillNom f = fills.get(position);
                    Call<Collection<GeneralUsage>> call = mTodoService.getAppUsage(f.idChild, xDays);
                    call.enqueue(new Callback<Collection<GeneralUsage>>() {
                        @Override
                        public void onResponse(Call<Collection<GeneralUsage>> call, Response<Collection<GeneralUsage>> response) {
                            TextView emptyView = findViewById(R.id.TV_emptyList);
                            ListView listView = findViewById(R.id.pkg_list);
                            if (response.isSuccessful() && response.body()!=null && !response.body().isEmpty()) {
                                listView.setVisibility(View.VISIBLE);
                                emptyView.setVisibility(View.GONE);
                                mAdapter2 = new GeneralUsageStatsAdapter((List<GeneralUsage>) response.body());
                                listView.setAdapter(mAdapter2);
                            } else {
                                listView.setVisibility(View.GONE);
                                emptyView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<Collection<GeneralUsage>> call, Throwable t) {
                            Toast toast = Toast.makeText(MainActivity.this, "Error al rebre appUsage", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.typeSpinner) mAdapter.sortList(position);
        else if(parent.getId() == R.id.SP_XDays){
            switch(position){
                case 0:
                    xDays = 1;
                    break;
                case 1:
                    xDays = 3;
                    break;
                case 2:
                    xDays = 5;
                    break;
                case 3:
                    xDays = 7;
                    break;
                case 4:
                    xDays = 10;
                    break;
            }

            ListView listView = findViewById(R.id.pkg_list);
            mAdapter = new UsageStatsAdapter();
            listView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

    private void requestPermissions() {
        List<UsageStats> stats = mUsageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis());
        boolean isEmpty = stats.isEmpty();
        if (isEmpty) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

}