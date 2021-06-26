package com.example.adictic_admin.util;

import android.graphics.Color;

import java.util.Arrays;

public class Constants {
    public static final long HOUR_IN_MILLIS = 3600000;
    public static final int TOTAL_MILLIS_IN_DAY = 86400000;

    public static final String SHARED_PREFS_PATCH_NOTES = "patch_notes";

    public static final int[] GRAPH_COLORS = {
            Color.parseColor("#3c9df8"), Color.parseColor("#deefff"), Color.parseColor("#76b3ec"),
            Color.parseColor("#2390F5"), Color.parseColor("#1b62a5")
    };

    public static final String CHANNEL_ID = "my_channel_01";
    public static final String CHANNEL_NAME = "Standard Notification";
    public static final String CHANNEL_DESCRIPTION = "Trying out notifications";

    public static final String SHARED_PREFS_ID_USER = "userId";
    public static final String SHARED_PREFS_ID_ADMIN = "adminId";
    public static final String SHARED_PREFS_USERNAME = "username";
    public static final String SHARED_PREFS_PASSWORD = "password";
    public static final String SHARED_PREFS_TOKEN = "firebaseToken";

    public static int CORRECT_USAGE_DAY = 3;
    public static int DANGEROUS_USAGE_DAY = 5;
    public static int CORRECT_USAGE_APP = 2;
    public static int DANGEROUS_USAGE_APP = 4;

    public static final long[] AGE_TIMES_MILLIS = createAgeTimesMillis();
    public static final String[] AGE_TIMES_STRING = createAgeTimesString();

    private static long[] createAgeTimesMillis(){
        long[] array = new long[30];
        Arrays.fill(array, 0, 3, 0);
        array[3] = Math.round(0.5 * HOUR_IN_MILLIS);
        array[4] = Math.round(0.75 * HOUR_IN_MILLIS);
        Arrays.fill(array, 5, 13, HOUR_IN_MILLIS);
        Arrays.fill(array, 13, 16, Math.round(1.5 * HOUR_IN_MILLIS));
        Arrays.fill(array, 16, 30, 2 * HOUR_IN_MILLIS);

        return array;
    }

    private static String[] createAgeTimesString(){
        String[] array = new String[30];
        Arrays.fill(array, 0, 3, "0 hores");
        array[3] = "30 minuts";
        array[4] = "45 minuts";
        Arrays.fill(array, 5, 13, "1 hora");
        Arrays.fill(array, 13, 16, "1:30 hores");
        Arrays.fill(array, 16, 30, "2 hores");

        return array;
    }
}
