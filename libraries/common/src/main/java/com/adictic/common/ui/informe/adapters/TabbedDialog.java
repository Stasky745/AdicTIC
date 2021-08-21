package com.adictic.common.ui.informe.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.adictic.common.R;
import com.adictic.common.entity.CanvisHoraris;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

public class TabbedDialog extends DialogFragment {
    private final CanvisHoraris canvisHoraris;

    public TabbedDialog(CanvisHoraris canvisHoraris1) {
        canvisHoraris = canvisHoraris1;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.informe_dialog_fragment, container, false);
        TabLayout tabLayout = rootView.findViewById(R.id.TL_dialog_tab);
        ViewPager2 viewPager2 = rootView.findViewById(R.id.VP2_dialog_viewpager);
        HorarisNitTabsAdapter adapter = new HorarisNitTabsAdapter(getChildFragmentManager(), getLifecycle());
        adapter.setCanvisHoraris(canvisHoraris);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            if(canvisHoraris.horariAntic == null)
                tab.setText("Horario creado");
            else if(canvisHoraris.horariNou == null)
                tab.setText("Horario borrado");
            else{
                if(position == 0)
                    tab.setText("Antes");
                else
                    tab.setText("Después");
            }
        }).attach();

        return rootView;

    }

    @Override
    public void onResume() {
        super.onResume();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        Objects.requireNonNull(getDialog()).getWindow().setLayout(width, height);

//        WindowManager.LayoutParams params = Objects.requireNonNull(getDialog()).getWindow().getAttributes();
//        params.width = width;
//        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//        getDialog().getWindow().setAttributes(params);
    }
}
