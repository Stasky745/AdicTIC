package com.example.adictic.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.adictic.R;
import com.example.adictic.TodoApp;
import com.example.adictic.entity.FillNom;
import com.example.adictic.entity.NouFillLogin;
import com.example.adictic.entity.User;
import com.example.adictic.entity.VellFillLogin;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.util.Funcions;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NomFill extends AppCompatActivity {
    private MyAdapter mAdapter;

    TodoApi mTodoService;

    ColorStateList oldColors;

    Button BT_birthday;
    String birthday;

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

            TextView nom = convertView.findViewById(R.id.TV_nomFill);
            oldColors = nom.getTextColors();
            nom.setText(llista.get(position).deviceName);

            return convertView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nom_fill);

        mTodoService = ((TodoApp)this.getApplication()).getAPI();

        BT_birthday = (Button) findViewById(R.id.BT_birthday);

        setBirthdayButton();

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        User usuari = (User) bundle.getSerializable("user");
        final String token = bundle.getString("token");
        final long idParent = bundle.getLong("id");

        final List<FillNom> llista = usuari.llista;

        TextView tv = findViewById(R.id.TV_fillsActuals);
        if(llista.isEmpty()){
            tv.setVisibility(GONE);
        }
        else{
            tv.setVisibility(VISIBLE);
        }

        final EditText tv_nom = findViewById(R.id.TXT_fillNou);

        final ListView listView = findViewById(R.id.LST_nomsActuals);

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
                        tv.setTextColor(getResources().getColor(R.color.colorPrimary));
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

        Button b_log = findViewById(R.id.BT_fillNou);

        b_log.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                TextView TV_errorNoName = findViewById(R.id.TV_errorNoName);
                TextView TV_errorNoBday = findViewById(R.id.TV_errorNoBday);

                TV_errorNoBday.setVisibility(GONE);
                TV_errorNoName.setVisibility(GONE);

                if(tv_nom.getText().toString().equals("")){
                    TV_errorNoName.setVisibility(VISIBLE);
                }
                else if(birthday == null){
                    TV_errorNoBday.setVisibility(VISIBLE);
                }
                else{
                    long id = 0;
                    boolean existeix = false;
                    int i = 0;

                    while(!existeix && i < llista.size()){
                        if(tv_nom.getText().toString().equals(llista.get(i).deviceName)){
                            id=llista.get(i).idChild;
                            existeix=true;
                        }
                        i++;
                    }

                    if(existeix) { /** Canviar token d'un fill que existeix **/
                        final VellFillLogin fillVell = new VellFillLogin();
                        fillVell.deviceName = tv_nom.getText().toString();
                        fillVell.idChild = id;
                        fillVell.token = token;

                        Call<String> call = mTodoService.sendOldName(idParent, fillVell);

                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if (response.isSuccessful()) {
                                    TodoApp.setIDChild(fillVell.idChild);
                                    if(!Funcions.isAdminPermissionsOn(NomFill.this)){
                                        NomFill.this.startActivity(new Intent(NomFill.this, DevicePolicyAdmin.class));
                                        NomFill.this.finish();
                                    }
                                    else if(!Funcions.isAppUsagePermissionOn(NomFill.this)){
                                        NomFill.this.startActivity(new Intent(NomFill.this, AppUsagePermActivity.class));
                                        NomFill.this.finish();
                                    }
                                    else if(!Funcions.isAccessibilitySettingsOn(NomFill.this)){
                                        NomFill.this.startActivity(new Intent(NomFill.this, AccessibilityPermActivity.class));
                                        NomFill.this.finish();
                                    }
                                    else{
                                        NomFill.this.startActivity(new Intent(NomFill.this, MainActivityChild.class));
                                        NomFill.this.finish();
                                    }
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
                    else { /** Crear un fill nou **/
                        NouFillLogin fillNou = new NouFillLogin();
                        fillNou.deviceName = tv_nom.getText().toString();
                        fillNou.token = token;
                        fillNou.birthday = birthday;

                        Call<Long> call = mTodoService.sendNewName(idParent, fillNou);

                        call.enqueue(new Callback<Long>() {
                            @Override
                            public void onResponse(Call<Long> call, Response<Long> response) {
                                if (response.isSuccessful()) {
                                    TodoApp.setIDChild(response.body());
                                    if(!Funcions.isAdminPermissionsOn(NomFill.this)){
                                        NomFill.this.startActivity(new Intent(NomFill.this, DevicePolicyAdmin.class));
                                        NomFill.this.finish();
                                    }
                                    else if(!Funcions.isAppUsagePermissionOn(NomFill.this)){
                                        NomFill.this.startActivity(new Intent(NomFill.this, AppUsagePermActivity.class));
                                        NomFill.this.finish();
                                    }
                                    else if(!Funcions.isAccessibilitySettingsOn(NomFill.this)){
                                        NomFill.this.startActivity(new Intent(NomFill.this, AccessibilityPermActivity.class));
                                        NomFill.this.finish();
                                    }
                                    else{
                                        NomFill.this.startActivity(new Intent(NomFill.this, MainActivityChild.class));
                                        NomFill.this.finish();
                                    }
                                } else {
                                    Toast toast = Toast.makeText(NomFill.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Long> call, Throwable t) {
                                Toast toast = Toast.makeText(NomFill.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });
                    }
                }
            }
        });
    }

    private void setBirthdayButton(){
        BT_birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(NomFill.this,R.style.datePicker,birthdayListener,year,month,day);
                datePicker.getDatePicker().setMaxDate(cal.getTimeInMillis());

                datePicker.show();
            }
        });
    }

    private DatePickerDialog.OnDateSetListener birthdayListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            birthday = year+"/"+(month+1)+"/"+dayOfMonth;

            BT_birthday.setText(getResources().getString(R.string.date_format,dayOfMonth,getResources().getStringArray(R.array.month_names)[month+1],year));
        }
    };
}
