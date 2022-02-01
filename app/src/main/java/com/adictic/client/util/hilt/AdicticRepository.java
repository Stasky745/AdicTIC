package com.adictic.client.util.hilt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.adictic.client.R;
import com.adictic.client.dao.BlockedAppDao;
import com.adictic.client.dao.EventBlockDao;
import com.adictic.client.dao.HorarisNitDao;
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.service.ClientNotificationManager;
import com.adictic.client.ui.inici.Login;
import com.adictic.client.ui.inici.Permisos;
import com.adictic.client.ui.inici.SplashScreen;
import com.adictic.client.util.AdicticApp;
import com.adictic.client.util.Funcions;
import com.adictic.client.workers.AppUsageWorker;
import com.adictic.client.workers.EventWorker;
import com.adictic.client.workers.GeoLocWorker;
import com.adictic.client.workers.HorarisEventsWorkerManager;
import com.adictic.client.workers.HorarisWorker;
import com.adictic.client.workers.NotifWorker;
import com.adictic.client.workers.ServiceWorker;
import com.adictic.common.callbacks.BooleanCallback;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.BlockedApp;
import com.adictic.common.entity.BlockedLimitedLists;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.EventsAPI;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.HorarisAPI;
import com.adictic.common.entity.HorarisNit;
import com.adictic.common.entity.LimitedApps;
import com.adictic.common.entity.NotificationInformation;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.rest.Api;
import com.adictic.common.ui.ReportActivity;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.adictic.common.util.hilt.Repository;
import com.bumptech.glide.RequestManager;
import com.google.firebase.messaging.FirebaseMessaging;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.EntryPointAccessors;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.observers.SafeObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Response;

public class AdicticRepository extends Repository {

    private final AdicticApi api;

    private final BlockedAppDao blockedAppDao;
    private final EventBlockDao eventBlockDao;
    private final HorarisNitDao horarisNitDao;

    private final SharedPreferences sharedPreferences;

    private final Application application;
    private final WorkManager workManager;
    private final UsageStatsManager usageStatsManager;
    private final ClientNotificationManager clientNotificationManager;

    @Inject
    public AdicticRepository(Application application, ClientNotificationManager clientNotificationManager, WorkManager workManager, UsageStatsManager usageStatsManager, AdicticApi api, BlockedAppDao blockedAppDao, EventBlockDao eventBlockDao, HorarisNitDao horarisNitDao, RequestManager glideRequestManager, SharedPreferences sharedPreferences) {
        super(application, api, glideRequestManager, sharedPreferences);
        this.application = application;
        this.clientNotificationManager = clientNotificationManager;
        this.workManager = workManager;
        this.usageStatsManager = usageStatsManager;
        this.api = api;
        this.sharedPreferences = sharedPreferences;
        this.blockedAppDao = blockedAppDao;
        this.eventBlockDao = eventBlockDao;
        this.horarisNitDao = horarisNitDao;
    }

    //region TOOLS

    public ClientNotificationManager getNotificationManager() { return clientNotificationManager; }

