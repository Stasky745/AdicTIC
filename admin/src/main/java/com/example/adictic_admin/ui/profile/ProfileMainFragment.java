package com.example.adictic_admin.ui.profile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.adictic_admin.App;
import com.example.adictic_admin.MainActivity;
import com.example.adictic_admin.R;
import com.example.adictic_admin.entity.AdminProfile;
import com.example.adictic_admin.rest.Api;
import com.example.adictic_admin.util.Constants;
import com.example.adictic_admin.util.Funcions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileMainFragment extends Fragment {
    private Api mService;
    private SharedPreferences sharedPreferences;
    private MainActivity parentActivity = null;
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getContext());
        parentActivity = (MainActivity) getActivity();
        root = inflater.inflate(R.layout.profile_main_activity, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);
        mService = ((App) requireActivity().getApplication()).getAPI();
        root = view;
        getInfoFromServer();
    }

    private void getInfoFromServer() {
        if(parentActivity!=null && parentActivity.yourAdminProfile!=null){
            showProfileInfo(parentActivity.yourAdminProfile);
            return;
        }
        Call<AdminProfile> call = mService.getProfile(sharedPreferences.getLong(Constants.SHARED_PREFS_ID_ADMIN,-1));
        call.enqueue(new Callback<AdminProfile>() {
            @Override
            public void onResponse(Call<AdminProfile> call, Response<AdminProfile> response) {
                if(response.isSuccessful() && response.body() != null){
                    parentActivity.yourAdminProfile = response.body();
                    showProfileInfo(response.body());
                }
            }

            @Override
            public void onFailure(Call<AdminProfile> call, Throwable t) {

            }
        });
    }

    private void showProfileInfo(AdminProfile profile){
        ViewPager2 viewPager = (ViewPager2) root.findViewById(R.id.ViewPager);
        TabLayout tabLayout = (TabLayout) root.findViewById(R.id.TabLayout);

        TabProfileAdapter adapter = new TabProfileAdapter(getContext(),ProfileMainFragment.this, profile);

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout,viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();
    }
}
