package com.adictic.common.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.adictic.common.R;
import com.adictic.common.entity.EventBlock;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.MonthEntity;
import com.adictic.common.entity.YearEntity;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Funcions {
    public static String millis2dateTime(long millis){
            DateTime date = new DateTime(millis);
            return date.toString("dd/MM/yyyy - HH:mm", Locale.getDefault());
    }

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

    public static boolean eventBlockIsActive(EventBlock eventBlock){
        int now = new DateTime().getMillisOfDay();
        if(eventBlock.startEvent > now || eventBlock.endEvent < now)
            return false;

        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        return eventBlock.days.contains(dayOfWeek);
    }
}
