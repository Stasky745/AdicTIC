package com.adictic.common.util.hilt;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.core.text.HtmlCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.adictic.common.BuildConfig;
import com.adictic.common.R;
import com.adictic.common.dao.BlockedAppDao;
import com.adictic.common.dao.EventBlockDao;
import com.adictic.common.dao.HorarisNitDao;
import com.adictic.common.entity.AppInfo;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.BlockedApp;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.entity.NotificationInformation;
import com.adictic.common.rest.Api;
import com.adictic.common.ui.NotificationSettings;
import com.adictic.common.ui.settings.ChangePasswordActivity;
import com.adictic.common.ui.settings.SecuritySettings;
import com.adictic.common.ui.settings.SettingsActivity;
import com.adictic.common.ui.settings.notifications.NotificationActivity;
import com.adictic.common.util.App;
import com.adictic.common.util.BiometricAuthUtil;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Global;
import com.adictic.common.util.HiltEntryPoint;
import com.adictic.common.workers.UpdateTokenWorker;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.EntryPoints;
import retrofit2.Call;
import retrofit2.Response;

public class Repository {
    private final BlockedAppDao blockedAppDao;
    private final EventBlockDao eventBlockDao;
    private final HorarisNitDao horarisNitDao;

    private final Api api;

    private final RequestManager glideRequestManager;

    private final SharedPreferences sharedPreferences;

    private final Application application;

    @Inject
    public Repository(Application application, Api api, BlockedAppDao blockedAppDao, EventBlockDao eventBlockDao, HorarisNitDao horarisNitDao, RequestManager glideRequestManager, SharedPreferences sharedPreferences) {
        this.application = application;
        this.api = api;
        this.blockedAppDao = blockedAppDao;
        this.eventBlockDao = eventBlockDao;
        this.horarisNitDao = horarisNitDao;
        this.glideRequestManager = glideRequestManager;
        this.sharedPreferences = sharedPreferences;
    }

    //region Shared Preferences

    public SharedPreferences getEncryptedSharedPreferences() { return sharedPreferences; }

    //endregion

    //region ROOM
    //region AppsDB
    public void updateApps(List<BlockedApp> list) {
        blockedAppDao.update(list);
    }

    public void deleteAppByName(String pkgName) {
        blockedAppDao.deleteByName(pkgName);
    }

    public List<BlockedApp> getAllApps() {
        return blockedAppDao.getAll();
    }
    //endregion

    //region EventsDB

    public void updateEvents(List<EventBlock> list) {
        eventBlockDao.update(list);
    }

    public List<EventBlock> getEventsByDay(int day) {
        return eventBlockDao.getEventsByDay(day);
    }

    public List<EventBlock> getAllEvents() {
        return eventBlockDao.getAll();
    }

    //endregion

    //region HorarisDB

    public void updateHoraris(List<HorarisNit> list) {
        horarisNitDao.update(list);
    }

    public HorarisNit getHorariByDay(int day) {
        return horarisNitDao.findByDay(day);
    }

    public List<HorarisNit> getAllHoraris() {
        return horarisNitDao.getAll();
    }

    //endregion
    //endregion

    //region API
    public Api getApi() {
        return api;
    }

