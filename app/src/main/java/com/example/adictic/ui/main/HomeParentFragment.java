package com.example.adictic.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.adictic.R;
import com.example.adictic.entity.FillNom;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeParentFragment extends Fragment {
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.home_parent_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getActivity());

        ViewPager2 viewPager = view.findViewById(R.id.ViewPager);
        TabLayout tabLayout = view.findViewById(R.id.TabLayout);

        TodoApi mTodoService = ((TodoApp) requireActivity().getApplicationContext()).getAPI();

        long idTutor;
        if(sharedPreferences.getBoolean("isTutor",false)) idTutor = sharedPreferences.getLong("idUser",-1);
        else idTutor = sharedPreferences.getLong("idTutor",-1);
        Call<Collection<FillNom>> call = mTodoService.getUserChilds(idTutor);

        call.enqueue(new Callback<Collection<FillNom>>() {
            @Override
            public void onResponse(@NonNull Call<Collection<FillNom>> call, @NonNull Response<Collection<FillNom>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().size() > 0) {
                    ArrayList<FillNom> fills = new ArrayList<>(response.body());

                    // Si és l'app fill només ensenyem el fill actual
                    if (!sharedPreferences.getBoolean("isTutor",false)) {
                        boolean trobat = false;
                        int i = 0;
                        while (!trobat && i < fills.size()) {
                            if (fills.get(i).idChild == sharedPreferences.getLong("idUser",-1)) {
                                trobat = true;
                                FillNom fill = fills.get(i);
                                fills.clear();
                                fills.add(fill);
                            }
                            i++;
                        }
                    }

                    TabFillsAdapter adapter = new TabFillsAdapter(HomeParentFragment.this, getContext(), fills);

                    viewPager.setAdapter(adapter);

                    new TabLayoutMediator(tabLayout, viewPager,
                            (tab, position) -> tab.setText(adapter.getPageTitle(position))
                    ).attach();
                } else {
                    TextView error = view.findViewById(R.id.TV_noFills);
                    error.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Collection<FillNom>> call, @NonNull Throwable t) {
                TextView error = view.findViewById(R.id.TV_noFills);
                error.setVisibility(View.VISIBLE);
            }
        });
    }
}
