package com.adictic.admin.ui.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adictic.admin.R;
import com.adictic.admin.entity.NewAdmin;
import com.adictic.admin.rest.AdminApi;
import com.adictic.admin.ui.profile.SubmitWeblinkFragment;
import com.adictic.admin.util.AdminApp;
import com.adictic.admin.util.Funcions;
import com.adictic.admin.util.hilt.AdminRepository;
import com.adictic.common.entity.AdminProfile;
import com.adictic.common.entity.Oficina;
import com.adictic.common.entity.WebLink;
import com.adictic.common.ui.AdminProfileActivity;
import com.adictic.common.util.Callback;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Response;

@AndroidEntryPoint
public class AdminCreatorActivity extends AppCompatActivity {

    @Inject
    AdminRepository repository;

    private TextInputEditText TIET_profileName;
    private TextInputEditText TIET_professio;
    private TextInputEditText TIET_desc;
    private TextInputEditText TIET_create_profileEmail;

    private ImageView IV_profilePic;
    private TextView TV_create_profile_no_links;
    private TextView TV_create_profile_error_hint;
    private Button acceptButton;

    private List<WebLink> webList;
    private RV_Links RV_Links;
    private RecyclerView RV_webLinks;

    private CheckBox CB_create_admin_office;
    private Spinner spinner_oficines;
    private List<Oficina> oficinaList;
    private ImageView IV_create_admin_office;
    private Oficina novaOficina;
    private Integer usernameQuant;

