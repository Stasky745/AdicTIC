package com.example.adictic.activity.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.Dubte;
import com.example.adictic.entity.Localitzacio;
import com.example.adictic.rest.TodoApi;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnviarDubte extends Activity {
    TextInputEditText TIET_titol;
    TextInputEditText TIET_desc;
    Spinner SP_local;
    TodoApi mTodoService;
    Context mCtx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enviar_dubte);
        mTodoService = ((TodoApp)this.getApplication()).getAPI();
        mCtx = getApplicationContext();

        TIET_titol = (TextInputEditText) findViewById(R.id.TIET_titol);
        TIET_desc = (TextInputEditText) findViewById(R.id.TIET_desc);
        SP_local = (Spinner) findViewById(R.id.SP_localitats);

        getLocalitzacions();
        setSendButton();
    }

    private void getLocalitzacions(){
        Call<Collection<Localitzacio>> call = mTodoService.getLocalitzacions();
        call.enqueue(new Callback<Collection<Localitzacio>>() {
            @Override
            public void onResponse(Call<Collection<Localitzacio>> call, Response<Collection<Localitzacio>> response) {
                if(response.isSuccessful()) setLocalitzacions(response.body());
                else{
                    Toast toast = Toast.makeText(mCtx, R.string.error_local, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<Collection<Localitzacio>> call, Throwable t) {
                Toast toast = Toast.makeText(mCtx, R.string.error_server_read, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void setSendButton(){
        Button BT_enviar = (Button) findViewById(R.id.BT_enviar);
        BT_enviar.setOnClickListener(v -> {
            Dubte newDubte = new Dubte();
            newDubte.titol = TIET_titol.getText().toString();
            newDubte.descripcio = TIET_desc.getText().toString();
            //newDubte.localitzacio = ((Localitzacio) SP_local.getSelectedItem()).id;

            Call<String> call = mTodoService.postDubte(newDubte);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if(response.isSuccessful()){ finish(); }
                    else{
                        Toast toast = Toast.makeText(mCtx, R.string.error_local, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast toast = Toast.makeText(mCtx, R.string.error_server_read, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        });
    }

    private void setLocalitzacions(Collection<Localitzacio> localitzacions){
        ((List<Localitzacio>) localitzacions).add(0,new Localitzacio((long) 0, "Online"));
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<Localitzacio> spinnerArrayAdapter = new ArrayAdapter<Localitzacio>
                (this, android.R.layout.simple_spinner_item, (List<Localitzacio>) localitzacions);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        SP_local.setAdapter(spinnerArrayAdapter);
    }
}
