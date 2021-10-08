package com.adictic.common.ui.main;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.adictic.common.R;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.FillNom;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.LiveApp;
import com.adictic.common.rest.Api;
import com.adictic.common.ui.BlockAppsActivity;
import com.adictic.common.ui.DayUsageActivity;
import com.adictic.common.ui.GeoLocActivity;
import com.adictic.common.ui.HorarisActivity;
import com.adictic.common.ui.events.EventsActivity;
import com.adictic.common.ui.informe.InformeActivity;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class MainParentFragment extends Fragment {
    private final static String ARG_FILL = "arg_fill";

    private final static String TAG = "MainParentFragment";

    private final int STATE_NEUTRAL = 1;
    private final int STATE_BLOCKDEVICE = 2;
    private final int STATE_FREEUSE = 0;

    private int lastDeviceState;

    private Api mTodoService;
    private long idChildSelected = -1;
    private View root;
    private FillNom fill;
    private boolean isTutor = false;

    // ---------- CACHE -----------
    private long ultimaActualitzacioDades = 0L;
    private long totalUsageTime = 0;
    private int timesUnlocked = 0;
    private final Map<String, AppUsage> appUsageMap = new HashMap<>();

    private ImageView IV_liveIcon;
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("idChild").equals(String.valueOf(idChildSelected))) {
                String pkgName = intent.getStringExtra("pkgName");
                if(pkgName.equals("-1"))
                    setLastLiveApp();
                else {
                    TextView currentApp = root.findViewById(R.id.TV_CurrentApp);
                    try {
                        IV_liveIcon.setVisibility(View.VISIBLE);
                        Funcions.setIconDrawable(requireContext(), pkgName, IV_liveIcon);
                        String appName = intent.getStringExtra("appName");
                        currentApp.setText(appName);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private PieChart pieChart;

    public MainParentFragment() { }

    public static MainParentFragment newInstance(FillNom fill) {
        MainParentFragment mainParentFragment = new MainParentFragment();

        Bundle args = new Bundle(1);
        args.putParcelable(ARG_FILL, fill);
        mainParentFragment.setArguments(args);

        return mainParentFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.main_parent, container, false);

        getBundle();

        mTodoService = ((App) requireActivity().getApplicationContext()).getAPI();
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getActivity());
        assert sharedPreferences != null;

        isTutor = sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false);

        root.findViewById(R.id.Ch_Pie).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.TV_PieApp).setVisibility(View.INVISIBLE);
        root.findViewById(R.id.TV_timesUnlocked).setVisibility(View.GONE);

        IV_liveIcon = root.findViewById(R.id.IV_CurrentApp);
        IV_liveIcon.setVisibility(View.INVISIBLE);

        // LiveApp Broadcast
        if (isTutor) {
            LocalBroadcastManager.getInstance(root.getContext()).registerReceiver(messageReceiver,
                    new IntentFilter("liveApp"));
        }

        setButtons();

        return root;
    }

    private void getBundle() {
        Bundle arguments = getArguments();
        if(arguments == null)
            return;

        fill = arguments.getParcelable(ARG_FILL);
        idChildSelected = fill.idChild;
    }

    @Override
    public void onResume() {
        super.onResume();

        setLiveApp();

        // ----- Crear gràfiques -----
        // Fa +5 minuts des que s'han agafat dades?
        boolean actualitzarDades = System.currentTimeMillis() - ultimaActualitzacioDades > 1000*60*5;

        if(actualitzarDades){
            if(isTutor)
                getUsageFromServer();
            else
                makeGraph(Funcions.getGeneralUsages(getActivity(), 0));
        }
    }

    private void setLiveApp() {
        if(isTutor) {
            Funcions.askChildForLiveApp(requireContext(), idChildSelected, true);
            setLastLiveApp();
        }
        else{
            IV_liveIcon.setVisibility(View.GONE);
            TextView currentApp = root.findViewById(R.id.TV_CurrentApp);
            currentApp.setVisibility(View.GONE);
        }
    }

    private void setLastLiveApp(){
        Call<LiveApp> call = mTodoService.getLastAppUsed(idChildSelected);
        call.enqueue(new Callback<LiveApp>() {
            @Override
            public void onResponse(@NonNull Call<LiveApp> call, @NonNull Response<LiveApp> response) {
                super.onResponse(call, response);
                if (response.isSuccessful() && response.body() != null)
                    setLiveAppMenu(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<LiveApp> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    private void setLiveAppMenu(LiveApp liveApp){
        if(liveApp.pkgName != null && !liveApp.pkgName.equals("-1")) {
            try {
                IV_liveIcon.setVisibility(View.VISIBLE);
                Funcions.setIconDrawable(requireContext(), liveApp.pkgName, IV_liveIcon);
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
                return;
            }

            TextView currentApp = root.findViewById(R.id.TV_CurrentApp);

            DateTime hora = new DateTime(liveApp.time);
            String liveAppText;
            DateTimeFormatter fmt;
            if (DateTime.now().getMillis() - hora.getMillis() < Constants.TOTAL_MILLIS_IN_DAY) {
                fmt = DateTimeFormat.forPattern("HH:mm");
            } else {
                fmt = DateTimeFormat.forPattern("dd/MM");
            }
            liveAppText = liveApp.appName + "\n" + hora.toString(fmt);

            currentApp.setText(liveAppText);
        }
    }

    private void setButtons() {
        final ConstraintLayout CL_info = root.findViewById(R.id.CL_resumUs);
        CL_info.setVisibility(View.INVISIBLE);
        final ConstraintLayout CL_infoButtons = root.findViewById(R.id.CL_ResumUsButons);
        CL_infoButtons.setVisibility(View.INVISIBLE);
        final ConstraintLayout CL_limit = root.findViewById(R.id.CL_LimitsApps);
        CL_limit.setVisibility(View.INVISIBLE);
        final ConstraintLayout CL_limitButtons = root.findViewById(R.id.CL_LimitsAppsButtons);
        CL_limitButtons.setVisibility(View.INVISIBLE);
        final ConstraintLayout CL_Geoloc = root.findViewById(R.id.CL_geoloc);

        // DailyLimit
        Button BT_dailyLimit = root.findViewById(R.id.BT_dailyLimit);
        if(fill.dailyLimit != null && fill.dailyLimit > 0){
            LocalTime limit = new LocalTime().withMillisOfDay(fill.dailyLimit);
            String bt_string = getString(R.string.daily_limit_device) + "   (" + Funcions.millis2horaString(requireContext(), fill.dailyLimit) + ")";
            BT_dailyLimit.setText(bt_string);
        }

        View.OnClickListener dailyLimit = v -> {
            TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
                enviarTempsLimit(BT_dailyLimit, hourOfDay, minute);
            };
            int hourOfDay;
            int minute;
            if(fill.dailyLimit != null) {
                LocalTime localTime = new LocalTime().withMillisOfDay(fill.dailyLimit);
                hourOfDay = localTime.getHourOfDay();
                minute = localTime.getMinuteOfHour();
            }
            else{
                hourOfDay = 0;
                minute = 0;
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), R.style.datePicker, timeSetListener, hourOfDay, minute, true);
            timePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.esborrar), (dialogInterface, i) -> enviarTempsLimit(BT_dailyLimit, 0, 0));
            timePickerDialog.show();
        };

        if(isTutor)
            BT_dailyLimit.setOnClickListener(dailyLimit);

        // BlockAppsActivity
        View.OnClickListener blockApps = v -> {
            Intent i = new Intent(getActivity(), BlockAppsActivity.class);
            i.putExtra("idChild", idChildSelected);
            startActivity(i);
        };

        Button BT_BlockApps = root.findViewById(R.id.BT_BlockApps);
        BT_BlockApps.setOnClickListener(blockApps);

        // InformeActivity
        View.OnClickListener informe = v -> {
            Intent i = new Intent(getActivity(), InformeActivity.class);
            i.putExtra("idChild", idChildSelected);
            startActivity(i);
        };

        Button BT_Informe = root.findViewById(R.id.BT_InformeMensual);
        BT_Informe.setOnClickListener(informe);

        // DayUsageActivity
        View.OnClickListener appUsage = v -> {
            Intent i = new Intent(getActivity(), DayUsageActivity.class);
            i.putExtra("idChild", idChildSelected);
            startActivity(i);
        };

        Button BT_appUse = root.findViewById(R.id.BT_UsApps);
        BT_appUse.setOnClickListener(appUsage);

        // EventsActivity
        View.OnClickListener horaris = v -> {
            Intent i = new Intent(getActivity(), EventsActivity.class);
            i.putExtra("idChild", idChildSelected);
            startActivity(i);
        };

        Button BT_horaris = root.findViewById(R.id.BT_Events);
        BT_horaris.setOnClickListener(horaris);

        // GeoLocActivity
        View.OnClickListener geoloc = v -> {
            Intent i = new Intent(getActivity(), GeoLocActivity.class);
            i.putExtra("idChild", idChildSelected);
            startActivity(i);
        };

        if(isTutor)
            CL_Geoloc.setOnClickListener(geoloc);
        else
            CL_Geoloc.setVisibility(View.GONE);

        // seekbar
        setSeekbar();

        // HorarisActivity
        Button nitButton = root.findViewById(R.id.BT_Horaris);
        nitButton.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), HorarisActivity.class);
            i.putExtra("idChild", idChildSelected);
            startActivity(i);
        });

        // CL_Informes
        CL_info.setOnClickListener(v -> {
            if (CL_infoButtons.getVisibility() == View.GONE) {
                CL_infoButtons.setVisibility(View.VISIBLE);

                ImageView IV_openInfo = root.findViewById(R.id.IV_openResumUs);
                IV_openInfo.setImageResource(R.drawable.ic_arrow_close);
            } else {
                CL_infoButtons.setVisibility(View.GONE);

                ImageView IV_openInfo = root.findViewById(R.id.IV_openResumUs);
                IV_openInfo.setImageResource(R.drawable.ic_arrow_open);
            }
        });

        /* Posar icona de desplegar en la posició correcta **/
        if (CL_infoButtons.getVisibility() == View.GONE) {
            ImageView IV_openInfo = root.findViewById(R.id.IV_openResumUs);
            IV_openInfo.setImageResource(R.drawable.ic_arrow_open);
        } else {
            ImageView IV_openInfo = root.findViewById(R.id.IV_openResumUs);
            IV_openInfo.setImageResource(R.drawable.ic_arrow_close);
        }

        // CL_Limits
        CL_limit.setOnClickListener(v -> {
            if (CL_limitButtons.getVisibility() == View.GONE) {
                CL_limitButtons.setVisibility(View.VISIBLE);

                ImageView IV_openLimit = root.findViewById(R.id.IV_openLimitsApps);
                IV_openLimit.setImageResource(R.drawable.ic_arrow_close);
            } else {
                CL_limitButtons.setVisibility(View.GONE);

                ImageView IV_openLimit = root.findViewById(R.id.IV_openLimitsApps);
                IV_openLimit.setImageResource(R.drawable.ic_arrow_open);
            }
        });

        /* Posar icona de desplegar en la posició correcta **/
        if (CL_limitButtons.getVisibility() == View.GONE) {
            ImageView IV_openLimit = root.findViewById(R.id.IV_openLimitsApps);
            IV_openLimit.setImageResource(R.drawable.ic_arrow_open);
        } else {
            ImageView IV_openLimit = root.findViewById(R.id.IV_openLimitsApps);
            IV_openLimit.setImageResource(R.drawable.ic_arrow_close);
        }
    }

    private void enviarTempsLimit(Button BT_dailyLimit, int hourOfDay, int minute) {
        // Enviar nou temps límit
        int newDailyLimit = (hourOfDay * 1000 * 60 * 60) + (minute * 1000 * 60);
        Call<String> call = mTodoService.postDailyLimit(fill.idChild, newDailyLimit);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if(response.isSuccessful()){
                    fill.dailyLimit = newDailyLimit;
                    if(newDailyLimit == 0)
                        BT_dailyLimit.setText(getString(R.string.daily_limit_device));
                    else{
                        String bt_string = getString(R.string.daily_limit_device) + "   (" + Funcions.millis2horaString(requireContext(), newDailyLimit) + ")";
                        BT_dailyLimit.setText(bt_string);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) { super.onFailure(call, t); }
        });
    }

    private void freeTimeServer(boolean freetime, int newState) {
        SeekBar SB_deviceState = root.findViewById(R.id.SB_deviceState);
        TextView TV_freeTime = root.findViewById(R.id.TV_freeTime);
        TextView TV_blockDevice = root.findViewById(R.id.TV_blockDevice);

        Call<String> call = mTodoService.freeUse(idChildSelected, freetime);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()){
                    if(freetime){
                        TV_freeTime.setTextColor(requireContext().getColor(R.color.colorPrimary));
                        TV_freeTime.setTypeface(Typeface.DEFAULT_BOLD);

                        TV_blockDevice.setTextColor(requireContext().getColor(R.color.gris));
                        TV_blockDevice.setTypeface(Typeface.DEFAULT);

                    }
                    else {
                        TV_freeTime.setTextColor(requireContext().getColor(R.color.gris));
                        TV_freeTime.setTypeface(Typeface.DEFAULT);

                        if(newState == STATE_BLOCKDEVICE) {
                            TV_blockDevice.setTextColor(requireContext().getColor(R.color.colorPrimary));
                            TV_blockDevice.setTypeface(Typeface.DEFAULT_BOLD);
                        }

                    }
                    lastDeviceState = newState;
                }
                else
                    SB_deviceState.setProgress(lastDeviceState);
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            super.onFailure(call, t);
                SB_deviceState.setProgress(lastDeviceState);
            }
        });
    }

    private void blockDeviceServer(boolean blockDevice, int newState) {
        SeekBar SB_deviceState = root.findViewById(R.id.SB_deviceState);
        TextView TV_blockDevice = root.findViewById(R.id.TV_blockDevice);
        TextView TV_freeTime = root.findViewById(R.id.TV_freeTime);

        Call<String> call;
        if (blockDevice)
            call = mTodoService.blockChild(idChildSelected);
        else
            call = mTodoService.unblockChild(idChildSelected);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if(blockDevice) {
                        TV_blockDevice.setTextColor(requireContext().getColor(R.color.colorPrimary));
                        TV_blockDevice.setTypeface(Typeface.DEFAULT_BOLD);

                        TV_freeTime.setTextColor(requireContext().getColor(R.color.gris));
                        TV_freeTime.setTypeface(Typeface.DEFAULT);

                    }
                    else {
                        TV_blockDevice.setTextColor(requireContext().getColor(R.color.gris));
                        TV_blockDevice.setTypeface(Typeface.DEFAULT);

                        if(newState == STATE_FREEUSE) {
                            TV_freeTime.setTextColor(requireContext().getColor(R.color.colorPrimary));
                            TV_freeTime.setTypeface(Typeface.DEFAULT_BOLD);
                        }

                    }
                    lastDeviceState = newState;
                }
                else {
                    SB_deviceState.setProgress(lastDeviceState);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                SB_deviceState.setProgress(lastDeviceState);
            }
        });
    }

    private void setSeekbar() {
        SeekBar SB_deviceState = root.findViewById(R.id.SB_deviceState);
        SB_deviceState.setVisibility(View.GONE);
        TextView TV_blockDevice = root.findViewById(R.id.TV_blockDevice);
        TV_blockDevice.setVisibility(View.GONE);
        TextView TV_freeTime = root.findViewById(R.id.TV_freeTime);
        TV_freeTime.setVisibility(View.GONE);

        if(isTutor){
            SB_deviceState.setVisibility(View.VISIBLE);
            TV_blockDevice.setVisibility(View.VISIBLE);
            TV_blockDevice.setTextColor(requireContext().getColor(R.color.gris));
            TV_blockDevice.setTypeface(Typeface.DEFAULT);
            TV_freeTime.setVisibility(View.VISIBLE);
            TV_freeTime.setTextColor(requireContext().getColor(R.color.gris));
            TV_freeTime.setTypeface(Typeface.DEFAULT);

            if(fill.freeuse) {
                SB_deviceState.setProgress(STATE_FREEUSE);
                TV_freeTime.setTextColor(requireContext().getColor(R.color.colorPrimary));
                TV_freeTime.setTypeface(Typeface.DEFAULT_BOLD);
                lastDeviceState = STATE_FREEUSE;
            }
            else if(fill.blocked) {
                SB_deviceState.setProgress(STATE_BLOCKDEVICE);
                TV_blockDevice.setTextColor(requireContext().getColor(R.color.colorPrimary));
                TV_blockDevice.setTypeface(Typeface.DEFAULT_BOLD);
                lastDeviceState = STATE_BLOCKDEVICE;
            }
            else {
                SB_deviceState.setProgress(STATE_NEUTRAL);
                lastDeviceState = STATE_NEUTRAL;
            }

            SB_deviceState.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(!fromUser)
                        return;

                    switch (progress) {
                        case STATE_NEUTRAL :
                            if(lastDeviceState == STATE_BLOCKDEVICE)
                                blockDeviceServer(false, STATE_NEUTRAL);
                            else if(lastDeviceState == STATE_FREEUSE)
                                freeTimeServer(false, STATE_NEUTRAL);
                            break;

                        case STATE_BLOCKDEVICE :
                            blockDeviceServer(true, STATE_BLOCKDEVICE);
                            break;

                        case STATE_FREEUSE :
                            freeTimeServer(true, STATE_FREEUSE);
                            break;
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        setSeekBarTextViews();
    }

    private void setSeekBarTextViews() {
        SeekBar SB_deviceState = root.findViewById(R.id.SB_deviceState);
        TextView TV_blockDevice = root.findViewById(R.id.TV_blockDevice);
        TextView TV_freeTime = root.findViewById(R.id.TV_freeTime);

        TV_blockDevice.setFocusable(true);
        TV_blockDevice.setClickable(true);
        TV_blockDevice.setOnClickListener(view -> {
            // Si ja estava clicat, el descliquem
            if(lastDeviceState == STATE_BLOCKDEVICE) {
                blockDeviceServer(false, STATE_NEUTRAL);
                SB_deviceState.setProgress(STATE_NEUTRAL);
                return;
            }

            if(lastDeviceState == STATE_FREEUSE)
                freeTimeServer(false, STATE_BLOCKDEVICE);

            SB_deviceState.setProgress(STATE_BLOCKDEVICE);
            blockDeviceServer(true, STATE_BLOCKDEVICE);
        });

        TV_freeTime.setFocusable(true);
        TV_freeTime.setClickable(true);
        TV_freeTime.setOnClickListener(view -> {
            // Si ja estava clicat, el descliquem
            if(lastDeviceState == STATE_FREEUSE) {
                freeTimeServer(false, STATE_NEUTRAL);
                SB_deviceState.setProgress(STATE_NEUTRAL);
                return;
            }

            if(lastDeviceState == STATE_BLOCKDEVICE)
                blockDeviceServer(false, STATE_FREEUSE);

            SB_deviceState.setProgress(STATE_FREEUSE);
            freeTimeServer(true, STATE_FREEUSE);
        });

    }

    private void getUsageFromServer() {
        String dataAvui = Funcions.date2String(Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR));
        Call<Collection<GeneralUsage>> call = mTodoService.getGenericAppUsage(idChildSelected, dataAvui, dataAvui);
        call.enqueue(new Callback<Collection<GeneralUsage>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<GeneralUsage>> call, @NonNull Response<Collection<GeneralUsage>> response) {
                super.onResponse(call, response);
                if (response.isSuccessful() && response.body() != null) {
                    List<GeneralUsage> collection = new ArrayList<>(response.body());
                    Funcions.canviarMesosDeServidor(collection);
                    makeGraph(collection);
                } else {
                    Toast.makeText(requireActivity().getApplicationContext(), getString(R.string.error_noData), Toast.LENGTH_SHORT).show();
                    TextView TV_timesUnlocked = root.findViewById(R.id.TV_timesUnlocked);
                    TV_timesUnlocked.setVisibility(View.VISIBLE);
                    Spanned text = Funcions.getSpannedText(requireContext(), getString(R.string.cops_desbloquejat, 0));
                    TV_timesUnlocked.setText(text);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<GeneralUsage>> call, @NonNull Throwable t) {
                super.onFailure(call, t);
                Toast.makeText(requireActivity().getApplicationContext(), getString(R.string.error_noData), Toast.LENGTH_SHORT).show();
                TextView TV_timesUnlocked = root.findViewById(R.id.TV_timesUnlocked);
                TV_timesUnlocked.setVisibility(View.VISIBLE);
                Spanned text = Funcions.getSpannedText(requireContext(), getString(R.string.cops_desbloquejat, 0));
                TV_timesUnlocked.setText(text);
            }
        });
    }

    private void makeGraph(List<GeneralUsage> genericAppUsage) {
        ultimaActualitzacioDades = System.currentTimeMillis();

        // Si l'ús diari està buit o és inferior a 1 minut, tot a GONE
        if (genericAppUsage.isEmpty() || genericAppUsage.get(0) == null || genericAppUsage.get(0).totalTime < 1000 * 60) {
            root.findViewById(R.id.Ch_Pie).setVisibility(View.GONE);
            root.findViewById(R.id.TV_PieApp).setVisibility(View.GONE);
            setVisibilities();

            TextView TV_timesUnlocked = root.findViewById(R.id.TV_timesUnlocked);
            TV_timesUnlocked.setVisibility(View.VISIBLE);
            Spanned text = Funcions.getSpannedText(requireContext(), getString(R.string.cops_desbloquejat, 0));
            TV_timesUnlocked.setText(text);
        } else {
            root.findViewById(R.id.Ch_Pie).setVisibility(View.VISIBLE);
            root.findViewById(R.id.TV_PieApp).setVisibility(View.VISIBLE);

            totalUsageTime = genericAppUsage.get(0).totalTime;
            timesUnlocked = genericAppUsage.get(0).timesUnlocked != null ? genericAppUsage.get(0).timesUnlocked : -1;

            if (timesUnlocked != -1) {
                TextView TV_timesUnlocked = root.findViewById(R.id.TV_timesUnlocked);
                TV_timesUnlocked.setVisibility(View.VISIBLE);
                Spanned text = Funcions.getSpannedText(requireContext(), getString(R.string.cops_desbloquejat, timesUnlocked));
                TV_timesUnlocked.setText(text);
            }

            GeneralUsage gu = genericAppUsage.stream().findFirst().orElse(null);
            if(gu.totalTime > 0) {
                for (AppUsage au : gu.usage) {
                    if (appUsageMap.containsKey(au.app.pkgName))
                        Objects.requireNonNull(appUsageMap.get(au.app.pkgName)).totalTime += au.totalTime;
                    else
                        appUsageMap.put(au.app.pkgName, au);
                }
            }
            else{
                root.findViewById(R.id.Ch_Pie).setVisibility(View.GONE);
                root.findViewById(R.id.TV_PieApp).setVisibility(View.GONE);
            }
            
            setUsageMenu();
        }

    }

    private void setUsageMenu(){
        setMascot(totalUsageTime);
        setPieChart(appUsageMap, totalUsageTime);
    }

    private void setMascot(long totalUsageTime) {
        ImageView IV_mascot = root.findViewById(R.id.IV_mascot);
        IV_mascot.setVisibility(View.INVISIBLE);

        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 21) {
            if (totalUsageTime < TimeUnit.HOURS.toMillis(1))
                IV_mascot.setImageResource(R.drawable.mascot_min);
            else if (totalUsageTime < TimeUnit.MINUTES.toMillis(90))
                IV_mascot.setImageResource(R.drawable.mascot_hora);
            else if (totalUsageTime < TimeUnit.MINUTES.toMillis(135))
                IV_mascot.setImageResource(R.drawable.mascot_molt);
            else
                IV_mascot.setImageResource(R.drawable.mascot_max);
        } else {
            IV_mascot.setImageResource(R.drawable.mascot_nit);
        }

        IV_mascot.setVisibility(View.VISIBLE);
    }

    private void setPieChart(Map<String, AppUsage> mapUsage, long totalUsageTime) {
        root.findViewById(R.id.Ch_Pie).setVisibility(View.VISIBLE);
        root.findViewById(R.id.TV_PieApp).setVisibility(View.VISIBLE);
        pieChart = root.findViewById(R.id.Ch_Pie);
        ArrayList<PieEntry> yValues = new ArrayList<>();
        long others = 0;
        for (Map.Entry<String, AppUsage> entry : mapUsage.entrySet()) {
            // Si hi ha poques entrades no crear "Altres"
            if(mapUsage.size() < 5)
                yValues.add(new PieEntry(entry.getValue().totalTime, entry.getValue().app.appName));
            else{
                if (entry.getValue().totalTime >= totalUsageTime * 0.05)
                    yValues.add(new PieEntry(entry.getValue().totalTime, entry.getValue().app.appName));
                else {
                    others += entry.getValue().totalTime;
                }
            }
        }

        Pair<Integer, Integer> totalTime = Funcions.millisToString(totalUsageTime);

        if(!(mapUsage.size() < 5))
            yValues.add(new PieEntry(others, getString(R.string.other)));

        PieDataSet pieDataSet = new PieDataSet(yValues, "Ús d'apps");
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setColors(Constants.GRAPH_COLORS);

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(12);
        pieData.setValueTypeface(Typeface.DEFAULT_BOLD);

        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterTextSize(25);

        if (totalTime.first == 0)
            pieChart.setCenterText(getResources().getString(R.string.mins, totalTime.second));
        else
            pieChart.setCenterText(getResources().getString(R.string.hours_endl_minutes, totalTime.first, totalTime.second));

        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(61f);

        pieChart.animateY(1000);

        pieChart.setDrawEntryLabels(false);
        pieChart.getLegend().setEnabled(false);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                final PieEntry pe = (PieEntry) e;

                TextView TV_pieApp = root.findViewById(R.id.TV_PieApp);
                TV_pieApp.setText(pe.getLabel());
                TV_pieApp.setTextSize(20);
                TV_pieApp.setTypeface(Typeface.DEFAULT_BOLD);

                Pair<Integer, Integer> appTime = Funcions.millisToString(e.getY());

                if (appTime.first == 0)
                    pieChart.setCenterText(getResources().getString(R.string.mins, appTime.second));
                else
                    pieChart.setCenterText(getResources().getString(R.string.hours_endl_minutes, appTime.first, appTime.second));
            }

            @Override
            public void onNothingSelected() {
                TextView TV_pieApp = root.findViewById(R.id.TV_PieApp);
                TV_pieApp.setTextSize(14);
                TV_pieApp.setTypeface(Typeface.DEFAULT);

                TV_pieApp.setText(getResources().getString(R.string.press_pie_chart));
                if (totalTime.first == 0)
                    pieChart.setCenterText(getResources().getString(R.string.mins, totalTime.second));
                else
                    pieChart.setCenterText(getResources().getString(R.string.hours_endl_minutes, totalTime.first, totalTime.second));
            }
        });
        pieChart.invalidate();

        setVisibilities();
    }

    private void setVisibilities() {
        ConstraintLayout CL_info = root.findViewById(R.id.CL_resumUs);
        CL_info.setVisibility(View.VISIBLE);
        ConstraintLayout CL_infoButtons = root.findViewById(R.id.CL_ResumUsButons);
        CL_infoButtons.setVisibility(View.VISIBLE);
        ConstraintLayout CL_limit = root.findViewById(R.id.CL_LimitsApps);
        CL_limit.setVisibility(View.VISIBLE);
        ConstraintLayout CL_limitButtons = root.findViewById(R.id.CL_LimitsAppsButtons);
        CL_limitButtons.setVisibility(View.VISIBLE);
        ConstraintLayout CL_Geoloc = root.findViewById(R.id.CL_geoloc);
        if(isTutor)
            CL_Geoloc.setVisibility(View.VISIBLE);
    }

    @Override
    protected void finalize() throws Throwable {
        if (isTutor)
            Funcions.askChildForLiveApp(requireContext(), idChildSelected, false);
        super.finalize();
    }
}
