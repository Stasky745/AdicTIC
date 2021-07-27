package com.adictic.common.ui.informe.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.CanvisHoraris;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HorarisNitAdapter extends RecyclerView.Adapter<HorarisNitAdapter.HorarisNitViewHolder>{
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM yyyy");
    private final ArrayList<CanvisHoraris> horarisList;
    private final LayoutInflater mInflater;

    public HorarisNitAdapter(List<CanvisHoraris> list, Context c){
        mInflater = LayoutInflater.from(c);
        horarisList = new ArrayList<>(list);

        horarisList.sort((o1, o2) -> o2.data.compareTo(o1.data));
    }

    @NonNull
    @Override
    public HorarisNitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.informe_item, parent);

        return new HorarisNitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HorarisNitViewHolder holder, int position) {
        CanvisHoraris canvisHoraris = horarisList.get(position);
        holder.TV_informeData.setText(dateFormatter.format(canvisHoraris.data));

        if(canvisHoraris.actiu)
            holder.TV_informeActiu.setVisibility(View.VISIBLE);
        else
            holder.TV_informeActiu.setVisibility(View.GONE);

        if(canvisHoraris.horariAntic == null)
            holder.TV_informeCanvis.setText(R.string.nou_horari);
        else if(canvisHoraris.horariNou == null)
            holder.TV_informeCanvis.setText(R.string.borrat_horari);
        else
            holder.TV_informeCanvis.setText(R.string.canvis_horari);

        holder.root.setOnClickListener(v -> {
            // Obrir dialog amb canvis
            TabbedDialog tabbedDialog = new TabbedDialog(horarisList.get(position));
            tabbedDialog.show(tabbedDialog.getChildFragmentManager(), "TabbedDialog");
        });
    }

    @Override
    public int getItemCount() {
        return horarisList.size();
    }

    public static class HorarisNitViewHolder extends RecyclerView.ViewHolder {
        View root;
        TextView TV_informeData, TV_informeCanvis, TV_informeActiu;

        public HorarisNitViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView;
            TV_informeCanvis = root.findViewById(R.id.TV_informe_item_canvis);
            TV_informeData = root.findViewById(R.id.TV_informe_item_data);
            TV_informeActiu = root.findViewById(R.id.TV_informe_item_final);
        }
    }
}
