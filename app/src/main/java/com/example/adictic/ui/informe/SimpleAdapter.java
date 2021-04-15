package com.example.adictic.ui.informe;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic.R;
import com.example.adictic.util.Funcions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.MyViewHolder> {
    private Map<String,Long> mDataset;

    private Context context;

    /** Si tipus = 0 s'imprimeix el long del map; tipus = 1 es passa a "x hores y minuts" **/
    private int tipus;

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView value;

        public MyViewHolder(View v){
            super(v);

            name = v.findViewById(R.id.TV_nomApp);
            value = v.findViewById(R.id.TV_value);
        }
    }

    public SimpleAdapter(Map<String,Long> map, int t, Context ctx){
        mDataset = map;
        tipus = t;
        context = ctx;
    }

    @Override
    public SimpleAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_rv_item,parent,false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position){
        String title = (String) mDataset.keySet().toArray()[position];
        holder.name.setText(title);

        List<Long> listValues = new ArrayList<>(mDataset.values());
        if(tipus == 0) {
            String value = String.valueOf(listValues.get(position));
            holder.value.setText(value);
        }
        else{
            Long l = listValues.get(position);
            Pair<Integer,Integer> pair = Funcions.millisToString(l);
            String value = context.getString(R.string.hours_minutes,pair.first,pair.second);
            holder.value.setText(value);
        }
    }

    @Override
    public int getItemCount(){
        return mDataset.size();
    }

}
