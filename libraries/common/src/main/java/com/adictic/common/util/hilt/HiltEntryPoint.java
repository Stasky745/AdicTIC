package com.adictic.common.util.hilt;

import android.content.SharedPreferences;

import com.adictic.common.util.hilt.Repository;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@EntryPoint
@InstallIn(SingletonComponent.class)
public interface HiltEntryPoint {
    Repository getRepository();
    SharedPreferences getSharedPrefs();
}
