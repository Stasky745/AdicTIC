package com.adictic.common.ui.informe.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.AppUsage;
import com.adictic.common.util.Funcions;
import com.adictic.common.util.hilt.HiltEntryPoint;
import com.adictic.common.util.hilt.Repository;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.EntryPointAccessors;

public class TopAppsAdapter extends RecyclerView.Adapter<TopAppsAdapter.TopAppsViewHolder> {
    private final LayoutInflater mInflater;
    private final ArrayList<AppUsage> appUsages;
    private final Context mContext;

    Repository repository;

    public TopAppsAdapter(Context context, List<AppUsage> list){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        appUsages = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public TopAppsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.informe_top_apps_item, parent, false);

        HiltEntryPoint mEntryPoint = EntryPointAccessors.fromApplication(mContext.getApplicationContext(), HiltEntryPoint.class);
        repository = mEntryPoint.getRepository();

        return new TopAppsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopAppsViewHolder holder, int position) {
        AppUsage appUsage = appUsages.get(position);

        holder.TV_topAppTitle.setText(appUsage.app.appName);
        holder.TV_topApptime.setText(Funcions.millis2horaString(mContext, appUsage.totalTime));
        repository.setIconDrawable(appUsage.app.pkgName, holder.IV_topAppLogo);
    }

    @Override
    public int getItemCount() {
        return appUsages.size();
    }

    public static class TopAppsViewHolder extends RecyclerView.ViewHolder {
        protected View root;
        private final TextView TV_topAppTitle;
        private final TextView TV_topApptime;
        private final ImageView IV_topAppLogo;

        TopAppsViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView;
            TV_topApptime = root.findViewById(R.id.TV_top_app_time);
            TV_topAppTitle = root.findViewById(R.id.TV_top_app_title);
            IV_topAppLogo = root.findViewById(R.id.IV_top_app_logo);
        }
    }
}
