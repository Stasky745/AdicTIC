package com.example.adictic.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.entity.FillNom;
import com.example.adictic.entity.User;
import com.example.adictic.rest.TodoApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NomFill extends AppCompatActivity {
    private MyAdapter mAdapter;

    TodoApi mTodoService;

    ColorStateList oldColors;

    private class MyAdapter extends BaseAdapter{
        private List<FillNom> llista;
        LayoutInflater inflter;

        MyAdapter(List<FillNom> list){
            llista = list;
            inflter = (LayoutInflater.from(NomFill.this));
        }

        @Override
        public int getCount() {
            return llista.size();
        }

        @Override
        public FillNom getItem(int position) {
            return llista.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflter.inflate(R.layout.nom_fill_item,null);

            TextView nom = (TextView) convertView.findViewById(R.id.TV_nomFill);
            oldColors = nom.getTextColors();
            nom.setText(llista.get(position).deviceName);

            return convertView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nom_fill);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        User usuari = (User) bundle.getSerializable("user");
        final List<FillNom> llista = usuari.llista;

        final EditText tv_nom = (EditText)findViewById(R.id.TXT_fillNou);

        final ListView listView = (ListView) findViewById(R.id.LST_nomsActuals);

        mAdapter = new MyAdapter(llista);
        listView.setAdapter(mAdapter);

        tv_nom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for(int i = 0; i < llista.size(); i++){
                    View v = listView.getChildAt(i);
                    TextView tv = v.findViewById(R.id.TV_nomFill);

                    if(tv.getText().toString().equals(s.toString())){
                        tv.setTypeface(null, Typeface.BOLD);
                        tv.setTextColor(Color.BLUE);
                    }
                    else{
                        tv.setTypeface(null, Typeface.NORMAL);
                        tv.setTextColor(oldColors);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = listView.getItemAtPosition(position);
                FillNom fill = (FillNom)o;

                tv_nom.setText(fill.deviceName);
            }
        });

        Button b_log = (Button)findViewById(R.id.BT_fillNou);

        b_log.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                TextView TV_errorNoName = (TextView)findViewById(R.id.TV_errorNoName);
                TV_errorNoName.setVisibility(View.GONE);

                if(tv_nom.getText().toString().equals("")){
                    TV_errorNoName.setVisibility(View.VISIBLE);
                }
                else{
                    Call<String> call = mTodoService.sendName(tv_nom.getText().toString());

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
