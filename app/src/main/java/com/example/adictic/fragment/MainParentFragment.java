package com.example.adictic.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.activity.BlockAppsActivity;
import com.example.adictic.activity.DayUsageActivity;
import com.example.adictic.activity.HorarisActivity;
import com.example.adictic.activity.HorarisMainActivity;
import com.example.adictic.activity.informe.InformeActivity;
import com.example.adictic.entity.FillNom;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainParentFragment extends Fragment {

    private TodoApi mTodoService;
    private long idChildSelected = -1;
    private FillNom fillNom;
    private View root;

    private ImageView IV_liveIcon;

    public MainParentFragment(FillNom fill){
        idChildSelected = fill.idChild;
        fillNom = fill;

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.main_parent, container, false);
        mTodoService = ((TodoApp) getActivity().getApplication()).getAPI();

        IV_liveIcon = (ImageView) root.findViewById(R.id.IV_CurrentApp);

        ImageView im_informe = (ImageView) root.findViewById(R.id.IV_Informe);
        TextView tv_informe = (TextView) root.findViewById(R.id.TV_Informe);
        ImageView im_appUsage = (ImageView) root.findViewById(R.id.IV_AppUsage);
        TextView tv_appUsage = (TextView) root.findViewById(R.id.TV_AppUsage);
        ImageView im_blockApps = (ImageView) root.findViewById(R.id.IV_BlockApps);
        TextView tv_blockApps = (TextView) root.findViewById(R.id.TV_BlockApps);
        ImageView im_horaris = (ImageView) root.findViewById(R.id.IV_Horaris);
        TextView tv_horaris = (TextView) root.findViewById(R.id.TV_Horaris);

        View.OnClickListener blockApps = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), BlockAppsActivity.class);
                i.putExtra("idChild",idChildSelected);
                getActivity().startActivity(i);
            }
        };

        im_blockApps.setOnClickListener(blockApps);
        tv_blockApps.setOnClickListener(blockApps);

        View.OnClickListener informe = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), InformeActivity.class);
                i.putExtra("idChild",idChildSelected);
                getActivity().startActivity(i);
            }
        };

        im_informe.setOnClickListener(informe);
        tv_informe.setOnClickListener(informe);


        View.OnClickListener appUsage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), DayUsageActivity.class);
                i.putExtra("idChild",idChildSelected);
                getActivity().startActivity(i);
            }
        };

        im_appUsage.setOnClickListener(appUsage);
        tv_appUsage.setOnClickListener(appUsage);

        View.OnClickListener horaris = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), HorarisMainActivity.class);
                i.putExtra("idChild",idChildSelected);
                getActivity().startActivity(i);
            }
        };

        im_horaris.setOnClickListener(horaris);
        tv_horaris.setOnClickListener(horaris);

        LocalBroadcastManager.getInstance(root.getContext()).registerReceiver(messageReceiver,
                new IntentFilter("liveApp"));

        Button blockButton = (Button) root.findViewById(R.id.BT_BlockDevice);
        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = v.findViewById(R.id.BT_BlockDevice);
                Call<String> call = null;
                if(b.getText().equals(getString(R.string.block_device))){
                    call = mTodoService.blockChild(idChildSelected);
                }
                else call = mTodoService.unblockChild(idChildSelected);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                    }
                });
                if(b.getText().equals(getString(R.string.block_device))) b.setText(getString(R.string.unblock_device));
                else b.setText(getString(R.string.block_device));
            }
        });


        return root;
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            TextView currentApp = root.findViewById(R.id.TV_CurrentApp);

            String pkgName = intent.getStringExtra("pkgName");

            Funcions.setIconDrawable(getContext(),pkgName,IV_liveIcon);

            currentApp.setText(intent.getStringExtra("appName"));
        }
    };

    @Override
    protected void finalize() throws Throwable {
        askChildForLiveApp(idChildSelected, false);
        super.finalize();
    }

    private void askChildForLiveApp(long idChild, boolean liveApp){
        Call<String> call = mTodoService.askChildForLiveApp(idChild, liveApp);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!response.isSuccessful()){
                    Toast toast = Toast.makeText(getActivity(), getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast toast = Toast.makeText(getActivity(), getString(R.string.error_liveApp), Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }
}
