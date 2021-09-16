package com.adictic.admin.ui.dubtes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.admin.entity.ChatInfo;
import com.adictic.admin.rest.AdminApi;
import com.adictic.admin.util.AdminApp;
import com.adictic.common.entity.Dubte;
import com.adictic.common.entity.Localitzacio;
import com.adictic.common.util.Callback;
import com.example.adictic_admin.R;
import com.adictic.admin.ui.Xats.XatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Response;

public class ConsultesFragment extends Fragment {

    private final String TAG = "ConsultesFragment";

    private TextView TV_localitats;
    private Button BT_filter;
    private RecyclerView RV_consultes;
    private View root;
    private AdminApi adminApi;
    private ArrayList<Dubte> dubtesList;
    private RV_Adapter RVadapter;

    private String[] localitatsArray;
    private boolean[] checkedArray;
    private ArrayList<String> filterPoblacions;

    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.consultes_layout, container, false);
        adminApi = ((AdminApp) requireActivity().getApplication()).getAPI();
        
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        setViews();
        getConsultes();
    }

    private void getConsultes() {
        Call<List<Dubte>> call = adminApi.getDubtes();
        call.enqueue(new Callback<List<Dubte>>() {
            @Override
            public void onResponse(@NotNull Call<List<Dubte>> call, @NotNull Response<List<Dubte>> response) {
                    super.onResponse(call, response);
                if(response.isSuccessful() && response.body() != null && !response.body().isEmpty()){
                    dubtesList = new ArrayList<>(response.body());
                    setRecyclerView();
                    setLocalitats();
                }
            }

            @Override
            public void onFailure(@NotNull Call<List<Dubte>> call, @NotNull Throwable t) {

            }
        });
    }

    private void setLocalitats(){
        // Agafar totes les localitats possibles
        Set<String> locList = new TreeSet<>();
        for (Dubte dubte : dubtesList){
//            locList.add(dubte.localitzacio.stream().map(Localitzacio::getNom).collect(Collectors.joining()));
            for (Localitzacio loc : dubte.localitzacio){
                locList.add(loc.poblacio);
            }
        }
        localitatsArray = locList.toArray(new String[0]);

        checkedArray = new boolean[locList.size()];
        Arrays.fill(checkedArray,false);

        filterPoblacions = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Escull les poblacions que vols veure.");
        builder.setMultiChoiceItems(localitatsArray, checkedArray, (dialogInterface, i, b) -> {
            checkedArray[i] = b;
            if(b)
                filterPoblacions.add(localitatsArray[i]);
            else
                filterPoblacions.remove(localitatsArray[i]);
        });
        builder.setPositiveButton(R.string.accept, (dialogInterface, i) -> {
            String filteredLocs = "";
            for(String s : filterPoblacions) filteredLocs = filteredLocs.concat(s).concat(", ");
            if(filteredLocs.length()>2) filteredLocs = filteredLocs.substring(0,filteredLocs.length()-2);
            TV_localitats.setText(filteredLocs);

            setFilter();

            dialogInterface.dismiss();
        });
        builder.create();

        BT_filter.setOnClickListener(view -> builder.show());
    }

    private void setFilter(){
        ArrayList<Dubte> filteredDubtes;
        if(filterPoblacions.isEmpty()) filteredDubtes = dubtesList;
        else {
//            for(Dubte dubte : dubtesList){
//                ArrayList<String> dubtePoblacions = dubte.localitzacio.stream()
//                        .map(localitzacio -> localitzacio.poblacio)
//                        .collect(Collectors.toCollection(ArrayList::new));
//                if(!Collections.disjoint(dubtePoblacions, filterPoblacions))
//                    filteredDubtes.add(dubte);
//            }

            filteredDubtes = dubtesList.stream()
                    .filter(dubte -> !Collections.disjoint(
                                            dubte.localitzacio.stream()
                                            .map(localitzacio -> localitzacio.poblacio)
                                            .collect(Collectors.toCollection(ArrayList::new))
                            , filterPoblacions))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        RVadapter.filterList(filteredDubtes);
    }

    private void setRecyclerView(){
        RV_consultes.setLayoutManager(new LinearLayoutManager(getContext()));
        RVadapter = new RV_Adapter(getContext(), dubtesList);
        RV_consultes.setAdapter(RVadapter);
    }

    private void setViews() {
        TV_localitats = root.findViewById(R.id.TV_localitats);
        BT_filter = root.findViewById(R.id.BT_filter);
        RV_consultes = root.findViewById(R.id.RV_consultes);
    }

    public static class RV_Adapter extends RecyclerView.Adapter<RV_Adapter.MyViewHolder> {
        ArrayList<Dubte> dubteArrayList;
        Context mContext;
        LayoutInflater mInflater;

        RV_Adapter(Context context, ArrayList<Dubte> list) {
            mContext = context;
            dubteArrayList = list;
            mInflater = LayoutInflater.from(mContext);
        }

        @NonNull
        @Override
        public RV_Adapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = mInflater.inflate(R.layout.dubte_rv_item, parent, false);


            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
            Dubte dubteItem = dubteArrayList.get(position);
            holder.TV_dubteTitol.setText(dubteItem.titol);

            String localitzacions = "";
            for (Localitzacio localitzacio : dubteItem.localitzacio){
                localitzacions = localitzacions.concat(localitzacio.poblacio).concat(", ");
            }
            localitzacions = localitzacions.substring(0,localitzacions.length()-2);
            holder.TV_dubteLocalitats.setText(localitzacions);

            String finalLocalitzacions = localitzacions;
            holder.mRootView.setOnClickListener(view -> {
                // Obrir el dubte en un AlertDialog
                new AlertDialog.Builder(mContext)
                        .setTitle(dubteItem.titol)
                        .setMessage(dubteItem.descripcio.concat("\n").concat(finalLocalitzacions))
                        .setPositiveButton("Acceptar Consulta", (dialogInterface, i) -> {
                            AdminApi adminApi = ((AdminApp) mContext.getApplicationContext()).getAPI();
                            Call<ChatInfo> call = adminApi.getUserChatInfo(dubteItem.id);
                            call.enqueue(new Callback<ChatInfo>() {
                                @Override
                                public void onResponse(@NotNull Call<ChatInfo> call, @NotNull Response<ChatInfo> response) {
                    super.onResponse(call, response);
                                    if(response.isSuccessful()){
                                        if(response.body() == null)
                                            Toast.makeText(mContext, "Aquesta consulta ja ha estat agafada per algú. Refresca la llista.", Toast.LENGTH_SHORT).show();
                                        else{
                                            // Entrem al xat
                                            ChatInfo userProfile = response.body();
                                            userProfile.hasAccess = false;
                                            userProfile.lastMessage = dubteItem.descripcio;
                                            Intent intent = new Intent(mContext, XatActivity.class);
                                            intent.putExtra("chat",userProfile);

                                            mContext.startActivity(intent);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull Call<ChatInfo> call, @NotNull Throwable t) {
                                    Toast.makeText(mContext, "No s'ha pogut connectar amb el servidor.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel·lar Consulta", (dialogInterface, i) -> dialogInterface.cancel())
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return dubteArrayList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public void refreshList(){
            notifyDataSetChanged();
        }

        public void filterList(ArrayList<Dubte> fList) {
            dubteArrayList = fList;
            notifyDataSetChanged();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {
            protected View mRootView;

            TextView TV_dubteTitol, TV_dubteLocalitats;

            MyViewHolder(@NonNull View itemView) {
                super(itemView);

                mRootView = itemView;

                TV_dubteTitol = itemView.findViewById(R.id.TV_dubteTitol);
                TV_dubteLocalitats = itemView.findViewById(R.id.TV_dubteLocalitats);
            }
        }
    }
}