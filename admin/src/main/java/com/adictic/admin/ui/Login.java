package com.adictic.admin.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adictic.admin.rest.AdminApi;
import com.adictic.admin.util.AdminApp;
import com.adictic.admin.util.Funcions;
import com.adictic.common.entity.User;
import com.adictic.common.entity.UserLogin;
import com.adictic.common.util.Callback;
import com.adictic.common.util.Constants;
import com.adictic.common.util.Crypt;
import com.adictic.admin.MainActivity;
import com.adictic.admin.R;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Response;

public class Login extends AppCompatActivity {
    private final String TAG = "Login";

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        Funcions.closeKeyboard(findViewById(R.id.main_parent),Login.this);

        Button BT_login = findViewById(R.id.login_button);
        BT_login.setOnClickListener(view -> {
            FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult();

                    AdminApi adminApi = ((AdminApp) getApplicationContext()).getAPI();

                    EditText ET_username = findViewById(R.id.login_username);
                    EditText ET_password = findViewById(R.id.login_password);

                    UserLogin userLogin = new UserLogin();
                    userLogin.username = ET_username.getText().toString().trim().toLowerCase();
                    userLogin.password = Crypt.getSHA256(ET_password.getText().toString());
                    userLogin.token = Crypt.getAES(token);

                    Call<User> call = adminApi.login(userLogin);
                    call.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                    super.onResponse(call, response);
                            if(response.isSuccessful() && response.body() != null){
                                User adminLogin = response.body();
                                SharedPreferences sharedPreferences = Funcions.getEncryptedSharedPreferences(getApplicationContext());
                                assert sharedPreferences != null;
                                sharedPreferences.edit().putLong(Constants.SHARED_PREFS_IDUSER,adminLogin.id).apply();
                                sharedPreferences.edit().putLong(Constants.SHARED_PREFS_ID_ADMIN,adminLogin.adminId).apply();

                                sharedPreferences.edit().putString(Constants.SHARED_PREFS_USERNAME, userLogin.username).apply();
                                sharedPreferences.edit().putString(Constants.SHARED_PREFS_PASSWORD, userLogin.password).apply();
                                sharedPreferences.edit().putString(Constants.SHARED_PREFS_TOKEN, userLogin.token).apply();

                                Login.this.startActivity(new Intent(Login.this, MainActivity.class));
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {

                        }
                    });
                });
            });
    }
}
