package com.example.adictic.util;

import android.graphics.Color;

import static androidx.security.crypto.MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE;
import static androidx.security.crypto.MasterKey.DEFAULT_MASTER_KEY_ALIAS;

public class Constants {
    public static final String MASTER_KEY_ALIAS = DEFAULT_MASTER_KEY_ALIAS;
    public static final int KEY_SIZE = DEFAULT_AES_GCM_MASTER_KEY_SIZE;

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
