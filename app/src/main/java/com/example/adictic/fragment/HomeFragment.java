package com.example.adictic.fragment;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.activity.MainActivityChild;
import com.example.adictic.entity.AppUsage;
import com.example.adictic.entity.GeneralUsage;
import com.example.adictic.rest.TodoApi;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "UsageStatsActivity";
    private static final boolean localLOGV = false;
    private UsageStatsManager mUsageStatsManager;
    private LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    private PackageManager mPm;
    private int xDays = 1;
    private AppBarConfiguration mAppBarConfiguration;

    private float CORRECT_USAGE_APP = 2;
    private float DANGEROUS_USAGE_APP = 4;

    private float CORRECT_USAGE_DAY = 3;
    private float DANGEROUS_USAGE_DAY = 6;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        Spinner spinner = root.findViewById(R.id.SP_XDays);
        Resources res = getResources();
        String[] items = res.getStringArray(R.array.spinner_xDays);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(adapter);
        //spinner.setOnItemSelectedListener(this);
        final TodoApi mTodoService = ((TodoApp)  getActivity().getApplication()).getAPI();
        final TodoApp todoApp = ((TodoApp) getActivity().getApplicationContext());

        Button pujarInfo = root.findViewById(R.id.Debug_PujarInfo);
        pujarInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<GeneralUsage> gul = new ArrayList<>();
                List<UsageStats> stats;
                for (int i = 0; i < xDays; i++) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.add(Calendar.DAY_OF_YEAR, -i);

                    Calendar cal2 = Calendar.getInstance();
                    cal2.add(Calendar.DAY_OF_YEAR, -i);
                    cal2.set(Calendar.HOUR_OF_DAY, 0);
                    cal2.set(Calendar.MINUTE, 0);
                    cal2.set(Calendar.SECOND, 0);

                    stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                            cal2.getTimeInMillis(), cal.getTimeInMillis());

                    List<AppUsage> appUsages = new ArrayList<>();
                    final int statCount = stats.size();
                    for (int j = 0; j < statCount; j++) {
                        final android.app.usage.UsageStats pkgStats = stats.get(j);
                        ApplicationInfo appInfo = null;
                        try {
                            appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (pkgStats.getLastTimeUsed() >= cal2.getTimeInMillis() && pkgStats.getLastTimeUsed() <= cal.getTimeInMillis() && pkgStats.getTotalTimeInForeground() > 5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                            AppUsage appUsage = new AppUsage();
                            appUsage.pkgName = pkgStats.getPackageName();
                            appUsage.lastTimeUsed = pkgStats.getLastTimeUsed();
                            appUsage.totalTime = pkgStats.getTotalTimeInForeground();
                            appUsages.add(appUsage);
                        }
                    }
                    GeneralUsage gu = new GeneralUsage();
                    gu.day = cal.get(Calendar.DAY_OF_MONTH);
                    gu.month = cal.get(Calendar.MONTH) + 1;
                    gu.year = cal.get(Calendar.YEAR);
                    gu.usage = appUsages;
                    gul.add(gu);
                }

                Call<String> call = mTodoService.sendAppUsage(todoApp.getID(), gul);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                    }
                });
            }
        });

        mUsageStatsManager = (UsageStatsManager) getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
        requestPermissions();
        mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm =  getActivity().getPackageManager();

        Spinner typeSpinner = root.findViewById(R.id.typeSpinner);
        //typeSpinner.setOnItemSelectedListener(getContext());

        ListView listView = root.findViewById(R.id.pkg_list);
        mAdapter = new UsageStatsAdapter(root);
        listView.setAdapter(mAdapter);

        return root;
    }

    class UsageStatsAdapter extends BaseAdapter {
        // Constants defining order for display order
        private static final int _DISPLAY_ORDER_USAGE_TIME = 0;
        private static final int _DISPLAY_ORDER_LAST_TIME_USED = 1;
        private static final int _DISPLAY_ORDER_APP_NAME = 2;

        private int mDisplayOrder = _DISPLAY_ORDER_USAGE_TIME;
        private MainActivityChild.LastTimeUsedComparator mLastTimeUsedComparator = new MainActivityChild.LastTimeUsedComparator();
        private MainActivityChild.UsageTimeComparator mUsageTimeComparator = new MainActivityChild.UsageTimeComparator();
        private MainActivityChild.AppNameComparator mAppLabelComparator;
        private final ArrayMap<String, String> mAppLabelMap = new ArrayMap<>();
        private final ArrayList<UsageStats> mPackageStats = new ArrayList<>();
        private final ArrayMap<String, Drawable> mIcons = new ArrayMap<>();

        private long totalTime = 0;

        UsageStatsAdapter(View root) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -xDays);

            final List<UsageStats> stats =
                    mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                            cal.getTimeInMillis(), System.currentTimeMillis());
            if (stats == null) {
                return;
            }

            ArrayMap<String, UsageStats> map = new ArrayMap<>();
            final int statCount = stats.size();
            for (int i = 0; i < statCount; i++) {
                final UsageStats pkgStats = stats.get(i);

                // load application labels for each application
                try {
                    ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                    if(pkgStats.getTotalTimeInForeground()>5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        Drawable appIcon = getActivity().getPackageManager().getApplicationIcon(pkgStats.getPackageName());
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

                } catch (PackageManager.NameNotFoundException e) {
                    // This package may be gone.
                }
            }
            mPackageStats.addAll(map.values());

            TextView TV_totalUse = root.findViewById(R.id.TV_totalUseVar);

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
            //mAppLabelComparator = new MainActivityChild.AppNameComparator(mAppLabelMap);
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
//                holder.lastTimeUsed.setText(((Long)pkgStats.getLastTimeUsed()).toString());
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

    static class AppViewHolder {
        TextView pkgName;
        TextView lastTimeUsed;
        TextView usageTime;
        ImageView icon;
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
