package com.adictic.client.util;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.adictic.client.R;

import java.util.concurrent.Executor;

public class BiometricAuthUtil {

    private static final int authenticators = Authenticators.BIOMETRIC_WEAK | Authenticators.DEVICE_CREDENTIAL;

    public static void startAuthentication(Context context, BiometricPrompt.AuthenticationCallback bioCallback) throws UnsupportedOperationException{
        if(!isAuthenticationEnabled(context)) throw new UnsupportedOperationException();
        Executor executor = ContextCompat.getMainExecutor(context);
        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) context, executor, bioCallback);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.unlockAdictic))
                .setNegativeButtonText(context.getString(R.string.enterWithPassword)) //Si es posa el setAllowedAuthenticators s'ha de treure aquesta opció
                //.setAllowedAuthenticators(authenticators) //Això peta al entrar en la opció de posar PIN (almenys en el meu dispositiu EMUI 10)
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    public static boolean isAuthenticationEnabled(Context context) {
        return BiometricManager.from(context).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static int isAuthenticationSupported(Context context) {
        return BiometricManager.from(context).canAuthenticate(authenticators);
    }

    public static void createBiometricCredentials(Fragment fragment){
        final Intent enrollIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, authenticators);
            fragment.startActivityForResult(enrollIntent, 4567);
        }
    }
}
