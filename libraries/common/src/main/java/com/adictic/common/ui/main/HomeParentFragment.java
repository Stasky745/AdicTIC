package com.adictic.common.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.adictic.common.R;
import com.adictic.common.entity.FillNom;
import com.adictic.common.rest.Api;
import com.adictic.common.util.App;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;
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
    private MainActivityAbstractClass parentActivity;
    private View root;
    private TabFillsAdapter adapter;

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
        parentActivity = (MainActivityAbstractClass) requireActivity();

        if(parentActivity.getHomeParent_childs()!=null && !parentActivity.getHomeParent_childs().isEmpty()) setupTabLayout(parentActivity.getHomeParent_childs());
        if(parentActivity.getHomeParent_childs()==null || (parentActivity.getHomeParent_lastChildsUpdate()+ parentActivity.tempsPerActu)<Calendar.getInstance().getTimeInMillis()) {
            Api mTodoService = ((App) requireActivity().getApplicationContext()).getAPI();

            long idTutor;
            Call<Collection<FillNom>> call;

            //Si es tutor
            if (sharedPreferences.getBoolean(Constants.SHARED_PREFS_ISTUTOR, false)) {
                idTutor = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
                call = mTodoService.getUserChilds(idTutor);
            }
            else {
                idTutor = sharedPreferences.getLong(Constants.SHARED_PREFS_IDTUTOR, -1);
                long idChild = sharedPreferences.getLong(Constants.SHARED_PREFS_IDUSER, -1);
                call = mTodoService.getChildInfo(idTutor,idChild);
            }

            call.enqueue(new Callback<Collection<FillNom>>() {
                @Override
                public void onResponse(@NonNull Call<Collection<FillNom>> call, @NonNull Response<Collection<FillNom>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().size() > 0) {
                        parentActivity.setHomeParent_childs(new ArrayList<>(response.body()));
                        parentActivity.setHomeParent_lastChildsUpdate(Calendar.getInstance().getTimeInMillis());
                        setupTabLayout(parentActivity.getHomeParent_childs());
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
        if(adapter == null) {
            ViewPager2 viewPager = root.findViewById(R.id.ViewPager);
            TabLayout tabLayout = root.findViewById(R.id.TabLayout);
            adapter = new TabFillsAdapter(HomeParentFragment.this, getContext(), fills);
            viewPager.setAdapter(adapter);
            new TabLayoutMediator(tabLayout, viewPager,
                    (tab, position) -> tab.setText(adapter.getPageTitle(position))
            ).attach();
        }
        else{
            adapter.updateFills(fills);
        }

    }
}
