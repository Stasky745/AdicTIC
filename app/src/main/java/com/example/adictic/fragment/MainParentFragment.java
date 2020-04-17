package com.example.adictic.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.FillNom;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Global;

import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainParentFragment extends Fragment {

    private TodoApi mTodoService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main_parent, container, false);
        mTodoService = ((TodoApp) getActivity().getApplication()).getAPI();

        Call<Collection<FillNom>> call = mTodoService.getUserChilds(Global.ID);
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
                    for(FillNom child : response.body()) {
                        Button btnTag = new Button(getActivity());
                        btnTag.setLayoutParams(new LinearLayout.LayoutParams
                                (LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT));
                        btnTag.setText(child.deviceName);
                        btnTag.setId(i);
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

        ImageView im_informe = (ImageView) getActivity().findViewById(R.id.IV_Informe);
        TextView tv_informe = (TextView) getActivity().findViewById(R.id.TV_Informe);
        ImageView im_horaris = (ImageView) getActivity().findViewById(R.id.IV_AppUsage);
        TextView tv_horaris = (TextView) getActivity().findViewById(R.id.TV_AppUsage);
        ImageView im_blockApps = (ImageView) getActivity().findViewById(R.id.IV_BlockApps);
        TextView tv_blockApps = (TextView) getActivity().findViewById(R.id.TV_BlockApps);
        ImageView im_notif = (ImageView) getActivity().findViewById(R.id.IV_Horaris);
        TextView tv_notif = (TextView) getActivity().findViewById(R.id.TV_Horaris);

        /*im_informe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(),Informe.class));
            }
        });*/


        return root;
    }
}
