package com.adictic.common.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.adictic.common.R;
import com.adictic.common.util.App;
import com.adictic.common.util.Funcions;
import com.adictic.common.entity.ChangePassword;
import com.adictic.common.rest.Api;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;

import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_change_password);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(com.adictic.common.R.string.changePassword));
        api = ((App) this.getApplication()).getAPI();

        Funcions.closeKeyboard(findViewById(R.id.popCP_constraint), this);

        Button b_accept = findViewById(R.id.BT_popCP_accept);
        Button b_cancel = findViewById(R.id.BT_popCP_cancel);

        EditText pOld = findViewById(R.id.ET_popCP_actualPass);
        EditText p1 = findViewById(R.id.ET_popCP_newPass);
        EditText p2 = findViewById(R.id.ET_popCP_newPass2);

        TextView err_pOld = findViewById(R.id.TV_popCP_error_actualPass);
        TextView err_p2 = findViewById(R.id.TV_popCP_error_newPass2);

        b_accept.setOnClickListener(view -> {
            err_pOld.setVisibility(View.GONE);

            if(!p1.getText().toString().equals(p2.getText().toString())){
                err_p2.setVisibility(View.VISIBLE);
            } else {
                err_p2.setVisibility(View.GONE);
                ChangePassword changePassword = new ChangePassword();
                changePassword.oldPassword = Crypt.getSHA256(pOld.getText().toString().trim());
                changePassword.newPassword = Crypt.getSHA256(p1.getText().toString().trim());
                Call<String> call = api.changePassword(changePassword);

                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        super.onResponse(call, response);
                        if(response.isSuccessful()){
                            SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(ChangePasswordActivity.this);
                            assert sharedPreferences != null;
                            sharedPreferences.edit().putString(Constants.SHARED_PREFS_PASSWORD,changePassword.newPassword).apply();
                            Toast.makeText(ChangePasswordActivity.this, getString(R.string.successful_entry), Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            try {
                                assert response.errorBody() != null;
                                JSONObject obj = new JSONObject(response.errorBody().string());
                                if (obj.getString("message").trim().equals("OldPass does not match")) {
                                    err_pOld.setVisibility(View.VISIBLE);
                                } else {
                                    Toast toast = Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_sending_data), Toast.LENGTH_LONG);
                                    toast.show();
                                    System.err.println("Error desconegut HTTP en ChangePasswordActivity: " + obj.getString("message"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast toast = Toast.makeText(ChangePasswordActivity.this, getString(R.string.error_sending_data), Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
                api.changePassword(changePassword);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        b_cancel.setOnClickListener(view -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
