package com.adictic.common.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.Spanned;
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
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.adictic.common.BuildConfig;
import com.adictic.common.R;
import com.adictic.common.entity.AppInfo;
import com.adictic.common.entity.AppUsage;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
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

    public static String millis2horaString(long l){
        Pair<Integer, Integer> temps = millisToString(l);
        String hora = "";
        String minuts = "";

        if(temps.first > 0)
            hora = temps.first.toString() + " " + R.string.hours;

        if(temps.second > 0)
            minuts = temps.second.toString() + " " + R.string.minutes;

        if(!hora.equals("") && !minuts.equals(""))
            return hora + " " + minuts;

        return hora + minuts;
    }

    public static Pair<Integer, Integer> millisToString(float l) {
        float minuts = l / (60000);
        int hores = 0;

        while (minuts >= 60) {
            hores++;
            minuts -= 60;
        }

        return new Pair<>(hores, Math.round(minuts));
    }

    // retorna -1 si no hi ha hora establerta
    public static int string2MillisOfDay(String time){
        if(time == null || time.equals(""))
            return -1;

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

    /**
     * pre: si fTime = -1, agafa valors del dia actual inacabat
     * post: retorna la llista amb els mesos ja adaptats pel servidor (+1)
     **/
    public static List<GeneralUsage> getGeneralUsages(Context mContext, int iTime, int fTime) {
        List<GeneralUsage> gul = new ArrayList<>();

        if (fTime == -1) {
            Calendar finalTime = Calendar.getInstance();

            Calendar initialTime = Calendar.getInstance();
            initialTime.set(Calendar.HOUR_OF_DAY, 0);
            initialTime.set(Calendar.MINUTE, 0);
            initialTime.set(Calendar.SECOND, 0);

            List<AppUsage> appUsages = getAppUsages(mContext, initialTime, finalTime);

            GeneralUsage gu = new GeneralUsage();
            gu.day = finalTime.get(Calendar.DAY_OF_MONTH);
            gu.month = finalTime.get(Calendar.MONTH) + 1;
            gu.year = finalTime.get(Calendar.YEAR);
            gu.usage = appUsages;

            gu.totalTime = 0L;
            for (AppUsage au : appUsages) {
                gu.totalTime += au.totalTime;
            }

            gul.add(gu);
        } else {
            for (int i = iTime; i <= fTime; i++) {
                Calendar finalTime = Calendar.getInstance();
                finalTime.set(Calendar.DAY_OF_YEAR, i);
                finalTime.set(Calendar.HOUR_OF_DAY, 23);
                finalTime.set(Calendar.MINUTE, 59);
                finalTime.set(Calendar.SECOND, 59);

                Calendar initialTime = Calendar.getInstance();
                initialTime.set(Calendar.DAY_OF_YEAR, i);
                initialTime.set(Calendar.HOUR_OF_DAY, 0);
                initialTime.set(Calendar.MINUTE, 0);
                initialTime.set(Calendar.SECOND, 0);

                List<AppUsage> appUsages = getAppUsages(mContext, initialTime, finalTime);

                GeneralUsage gu = new GeneralUsage();
                gu.day = finalTime.get(Calendar.DAY_OF_MONTH);
                gu.month = finalTime.get(Calendar.MONTH) + 1;
                gu.year = finalTime.get(Calendar.YEAR);
                gu.usage = appUsages;

                gu.totalTime = 0L;
                for (AppUsage au : appUsages) {
                    gu.totalTime += au.totalTime;
                }

                gul.add(gu);
            }
        }
        return gul;
    }

    private static List<AppUsage> getAppUsages(Context mContext, Calendar initialTime, Calendar finalTime) {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);
        PackageManager mPm = mContext.getPackageManager();

        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,
                initialTime.getTimeInMillis(), finalTime.getTimeInMillis());

        List<AppUsage> appUsages = new ArrayList<>();
        final int statCount = stats.size();
        for (int j = 0; j < statCount; j++) {
            final android.app.usage.UsageStats pkgStats = stats.get(j);
            ApplicationInfo appInfo = null;
            try {
                appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                //e.printStackTrace();
            }
            if (appInfo != null && pkgStats.getLastTimeUsed() >= initialTime.getTimeInMillis() && pkgStats.getLastTimeUsed() <= finalTime.getTimeInMillis() && pkgStats.getTotalTimeInForeground() > 5000 && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                AppUsage appUsage = new AppUsage();
                appUsage.app = new AppInfo();

                if (Build.VERSION.SDK_INT >= 26) appUsage.app.category = appInfo.category;
                appUsage.app.appName = mPm.getApplicationLabel(appInfo).toString();
                appUsage.app.pkgName = pkgStats.getPackageName();
                appUsage.lastTimeUsed = pkgStats.getLastTimeUsed();
                appUsage.totalTime = pkgStats.getTotalTimeInForeground();
                appUsages.add(appUsage);
            }
        }

        return appUsages;
    }

    public static void askChildForLiveApp(Context ctx, long idChild, boolean liveApp) {
        Api mTodoService = ((App) (ctx.getApplicationContext())).getAPI();
        Call<String> call = mTodoService.askChildForLiveApp(idChild, liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (!response.isSuccessful()) {
                    Toast toast = Toast.makeText(ctx, ctx.getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast toast = Toast.makeText(ctx, ctx.getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    public static void runUpdateTokenWorker(Context mContext, long idUser, String token, long delay){
        Data.Builder data = new Data.Builder();
        data.putLong("idUser", idUser);
        data.putString("token", token);

        OneTimeWorkRequest myWork =
                new OneTimeWorkRequest.Builder(UpdateTokenWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(data.build())
                        .build();

        WorkManager.getInstance(mContext)
                .enqueueUniqueWork("UpdateTokenWorker", ExistingWorkPolicy.REPLACE, myWork);

        Log.d(TAG,"Worker UpdateToken Configurat - ID=" + idUser + " | delay=" + delay);
    }

    public static Spanned getSpannedText(String s){
        return HtmlCompat.fromHtml(s, HtmlCompat.FROM_HTML_MODE_LEGACY);
    }
}
