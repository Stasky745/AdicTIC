package com.adictic.common.util;

import android.content.Context;
import android.util.Log;

import com.adictic.common.rest.Api;
import com.google.auto.service.AutoService;

import org.acra.ReportField;
import org.acra.config.CoreConfiguration;
import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.acra.sender.ReportSenderFactory;
import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcraSender implements ReportSender {

    private static final String TAG = "AcraSender";

    @Override
    public void send(@NotNull Context context, @NotNull CrashReportData errorContent) throws ReportSenderException {
        try {
            Api api = ((App) context.getApplicationContext()).getAPI();
            Call<String> callSendCrash = api.sendCrashACRA(errorContent.getString(ReportField.PACKAGE_NAME), errorContent.getString(ReportField.APP_VERSION_NAME), errorContent.toJSON().replace("\"","`"));
            callSendCrash.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(!response.isSuccessful()){
                        Log.e(TAG,"Error al enviar al servidor!");
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    // TODO: Implementar repetició
                    t.printStackTrace();
                }
            });

            Log.d(TAG, "Report Sent!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Report crash! - This is very bad xD");
        }
    }

    @AutoService(ReportSenderFactory.class)
    public static class MyAcraSenderFactory implements ReportSenderFactory {
        @NotNull
        @Override
        public ReportSender create(@NotNull Context context, @NotNull CoreConfiguration coreConfiguration) {
            return new AcraSender();
        }
    }
}