    // Contrasenya ha d'estar encriptada amb SHA256
    public void isPasswordCorrect(String pwd, BooleanCallback callback){
        UserLogin userLogin = new UserLogin();
        userLogin.password = pwd;
        userLogin.username = sharedPreferences.getString(Constants.SHARED_PREFS_USERNAME, "");
        userLogin.token = "";
        userLogin.tutor = -1;

        Call<String> call = api.checkPassword(userLogin);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                callback.onDataGot(response.isSuccessful() && response.body() != null && response.body().equals("ok"));
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                callback.onDataGot(false);
            }
        });
    }

    public int getDayAppUsage(String pkgName){
        Calendar finalTime = Calendar.getInstance();

        Calendar initialTime = Calendar.getInstance();
        initialTime.set(Calendar.HOUR_OF_DAY, 0);
        initialTime.set(Calendar.MINUTE, 0);
        initialTime.set(Calendar.SECOND, 0);

        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, initialTime.getTimeInMillis(), finalTime.getTimeInMillis());

        UsageStats appUsageStats = stats.stream()
                .filter(usageStats -> usageStats.getPackageName().equals(pkgName))
                .findFirst()
                .orElse(null);

        if (appUsageStats == null)
            return 0;
        else
            return (int) appUsageStats.getTotalTimeInForeground();
    }

    //endregion

    //region ROOM
    //region AppsDB
    public Completable updateApps(List<BlockedApp> list) {
        return blockedAppDao.update(list);
    }

    public void deleteAppByName(String pkgName) {
        Disposable disposable = blockedAppDao.deleteByName(pkgName).subscribeWith(new DisposableCompletableObserver() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                e.printStackTrace();
            }
        });

        disposable.dispose();
    }

    public Single<List<BlockedApp>> getAllApps() {
        return blockedAppDao.getAll();
    }
    //endregion

    //region EventsDB

    public Completable updateEvents(List<EventBlock> list) {
        return eventBlockDao.update(list);
    }

    public Single<List<EventBlock>> getEventsByDay(int day) {
        return eventBlockDao.getEventsByDay(day);
    }

    public Single<List<EventBlock>> getAllEvents() {
        return eventBlockDao.getAll();
    }

    //endregion

    //region HorarisDB

    public void updateHoraris(List<HorarisNit> list) {

        horarisNitDao.update(list);
    }

    public Single<HorarisNit> getHorariByDay(int day) {
        return horarisNitDao.findByDay(day);
    }

    public Single<List<HorarisNit>> getAllHoraris() {
        return horarisNitDao.getAll();
    }

    //endregion
    //endregion

    //region API

    public AdicticApi getApi() {
        return api;
    }


    //region Events

    public Completable checkEvents() {
        // Agafem els horaris de la nit i Events
        return api.getEvents(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1))
                .subscribeOn(Schedulers.io())
                .toObservable()
                .switchMapCompletable(eventsAPI -> {
                    List<EventBlock> eventBlocks = eventsAPI != null && eventsAPI.events != null
                            ? new ArrayList<>(eventsAPI.events)
                            : new ArrayList<>();

                    return updateEvents(eventBlocks);
                });
    }

    public void setEvents(List<EventBlock> events) {
        // Aturem tots els workers d'Events que estiguin configurats
        workManager
                .cancelAllWorkByTag(Constants.WORKER_TAG_EVENT_BLOCK);

        if(!accessibilityServiceOn())
            return;

        AccessibilityScreenService.instance.setActiveEvents(0);

        if(events == null || events.isEmpty())
            return;

        int diaSetmana = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        List<EventBlock> eventsTodayList = events.stream()
                .filter(eventBlock -> eventBlock.days.contains(diaSetmana))
                .collect(Collectors.toList());

        setEventWorkerByDay(eventsTodayList);
    }

    //endregion

    //region Horaris

    public Completable checkHoraris() {
        // Agafem els horaris de la nit i Events
        return api.getHoraris(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1))
                .subscribeOn(Schedulers.io())
                .toObservable()
                .switchMapCompletable(horarisAPI -> {
                    List<HorarisNit> horarisList =
                            horarisAPI != null && horarisAPI.horarisNit != null
                                    ? new ArrayList<>(horarisAPI.horarisNit)
                                    : new ArrayList<>();

                    updateHoraris(horarisList);
                    startHorarisEventsManagerWorker();
                    setHoraris(horarisList);
                    return Completable.complete();
                });
    }

    public void setHoraris(List<HorarisNit> horarisNit){
        // Aturem tots els workers d'Horaris que estiguin configurats
        workManager
                .cancelAllWorkByTag(Constants.WORKER_TAG_HORARIS_BLOCK);

        if(!accessibilityServiceOn())
            return;

        AccessibilityScreenService.instance.setHorarisActius(false);

        HorarisNit horariAvui = horarisNit.stream()
                .filter(horarisNit1 -> horarisNit1.dia.equals(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)))
                .findFirst()
                .orElse(null);

        if(horariAvui == null){
            AccessibilityScreenService.instance.updateDeviceBlock();
            return;
        }

        int now = DateTime.now().getMillisOfDay();

        int wakeTimeDelay = horariAvui.despertar - now;
        int sleepTimeDelay = horariAvui.dormir - now;

        if(wakeTimeDelay > 0) {
            setUpHorariWorker(wakeTimeDelay, false);
            setUpHorariWorker(sleepTimeDelay, true);

            AccessibilityScreenService.instance.setHorarisActius(true);
        }
        else if(sleepTimeDelay > 0)
            setUpHorariWorker(sleepTimeDelay, true);
        else
            AccessibilityScreenService.instance.setHorarisActius(true);

        AccessibilityScreenService.instance.updateDeviceBlock();
    }

    //endregion

    //region Usage

    /**
     * Actualitza la bdd de room de l'ús d'apps del dia actual i envia al servidor si fa molt que s'ha fet. Retorna el general usage del dia actual
     */
    public GeneralUsage sendAppUsage(){
        // Agafem quants dies fa que no s'agafen dades (màxim 6)
        long lastMillisAppUsage = sharedPreferences.getLong(Constants.SHARED_PREFS_LAST_DAY_SENT_DATA, 0);

        // Si hem enviat dades fa menys de 1 minuts, no tornem a enviar, actualitzem bdd del dia d'avui
        if(System.currentTimeMillis() - lastMillisAppUsage < 1000*60)
            return getGeneralUsages(0).get(0);

        int daysToFetch;
        if(lastMillisAppUsage == 0)
            daysToFetch = 6;
        else {
            long lastDay = new DateTime(lastMillisAppUsage).withTimeAtStartOfDay().getMillis();
            long today = new DateTime().withTimeAtStartOfDay().getMillis();
            daysToFetch = Math.min(Math.round(TimeUnit.MILLISECONDS.toDays(Math.abs(today-lastDay))), 6);
        }

        // Agafem les dades
        List<GeneralUsage> gul = getGeneralUsages(daysToFetch);

        long totalTime = gul.stream()
                .mapToLong(generalUsage -> generalUsage.totalTime)
                .sum();

//        long lastTotalUsage = sharedPreferences.getLong(Constants.SHARED_PREFS_LAST_TOTAL_USAGE, 0);

//        // Si és el mateix dia i no ha pujat més de 5 minuts el total, tornem
//        if(sameDay(lastMillisAppUsage, System.currentTimeMillis()) && totalTime - lastTotalUsage < 5 * 60 * 1000)
//            return;

        Call<String> call = api.sendAppUsage(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1), gul);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if(response.isSuccessful()) {
                    sharedPreferences.edit().putLong(Constants.SHARED_PREFS_LAST_TOTAL_USAGE, totalTime).apply();
                    sharedPreferences.edit().putLong(Constants.SHARED_PREFS_LAST_DAY_SENT_DATA, System.currentTimeMillis()).apply();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });

        return gul.stream()
                .filter(generalUsage ->
                        generalUsage.day.equals(DateTime.now().getDayOfMonth()) &&
                                generalUsage.month.equals(DateTime.now().getMonthOfYear()) &&
                                generalUsage.year.equals(DateTime.now().getYear())
                )
                .findFirst()
                .orElse(gul.get(0));
    }

    //endregion

    public Map<String, Long> getTodayAppUsage() {
        long startOfDay = new DateTime().withTimeAtStartOfDay().getMillis();
        long now = System.currentTimeMillis();

        List<AppUsage> listUsages = getAppUsages(startOfDay, now).first;

        return listUsages.stream()
                .collect(Collectors.toMap(appUsage -> appUsage.app.pkgName,appUsage -> appUsage.totalTime));
    }

    public void fetchAppBlockFromServer(){

        if(sharedPreferences.contains(Constants.SHARED_PREFS_IDUSER)) {
            long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            Call<BlockedLimitedLists> call = api.getBlockedLimitedLists(idChild);
            call.enqueue(new Callback<BlockedLimitedLists>() {
                @Override
                public void onResponse(@NonNull Call<BlockedLimitedLists> call, @NonNull Response<BlockedLimitedLists> response) {
                    super.onResponse(call, response);
                    if (response.isSuccessful() && response.body() != null) {
                        updateDB_BlockedApps(response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BlockedLimitedLists> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                }
            });
        }
    }

    private void updateDB_BlockedApps(BlockedLimitedLists body) {
        if(!accessibilityServiceOn())
            return;

        List<BlockedApp> blockedApps = new ArrayList<>();
        List<String> BlockedAppsList = body.blockedApps != null ? body.blockedApps : new ArrayList<>();
        for(String blockedApp : BlockedAppsList) {
            BlockedApp app = new BlockedApp();
            app.pkgName = blockedApp;
            app.timeLimit = 0L;
            blockedApps.add(app);
        }

        List<LimitedApps> listLimitedApps = body.limitApps != null ? body.limitApps : new ArrayList<>();
        for(LimitedApps limitedApps : listLimitedApps){
            BlockedApp app = new BlockedApp();
            app.pkgName = limitedApps.name;
            app.timeLimit = limitedApps.time;
            blockedApps.add(app);
        }
        updateApps(blockedApps);
        GeneralUsage generalUsage = getGeneralUsages(0).get(0);

        Disposable disposable = getAllApps().subscribeWith(new DisposableSingleObserver<List<BlockedApp>>() {
            @Override
            public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<BlockedApp> blockedApps) {
                Map<String, Long> mapUsage = getTimeLeftMapAccessibilityService(blockedApps, generalUsage);

                AccessibilityScreenService.instance.setTimeLeftMap(mapUsage);
                AccessibilityScreenService.instance.isCurrentAppBlocked();
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                Map<String, Long> mapUsage = getTimeLeftMapAccessibilityService(new ArrayList<>(), generalUsage);

                AccessibilityScreenService.instance.setTimeLeftMap(mapUsage);
                AccessibilityScreenService.instance.isCurrentAppBlocked();
            }
        });

        disposable.dispose();
    }

    public Map<String, Long> getTimeLeftMapAccessibilityService(List<BlockedApp> listBlockedApps, GeneralUsage generalUsage) {
        Map<String, Long> mapUsage = new HashMap<>();
        for(BlockedApp blockedApp : listBlockedApps){
            AppUsage app= generalUsage.usage.stream()
                    .filter(appUsage -> appUsage.app.pkgName.equals(blockedApp.pkgName))
                    .findFirst().orElse(null);
            if(app == null)
                continue;

            long timeUsed = app.totalTime;

            mapUsage.put(blockedApp.pkgName, blockedApp.timeLimit - timeUsed);
        }

        return mapUsage;
    }

    //endregion

    //region WORKERS

    public void startServiceWorker(){
        if(sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, true))
            return;

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(ServiceWorker.class, 20, TimeUnit.MINUTES)
                        .build();

        workManager
                .enqueueUniquePeriodicWork("ServiceWorker",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myWork);
    }

    public void startAppUsageWorker24h(){
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(AppUsageWorker.class, 24, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                5,
                                TimeUnit.MINUTES)
                        .addTag(Constants.WORKER_TAG_APP_USAGE)
                        .build();

        workManager
                .enqueueUniquePeriodicWork("pujarAppInfo",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        myWork);
    }

    public void setUpEventWorker(long delay, boolean startEvent){
        Data data = new Data.Builder()
                .putBoolean("start", startEvent)
                .build();

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(EventWorker.class, 7, TimeUnit.DAYS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .addTag(Constants.WORKER_TAG_EVENT_BLOCK)
                        .setInputData(data)
                        .build();

        workManager
                .enqueue(myWork);
    }

    private void setEventWorkerByDay(List<EventBlock> eventList) {
        eventList.sort(Comparator.comparingInt(eventBlock -> eventBlock.startEvent));

        // Mirem quins workers són necessaris en cas que hi hagi events que es solapin
        List<Pair<Integer, Integer>> workersList = new ArrayList<>();
        for(EventBlock event : eventList){
            Pair<Integer, Integer> newTime = new Pair<>(event.startEvent, event.endEvent);

            if(workersList.isEmpty())
                workersList.add(newTime);
            else {
                int index = workersList.size()-1;
                Pair<Integer, Integer> lastTime = workersList.get(index);
                if(newTime.first < lastTime.second && newTime.second > lastTime.second)
                    workersList.set(index, new Pair<>(lastTime.first, newTime.second));
                else if(newTime.first > lastTime.second)
                    workersList.add(newTime);
            }
        }

        // Fer workers
        for(Pair<Integer, Integer> pair : workersList){
            long now = DateTime.now().getMillisOfDay();

            long startTimeDelay = pair.first - now;
            long endTimeDelay = pair.second - now;

            if(startTimeDelay > 0) {
                setUpEventWorker(startTimeDelay, true);
                setUpEventWorker(endTimeDelay, false);
            }
            else if(endTimeDelay > 0){
                setUpEventWorker(endTimeDelay, false);

                if(accessibilityServiceOn())
                    AccessibilityScreenService.instance.setActiveEvents(1);
            }
        }

        if(accessibilityServiceOn())
            AccessibilityScreenService.instance.updateDeviceBlock();
    }

    public void startHorarisEventsManagerWorker(){
        long startOfDay = DateTime.now().withTimeAtStartOfDay().plusDays(1).getMillisOfDay() + 500;
        long delay = startOfDay - DateTime.now().getMillis();

        PeriodicWorkRequest myWork =
                new PeriodicWorkRequest.Builder(HorarisEventsWorkerManager.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(delay,TimeUnit.MILLISECONDS)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                30,
                                TimeUnit.SECONDS
                        )
                        .addTag(Constants.WORKER_TAG_HORARIS_EVENTS_MANAGER)
                        .build();

        workManager
                .enqueueUniquePeriodicWork(Constants.WORKER_TAG_HORARIS_EVENTS_MANAGER,
                        ExistingPeriodicWorkPolicy.KEEP,
                        myWork);
    }



    private void setUpHorariWorker(long delay, boolean startSleep){
        Data data = new Data.Builder()
                .putBoolean("start", startSleep)
                .build();

        WorkRequest myWork;

        myWork = new OneTimeWorkRequest.Builder(HorarisWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();

        workManager
                .enqueue(myWork);
    }

    public void runGeoLocWorkerOnce() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(GeoLocWorker.class)
                        .setConstraints(constraints)
                        .build();

        workManager
                .enqueueUniqueWork("geoLocWorkerOnce", ExistingWorkPolicy.REPLACE, myWork);
    }

    public void startNotificationWorker(NotificationInformation notif, Long idChild){
        Data data = new Data.Builder()
                .putString("title", notif.title)
                .putString("body", notif.message)
                .putBoolean("important", notif.important)
                .putLong("dateMillis", notif.dateMillis)
                .putLong("idChild", idChild)
                .putString("notifCode", notif.notifCode)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkRequest myWork =
                new OneTimeWorkRequest.Builder(NotifWorker.class)
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                5,
                                TimeUnit.MINUTES)
                        .setInputData(data)
                        .build();

        workManager
                .enqueue(myWork);
    }

    //endregion

    //region NOTIFICATIONS

    public boolean accessibilityServiceOn(){
        boolean res = AccessibilityScreenService.instance != null;

        if(!res){
            NotificationInformation notif = new NotificationInformation();
            notif.title = application.getString(R.string.notif_accessibility_error_title);
            notif.message = application.getString(R.string.notif_accessibility_error_body);
            notif.important = true;
            notif.dateMillis = System.currentTimeMillis();
            notif.read = false;
            notif.notifCode = Constants.NOTIF_SETTINGS_ACCESSIBILITY_ERROR;

            sendNotifToParent(notif);
        }

        return res;
    }

    public void sendNotifToParent(NotificationInformation notif) {
        long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);

        Call<String> call = api.sendNotification(idChild, notif);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                startNotificationWorker(notif, idChild);
            }
        });
    }

    //endregion

    //region SETTINGS

    public void settings_report_suggestion(PreferenceFragmentCompat context) {
        Preference report_suggestion = context.findPreference("setting_report_suggestion");

        assert report_suggestion != null;
        report_suggestion.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.requireActivity(), ReportActivity.class);
            intent.putExtra("isTypeBug", false);
            context.startActivity(intent);
            return true;
        });
    }

    public void settings_report_bug(PreferenceFragmentCompat context) {
        Preference report_bug = context.findPreference("setting_report_bug");

        assert report_bug != null;
        report_bug.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(context.requireActivity(), ReportActivity.class);
            intent.putExtra("isTypeBug", true);
            context.startActivity(intent);
            return true;
        });
    }

    public void settings_permission(PreferenceFragmentCompat context) {
        Preference change_perm = context.findPreference("setting_permission");

        Activity ctx = context.requireActivity();

        assert change_perm != null;
        change_perm.setOnPreferenceClickListener(preference -> {
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(ctx);

            final View dialogLayout = ctx.getLayoutInflater().inflate(R.layout.desbloqueig_dialog, null);
            builder.setView(dialogLayout);
            builder.setNegativeButton(ctx.getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            // Posem el text adequat al dialog
            TextView TV_unlock_title = dialogLayout.findViewById(R.id.TV_unlock_title);
            TV_unlock_title.setText(ctx.getString(R.string.permisos));

            TextView TV_unlock_text = dialogLayout.findViewById(R.id.TV_unlock_text);
            TV_unlock_text.setText(ctx.getString(R.string.password));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                TV_unlock_text.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }

            TextView TV_pwd_error = dialogLayout.findViewById(R.id.TV_pwd_error);
            TV_pwd_error.setVisibility(View.INVISIBLE);

            AlertDialog alertDialog = builder.show();

            Button BT_unlock = dialogLayout.findViewById(R.id.BT_dialog_unlock);
            BT_unlock.setOnClickListener(v1 -> {
                TV_pwd_error.setVisibility(View.INVISIBLE);

                assert sharedPreferences != null;

                EditText ET_unlock_pwd = dialogLayout.findViewById(R.id.ET_unlock_pwd);
                String pwd = Crypt.getSHA256(ET_unlock_pwd.getText().toString());

                isPasswordCorrect(pwd, valid -> {
                    if(valid){
                        Intent intent = new Intent(ctx, Permisos.class);
                        intent.putExtra("settings", true);
                        ctx.startActivity(intent);
                        alertDialog.dismiss();
                    }
                    else
                        TV_pwd_error.setVisibility(View.VISIBLE);
                });
            });
            return true;
        });
    }

    public void settings_pujar_informe(PreferenceFragmentCompat context) {
        Preference pujar_informe = context.findPreference("setting_pujar_informe");

        assert pujar_informe != null;
        pujar_informe.setOnPreferenceClickListener(preference -> {
            AdicticRepository repository = EntryPointAccessors.fromApplication(context.requireContext(), AdicticRepository.class);
            repository.sendAppUsage();
            return true;
        });

    }

    public void settings_change_language(PreferenceFragmentCompat context) {
        ListPreference language_preference = context.findPreference("setting_change_language");

        assert sharedPreferences != null;
        String selectedLanguage = sharedPreferences.getString("language", Locale.getDefault().getLanguage());

        assert language_preference != null;
        language_preference.setValue(selectedLanguage);
        if(language_preference.getEntry() == null || language_preference.getEntry().length()==0) language_preference.setSummary(context.getString(R.string.language_not_supported));
        else language_preference.setSummary(language_preference.getEntry());

        language_preference.setOnPreferenceChangeListener((preference, newValue) -> {
            sharedPreferences.edit().putString("language", (String) newValue).apply();

            Intent refresh = new Intent(context.requireActivity(), SplashScreen.class);
            context.requireActivity().finish();
            context.startActivity(refresh);

            return true;
        });
    }

    public void settings_tancar_sessio(PreferenceFragmentCompat context) {
        Preference tancarSessio = context.findPreference("setting_tancar_sessio");

        assert sharedPreferences != null;

        assert tancarSessio != null;
        tancarSessio.setOnPreferenceClickListener(preference -> {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {

                        Call<String> call;
                        if (!task.isSuccessful()) {
                            call = api.logout("");
                        } else {
                            // Get new Instance ID token
                            String token = task.getResult();
                            call = api.logout(Crypt.getAES(token));
                        }
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                super.onResponse(call, response);
                                if (response.isSuccessful()) {
                                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_USERNAME,null).apply();
                                    sharedPreferences.edit().putString(Constants.SHARED_PREFS_PASSWORD,null).apply();
                                    context.requireActivity().startActivity(new Intent(context.getActivity(), Login.class));
                                    context.requireActivity().finish();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                super.onFailure(call, t);
                            }
                        });

                    });
            return true;
        });
    }

    //endregion
}
