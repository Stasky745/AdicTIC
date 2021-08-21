package com.adictic.common.ui.informe.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.adictic.common.entity.CanvisHoraris;
import com.adictic.common.ui.informe.DialogHorarisNitFragment;

public class HorarisNitTabsAdapter extends FragmentStateAdapter {
    private CanvisHoraris canvisHoraris;
    public void setCanvisHoraris(CanvisHoraris canvisHoraris1){
        canvisHoraris = canvisHoraris1;
    }

    public HorarisNitTabsAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(canvisHoraris.horariNou == null)
            return new DialogHorarisNitFragment(canvisHoraris.horariAntic);
        else if(canvisHoraris.horariAntic == null)
            return new DialogHorarisNitFragment(canvisHoraris.horariNou);
        else{
            if(position == 0)
                return new DialogHorarisNitFragment(canvisHoraris.horariAntic);
            else
                return new DialogHorarisNitFragment(canvisHoraris.horariNou);
        }
    }

    @Override
    public int getItemCount() {
        if(canvisHoraris.horariNou == null || canvisHoraris.horariAntic == null)
            return 1;
        return 2;
    }
}
