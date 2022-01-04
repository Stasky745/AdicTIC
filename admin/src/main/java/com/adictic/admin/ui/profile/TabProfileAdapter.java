package com.adictic.admin.ui.profile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.adictic.admin.R;
import com.adictic.common.entity.AdminProfile;

public class TabProfileAdapter extends FragmentStateAdapter {
    private final Context mCtx;
    private final AdminProfile adminProfile;

    public TabProfileAdapter(Context ctx, Fragment fa, AdminProfile profile) {
        super(fa);
        mCtx = ctx;
        adminProfile = profile;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return ProfileFragment.newInstance(adminProfile);
            case 1:
                return OfficeFragment.newInstance(adminProfile.oficina);
            default:
                throw new IllegalStateException("Unexpected position TabProfileAdapter (getItem): " + position);
        }
    }

    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return mCtx.getString(R.string.profile);
            case 1:
                return mCtx.getString(R.string.oficina);
            default:
                throw new IllegalStateException("Unexpected position TabProfileAdapter (getPageTitle): " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }


}