    public void askChildForLiveApp(long idChild, boolean liveApp) {
        Call<String> call = api.askChildForLiveApp(idChild, liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if (!response.isSuccessful()) {
                    Toast toast = Toast.makeText(application, application.getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Toast toast = Toast.makeText(application, application.getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
    //endregion

    //region GLIDE

    public void setIconDrawable(String pkgName, final ImageView d) {

        String URL = Global.BASE_URL_RELEASE;
        if(BuildConfig.DEBUG) URL = Global.BASE_URL_DEBUG;

        Uri imageUri = Uri.parse(URL).buildUpon()
                .appendPath("icons")
                .appendPath(pkgName)
                .build();

        glideRequestManager
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(d);
    }


    public void setAdminPhoto(Long idAdmin, final ImageView d){
        if(idAdmin==-1)
            return;

        if(App.getAdminPic() != null) {
            d.setImageDrawable(App.getAdminPic());
            return;
        }

        String URL = Global.BASE_URL_RELEASE;
        if(BuildConfig.DEBUG)
            URL = Global.BASE_URL_DEBUG;

        Uri imageUri = Uri.parse(URL).buildUpon()
                .appendPath("admins")
                .appendPath("pictures")
                .appendPath(idAdmin.toString())
                .build();

        glideRequestManager
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(d);

        App.setAdminPic(d.getDrawable());
    }
    //endregion

    //region USAGE
    /**
     * pre: agafa informació dels últims nDies (0 si dia actual)
     * post: retorna la llista amb els mesos ja adaptats pel servidor (+1)
     **/
    public List<GeneralUsage> getGeneralUsages(int nDies) {
        List<GeneralUsage> gul = new ArrayList<>();

        nDies = Math.min(nDies, 6);
        nDies = Math.max(nDies, 0);

        long timer = System.currentTimeMillis();

        long initialTime, finalTime;

        for(int i = 0; i <= nDies; i++){
            DateTime initialDate = new DateTime();
            initialDate = initialDate.withTimeAtStartOfDay();
            initialDate = initialDate.minusDays(i);

            DateTime finalDate = new DateTime(initialDate).plusDays(1);

            initialTime = initialDate.getMillis();

            finalTime = Math.min(finalDate.getMillis(), DateTime.now().getMillis());

            Pair<List<AppUsage>, Integer> pair = getAppUsages(initialTime, finalTime);
            List<AppUsage> appUsages = pair.first;
            int timesUnlocked = pair.second;

            GeneralUsage gu = new GeneralUsage();
            gu.day = initialDate.getDayOfMonth();
            gu.month = initialDate.getMonthOfYear();
            gu.year = initialDate.getYear();
            gu.usage = appUsages;

            gu.totalTime = appUsages.stream()
                    .mapToLong(appUsage -> appUsage.totalTime)
                    .sum();

            gu.timesUnlocked=timesUnlocked;

            gul.add(gu);
        }

        System.out.println("TIME: " + (System.currentTimeMillis() - timer));

        return gul;
    }

    public Pair<List<AppUsage>,Integer> getAppUsages(long initialTime, long finalTime) {
        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppUsage> map = new HashMap<>();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) application.getSystemService(Context.USAGE_STATS_SERVICE);
        assert  mUsageStatsManager != null;
        PackageManager mPm = application.getPackageManager();

        UsageEvents usageEvents = mUsageStatsManager.queryEvents(initialTime, finalTime);

        //finding the launcher
//        List<String> launcherList = getLaunchers(mContext);

        //capturing all events in a array to compare with next element
        int timesUnlocked = 0;
        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);

//            if(launcherList.contains(currentEvent.getPackageName()))
//                continue;

            if(currentEvent.getEventType() == UsageEvents.Event.KEYGUARD_HIDDEN){
                timesUnlocked++;
                continue;
            }
            if (currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED ||
                    currentEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED) {
                allEvents.add(currentEvent);
                String pkgName = currentEvent.getPackageName();

                // taking it into a collection to access by package name
                if (map.get(pkgName)==null) {
                    try {
                        ApplicationInfo appInfo;
                        appInfo = mPm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);

                        AppUsage appUsage = new AppUsage();
                        appUsage.app = new AppInfo();

                        if (Build.VERSION.SDK_INT >= 26)
                            appUsage.app.category = appInfo.category;
                        appUsage.app.appName = mPm.getApplicationLabel(appInfo).toString();
                        appUsage.app.pkgName = pkgName;
                        appUsage.totalTime = 0L;
                        appUsage.timesOpened = 0;
                        map.put(pkgName, appUsage);
                    } catch (PackageManager.NameNotFoundException e) {
                        AppUsage appUsage = new AppUsage();
                        appUsage.app = new AppInfo();

                        if (Build.VERSION.SDK_INT >= 26)
                            appUsage.app.category = -1;
                        appUsage.app.appName = pkgName;
                        appUsage.app.pkgName = pkgName;
                        appUsage.totalTime = 0L;
                        appUsage.timesOpened = 0;
                        map.put(pkgName, appUsage);
                    }
                }
            }
        }

        if(allEvents.isEmpty())
            return new Pair<>(new ArrayList<>(), timesUnlocked);

        allEvents.sort(Comparator.comparing(UsageEvents.Event::getTimeStamp));

        // Si el primer event és de pausar activitat és que estava oberta abans de mitjanit, per tant sumar aquell temps també
        UsageEvents.Event primerEvent = allEvents.get(0);
        if(primerEvent.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED){
            long diff = primerEvent.getTimeStamp() - initialTime;
            Objects.requireNonNull(map.get(primerEvent.getPackageName())).totalTime += diff;
        }

        // Si l'últim event és de començar activitat és que està oberta ara, per tant sumar aquest temps també
        UsageEvents.Event ultimEvent = allEvents.get(allEvents.size() - 1);
        if(ultimEvent.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED){
            long diff = finalTime - ultimEvent.getTimeStamp();
            Objects.requireNonNull(map.get(ultimEvent.getPackageName())).totalTime += diff;
        }

        // iterating through the arraylist
        for (int i=0;i<allEvents.size()-1;i++){
            UsageEvents.Event E0=allEvents.get(i);
            UsageEvents.Event E1=allEvents.get(i+1);

            //Si els noms són diferents i l'event és obrir
            if (!E0.getPackageName().equals(E1.getPackageName()) && E1.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED){
                // if true, E1 (launch event of an app) app launched
                Objects.requireNonNull(map.get(E0.getPackageName())).lastTimeUsed = E0.getTimeStamp();
                Objects.requireNonNull(map.get(E1.getPackageName())).timesOpened++;

                if(E0.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED){
                    long diff = E1.getTimeStamp()-E0.getTimeStamp();
                    Objects.requireNonNull(map.get(E0.getPackageName())).totalTime += diff;
                }
            }

            // Si són la mateixa classe que ha començat i acabat, mirar temps
            if (E0.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED
                    && E1.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED
                    && E0.getClassName().equals(E1.getClassName())){
                long diff = E1.getTimeStamp()-E0.getTimeStamp();
                Objects.requireNonNull(map.get(E0.getPackageName())).totalTime += diff;
                Objects.requireNonNull(map.get(E0.getPackageName())).lastTimeUsed = E0.getTimeStamp();
            }
        }

        return new Pair<>(new ArrayList<>(map.values()), timesUnlocked);
    }

    public List<String> getLaunchers(){
        //finding the launcher
        PackageManager localPackageManager = application.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> lst = localPackageManager.queryIntentActivities(intent, 0);
        List<String> launcherList = new ArrayList<>();
        if (!lst.isEmpty()) {
            launcherList = lst.stream()
                    .map(resolveInfo -> resolveInfo.activityInfo.packageName)
                    .collect(Collectors.toList());
        }

        return launcherList;
    }
    //endregion

    //region WORKERS

    public void runUpdateTokenWorker(){
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(UpdateTokenWorker.class, 1, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                5,
                                TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(application)
                .enqueueUniquePeriodicWork("UpdateTokenWorker", ExistingPeriodicWorkPolicy.REPLACE, myWork);

    }

    //endregion

    //region SPANNABLES

    public Spanned getSpannedText(String string){
        SpannableString spannableString;
        if(string.startsWith("- ") ||
                string.startsWith("* ") ||
                string.startsWith("· ")){
            spannableString = new SpannableString(string.substring(2));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                spannableString.setSpan(new BulletSpan(BulletSpan.STANDARD_GAP_WIDTH, application.getColor(R.color.colorPrimary)), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            else
                spannableString.setSpan(new BulletSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else
            spannableString = new SpannableString(string);
        return HtmlCompat.fromHtml(spannableString.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    public ClickableSpan getClickableSpan(Intent intent) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                application.startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setFakeBoldText(true);
                ds.setUnderlineText(false);
            }
        };
    }

    //endregion

    //region NOTIFICATIONS

    public void addNotificationToList(NotificationInformation notificationInformation) {
        final int MAX_NOTIF_SIZE = 25;

        String json = sharedPreferences.getString(Constants.SHARED_PREFS_NOTIFS, "");
        Type type = new TypeToken<ArrayList<NotificationInformation>>() {}.getType();

        Gson gson = new Gson();
        ArrayList<NotificationInformation> notifList = gson.fromJson(json, type) != null ? gson.fromJson(json, type) : new ArrayList<>();

        // Si la llista té 15 elements
        if(notifList.size() == MAX_NOTIF_SIZE){
            NotificationInformation oldNotif = notifList.stream()
                    .min(Comparator.comparing(v -> v.dateMillis)).get();

            notifList.remove(oldNotif);
        }
        else if(notifList.size() > MAX_NOTIF_SIZE){
            for(int i = 0; i < notifList.size() - (MAX_NOTIF_SIZE-1); i++){
                NotificationInformation oldNotif = notifList.stream()
                        .min(Comparator.comparing(v -> v.dateMillis)).get();

                notifList.remove(oldNotif);
            }
        }

        notifList.add(notificationInformation);

        String newJson = gson.toJson(notifList);
        sharedPreferences.edit().putString(Constants.SHARED_PREFS_NOTIFS, newJson).apply();
    }

    public ArrayList<NotificationInformation> getNotificationList(){
        String json = sharedPreferences.getString(Constants.SHARED_PREFS_NOTIFS, "");
        Type type = new TypeToken<ArrayList<NotificationInformation>>() {}.getType();

        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    public void setNotificationList(List<NotificationInformation> list){
        Gson gson = new Gson();

        String newJson = gson.toJson(list);
        sharedPreferences.edit().putString(Constants.SHARED_PREFS_NOTIFS, newJson).apply();
    }

    //endregion

    //region SETTINGS

    public void settings_android(PreferenceFragmentCompat context) {
        Preference setting_android = context.findPreference("setting_android");
        ApplicationInfo appInfo = context.requireContext().getApplicationContext().getApplicationInfo();

        assert setting_android != null;
        setting_android.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", appInfo.packageName, null);
            intent.setData(uri);
            context.startActivity(intent);
            return true;
        });
    }

    public void settings_change_notifications(PreferenceFragmentCompat context) {
        Preference change_notif = context.findPreference("setting_notifications");
        assert change_notif != null;

        change_notif.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.requireContext(), NotificationSettings.class);
            context.startActivity(intent);
            return true;
        });
    }

