package com.adictic.client.ui.support.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.client.R;
import com.adictic.common.util.Funcions;
import com.adictic.common.util.HiltEntryPoint;
import com.adictic.common.util.hilt.Repository;

import java.util.ArrayList;

import dagger.hilt.EntryPoints;

public class TextAdapter extends RecyclerView.Adapter<TextAdapter.TextViewHolder> {

    private final Context mContext;
    private final ArrayList<String> stringList;

    Repository repository;

    public TextAdapter(Context context, ArrayList<String> list){
        mContext = context;
        stringList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public TextAdapter.TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.support_text_item, parent, false);

        HiltEntryPoint mEntryPoint = EntryPoints.get(mContext, HiltEntryPoint.class);
        repository = mEntryPoint.getRepository();

        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TextAdapter.TextViewHolder holder, int position) {
        final String string = stringList.get(position);
        holder.text.setText(repository.getSpannedText(string));
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView text;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);

            text = itemView.findViewById(R.id.support_text_item_title);
        }
    }
}
