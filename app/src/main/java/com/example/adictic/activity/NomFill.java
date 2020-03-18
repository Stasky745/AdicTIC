package com.example.adictic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.rest.TodoApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NomFill extends AppCompatActivity {

    TodoApi mTodoService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nom_fill);

        Button b_log = (Button)findViewById(R.id.BT_fillNou);

        b_log.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText newName = (EditText)findViewById(R.id.TXT_fillNou);

                TextView TV_errorNoName = (TextView)findViewById(R.id.TV_errorNoName);
                TV_errorNoName.setVisibility(View.GONE);

                if(newName.getText().toString().equals("")){
                    TV_errorNoName.setVisibility(View.VISIBLE);
                }
                else{
                    Call<String> call = mTodoService.sendName(newName.getText().toString());

                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {
                                NomFill.this.startActivity(new Intent(NomFill.this, MainActivity.class));
                                NomFill.this.finish();
                            } else {
                                Toast toast = Toast.makeText(NomFill.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast toast = Toast.makeText(NomFill.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
            }
        });
    }
}
