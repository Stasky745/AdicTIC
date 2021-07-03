package com.example.adictic.ui.inici;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BulletSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.example.adictic.R;
import com.example.adictic.entity.FillNom;
import com.example.adictic.entity.NouFillLogin;
import com.example.adictic.entity.User;
import com.example.adictic.entity.VellFillLogin;
import com.example.adictic.rest.TodoApi;
import com.example.adictic.ui.main.NavActivity;
import com.example.adictic.ui.permisos.AccessibilityPermActivity;
import com.example.adictic.ui.permisos.AppUsagePermActivity;
import com.example.adictic.ui.permisos.BackgroundLocationPerm;
import com.example.adictic.ui.permisos.DevicePolicyAdmin;
import com.example.adictic.util.Funcions;
import com.example.adictic.util.TodoApp;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class NomFill extends AppCompatActivity {
    TodoApi mTodoService;
    private SharedPreferences sharedPreferences;

    ColorStateList oldColors;

    Button BT_birthday;
    String birthday;
    private final DatePickerDialog.OnDateSetListener birthdayListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            birthday = year + "/" + (month + 1) + "/" + dayOfMonth;

            BT_birthday.setText(getString(R.string.date_format, dayOfMonth, getResources().getStringArray(R.array.month_names)[month + 1], year));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nom_fill);

        sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());

        mTodoService = ((TodoApp) this.getApplication()).getAPI();

        BT_birthday = findViewById(R.id.BT_birthday);

        setBirthdayButton();

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        User usuari = (User) bundle.getSerializable("user");
        final String token = bundle.getString("token");
        final long idParent = bundle.getLong("id");

        final List<FillNom> llista = usuari.llista;

        TextView tv = findViewById(R.id.TV_fillsActuals);
        if (llista.isEmpty()) {
            tv.setVisibility(GONE);
        } else {
            tv.setVisibility(VISIBLE);
        }

        final EditText tv_nom = findViewById(R.id.TXT_fillNou);

        final ListView listView = findViewById(R.id.LST_nomsActuals);

        MyAdapter mAdapter = new MyAdapter(llista);
        listView.setAdapter(mAdapter);

        tv_nom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                for (int i = 0; i < llista.size(); i++) {
                    View v = listView.getChildAt(i);
                    TextView tv = v.findViewById(R.id.TV_nomFill);

                    if (tv.getText().toString().equals(s.toString())) {
                        tv.setTypeface(null, Typeface.BOLD);
                        tv.setTextColor(getColor(R.color.colorPrimary));
                    } else {
                        tv.setTypeface(null, Typeface.NORMAL);
                        tv.setTextColor(oldColors);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Object o = listView.getItemAtPosition(position);
            FillNom fill = (FillNom) o;
            String[] bday = fill.birthday.split("/");

            int year = Integer.parseInt(bday[0]);
            int month = Integer.parseInt(bday[1]);
            int day = Integer.parseInt(bday[2]);

            BT_birthday.setText(getString(R.string.date_format, day, getResources().getStringArray(R.array.month_names)[month], year));

            tv_nom.setText(fill.deviceName);
        });

        Button b_log = findViewById(R.id.BT_fillNou);

        b_log.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(NomFill.this);
            ViewGroup viewGroup = findViewById(android.R.id.content);
            View dialog = LayoutInflater.from(this).inflate(R.layout.permisos_dispositiu_menor, viewGroup, false);
            builder.setView(dialog);
            AlertDialog alertDialog = builder.create();

            setDialogTexts(dialog);

            Button BT_permisAccept = dialog.findViewById(R.id.BT_acceptPermis);
            BT_permisAccept.setOnClickListener(v1 -> {
                login(token, idParent, llista, tv_nom);
                alertDialog.dismiss();
            });

            Button BT_permisCancel = dialog.findViewById(R.id.BT_cancelPermis);
            BT_permisCancel.setOnClickListener(v12 -> alertDialog.dismiss());

            alertDialog.show();

        });
    }

    private void setDialogTexts(View dialog) {
        TextView TV_permis1 = dialog.findViewById(R.id.TV_permis1);
        TextView TV_permis2 = dialog.findViewById(R.id.TV_permis2);
        TextView TV_permis3 = dialog.findViewById(R.id.TV_permis3);
        TextView TV_permis4 = dialog.findViewById(R.id.TV_permis4);
        TextView TV_permis5 = dialog.findViewById(R.id.TV_permis5);
        TextView TV_permis6 = dialog.findViewById(R.id.TV_permis6);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            TV_permis1.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            TV_permis2.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            TV_permis3.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            TV_permis4.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            TV_permis5.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            TV_permis6.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        // Posem estil a les frases que cal
        Spanned body;

        body = HtmlCompat.fromHtml(getString(R.string.permis_2), HtmlCompat.FROM_HTML_MODE_LEGACY);
        TV_permis2.setText(body);

        body = HtmlCompat.fromHtml(getString(R.string.permis_3), HtmlCompat.FROM_HTML_MODE_LEGACY);
        TV_permis3.setText(body);

        body = HtmlCompat.fromHtml(getString(R.string.permis_5), HtmlCompat.FROM_HTML_MODE_LEGACY);
        TV_permis5.setText(body);

        // Fem llistes amb punts
        CharSequence text = "";
        String[] fullText = getString(R.string.permis_4).split("\n");
        for(String item : fullText){
            SpannableString spannableString = new SpannableString(item + "\n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                spannableString.setSpan(new BulletSpan(BulletSpan.STANDARD_GAP_WIDTH, getColor(R.color.colorPrimary), 5), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else
                spannableString.setSpan(new BulletSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text = TextUtils.concat(text, spannableString);
        }
        TV_permis4.setText(text);

        text = "";
        fullText = getString(R.string.permis_6).split("\n");
        for(String item : fullText){
            SpannableString spannableString = new SpannableString(item + "\n");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                spannableString.setSpan(new BulletSpan(16, getColor(R.color.colorPrimary), 5), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            else
                spannableString.setSpan(new BulletSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text = TextUtils.concat(text, spannableString);
        }
        TV_permis6.setText(text);
    }

    private void login(String token, long idParent, List<FillNom> llista, EditText tv_nom) {
        TextView TV_errorNoName = findViewById(R.id.TV_errorNoName);
        TextView TV_errorNoBday = findViewById(R.id.TV_errorNoBday);

        TV_errorNoBday.setVisibility(GONE);
        TV_errorNoName.setVisibility(GONE);

        if (tv_nom.getText().toString().equals("")) {
            TV_errorNoName.setVisibility(VISIBLE);
        } else {
            long id = 0;
            boolean existeix = false;
            int i = 0;

            while (!existeix && i < llista.size()) {
                if (tv_nom.getText().toString().equals(llista.get(i).deviceName)) {
                    id = llista.get(i).idChild;
                    existeix = true;
                }
                i++;
            }

            if (existeix) { /* Canviar token d'un fill que existeix **/
                final VellFillLogin fillVell = new VellFillLogin();
                fillVell.deviceName = tv_nom.getText().toString();
                fillVell.idChild = id;
                fillVell.token = Crypt.getAES(token);

                Call<String> call = mTodoService.sendOldName(idParent, fillVell);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDUSER, fillVell.idChild).apply();
                            if (!Funcions.isAppUsagePermissionOn(NomFill.this)) {
                                NomFill.this.startActivity(new Intent(NomFill.this, AppUsagePermActivity.class));
                                NomFill.this.finish();
                            } else {
                                Funcions.startAppUsageWorker24h(getApplicationContext());
                                if (!Funcions.isAdminPermissionsOn(NomFill.this)) {
                                    NomFill.this.startActivity(new Intent(NomFill.this, DevicePolicyAdmin.class));
                                    NomFill.this.finish();
                                } else if (!Funcions.isAccessibilitySettingsOn(NomFill.this)) {
                                    NomFill.this.startActivity(new Intent(NomFill.this, AccessibilityPermActivity.class));
                                    NomFill.this.finish();
                                }
                                else if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
                                    NomFill.this.startActivity(new Intent(NomFill.this, BackgroundLocationPerm.class));
                                else {
                                    NomFill.this.startActivity(new Intent(NomFill.this, NavActivity.class));
                                    NomFill.this.finish();
                                }
                            }
                        } else {
                            Toast toast = Toast.makeText(NomFill.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast toast = Toast.makeText(NomFill.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            } else { /* Crear un fill nou **/

                if (birthday == null) {
                    TV_errorNoBday.setVisibility(VISIBLE);
                } else {
                    NouFillLogin fillNou = new NouFillLogin();
                    fillNou.deviceName = tv_nom.getText().toString();
                    fillNou.token = Crypt.getAES(token);
                    fillNou.birthday = birthday;

                    Call<Long> call = mTodoService.sendNewName(idParent, fillNou);

                    call.enqueue(new Callback<Long>() {
                        @Override
                        public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDUSER,response.body()).apply();
                                if (!Funcions.isAppUsagePermissionOn(NomFill.this)) {
                                    NomFill.this.startActivity(new Intent(NomFill.this, AppUsagePermActivity.class));
                                    NomFill.this.finish();
                                } else {
                                    Funcions.startAppUsageWorker24h(getApplicationContext());
                                    if (!Funcions.isAdminPermissionsOn(NomFill.this)) {
                                        NomFill.this.startActivity(new Intent(NomFill.this, DevicePolicyAdmin.class));
                                        NomFill.this.finish();
                                    } else if (!Funcions.isAccessibilitySettingsOn(NomFill.this)) {
                                        NomFill.this.startActivity(new Intent(NomFill.this, AccessibilityPermActivity.class));
                                        NomFill.this.finish();
                                    }
                                    else if(!Funcions.isBackgroundLocationPermissionOn(getApplicationContext()))
                                        NomFill.this.startActivity(new Intent(NomFill.this, BackgroundLocationPerm.class));
                                    else {
                                        NomFill.this.startActivity(new Intent(NomFill.this, NavActivity.class));
                                        NomFill.this.finish();
                                    }
                                }
                            } else {
                                Toast toast = Toast.makeText(NomFill.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
                            Toast toast = Toast.makeText(NomFill.this, getString(R.string.error_noLogin), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                }
            }
        }
    }

    private void setBirthdayButton() {
        BT_birthday.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(NomFill.this, R.style.datePicker, birthdayListener, year, month, day);
            datePicker.getDatePicker().setMaxDate(cal.getTimeInMillis());

            datePicker.show();
        });
    }

    private class MyAdapter extends BaseAdapter {
        private final List<FillNom> llista;
        LayoutInflater inflter;

        MyAdapter(List<FillNom> list) {
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

        @SuppressLint({"ViewHolder", "InflateParams"})
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflter.inflate(R.layout.nom_fill_item, null);

            TextView nom = convertView.findViewById(R.id.TV_nomFill);
            oldColors = nom.getTextColors();
            nom.setText(llista.get(position).deviceName);

            return convertView;
        }
    }
}