    private AdminApi mService;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_profile);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.crear_admin));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Funcions.closeKeyboard(findViewById(R.id.CL_create_admin_parent), this);

        mService = repository.getApi();
        sharedPreferences = repository.getEncryptedSharedPreferences();

        IV_profilePic = findViewById(R.id.IV_create_profilePic);
        TIET_profileName = findViewById(R.id.TIET_create_profileName);
        TIET_professio = findViewById(R.id.TIET_create_professio);
        TIET_desc = findViewById(R.id.TIET_create_desc);
        TIET_create_profileEmail = findViewById(R.id.TIET_create_profileEmail);

        TV_create_profile_no_links = findViewById(R.id.TV_create_profile_no_links);
        RV_webLinks = findViewById(R.id.RV_create_profileLinks);
        webList = new ArrayList<>();

        spinner_oficines = findViewById(R.id.SPIN_create_admin_office);
        CB_create_admin_office = findViewById(R.id.CB_create_admin_office);
        IV_create_admin_office = findViewById(R.id.IV_create_admin_office);
        TV_create_profile_error_hint = findViewById(R.id.TV_create_profile_error_hint);

        acceptButton = findViewById(R.id.BT_create_profileAccept);

        setImageHandler();
        setOficines();
        setWebLinks();
        setAcceptButton();
    }

    private void setImageHandler(){
        IV_profilePic.setClickable(true);
        IV_profilePic.setOnClickListener(view -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            resultAgafarImg.launch(photoPickerIntent);
        });
    }

    private void setOficines(){
        Call<List<Oficina>> call = mService.getOficines();
        call.enqueue(new Callback<List<Oficina>>() {
            @Override
            public void onResponse(@NonNull Call<List<Oficina>> call, @NonNull Response<List<Oficina>> response) {
                super.onResponse(call, response);
                String[] items;
                oficinaList = response.body();
                if(response.body()==null || response.body().isEmpty()) {
                    items = new String[]{"Nueva oficina"};
                } else {
                    items = new String[response.body().size()+1];
                    items[0] = "Nueva oficina";
                    for (int i = 1; i <= response.body().size(); i++) {
                        Oficina oficina = response.body().get(i-1);
                        items[i] = oficina.name + " - " + oficina.address + " - " + oficina.ciutat;
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdminCreatorActivity.this, android.R.layout.simple_spinner_dropdown_item, items);
                spinner_oficines.setAdapter(adapter);
                spinner_oficines.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if(i==0){
                            IV_create_admin_office.setVisibility(View.VISIBLE);
                            CB_create_admin_office.setVisibility(View.GONE);
                        } else {
                            IV_create_admin_office.setVisibility(View.GONE);
                            CB_create_admin_office.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {}
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<Oficina>> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });
        IV_create_admin_office.setOnClickListener(view -> {
           Intent i = new Intent(this, OfficeCreatorActivity.class);
           i.putExtra("oficina", novaOficina);
           mStartOffice.launch(i);
        });
    }

    ActivityResultLauncher<Intent> mStartOffice = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if(intent!=null)
                        novaOficina = intent.getParcelableExtra("oficina");
                }
            });

    @SuppressLint("NotifyDataSetChanged")
    private void setWebLinks(){
        RV_webLinks.setLayoutManager(new LinearLayoutManager(this));
        RV_Links = new RV_Links(this, webList);

        RV_webLinks.setAdapter(RV_Links);

        Button BT_addLink = findViewById(R.id.BT_create_addLink);
        BT_addLink.setOnClickListener(view -> {
            FragmentManager fm = getSupportFragmentManager();

            SubmitWeblinkFragment dialog = new SubmitWeblinkFragment();
            fm.setFragmentResultListener("weblink", this, (requestKey, bundle) -> {
                System.out.println("OriDebug: " + requestKey);
                if(requestKey.equals("weblink")) {
                    WebLink webLink = new WebLink();
                    webLink.name = bundle.getString("name");
                    webLink.url = bundle.getString("url");
                    webList.add(webLink);
                    RV_webLinks.setVisibility(View.VISIBLE);
                    TV_create_profile_no_links.setVisibility(View.GONE);
                    RV_Links.notifyDataSetChanged();
                }

            });
            fm.beginTransaction().add(dialog, getString(R.string.new_weblink)).commitNow();
        });
    }

    private class RV_Links extends RecyclerView.Adapter<RV_Links.MyViewHolder>{
        Context mContext;
        LayoutInflater mInflater;
        List<WebLink> webList;

        private class MyViewHolder extends RecyclerView.ViewHolder{
            protected View mRootView;
            TextView TV_weblink;
            ImageView IV_delete, IV_edit;

            MyViewHolder(@NonNull View itemView){
                super(itemView);

                mRootView = itemView;

                TV_weblink = mRootView.findViewById(R.id.TV_weblink);
                IV_delete = mRootView.findViewById(R.id.IV_delete);
                IV_edit = mRootView.findViewById(R.id.IV_edit);
            }
        }

        RV_Links(Context context, List<WebLink> list){
            mContext = context;
            webList = list;
            mInflater = LayoutInflater.from(mContext);
        }

        @NonNull
        @Override
        public RV_Links.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = mInflater.inflate(R.layout.weblink_item, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new RV_Links.MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final RV_Links.MyViewHolder holder, int position) {
            holder.TV_weblink.setText(webList.get(position).name);

            setDeleteIV(holder,position);
            setEditIV(holder,position);
        }

        @SuppressLint("NotifyDataSetChanged")
        private void setEditIV(RV_Links.MyViewHolder holder, int position) {
            holder.IV_edit.setClickable(true);

            holder.IV_edit.setOnClickListener(view -> {
                FragmentManager fm = AdminCreatorActivity.this.getSupportFragmentManager();

                WebLink webLink = webList.get(position);

                SubmitWeblinkFragment dialog = new SubmitWeblinkFragment(webLink, position);
                fm.setFragmentResultListener("weblink", AdminCreatorActivity.this, (requestKey, bundle) -> {
                    System.out.println("OriDebug: " + requestKey);
                    if(requestKey.equals("weblink")) {
                        int pos = bundle.getInt("posicio");

                        webList.get(pos).name = bundle.getString("name");
                        webList.get(pos).url = bundle.getString("url");

                        RV_Links.notifyDataSetChanged();
                    }

                });
                fm.beginTransaction().add(dialog, getString(R.string.weblink_edit)).commitNow();
            });
        }

        @SuppressLint("NotifyDataSetChanged")
        private void setDeleteIV(RV_Links.MyViewHolder holder, int position){
            holder.IV_delete.setClickable(true);

            holder.IV_delete.setOnClickListener(view -> new AlertDialog.Builder(mContext)
                    .setTitle(R.string.delete_weblink)
                    .setMessage(getString(R.string.confirm_delete_link,holder.TV_weblink.getText().toString()))

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(R.string.accept, (dialog, which) -> {
                        // Continue with delete operation
                        webList.remove(position);
                        if(webList.isEmpty()){
                            RV_webLinks.setVisibility(View.GONE);
                            TV_create_profile_no_links.setVisibility(View.VISIBLE);
                        }
                        notifyDataSetChanged();
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show());
        }

        @Override
        public int getItemViewType(int position) { return position; }

        @Override
        public int getItemCount() { return webList.size(); }
    }

    private void setAcceptButton(){
        acceptButton.setOnClickListener(view -> {
            boolean error = false;
            TV_create_profile_error_hint.setText("Error: ");
            TV_create_profile_error_hint.setVisibility(View.INVISIBLE);
            AdminProfile profile = new AdminProfile();
            profile.name = Objects.requireNonNull(TIET_profileName.getText()).toString();
            if(profile.name.trim().isEmpty() || !profile.name.trim().contains(" ")) {
                error = true;
                TV_create_profile_error_hint.setText(String.format("%sNombre invalido ", TV_create_profile_error_hint.getText()));
                TV_create_profile_error_hint.setVisibility(View.VISIBLE);
            }
            String email = Objects.requireNonNull(TIET_create_profileEmail.getText()).toString();
            if(email.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                if(error) TV_create_profile_error_hint.setText(String.format("%sy ", TV_create_profile_error_hint.getText()));
                error = true;
                TV_create_profile_error_hint.setText(String.format("%sEmail invalido", TV_create_profile_error_hint.getText()));
                TV_create_profile_error_hint.setVisibility(View.VISIBLE);
            }
            if(error) return;

            profile.professio = Objects.requireNonNull(TIET_professio.getText()).toString();
            profile.description = Objects.requireNonNull(TIET_desc.getText()).toString();
            profile.webLinks = webList;
            if(spinner_oficines.getSelectedItemPosition()==0) profile.oficina = novaOficina;
            else profile.oficina = oficinaList.get(spinner_oficines.getSelectedItemPosition()-1);

            NewAdmin newAdmin = new NewAdmin();
            newAdmin.username = calculateUsername(profile.name);
            newAdmin.email = email;
            newAdmin.adminProfile = profile;
            newAdmin.officeAdmin = CB_create_admin_office.isChecked();

            usernameQuant = 1;
            sendNewAdmin(newAdmin);
        });
    }

    private void sendNewAdmin(NewAdmin newAdmin){
        Call<String> call = mService.createNewAdmin(newAdmin);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                super.onResponse(call, response);
                if(response.isSuccessful()) {
                    Toast.makeText(AdminCreatorActivity.this, "Admin se ha creado correctamente.\nSe ha enviado un correo electronico.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    try {
                        JSONObject obj = new JSONObject(response.errorBody().string());
                        String[] errors = obj.getString("message").split("\\|");
                        for(String error : errors) {
                            switch (error.trim()) {
                                case "Username already exists":
                                    newAdmin.username += usernameQuant;
                                    usernameQuant++;
                                    sendNewAdmin(newAdmin);
                                    break;
                                case "Email already exist":
                                    Toast.makeText(AdminCreatorActivity.this, "Email ya existe", Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(AdminCreatorActivity.this, getString(R.string.error_noRegister), Toast.LENGTH_SHORT).show();
                                    System.err.println("Error desconegut HTTP: "+error);
                                    break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                super.onFailure(call, t);
            }
        });
    }

    private String calculateUsername(String name) {
        String[] strings = name.trim().split(" ");
        StringBuilder username = new StringBuilder();
        for(String s : strings){
            username.append(s.substring(0, 3));
        }
        return username.toString();
    }

    private void previewProfile(AdminProfile adminProfile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.previsualitzar_titol);
        builder.setMessage(R.string.previsualitzar_desc);
        builder.setPositiveButton(getString(R.string.accept), (dialogInterface, i) -> {
            Intent intent = new Intent(this, AdminProfileActivity.class);
            intent.putExtra("adminProfile", adminProfile);
            startActivity(intent);
            dialogInterface.dismiss();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }

    ActivityResultLauncher<Intent> resultAgafarImg = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    if(result.getData()==null) return;
                    final Uri imageUri = result.getData().getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    IV_profilePic.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(AdminCreatorActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }
            }
    );

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
