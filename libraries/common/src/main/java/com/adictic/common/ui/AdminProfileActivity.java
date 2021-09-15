package com.adictic.common.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.common.R;
import com.adictic.common.entity.AdminProfile;
import com.adictic.common.entity.WebLink;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Funcions;

import java.util.ArrayList;
import java.util.Objects;

public class AdminProfileActivity extends AppCompatActivity {

    private AdminProfile adminProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);

        adminProfile = getIntent().getExtras().getParcelable("adminProfile");

        setDades();
    }

    private void setDades() {
        TextView TV_nomPerfil = findViewById(R.id.TV_nomPerfil);
        TV_nomPerfil.setText(adminProfile.name);

        TextView TV_desc = findViewById(R.id.TV_desc);
        TV_desc.setText(adminProfile.description);

        TextView TV_professio = findViewById(R.id.TV_professio);
        TV_professio.setText(adminProfile.professio);

        setFoto();
        setEnllacos();
        setOfficeButton();
    }

    private void setOfficeButton() {
        Button BT_oficina = findViewById(R.id.BT_oficina);
        if(adminProfile.oficina==null) BT_oficina.setVisibility(View.GONE);
        else BT_oficina.setOnClickListener(view -> {
                Intent intent = new Intent(this, OficinesActivity.class);
                intent.putExtra("idOficina", adminProfile.oficina.id);

                startActivity(intent);
            });
    }

    private void setEnllacos() {
        RecyclerView RV_profileLinks = findViewById(R.id.RV_profileLinks);
        TextView TV_profileLinks = findViewById(R.id.TV_profileLinks);
        if(adminProfile.webLinks == null || adminProfile.webLinks.isEmpty()){
            RV_profileLinks.setVisibility(View.GONE);
            TV_profileLinks.setVisibility(View.GONE);
        } else {
            RV_profileLinks.setLayoutManager(new LinearLayoutManager(this.getApplication()));
            ArrayList<WebLink> webList = new ArrayList<>(adminProfile.webLinks);
            RV_Adapter RVadapter = new RV_Adapter(getApplicationContext(), webList);

            RV_profileLinks.setAdapter(RVadapter);
        }
    }

    private void setFoto(){
        ImageView IV_profilePic = findViewById(R.id.IV_profilePic);
        long idAdmin;
        if(adminProfile == null || adminProfile.idAdmin == null)
            idAdmin = Objects.requireNonNull(Funcions.getEncryptedSharedPreferences(this)).getLong(Constants.SHARED_PREFS_ID_ADMIN,-1);
        else idAdmin = adminProfile.idAdmin;
        Funcions.setAdminPhoto(AdminProfileActivity.this, idAdmin, IV_profilePic);
    }

    public class RV_Adapter extends RecyclerView.Adapter<RV_Adapter.MyViewHolder> {
        ArrayList<WebLink> webList;
        Context mContext;
        LayoutInflater mInflater;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            protected View mRootView;

            TextView TV_webLink;
            ImageView IV_delete, IV_edit;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);

                mRootView = itemView;

                TV_webLink = (TextView) itemView.findViewById(R.id.TV_weblink);
                IV_delete = mRootView.findViewById(R.id.IV_delete);
                IV_edit = mRootView.findViewById(R.id.IV_edit);
            }
        }

        RV_Adapter(Context context, ArrayList<WebLink> list){
            mContext = context;
            webList = list;
            mInflater = LayoutInflater.from(mContext);
        }

        @NonNull
        @Override
        public RV_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = mInflater.inflate(R.layout.weblink_item, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            holder.TV_webLink.setText(webList.get(position).name);
            holder.IV_delete.setVisibility(View.GONE);
            holder.IV_edit.setVisibility(View.GONE);

            holder.mRootView.setOnClickListener(v -> {
                String url = webList.get(position).url;

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Funcions.getFullURL(url)));
                startActivity(browserIntent);
            });
        }

        @Override
        public int getItemCount() {
            return webList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }
}
