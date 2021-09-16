package com.adictic.common.ui.informe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.entity.BlockInfo;
import com.adictic.common.entity.CanvisAppBlock;
import com.adictic.common.entity.CanvisEvents;
import com.adictic.common.entity.CanvisHoraris;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.entity.TimeBlock;
import com.adictic.common.entity.TimeFreeUse;
import com.adictic.common.rest.Api;
import com.adictic.common.ui.informe.adapters.AppsAdapter;
import com.adictic.common.ui.informe.adapters.EventsAdapter;
import com.adictic.common.ui.informe.adapters.HorarisNitAdapter;
import com.adictic.common.ui.informe.adapters.TopAppsAdapter;
import com.adictic.common.util.App;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;
import com.adictic.common.util.RVSpaceDecoration;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class InformeDetallatFragment extends Fragment {
    private final int RV_APPS_EVENTS_HEIGHT = 1300;
    private final int RV_HORARIS_HEIGHT = 1000;

    private static final String ARG_TOTALUSAGETIME = "totalUsageTime";
    private static final String ARG_EDAT = "edat";
    private static final String ARG_ID = "id";
    private static final String ARG_APPLIST = "appList";

    private List<GeneralUsage> appList;
    private int age;
    private long mitjanaMillisDia;
    private Api api;
    private long idChild;
    private String activeDateString;
    private int activeMonth;
    private int activeYear;

    public static InformeDetallatFragment newInstance(Collection<GeneralUsage> col, long totalUsageT, int edat, long id){
        final InformeDetallatFragment informeDetallatFragment = new InformeDetallatFragment();

        final Bundle args = new Bundle(4);
        args.putLong(ARG_TOTALUSAGETIME, totalUsageT);
        args.putLong(ARG_ID, id);
        args.putInt(ARG_EDAT, edat);
        args.putParcelableArrayList(ARG_APPLIST, new ArrayList<>(col));

        informeDetallatFragment.setArguments(args);
        return informeDetallatFragment;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.informe_detallat, container, false);

        getBundle();

        api = ((App) requireContext().getApplicationContext()).getAPI();

        setIntro(root);
        setLimitsMarcats(root);

        return root;
    }

    private void getBundle() {
        final Bundle arguments = getArguments();

        if(arguments == null)
            return;

        appList = arguments.getParcelableArrayList(ARG_APPLIST);
        age = arguments.getInt(ARG_EDAT);
        mitjanaMillisDia = arguments.getLong(ARG_TOTALUSAGETIME) / appList.size();
        idChild = arguments.getLong(ARG_ID);

        GeneralUsage generalUsage = appList.get(0);
        activeMonth = generalUsage.month;
        activeYear = generalUsage.year;
        activeDateString = (activeMonth+1) +"-"+ activeYear;
    }

    private void setLimitsMarcats(View root) {
        setAccessInfo(root);
        setBlockedApps(root);
        setEvents(root);
        setHorarisNit(root);
    }

    private void setAccessInfo(View root) {
        Call<BlockInfo> call = api.getAccessInfo(idChild, activeDateString);
        call.enqueue(new Callback<BlockInfo>() {
            @Override
            public void onResponse(@NonNull Call<BlockInfo> call, @NonNull Response<BlockInfo> response) {
                    super.onResponse(call, response);
                if(response.isSuccessful()){
                    BlockInfo blockInfo = response.body();
                    if(blockInfo == null)
                        blockInfo = new BlockInfo();

                    setAccessTexts(root, blockInfo);
                }
                else
                    Toast.makeText(getContext(), response.code() + ":" + response.message(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(@NonNull Call<BlockInfo> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAccessTexts(View root, BlockInfo blockInfo) {
        TextView TV_informeBlockDevice = root.findViewById(R.id.TV_informeBlockDevice);
        TextView TV_informeFreeUse = root.findViewById(R.id.TV_informeFreeUse);
        TextView TV_intentsAccessDevice = root.findViewById(R.id.TV_intentsAccessDevice);
        TextView TV_intentsAccessApps = root.findViewById(R.id.TV_intentsAccessApps);

        long tempsBloqueig = 0;
        for(TimeBlock timeBlock : blockInfo.tempsBloqueig){
            tempsBloqueig += (timeBlock.end - timeBlock.start);
        }
        TV_informeBlockDevice.setText(getString(R.string.info_disp_bloq, blockInfo.tempsBloqueig.size(), Funcions.millis2horaString(getContext(), tempsBloqueig)));

        long tempsFreeUse = 0;
        for(TimeFreeUse timeFreeUse : blockInfo.tempsFreeUse){
            tempsFreeUse += timeFreeUse.end - timeFreeUse.start;
        }
        TV_informeFreeUse.setText(getString(R.string.info_freeuse, blockInfo.tempsFreeUse.size(), Funcions.millis2horaString(getContext(), tempsFreeUse)));

        TV_intentsAccessDevice.setText(getString(R.string.access_block_device, blockInfo.intentsAccesDisps.size()));
        TV_intentsAccessApps.setText(getString(R.string.access_block_app, blockInfo.intentsAccesApps.size()));

        double mitjanaIntents;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int daysInMonth = YearMonth.of(activeYear, activeMonth).lengthOfMonth();
            mitjanaIntents = (blockInfo.intentsAccesApps.size() + blockInfo.intentsAccesDisps.size()) / (double) daysInMonth;
        }
        else{
            Calendar calendar = new GregorianCalendar(activeYear, activeMonth, 1);
            int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            mitjanaIntents = (blockInfo.intentsAccesApps.size() + blockInfo.intentsAccesDisps.size()) / (double) daysInMonth;
        }

        TextView TV_informeIntentsAcces = root.findViewById(R.id.TV_informeIntentsAcces);

        if(mitjanaIntents > 9)
            TV_informeIntentsAcces.setText(getString(R.string.access_mitjana_dolenta, (int) mitjanaIntents)); //Text dolent
        else
            TV_informeIntentsAcces.setText(getString(R.string.access_mitjana_bona, (int) mitjanaIntents)); //text bo
    }

    private void setHorarisNit(View root) {
        RecyclerView RV_informeHoraris = root.findViewById(R.id.RV_informeHorarisNit);
        RV_informeHoraris.setLayoutManager(new LinearLayoutManager(getContext()));

        Call<Collection<CanvisHoraris>> call = api.getCanvisHoraris(idChild, activeDateString);
        call.enqueue(new Callback<Collection<CanvisHoraris>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<CanvisHoraris>> call, @NonNull Response<Collection<CanvisHoraris>> response) {
                    super.onResponse(call, response);
                if(response.isSuccessful() && response.body() != null){
                    if(response.body().isEmpty()){
                        TextView TV_informeCanvisHorarisNit = root.findViewById(R.id.TV_informeCanvisHorarisNit);
                        String text = TV_informeCanvisHorarisNit.getText() + ": 0";
                        TV_informeCanvisHorarisNit.setText(text);
                        RV_informeHoraris.setVisibility(View.GONE);
                    }
                    else {
                        List<CanvisHoraris> horarisList = new ArrayList<>(response.body());
                        HorarisNitAdapter eventsAdapter = new HorarisNitAdapter(horarisList, getContext());
                        RVSpaceDecoration rvSpaceDecoration = new RVSpaceDecoration(8);
                        RV_informeHoraris.addItemDecoration(rvSpaceDecoration);
                        RV_informeHoraris.setAdapter(eventsAdapter);

                        if(horarisList.size() > 3) {
                            ViewGroup.LayoutParams params = RV_informeHoraris.getLayoutParams();
                            params.height = RV_HORARIS_HEIGHT;
                            RV_informeHoraris.setLayoutParams(params);
                        }
                    }
                }
                else{
                    TextView TV_informeCanvisHorarisNit = root.findViewById(R.id.TV_informeCanvisHorarisNit);
                    String text = TV_informeCanvisHorarisNit.getText() + ": 0";
                    TV_informeCanvisHorarisNit.setText(text);
                    RV_informeHoraris.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<CanvisHoraris>> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                TextView TV_informeCanvisHorarisNit = root.findViewById(R.id.TV_informeCanvisHorarisNit);
                String text = TV_informeCanvisHorarisNit.getText() + ": 0";
                TV_informeCanvisHorarisNit.setText(text);
                RV_informeHoraris.setVisibility(View.GONE);
            }
        });
    }

    private void setEvents(View root) {
        RecyclerView RV_informeEvents = root.findViewById(R.id.RV_informeEvents);
        RV_informeEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        Call<Collection<CanvisEvents>> call = api.getCanvisEvents(idChild, activeDateString);
        call.enqueue(new Callback<Collection<CanvisEvents>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<CanvisEvents>> call, @NonNull Response<Collection<CanvisEvents>> response) {
                    super.onResponse(call, response);
                if(response.isSuccessful() && response.body() != null){
                    if(response.body().isEmpty()){
                        TextView TV_informeCanvisEvents = root.findViewById(R.id.TV_informeCanvisEvents);
                        String text = TV_informeCanvisEvents.getText() + ": 0";
                        TV_informeCanvisEvents.setText(text);
                        RV_informeEvents.setVisibility(View.GONE);
                    }
                    else{
                        List<CanvisEvents> eventsList = new ArrayList<>(response.body());
                        EventsAdapter eventsAdapter = new EventsAdapter(eventsList, getContext());
                        RVSpaceDecoration rvSpaceDecoration = new RVSpaceDecoration(8);
                        RV_informeEvents.addItemDecoration(rvSpaceDecoration);
                        RV_informeEvents.setAdapter(eventsAdapter);

                        if(eventsList.size() > 3) {
                            ViewGroup.LayoutParams params = RV_informeEvents.getLayoutParams();
                            params.height = RV_APPS_EVENTS_HEIGHT;
                            RV_informeEvents.setLayoutParams(params);
                        }
                    }
                }
                else {
                    TextView TV_informeCanvisEvents = root.findViewById(R.id.TV_informeCanvisEvents);
                    String text = TV_informeCanvisEvents.getText() + ": 0";
                    TV_informeCanvisEvents.setText(text);
                    RV_informeEvents.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<CanvisEvents>> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                TextView TV_informeCanvisEvents = root.findViewById(R.id.TV_informeCanvisEvents);
                String text = TV_informeCanvisEvents.getText() + ": 0";
                TV_informeCanvisEvents.setText(text);
                RV_informeEvents.setVisibility(View.GONE);
            }
        });
    }

    private void setBlockedApps(View root) {
        RecyclerView RV_informeApps = root.findViewById(R.id.RV_informeBlockApps);
        RV_informeApps.setLayoutManager(new LinearLayoutManager(getContext()));

        Call<Collection<CanvisAppBlock>> call = api.getCanvisApps(idChild, activeDateString);
        call.enqueue(new Callback<Collection<CanvisAppBlock>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<CanvisAppBlock>> call, @NonNull Response<Collection<CanvisAppBlock>> response) {
                    super.onResponse(call, response);
                if(response.isSuccessful() && response.body() != null){
                    if(response.body().isEmpty()){
                        TextView TV_informeCanvisApps = root.findViewById(R.id.TV_informeCanvisBlock);
                        String text = TV_informeCanvisApps.getText() + ": 0";
                        TV_informeCanvisApps.setText(text);
                        RV_informeApps.setVisibility(View.GONE);
                    }
                    else{
                        List<CanvisAppBlock> appBlockList = new ArrayList<>(response.body());
                        AppsAdapter appsAdapter = new AppsAdapter(appBlockList, getContext());
                        RVSpaceDecoration rvSpaceDecoration = new RVSpaceDecoration(8);
                        RV_informeApps.addItemDecoration(rvSpaceDecoration);
                        RV_informeApps.setAdapter(appsAdapter);

                        if(appBlockList.size() > 3) {
                            ViewGroup.LayoutParams params = RV_informeApps.getLayoutParams();
                            params.height = RV_APPS_EVENTS_HEIGHT;
                            RV_informeApps.setLayoutParams(params);
                        }
                    }
                }
                else {
                    TextView TV_informeCanvisApps = root.findViewById(R.id.TV_informeCanvisBlock);
                    String text = TV_informeCanvisApps.getText() + ": 0";
                    TV_informeCanvisApps.setText(text);
                    RV_informeApps.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<CanvisAppBlock>> call, @NonNull Throwable t) {
                    super.onFailure(call, t);
                TextView TV_informeCanvisApps = root.findViewById(R.id.TV_informeCanvisBlock);
                String text = TV_informeCanvisApps.getText() + ": 0";
                TV_informeCanvisApps.setText(text);
                RV_informeApps.setVisibility(View.GONE);
            }
        });
    }

    private void setIntro(View root) {
        TextView TV_informeIntro = root.findViewById(R.id.TV_informeIntro);
        String intro;
        long millisRecomanats = Constants.AGE_TIMES_MILLIS[age];

        if(mitjanaMillisDia <= millisRecomanats)
            intro = getString(R.string.intro_bona, age, Funcions.millis2horaString(getContext(), millisRecomanats), Funcions.millis2horaString(getContext(), mitjanaMillisDia));
        else
            intro = getString(R.string.intro_dolenta, age, Funcions.millis2horaString(getContext(), millisRecomanats), Funcions.millis2horaString(getContext(), mitjanaMillisDia));

        TV_informeIntro.setText(intro);

        setMostUsedApps(root);
    }

    private void setMostUsedApps(View root) {
        RecyclerView RV_mostUsedApps = root.findViewById(R.id.RV_informeTopApps);
        RV_mostUsedApps.setLayoutManager(new LinearLayoutManager(getContext()));

        Map<String,AppUsage> appUsages = new HashMap<>();

        for(GeneralUsage generalUsage : appList){
            for(AppUsage appUsage : generalUsage.usage){
                AppUsage mapAppUsage = appUsages.get(appUsage.app.pkgName);
                if(mapAppUsage != null){
                    mapAppUsage.totalTime += appUsage.totalTime;
                    appUsages.put(appUsage.app.pkgName, mapAppUsage);
                }
                else
                    appUsages.put(appUsage.app.pkgName, appUsage);
            }
        }

        List<AppUsage> RV_list = new ArrayList<>(appUsages.values());
        RV_list.sort((o1, o2) -> o2.totalTime.compareTo(o1.totalTime));
        RV_list = RV_list.stream()
                .limit(5)
                .collect(Collectors.toList());

        TopAppsAdapter topAppsAdapter = new TopAppsAdapter(getContext(), RV_list);
        RV_mostUsedApps.setAdapter(topAppsAdapter);
    }
}
