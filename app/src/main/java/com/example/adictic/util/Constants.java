package com.example.adictic.util;

import android.graphics.Color;

import java.util.Arrays;

import static androidx.security.crypto.MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE;
import static androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS;
import static java.lang.Math.round;

public class Constants {
    public static final long HOUR_IN_MILLIS = 3600000;
    public static final int TOTAL_MILLIS_IN_DAY = 86400000;

    public static final String MASTER_KEY_ALIAS = DEFAULT_MASTER_KEY_ALIAS;
    public static final int KEY_SIZE = DEFAULT_AES_GCM_MASTER_KEY_SIZE;

    public static final String FILE_BLOCKED_APPS = "blocked_apps";
    public static final String FILE_EVENT_BLOCK = "event_block";
    public static final String FILE_FREE_USE_APPS = "freeuse_apps";
    public static final String FILE_HORARIS_NIT = "horaris_nit";
    public static final String FILE_CURRENT_BLOCKED_APPS = "current_blocked_apps";

    public static final String SHARED_PREFS_CHANGE_BLOCKED_APPS = "blocked_apps_change";
    public static final String SHARED_PREFS_CHANGE_EVENT_BLOCK = "event_block_change";
    public static final String SHARED_PREFS_CHANGE_FREE_USE_APPS = "freeuse_apps_change";
    public static final String SHARED_PREFS_CHANGE_HORARIS_NIT = "horaris_nit_change";

    public static final String SHARED_PREFS_IDUSER = "idUser";
    public static final String SHARED_PREFS_IDTUTOR = "idTutor";
    public static final String SHARED_PREFS_ISTUTOR = "isTutor";
    public static final String SHARED_PREFS_BLOCKEDDEVICE = "blockedDevice";
    public static final String SHARED_PREFS_FREEUSE = "freeuse";
    public static final String SHARED_PREFS_DAYOFYEAR = "dayOfYear";
    public static final String SHARED_PREFS_LIVEAPP = "liveApp";

    public static final String SHARED_PREFS_APPUSAGEWORKERUPDATE = "appUsageWorkerUpdate";
    public static final String SHARED_PREFS_LASTUPDATEAPPUSAGEWORKER = "lastUpdateAppUsageWorker";

    public static final String SHARED_PREFS_ACTIVE_EVENTS = "current_active_events";
    public static final String SHARED_PREFS_ACTIVE_HORARIS_NIT = "blockedDeviceNit";

    public static final String CHANNEL_ID = "my_channel_01";
    public static final String CHANNEL_NAME = "Standard Notification";
    public static final String CHANNEL_DESCRIPTION = "Trying out notifications";

    public static final String WORKER_TAG_BLOCK_APPS = "blocked_apps_worker_tag";
    public static final String WORKER_TAG_EVENT_BLOCK = "event_block_worker_tag";

    public static final int[] GRAPH_COLORS = {
            Color.parseColor("#3c9df8"), Color.parseColor("#deefff"), Color.parseColor("#76b3ec"),
            Color.parseColor("#2390F5"), Color.parseColor("#1b62a5")
    };

    public static int CORRECT_USAGE_DAY = 3;
    public static int DANGEROUS_USAGE_DAY = 5;
    public static int CORRECT_USAGE_APP = 2;
    public static int DANGEROUS_USAGE_APP = 4;

    public static final long[] AGE_TIMES_MILLIS = createAgeTimesMillis();
    public static final double[] AGE_TIMES = createAgeTimes();

    private static long[] createAgeTimesMillis(){
        long[] array = new long[30];
        Arrays.fill(array, 0, 2, 0);
        Arrays.fill(array, 2, 12, HOUR_IN_MILLIS);
        Arrays.fill(array, 12, 15, Math.round(1.5 * HOUR_IN_MILLIS));
        Arrays.fill(array, 15, 30, 2 * HOUR_IN_MILLIS);

        return array;
    }

    private static double[] createAgeTimes(){
        double[] array = new double[30];
        Arrays.fill(array, 0, 2, 0);
        Arrays.fill(array, 2, 12, 1);
        Arrays.fill(array, 12, 15, 1.5);
        Arrays.fill(array, 15, 30, 2);

        return array;
    }
}
