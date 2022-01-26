package com.adictic.common.util.hilt;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.adictic.common.util.App;
import com.adictic.common.util.Constants;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Objects;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public static SharedPreferences provideSharedPreferences(@ApplicationContext Context application){
        MasterKey masterKey = null;
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    Constants.MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(Constants.KEY_SIZE)
                    .build();

            masterKey = new MasterKey.Builder(application)
                    .setKeyGenParameterSpec(spec)
                    .build();
        } catch (Exception e) {
            Log.e(application.getClass().getSimpleName(), "Error on getting master key", e);
        }

        if(masterKey == null)
            return null;

        try {
            return EncryptedSharedPreferences.create(
                        application,
                        "values",
                        Objects.requireNonNull(masterKey), // calling the method above for creating MasterKey
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                );
        } catch (GeneralSecurityException | IOException generalSecurityException) {
            generalSecurityException.printStackTrace();
        }
        return null;
    }

    @Provides
    @Singleton
    public static Context provideApplication(@ApplicationContext Context context) { return context; }
}
