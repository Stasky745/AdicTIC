package com.adictic.common.ui.settings.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.NotificationInformation;
import com.adictic.common.util.Funcions;

import java.util.List;

public class NotificationRVadapter extends RecyclerView.Adapter<NotificationRVadapter.notifViewHolder> {
    private final Context mContext;
    private final List<NotificationInformation> notifList;
    private final LayoutInflater mInflater;

    NotificationRVadapter(Context context,List<NotificationInformation> list) {
        mContext = context;
        notifList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public notifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.notif_list_item, parent, false);
        return new notifViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull notifViewHolder holder, int position) {
        NotificationInformation notif = notifList.get(position);

        if(notif.read)
            holder.CL_notif_bground.setBackgroundResource(0);
        else
            holder.CL_notif_bground.setBackgroundResource(R.drawable.rounded_rectangle_received);

        holder.TV_body.setText(notif.message);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            holder.TV_body.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        holder.TV_title.setText(notif.title);
        holder.TV_childName.setText(notif.childName);
        holder.TV_date.setText(Funcions.millis2dateTime(notif.dateMillis));

        holder.mRootView.setOnClickListener(v -> {
            notif.read = !notif.read;
            this.editItem(position);
        });
    }

    @Override
    public int getItemCount() { return notifList.size(); }

    private void updateList() {
        SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(mContext);
        assert sharedPreferences != null;

        Funcions.setNotificationList(mContext, notifList);
    }

    public void editItem(int position){
        this.notifyItemChanged(position);
        updateList();
    }

    public void insertItem(int position){
        this.notifyItemInserted(position);
        updateList();
    }

    public void deleteItem(int position){
        this.notifyItemRemoved(position);
        updateList();
    }

    public static class notifViewHolder extends RecyclerView.ViewHolder {
        protected View mRootView;
        TextView TV_childName, TV_date, TV_title, TV_body;
        ConstraintLayout CL_notif_bground;

        notifViewHolder(@NonNull View itemView) {
            super(itemView);

            mRootView = itemView;

            CL_notif_bground = itemView.findViewById(R.id.CL_notif_bground);

            TV_childName = itemView.findViewById(R.id.TV_notif_nom_fill);
            TV_date = itemView.findViewById(R.id.TV_notif_date);
            TV_title = itemView.findViewById(R.id.TV_notif_title);
            TV_body = itemView.findViewById(R.id.TV_notif_body);
        }
    }
}
