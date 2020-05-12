package com.example.adictic.activity;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.BlockAppEntity;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BlockAppsActivity extends AppCompatActivity {
    TodoApi mTodoService;

    Long idChild;
    List<BlockAppEntity> blockAppList;
    List<String> selectedApps;

    RecyclerView RV_appList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_app_layout);

        idChild = getIntent().getLongExtra("idChild",-1);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        selectedApps = new ArrayList<>();
        RV_appList = (RecyclerView) findViewById(R.id.RV_Apps);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        RV_appList.setLayoutManager(linearLayoutManager);

        setRecyclerView();
    }

    private void setRecyclerView(){
        Call<Collection<BlockAppEntity>> call = mTodoService.getBlockApps(idChild);

        call.enqueue(new Callback<Collection<BlockAppEntity>>() {
            @Override
            public void onResponse(Call<Collection<BlockAppEntity>> call, Response<Collection<BlockAppEntity>> response) {
                if(response.isSuccessful() && response.body() != null){
                    blockAppList = new ArrayList<>(response.body());

                    RV_Adapter RVadapter = new RV_Adapter(BlockAppsActivity.this,blockAppList);
                    RV_appList.setAdapter(RVadapter);
                }
            }

            @Override
            public void onFailure(Call<Collection<BlockAppEntity>> call, Throwable t) {

            }
        });
    }

    private class RV_Adapter extends RecyclerView.Adapter<RV_Adapter.MyViewHolder>{
        List<BlockAppEntity> blockAppList;
        Context mContext;

        RV_Adapter(Context context, List<BlockAppEntity> list){
            mContext = context;
            blockAppList = list;
        }


        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.block_app_item, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            holder.itemView.setActivated(selectedApps.contains(blockAppList.get(position).pkgName));
            if(holder.itemView.isActivated()) holder.itemView.setBackgroundColor(getResources().getColor(R.color.colorBackground));
            else holder.itemView.setBackgroundColor(Color.WHITE);

            BlockAppEntity blockedApp = blockAppList.get(position);

            holder.pkgName = blockedApp.pkgName;

            holder.TV_appName.setText(blockedApp.appName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                holder.TV_category.setText(ApplicationInfo.getCategoryTitle(mContext,blockedApp.appCategory));
            }
            else{
                holder.TV_category.setVisibility(View.INVISIBLE);
                holder.TV_categoryLabel.setVisibility(View.INVISIBLE);
            }

            if (blockedApp.appTime>0){
                Pair<Integer,Integer> pairTime = Funcions.millisToString(blockedApp.appTime);
                holder.TV_appMaxTime.setText(getResources().getString(R.string.hours_minutes,pairTime.first,pairTime.second));
            }
            else if(blockedApp.appTime == 0){
                holder.TV_hPerDay.setVisibility(View.GONE);
                holder.TV_appMaxTime.setText(getResources().getString(R.string.blocked));
            }
            else{
                holder.TV_hPerDay.setVisibility(View.GONE);
                holder.TV_appMaxTime.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return blockAppList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            String pkgName;

            ImageView IV_appIcon;
            TextView TV_appName, TV_appMaxTime, TV_category, TV_hPerDay, TV_categoryLabel;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);

                IV_appIcon = (ImageView) findViewById(R.id.IV_appIcon);
                TV_appName = (TextView) findViewById(R.id.TV_appName);
                TV_appMaxTime = (TextView) findViewById(R.id.TV_appMaxTime);
                TV_category = (TextView) findViewById(R.id.TV_Category);

                TV_hPerDay = (TextView) findViewById(R.id.TV_hPerDay);
                TV_categoryLabel = (TextView) findViewById(R.id.TV_categoryLabel);
            }

            @Override
            public void onClick(View v){
                MyViewHolder mvh = new MyViewHolder(v);
                if(selectedApps.contains(mvh.pkgName)) selectedApps.remove(mvh.pkgName);
                else selectedApps.add(mvh.pkgName);

                notifyItemChanged(mvh.getAdapterPosition());
            }
        }
    }
}
