package com.adictic.common.ui.informe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.CanvisEvents;
import com.adictic.common.entity.CanvisHoraris;
import com.adictic.common.entity.GeneralUsage;
import com.adictic.common.rest.Api;
import com.adictic.common.ui.informe.adapters.EventsAdapter;
import com.adictic.common.ui.informe.adapters.HorarisNitAdapter;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InformeDetallatFragment extends Fragment {
    private final List<GeneralUsage> appList;
    private final int age;
    private final long mitjanaMillisDia;
    private Api api;
    private final long idChild;
    private final String activeMonth;

    InformeDetallatFragment(Collection<GeneralUsage> col, long totalUsageT, int edat, long id, int currentYear, int currentMonth){
        appList = new ArrayList<>(col);
        age = edat;
        mitjanaMillisDia = totalUsageT / appList.size();
        idChild = id;

        activeMonth = currentMonth +"-"+ currentYear;

    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.informe_resum, container, false);

        api = ((App) requireContext()).getAPI();

        setIntro(root);
        setLimitsMarcats(root);

        return root;
    }

    private void setLimitsMarcats(View root) {
        TextView TV_informeBlockDevice = root.findViewById(R.id.TV_informeBlockDevice);
        TextView TV_informeFreeUse = root.findViewById(R.id.TV_informeFreeUse);

        setBlockedApps(root);
        setEvents(root);
        setHorarisNit(root);
    }

    private void setHorarisNit(View root) {
        RecyclerView RV_informeHoraris = root.findViewById(R.id.RV_informeHorarisNit);

        Call<Collection<CanvisHoraris>> call = api.getCanvisHoraris(idChild, activeMonth);
        call.enqueue(new Callback<Collection<CanvisHoraris>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<CanvisHoraris>> call, @NonNull Response<Collection<CanvisHoraris>> response) {
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
                        RV_informeHoraris.setAdapter(eventsAdapter);
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
                TextView TV_informeCanvisHorarisNit = root.findViewById(R.id.TV_informeCanvisHorarisNit);
                String text = TV_informeCanvisHorarisNit.getText() + ": 0";
                TV_informeCanvisHorarisNit.setText(text);
                RV_informeHoraris.setVisibility(View.GONE);
            }
        });
    }

    private void setEvents(View root) {
        RecyclerView RV_informeEvents = root.findViewById(R.id.RV_informeEvents);

        Call<Collection<CanvisEvents>> call = api.getCanvisEvents(idChild, activeMonth);
        call.enqueue(new Callback<Collection<CanvisEvents>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<CanvisEvents>> call, @NonNull Response<Collection<CanvisEvents>> response) {
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
                        RV_informeEvents.setAdapter(eventsAdapter);
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
                TextView TV_informeCanvisEvents = root.findViewById(R.id.TV_informeCanvisEvents);
                String text = TV_informeCanvisEvents.getText() + ": 0";
                TV_informeCanvisEvents.setText(text);
                RV_informeEvents.setVisibility(View.GONE);
            }
        });
    }

    private void setBlockedApps(View root) {
        TextView TV_informeCanvisBlockApps = root.findViewById(R.id.TV_informeCanvisBlock);
    }

    private void setIntro(View root) {
        TextView TV_informeIntro = root.findViewById(R.id.TV_informeIntro);
        String intro;
        long millisRecomanats = Constants.AGE_TIMES_MILLIS[age];

        if(mitjanaMillisDia <= millisRecomanats)
            intro = getString(R.string.intro_bona, age, Funcions.millis2horaString(millisRecomanats), Funcions.millis2horaString(mitjanaMillisDia));
        else
            intro = getString(R.string.intro_dolenta, age, Funcions.millis2horaString(millisRecomanats), Funcions.millis2horaString(mitjanaMillisDia));

        TV_informeIntro.setText(intro);


    }
}
