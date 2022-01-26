package com.adictic.client.util.hilt;

import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
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
import com.adictic.client.rest.AdicticApi;
import com.adictic.client.service.AccessibilityScreenService;
import com.adictic.client.service.ClientNotificationManager;
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
import com.adictic.common.dao.BlockedAppDao;
import com.adictic.common.dao.EventBlockDao;
import com.adictic.common.dao.HorarisNitDao;
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
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.hilt.Repository;
import com.bumptech.glide.RequestManager;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class AdicticRepository extends Repository {
    private final BlockedAppDao blockedAppDao;
    private final EventBlockDao eventBlockDao;
    private final HorarisNitDao horarisNitDao;

    private final AdicticApi api;

    private final RequestManager glideRequestManager;

    private final SharedPreferences sharedPreferences;

    private final Application application;
    private final WorkManager workManager;
    private final UsageStatsManager usageStatsManager;
    private final ClientNotificationManager clientNotificationManager;

    public AdicticRepository(Application application, ClientNotificationManager clientNotificationManager, WorkManager workManager, UsageStatsManager usageStatsManager, AdicticApi api, BlockedAppDao blockedAppDao, EventBlockDao eventBlockDao, HorarisNitDao horarisNitDao, RequestManager glideRequestManager, SharedPreferences sharedPreferences) {
        super(application, api, blockedAppDao, eventBlockDao, horarisNitDao, glideRequestManager, sharedPreferences);
        this.application = application;
        this.clientNotificationManager = clientNotificationManager;
        this.workManager = workManager;
        this.usageStatsManager = usageStatsManager;
        this.api = api;
        this.blockedAppDao = blockedAppDao;
        this.eventBlockDao = eventBlockDao;
        this.horarisNitDao = horarisNitDao;
        this.glideRequestManager = glideRequestManager;
        this.sharedPreferences = sharedPreferences;
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

    //region API

    public AdicticApi getApi() {
        return api;
    }


    //region Events

    public void checkEvents() {
        // Agafem els horaris de la nit i Events
        Call<EventsAPI> call = api.getEvents(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1));
        call.enqueue(new Callback<EventsAPI>() {
            @Override
            public void onResponse(@NonNull Call<EventsAPI> call, @NonNull Response<EventsAPI> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<EventBlock> eventBlocks = new ArrayList<>(response.body().events);

                    updateEvents(eventBlocks);

                    startHorarisEventsManagerWorker();

                    setEvents(eventBlocks);
                }
                else {
                    List<EventBlock> list = getAllEvents();

                    setEvents(list);
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventsAPI> call, @NonNull Throwable t) {
                List<EventBlock> list = getAllEvents();

                setEvents(list);
            }
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

    public void checkHoraris() {
        // Agafem els horaris de la nit i Events
        Call<HorarisAPI> call = api.getHoraris(sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER,-1));
        call.enqueue(new Callback<HorarisAPI>() {
            @Override
            public void onResponse(@NonNull Call<HorarisAPI> call, @NonNull Response<HorarisAPI> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Actualitzem BDD
                    List<HorarisNit> horarisList = new ArrayList<>(response.body().horarisNit);

                    updateHoraris(horarisList);

                    startHorarisEventsManagerWorker();

                    setHoraris(horarisList);
                }
                else {
                    List<HorarisNit> horarisNit = getAllHoraris();

                    setHoraris(horarisNit);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HorarisAPI> call, @NonNull Throwable t) {
                List<HorarisNit> horarisNit = getAllHoraris();

                setHoraris(horarisNit);
            }
        });
    }

    public void setHoraris(List<HorarisNit> horarisNit){
        // Aturem tots els workers d'Horaris que estiguin configurats
        workManager
                .cancelAllWorkByTag(Constants.WORKER_TAG_HORARIS_BLOCK);

        if(!accessibilityServiceOn())
            return;

        AccessibilityScreenService.instance.setHorarisActius(false);

        HorarisNit horariAvui = getHorariByDay(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));

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

        List<BlockedApp> listBlockedApps = getAllApps();

        Map<String, Long> mapUsage = getTimeLeftMapAccessibilityService(listBlockedApps, generalUsage);

        AccessibilityScreenService.instance.setTimeLeftMap(mapUsage);
        AccessibilityScreenService.instance.isCurrentAppBlocked();
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
}
