package com.example.adictic.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.adictic.TodoApp;
import com.example.adictic.entity.GeoFill;
import com.example.adictic.rest.TodoApi;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.xml.transform.Result;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeoLocWorker extends Worker {

    private final Context mContext;
    private static final String TAG = GeoLocWorker.class.getSimpleName();
    private GeoPoint currentLocation;
    private TodoApi mTodoService;

    int compt = 0;

    public GeoLocWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mTodoService = ((TodoApp)getApplicationContext()).getAPI();

        if(isNetworkEnabled){
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,null);

            Log.d(TAG,"Network Enabled");

            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if( location != null ) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            enviarLoc();
            return Result.success();
        }
        else if(isGPSEnabled){
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,null);

            Log.d(TAG,"GPS Enabled");

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if( location != null ) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }

            enviarLoc();
            return Result.success();
        }
        else if(compt < 7){
            compt++;
            return Result.retry();
        }
        else return Result.failure();
    }

    private void enviarLoc(){
        GeoFill fill = new GeoFill();
        fill.longitud = currentLocation.getLongitude();
        fill.latitud = currentLocation.getLatitude();

        String CURRENT_TIME_FORMAT = "HH:mm dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(CURRENT_TIME_FORMAT, Locale.getDefault());
        fill.hora = dateFormat.format(Calendar.getInstance().getTime());

        retrofit2.Call<String> call = mTodoService.postCurrentLocation(TodoApp.getIDChild(),fill);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) { }

            @Override
            public void onFailure(Call<String> call, Throwable t) { }
        });

    }
}