package com.example.adictic.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.Localitzacio;
import com.example.adictic.rest.TodoApi;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collection;

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
        setButton();
    }

    private void getLocalitzacions(){
        Call<Collection<Localitzacio>> call = mTodoService.getLocalitzacions();
        call.enqueue(new Callback<Collection<Localitzacio>>() {
            @Override
            public void onResponse(Call<Collection<Localitzacio>> call, Response<Collection<Localitzacio>> response) {
                if(response.isSuccessful()) setButton();
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

    private void setButton(){
        Button BT_enviar = (Button) findViewById(R.id.BT_enviar);
        BT_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });
    }
}
