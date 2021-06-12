package com.example.adictic.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.adictic.R;
import com.example.adictic.entity.FillNom;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Constants;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeParentFragment extends Fragment {
    private SharedPreferences sharedPreferences;
    private NavActivity parentActivity;
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.home_parent_fragment, container, false);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences = Funcions.getEncryptedSharedPreferences(getActivity());
        parentActivity = (NavActivity) requireActivity();

        if(parentActivity.homeParent_childs!=null && !parentActivity.homeParent_childs.isEmpty()) setupTabLayout(parentActivity.homeParent_childs);
        if(parentActivity.homeParent_childs==null || (parentActivity.homeParent_lastChildsUpdate+ parentActivity.tempsPerActu)<Calendar.getInstance().getTimeInMillis()) {
            TodoApi mTodoService = ((TodoApp) requireActivity().getApplicationContext()).getAPI();

            long idTutor;
            if (sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false))
                idTutor = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
            else idTutor = sharedPreferences.getLong(Constants.SHARED_PREFS_IDTUTOR, -1);
            Call<Collection<FillNom>> call = mTodoService.getUserChilds(idTutor);

            call.enqueue(new Callback<Collection<FillNom>>() {
                @Override
                public void onResponse(@NonNull Call<Collection<FillNom>> call, @NonNull Response<Collection<FillNom>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().size() > 0) {
                        parentActivity.homeParent_childs = new ArrayList<>(response.body());
                        parentActivity.homeParent_lastChildsUpdate = Calendar.getInstance().getTimeInMillis();
                        setupTabLayout(parentActivity.homeParent_childs);
                        TextView error = root.findViewById(R.id.TV_noFills);
                        error.setVisibility(View.GONE);
                    } else {
                        TextView error = root.findViewById(R.id.TV_noFills);
                        error.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Collection<FillNom>> call, @NonNull Throwable t) {
                    TextView error = root.findViewById(R.id.TV_noFills);
                    error.setVisibility(View.VISIBLE);
                }
            });
        }
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        //super.onViewCreated(view, savedInstanceState);
//
//    }

    private void setupTabLayout(ArrayList<FillNom> fills){
        ViewPager2 viewPager = root.findViewById(R.id.ViewPager);
        TabLayout tabLayout = root.findViewById(R.id.TabLayout);
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
    }
}
