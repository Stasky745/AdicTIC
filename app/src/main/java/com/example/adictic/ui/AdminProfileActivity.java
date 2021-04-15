package com.example.adictic.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.adictic.R;
import com.example.adictic.entity.AdminProfile;
import com.example.adictic.entity.WebLink;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.support.OficinesActivity;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileActivity extends AppCompatActivity {
    AdminProfile adminProfile;
    TodoApi todoApi;

    ArrayList<WebLink> webList;
    RV_Adapter RVadapter;
    RecyclerView RV_profileLinks;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);

        todoApi = ((TodoApp) this.getApplication()).getAPI();
        adminProfile = (AdminProfile) getIntent().getExtras().get("adminProfile");

        setDades();
    }

    private void setDades() {
        TextView TV_nomPerfil = (TextView) findViewById(R.id.TV_nomPerfil);
        TV_nomPerfil.setText(adminProfile.name);

        TextView TV_desc = (TextView) findViewById(R.id.TV_desc);
        TV_desc.setText(adminProfile.description);

        TextView TV_professio = (TextView) findViewById(R.id.TV_professio);
        TV_professio.setText(adminProfile.professio);

        setFoto();
        setRecyclerView();
        setOfficeButton();
    }

    private void setOfficeButton() {
        Button BT_oficina = findViewById(R.id.BT_oficina);
        BT_oficina.setOnClickListener(view -> {
            Intent intent = new Intent(this, OficinesActivity.class);
            intent.putExtra("idOficina", adminProfile.idOficina);

            startActivity(intent);
        });
    }

    private void setRecyclerView() {
        RV_profileLinks = (RecyclerView) findViewById(R.id.RV_profileLinks);
        RV_profileLinks.setLayoutManager(new LinearLayoutManager(this.getApplication()));
        webList = new ArrayList<>(adminProfile.webLinks);
        RVadapter = new RV_Adapter(getApplicationContext(), webList);

        RV_profileLinks.setAdapter(RVadapter);
    }

    private void setFoto() {
        ImageView IV_profilePic = (ImageView) findViewById(R.id.IV_profilePic);

        Call<ResponseBody> call = todoApi.getAdminPicture(adminProfile.idUser);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Bitmap bmp = BitmapFactory.decodeStream(response.body().byteStream());
                    IV_profilePic.setImageBitmap(bmp);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public class RV_Adapter extends RecyclerView.Adapter<RV_Adapter.MyViewHolder> {
        ArrayList<WebLink> webList;
        Context mContext;
        LayoutInflater mInflater;

        RV_Adapter(Context context, ArrayList<WebLink> list) {
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
    }
}
