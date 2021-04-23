package com.example.adictic.util;

import android.graphics.Color;

import static androidx.security.crypto.MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE;
import static androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS;

public class Constants {
    public static final String MASTER_KEY_ALIAS = DEFAULT_MASTER_KEY_ALIAS;
    public static final int KEY_SIZE = DEFAULT_AES_GCM_MASTER_KEY_SIZE;

    public static final String FILE_BLOCKED_APPS = "blocked_apps";
    public static final String FILE_EVENT_BLOCK = "event_block";
    public static final String FILE_FREE_USE_APPS = "freeuse_apps";
    public static final String FILE_HORARIS_NIT = "horaris_nit";

    public static final String SHARED_PREFS_CHANGE_BLOCKED_APPS = "blocked_apps_change";
    public static final String SHARED_PREFS_CHANGE_EVENT_BLOCK = "event_block_change";
    public static final String SHARED_PREFS_CHANGE_FREE_USE_APPS = "freeuse_apps_change";
    public static final String SHARED_PREFS_CHANGE_HORARIS_NIT = "horaris_nit_change";

    public static final String CHANNEL_ID = "my_channel_01";
    public static final String CHANNEL_NAME = "Standard Notification";
    public static final String CHANNEL_DESCRIPTION = "Trying out notifications";

    public static final int[] GRAPH_COLORS = {
            Color.parseColor("#3c9df8"), Color.parseColor("#deefff"), Color.parseColor("#76b3ec"),
            Color.parseColor("#2390F5"), Color.parseColor("#1b62a5")
    };

    public static int CORRECT_USAGE_DAY = 3;
    public static int DANGEROUS_USAGE_DAY = 5;
    public static int CORRECT_USAGE_APP = 2;
    public static int DANGEROUS_USAGE_APP = 4;
}
