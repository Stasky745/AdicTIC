package com.adictic.common.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.adictic.common.BuildConfig;
import com.adictic.common.R;
import com.adictic.common.entity.AppInfo;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.MonthEntity;
import com.adictic.common.entity.YearEntity;
import com.adictic.common.rest.Api;
import com.adictic.common.workers.UpdateTokenWorker;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class Funcions {
    private final static String TAG = "Funcions";

    public static String formatHora(int hora, int min){
        String res = "";

        if(hora < 10)
            res += "0"+hora;
        else
            res += hora;

        res += ":";

        if(min < 10)
            res += "0"+min;
        else
            res += min;

        return res;
    }

    public static String date2String(int dia, int mes, int any) {
        String data;
        if (dia < 10) data = "0" + dia + "-";
        else data = dia + "-";
        if (mes < 10) data += "0" + mes + "-";
        else data += mes + "-";

        return data + any;
    }

    public static void setIconDrawable(Context ctx, String pkgName, final ImageView d) {

        String URL = Global.BASE_URL_RELEASE;
        if(BuildConfig.DEBUG) URL = Global.BASE_URL_DEBUG;

        Uri imageUri = Uri.parse(URL).buildUpon()
                .appendPath("icons")
                .appendPath(pkgName)
                .build();

        Glide.with(ctx)
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(d);
    }

    public static String millis2horaString(Context context, long l){

        if(l == 0)
            return "0 " + context.getString(R.string.minutes);

        Pair<Integer, Integer> temps = millisToString(l);
        String hora = "";
        String minuts = "";

        if(temps.first > 0)
            hora = temps.first.toString() + " " + context.getString(R.string.hours);

        if(temps.second > 0)
            minuts = temps.second.toString() + " " + context.getString(R.string.minutes);

        if(!hora.equals("") && !minuts.equals(""))
            return hora + " " + minuts;

        return hora + minuts;
    }

    public static String millis2horaString(Context context, int l){
        Pair<Integer, Integer> temps = millisToString(l);
        String hora = "";
        String minuts = "";

        if(temps.first > 0)
            hora = temps.first.toString() + " " + context.getString(R.string.hours);

        if(temps.second > 0)
            minuts = temps.second.toString() + " " + context.getString(R.string.minutes);

        if(!hora.equals("") && !minuts.equals(""))
            return hora + " " + minuts;

        return hora + minuts;
    }

    public static Pair<Integer, Integer> millisToString(float l) {
        float minuts = (l%(1000*60*60))/(1000*60);
        int hores = (int) l/(1000*60*60);

        return new Pair<>(hores, (int) Math.floor(minuts));
    }

    // retorna -1 si no hi ha hora establerta
    public static Integer string2MillisOfDay(String time){
        if(time == null || time.equals(""))
            return null;

        String[] time2 = time.split(":");
        DateTime dateTime = new DateTime()
                .withHourOfDay(Integer.parseInt(time2[0]))
                .withMinuteOfHour(Integer.parseInt(time2[1]));

        return dateTime.getMillisOfDay();
    }

    public static String millisOfDay2String(int millis){
        DateTime dateTime = new DateTime()
                .withMillisOfDay(millis);

        return formatHora(dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
    }

    public static Map<Integer, Map<Integer, List<Integer>>> convertYearEntityToMap(List<YearEntity> yearList) {
        Map<Integer, Map<Integer, List<Integer>>> res = new HashMap<>();
        for (YearEntity yEntity : yearList) {
            Map<Integer, List<Integer>> mMap = new HashMap<>();
            for (MonthEntity mEntity : yEntity.months) {
                mMap.put(mEntity.month, mEntity.days);
            }
            res.put(yEntity.year, mMap);
        }

        return res;
    }

    /**
     * Retorna -1 als dos valors si no és un string acceptable
     **/
    public static Pair<Integer, Integer> stringToTime(String s) {
        int hour, minutes;
        String[] hora = s.split(":");

        if (hora.length != 2) {
            hour = -1;
            minutes = -1;
        } else {
            if (Integer.parseInt(hora[0]) < 0 || Integer.parseInt(hora[0]) > 23) {
                hour = -1;
                minutes = -1;
            } else if (Integer.parseInt(hora[1]) < 0 || Integer.parseInt(hora[1]) > 59) {
                hour = -1;
                minutes = -1;
            } else {
                hour = Integer.parseInt(hora[0]);
                minutes = Integer.parseInt(hora[1]);
            }
        }

        return new Pair<>(hour, minutes);
    }

    public static void canviarMesosDeServidor(Collection<GeneralUsage> generalUsages) {
        for (GeneralUsage generalUsage : generalUsages) {
            generalUsage.month -= 1;
        }
    }

    public static void canviarMesosAServidor(Collection<GeneralUsage> generalUsages) {
        for (GeneralUsage generalUsage : generalUsages) {
            generalUsage.month += 1;
        }
    }

    public static void canviarMesosDeServidor(List<YearEntity> yearList) {
        for (YearEntity yearEntity : yearList) {
            for (MonthEntity monthEntity : yearEntity.months) {
                monthEntity.month -= 1;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void closeKeyboard(View view, Activity a) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                hideSoftKeyboard(a);
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                closeKeyboard(innerView, a);
            }
        }
    }

    private static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isAcceptingText() && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    public static String getFullURL(String url) {
        if (!url.contains("https://")) return "https://" + url;
        else return url;
    }

    /**
     * SHARED PREFERENCES
     */

    protected static MasterKey getMasterKey(Context mCtx) {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    Constants.MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(Constants.KEY_SIZE)
                    .build();

            return new MasterKey.Builder(mCtx)
                    .setKeyGenParameterSpec(spec)
                    .build();
        } catch (Exception e) {
            Log.e(mCtx.getClass().getSimpleName(), "Error on getting master key", e);
        }
        return null;
    }

    public static SharedPreferences getEncryptedSharedPreferences(Context mCtx) {
        try {
            if(App.getSharedPreferences()==null) {
                App.setSharedPreferences(EncryptedSharedPreferences.create(
                        mCtx,
                        "values",
                        Objects.requireNonNull(getMasterKey(mCtx)), // calling the method above for creating MasterKey
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                ));
            }
            return App.getSharedPreferences();
        } catch (Exception e) {
            Log.e(mCtx.getClass().getSimpleName(), "Error on getting encrypted shared preferences", e);
        }
        return null;
    }

    private static EncryptedFile getEncryptedFile(Context mCtx, String fileName, boolean write){
        File file = new File(mCtx.getFilesDir(),fileName);

        try {
            boolean wasSuccessful = true;
            if(write && file.exists())
                wasSuccessful = file.delete();

            if(!wasSuccessful) {
                String TAG = "Funcions (getEncryptedFile)";
                Log.i(TAG, "No s'ha pogut esborrar el fitxer: " + fileName);
                return null;
            }

            return new EncryptedFile.Builder(
                    mCtx,
                    file,
                    Objects.requireNonNull(getMasterKey(mCtx)),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setAdminPhoto(Context ctx, Long idAdmin, final ImageView d){
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

        Glide.with(ctx)
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(d);

        App.setAdminPic(d.getDrawable());
    }

    public static boolean eventBlockIsActive(EventBlock eventBlock){
        int now = new DateTime().getMillisOfDay();
        if(eventBlock.startEvent > now || eventBlock.endEvent < now)
            return false;

        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek){
            case 1:
                return eventBlock.sunday;
            case 2:
                return eventBlock.monday;
            case 3:
                return eventBlock.tuesday;
            case 4:
                return eventBlock.wednesday;
            case 5:
                return eventBlock.thursday;
            case 6:
                return eventBlock.friday;
            default:
                return eventBlock.saturday;
        }
    }

    /**
     * pre: si fTime = -1, agafa valors del dia actual inacabat
     * post: retorna la llista amb els mesos ja adaptats pel servidor (+1)
     **/
    public static List<GeneralUsage> getGeneralUsages(Context mContext, int nDies) {
        List<GeneralUsage> gul = new ArrayList<>();

        long timer = System.currentTimeMillis();

        long initialTime, finalTime;

        for(int i = 0; i <= nDies; i++){
            DateTime initialDate = new DateTime();
            initialDate = initialDate.withTimeAtStartOfDay();
            initialDate = initialDate.minusDays(i);

            DateTime finalDate = new DateTime(initialDate).plusDays(1);

            initialTime = initialDate.getMillis();

            finalTime = Math.min(finalDate.getMillis(), DateTime.now().getMillis());

            List<AppUsage> appUsages = getAppUsages(mContext, initialTime, finalTime);

            GeneralUsage gu = new GeneralUsage();
            gu.day = initialDate.getDayOfMonth();
            gu.month = initialDate.getMonthOfYear();
            gu.year = initialDate.getYear();
            gu.usage = appUsages;

            gu.totalTime = appUsages.stream()
                    .mapToLong(appUsage -> appUsage.totalTime)
                    .sum();

            gul.add(gu);
        }

        System.out.println("TIME: " + (System.currentTimeMillis() - timer));

        return gul;
    }

    private static List<AppUsage> getAppUsages(Context mContext, long initialTime, long finalTime) {
        UsageEvents.Event currentEvent;
        List<UsageEvents.Event> allEvents = new ArrayList<>();
        HashMap<String, AppUsage> map = new HashMap<>();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        assert  mUsageStatsManager != null;
        PackageManager mPm = mContext.getPackageManager();

        UsageEvents usageEvents = mUsageStatsManager.queryEvents(initialTime, finalTime);

        //capturing all events in a array to compare with next element

        while (usageEvents.hasNextEvent()) {
            currentEvent = new UsageEvents.Event();
            usageEvents.getNextEvent(currentEvent);
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
            return new ArrayList<>();

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

        return new ArrayList<>(map.values());
    }

    public static void askChildForLiveApp(Context ctx, long idChild, boolean liveApp) {
        Api mTodoService = ((App) (ctx.getApplicationContext())).getAPI();
        Call<String> call = mTodoService.askChildForLiveApp(idChild, liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if (!response.isSuccessful()) {
                    Toast toast = Toast.makeText(ctx, ctx.getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Toast toast = Toast.makeText(ctx, ctx.getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public static void runUpdateTokenWorker(Context mContext){
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

        WorkManager.getInstance(mContext)
                .enqueueUniquePeriodicWork("UpdateTokenWorker", ExistingPeriodicWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker UpdateToken Configurat");
    }

    public static Spanned getSpannedText(Context context, String string){
        SpannableString spannableString;
        if(string.startsWith("- ") ||
            string.startsWith("* ") ||
            string.startsWith("· ")){
            spannableString = new SpannableString(string.substring(2));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                spannableString.setSpan(new BulletSpan(BulletSpan.STANDARD_GAP_WIDTH, context.getColor(R.color.colorPrimary)), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            else
                spannableString.setSpan(new BulletSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else
            spannableString = new SpannableString(string);
        return HtmlCompat.fromHtml(spannableString.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);
    }

    public static ClickableSpan getClickableSpan(Context ctx, Intent intent) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                ctx.startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setFakeBoldText(true);
                ds.setUnderlineText(false);
            }
        };
    }
}
