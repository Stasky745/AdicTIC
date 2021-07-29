package com.adictic.common.ui.informe.adapters;

import android.annotation.SuppressLint;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TopAppsAdapter extends RecyclerView.Adapter<TopAppsAdapter.TopAppsViewHolder> {
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm");

    private final LayoutInflater mInflater;
    private final ArrayList<AppUsage> appUsages;
    private final Context mContext;

    public TopAppsAdapter(Context context, List<AppUsage> list){
        mContext = context;
        mInflater = LayoutInflater.from(context);
        appUsages = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public TopAppsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.informe_apps_item, parent);

        return new TopAppsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopAppsViewHolder holder, int position) {
        AppUsage appUsage = appUsages.get(position);

        holder.TV_topAppTitle.setText(appUsage.app.appName);
        holder.TV_topApptime.setText(Funcions.millis2horaString(mContext, appUsage.totalTime));
        Funcions.setIconDrawable(mContext, appUsage.app.pkgName, holder.IV_topAppLogo);
    }

    @Override
    public int getItemCount() {
        return appUsages.size();
    }

    public class TopAppsViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView TV_topAppTitle, TV_topApptime;
        ImageView IV_topAppLogo;

        TopAppsViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView;
            TV_topApptime = root.findViewById(R.id.TV_top_app_time);
            TV_topAppTitle = root.findViewById(R.id.TV_top_app_title);
            IV_topAppLogo = root.findViewById(R.id.IV_top_app_logo);
        }
    }
}