    public void settings_notification_history(PreferenceFragmentCompat context) {
        Preference setting_notification_history = context.findPreference("setting_notification_history");
        assert setting_notification_history != null;

        setting_notification_history.setOnPreferenceClickListener(preference -> {
            context.requireActivity().startActivity(new Intent(context.getActivity(), NotificationActivity.class));
            return true;
        });
    }

    public void settings_change_theme(PreferenceFragmentCompat context) {
        ListPreference theme_preference = context.findPreference("setting_change_theme");
        assert sharedPreferences != null;
        String selectedTheme = sharedPreferences.getString("theme", "follow_system");

        assert theme_preference != null;
        theme_preference.setValue(selectedTheme);
        theme_preference.setSummary(theme_preference.getEntry());

        theme_preference.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putString("theme", (String) newValue).apply();
            switch((String) newValue){
                case "no":
                    theme_preference.setSummary(context.getString(R.string.theme_light));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "yes":
                    theme_preference.setSummary(context.getString(R.string.theme_dark));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                default:
                    theme_preference.setSummary(context.getString(R.string.theme_default));
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
            return true;
        });
    }

    public void settings_security(PreferenceFragmentCompat context) {
        Preference setting_security = context.findPreference("setting_security");
        assert setting_security != null;

        setting_security.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.getActivity(), SettingsActivity.class);
            intent.putExtra("fragment", SecuritySettings.class.getCanonicalName());
            intent.putExtra("title", context.getString(R.string.security));
            context.requireActivity().startActivity(intent);
            return true;
        });
    }

    public void settings_change_password(PreferenceFragmentCompat context) {
        Preference change_password = context.findPreference("setting_change_password");

        assert change_password != null;
        change_password.setOnPreferenceClickListener(preference -> {
            context.requireActivity().startActivity(new Intent(context.getActivity(), ChangePasswordActivity.class));
            return true;
        });
    }

    public void setting_require_biometric(PreferenceFragmentCompat context) {
        SwitchPreference setting_require_biometric = context.findPreference("setting_require_biometric");
        assert setting_require_biometric != null;
        assert sharedPreferences != null;

        int bioType = BiometricAuthUtil.isAuthenticationSupported(context.requireContext());
        if (bioType != BiometricManager.BIOMETRIC_SUCCESS && !(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && bioType == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)){
            Objects.requireNonNull(setting_require_biometric.getParent()).removePreference(setting_require_biometric);
        }

        setting_require_biometric.setChecked(sharedPreferences.getBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, false));

        setting_require_biometric.setOnPreferenceChangeListener((preference, newValue) -> {
            if(newValue.toString().equals("true")){
                int bioType2 = BiometricAuthUtil.isAuthenticationSupported(context.requireContext());
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && bioType2 == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED){
                    BiometricAuthUtil.createBiometricCredentials(context);
                    return false;
                } else if (bioType2 == BiometricManager.BIOMETRIC_SUCCESS){
                    sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, true).apply();
                } else
                    return false;
            } else if(newValue.toString().equals("false")){
                sharedPreferences.edit().putBoolean(Constants.SHARED_PREFS_BIOMETRIC_AUTH, false).apply();
            }
            return true;
        });
    }

    //
}
