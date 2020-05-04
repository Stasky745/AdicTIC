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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.activity.Informe;
import com.example.adictic.entity.FillNom;
import com.example.adictic.rest.TodoApi;

import java.util.Collection;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainParentFragment extends Fragment {

    private TodoApi mTodoService;
    private long idChildSelected = -1;
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.main_parent, container, false);
        mTodoService = ((TodoApp) getActivity().getApplication()).getAPI();

        Call<Collection<FillNom>> call = mTodoService.getUserChilds(((TodoApp) getActivity().getApplication()).getIDTutor());
        call.enqueue(new Callback<Collection<FillNom>>() {
            @Override
            public void onResponse(Call<Collection<FillNom>> call, Response<Collection<FillNom>> response) {
                if (response.isSuccessful() && response.body()!=null && !response.body().isEmpty()) {
                    LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.HSV_mainParent_LL);
                    int i = 1;
                    LinearLayout row = new LinearLayout(getActivity());
                    row.setLayoutParams(new LinearLayout.LayoutParams
                            (LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                    boolean primer = true;
                    for(FillNom child : response.body()) {
                        Button btnTag = new Button(getActivity());
                        btnTag.setLayoutParams(new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT));
                        btnTag.setText(child.deviceName);
                        btnTag.setTag(child.idChild);
                        if(primer) {
                            idChildSelected = child.idChild;
                            if(child.blocked){
                                ((Button)getActivity().findViewById(R.id.BT_BlockDevice)).setText(getString(R.string.unblock_device));
                            }
                            btnTag.setSelected(true);
                            primer = false;
                            askChildForLiveApp(idChildSelected, true);
                        }
                        btnTag.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                askChildForLiveApp(idChildSelected, false);
                                idChildSelected = (long)v.getTag();
                                TextView currentApp = root.findViewById(R.id.TV_CurrentApp);
                                currentApp.setText(getString(R.string.disconnected));
                                askChildForLiveApp(idChildSelected, true);
                            }
                        });
                        row.addView(btnTag);
                        i++;
                    }
                    linearLayout.addView(row);
                }
            }

            @Override
            public void onFailure(Call<Collection<FillNom>> call, Throwable t) {
            }
        });

        ImageView im_informe = (ImageView) root.findViewById(R.id.IV_Informe);
        TextView tv_informe = (TextView) root.findViewById(R.id.TV_Informe);
        ImageView im_horaris = (ImageView) root.findViewById(R.id.IV_AppUsage);
        TextView tv_horaris = (TextView) root.findViewById(R.id.TV_AppUsage);
        ImageView im_blockApps = (ImageView) root.findViewById(R.id.IV_BlockApps);
        TextView tv_blockApps = (TextView) root.findViewById(R.id.TV_BlockApps);
        ImageView im_notif = (ImageView) root.findViewById(R.id.IV_Horaris);
        TextView tv_notif = (TextView) root.findViewById(R.id.TV_Horaris);

        View.OnClickListener informe = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), Informe.class);
                i.putExtra("idChild",idChildSelected);
                getActivity().startActivity(i);
                System.out.println("idChild: " + idChildSelected);
            }
        };

        im_informe.setOnClickListener(informe);
        tv_informe.setOnClickListener(informe);

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
