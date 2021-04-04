package com.example.adictic.fragment;

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
import com.example.adictic.TodoApp;
import com.example.adictic.adapters.TabFillsAdapter;
import com.example.adictic.entity.FillNom;
import com.example.adictic.rest.TodoApi;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeParentFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.home_parent_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = (ViewPager2) view.findViewById(R.id.ViewPager);
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.TabLayout);

        TodoApi mTodoService = ((TodoApp)getActivity().getApplicationContext()).getAPI();

        long idTutor = TodoApp.getIDTutor();
        Call<Collection<FillNom>> call = mTodoService.getUserChilds(idTutor);

        call.enqueue(new Callback<Collection<FillNom>>() {
            @Override
            public void onResponse(Call<Collection<FillNom>> call, Response<Collection<FillNom>> response) {
                if(response.isSuccessful() && response.body() != null && response.body().size() > 0){
                    ArrayList<FillNom> fills = new ArrayList<>(response.body());

                    // Si és l'app fill només ensenyem el fill actual
                    if(TodoApp.getTutor() == 0 && TodoApp.getIDChild() > 0){
                        boolean trobat = false;
                        int i = 0;
                        while(!trobat && i < fills.size()) {
                            if(fills.get(i).idChild == TodoApp.getIDChild()){
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

                    new TabLayoutMediator(tabLayout,viewPager,
                            (tab, position) -> tab.setText(adapter.getPageTitle(position))
                    ).attach();
                }
                else{
                    TextView error = (TextView) view.findViewById(R.id.TV_noFills);
                    error.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Collection<FillNom>> call, Throwable t) {
                TextView error = (TextView) view.findViewById(R.id.TV_noFills);
                error.setVisibility(View.VISIBLE);
            }
        });
    }
}
