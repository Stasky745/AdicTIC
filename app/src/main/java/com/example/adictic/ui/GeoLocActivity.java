package com.example.adictic.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.entity.GeoFill;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.TodoApp;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeoLocActivity extends AppCompatActivity {
    private TodoApi mTodoService;

    private MapView map = null;

    private long idChild;

    private Spinner SP_fills;

    private List<GeoFill> fills = new ArrayList<>();

    ArrayList<Marker> markers = new ArrayList<>();
    private int posicio = 0;

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        try{
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        }catch(java.lang.NullPointerException exc) {
            exc.printStackTrace();
        }
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        //Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        idChild = getIntent().getLongExtra("idChild",0);

        setContentView(R.layout.oficines_layout);
        SP_fills = findViewById(R.id.SP_listOficines);

        map = (MapView) findViewById(R.id.MV_map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        mTodoService = ((TodoApp) getApplication()).getAPI();

        demanarLocFills();
    }

    public void demanarLocFills(){
        // Actualitzem la llista de fills
        Call<List<GeoFill>> call = mTodoService.getGeoLoc();
        call.enqueue(new Callback<List<GeoFill>>() {
            @Override
            public void onResponse(Call<List<GeoFill>> call, Response<List<GeoFill>> response) {
                if (response.isSuccessful() && !response.body().isEmpty() && response.body().get(0) != null) {
                    fills = response.body();

                    if(TodoApp.getTutor() == 0 && TodoApp.getIDChild() > 0){
                        boolean trobat = false;
                        int i = 0;
                        while (!trobat && i < fills.size()){
                            if(fills.get(i).id == TodoApp.getIDChild()){
                                trobat = true;
                                GeoFill fill = fills.get(i);
                                fills.clear();
                                fills.add(fill);
                            }
                            i++;
                        }
                    }

                    TodoApp.setGeoFills(fills);
                    setMap();

                } else {
                    fills = TodoApp.getGeoFills();
                    Toast.makeText(getApplicationContext(), getString(R.string.error_noData), Toast.LENGTH_SHORT).show();
                    if(!fills.isEmpty()) setMap();
                }
            }

            @Override
            public void onFailure(Call<List<GeoFill>> call, Throwable t) {
                fills = TodoApp.getGeoFills();
                Toast.makeText(getApplicationContext(), getString(R.string.error_noData), Toast.LENGTH_SHORT).show();
                if(!fills.isEmpty()) setMap();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void setMap(){
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(17.0);

        int i = 0;
        for(GeoFill fill : fills){
            if(fill != null) {
                Marker marker = new Marker(map);
                if (fill.latitud != null && fill.longitud != null)
                    marker.setPosition(new GeoPoint(fill.latitud, fill.longitud));
                marker.setTitle(fill.nom);
                marker.setSnippet(fill.hora);

                marker.setRelatedObject(fill);

                marker.setOnMarkerClickListener((marker1, mapView) -> {
                    if (marker1.isInfoWindowShown()) InfoWindow.closeAllInfoWindowsOn(map);
                    else {
                        int pos = markers.indexOf(marker1);
                        SP_fills.setSelection(pos);
                        marker1.showInfoWindow();
                        map.getController().setCenter(setInfoWindowOffset(marker1.getPosition()));
                    }

                    return true;
                });

                markers.add(marker);
                map.getOverlays().add(marker);

                if (fill.id == idChild) posicio = i;
                i++;
            }
        }

        GeoPoint startPoint;

        if(!fills.isEmpty()){
            startPoint = new GeoPoint(fills.get(0).latitud,fills.get(0).longitud);
        }
        else{
            startPoint = new GeoPoint(41.981177,2.818997); // Oficina Girona
        }

        mapController.setCenter(startPoint);
        setSpinner();
    }

    private void setSpinner(){
        SpinAdapter adapter = new SpinAdapter(getApplicationContext(),android.R.layout.simple_spinner_item,markers);

        SP_fills.setAdapter(adapter);

        SP_fills.setSelection(posicio);

        SP_fills.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Marker marker = markers.get(i);
                InfoWindow.closeAllInfoWindowsOn(map);
                map.getController().setCenter(setInfoWindowOffset(marker.getPosition()));
                marker.showInfoWindow();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
    }

    public GeoPoint setInfoWindowOffset(GeoPoint gp){
        return new GeoPoint(gp.getLatitude()+0.0025, gp.getLongitude());
    }

    private class SpinAdapter extends ArrayAdapter<Marker>{
        ArrayList<Marker> markers;
        public SpinAdapter(@NonNull Context context, int resource, @NonNull List<Marker> objects) {
            super(context, resource, objects);
            markers = new ArrayList<>(objects);
        }

        @Override
        public int getCount() {
            return markers.size();
        }

        @Nullable
        @Override
        public Marker getItem(int position) {
            return markers.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            TextView label = (TextView) super.getView(position,convertView,parent);
            label.setTextColor(getColor(R.color.colorPrimary));
            GeoFill marker = (GeoFill) markers.get(position).getRelatedObject();
            label.setText(marker.nom);
            return label;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            TextView label = (TextView) super.getDropDownView(position, convertView, parent);
            GeoFill marker = (GeoFill) markers.get(position).getRelatedObject();
            label.setText(marker.nom);
            label.setGravity(Gravity.CENTER_VERTICAL);
            label.setHeight(100);

            return label;
        }
    }
}
